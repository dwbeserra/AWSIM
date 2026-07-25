package awsim;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

public class AwsVm extends Vm {
    private final AwsRegion region;
    private final PurchaseOption purchaseOption;
    private final Ec2InstanceProfile instanceProfile;
    private final EbsVolumeSpec ebsVolumeSpec;
    private final boolean billable;

    public AwsVm(
            int id,
            int userId,
            AwsRegion region,
            PurchaseOption purchaseOption,
            Ec2InstanceProfile instanceProfile,
            EbsVolumeSpec ebsVolumeSpec,
            String vmm,
            CloudletScheduler scheduler) {
        this(
                id,
                userId,
                region,
                purchaseOption,
                instanceProfile,
                ebsVolumeSpec,
                vmm,
                scheduler,
                true);
    }

    protected AwsVm(
            int id,
            int userId,
            AwsRegion region,
            PurchaseOption purchaseOption,
            Ec2InstanceProfile instanceProfile,
            EbsVolumeSpec ebsVolumeSpec,
            String vmm,
            CloudletScheduler scheduler,
            boolean billable) {
        super(id, userId,
                instanceProfile.getMips(),
                instanceProfile.getPes(),
                instanceProfile.getRamMb(),
                instanceProfile.getBwMb(),
                instanceProfile.getImageSizeMb(),
                vmm,
                scheduler);
        this.region = region;
        this.purchaseOption = purchaseOption;
        this.instanceProfile = instanceProfile;
        this.ebsVolumeSpec = ebsVolumeSpec;
        this.billable = billable;
    }

    public AwsRegion getRegion() { return region; }
    public PurchaseOption getPurchaseOption() { return purchaseOption; }
    public Ec2InstanceProfile getInstanceProfile() { return instanceProfile; }
    public EbsVolumeSpec getEbsVolumeSpec() { return ebsVolumeSpec; }
    public boolean isBillable() { return billable; }
}
