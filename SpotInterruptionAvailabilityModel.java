package awsim;

import java.util.Random;

public class SpotInterruptionAvailabilityModel implements AvailabilityModel {
    private final double interruptionRatePerHour;
    private final double restartOverheadHours;
    private final double checkpointIntervalHours;
    private final long seed;

    public SpotInterruptionAvailabilityModel(double interruptionRatePerHour, double restartOverheadHours, long seed) {
        this(interruptionRatePerHour, restartOverheadHours, 0.0, seed);
    }

    public SpotInterruptionAvailabilityModel(
            double interruptionRatePerHour,
            double restartOverheadHours,
            double checkpointIntervalHours,
            long seed) {
        this.interruptionRatePerHour = Math.max(0.0, interruptionRatePerHour);
        this.restartOverheadHours = Math.max(0.0, restartOverheadHours);
        this.checkpointIntervalHours = Math.max(0.0, checkpointIntervalHours);
        this.seed = seed;
    }

    @Override
    public AvailabilityOutcome assess(AwsVm vm, double runtimeHours) {
        if (vm.getPurchaseOption() != PurchaseOption.SPOT || runtimeHours <= 0.0) {
            return AvailabilityOutcome.unchanged(runtimeHours);
        }

        double expectedInterruptions = interruptionRatePerHour * runtimeHours;
        long runtimeBits = Double.doubleToLongBits(runtimeHours);
        Random random = new Random(seed ^ (31L * vm.getId()) ^ runtimeBits);
        int realized = samplePoisson(expectedInterruptions, random);
        double expectedLostWorkPerInterruption = checkpointIntervalHours / 2.0;
        double adjustedRuntime = runtimeHours
                + realized * (restartOverheadHours + expectedLostWorkPerInterruption);
        return new AvailabilityOutcome(adjustedRuntime, realized);
    }

    private int samplePoisson(double lambda, Random random) {
        if (lambda <= 0.0) {
            return 0;
        }
        double l = Math.exp(-lambda);
        int k = 0;
        double p = 1.0;
        do {
            k++;
            p *= random.nextDouble();
        } while (p > l);
        return k - 1;
    }
}
