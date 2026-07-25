package awsim;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import awsim.*;

import java.util.ArrayList;
import java.util.List;

public final class MobileNodeFactory {
    private MobileNodeFactory() {
    }

    public static MobileNode createNode(int id, int brokerId, AwsRegion region, PurchaseOption option, MobileDeviceProfile profile, EbsVolumeSpec ebs) {
        return new MobileNode(id, brokerId, region, option, profile, ebs, "Xen", new CloudletSchedulerTimeShared());
    }

    public static List<MobileNode> replicate(int brokerId, AwsRegion region, PurchaseOption option, MobileDeviceProfile profile, EbsVolumeSpec ebs, int count) {
        return replicate(0, brokerId, region, option, profile, ebs, count);
    }

    public static List<MobileNode> replicate(int startId, int brokerId, AwsRegion region, PurchaseOption option, MobileDeviceProfile profile, EbsVolumeSpec ebs, int count) {
        List<MobileNode> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createNode(startId + i, brokerId, region, option, profile, ebs));
        }
        return list;
    }
}
