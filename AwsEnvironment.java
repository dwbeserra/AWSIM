package awsim;

public class AwsEnvironment {
    private final AwsRegion region;
    private final FsxLustreSpec fsxLustreSpec;
    private final S3BucketSpec s3BucketSpec;
    private final double fixedOutboundInternetGb;
    private final AvailabilityModel availabilityModel;

    public AwsEnvironment(AwsRegion region, FsxLustreSpec fsxLustreSpec, S3BucketSpec s3BucketSpec, double fixedOutboundInternetGb) {
        this(region, fsxLustreSpec, s3BucketSpec, fixedOutboundInternetGb, new NoAvailabilityModel());
    }

    public AwsEnvironment(AwsRegion region, FsxLustreSpec fsxLustreSpec, S3BucketSpec s3BucketSpec, double fixedOutboundInternetGb, AvailabilityModel availabilityModel) {
        this.region = region;
        this.fsxLustreSpec = fsxLustreSpec;
        this.s3BucketSpec = s3BucketSpec;
        this.fixedOutboundInternetGb = fixedOutboundInternetGb;
        this.availabilityModel = availabilityModel == null ? new NoAvailabilityModel() : availabilityModel;
    }

    public AwsRegion getRegion() { return region; }
    public FsxLustreSpec getFsxLustreSpec() { return fsxLustreSpec; }
    public S3BucketSpec getS3BucketSpec() { return s3BucketSpec; }
    public double getFixedOutboundInternetGb() { return fixedOutboundInternetGb; }
    public AvailabilityModel getAvailabilityModel() { return availabilityModel; }
}
