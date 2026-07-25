package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioValidator;
import awsim.AwsScenarioExecutor;
import awsim.SimulationTime;
import awsim.AwsRegion;
import awsim.AvailabilityOutcome;
import awsim.Ec2ProfileCatalog;
import awsim.GaussianPerformanceVariationModel;
import awsim.PurchaseOption;
import awsim.SpotInterruptionAvailabilityModel;
import awsim.StandardEc2Profiles;
import awsim.AwsVm;
import awsim.AwsVmFactory;
import awsim.AwsPriceListApiCatalog;
import awsim.AwsOfficialPriceCache;
import awsim.GaussianPriceShockModel;
import awsim.PriceDimension;
import awsim.MicroserviceAutoscalingSimulator;
import awsim.MicroserviceScenarioConfig;
import awsim.MicroserviceSimulationReport;
import awsim.MicroserviceSpec;
import awsim.AwsimGui;
import awsim.AwsSimulationReport;
import org.cloudbus.cloudsim.Log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class AwsimSelfTest {
    private AwsimSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        testTimeConversion();
        testDeterministicModels();
        testValidation();
        testNormalizedCatalog();
        testOfficialPriceNormalization();
        testOfficialPriceCacheReuse();
        testCloudSim40Integration();
        testMicroserviceAutoscaling();
        AwsimGui.main(new String[]{"--self-check"});
        System.out.println("AWSIM SELF-TEST: PASS");
    }

    private static void testTimeConversion() {
        close("180000 seconds to hours", 50.0, SimulationTime.secondsToHours(180_000.0), 1e-12);
        require(SimulationTime.recalibrateLegacyLengthMi(200_000L) == 720_000_000L,
                "legacy MI recalibration");
    }

    private static void testDeterministicModels() {
        GaussianPriceShockModel prices = new GaussianPriceShockModel(42L, 0.1, 0.2, 0.05);
        double first = prices.multiplier(
                PriceDimension.EC2,
                AwsRegion.US_EAST_1,
                "c7i.2xlarge",
                PurchaseOption.SPOT);
        double second = prices.multiplier(
                PriceDimension.EC2,
                AwsRegion.US_EAST_1,
                "c7i.2xlarge",
                PurchaseOption.SPOT);
        close("idempotent price shock", first, second, 0.0);

        AwsVm vm = AwsVmFactory.createVm(
                7,
                1,
                AwsRegion.US_EAST_1,
                PurchaseOption.SPOT,
                StandardEc2Profiles.C7I_2XLARGE,
                null);
        SpotInterruptionAvailabilityModel availability =
                new SpotInterruptionAvailabilityModel(0.2, 0.5, 1.0, 99L);
        AvailabilityOutcome a = availability.assess(vm, 20.0);
        AvailabilityOutcome b = availability.assess(vm, 20.0);
        require(a.getInterruptionCount() == b.getInterruptionCount(),
                "idempotent interruption count");
        close("idempotent adjusted runtime",
                a.getAdjustedRuntimeHours(),
                b.getAdjustedRuntimeHours(),
                0.0);

        GaussianPerformanceVariationModel performance =
                new GaussianPerformanceVariationModel(123L, 0.1, 0.6, 1.6);
        long lengthA = performance.realizedLengthMi(
                4,
                900_000_000L,
                StandardEc2Profiles.C7I_2XLARGE);
        long lengthB = performance.realizedLengthMi(
                4,
                900_000_000L,
                StandardEc2Profiles.C7I_2XLARGE);
        require(lengthA == lengthB, "idempotent performance realization");
    }

    private static void testValidation() {
        AwsScenarioConfig valid = new AwsScenarioConfig();
        require(AwsScenarioValidator.validate(
                valid,
                Ec2ProfileCatalog.standard().require(valid.getInstanceType())).isEmpty(),
                "default scenario validation");

        AwsScenarioConfig invalid = new AwsScenarioConfig();
        invalid.setHostPes(1);
        List<String> errors = AwsScenarioValidator.validate(
                invalid,
                Ec2ProfileCatalog.standard().require(invalid.getInstanceType()));
        require(!errors.isEmpty(), "capacity validation must reject an impossible host");
    }

    private static void testNormalizedCatalog() throws Exception {
        Path cache = Files.createTempFile("awsim-price-cache-", ".properties");
        Files.writeString(cache,
                "metadata.catalogId=test-official-cache\n"
                        + "metadata.source=https://pricing.us-east-1.amazonaws.com/\n"
                        + "metadata.retrievedAt=2026-07-25\n"
                        + "metadata.currency=USD\n"
                        + "ec2.us-east-1.ON_DEMAND.c7i.2xlarge=0.357\n");
        AwsPriceListApiCatalog catalog = new AwsPriceListApiCatalog(cache);
        close("normalized EC2 unit price",
                0.357,
                catalog.ec2Hourly(
                        AwsRegion.US_EAST_1,
                        "c7i.2xlarge",
                        PurchaseOption.ON_DEMAND),
                1e-12);
        require(!catalog.getMetadata().isIllustrative(), "official-cache metadata");
        Files.deleteIfExists(cache);
    }

    private static void testCloudSim40Integration() throws Exception {
        Log.disable();
        AwsScenarioConfig cfg = new AwsScenarioConfig();
        cfg.setTitle("CloudSim 4.0 one-hour integration test");
        cfg.setVmCount(1);
        cfg.setCloudletCount(4);
        cfg.setCloudletLength(18_000_000L);
        cfg.setFixedOutboundInternetGb(0.0);
        cfg.setS3StoredDataGb(0.0);
        cfg.setEbsSizeGb(0);
        AwsSimulationReport report = new AwsScenarioExecutor().run(cfg);
        require(report.getFinishedCloudlets().size() == 4,
                "all integration-test cloudlets finished");
        close("one-hour trace runtime", 1.0, report.getTraceMakespanHours(), 0.001);
        close("one-hour EC2 cost",
                0.357,
                report.getCostBreakdown().getEc2Cost(),
                1e-9);
    }

    private static void testOfficialPriceNormalization() throws Exception {
        Path cache = Files.createTempFile("awsim-official-refresh-", ".properties");
        Files.deleteIfExists(cache);
        AwsOfficialPriceCache.Requirements requirements =
                new AwsOfficialPriceCache.Requirements(
                        Collections.singleton("c7i.2xlarge"),
                        PurchaseOption.ON_DEMAND,
                        true,
                        true,
                        true,
                        true);
        AwsOfficialPriceCache.PriceFileSource source = (service, region) ->
                new ByteArrayInputStream(
                        fixture(service).getBytes(StandardCharsets.UTF_8));
        AwsOfficialPriceCache.refresh(
                cache,
                AwsRegion.US_EAST_1,
                requirements,
                source);
        AwsPriceListApiCatalog catalog = new AwsPriceListApiCatalog(cache);
        close("official EC2 normalization",
                0.357,
                catalog.ec2Hourly(
                        AwsRegion.US_EAST_1,
                        "c7i.2xlarge",
                        PurchaseOption.ON_DEMAND),
                1e-12);
        close("official EBS normalization",
                8.0,
                catalog.ebsMonthly(
                        AwsRegion.US_EAST_1,
                        new awsim.EbsVolumeSpec(100, 3000, 125)),
                1e-12);
        close("official S3 normalization",
                2.3,
                catalog.s3Monthly(
                        AwsRegion.US_EAST_1,
                        new awsim.S3BucketSpec(100.0, 0, 0)),
                1e-12);
        close("official S3 PUT normalization",
                0.005,
                catalog.s3PutPerThousand(AwsRegion.US_EAST_1),
                1e-12);
        close("official S3 GET normalization",
                0.0004,
                catalog.s3GetPerThousand(AwsRegion.US_EAST_1),
                1e-12);
        close("official transfer normalization",
                0.09,
                catalog.transferOutPerGb(AwsRegion.US_EAST_1),
                1e-12);
        Files.deleteIfExists(cache);
    }

    private static void testOfficialPriceCacheReuse() throws Exception {
        Path cache = Files.createTempFile("awsim-official-cache-reuse-", ".properties");
        Files.deleteIfExists(cache);
        AwsOfficialPriceCache.Requirements requirements =
                new AwsOfficialPriceCache.Requirements(
                        Collections.singleton("c7i.2xlarge"),
                        PurchaseOption.ON_DEMAND,
                        true,
                        true,
                        true,
                        true);
        AtomicInteger downloads = new AtomicInteger();
        AwsOfficialPriceCache.PriceFileSource source = (service, region) -> {
            downloads.incrementAndGet();
            return new ByteArrayInputStream(
                    fixture(service).getBytes(StandardCharsets.UTF_8));
        };

        AwsOfficialPriceCache.refreshIfStale(
                cache,
                AwsRegion.US_EAST_1,
                requirements,
                24.0,
                source);
        require(downloads.get() == 4,
                "first official-price refresh downloads four required service files");

        AwsOfficialPriceCache.refreshIfStale(
                cache,
                AwsRegion.US_EAST_1,
                requirements,
                24.0,
                source);
        require(downloads.get() == 4,
                "fresh complete official-price cache prevents redundant downloads");
        AwsPriceListApiCatalog catalog = new AwsPriceListApiCatalog(cache);
        require(catalog.getMetadata().getCatalogId().contains("AmazonEC2")
                        && catalog.getMetadata().getCatalogId().contains("AmazonS3")
                        && catalog.getMetadata().getCatalogId().contains("AmazonFSx")
                        && catalog.getMetadata().getCatalogId().contains("AWSDataTransfer"),
                "official-price cache preserves every service version");
        Files.deleteIfExists(cache);
    }

    private static void testMicroserviceAutoscaling() throws Exception {
        MicroserviceScenarioConfig config = new MicroserviceScenarioConfig();
        config.setArrivalRatesRps(Arrays.asList(
                20.0, 20.0, 40.0, 80.0, 140.0,
                140.0, 140.0, 80.0, 40.0, 20.0));
        MicroserviceSimulationReport first =
                new MicroserviceAutoscalingSimulator().run(config);
        MicroserviceSimulationReport second =
                new MicroserviceAutoscalingSimulator().run(config);
        require(first.getCompletedRequests() > 0.0,
                "microservice requests completed");
        require(!first.getScalingEvents().isEmpty(),
                "microservice target tracking produced scaling events");
        require(first.getTotalEc2Cost() > 0.0,
                "microservice EC2 cost is positive");
        close("deterministic microservice completion",
                first.getCompletedRequests(),
                second.getCompletedRequests(),
                0.0);
        close("deterministic microservice cost",
                first.getTotalEc2Cost(),
                second.getTotalEc2Cost(),
                0.0);

        MicroserviceScenarioConfig fixed = new MicroserviceScenarioConfig();
        fixed.setTitle("Fixed peak capacity regression");
        fixed.setArrivalRatesRps(config.getArrivalRatesRps());
        List<MicroserviceSpec> fixedServices = new ArrayList<>();
        for (MicroserviceSpec service : fixed.getServices()) {
            fixedServices.add(new MicroserviceSpec(
                    service.getName(),
                    service.getInstanceType(),
                    service.getMaxReplicas(),
                    service.getMaxReplicas(),
                    service.getMaxReplicas(),
                    service.getRequestMi(),
                    service.getTargetUtilization()));
        }
        fixed.setServices(fixedServices);
        MicroserviceSimulationReport fixedReport =
                new MicroserviceAutoscalingSimulator().run(fixed);
        require(fixedReport.getScalingEvents().isEmpty(),
                "fixed peak-capacity scenario has no scaling events");
        close("autoscaling and fixed capacity complete the same offered load",
                fixedReport.getCompletedRequests(),
                first.getCompletedRequests(),
                0.0);
        require(first.getTotalEc2Cost() < fixedReport.getTotalEc2Cost(),
                "target tracking costs less than fixed peak capacity in the regression trace");
        require(first.getMeanResponseSeconds() > fixedReport.getMeanResponseSeconds(),
                "startup delay exposes the expected autoscaling latency trade-off");
    }

    private static String fixture(String service) {
        String metadata = "\"FormatVersion\",\"v1.0\"\n"
                + "\"Disclaimer\",\"test\"\n"
                + "\"Publication Date\",\"2026-07-25T00:00:00Z\"\n"
                + "\"Version\",\"20260725000000\"\n"
                + "\"OfferCode\",\"" + service + "\"\n";
        if ("AmazonEC2".equals(service)) {
            return metadata
                    + "\"TermType\",\"PricePerUnit\",\"Unit\",\"Product Family\","
                    + "\"Instance Type\",\"Operating System\",\"Tenancy\",\"Pre Installed S/W\","
                    + "\"CapacityStatus\",\"operation\",\"Volume API Name\",\"usageType\"\n"
                    + "\"OnDemand\",\"0.357\",\"Hrs\",\"Compute Instance\",\"c7i.2xlarge\","
                    + "\"Linux\",\"Shared\",\"NA\",\"Used\",\"RunInstances\",\"\",\"BoxUsage:c7i.2xlarge\"\n"
                    + "\"OnDemand\",\"0.080\",\"GB-Mo\",\"Storage\",\"\",\"\",\"\",\"\",\"\",\"\","
                    + "\"gp3\",\"EBS:VolumeUsage.gp3\"\n"
                    + "\"OnDemand\",\"0.005\",\"IOPS-Mo\",\"System Operation\",\"\",\"\",\"\",\"\",\"\",\"\","
                    + "\"gp3\",\"EBS:VolumeP-IOPS.gp3\"\n"
                    + "\"OnDemand\",\"40.960\",\"GiBps-mo\",\"Provisioned Throughput\",\"\",\"\",\"\",\"\",\"\",\"\","
                    + "\"gp3\",\"EBS:VolumeP-Throughput.gp3\"\n";
        }
        if ("AmazonS3".equals(service)) {
            return metadata
                    + "\"TermType\",\"PricePerUnit\",\"Unit\",\"Product Family\","
                    + "\"Storage Class\",\"StartingRange\",\"Group\"\n"
                    + "\"OnDemand\",\"0.023\",\"GB-Mo\",\"Storage\",\"General Purpose\",\"0\",\"\"\n"
                    + "\"OnDemand\",\"0.000005\",\"Requests\",\"API Request\",\"\",\"0\",\"S3-API-Tier1\"\n"
                    + "\"OnDemand\",\"0.0000004\",\"Requests\",\"API Request\",\"\",\"0\",\"S3-API-Tier2\"\n";
        }
        if ("AmazonFSx".equals(service)) {
            return metadata
                    + "\"TermType\",\"PricePerUnit\",\"Unit\",\"Product Family\","
                    + "\"File system type\",\"operation\",\"Storage type\",\"Deployment option\","
                    + "\"Throughput capacity\",\"usageType\"\n"
                    + "\"OnDemand\",\"0.145\",\"GB-Mo\",\"Storage\",\"Lustre\","
                    + "\"CreateFileSystem:Lustre\",\"SSD\",\"Persistent\",\"125\",\"USE1-Storage.SSD.125\"\n"
                    + "\"OnDemand\",\"0.520\",\"MiBps-Mo\",\"Provisioned Throughput\",\"Lustre\","
                    + "\"CreateFileSystem:Lustre\",\"\",\"Persistent\",\"\",\"USE1-ThroughputCapacity.P2\"\n"
                    + "\"OnDemand\",\"0.055\",\"IOPS-Mo\",\"Provisioned IOPS\",\"Lustre\","
                    + "\"CreateFileSystem:Lustre\",\"\",\"Persistent\",\"\",\"USE1-MetadataIOPS\"\n"
                    + "\"OnDemand\",\"0.050\",\"GB-Mo\",\"Storage\",\"Lustre\","
                    + "\"CreateFileSystem:Lustre\",\"\",\"Persistent\",\"\",\"USE1-BackupUsage\"\n";
        }
        if ("AWSDataTransfer".equals(service)) {
            return metadata
                    + "\"TermType\",\"PricePerUnit\",\"Unit\",\"Transfer Type\","
                    + "\"From Region Code\",\"To Location\",\"usageType\",\"StartingRange\"\n"
                    + "\"OnDemand\",\"0.090\",\"GB\",\"AWS Outbound\",\"us-east-1\","
                    + "\"External\",\"DataTransfer-Out-Bytes\",\"0\"\n";
        }
        throw new IllegalArgumentException("Unexpected fixture service " + service);
    }

    private static void close(String name, double expected, double actual, double tolerance) {
        if (Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static void require(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name);
        }
    }
}
