package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioExecutor;

/**
 * Combined scenario with Spot interruptions and stochastic price variation.
 */
public class AwsSimulation8SpotAndVariablePricing {
    public static void main(String[] args) throws Exception {
        AwsScenarioConfig cfg = new AwsScenarioConfig();
        cfg.setTitle("AWS Simulation 8 - Spot interruptions + variable pricing");
        cfg.setRegion("sa-east-1");
        cfg.setPurchaseOption("SPOT");
        cfg.setInstanceType("c7i.xlarge");
        cfg.setVmCount(5);
        cfg.setHostCount(3);
        cfg.setCloudletCount(30);
        cfg.setCloudletLength(1_008_000_000L);
        cfg.setEbsSizeGb(80);
        cfg.setFsxStorageGb(900);
        cfg.setFsxThroughputMbps(250);
        cfg.setS3StoredDataGb(60.0);
        cfg.setS3BasePutRequests(1500);
        cfg.setS3BaseGetRequests(1500);
        cfg.setFixedOutboundInternetGb(8.0);
        cfg.setVariablePricing(true);
        cfg.setPriceSeed(2027L);
        cfg.setOnDemandVolatility(0.08);
        cfg.setSpotVolatility(0.22);
        cfg.setStorageVolatility(0.08);
        cfg.setSpotAvailability(true);
        cfg.setInterruptionRatePerHour(0.18);
        cfg.setRestartOverheadHours(0.40);
        cfg.setCheckpointIntervalHours(0.75);

        System.out.println(new AwsScenarioExecutor().run(cfg).toPrettyString());
    }
}
