package awsim;

import awsim.AwsRegion;
import awsim.EbsVolumeSpec;
import awsim.FsxLustreSpec;
import awsim.PurchaseOption;
import awsim.S3BucketSpec;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Catalog backed by a normalized export of the official AWS Price List APIs.
 *
 * <p>The AWS bulk files are service-specific and can be hundreds of megabytes.
 * AWSIM therefore consumes a small, versioned properties file produced by the
 * documented normalization step. Missing values fail closed by default so a
 * stale illustrative value cannot silently masquerade as an official price.
 */
public class AwsPriceListApiCatalog implements AwsPriceCatalog {
    private final Properties values = new Properties();
    private final AwsPriceCatalog fallback;
    private final AwsPriceCatalogMetadata metadata;

    public AwsPriceListApiCatalog(Path normalizedCacheFile) throws IOException {
        this(normalizedCacheFile, null);
    }

    public AwsPriceListApiCatalog(Path normalizedCacheFile, AwsPriceCatalog fallback) throws IOException {
        if (normalizedCacheFile == null || !Files.isRegularFile(normalizedCacheFile)) {
            throw new IOException("Normalized AWS price cache does not exist: " + normalizedCacheFile);
        }
        try (InputStream in = Files.newInputStream(normalizedCacheFile)) {
            values.load(in);
        }
        this.fallback = fallback;
        this.metadata = new AwsPriceCatalogMetadata(
                requiredText("metadata.catalogId"),
                requiredText("metadata.source"),
                requiredText("metadata.retrievedAt"),
                values.getProperty("metadata.currency", "USD"),
                false);
    }

    public static void downloadToCache(URI uri, Path destination)
            throws IOException, InterruptedException {
        if (uri == null || !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("AWS price downloads require an HTTPS URI");
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<Path> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofFile(destination));
        if (response.statusCode() >= 400) {
            throw new IOException("Failed to download AWS price list: HTTP " + response.statusCode());
        }
    }

    @Override
    public AwsPriceCatalogMetadata getMetadata() {
        return metadata;
    }

    @Override
    public double ec2Hourly(AwsRegion region, String instanceType, PurchaseOption option) {
        String key = "ec2." + region.getCode() + "." + option.name() + "." + instanceType;
        if (values.containsKey(key)) {
            return requiredDouble(key);
        }
        return fallbackOrThrow(
                key,
                fallback == null ? null : fallback.ec2Hourly(region, instanceType, option));
    }

    @Override
    public double ebsMonthly(AwsRegion region, EbsVolumeSpec spec) {
        String prefix = "ebs.gp3." + region.getCode() + ".";
        if (hasAll(prefix + "storagePerGbMonth", prefix + "extraIopsMonth", prefix + "extraThroughputMonth")) {
            return spec.getSizeGb() * requiredDouble(prefix + "storagePerGbMonth")
                    + Math.max(0, spec.getProvisionedIops() - 3000)
                    * requiredDouble(prefix + "extraIopsMonth")
                    + Math.max(0, spec.getThroughputMbps() - 125)
                    * requiredDouble(prefix + "extraThroughputMonth");
        }
        return fallbackOrThrow(
                prefix,
                fallback == null ? null : fallback.ebsMonthly(region, spec));
    }

    @Override
    public double fsxMonthly(AwsRegion region, FsxLustreSpec spec) {
        String prefix = "fsx.lustre." + region.getCode() + ".";
        if (hasAll(
                prefix + "storagePerGbMonth",
                prefix + "throughputPerMbpsMonth",
                prefix + "metadataPerIopsMonth",
                prefix + "backupPerGbMonth")) {
            return spec.getStorageGb() * requiredDouble(prefix + "storagePerGbMonth")
                    + spec.getThroughputMbps() * requiredDouble(prefix + "throughputPerMbpsMonth")
                    + spec.getMetadataIops() * requiredDouble(prefix + "metadataPerIopsMonth")
                    + spec.getBackupGb() * requiredDouble(prefix + "backupPerGbMonth");
        }
        return fallbackOrThrow(
                prefix,
                fallback == null ? null : fallback.fsxMonthly(region, spec));
    }

    @Override
    public double s3Monthly(AwsRegion region, S3BucketSpec spec) {
        String key = "s3.standard." + region.getCode() + ".storagePerGbMonth";
        if (values.containsKey(key)) {
            return spec.getStoredDataGb() * requiredDouble(key);
        }
        return fallbackOrThrow(
                key,
                fallback == null ? null : fallback.s3Monthly(region, spec));
    }

    @Override
    public double s3PutPerThousand(AwsRegion region) {
        return unitOrFallback(
                "s3.standard." + region.getCode() + ".putPerThousand",
                fallback == null ? null : fallback.s3PutPerThousand(region));
    }

    @Override
    public double s3GetPerThousand(AwsRegion region) {
        return unitOrFallback(
                "s3.standard." + region.getCode() + ".getPerThousand",
                fallback == null ? null : fallback.s3GetPerThousand(region));
    }

    @Override
    public double transferOutPerGb(AwsRegion region) {
        return unitOrFallback(
                "transfer." + region.getCode() + ".internetPerGb",
                fallback == null ? null : fallback.transferOutPerGb(region));
    }

    private double unitOrFallback(String key, Double fallbackValue) {
        return values.containsKey(key)
                ? requiredDouble(key)
                : fallbackOrThrow(key, fallbackValue);
    }

    private boolean hasAll(String... keys) {
        for (String key : keys) {
            if (!values.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    private String requiredText(String key) {
        String value = values.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing normalized AWS price metadata: " + key);
        }
        return value.trim();
    }

    private double requiredDouble(String key) {
        String value = requiredText(key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric price for " + key + ": " + value, e);
        }
    }

    private double fallbackOrThrow(String key, Double fallbackValue) {
        if (fallbackValue != null) {
            return fallbackValue;
        }
        throw new IllegalArgumentException(
                "The normalized AWS price cache has no value for '" + key + "'. "
                        + "Refresh the cache or explicitly provide a fallback catalog.");
    }
}
