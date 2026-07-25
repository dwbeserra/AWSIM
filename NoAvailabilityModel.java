package awsim;

public class NoAvailabilityModel implements AvailabilityModel {
    @Override
    public AvailabilityOutcome assess(AwsVm vm, double runtimeHours) {
        return AvailabilityOutcome.unchanged(runtimeHours);
    }
}
