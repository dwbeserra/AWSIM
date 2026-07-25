package awsim;

public interface AvailabilityModel {
    AvailabilityOutcome assess(AwsVm vm, double runtimeHours);
}
