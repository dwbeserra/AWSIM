package awsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MicroserviceScenarioConfig {
    private String title = "AWSIM microservice autoscaling";
    private String region = "us-east-1";
    private String purchaseOption = "ON_DEMAND";
    private int durationSeconds = 3600;
    private int intervalSeconds = 60;
    private List<Double> arrivalRatesRps = new ArrayList<>();
    private List<MicroserviceSpec> services = new ArrayList<>();
    private int startupDelaySeconds = 60;
    private int scaleOutCooldownSeconds = 120;
    private int scaleInCooldownSeconds = 300;
    private double maxQueueRequests = 100_000.0;
    private double sloSeconds = 2.0;
    private String priceCatalogMode = "ILLUSTRATIVE";
    private String priceCatalogFile;
    private String officialPriceCacheFile = "target/aws-microservice-price-cache.properties";
    private double officialPriceCacheTtlHours = 24.0;

    public MicroserviceScenarioConfig() {
        arrivalRatesRps.add(20.0);
        arrivalRatesRps.add(20.0);
        arrivalRatesRps.add(80.0);
        arrivalRatesRps.add(140.0);
        arrivalRatesRps.add(140.0);
        arrivalRatesRps.add(60.0);
        arrivalRatesRps.add(20.0);
        services.add(new MicroserviceSpec(
                "api", "c7i.xlarge", 1, 8, 1, 480.0, 0.60));
        services.add(new MicroserviceSpec(
                "worker", "c7i.2xlarge", 1, 10, 1, 1000.0, 0.65));
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getPurchaseOption() { return purchaseOption; }
    public void setPurchaseOption(String purchaseOption) { this.purchaseOption = purchaseOption; }
    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
    public List<Double> getArrivalRatesRps() { return Collections.unmodifiableList(arrivalRatesRps); }
    public void setArrivalRatesRps(List<Double> arrivalRatesRps) { this.arrivalRatesRps = new ArrayList<>(arrivalRatesRps); }
    public List<MicroserviceSpec> getServices() { return Collections.unmodifiableList(services); }
    public void setServices(List<MicroserviceSpec> services) { this.services = new ArrayList<>(services); }
    public int getStartupDelaySeconds() { return startupDelaySeconds; }
    public void setStartupDelaySeconds(int startupDelaySeconds) { this.startupDelaySeconds = startupDelaySeconds; }
    public int getScaleOutCooldownSeconds() { return scaleOutCooldownSeconds; }
    public void setScaleOutCooldownSeconds(int scaleOutCooldownSeconds) { this.scaleOutCooldownSeconds = scaleOutCooldownSeconds; }
    public int getScaleInCooldownSeconds() { return scaleInCooldownSeconds; }
    public void setScaleInCooldownSeconds(int scaleInCooldownSeconds) { this.scaleInCooldownSeconds = scaleInCooldownSeconds; }
    public double getMaxQueueRequests() { return maxQueueRequests; }
    public void setMaxQueueRequests(double maxQueueRequests) { this.maxQueueRequests = maxQueueRequests; }
    public double getSloSeconds() { return sloSeconds; }
    public void setSloSeconds(double sloSeconds) { this.sloSeconds = sloSeconds; }
    public String getPriceCatalogMode() { return priceCatalogMode; }
    public void setPriceCatalogMode(String priceCatalogMode) { this.priceCatalogMode = priceCatalogMode; }
    public String getPriceCatalogFile() { return priceCatalogFile; }
    public void setPriceCatalogFile(String priceCatalogFile) { this.priceCatalogFile = priceCatalogFile; }
    public String getOfficialPriceCacheFile() { return officialPriceCacheFile; }
    public void setOfficialPriceCacheFile(String officialPriceCacheFile) { this.officialPriceCacheFile = officialPriceCacheFile; }
    public double getOfficialPriceCacheTtlHours() { return officialPriceCacheTtlHours; }
    public void setOfficialPriceCacheTtlHours(double officialPriceCacheTtlHours) { this.officialPriceCacheTtlHours = officialPriceCacheTtlHours; }
}
