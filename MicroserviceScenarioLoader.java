package awsim;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads a flat YAML/JSON microservice scenario.
 *
 * <p>The service chain uses
 * {@code name|instance|min|max|initial|requestMi|targetUtilization} records
 * separated by semicolons. Arrival rates are comma-separated requests/second
 * and repeat cyclically for the configured duration.
 */
public final class MicroserviceScenarioLoader {
    private static final Set<String> KEYS = new HashSet<>(Arrays.asList(
            "title", "region", "purchaseOption", "durationSeconds",
            "intervalSeconds", "arrivalRatesRps", "services",
            "startupDelaySeconds", "scaleOutCooldownSeconds",
            "scaleInCooldownSeconds", "maxQueueRequests", "sloSeconds",
            "priceCatalogMode", "priceCatalogFile", "officialPriceCacheFile",
            "officialPriceCacheTtlHours"));

    private MicroserviceScenarioLoader() {
    }

    public static MicroserviceScenarioConfig load(Path path) throws IOException {
        String name = path.getFileName().toString().toLowerCase();
        Map<String, String> values;
        if (name.endsWith(".json")) {
            values = json(Files.readString(path));
        } else if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            values = yaml(Files.readAllLines(path));
        } else {
            throw new IllegalArgumentException("Unsupported microservice config: " + path);
        }
        Set<String> unknown = new TreeSet<>(values.keySet());
        unknown.removeAll(KEYS);
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown microservice scenario keys: " + unknown);
        }
        MicroserviceScenarioConfig cfg = new MicroserviceScenarioConfig();
        if (values.containsKey("title")) cfg.setTitle(values.get("title"));
        if (values.containsKey("region")) cfg.setRegion(values.get("region"));
        if (values.containsKey("purchaseOption")) cfg.setPurchaseOption(values.get("purchaseOption"));
        if (values.containsKey("durationSeconds")) cfg.setDurationSeconds(integer(values, "durationSeconds"));
        if (values.containsKey("intervalSeconds")) cfg.setIntervalSeconds(integer(values, "intervalSeconds"));
        if (values.containsKey("arrivalRatesRps")) cfg.setArrivalRatesRps(parseRates(values.get("arrivalRatesRps")));
        if (values.containsKey("services")) cfg.setServices(parseServices(values.get("services")));
        if (values.containsKey("startupDelaySeconds")) cfg.setStartupDelaySeconds(integer(values, "startupDelaySeconds"));
        if (values.containsKey("scaleOutCooldownSeconds")) cfg.setScaleOutCooldownSeconds(integer(values, "scaleOutCooldownSeconds"));
        if (values.containsKey("scaleInCooldownSeconds")) cfg.setScaleInCooldownSeconds(integer(values, "scaleInCooldownSeconds"));
        if (values.containsKey("maxQueueRequests")) cfg.setMaxQueueRequests(number(values, "maxQueueRequests"));
        if (values.containsKey("sloSeconds")) cfg.setSloSeconds(number(values, "sloSeconds"));
        if (values.containsKey("priceCatalogMode")) cfg.setPriceCatalogMode(values.get("priceCatalogMode"));
        if (values.containsKey("priceCatalogFile")) cfg.setPriceCatalogFile(values.get("priceCatalogFile"));
        if (values.containsKey("officialPriceCacheFile")) cfg.setOfficialPriceCacheFile(values.get("officialPriceCacheFile"));
        if (values.containsKey("officialPriceCacheTtlHours")) cfg.setOfficialPriceCacheTtlHours(number(values, "officialPriceCacheTtlHours"));
        return cfg;
    }

    public static List<Double> parseRates(String text) {
        List<Double> rates = new ArrayList<>();
        for (String item : text.split(",")) {
            if (!item.trim().isEmpty()) {
                rates.add(Double.parseDouble(item.trim()));
            }
        }
        return rates;
    }

    public static List<MicroserviceSpec> parseServices(String text) {
        List<MicroserviceSpec> services = new ArrayList<>();
        for (String encoded : text.split(";")) {
            if (encoded.trim().isEmpty()) {
                continue;
            }
            String[] fields = encoded.trim().split("\\|", -1);
            if (fields.length != 7) {
                throw new IllegalArgumentException(
                        "Each service needs name|instance|min|max|initial|requestMi|targetUtilization: "
                                + encoded);
            }
            services.add(new MicroserviceSpec(
                    fields[0].trim(),
                    fields[1].trim(),
                    Integer.parseInt(fields[2].trim()),
                    Integer.parseInt(fields[3].trim()),
                    Integer.parseInt(fields[4].trim()),
                    Double.parseDouble(fields[5].trim()),
                    Double.parseDouble(fields[6].trim())));
        }
        return services;
    }

    public static String encodeRates(List<Double> values) {
        StringBuilder out = new StringBuilder();
        for (double value : values) {
            if (out.length() > 0) out.append(',');
            out.append(value);
        }
        return out.toString();
    }

    public static String encodeServices(List<MicroserviceSpec> values) {
        StringBuilder out = new StringBuilder();
        for (MicroserviceSpec value : values) {
            if (out.length() > 0) out.append(';');
            out.append(value.encode());
        }
        return out.toString();
    }

    private static int integer(Map<String, String> values, String key) {
        return Integer.parseInt(values.get(key));
    }

    private static double number(Map<String, String> values, String key) {
        return Double.parseDouble(values.get(key));
    }

    private static Map<String, String> json(String text) {
        Map<String, String> values = new HashMap<>();
        Pattern pattern = Pattern.compile(
                "\\\"([^\\\"]+)\\\"\\s*:\\s*(\\\"(?:\\\\.|[^\\\"])*\\\"|true|false|-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String value = matcher.group(2);
            if (value.startsWith("\"")) {
                value = value.substring(1, value.length() - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            }
            values.put(matcher.group(1), value);
        }
        return values;
    }

    private static Map<String, String> yaml(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int colon = line.indexOf(':');
            if (colon <= 0) continue;
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            if ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            values.put(key, value);
        }
        return values;
    }
}
