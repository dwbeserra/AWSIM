package awsim;

public class S3BucketSpec {
    private final double storedDataGb;
    private final long basePutRequests;
    private final long baseGetRequests;

    public S3BucketSpec(double storedDataGb, long basePutRequests, long baseGetRequests) {
        this.storedDataGb = storedDataGb;
        this.basePutRequests = basePutRequests;
        this.baseGetRequests = baseGetRequests;
    }

    public double getStoredDataGb() { return storedDataGb; }
    public long getBasePutRequests() { return basePutRequests; }
    public long getBaseGetRequests() { return baseGetRequests; }
}
