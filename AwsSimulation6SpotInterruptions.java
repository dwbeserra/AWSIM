package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioExecutor;

/**
 * Scenario focused on service interruptions / availability penalties for Spot instances.
 */
public class AwsSimulation6SpotInterruptions {
    public static void main(String[] args) throws Exception {
        AwsScenarioConfig cfg = new AwsScenarioConfig();
        cfg.setTitle("AWS Simulation 6 - Spot interruptions");
        cfg.setRegion("us-east-1");
        cfg.setPurchaseOption("SPOT");
        cfg.setInstanceType("c7i.2xlarge");
        cfg.setVmCount(6);
        cfg.setHostCount(3);
        cfg.setHostPes(32);
        cfg.setHostPeMips(5000);
        cfg.setCloudletCount(36);
        cfg.setCloudletLength(1_080_000_000L);
        cfg.setCloudletPes(1);
        cfg.setCloudletFileSizeMb(64);
        cfg.setCloudletOutputSizeMb(48);
        cfg.setEbsSizeGb(120);
        cfg.setFsxStorageGb(1800);
        cfg.setFsxThroughputMbps(250);
        cfg.setS3StoredDataGb(80.0);
        cfg.setS3BasePutRequests(2000);
        cfg.setS3BaseGetRequests(2000);
        cfg.setFixedOutboundInternetGb(10.0);
        cfg.setSpotAvailability(true);
        cfg.setInterruptionRatePerHour(0.20);
        cfg.setRestartOverheadHours(0.50);
        cfg.setCheckpointIntervalHours(1.0);

        System.out.println(new AwsScenarioExecutor().run(cfg).toPrettyString());
    }
}
