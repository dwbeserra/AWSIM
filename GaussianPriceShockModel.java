package awsim;

import awsim.AwsRegion;
import awsim.PurchaseOption;

import java.util.Objects;
import java.util.Random;

public class GaussianPriceShockModel implements PriceShockModel {
    private final long seed;
    private final double onDemandSigma;
    private final double spotSigma;
    private final double storageSigma;
    private final double minMultiplier;
    private final double maxMultiplier;

    public GaussianPriceShockModel(long seed, double onDemandSigma, double spotSigma, double storageSigma) {
        this(seed, onDemandSigma, spotSigma, storageSigma, 0.5, 2.0);
    }

    public GaussianPriceShockModel(long seed, double onDemandSigma, double spotSigma, double storageSigma, double minMultiplier, double maxMultiplier) {
        this.seed = seed;
        this.onDemandSigma = Math.max(0.0, onDemandSigma);
        this.spotSigma = Math.max(0.0, spotSigma);
        this.storageSigma = Math.max(0.0, storageSigma);
        this.minMultiplier = minMultiplier;
        this.maxMultiplier = maxMultiplier;
    }

    @Override
    public double multiplier(PriceDimension dimension, AwsRegion region, String instanceType, PurchaseOption option) {
        double sigma;
        switch (dimension) {
            case EC2:
                sigma = option == PurchaseOption.SPOT ? spotSigma : onDemandSigma;
                break;
            default:
                sigma = storageSigma;
        }
        long key = Objects.hash(seed, dimension, region, instanceType, option);
        Random random = new Random(seed ^ key);
        double value = 1.0 + random.nextGaussian() * sigma;
        if (value < minMultiplier) value = minMultiplier;
        if (value > maxMultiplier) value = maxMultiplier;
        return value;
    }
}
