package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import awsim.*;
import awsim.AwsPriceCatalog;
import awsim.ExampleAwsPriceCatalog;
import awsim.CostBreakdown;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AwsCostCalculator {
    public AwsAccountingResult calculate(
            List<AwsVm> vms,
            List<Cloudlet> finishedCloudlets,
            AwsEnvironment environment,
            AwsPriceCatalog catalog) {
        Map<Integer, Double> firstStart = new HashMap<>();
        Map<Integer, Double> lastFinish = new HashMap<>();

        double totalPutRequests = environment.getS3BucketSpec() != null ? environment.getS3BucketSpec().getBasePutRequests() : 0.0;
        double totalGetRequests = environment.getS3BucketSpec() != null ? environment.getS3BucketSpec().getBaseGetRequests() : 0.0;
        double totalTransferOutGb = environment.getFixedOutboundInternetGb();
        double firstTraceStartSeconds = Double.POSITIVE_INFINITY;
        double lastTraceFinishSeconds = 0.0;
        double traceMakespanHours = 0.0;
        double adjustedMakespanHours = 0.0;
        int interruptions = 0;
        double penaltyHours = 0.0;

        for (Cloudlet cloudlet : finishedCloudlets) {
            int vmId = cloudlet.getVmId();
            double start = cloudlet.getExecStartTime();
            double finish = cloudlet.getFinishTime();
            firstStart.merge(vmId, start, Math::min);
            lastFinish.merge(vmId, finish, Math::max);
            firstTraceStartSeconds = Math.min(firstTraceStartSeconds, start);
            lastTraceFinishSeconds = Math.max(lastTraceFinishSeconds, finish);

            totalPutRequests += 1.0;
            totalGetRequests += 1.0;
            double outputMiB = cloudlet instanceof AwsCloudlet
                    ? ((AwsCloudlet) cloudlet).getOutputSizeMiB()
                    : cloudlet.getCloudletOutputSize();
            totalTransferOutGb += outputMiB / 1024.0;
        }
        if (firstTraceStartSeconds < Double.POSITIVE_INFINITY) {
            traceMakespanHours = SimulationTime.secondsToHours(
                    Math.max(0.0, lastTraceFinishSeconds - firstTraceStartSeconds));
        }

        double ec2 = 0.0;
        double ebs = 0.0;
        for (AwsVm vm : vms) {
            if (!firstStart.containsKey(vm.getId())) {
                continue;
            }
            double runtimeSeconds = Math.max(
                    0.0,
                    lastFinish.get(vm.getId()) - firstStart.get(vm.getId()));
            double runtimeHours = SimulationTime.secondsToHours(runtimeSeconds);
            AvailabilityOutcome outcome = environment.getAvailabilityModel().assess(vm, runtimeHours);
            double adjustedRuntimeHours = Math.max(runtimeHours, outcome.getAdjustedRuntimeHours());
            interruptions += outcome.getInterruptionCount();
            penaltyHours += Math.max(0.0, adjustedRuntimeHours - runtimeHours);
            adjustedMakespanHours = Math.max(
                    adjustedMakespanHours,
                    SimulationTime.secondsToHours(
                            firstStart.get(vm.getId()) - firstTraceStartSeconds)
                            + adjustedRuntimeHours);

            if (!vm.isBillable()) {
                continue;
            }

            long billableSeconds = Math.max(60L, (long) Math.ceil(adjustedRuntimeHours * 3600.0));
            ec2 += catalog.ec2Hourly(vm.getRegion(), vm.getInstanceProfile().getInstanceType(), vm.getPurchaseOption())
                    * (billableSeconds / 3600.0);

            if (vm.getEbsVolumeSpec() != null) {
                ebs += catalog.ebsMonthly(vm.getRegion(), vm.getEbsVolumeSpec())
                        * ((billableSeconds / 3600.0) / ExampleAwsPriceCatalog.HOURS_PER_MONTH);
            }
        }

        double fsx = 0.0;
        if (environment.getFsxLustreSpec() != null) {
            fsx = catalog.fsxMonthly(environment.getRegion(), environment.getFsxLustreSpec())
                    * (adjustedMakespanHours / ExampleAwsPriceCatalog.HOURS_PER_MONTH);
        }

        double s3Storage = 0.0;
        double s3Requests = 0.0;
        if (environment.getS3BucketSpec() != null) {
            s3Storage = catalog.s3Monthly(environment.getRegion(), environment.getS3BucketSpec())
                    * (adjustedMakespanHours / ExampleAwsPriceCatalog.HOURS_PER_MONTH);
            s3Requests = (totalPutRequests / 1000.0) * catalog.s3PutPerThousand(environment.getRegion())
                    + (totalGetRequests / 1000.0) * catalog.s3GetPerThousand(environment.getRegion());
        }

        double transfer = totalTransferOutGb <= 0.0
                ? 0.0
                : totalTransferOutGb * catalog.transferOutPerGb(environment.getRegion());
        CostBreakdown breakdown = new CostBreakdown(
                ec2,
                ebs,
                fsx,
                s3Storage,
                s3Requests,
                transfer,
                interruptions,
                penaltyHours);
        return new AwsAccountingResult(
                breakdown,
                traceMakespanHours,
                Math.max(traceMakespanHours, adjustedMakespanHours));
    }

    public CostBreakdown compute(
            List<AwsVm> vms,
            List<Cloudlet> finishedCloudlets,
            AwsEnvironment environment,
            AwsPriceCatalog catalog) {
        return calculate(vms, finishedCloudlets, environment, catalog).getCostBreakdown();
    }

    public double computeMakespanHours(List<Cloudlet> finishedCloudlets) {
        double firstStart = Double.POSITIVE_INFINITY;
        double lastFinish = 0.0;
        for (Cloudlet cloudlet : finishedCloudlets) {
            firstStart = Math.min(firstStart, cloudlet.getExecStartTime());
            lastFinish = Math.max(lastFinish, cloudlet.getFinishTime());
        }
        if (firstStart == Double.POSITIVE_INFINITY) {
            return 0.0;
        }
        return SimulationTime.secondsToHours(Math.max(0.0, lastFinish - firstStart));
    }

    public double computeMakespanHours(List<AwsVm> vms, List<Cloudlet> finishedCloudlets, AwsEnvironment environment) {
        Map<Integer, Double> firstStart = new HashMap<>();
        Map<Integer, Double> lastFinish = new HashMap<>();
        double firstTraceStartSeconds = Double.POSITIVE_INFINITY;
        double lastTraceFinishSeconds = 0.0;
        for (Cloudlet cloudlet : finishedCloudlets) {
            int vmId = cloudlet.getVmId();
            firstStart.merge(vmId, cloudlet.getExecStartTime(), Math::min);
            lastFinish.merge(vmId, cloudlet.getFinishTime(), Math::max);
            firstTraceStartSeconds = Math.min(
                    firstTraceStartSeconds, cloudlet.getExecStartTime());
            lastTraceFinishSeconds = Math.max(
                    lastTraceFinishSeconds, cloudlet.getFinishTime());
        }
        if (firstTraceStartSeconds == Double.POSITIVE_INFINITY) {
            return 0.0;
        }
        double traceMakespanHours = SimulationTime.secondsToHours(
                Math.max(0.0, lastTraceFinishSeconds - firstTraceStartSeconds));
        double adjustedMakespanHours = traceMakespanHours;
        for (AwsVm vm : vms) {
            if (!firstStart.containsKey(vm.getId())) {
                continue;
            }
            double runtimeHours = SimulationTime.secondsToHours(
                    Math.max(0.0, lastFinish.get(vm.getId()) - firstStart.get(vm.getId())));
            AvailabilityOutcome outcome = environment.getAvailabilityModel().assess(vm, runtimeHours);
            adjustedMakespanHours = Math.max(
                    adjustedMakespanHours,
                    SimulationTime.secondsToHours(
                            firstStart.get(vm.getId()) - firstTraceStartSeconds)
                            + outcome.getAdjustedRuntimeHours());
        }
        return adjustedMakespanHours;
    }
}
