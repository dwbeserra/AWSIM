package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioExecutor;

public class AwsSimulation5InstanceSensitivity {
    public static void main(String[] args) throws Exception {
        String[] instanceTypes = {"c7i.xlarge", "c7i.2xlarge", "c7i.4xlarge"};
        AwsScenarioExecutor executor = new AwsScenarioExecutor();

        for (String instanceType : instanceTypes) {
            AwsScenarioConfig cfg = new AwsScenarioConfig();
            cfg.setTitle("AWS Simulation 5 - Instance sensitivity - " + instanceType);
            cfg.setInstanceType(instanceType);
            cfg.setVmCount(4);
            cfg.setHostPeMips(5200);
            cfg.setCloudletCount(32);
            cfg.setCloudletLength(900_000_000L);
            cfg.setEbsSizeGb(120);
            cfg.setFsxStorageGb(1200);
            cfg.setFsxThroughputMbps(250);
            cfg.setS3StoredDataGb(100);
            cfg.setS3BasePutRequests(2000);
            cfg.setS3BaseGetRequests(2000);
            cfg.setFixedOutboundInternetGb(12);
            System.out.println(executor.run(cfg).toPrettyString());
        }
    }
}
