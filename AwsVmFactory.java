package awsim;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import awsim.*;

import java.util.ArrayList;
import java.util.List;

public final class AwsVmFactory {
    private AwsVmFactory() {
    }

    public static AwsVm createVm(int id, int brokerId, AwsRegion region, PurchaseOption option, Ec2InstanceProfile profile, EbsVolumeSpec ebs) {
        return new AwsVm(id, brokerId, region, option, profile, ebs, "Xen", new CloudletSchedulerTimeShared());
    }

    public static List<AwsVm> replicate(int brokerId, AwsRegion region, PurchaseOption option, Ec2InstanceProfile profile, EbsVolumeSpec ebs, int count) {
        return replicate(0, brokerId, region, option, profile, ebs, count);
    }

    public static List<AwsVm> replicate(int startId, int brokerId, AwsRegion region, PurchaseOption option, Ec2InstanceProfile profile, EbsVolumeSpec ebs, int count) {
        List<AwsVm> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createVm(startId + i, brokerId, region, option, profile, ebs));
        }
        return list;
    }
}
