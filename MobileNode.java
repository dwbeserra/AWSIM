package awsim;

import org.cloudbus.cloudsim.CloudletScheduler;

public class MobileNode extends AwsVm {
    private final MobileDeviceProfile mobileProfile;

    public MobileNode(int id,
                      int userId,
                      AwsRegion region,
                      PurchaseOption purchaseOption,
                      MobileDeviceProfile mobileProfile,
                      EbsVolumeSpec ebsVolumeSpec,
                      String vmm,
                      CloudletScheduler scheduler) {
        super(id,
                userId,
                region,
                purchaseOption,
                new Ec2InstanceProfile(mobileProfile.getName(), mobileProfile.getMips(), mobileProfile.getPes(), mobileProfile.getRamMb(), mobileProfile.getBwMb(), mobileProfile.getImageSizeMb()),
                ebsVolumeSpec,
                vmm,
                scheduler,
                false);
        this.mobileProfile = mobileProfile;
    }

    public MobileDeviceProfile getMobileProfile() {
        return mobileProfile;
    }
}
