package awsim;

import awsim.AwsPriceCatalogMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class MicroserviceSimulationReport {
    public static final class ServiceMetrics {
        private final String name;
        private final String instanceType;
        private final double completedRequests;
        private final double droppedRequests;
        private final double meanReplicas;
        private final int peakReplicas;
        private final double meanUtilization;
        private final double peakQueue;
        private final int scaleOutActions;
        private final int scaleInActions;
        private final double ec2Cost;

        ServiceMetrics(
                String name,
                String instanceType,
                double completedRequests,
                double droppedRequests,
                double meanReplicas,
                int peakReplicas,
                double meanUtilization,
                double peakQueue,
                int scaleOutActions,
                int scaleInActions,
                double ec2Cost) {
            this.name = name;
            this.instanceType = instanceType;
            this.completedRequests = completedRequests;
            this.droppedRequests = droppedRequests;
            this.meanReplicas = meanReplicas;
            this.peakReplicas = peakReplicas;
            this.meanUtilization = meanUtilization;
            this.peakQueue = peakQueue;
            this.scaleOutActions = scaleOutActions;
            this.scaleInActions = scaleInActions;
            this.ec2Cost = ec2Cost;
        }

        public String getName() { return name; }
        public String getInstanceType() { return instanceType; }
        public double getCompletedRequests() { return completedRequests; }
        public double getDroppedRequests() { return droppedRequests; }
        public double getMeanReplicas() { return meanReplicas; }
        public int getPeakReplicas() { return peakReplicas; }
        public double getMeanUtilization() { return meanUtilization; }
        public double getPeakQueue() { return peakQueue; }
        public int getScaleOutActions() { return scaleOutActions; }
        public int getScaleInActions() { return scaleInActions; }
        public double getEc2Cost() { return ec2Cost; }
    }

    public static final class ScalingEvent {
        private final int timeSeconds;
        private final String service;
        private final String action;
        private final int amount;
        private final int desiredReplicas;

        ScalingEvent(int timeSeconds, String service, String action, int amount, int desiredReplicas) {
            this.timeSeconds = timeSeconds;
            this.service = service;
            this.action = action;
            this.amount = amount;
            this.desiredReplicas = desiredReplicas;
        }

        public int getTimeSeconds() { return timeSeconds; }
        public String getService() { return service; }
        public String getAction() { return action; }
        public int getAmount() { return amount; }
        public int getDesiredReplicas() { return desiredReplicas; }
    }

    private final String title;
    private final int durationSeconds;
    private final double offeredRequests;
    private final double completedRequests;
    private final double droppedRequests;
    private final double throughputRps;
    private final double meanResponseSeconds;
    private final double approximateSloViolationFraction;
    private final double totalEc2Cost;
    private final List<ServiceMetrics> services;
    private final List<ScalingEvent> scalingEvents;
    private final AwsPriceCatalogMetadata catalogMetadata;

    MicroserviceSimulationReport(
            String title,
            int durationSeconds,
            double offeredRequests,
            double completedRequests,
            double droppedRequests,
            double throughputRps,
            double meanResponseSeconds,
            double approximateSloViolationFraction,
            double totalEc2Cost,
            List<ServiceMetrics> services,
            List<ScalingEvent> scalingEvents,
            AwsPriceCatalogMetadata catalogMetadata) {
        this.title = title;
        this.durationSeconds = durationSeconds;
        this.offeredRequests = offeredRequests;
        this.completedRequests = completedRequests;
        this.droppedRequests = droppedRequests;
        this.throughputRps = throughputRps;
        this.meanResponseSeconds = meanResponseSeconds;
        this.approximateSloViolationFraction = approximateSloViolationFraction;
        this.totalEc2Cost = totalEc2Cost;
        this.services = Collections.unmodifiableList(new ArrayList<>(services));
        this.scalingEvents = Collections.unmodifiableList(new ArrayList<>(scalingEvents));
        this.catalogMetadata = catalogMetadata;
    }

    public String getTitle() { return title; }
    public int getDurationSeconds() { return durationSeconds; }
    public double getOfferedRequests() { return offeredRequests; }
    public double getCompletedRequests() { return completedRequests; }
    public double getDroppedRequests() { return droppedRequests; }
    public double getThroughputRps() { return throughputRps; }
    public double getMeanResponseSeconds() { return meanResponseSeconds; }
    public double getApproximateSloViolationFraction() { return approximateSloViolationFraction; }
    public double getTotalEc2Cost() { return totalEc2Cost; }
    public List<ServiceMetrics> getServices() { return services; }
    public List<ScalingEvent> getScalingEvents() { return scalingEvents; }
    public AwsPriceCatalogMetadata getCatalogMetadata() { return catalogMetadata; }

    public String toPrettyString() {
        StringBuilder out = new StringBuilder();
        out.append("\n==============================\n")
                .append(title).append("\n")
                .append("==============================\n")
                .append(String.format(Locale.ROOT, "Duration           : %d s%n", durationSeconds))
                .append(String.format(Locale.ROOT, "Offered requests   : %.0f%n", offeredRequests))
                .append(String.format(Locale.ROOT, "Completed requests : %.0f%n", completedRequests))
                .append(String.format(Locale.ROOT, "Dropped requests   : %.0f%n", droppedRequests))
                .append(String.format(Locale.ROOT, "Throughput         : %.4f req/s%n", throughputRps))
                .append(String.format(Locale.ROOT, "Mean response est. : %.4f s%n", meanResponseSeconds))
                .append(String.format(Locale.ROOT, "Approx. SLO breach : %.2f%%%n", approximateSloViolationFraction * 100.0))
                .append(String.format(Locale.ROOT, "EC2 total cost     : $%.6f%n", totalEc2Cost))
                .append("Price catalog      : ").append(catalogMetadata.getCatalogId()).append("\n");
        for (ServiceMetrics service : services) {
            out.append(String.format(
                    Locale.ROOT,
                    "  %-12s %-13s avg/peak replicas %.2f/%d, util %.1f%%, "
                            + "queue peak %.0f, scale +/- %d/%d, cost $%.6f%n",
                    service.getName(),
                    service.getInstanceType(),
                    service.getMeanReplicas(),
                    service.getPeakReplicas(),
                    service.getMeanUtilization() * 100.0,
                    service.getPeakQueue(),
                    service.getScaleOutActions(),
                    service.getScaleInActions(),
                    service.getEc2Cost()));
        }
        return out.toString();
    }

    public static String csvHeader() {
        return "title,duration_seconds,offered_requests,completed_requests,dropped_requests,"
                + "throughput_rps,mean_response_seconds,approx_slo_violation_fraction,"
                + "ec2_total_usd,scaling_events,catalog_id,catalog_date";
    }

    public String toCsvRow() {
        return csv(title) + "," + durationSeconds + ","
                + number(offeredRequests) + "," + number(completedRequests) + ","
                + number(droppedRequests) + "," + number(throughputRps) + ","
                + number(meanResponseSeconds) + ","
                + number(approximateSloViolationFraction) + ","
                + number(totalEc2Cost) + "," + scalingEvents.size() + ","
                + csv(catalogMetadata.getCatalogId()) + ","
                + csv(catalogMetadata.getRetrievedAt());
    }

    private static String number(double value) {
        return String.format(Locale.ROOT, "%.8f", value);
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
