package awsim;

import java.util.Arrays;
import java.util.List;

public final class StandardEc2Profiles {
    public static final Ec2InstanceProfile T3_MEDIUM = new Ec2InstanceProfile("t3.medium", 2200, 2, 4096, 5000, 4096);
    public static final Ec2InstanceProfile M7I_LARGE = new Ec2InstanceProfile("m7i.large", 3200, 2, 8192, 7500, 8192);
    public static final Ec2InstanceProfile C7I_XLARGE = new Ec2InstanceProfile("c7i.xlarge", 4800, 4, 8192, 10000, 8192);
    public static final Ec2InstanceProfile C7I_2XLARGE = new Ec2InstanceProfile("c7i.2xlarge", 5000, 8, 16384, 10000, 10000);
    public static final Ec2InstanceProfile C7I_4XLARGE = new Ec2InstanceProfile("c7i.4xlarge", 5200, 16, 32768, 12500, 12000);
    public static final Ec2InstanceProfile C7I_8XLARGE = new Ec2InstanceProfile("c7i.8xlarge", 5400, 32, 65536, 15000, 14000);
    public static final Ec2InstanceProfile R7I_2XLARGE = new Ec2InstanceProfile("r7i.2xlarge", 4600, 8, 65536, 10000, 10000);
    public static final Ec2InstanceProfile HPC7G_16XLARGE = new Ec2InstanceProfile("hpc7g.16xlarge", 7000, 64, 131072, 25000, 15000);
    public static final Ec2InstanceProfile HPC7A_48XLARGE = new Ec2InstanceProfile("hpc7a.48xlarge", 7200, 96, 393216, 50000, 20000);

    private StandardEc2Profiles() {
    }

    public static List<Ec2InstanceProfile> all() {
        return Arrays.asList(
                T3_MEDIUM,
                M7I_LARGE,
                C7I_XLARGE,
                C7I_2XLARGE,
                C7I_4XLARGE,
                C7I_8XLARGE,
                R7I_2XLARGE,
                HPC7G_16XLARGE,
                HPC7A_48XLARGE);
    }
}
