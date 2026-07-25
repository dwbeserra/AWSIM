package awsim;

import awsim.AwsRegion;
import awsim.Ec2InstanceProfile;
import awsim.Ec2ProfileCatalog;
import awsim.PurchaseOption;
import awsim.AwsOfficialPriceCache;
import awsim.AwsPriceCatalog;
import awsim.AwsPriceListApiCatalog;
import awsim.ExampleAwsPriceCatalog;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Request-driven, discrete-time microservice chain with target-tracking
 * autoscaling.
 *
 * <p>Each service has its own queue, EC2 profile, replica bounds, target
 * utilization, startup delay, and cooldowns. Completed work flows to the next
 * service in the configured linear chain. This model is separate from
 * CloudSim's finite-Cloudlet broker because CloudSim 4.0 does not provide an
 * ECS control plane; it deliberately reuses AWSIM's instance mapping, units,
 * price catalogs, and provenance.
 */
public final class MicroserviceAutoscalingSimulator {
    private static final class Replica {
        final int startSeconds;

        Replica(int startSeconds) {
            this.startSeconds = startSeconds;
        }
    }

    private static final class Pending {
        final int readySeconds;
        final int count;

        Pending(int readySeconds, int count) {
            this.readySeconds = readySeconds;
            this.count = count;
        }
    }

    private static final class State {
        final MicroserviceSpec spec;
        final Ec2InstanceProfile profile;
        final double hourlyPrice;
        final List<Replica> active = new ArrayList<>();
        final List<Pending> pending = new ArrayList<>();
        double queue;
        double queueArea;
        double completed;
        double dropped;
        double processedCapacity;
        double totalCapacity;
        double activeReplicaSeconds;
        double billedCost;
        double peakQueue;
        int peakReplicas;
        int scaleOutActions;
        int scaleInActions;
        int lastScaleOut = Integer.MIN_VALUE / 2;
        int lastScaleIn = Integer.MIN_VALUE / 2;

        State(MicroserviceSpec spec, Ec2InstanceProfile profile, double hourlyPrice) {
            this.spec = spec;
            this.profile = profile;
            this.hourlyPrice = hourlyPrice;
            for (int i = 0; i < spec.getInitialReplicas(); i++) {
                active.add(new Replica(0));
            }
            peakReplicas = active.size();
        }

        double capacityPerReplicaRps() {
            return profile.getMips() * profile.getPes() / spec.getRequestMi();
        }

        int pendingCount() {
            int count = 0;
            for (Pending item : pending) count += item.count;
            return count;
        }
    }

    public MicroserviceSimulationReport run(MicroserviceScenarioConfig cfg) throws Exception {
        MicroserviceScenarioValidator.requireValid(cfg);
        AwsRegion region = parseRegion(cfg.getRegion());
        PurchaseOption option = PurchaseOption.valueOf(cfg.getPurchaseOption().toUpperCase());
        AwsPriceCatalog catalog = resolveCatalog(cfg, region, option);

        List<State> states = new ArrayList<>();
        for (MicroserviceSpec spec : cfg.getServices()) {
            Ec2InstanceProfile profile = Ec2ProfileCatalog.standard().require(spec.getInstanceType());
            states.add(new State(
                    spec,
                    profile,
                    catalog.ec2Hourly(region, spec.getInstanceType(), option)));
        }

        List<MicroserviceSimulationReport.ScalingEvent> events = new ArrayList<>();
        double offeredTotal = 0.0;
        double completedEndToEnd = 0.0;
        double violatingOffered = 0.0;
        double baseServiceSeconds = 0.0;
        for (State state : states) {
            baseServiceSeconds += state.spec.getRequestMi()
                    / (state.profile.getMips() * state.profile.getPes());
        }

        int stepIndex = 0;
        for (int start = 0; start < cfg.getDurationSeconds(); start += cfg.getIntervalSeconds()) {
            int dt = Math.min(cfg.getIntervalSeconds(), cfg.getDurationSeconds() - start);
            activateReady(states, start, events);
            double external = cfg.getArrivalRatesRps().get(
                    stepIndex % cfg.getArrivalRatesRps().size()) * dt;
            offeredTotal += external;
            double downstream = external;

            for (State state : states) {
                double offeredToService = downstream;
                state.queue += offeredToService;
                if (cfg.getMaxQueueRequests() > 0.0
                        && state.queue > cfg.getMaxQueueRequests()) {
                    double dropped = state.queue - cfg.getMaxQueueRequests();
                    state.queue = cfg.getMaxQueueRequests();
                    state.dropped += dropped;
                }
                double beforeProcessing = state.queue;
                double capacity = state.active.size() * state.capacityPerReplicaRps() * dt;
                double processed = Math.min(beforeProcessing, capacity);
                state.queue -= processed;
                state.completed += processed;
                state.processedCapacity += processed;
                state.totalCapacity += capacity;
                state.queueArea += state.queue * dt;
                state.peakQueue = Math.max(state.peakQueue, state.queue);
                state.activeReplicaSeconds += state.active.size() * (double) dt;
                state.peakReplicas = Math.max(state.peakReplicas, state.active.size());
                downstream = processed;

                int desired = desiredReplicas(state, beforeProcessing, dt);
                decideScaling(
                        state,
                        desired,
                        start + dt,
                        cfg,
                        events);
            }
            completedEndToEnd += downstream;

            double estimatedResponse = baseServiceSeconds;
            for (State state : states) {
                double capacityRps = state.active.size() * state.capacityPerReplicaRps();
                if (capacityRps > 0.0) {
                    estimatedResponse += state.queue / capacityRps;
                }
            }
            if (estimatedResponse > cfg.getSloSeconds()) {
                violatingOffered += external;
            }
            stepIndex++;
        }

        double droppedTotal = 0.0;
        double queueAreaTotal = 0.0;
        double costTotal = 0.0;
        List<MicroserviceSimulationReport.ServiceMetrics> serviceMetrics = new ArrayList<>();
        for (State state : states) {
            terminateAll(state, cfg.getDurationSeconds());
            droppedTotal += state.dropped;
            queueAreaTotal += state.queueArea;
            costTotal += state.billedCost;
            serviceMetrics.add(new MicroserviceSimulationReport.ServiceMetrics(
                    state.spec.getName(),
                    state.spec.getInstanceType(),
                    state.completed,
                    state.dropped,
                    state.activeReplicaSeconds / cfg.getDurationSeconds(),
                    state.peakReplicas,
                    state.totalCapacity == 0.0 ? 0.0 : state.processedCapacity / state.totalCapacity,
                    state.peakQueue,
                    state.scaleOutActions,
                    state.scaleInActions,
                    state.billedCost));
        }

        double meanResponse = baseServiceSeconds
                + (completedEndToEnd <= 0.0 ? 0.0 : queueAreaTotal / completedEndToEnd);
        return new MicroserviceSimulationReport(
                cfg.getTitle(),
                cfg.getDurationSeconds(),
                offeredTotal,
                completedEndToEnd,
                droppedTotal,
                completedEndToEnd / cfg.getDurationSeconds(),
                meanResponse,
                offeredTotal <= 0.0 ? 0.0 : violatingOffered / offeredTotal,
                costTotal,
                serviceMetrics,
                events,
                catalog.getMetadata());
    }

    private void activateReady(
            List<State> states,
            int now,
            List<MicroserviceSimulationReport.ScalingEvent> events) {
        for (State state : states) {
            List<Pending> remaining = new ArrayList<>();
            for (Pending item : state.pending) {
                if (item.readySeconds <= now) {
                    for (int i = 0; i < item.count; i++) {
                        state.active.add(new Replica(now));
                    }
                    state.peakReplicas = Math.max(state.peakReplicas, state.active.size());
                    events.add(new MicroserviceSimulationReport.ScalingEvent(
                            now,
                            state.spec.getName(),
                            "ACTIVATE",
                            item.count,
                            state.active.size()));
                } else {
                    remaining.add(item);
                }
            }
            state.pending.clear();
            state.pending.addAll(remaining);
        }
    }

    private int desiredReplicas(State state, double demandRequests, int dt) {
        double targetCapacityPerReplica =
                state.capacityPerReplicaRps() * state.spec.getTargetUtilization();
        int desired = targetCapacityPerReplica <= 0.0
                ? state.spec.getMaxReplicas()
                : (int) Math.ceil((demandRequests / dt) / targetCapacityPerReplica);
        return Math.max(
                state.spec.getMinReplicas(),
                Math.min(state.spec.getMaxReplicas(), desired));
    }

    private void decideScaling(
            State state,
            int desired,
            int decisionTime,
            MicroserviceScenarioConfig cfg,
            List<MicroserviceSimulationReport.ScalingEvent> events) {
        int provisioned = state.active.size() + state.pendingCount();
        if (desired > provisioned
                && decisionTime - state.lastScaleOut >= cfg.getScaleOutCooldownSeconds()) {
            int add = desired - provisioned;
            state.pending.add(new Pending(
                    decisionTime + cfg.getStartupDelaySeconds(),
                    add));
            state.lastScaleOut = decisionTime;
            state.scaleOutActions++;
            events.add(new MicroserviceSimulationReport.ScalingEvent(
                    decisionTime,
                    state.spec.getName(),
                    "SCALE_OUT",
                    add,
                    desired));
        } else if (desired < state.active.size()
                && state.pending.isEmpty()
                && decisionTime - state.lastScaleIn >= cfg.getScaleInCooldownSeconds()) {
            int remove = state.active.size() - desired;
            for (int i = 0; i < remove; i++) {
                Replica replica = state.active.remove(state.active.size() - 1);
                bill(state, replica, decisionTime);
            }
            state.lastScaleIn = decisionTime;
            state.scaleInActions++;
            events.add(new MicroserviceSimulationReport.ScalingEvent(
                    decisionTime,
                    state.spec.getName(),
                    "SCALE_IN",
                    remove,
                    desired));
        }
    }

    private void terminateAll(State state, int endSeconds) {
        for (Replica replica : state.active) {
            bill(state, replica, endSeconds);
        }
        state.active.clear();
    }

    private void bill(State state, Replica replica, int endSeconds) {
        int lifetime = Math.max(0, endSeconds - replica.startSeconds);
        int billable = Math.max(60, lifetime);
        state.billedCost += state.hourlyPrice * (billable / 3600.0);
    }

    private AwsPriceCatalog resolveCatalog(
            MicroserviceScenarioConfig cfg,
            AwsRegion region,
            PurchaseOption option) throws Exception {
        String mode = cfg.getPriceCatalogMode() == null
                ? "ILLUSTRATIVE"
                : cfg.getPriceCatalogMode().trim().toUpperCase();
        if ("CACHE".equals(mode)) {
            return new AwsPriceListApiCatalog(Path.of(cfg.getPriceCatalogFile()));
        }
        if ("OFFICIAL_AUTO".equals(mode)) {
            Set<String> instances = new LinkedHashSet<>();
            for (MicroserviceSpec service : cfg.getServices()) {
                instances.add(service.getInstanceType());
            }
            AwsOfficialPriceCache.Requirements requirements =
                    new AwsOfficialPriceCache.Requirements(
                            instances,
                            option,
                            false,
                            false,
                            false,
                            false);
            Path cache = AwsOfficialPriceCache.refreshIfStale(
                    Path.of(cfg.getOfficialPriceCacheFile()),
                    region,
                    requirements,
                    cfg.getOfficialPriceCacheTtlHours());
            return new AwsPriceListApiCatalog(cache);
        }
        return new ExampleAwsPriceCatalog();
    }

    private AwsRegion parseRegion(String value) {
        for (AwsRegion region : AwsRegion.values()) {
            if (region.name().equalsIgnoreCase(value)
                    || region.getCode().equalsIgnoreCase(value)) {
                return region;
            }
        }
        throw new IllegalArgumentException("Unknown AWS region: " + value);
    }
}
