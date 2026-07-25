package awsim;

public class AvailabilityOutcome {
    private final double adjustedRuntimeHours;
    private final int interruptionCount;

    public AvailabilityOutcome(double adjustedRuntimeHours, int interruptionCount) {
        this.adjustedRuntimeHours = adjustedRuntimeHours;
        this.interruptionCount = interruptionCount;
    }

    public double getAdjustedRuntimeHours() { return adjustedRuntimeHours; }
    public int getInterruptionCount() { return interruptionCount; }

    public static AvailabilityOutcome unchanged(double runtimeHours) {
        return new AvailabilityOutcome(runtimeHours, 0);
    }
}
