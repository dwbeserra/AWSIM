package awsim;

import awsim.*;

public interface AwsPriceCatalog {
    default AwsPriceCatalogMetadata getMetadata() {
        return AwsPriceCatalogMetadata.unspecified();
    }

    double ec2Hourly(AwsRegion region, String instanceType, PurchaseOption option);
    double ebsMonthly(AwsRegion region, EbsVolumeSpec ebsVolumeSpec);
    double fsxMonthly(AwsRegion region, FsxLustreSpec fsxLustreSpec);
    double s3Monthly(AwsRegion region, S3BucketSpec s3BucketSpec);
    double s3PutPerThousand(AwsRegion region);
    double s3GetPerThousand(AwsRegion region);
    double transferOutPerGb(AwsRegion region);
}
