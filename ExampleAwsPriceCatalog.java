package awsim;

import awsim.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ExampleAwsPriceCatalog implements AwsPriceCatalog {
    public static final double HOURS_PER_MONTH = 730.0;

    private final Map<AwsRegion, Map<String, Double>> onDemandHourly = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Map<String, Double>> spotHourly = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> ebsPerGbMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> ebsPerExtraIopsMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> ebsPerExtraThroughputMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> fsxPerGbMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> fsxThroughputPerMbpsMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> fsxMetadataPerIopsMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> fsxBackupPerGbMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> s3StandardPerGbMonth = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> s3PutPerThousand = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> s3GetPerThousand = new EnumMap<>(AwsRegion.class);
    private final Map<AwsRegion, Double> transferOutPerGb = new EnumMap<>(AwsRegion.class);

    public ExampleAwsPriceCatalog() {
        seed();
    }

    @Override
    public AwsPriceCatalogMetadata getMetadata() {
        return new AwsPriceCatalogMetadata(
                "awsim-illustrative-2026-06",
                "AWSIM illustrative snapshot; not an official live AWS quotation",
                "2026-06-10",
                "USD",
                true);
    }

    private void seed() {
        seedEc2("t3.medium", 0.0416, 0.0470, 0.0464, 0.0480, 0.0610);
        seedEc2("m7i.large", 0.1344, 0.1510, 0.1480, 0.1560, 0.1890);
        seedEc2("c7i.xlarge", 0.1785, 0.2010, 0.1980, 0.2090, 0.2410);
        seedEc2("c7i.2xlarge", 0.3570, 0.4020, 0.3950, 0.4180, 0.4820);
        seedEc2("c7i.4xlarge", 0.7140, 0.8040, 0.7900, 0.8360, 0.9640);
        seedEc2("c7i.8xlarge", 1.4280, 1.6080, 1.5800, 1.6720, 1.9280);
        seedEc2("r7i.2xlarge", 0.5040, 0.5660, 0.5560, 0.5900, 0.7020);
        seedEc2("hpc7g.16xlarge", 1.6832, 1.9500, 1.8800, 2.0200, 2.4500);
        seedEc2("hpc7a.48xlarge", 4.5396, 5.1000, 4.9500, 5.4000, 6.2000);

        // Mobile-oriented surrogate profiles used by the hybrid mobile-cloud examples.
        // These are lightweight synthetic rates for simulation purposes only.
        seedEc2("smartphone-client", 0.0200, 0.0220, 0.0215, 0.0230, 0.0280);
        seedEc2("tablet-client", 0.0320, 0.0350, 0.0340, 0.0360, 0.0440);
        seedEc2("mobile-edge-server", 0.0950, 0.1040, 0.1020, 0.1080, 0.1320);

        for (AwsRegion region : AwsRegion.values()) {
            ebsPerExtraIopsMonth.put(region, 0.0050);
            ebsPerExtraThroughputMonth.put(region, 0.0600);
            fsxThroughputPerMbpsMonth.put(region, 0.52);
            fsxMetadataPerIopsMonth.put(region, 0.055);
            fsxBackupPerGbMonth.put(region, 0.050);
            s3PutPerThousand.put(region, 0.0050);
            s3GetPerThousand.put(region, 0.0004);
        }

        ebsPerGbMonth.put(AwsRegion.US_EAST_1, 0.080);
        ebsPerGbMonth.put(AwsRegion.EU_WEST_3, 0.088);
        ebsPerGbMonth.put(AwsRegion.AP_NORTHEAST_1, 0.095);
        ebsPerGbMonth.put(AwsRegion.AP_SOUTHEAST_2, 0.096);
        ebsPerGbMonth.put(AwsRegion.SA_EAST_1, 0.126);

        fsxPerGbMonth.put(AwsRegion.US_EAST_1, 0.145);
        fsxPerGbMonth.put(AwsRegion.EU_WEST_3, 0.160);
        fsxPerGbMonth.put(AwsRegion.AP_NORTHEAST_1, 0.162);
        fsxPerGbMonth.put(AwsRegion.AP_SOUTHEAST_2, 0.168);
        fsxPerGbMonth.put(AwsRegion.SA_EAST_1, 0.210);

        s3StandardPerGbMonth.put(AwsRegion.US_EAST_1, 0.0230);
        s3StandardPerGbMonth.put(AwsRegion.EU_WEST_3, 0.0250);
        s3StandardPerGbMonth.put(AwsRegion.AP_NORTHEAST_1, 0.0250);
        s3StandardPerGbMonth.put(AwsRegion.AP_SOUTHEAST_2, 0.0270);
        s3StandardPerGbMonth.put(AwsRegion.SA_EAST_1, 0.0405);

        transferOutPerGb.put(AwsRegion.US_EAST_1, 0.090);
        transferOutPerGb.put(AwsRegion.EU_WEST_3, 0.090);
        transferOutPerGb.put(AwsRegion.AP_NORTHEAST_1, 0.114);
        transferOutPerGb.put(AwsRegion.AP_SOUTHEAST_2, 0.114);
        transferOutPerGb.put(AwsRegion.SA_EAST_1, 0.250);
    }

    private void seedEc2(String instanceType, double us, double fr, double jp, double au, double br) {
        onDemandHourly.computeIfAbsent(AwsRegion.US_EAST_1, r -> new HashMap<>()).put(instanceType, us);
        onDemandHourly.computeIfAbsent(AwsRegion.EU_WEST_3, r -> new HashMap<>()).put(instanceType, fr);
        onDemandHourly.computeIfAbsent(AwsRegion.AP_NORTHEAST_1, r -> new HashMap<>()).put(instanceType, jp);
        onDemandHourly.computeIfAbsent(AwsRegion.AP_SOUTHEAST_2, r -> new HashMap<>()).put(instanceType, au);
        onDemandHourly.computeIfAbsent(AwsRegion.SA_EAST_1, r -> new HashMap<>()).put(instanceType, br);

        spotHourly.computeIfAbsent(AwsRegion.US_EAST_1, r -> new HashMap<>()).put(instanceType, us * 0.35);
        spotHourly.computeIfAbsent(AwsRegion.EU_WEST_3, r -> new HashMap<>()).put(instanceType, fr * 0.40);
        spotHourly.computeIfAbsent(AwsRegion.AP_NORTHEAST_1, r -> new HashMap<>()).put(instanceType, jp * 0.42);
        spotHourly.computeIfAbsent(AwsRegion.AP_SOUTHEAST_2, r -> new HashMap<>()).put(instanceType, au * 0.43);
        spotHourly.computeIfAbsent(AwsRegion.SA_EAST_1, r -> new HashMap<>()).put(instanceType, br * 0.48);
    }

    @Override
    public double ec2Hourly(AwsRegion region, String instanceType, PurchaseOption option) {
        Map<AwsRegion, Map<String, Double>> map = option == PurchaseOption.SPOT ? spotHourly : onDemandHourly;
        Map<String, Double> regional = map.get(region);
        if (regional == null || !regional.containsKey(instanceType)) {
            throw new IllegalArgumentException("No hourly price configured for instance type '" + instanceType
                    + "' in region '" + region + "' for option '" + option + "'.");
        }
        return regional.get(instanceType);
    }

    @Override
    public double ebsMonthly(AwsRegion region, EbsVolumeSpec ebsVolumeSpec) {
        double storage = ebsVolumeSpec.getSizeGb() * ebsPerGbMonth.get(region);
        int extraIops = Math.max(0, ebsVolumeSpec.getProvisionedIops() - 3000);
        int extraThroughput = Math.max(0, ebsVolumeSpec.getThroughputMbps() - 125);
        return storage
                + extraIops * ebsPerExtraIopsMonth.get(region)
                + extraThroughput * ebsPerExtraThroughputMonth.get(region);
    }

    @Override
    public double fsxMonthly(AwsRegion region, FsxLustreSpec fsxLustreSpec) {
        return fsxLustreSpec.getStorageGb() * fsxPerGbMonth.get(region)
                + fsxLustreSpec.getThroughputMbps() * fsxThroughputPerMbpsMonth.get(region)
                + fsxLustreSpec.getMetadataIops() * fsxMetadataPerIopsMonth.get(region)
                + fsxLustreSpec.getBackupGb() * fsxBackupPerGbMonth.get(region);
    }

    @Override
    public double s3Monthly(AwsRegion region, S3BucketSpec s3BucketSpec) {
        return s3BucketSpec.getStoredDataGb() * s3StandardPerGbMonth.get(region);
    }

    @Override
    public double s3PutPerThousand(AwsRegion region) {
        return s3PutPerThousand.get(region);
    }

    @Override
    public double s3GetPerThousand(AwsRegion region) {
        return s3GetPerThousand.get(region);
    }

    @Override
    public double transferOutPerGb(AwsRegion region) {
        return transferOutPerGb.get(region);
    }
}
