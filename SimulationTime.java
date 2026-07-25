package awsim;

/**
 * Unit conversions used at the boundary between CloudSim traces and AWS
 * billing. CloudSim 4.0 examples and schedulers conventionally express the
 * simulation clock in seconds; AWSIM reports and bills durations in hours.
 */
public final class SimulationTime {
    public static final double SECONDS_PER_HOUR = 3600.0;

    private SimulationTime() {
    }

    public static double secondsToHours(double seconds) {
        return seconds / SECONDS_PER_HOUR;
    }

    public static double hoursToSeconds(double hours) {
        return hours * SECONDS_PER_HOUR;
    }

    /**
     * Scales a historical cloudlet length that was previously interpreted as
     * hours even though CloudSim treated the resulting duration as seconds.
     */
    public static long recalibrateLegacyLengthMi(long legacyLengthMi) {
        return Math.multiplyExact(legacyLengthMi, (long) SECONDS_PER_HOUR);
    }
}
