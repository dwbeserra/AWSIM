package awsim;

import java.util.Objects;
import java.util.Random;

/**
 * Seeded, bounded Gaussian variation around a calibrated baseline length.
 * The same seed, cloudlet ID, baseline, and profile always produce the same
 * realized length, making a run reproducible.
 */
public final class GaussianPerformanceVariationModel implements PerformanceModel {
    private final long seed;
    private final double coefficientOfVariation;
    private final double minMultiplier;
    private final double maxMultiplier;

    public GaussianPerformanceVariationModel(
            long seed,
            double coefficientOfVariation,
            double minMultiplier,
            double maxMultiplier) {
        if (coefficientOfVariation < 0.0) {
            throw new IllegalArgumentException("performance coefficient of variation must be >= 0");
        }
        if (minMultiplier <= 0.0 || maxMultiplier < minMultiplier) {
            throw new IllegalArgumentException("invalid performance multiplier bounds");
        }
        this.seed = seed;
        this.coefficientOfVariation = coefficientOfVariation;
        this.minMultiplier = minMultiplier;
        this.maxMultiplier = maxMultiplier;
    }

    @Override
    public long realizedLengthMi(
            int cloudletId,
            long baselineLengthMi,
            Ec2InstanceProfile profile) {
        long key = Objects.hash(
                seed,
                cloudletId,
                baselineLengthMi,
                profile.getInstanceType());
        Random random = new Random(seed ^ key);
        double multiplier = 1.0 + random.nextGaussian() * coefficientOfVariation;
        multiplier = Math.max(minMultiplier, Math.min(maxMultiplier, multiplier));
        double realized = baselineLengthMi * multiplier;
        if (realized >= Long.MAX_VALUE) {
            throw new ArithmeticException("realized cloudlet length exceeds Long.MAX_VALUE");
        }
        return Math.max(1L, Math.round(realized));
    }
}
