package awsim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SimpleScenarioConfigLoader {
    private static final Set<String> KNOWN_KEYS = new HashSet<>(Arrays.asList(
            "title", "region", "purchaseOption", "instanceType", "vmCount",
            "hostCount", "hostPes", "hostPeMips", "hostRamMb", "hostStorageMb",
            "hostBwMb", "cloudletCount", "cloudletLength", "cloudletPes",
            "cloudletFileSizeMb", "cloudletOutputSizeMb", "ebsSizeGb", "ebsIops",
            "ebsThroughputMbps", "fsxStorageGb", "fsxThroughputMbps",
            "fsxMetadataIops", "fsxBackupGb", "s3StoredDataGb",
            "s3BasePutRequests", "s3BaseGetRequests", "fixedOutboundInternetGb",
            "bindingPolicy", "sequentialBlockSize", "variablePricing", "priceSeed",
            "onDemandVolatility", "spotVolatility", "storageVolatility",
            "spotAvailability", "interruptionRatePerHour", "restartOverheadHours",
            "checkpointIntervalHours", "performanceVariability", "performanceSeed",
            "performanceCv", "performanceMinMultiplier", "performanceMaxMultiplier",
            "priceCatalogMode", "priceCatalogFile", "officialPriceCacheFile",
            "officialPriceCacheTtlHours"));

    private SimpleScenarioConfigLoader() {
    }

    public static AwsScenarioConfig load(Path path) throws IOException {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".json")) {
            return fromMap(parseFlatJson(Files.readString(path)));
        }
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            return fromMap(parseFlatYaml(Files.readAllLines(path)));
        }
        throw new IllegalArgumentException("Unsupported config format: " + path);
    }

    private static Map<String, String> parseFlatJson(String text) {
        Map<String, String> values = new HashMap<>();
        Pattern p = Pattern.compile("\\\"([^\\\"]+)\\\"\\s*:\\s*(\\\"[^\\\"]*\\\"|true|false|-?\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(text);
        while (m.find()) {
            String value = m.group(2);
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            values.put(m.group(1), value);
        }
        return values;
    }

    private static Map<String, String> parseFlatYaml(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.indexOf(':');
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            values.put(key, value);
        }
        return values;
    }

    private static AwsScenarioConfig fromMap(Map<String, String> values) {
        Set<String> unknownKeys = new TreeSet<>(values.keySet());
        unknownKeys.removeAll(KNOWN_KEYS);
        if (!unknownKeys.isEmpty()) {
            throw new IllegalArgumentException("Unknown scenario keys: " + unknownKeys);
        }
        AwsScenarioConfig cfg = new AwsScenarioConfig();
        if (values.containsKey("title")) cfg.setTitle(values.get("title"));
        if (values.containsKey("region")) cfg.setRegion(values.get("region"));
        if (values.containsKey("purchaseOption")) cfg.setPurchaseOption(values.get("purchaseOption"));
        if (values.containsKey("instanceType")) cfg.setInstanceType(values.get("instanceType"));
        if (values.containsKey("vmCount")) cfg.setVmCount(Integer.parseInt(values.get("vmCount")));
        if (values.containsKey("hostCount")) cfg.setHostCount(Integer.parseInt(values.get("hostCount")));
        if (values.containsKey("hostPes")) cfg.setHostPes(Integer.parseInt(values.get("hostPes")));
        if (values.containsKey("hostPeMips")) cfg.setHostPeMips(Integer.parseInt(values.get("hostPeMips")));
        if (values.containsKey("hostRamMb")) cfg.setHostRamMb(Integer.parseInt(values.get("hostRamMb")));
        if (values.containsKey("hostStorageMb")) cfg.setHostStorageMb(Long.parseLong(values.get("hostStorageMb")));
        if (values.containsKey("hostBwMb")) cfg.setHostBwMb(Integer.parseInt(values.get("hostBwMb")));
        if (values.containsKey("cloudletCount")) cfg.setCloudletCount(Integer.parseInt(values.get("cloudletCount")));
        if (values.containsKey("cloudletLength")) cfg.setCloudletLength(Long.parseLong(values.get("cloudletLength")));
        if (values.containsKey("cloudletPes")) cfg.setCloudletPes(Integer.parseInt(values.get("cloudletPes")));
        if (values.containsKey("cloudletFileSizeMb")) cfg.setCloudletFileSizeMb(Long.parseLong(values.get("cloudletFileSizeMb")));
        if (values.containsKey("cloudletOutputSizeMb")) cfg.setCloudletOutputSizeMb(Long.parseLong(values.get("cloudletOutputSizeMb")));
        if (values.containsKey("ebsSizeGb")) cfg.setEbsSizeGb(Integer.parseInt(values.get("ebsSizeGb")));
        if (values.containsKey("ebsIops")) cfg.setEbsIops(Integer.parseInt(values.get("ebsIops")));
        if (values.containsKey("ebsThroughputMbps")) cfg.setEbsThroughputMbps(Integer.parseInt(values.get("ebsThroughputMbps")));
        if (values.containsKey("fsxStorageGb")) cfg.setFsxStorageGb(Integer.parseInt(values.get("fsxStorageGb")));
        if (values.containsKey("fsxThroughputMbps")) cfg.setFsxThroughputMbps(Integer.parseInt(values.get("fsxThroughputMbps")));
        if (values.containsKey("fsxMetadataIops")) cfg.setFsxMetadataIops(Integer.parseInt(values.get("fsxMetadataIops")));
        if (values.containsKey("fsxBackupGb")) cfg.setFsxBackupGb(Integer.parseInt(values.get("fsxBackupGb")));
        if (values.containsKey("s3StoredDataGb")) cfg.setS3StoredDataGb(Double.parseDouble(values.get("s3StoredDataGb")));
        if (values.containsKey("s3BasePutRequests")) cfg.setS3BasePutRequests(Long.parseLong(values.get("s3BasePutRequests")));
        if (values.containsKey("s3BaseGetRequests")) cfg.setS3BaseGetRequests(Long.parseLong(values.get("s3BaseGetRequests")));
        if (values.containsKey("fixedOutboundInternetGb")) cfg.setFixedOutboundInternetGb(Double.parseDouble(values.get("fixedOutboundInternetGb")));
        if (values.containsKey("bindingPolicy")) cfg.setBindingPolicy(values.get("bindingPolicy"));
        if (values.containsKey("sequentialBlockSize")) cfg.setSequentialBlockSize(Integer.parseInt(values.get("sequentialBlockSize")));
        if (values.containsKey("variablePricing")) cfg.setVariablePricing(Boolean.parseBoolean(values.get("variablePricing")));
        if (values.containsKey("priceSeed")) cfg.setPriceSeed(Long.parseLong(values.get("priceSeed")));
        if (values.containsKey("onDemandVolatility")) cfg.setOnDemandVolatility(Double.parseDouble(values.get("onDemandVolatility")));
        if (values.containsKey("spotVolatility")) cfg.setSpotVolatility(Double.parseDouble(values.get("spotVolatility")));
        if (values.containsKey("storageVolatility")) cfg.setStorageVolatility(Double.parseDouble(values.get("storageVolatility")));
        if (values.containsKey("spotAvailability")) cfg.setSpotAvailability(Boolean.parseBoolean(values.get("spotAvailability")));
        if (values.containsKey("interruptionRatePerHour")) cfg.setInterruptionRatePerHour(Double.parseDouble(values.get("interruptionRatePerHour")));
        if (values.containsKey("restartOverheadHours")) cfg.setRestartOverheadHours(Double.parseDouble(values.get("restartOverheadHours")));
        if (values.containsKey("checkpointIntervalHours")) cfg.setCheckpointIntervalHours(Double.parseDouble(values.get("checkpointIntervalHours")));
        if (values.containsKey("performanceVariability")) cfg.setPerformanceVariability(Boolean.parseBoolean(values.get("performanceVariability")));
        if (values.containsKey("performanceSeed")) cfg.setPerformanceSeed(Long.parseLong(values.get("performanceSeed")));
        if (values.containsKey("performanceCv")) cfg.setPerformanceCv(Double.parseDouble(values.get("performanceCv")));
        if (values.containsKey("performanceMinMultiplier")) cfg.setPerformanceMinMultiplier(Double.parseDouble(values.get("performanceMinMultiplier")));
        if (values.containsKey("performanceMaxMultiplier")) cfg.setPerformanceMaxMultiplier(Double.parseDouble(values.get("performanceMaxMultiplier")));
        if (values.containsKey("priceCatalogMode")) cfg.setPriceCatalogMode(values.get("priceCatalogMode"));
        if (values.containsKey("priceCatalogFile")) cfg.setPriceCatalogFile(values.get("priceCatalogFile"));
        if (values.containsKey("officialPriceCacheFile")) cfg.setOfficialPriceCacheFile(values.get("officialPriceCacheFile"));
        if (values.containsKey("officialPriceCacheTtlHours")) cfg.setOfficialPriceCacheTtlHours(Double.parseDouble(values.get("officialPriceCacheTtlHours")));
        return cfg;
    }
}
