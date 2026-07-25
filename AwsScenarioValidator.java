package awsim;

import awsim.AwsRegion;
import awsim.Ec2InstanceProfile;
import awsim.PurchaseOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AwsScenarioValidator {
    private AwsScenarioValidator() {
    }

    public static List<String> validate(AwsScenarioConfig cfg, Ec2InstanceProfile profile) {
        List<String> errors = new ArrayList<>();
        if (cfg.getTitle() == null || cfg.getTitle().trim().isEmpty()) {
            errors.add("title must not be blank");
        }
        if (!isRegion(cfg.getRegion())) {
            errors.add("unknown AWS region: " + cfg.getRegion());
        }
        if (!isPurchaseOption(cfg.getPurchaseOption())) {
            errors.add("unknown purchase option: " + cfg.getPurchaseOption());
        }
        positive(errors, "vmCount", cfg.getVmCount());
        positive(errors, "hostCount", cfg.getHostCount());
        positive(errors, "hostPes", cfg.getHostPes());
        positive(errors, "hostPeMips", cfg.getHostPeMips());
        positive(errors, "hostRamMb", cfg.getHostRamMb());
        positive(errors, "hostStorageMb", cfg.getHostStorageMb());
        positive(errors, "hostBwMb", cfg.getHostBwMb());
        positive(errors, "cloudletCount", cfg.getCloudletCount());
        positive(errors, "cloudletLength (MI)", cfg.getCloudletLength());
        positive(errors, "cloudletPes", cfg.getCloudletPes());
        nonNegative(errors, "cloudletFileSizeMb", cfg.getCloudletFileSizeMb());
        nonNegative(errors, "cloudletOutputSizeMb", cfg.getCloudletOutputSizeMb());

        if (cfg.getCloudletPes() > profile.getPes()) {
            errors.add("cloudletPes exceeds the selected VM profile's PEs");
        }
        if (cfg.getHostPeMips() < profile.getMips()) {
            errors.add("hostPeMips must be >= VM MIPS per PE (" + profile.getMips() + ")");
        }

        long fitByPes = cfg.getHostPes() / profile.getPes();
        long fitByRam = cfg.getHostRamMb() / profile.getRamMb();
        long fitByBw = cfg.getHostBwMb() / profile.getBwMb();
        long fitByStorage = cfg.getHostStorageMb() / profile.getImageSizeMb();
        long fitPerHost = Math.min(Math.min(fitByPes, fitByRam), Math.min(fitByBw, fitByStorage));
        if (fitPerHost <= 0) {
            errors.add("the selected VM profile cannot fit on a configured host");
        } else if (fitPerHost * cfg.getHostCount() < cfg.getVmCount()) {
            errors.add("configured hosts can place at most " + (fitPerHost * cfg.getHostCount())
                    + " VMs of type " + profile.getInstanceType()
                    + ", but vmCount is " + cfg.getVmCount());
        }

        nonNegative(errors, "EBS size", cfg.getEbsSizeGb());
        nonNegative(errors, "FSx size", cfg.getFsxStorageGb());
        nonNegative(errors, "S3 stored data", cfg.getS3StoredDataGb());
        nonNegative(errors, "fixed outbound traffic", cfg.getFixedOutboundInternetGb());
        positive(errors, "sequentialBlockSize", cfg.getSequentialBlockSize());
        nonNegative(errors, "interruptionRatePerHour", cfg.getInterruptionRatePerHour());
        nonNegative(errors, "restartOverheadHours", cfg.getRestartOverheadHours());
        nonNegative(errors, "checkpointIntervalHours", cfg.getCheckpointIntervalHours());
        nonNegative(errors, "performanceCv", cfg.getPerformanceCv());
        if (cfg.getPerformanceMinMultiplier() <= 0.0) {
            errors.add("performanceMinMultiplier must be > 0");
        }
        if (cfg.getPerformanceMaxMultiplier() < cfg.getPerformanceMinMultiplier()) {
            errors.add("performanceMaxMultiplier must be >= performanceMinMultiplier");
        }
        String catalogMode = cfg.getPriceCatalogMode() == null
                ? ""
                : cfg.getPriceCatalogMode().trim().toUpperCase();
        if (!"ILLUSTRATIVE".equals(catalogMode)
                && !"CACHE".equals(catalogMode)
                && !"OFFICIAL_AUTO".equals(catalogMode)) {
            errors.add("priceCatalogMode must be ILLUSTRATIVE, CACHE, or OFFICIAL_AUTO");
        }
        if ("CACHE".equals(catalogMode)
                && (cfg.getPriceCatalogFile() == null
                || cfg.getPriceCatalogFile().trim().isEmpty())) {
            errors.add("priceCatalogFile is required when priceCatalogMode=CACHE");
        }
        if ("OFFICIAL_AUTO".equals(catalogMode)
                && "SPOT".equalsIgnoreCase(cfg.getPurchaseOption())) {
            errors.add("OFFICIAL_AUTO uses the public AWS Price List Bulk API, "
                    + "which does not publish live Spot prices; use CACHE for Spot");
        }
        if (cfg.getOfficialPriceCacheTtlHours() <= 0.0) {
            errors.add("officialPriceCacheTtlHours must be > 0");
        }
        String policy = cfg.getBindingPolicy() == null
                ? ""
                : cfg.getBindingPolicy().toLowerCase();
        if (!"roundrobin".equals(policy)
                && !"blocks".equals(policy)
                && !"sequentialblocks".equals(policy)) {
            errors.add("bindingPolicy must be roundrobin, blocks, or sequentialblocks");
        }
        return Collections.unmodifiableList(errors);
    }

    public static void requireValid(AwsScenarioConfig cfg, Ec2InstanceProfile profile) {
        List<String> errors = validate(cfg, profile);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid AWSIM scenario:\n - "
                    + String.join("\n - ", errors));
        }
    }

    private static boolean isRegion(String value) {
        for (AwsRegion region : AwsRegion.values()) {
            if (region.name().equalsIgnoreCase(value)
                    || region.getCode().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPurchaseOption(String value) {
        for (PurchaseOption option : PurchaseOption.values()) {
            if (option.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private static void positive(List<String> errors, String name, long value) {
        if (value <= 0) {
            errors.add(name + " must be > 0");
        }
    }

    private static void nonNegative(List<String> errors, String name, double value) {
        if (value < 0.0) {
            errors.add(name + " must be >= 0");
        }
    }
}
