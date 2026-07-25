package awsim;

public final class AwsInstanceProfileMapper {
    private AwsInstanceProfileMapper() {
    }

    public static Ec2InstanceProfile fromAwsLikeParameters(
            String instanceType,
            int vcpus,
            int ramGiB,
            double estimatedMipsPerVcpu,
            long bandwidthMb,
            long imageSizeMb) {
        return new Ec2InstanceProfile(
                instanceType,
                estimatedMipsPerVcpu,
                vcpus,
                ramGiB * 1024,
                bandwidthMb,
                imageSizeMb);
    }

    public static Ec2InstanceProfile fromCalibratedParameters(
            String instanceType,
            int vcpus,
            int ramGiB,
            double calibratedMipsPerVcpu,
            long bandwidthMb,
            long imageSizeMb,
            String calibrationSource,
            String calibrationDate) {
        return new Ec2InstanceProfile(
                instanceType,
                calibratedMipsPerVcpu,
                vcpus,
                ramGiB * 1024,
                bandwidthMb,
                imageSizeMb,
                calibrationSource,
                calibrationDate,
                false);
    }
}
