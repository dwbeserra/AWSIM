package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioExecutor;

/**
 * Scenario focused on stochastic price variation while keeping availability unchanged.
 */
public class AwsSimulation7VariablePricing {
    public static void main(String[] args) throws Exception {
        AwsScenarioConfig cfg = new AwsScenarioConfig();
        cfg.setTitle("AWS Simulation 7 - Variable pricing");
        cfg.setRegion("eu-west-3");
        cfg.setPurchaseOption("ON_DEMAND");
        cfg.setInstanceType("c7i.2xlarge");
        cfg.setVmCount(4);
        cfg.setCloudletCount(32);
        cfg.setCloudletLength(900_000_000L);
        cfg.setEbsSizeGb(120);
        cfg.setS3StoredDataGb(100.0);
        cfg.setS3BasePutRequests(2000);
        cfg.setS3BaseGetRequests(2000);
        cfg.setFixedOutboundInternetGb(12.0);
        cfg.setVariablePricing(true);
        cfg.setPriceSeed(2026L);
        cfg.setOnDemandVolatility(0.10);
        cfg.setSpotVolatility(0.18);
        cfg.setStorageVolatility(0.06);

        System.out.println(new AwsScenarioExecutor().run(cfg).toPrettyString());
    }
}
