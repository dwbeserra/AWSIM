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
 * Simple hybrid example mixing mobile devices and cloud resources.
 * Two tablet-class client devices execute lightweight tasks locally,
 * while two cloud VMs execute heavier tasks in the same simulation.
 */
public class AwsMobileCloudSimpleExample {
    public static void main(String[] args) throws Exception {
        Log.printLine("Starting AwsMobileCloudSimpleExample...");
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter dc = AwsDatacenterFactory.createDatacenter(
                "mobile-cloud-simple-dc",
                2,
                24,
                5000,
                65536,
                5_000_000,
                25_000,
                0.0, 0.0, 0.0, 0.0);

        AwsDatacenterBroker broker = new AwsDatacenterBroker("MobileCloudSimpleBroker");
        int brokerId = broker.getId();

        List<AwsVm> resources = new ArrayList<>();
        resources.addAll((List<AwsVm>) (List<?>) MobileNodeFactory.replicate(
                0,
                brokerId,
                AwsRegion.EU_WEST_3,
                PurchaseOption.ON_DEMAND,
                MobileProfiles.TABLET_CLIENT,
                new EbsVolumeSpec(16, 3000, 125),
                2));
        resources.addAll(AwsVmFactory.replicate(
                2,
                brokerId,
                AwsRegion.EU_WEST_3,
                PurchaseOption.ON_DEMAND,
                StandardEc2Profiles.C7I_XLARGE,
                new EbsVolumeSpec(60, 3000, 125),
                2));
        broker.submitVmList(resources);

        List<Cloudlet> localTasks = AwsCloudletFactory.many(6, 216_000_000L, 1, 16, 8, brokerId);
        List<Cloudlet> cloudTasks = new ArrayList<>();
        int baseId = localTasks.size();
        for (int i = 0; i < 6; i++) {
            cloudTasks.add(AwsCloudletFactory.createCloudlet(baseId + i, 864_000_000L, 1, 128, 64, brokerId));
        }

        List<Cloudlet> allCloudlets = new ArrayList<>();
        allCloudlets.addAll(localTasks);
        allCloudlets.addAll(cloudTasks);
        broker.submitCloudletList(allCloudlets);

        List<AwsVm> mobileOnly = resources.subList(0, 2);
        List<AwsVm> cloudOnly = resources.subList(2, 4);
        broker.bindCloudletsRoundRobin(localTasks, mobileOnly);
        broker.bindCloudletsRoundRobin(cloudTasks, cloudOnly);

        AwsEnvironment environment = new AwsEnvironment(
                AwsRegion.EU_WEST_3,
                null,
                new S3BucketSpec(5.0, 100, 100),
                0.5);

        System.out.println(new AwsSimulationRunner().run(
                "AWS Mobile+Cloud Simple Example",
                broker,
                resources,
                environment,
                new ExampleAwsPriceCatalog()).toPrettyString());
    }
}
