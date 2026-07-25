package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.core.CloudSim;
import awsim.AwsScenarioConfig;
import awsim.AwsScenarioValidator;
import awsim.AwsCloudletFactory;
import awsim.AwsDatacenterFactory;
import awsim.AwsVmFactory;
import awsim.*;
import awsim.*;
import awsim.AwsSimulationReport;

import java.util.Calendar;
import java.util.List;

public class AwsScenarioExecutor {
    public AwsSimulationReport run(AwsScenarioConfig cfg) throws Exception {
        Ec2InstanceProfile profile = Ec2ProfileCatalog.standard().require(cfg.getInstanceType());
        AwsScenarioValidator.requireValid(cfg, profile);
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter dc = AwsDatacenterFactory.createDatacenter(
                cfg.getRegion() + "-dc",
                cfg.getHostCount(),
                cfg.getHostPes(),
                cfg.getHostPeMips(),
                cfg.getHostRamMb(),
                cfg.getHostStorageMb(),
                cfg.getHostBwMb(),
                0.0, 0.0, 0.0, 0.0);

        AwsDatacenterBroker broker = new AwsDatacenterBroker("CfgBroker-" + cfg.getRegion());
        int brokerId = broker.getId();

        EbsVolumeSpec ebs = cfg.getEbsSizeGb() > 0 ? new EbsVolumeSpec(cfg.getEbsSizeGb(), cfg.getEbsIops(), cfg.getEbsThroughputMbps()) : null;
        List<AwsVm> vms = AwsVmFactory.replicate(
                brokerId,
                parseRegion(cfg.getRegion()),
                PurchaseOption.valueOf(cfg.getPurchaseOption().toUpperCase()),
                profile,
                ebs,
                cfg.getVmCount());
        broker.submitVmList(vms);

        PerformanceModel performanceModel = cfg.isPerformanceVariability()
                ? new GaussianPerformanceVariationModel(
                        cfg.getPerformanceSeed(),
                        cfg.getPerformanceCv(),
                        cfg.getPerformanceMinMultiplier(),
                        cfg.getPerformanceMaxMultiplier())
                : new NoPerformanceVariationModel();
        List<Cloudlet> cloudlets = AwsCloudletFactory.many(
                cfg.getCloudletCount(),
                cfg.getCloudletLength(),
                cfg.getCloudletPes(),
                cfg.getCloudletFileSizeMb(),
                cfg.getCloudletOutputSizeMb(),
                brokerId,
                profile,
                performanceModel);
        broker.submitCloudletList(cloudlets);

        String policy = cfg.getBindingPolicy() == null ? "roundrobin" : cfg.getBindingPolicy().toLowerCase();
        if ("blocks".equals(policy) || "sequentialblocks".equals(policy)) {
            broker.bindCloudletsSequentialBlocks(cloudlets, vms, cfg.getSequentialBlockSize());
        } else {
            broker.bindCloudletsRoundRobin(cloudlets, vms);
        }

        FsxLustreSpec fsx = cfg.getFsxStorageGb() > 0 ? new FsxLustreSpec(cfg.getFsxStorageGb(), cfg.getFsxThroughputMbps(), cfg.getFsxMetadataIops(), cfg.getFsxBackupGb()) : null;
        S3BucketSpec s3 = cfg.getS3StoredDataGb() > 0 ? new S3BucketSpec(cfg.getS3StoredDataGb(), cfg.getS3BasePutRequests(), cfg.getS3BaseGetRequests()) : null;
        AvailabilityModel availability = cfg.isSpotAvailability()
                ? new SpotInterruptionAvailabilityModel(
                        cfg.getInterruptionRatePerHour(),
                        cfg.getRestartOverheadHours(),
                        cfg.getCheckpointIntervalHours(),
                        cfg.getPriceSeed())
                : new NoAvailabilityModel();

        AwsEnvironment environment = new AwsEnvironment(
                parseRegion(cfg.getRegion()),
                fsx,
                s3,
                cfg.getFixedOutboundInternetGb(),
                availability);

        AwsPriceCatalog catalog = AwsPriceCatalogResolver.resolve(
                cfg,
                parseRegion(cfg.getRegion()));
        if (cfg.isVariablePricing()) {
            catalog = new VariableAwsPriceCatalog(
                    catalog,
                    new GaussianPriceShockModel(cfg.getPriceSeed(), cfg.getOnDemandVolatility(), cfg.getSpotVolatility(), cfg.getStorageVolatility()));
        }

        return new AwsSimulationRunner().run(cfg.getTitle(), broker, vms, environment, catalog);
    }

    private AwsRegion parseRegion(String region) {
        for (AwsRegion r : AwsRegion.values()) {
            if (r.getCode().equalsIgnoreCase(region) || r.name().equalsIgnoreCase(region)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown AWS region: " + region);
    }
}
