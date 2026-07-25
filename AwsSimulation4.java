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

public class AwsSimulation4 {
    public static void main(String[] args) throws Exception {
        Log.printLine("Starting AwsSimulation4...");
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter dc = AwsDatacenterFactory.createDatacenter("tokyo-dc", 8, 64, 7000, 1048576, 50_000_000, 200_000, 0.0, 0.0, 0.0, 0.0);
        AwsDatacenterBroker broker = new AwsDatacenterBroker("AwsBroker4");
        int brokerId = broker.getId();

        List<AwsVm> vms = AwsVmFactory.replicate(
                brokerId,
                AwsRegion.AP_NORTHEAST_1,
                PurchaseOption.ON_DEMAND,
                StandardEc2Profiles.HPC7G_16XLARGE,
                new EbsVolumeSpec(500, 6000, 250),
                8);
        broker.submitVmList(vms);

        List<Cloudlet> cloudlets = AwsCloudletFactory.many(120, 2_880_000_000L, 2, 1024, 512, brokerId);
        broker.submitCloudletList(cloudlets);
        broker.bindCloudletsSequentialBlocks(cloudlets, vms, 15);

        AwsEnvironment environment = new AwsEnvironment(
                AwsRegion.AP_NORTHEAST_1,
                new FsxLustreSpec(4800, 500, 1500, 1200),
                new S3BucketSpec(900, 25_000, 25_000),
                75);

        AwsSimulationReport report = new AwsSimulationRunner().run(
                "AWS Simulation 4 - FSx + S3 + many Cloudlets",
                broker,
                vms,
                environment,
                new ExampleAwsPriceCatalog());

        System.out.println(report.toPrettyString());
        Log.printLine("AwsSimulation4 finished.");
    }
}
