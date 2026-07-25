package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import awsim.AwsDatacenterBroker;
import awsim.AwsSimulationRunner;
import awsim.AwsCloudletFactory;
import awsim.AwsDatacenterFactory;
import awsim.MobileNodeFactory;
import awsim.*;
import awsim.ExampleAwsPriceCatalog;

import java.util.Calendar;
import java.util.List;

public class AwsMobileScenarioExample {
    public static void main(String[] args) throws Exception {
        Log.printLine("Starting AwsMobileScenarioExample...");
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter dc = AwsDatacenterFactory.createDatacenter("mobile-edge-dc", 2, 16, 3000, 65536, 5_000_000, 10_000, 0.0, 0.0, 0.0, 0.0);
        AwsDatacenterBroker broker = new AwsDatacenterBroker("MobileBroker");
        int brokerId = broker.getId();

        List<MobileNode> nodes = MobileNodeFactory.replicate(0, brokerId, AwsRegion.EU_WEST_3, PurchaseOption.ON_DEMAND, MobileProfiles.MOBILE_EDGE_SERVER, new EbsVolumeSpec(40, 3000, 125), 2);
        broker.submitVmList(nodes);

        List<Cloudlet> cloudlets = AwsCloudletFactory.many(10, 432_000_000L, 1, 32, 16, brokerId);
        broker.submitCloudletList(cloudlets);
        broker.bindCloudletsRoundRobin(cloudlets, nodes);

        AwsEnvironment environment = new AwsEnvironment(AwsRegion.EU_WEST_3, null, new S3BucketSpec(10, 50, 50), 1.0);
        System.out.println(new AwsSimulationRunner().run(
                "AWS Mobile Scenario Example",
                broker,
                (List<AwsVm>) (List<?>) nodes,
                environment,
                new ExampleAwsPriceCatalog()).toPrettyString());
    }
}
