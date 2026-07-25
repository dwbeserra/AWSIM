package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsRegion;
import awsim.PurchaseOption;

import java.nio.file.Path;
import java.util.Collections;

/**
 * Resolves illustrative, explicit-cache, and automatically refreshed official
 * price modes for batch scenarios.
 */
public final class AwsPriceCatalogResolver {
    private AwsPriceCatalogResolver() {
    }

    public static AwsPriceCatalog resolve(AwsScenarioConfig cfg, AwsRegion region)
            throws Exception {
        String mode = cfg.getPriceCatalogMode() == null
                ? "ILLUSTRATIVE"
                : cfg.getPriceCatalogMode().trim().toUpperCase();
        if (cfg.getPriceCatalogFile() != null
                && !cfg.getPriceCatalogFile().trim().isEmpty()
                && "ILLUSTRATIVE".equals(mode)) {
            mode = "CACHE";
        }

        if ("CACHE".equals(mode)) {
            return new AwsPriceListApiCatalog(Path.of(cfg.getPriceCatalogFile()));
        }
        if ("OFFICIAL_AUTO".equals(mode)) {
            AwsOfficialPriceCache.Requirements requirements =
                    new AwsOfficialPriceCache.Requirements(
                            Collections.singleton(cfg.getInstanceType()),
                            PurchaseOption.valueOf(cfg.getPurchaseOption().toUpperCase()),
                            cfg.getEbsSizeGb() > 0,
                            cfg.getFsxStorageGb() > 0,
                            cfg.getS3StoredDataGb() > 0,
                            cfg.getFixedOutboundInternetGb() > 0.0
                                    || cfg.getCloudletOutputSizeMb() > 0);
            Path cache = AwsOfficialPriceCache.refreshIfStale(
                    Path.of(cfg.getOfficialPriceCacheFile()),
                    region,
                    requirements,
                    cfg.getOfficialPriceCacheTtlHours());
            return new AwsPriceListApiCatalog(cache);
        }
        return new ExampleAwsPriceCatalog();
    }
}
