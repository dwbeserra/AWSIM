package awsim;

import awsim.AwsRegion;
import awsim.PurchaseOption;

public interface PriceShockModel {
    double multiplier(PriceDimension dimension, AwsRegion region, String instanceType, PurchaseOption option);
}
