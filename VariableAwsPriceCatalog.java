package awsim;

import awsim.*;

public class VariableAwsPriceCatalog implements AwsPriceCatalog {
    private final AwsPriceCatalog delegate;
    private final PriceShockModel shocks;

    public VariableAwsPriceCatalog(AwsPriceCatalog delegate, PriceShockModel shocks) {
        this.delegate = delegate;
        this.shocks = shocks;
    }

    @Override
    public AwsPriceCatalogMetadata getMetadata() {
        AwsPriceCatalogMetadata base = delegate.getMetadata();
        return new AwsPriceCatalogMetadata(
                base.getCatalogId() + "+seeded-variation",
                base.getSource() + "; wrapped by a seeded AWSIM price-variation model",
                base.getRetrievedAt(),
                base.getCurrency(),
                base.isIllustrative());
    }

    @Override
    public double ec2Hourly(AwsRegion region, String instanceType, PurchaseOption option) {
        return delegate.ec2Hourly(region, instanceType, option)
                * shocks.multiplier(PriceDimension.EC2, region, instanceType, option);
    }

    @Override
    public double ebsMonthly(AwsRegion region, EbsVolumeSpec ebsVolumeSpec) {
        return delegate.ebsMonthly(region, ebsVolumeSpec)
                * shocks.multiplier(PriceDimension.EBS, region, null, null);
    }

    @Override
    public double fsxMonthly(AwsRegion region, FsxLustreSpec fsxLustreSpec) {
        return delegate.fsxMonthly(region, fsxLustreSpec)
                * shocks.multiplier(PriceDimension.FSX, region, null, null);
    }

    @Override
    public double s3Monthly(AwsRegion region, S3BucketSpec s3BucketSpec) {
        return delegate.s3Monthly(region, s3BucketSpec)
                * shocks.multiplier(PriceDimension.S3_STORAGE, region, null, null);
    }

    @Override
    public double s3PutPerThousand(AwsRegion region) {
        return delegate.s3PutPerThousand(region)
                * shocks.multiplier(PriceDimension.S3_PUT, region, null, null);
    }

    @Override
    public double s3GetPerThousand(AwsRegion region) {
        return delegate.s3GetPerThousand(region)
                * shocks.multiplier(PriceDimension.S3_GET, region, null, null);
    }

    @Override
    public double transferOutPerGb(AwsRegion region) {
        return delegate.transferOutPerGb(region)
                * shocks.multiplier(PriceDimension.TRANSFER_OUT, region, null, null);
    }
}
