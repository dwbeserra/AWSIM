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

public class AwsSimulation2 {
    public static void main(String[] args) throws Exception {
        Log.printLine("Starting AwsSimulation2...");
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter dc = AwsDatacenterFactory.createDatacenter("paris-dc", 3, 96, 7200, 393216, 50_000_000, 100_000, 0.0, 0.0, 0.0, 0.0);
        AwsDatacenterBroker broker = new AwsDatacenterBroker("AwsBroker2");
        int brokerId = broker.getId();

        List<AwsVm> vms = List.of(
                AwsVmFactory.createVm(0, brokerId, AwsRegion.EU_WEST_3, PurchaseOption.ON_DEMAND, StandardEc2Profiles.HPC7G_16XLARGE, new EbsVolumeSpec(400, 3000, 125)),
                AwsVmFactory.createVm(1, brokerId, AwsRegion.EU_WEST_3, PurchaseOption.ON_DEMAND, StandardEc2Profiles.HPC7G_16XLARGE, new EbsVolumeSpec(400, 3000, 125)),
                AwsVmFactory.createVm(2, brokerId, AwsRegion.EU_WEST_3, PurchaseOption.SPOT, StandardEc2Profiles.C7I_2XLARGE, new EbsVolumeSpec(100, 3000, 125)));
        broker.submitVmList(vms);

        List<Cloudlet> cloudlets = AwsCloudletFactory.many(18, 1_800_000_000L, 1, 256, 128, brokerId);
        broker.submitCloudletList(cloudlets);

        for (int i = 0; i < cloudlets.size(); i++) {
            if (i < 8) {
                broker.bindCloudletToVm(cloudlets.get(i).getCloudletId(), vms.get(0).getId());
            } else if (i < 16) {
                broker.bindCloudletToVm(cloudlets.get(i).getCloudletId(), vms.get(1).getId());
            } else {
                broker.bindCloudletToVm(cloudlets.get(i).getCloudletId(), vms.get(2).getId());
            }
        }

        AwsEnvironment environment = new AwsEnvironment(
                AwsRegion.EU_WEST_3,
                new FsxLustreSpec(2400, 250, 500, 0),
                new S3BucketSpec(150, 5_000, 5_000),
                10,
                new SpotInterruptionAvailabilityModel(0.10, 0.30, 42L));

        AwsSimulationReport report = new AwsSimulationRunner().run(
                "AWS Simulation 2 - Explicit Cloudlet/VM binding",
                broker,
                vms,
                environment,
                new ExampleAwsPriceCatalog());

        System.out.println(report.toPrettyString());
        Log.printLine("AwsSimulation2 finished.");
    }
}
