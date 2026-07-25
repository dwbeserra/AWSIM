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

public class AwsSimulation3 {
    public static void main(String[] args) throws Exception {
        ExampleAwsPriceCatalog catalog = new ExampleAwsPriceCatalog();

        for (AwsRegion region : AwsRegion.values()) {
            Log.printLine("Starting regional simulation for " + region.getLabel());
            CloudSim.init(1, Calendar.getInstance(), false);

            Datacenter dc = AwsDatacenterFactory.createDatacenter(
                    region.getCode() + "-dc", 3, 16, 5000, 262144, 10_000_000, 50_000,
                    0.0, 0.0, 0.0, 0.0);
            AwsDatacenterBroker broker = new AwsDatacenterBroker("Broker-" + region.getCode());
            int brokerId = broker.getId();

            List<AwsVm> vms = AwsVmFactory.replicate(
                    brokerId,
                    region,
                    PurchaseOption.ON_DEMAND,
                    StandardEc2Profiles.C7I_2XLARGE,
                    new EbsVolumeSpec(120, 3000, 125),
                    4);
            broker.submitVmList(vms);

            List<Cloudlet> cloudlets = AwsCloudletFactory.many(32, 900_000_000L, 1, 128, 64, brokerId);
            broker.submitCloudletList(cloudlets);
            broker.bindCloudletsRoundRobin(cloudlets, vms);

            AwsEnvironment environment = new AwsEnvironment(region, null, new S3BucketSpec(100, 2_000, 2_000), 12);
            AwsSimulationReport report = new AwsSimulationRunner().run(
                    "AWS Simulation 3 - Regional comparison for " + region.getLabel(),
                    broker, vms, environment, catalog);

            System.out.println(report.toPrettyString());
        }
    }
}
