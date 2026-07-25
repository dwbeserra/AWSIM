package awsim;

import awsim.AwsRegion;
import awsim.Ec2ProfileCatalog;
import awsim.PurchaseOption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MicroserviceScenarioValidator {
    private MicroserviceScenarioValidator() {
    }

    public static List<String> validate(MicroserviceScenarioConfig cfg) {
        List<String> errors = new ArrayList<>();
        if (cfg.getTitle() == null || cfg.getTitle().trim().isEmpty()) {
            errors.add("title must not be blank");
        }
        if (!knownRegion(cfg.getRegion())) errors.add("unknown AWS region: " + cfg.getRegion());
        if (!knownOption(cfg.getPurchaseOption())) errors.add("unknown purchase option: " + cfg.getPurchaseOption());
        if (cfg.getDurationSeconds() <= 0) errors.add("durationSeconds must be > 0");
        if (cfg.getIntervalSeconds() <= 0) errors.add("intervalSeconds must be > 0");
        if (cfg.getIntervalSeconds() > cfg.getDurationSeconds()) errors.add("intervalSeconds must not exceed durationSeconds");
        if (cfg.getArrivalRatesRps().isEmpty()) errors.add("arrivalRatesRps must not be empty");
        for (double rate : cfg.getArrivalRatesRps()) {
            if (rate < 0.0 || !Double.isFinite(rate)) errors.add("arrival rates must be finite and >= 0");
        }
        if (cfg.getServices().isEmpty()) errors.add("at least one microservice is required");
        Set<String> names = new HashSet<>();
        for (MicroserviceSpec service : cfg.getServices()) {
            if (service.getName().trim().isEmpty()) errors.add("service name must not be blank");
            if (!names.add(service.getName())) errors.add("duplicate service name: " + service.getName());
            if (Ec2ProfileCatalog.standard().get(service.getInstanceType()) == null) {
                errors.add("unknown instance profile for " + service.getName() + ": " + service.getInstanceType());
            }
            if (service.getMinReplicas() <= 0) errors.add(service.getName() + " minReplicas must be > 0");
            if (service.getMaxReplicas() < service.getMinReplicas()) errors.add(service.getName() + " maxReplicas must be >= minReplicas");
            if (service.getInitialReplicas() < service.getMinReplicas()
                    || service.getInitialReplicas() > service.getMaxReplicas()) {
                errors.add(service.getName() + " initialReplicas must be between min and max");
            }
            if (service.getRequestMi() <= 0.0) errors.add(service.getName() + " requestMi must be > 0");
            if (service.getTargetUtilization() <= 0.0 || service.getTargetUtilization() > 1.0) {
                errors.add(service.getName() + " targetUtilization must be in (0,1]");
            }
        }
        if (cfg.getStartupDelaySeconds() < 0) errors.add("startupDelaySeconds must be >= 0");
        if (cfg.getScaleOutCooldownSeconds() < 0) errors.add("scaleOutCooldownSeconds must be >= 0");
        if (cfg.getScaleInCooldownSeconds() < 0) errors.add("scaleInCooldownSeconds must be >= 0");
        if (cfg.getMaxQueueRequests() < 0.0) errors.add("maxQueueRequests must be >= 0");
        if (cfg.getSloSeconds() <= 0.0) errors.add("sloSeconds must be > 0");
        if (cfg.getOfficialPriceCacheTtlHours() <= 0.0) errors.add("officialPriceCacheTtlHours must be > 0");

        String mode = cfg.getPriceCatalogMode() == null ? "" : cfg.getPriceCatalogMode().toUpperCase();
        if (!"ILLUSTRATIVE".equals(mode) && !"CACHE".equals(mode) && !"OFFICIAL_AUTO".equals(mode)) {
            errors.add("priceCatalogMode must be ILLUSTRATIVE, CACHE, or OFFICIAL_AUTO");
        }
        if ("CACHE".equals(mode)
                && (cfg.getPriceCatalogFile() == null || cfg.getPriceCatalogFile().trim().isEmpty())) {
            errors.add("priceCatalogFile is required when priceCatalogMode=CACHE");
        }
        if ("OFFICIAL_AUTO".equals(mode) && "SPOT".equalsIgnoreCase(cfg.getPurchaseOption())) {
            errors.add("OFFICIAL_AUTO cannot obtain live Spot prices from the public Bulk API; use CACHE");
        }
        return errors;
    }

    public static void requireValid(MicroserviceScenarioConfig cfg) {
        List<String> errors = validate(cfg);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid microservice scenario:\n - " + String.join("\n - ", errors));
        }
    }

    private static boolean knownRegion(String value) {
        for (AwsRegion region : AwsRegion.values()) {
            if (region.name().equalsIgnoreCase(value) || region.getCode().equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    private static boolean knownOption(String value) {
        for (PurchaseOption option : PurchaseOption.values()) {
            if (option.name().equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
