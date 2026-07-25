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
import awsim.MobileNodeFactory;
import awsim.*;
import awsim.ExampleAwsPriceCatalog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * More complex hybrid scenario with mobile clients, mobile-edge servers,
 * cloud VMs, shared FSx storage and object storage in S3.
 */
public class AwsMobileCloudComplexExample {
    public static void main(String[] args) throws Exception {
        Log.printLine("Starting AwsMobileCloudComplexExample...");
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter dc = AwsDatacenterFactory.createDatacenter(
                "mobile-cloud-complex-dc",
                4,
                32,
                5000,
                131072,
                10_000_000,
                25_000,
                0.0, 0.0, 0.0, 0.0);

        AwsDatacenterBroker broker = new AwsDatacenterBroker("MobileCloudComplexBroker");
        int brokerId = broker.getId();

        List<AwsVm> allResources = new ArrayList<>();

        List<MobileNode> mobileClients = MobileNodeFactory.replicate(
                0,
                brokerId,
                AwsRegion.SA_EAST_1,
                PurchaseOption.ON_DEMAND,
                MobileProfiles.SMARTPHONE_CLIENT,
                new EbsVolumeSpec(8, 3000, 125),
                4);

        List<MobileNode> edgeServers = MobileNodeFactory.replicate(
                4,
                brokerId,
                AwsRegion.SA_EAST_1,
                PurchaseOption.ON_DEMAND,
                MobileProfiles.MOBILE_EDGE_SERVER,
                new EbsVolumeSpec(80, 3000, 125),
                2);

        List<AwsVm> cloudBackends = AwsVmFactory.replicate(
                6,
                brokerId,
                AwsRegion.SA_EAST_1,
                PurchaseOption.ON_DEMAND,
                StandardEc2Profiles.C7I_2XLARGE,
                new EbsVolumeSpec(200, 3000, 250),
                3);

        allResources.addAll((List<AwsVm>) (List<?>) mobileClients);
        allResources.addAll((List<AwsVm>) (List<?>) edgeServers);
        allResources.addAll(cloudBackends);
        broker.submitVmList(allResources);

        List<Cloudlet> clientTasks = AwsCloudletFactory.many(8, 162_000_000L, 1, 8, 4, brokerId);
        List<Cloudlet> edgeTasks = new ArrayList<>();
        int nextId = clientTasks.size();
        for (int i = 0; i < 8; i++) {
            edgeTasks.add(AwsCloudletFactory.createCloudlet(nextId + i, 576_000_000L, 1, 64, 24, brokerId));
        }
        nextId += edgeTasks.size();
        List<Cloudlet> cloudTasks = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            cloudTasks.add(AwsCloudletFactory.createCloudlet(nextId + i, 1_512_000_000L, 2, 256, 128, brokerId));
        }

        List<Cloudlet> allTasks = new ArrayList<>();
        allTasks.addAll(clientTasks);
        allTasks.addAll(edgeTasks);
        allTasks.addAll(cloudTasks);
        broker.submitCloudletList(allTasks);

        broker.bindCloudletsRoundRobin(clientTasks, mobileClients);
        broker.bindCloudletsRoundRobin(edgeTasks, edgeServers);
        broker.bindCloudletsRoundRobin(cloudTasks, cloudBackends);

        AwsEnvironment environment = new AwsEnvironment(
                AwsRegion.SA_EAST_1,
                new FsxLustreSpec(2400, 500, 1500, 200),
                new S3BucketSpec(150.0, 3000, 3000),
                18.0);

        System.out.println(new AwsSimulationRunner().run(
                "AWS Mobile+Cloud Complex Example",
                broker,
                allResources,
                environment,
                new ExampleAwsPriceCatalog()).toPrettyString());
    }
}
