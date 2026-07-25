package awsim;

public class FsxLustreSpec {
    private final int storageGb;
    private final int throughputMbps;
    private final int metadataIops;
    private final int backupGb;

    public FsxLustreSpec(int storageGb, int throughputMbps, int metadataIops, int backupGb) {
        this.storageGb = storageGb;
        this.throughputMbps = throughputMbps;
        this.metadataIops = metadataIops;
        this.backupGb = backupGb;
    }

    public int getStorageGb() { return storageGb; }
    public int getThroughputMbps() { return throughputMbps; }
    public int getMetadataIops() { return metadataIops; }
    public int getBackupGb() { return backupGb; }
}
