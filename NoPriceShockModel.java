package awsim;

import awsim.AwsRegion;
import awsim.PurchaseOption;

public class NoPriceShockModel implements PriceShockModel {
    @Override
    public double multiplier(PriceDimension dimension, AwsRegion region, String instanceType, PurchaseOption option) {
        return 1.0;
    }
}
