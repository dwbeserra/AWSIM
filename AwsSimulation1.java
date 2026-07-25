package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import awsim.AwsDatacenterBroker;
import awsim.AwsSimulationRunner;
import awsim.AwsCloudletFactory;
import awsim.AwsDatacenterFactory;
import awsim.AwsVmFactory;
import awsim.*;
import awsim.ExampleAwsPriceCatalog;
import awsim.AwsSimulationReport;

import java.util.Calendar;
import java.util.List;

public class AwsSimulation1 {
    public static void main(String[] args) throws Exception {
        Log.printLine("Starting AwsSimulation1...");
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter dc = AwsDatacenterFactory.createDatacenter("us-east-1-dc", 4, 16, 5000, 262144, 10_000_000, 100_000, 0.0, 0.0, 0.0, 0.0);
        AwsDatacenterBroker broker = new AwsDatacenterBroker("AwsBroker1");
        int brokerId = broker.getId();

        List<AwsVm> vms = AwsVmFactory.replicate(
                brokerId,
                AwsRegion.US_EAST_1,
                PurchaseOption.ON_DEMAND,
                StandardEc2Profiles.C7I_2XLARGE,
                new EbsVolumeSpec(200, 3000, 125),
                6);
        broker.submitVmList(vms);

        List<Cloudlet> cloudlets = AwsCloudletFactory.many(60, 720_000_000L, 1, 512, 256, brokerId);
        broker.submitCloudletList(cloudlets);
        broker.bindCloudletsRoundRobin(cloudlets, vms);

        AwsEnvironment environment = new AwsEnvironment(
                AwsRegion.US_EAST_1,
                null,
                new S3BucketSpec(500, 10_000, 10_000),
                30);

        AwsSimulationReport report = new AwsSimulationRunner().run(
                "AWS Simulation 1 - On-Demand EC2 + EBS + S3",
                broker,
                vms,
                environment,
                new ExampleAwsPriceCatalog());

        System.out.println(report.toPrettyString());
        Log.printLine("AwsSimulation1 finished.");
    }
}
