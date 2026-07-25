package awsim;

import awsim.AwsRegion;
import awsim.PurchaseOption;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Refreshes AWSIM's normalized cache directly from official AWS Price List
 * Bulk API files.
 *
 * <p>The public Bulk API is used instead of screen scraping and requires no AWS
 * credentials. Raw service files are streamed and discarded; only the
 * dimensions required by the scenario are persisted. This keeps the cache
 * small while preserving the official publication version and retrieval time.
 */
public final class AwsOfficialPriceCache {
    public interface PriceFileSource {
        InputStream open(String serviceCode, AwsRegion region) throws IOException, InterruptedException;
    }

    public static final class Requirements {
        private final Set<String> instanceTypes;
        private final PurchaseOption purchaseOption;
        private final boolean ebs;
        private final boolean fsx;
        private final boolean s3;
        private final boolean transfer;

        public Requirements(
                Set<String> instanceTypes,
                PurchaseOption purchaseOption,
                boolean ebs,
                boolean fsx,
                boolean s3,
                boolean transfer) {
            this.instanceTypes = Collections.unmodifiableSet(
                    new LinkedHashSet<>(instanceTypes));
            this.purchaseOption = purchaseOption;
            this.ebs = ebs;
            this.fsx = fsx;
            this.s3 = s3;
            this.transfer = transfer;
        }

        public Set<String> getInstanceTypes() { return instanceTypes; }
        public PurchaseOption getPurchaseOption() { return purchaseOption; }
        public boolean needsEbs() { return ebs; }
        public boolean needsFsx() { return fsx; }
        public boolean needsS3() { return s3; }
        public boolean needsTransfer() { return transfer; }
    }

    public static final class HttpPriceFileSource implements PriceFileSource {
        private static final String HOST = "pricing.us-east-1.amazonaws.com";
        private final HttpClient client;

        public HttpPriceFileSource() {
            this(defaultClient());
        }

        HttpPriceFileSource(HttpClient client) {
            this.client = client;
        }

        private static HttpClient defaultClient() {
            HttpClient.Builder builder = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(30));
            String proxy = System.getenv("HTTPS_PROXY");
            if (proxy == null || proxy.trim().isEmpty()) {
                proxy = System.getenv("https_proxy");
            }
            if (proxy != null && !proxy.trim().isEmpty()) {
                URI proxyUri = URI.create(proxy.trim());
                int port = proxyUri.getPort();
                if (proxyUri.getHost() != null && port > 0) {
                    builder.proxy(ProxySelector.of(
                            new InetSocketAddress(proxyUri.getHost(), port)));
                }
            }
            return builder.build();
        }

        @Override
        public InputStream open(String serviceCode, AwsRegion region)
                throws IOException, InterruptedException {
            if (!serviceCode.matches("[A-Za-z0-9]+")) {
                throw new IllegalArgumentException("Invalid AWS price service code");
            }
            URI uri = URI.create("https://" + HOST + "/offers/v1.0/aws/"
                    + serviceCode + "/current/" + region.getCode() + "/index.csv");
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMinutes(5))
                    .header("User-Agent", "AWSIM/2 official-price-refresh")
                    .GET()
                    .build();
            HttpResponse<InputStream> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                response.body().close();
                throw new IOException("AWS Price List download failed for "
                        + serviceCode + ": HTTP " + response.statusCode());
            }
            return response.body();
        }
    }

    private AwsOfficialPriceCache() {
    }

    public static Path refreshIfStale(
            Path cacheFile,
            AwsRegion region,
            Requirements requirements,
            double ttlHours) throws IOException, InterruptedException {
        return refreshIfStale(
                cacheFile,
                region,
                requirements,
                ttlHours,
                new HttpPriceFileSource());
    }

    public static Path refreshIfStale(
            Path cacheFile,
            AwsRegion region,
            Requirements requirements,
            double ttlHours,
            PriceFileSource source) throws IOException, InterruptedException {
        if (ttlHours <= 0.0) {
            throw new IllegalArgumentException("Official price cache TTL must be > 0");
        }
        if (requirements.getPurchaseOption() == PurchaseOption.SPOT) {
            throw new IllegalArgumentException(
                    "The public AWS Price List Bulk API does not publish current Spot prices. "
                            + "Use a versioned CACHE produced from the official EC2 Spot Price "
                            + "History API for Spot scenarios.");
        }
        if (isFreshAndComplete(cacheFile, region, requirements, ttlHours)) {
            return cacheFile;
        }
        return refresh(cacheFile, region, requirements, source);
    }

    public static Path refresh(
            Path cacheFile,
            AwsRegion region,
            Requirements requirements,
            PriceFileSource source) throws IOException, InterruptedException {
        Properties values = new Properties();
        values.setProperty("metadata.catalogId", "aws-price-list-bulk-current");
        values.setProperty("metadata.source",
                "https://pricing.us-east-1.amazonaws.com/offers/v1.0/aws/");
        values.setProperty("metadata.retrievedAt", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        values.setProperty("metadata.currency", "USD");
        values.setProperty("metadata.region", region.getCode());

        AwsOfficialPriceNormalizer normalizer =
                new AwsOfficialPriceNormalizer(region, requirements, values);
        readService(source, "AmazonEC2", region, normalizer::readEc2);
        if (requirements.needsS3()) {
            readService(source, "AmazonS3", region, normalizer::readS3);
        }
        if (requirements.needsFsx()) {
            readService(source, "AmazonFSx", region, normalizer::readFsx);
        }
        if (requirements.needsTransfer()) {
            readService(source, "AWSDataTransfer", region, normalizer::readTransfer);
        }
        normalizer.requireComplete();

        Path absolute = cacheFile.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporary = Files.createTempFile(
                parent,
                absolute.getFileName().toString(),
                ".tmp");
        try {
            try (java.io.OutputStream out = Files.newOutputStream(temporary)) {
                values.store(out,
                        "Generated automatically by AWSIM from official AWS Price List Bulk API CSV files");
            }
            try {
                Files.move(
                        temporary,
                        absolute,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return absolute;
    }

    private static boolean isFreshAndComplete(
            Path cacheFile,
            AwsRegion region,
            Requirements requirements,
            double ttlHours) {
        if (cacheFile == null || !Files.isRegularFile(cacheFile)) {
            return false;
        }
        try {
            Instant oldestAllowed = Instant.now().minusMillis(
                    Math.max(1L, (long) (ttlHours * 3_600_000.0)));
            if (Files.getLastModifiedTime(cacheFile).toInstant().isBefore(oldestAllowed)) {
                return false;
            }
            Properties cached = new Properties();
            try (InputStream in = Files.newInputStream(cacheFile)) {
                cached.load(in);
            }
            if (!region.getCode().equals(cached.getProperty("metadata.region"))) {
                return false;
            }
            for (String instanceType : requirements.getInstanceTypes()) {
                if (!cached.containsKey(
                        "ec2." + region.getCode() + "."
                                + requirements.getPurchaseOption().name() + "."
                                + instanceType)) {
                    return false;
                }
            }
            if (requirements.needsEbs()
                    && !hasAll(cached,
                    "ebs.gp3." + region.getCode() + ".storagePerGbMonth",
                    "ebs.gp3." + region.getCode() + ".extraIopsMonth",
                    "ebs.gp3." + region.getCode() + ".extraThroughputMonth")) {
                return false;
            }
            if (requirements.needsFsx()
                    && !hasAll(cached,
                    "fsx.lustre." + region.getCode() + ".storagePerGbMonth",
                    "fsx.lustre." + region.getCode() + ".throughputPerMbpsMonth",
                    "fsx.lustre." + region.getCode() + ".metadataPerIopsMonth",
                    "fsx.lustre." + region.getCode() + ".backupPerGbMonth")) {
                return false;
            }
            if (requirements.needsS3()
                    && !hasAll(cached,
                    "s3.standard." + region.getCode() + ".storagePerGbMonth",
                    "s3.standard." + region.getCode() + ".putPerThousand",
                    "s3.standard." + region.getCode() + ".getPerThousand")) {
                return false;
            }
            if (requirements.needsTransfer()
                    && !cached.containsKey(
                    "transfer." + region.getCode() + ".internetPerGb")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasAll(Properties values, String... keys) {
        for (String key : keys) {
            if (!values.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    private interface ServiceReader {
        void read(InputStreamReader reader) throws IOException;
    }

    private static void readService(
            PriceFileSource source,
            String service,
            AwsRegion region,
            ServiceReader reader) throws IOException, InterruptedException {
        try (InputStream in = source.open(service, region);
             InputStreamReader text = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            reader.read(text);
        }
    }
}
