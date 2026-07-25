package awsim;

public class EbsVolumeSpec {
    private final int sizeGb;
    private final int provisionedIops;
    private final int throughputMbps;

    public EbsVolumeSpec(int sizeGb, int provisionedIops, int throughputMbps) {
        this.sizeGb = sizeGb;
        this.provisionedIops = provisionedIops;
        this.throughputMbps = throughputMbps;
    }

    public int getSizeGb() { return sizeGb; }
    public int getProvisionedIops() { return provisionedIops; }
    public int getThroughputMbps() { return throughputMbps; }
}
