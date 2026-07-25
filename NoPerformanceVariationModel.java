package awsim;

public final class NoPerformanceVariationModel implements PerformanceModel {
    @Override
    public long realizedLengthMi(
            int cloudletId,
            long baselineLengthMi,
            Ec2InstanceProfile profile) {
        return baselineLengthMi;
    }
}
