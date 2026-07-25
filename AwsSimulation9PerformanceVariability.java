package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioExecutor;
import awsim.AwsSimulationReport;
import org.cloudbus.cloudsim.Log;

/**
 * Multi-seed experiment for execution-time variability. Each repetition is
 * individually reproducible and exports a machine-readable CSV row.
 */
public final class AwsSimulation9PerformanceVariability {
    private AwsSimulation9PerformanceVariability() {
    }

    public static void main(String[] args) throws Exception {
        int repetitions = args.length == 0 ? 30 : Integer.parseInt(args[0]);
        if (repetitions <= 0) {
            throw new IllegalArgumentException("repetitions must be > 0");
        }
        Log.disable();
        System.out.println(AwsSimulationReport.csvHeader() + ",performance_seed");
        for (int i = 0; i < repetitions; i++) {
            long seed = 10_000L + i;
            AwsScenarioConfig cfg = new AwsScenarioConfig();
            cfg.setTitle("AWS Simulation 9 - Performance variability - seed " + seed);
            cfg.setRegion("us-east-1");
            cfg.setInstanceType("c7i.2xlarge");
            cfg.setVmCount(4);
            cfg.setCloudletCount(32);
            cfg.setCloudletLength(900_000_000L);
            cfg.setEbsSizeGb(120);
            cfg.setFsxStorageGb(1200);
            cfg.setFsxThroughputMbps(250);
            cfg.setS3StoredDataGb(100.0);
            cfg.setS3BasePutRequests(2000);
            cfg.setS3BaseGetRequests(2000);
            cfg.setFixedOutboundInternetGb(12.0);
            cfg.setPerformanceVariability(true);
            cfg.setPerformanceSeed(seed);
            cfg.setPerformanceCv(0.12);
            cfg.setPerformanceMinMultiplier(0.65);
            cfg.setPerformanceMaxMultiplier(1.50);

            AwsSimulationReport report = new AwsScenarioExecutor().run(cfg);
            System.out.println(report.toCsvRow() + "," + seed);
        }
    }
}
