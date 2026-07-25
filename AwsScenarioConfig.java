package awsim;

public class AwsScenarioConfig {
    private String title = "AWS Scenario from config";
    private String region = "us-east-1";
    private String purchaseOption = "ON_DEMAND";
    private String instanceType = "c7i.2xlarge";
    private int vmCount = 4;
    private int hostCount = 2;
    private int hostPes = 32;
    private int hostPeMips = 5000;
    private int hostRamMb = 262144;
    private long hostStorageMb = 10000000;
    private int hostBwMb = 50000;
    private int cloudletCount = 16;
    private long cloudletLength = 900_000_000L;
    private int cloudletPes = 1;
    private long cloudletFileSizeMb = 128;
    private long cloudletOutputSizeMb = 64;
    private int ebsSizeGb = 0;
    private int ebsIops = 3000;
    private int ebsThroughputMbps = 125;
    private int fsxStorageGb = 0;
    private int fsxThroughputMbps = 0;
    private int fsxMetadataIops = 0;
    private int fsxBackupGb = 0;
    private double s3StoredDataGb = 0.0;
    private long s3BasePutRequests = 0L;
    private long s3BaseGetRequests = 0L;
    private double fixedOutboundInternetGb = 0.0;
    private String bindingPolicy = "roundrobin";
    private int sequentialBlockSize = 1;
    private boolean variablePricing = false;
    private long priceSeed = 42L;
    private double onDemandVolatility = 0.05;
    private double spotVolatility = 0.15;
    private double storageVolatility = 0.03;
    private boolean spotAvailability = false;
    private double interruptionRatePerHour = 0.08;
    private double restartOverheadHours = 0.25;
    private double checkpointIntervalHours = 0.0;
    private boolean performanceVariability = false;
    private long performanceSeed = 42L;
    private double performanceCv = 0.10;
    private double performanceMinMultiplier = 0.60;
    private double performanceMaxMultiplier = 1.60;
    private String priceCatalogMode = "ILLUSTRATIVE";
    private String priceCatalogFile;
    private String officialPriceCacheFile = "target/aws-price-cache.properties";
    private double officialPriceCacheTtlHours = 24.0;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getPurchaseOption() { return purchaseOption; }
    public void setPurchaseOption(String purchaseOption) { this.purchaseOption = purchaseOption; }
    public String getInstanceType() { return instanceType; }
    public void setInstanceType(String instanceType) { this.instanceType = instanceType; }
    public int getVmCount() { return vmCount; }
    public void setVmCount(int vmCount) { this.vmCount = vmCount; }
    public int getHostCount() { return hostCount; }
    public void setHostCount(int hostCount) { this.hostCount = hostCount; }
    public int getHostPes() { return hostPes; }
    public void setHostPes(int hostPes) { this.hostPes = hostPes; }
    public int getHostPeMips() { return hostPeMips; }
    public void setHostPeMips(int hostPeMips) { this.hostPeMips = hostPeMips; }
    public int getHostRamMb() { return hostRamMb; }
    public void setHostRamMb(int hostRamMb) { this.hostRamMb = hostRamMb; }
    public long getHostStorageMb() { return hostStorageMb; }
    public void setHostStorageMb(long hostStorageMb) { this.hostStorageMb = hostStorageMb; }
    public int getHostBwMb() { return hostBwMb; }
    public void setHostBwMb(int hostBwMb) { this.hostBwMb = hostBwMb; }
    public int getCloudletCount() { return cloudletCount; }
    public void setCloudletCount(int cloudletCount) { this.cloudletCount = cloudletCount; }
    public long getCloudletLength() { return cloudletLength; }
    public void setCloudletLength(long cloudletLength) { this.cloudletLength = cloudletLength; }
    public int getCloudletPes() { return cloudletPes; }
    public void setCloudletPes(int cloudletPes) { this.cloudletPes = cloudletPes; }
    public long getCloudletFileSizeMb() { return cloudletFileSizeMb; }
    public void setCloudletFileSizeMb(long cloudletFileSizeMb) { this.cloudletFileSizeMb = cloudletFileSizeMb; }
    public long getCloudletOutputSizeMb() { return cloudletOutputSizeMb; }
    public void setCloudletOutputSizeMb(long cloudletOutputSizeMb) { this.cloudletOutputSizeMb = cloudletOutputSizeMb; }
    public int getEbsSizeGb() { return ebsSizeGb; }
    public void setEbsSizeGb(int ebsSizeGb) { this.ebsSizeGb = ebsSizeGb; }
    public int getEbsIops() { return ebsIops; }
    public void setEbsIops(int ebsIops) { this.ebsIops = ebsIops; }
    public int getEbsThroughputMbps() { return ebsThroughputMbps; }
    public void setEbsThroughputMbps(int ebsThroughputMbps) { this.ebsThroughputMbps = ebsThroughputMbps; }
    public int getFsxStorageGb() { return fsxStorageGb; }
    public void setFsxStorageGb(int fsxStorageGb) { this.fsxStorageGb = fsxStorageGb; }
    public int getFsxThroughputMbps() { return fsxThroughputMbps; }
    public void setFsxThroughputMbps(int fsxThroughputMbps) { this.fsxThroughputMbps = fsxThroughputMbps; }
    public int getFsxMetadataIops() { return fsxMetadataIops; }
    public void setFsxMetadataIops(int fsxMetadataIops) { this.fsxMetadataIops = fsxMetadataIops; }
    public int getFsxBackupGb() { return fsxBackupGb; }
    public void setFsxBackupGb(int fsxBackupGb) { this.fsxBackupGb = fsxBackupGb; }
    public double getS3StoredDataGb() { return s3StoredDataGb; }
    public void setS3StoredDataGb(double s3StoredDataGb) { this.s3StoredDataGb = s3StoredDataGb; }
    public long getS3BasePutRequests() { return s3BasePutRequests; }
    public void setS3BasePutRequests(long s3BasePutRequests) { this.s3BasePutRequests = s3BasePutRequests; }
    public long getS3BaseGetRequests() { return s3BaseGetRequests; }
    public void setS3BaseGetRequests(long s3BaseGetRequests) { this.s3BaseGetRequests = s3BaseGetRequests; }
    public double getFixedOutboundInternetGb() { return fixedOutboundInternetGb; }
    public void setFixedOutboundInternetGb(double fixedOutboundInternetGb) { this.fixedOutboundInternetGb = fixedOutboundInternetGb; }
    public String getBindingPolicy() { return bindingPolicy; }
    public void setBindingPolicy(String bindingPolicy) { this.bindingPolicy = bindingPolicy; }
    public int getSequentialBlockSize() { return sequentialBlockSize; }
    public void setSequentialBlockSize(int sequentialBlockSize) { this.sequentialBlockSize = sequentialBlockSize; }
    public boolean isVariablePricing() { return variablePricing; }
    public void setVariablePricing(boolean variablePricing) { this.variablePricing = variablePricing; }
    public long getPriceSeed() { return priceSeed; }
    public void setPriceSeed(long priceSeed) { this.priceSeed = priceSeed; }
    public double getOnDemandVolatility() { return onDemandVolatility; }
    public void setOnDemandVolatility(double onDemandVolatility) { this.onDemandVolatility = onDemandVolatility; }
    public double getSpotVolatility() { return spotVolatility; }
    public void setSpotVolatility(double spotVolatility) { this.spotVolatility = spotVolatility; }
    public double getStorageVolatility() { return storageVolatility; }
    public void setStorageVolatility(double storageVolatility) { this.storageVolatility = storageVolatility; }
    public boolean isSpotAvailability() { return spotAvailability; }
    public void setSpotAvailability(boolean spotAvailability) { this.spotAvailability = spotAvailability; }
    public double getInterruptionRatePerHour() { return interruptionRatePerHour; }
    public void setInterruptionRatePerHour(double interruptionRatePerHour) { this.interruptionRatePerHour = interruptionRatePerHour; }
    public double getRestartOverheadHours() { return restartOverheadHours; }
    public void setRestartOverheadHours(double restartOverheadHours) { this.restartOverheadHours = restartOverheadHours; }
    public double getCheckpointIntervalHours() { return checkpointIntervalHours; }
    public void setCheckpointIntervalHours(double checkpointIntervalHours) { this.checkpointIntervalHours = checkpointIntervalHours; }
    public boolean isPerformanceVariability() { return performanceVariability; }
    public void setPerformanceVariability(boolean performanceVariability) { this.performanceVariability = performanceVariability; }
    public long getPerformanceSeed() { return performanceSeed; }
    public void setPerformanceSeed(long performanceSeed) { this.performanceSeed = performanceSeed; }
    public double getPerformanceCv() { return performanceCv; }
    public void setPerformanceCv(double performanceCv) { this.performanceCv = performanceCv; }
    public double getPerformanceMinMultiplier() { return performanceMinMultiplier; }
    public void setPerformanceMinMultiplier(double performanceMinMultiplier) { this.performanceMinMultiplier = performanceMinMultiplier; }
    public double getPerformanceMaxMultiplier() { return performanceMaxMultiplier; }
    public void setPerformanceMaxMultiplier(double performanceMaxMultiplier) { this.performanceMaxMultiplier = performanceMaxMultiplier; }
    public String getPriceCatalogMode() { return priceCatalogMode; }
    public void setPriceCatalogMode(String priceCatalogMode) { this.priceCatalogMode = priceCatalogMode; }
    public String getPriceCatalogFile() { return priceCatalogFile; }
    public void setPriceCatalogFile(String priceCatalogFile) { this.priceCatalogFile = priceCatalogFile; }
    public String getOfficialPriceCacheFile() { return officialPriceCacheFile; }
    public void setOfficialPriceCacheFile(String officialPriceCacheFile) { this.officialPriceCacheFile = officialPriceCacheFile; }
    public double getOfficialPriceCacheTtlHours() { return officialPriceCacheTtlHours; }
    public void setOfficialPriceCacheTtlHours(double officialPriceCacheTtlHours) { this.officialPriceCacheTtlHours = officialPriceCacheTtlHours; }
}
