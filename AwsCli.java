package awsim;

import awsim.AwsScenarioConfig;
import awsim.AwsScenarioValidator;
import awsim.SimpleScenarioConfigLoader;
import awsim.AwsScenarioExecutor;
import awsim.Ec2InstanceProfile;
import awsim.Ec2ProfileCatalog;
import awsim.AwsimGui;
import awsim.MicroserviceAutoscalingSimulator;
import awsim.MicroserviceScenarioConfig;
import awsim.MicroserviceScenarioLoader;
import awsim.MicroserviceScenarioValidator;
import awsim.MicroserviceSimulationReport;
import awsim.AwsSimulationReport;
import org.cloudbus.cloudsim.Log;

import java.nio.file.Path;
import java.util.List;

public final class AwsCli {
    private AwsCli() {
    }

    public static void main(String[] args) throws Exception {
        String configPath = option(args, "--config=");
        String microserviceConfigPath = option(args, "--microservices=");
        String scenario = option(args, "--scenario=");
        boolean csv = hasFlag(args, "--csv");
        boolean validateOnly = hasFlag(args, "--validate");

        if (hasFlag(args, "--help")) {
            printHelp();
            return;
        }
        if (hasFlag(args, "--list-profiles")) {
            printProfiles();
            return;
        }
        if (hasFlag(args, "--gui")) {
            AwsimGui.main(new String[0]);
            return;
        }
        if (microserviceConfigPath != null) {
            runMicroservices(Path.of(microserviceConfigPath), csv, validateOnly);
            return;
        }
        if (scenario != null && configPath == null) {
            runBuiltIn(scenario, args);
            return;
        }

        AwsScenarioConfig config = configPath == null
                ? new AwsScenarioConfig()
                : SimpleScenarioConfigLoader.load(Path.of(configPath));
        applyOverrides(config, args);

        Ec2InstanceProfile profile = Ec2ProfileCatalog.standard()
                .require(config.getInstanceType());
        List<String> errors = AwsScenarioValidator.validate(config, profile);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid AWSIM scenario:\n - "
                    + String.join("\n - ", errors));
        }
        if (validateOnly) {
            System.out.println("VALID: " + config.getTitle());
            return;
        }

        if (csv) {
            Log.disable();
        }
        AwsSimulationReport report = new AwsScenarioExecutor().run(config);
        if (csv) {
            System.out.println(AwsSimulationReport.csvHeader());
            System.out.println(report.toCsvRow());
        } else {
            System.out.println(report.toPrettyString());
        }
    }

    private static void applyOverrides(AwsScenarioConfig config, String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--region=")) {
                config.setRegion(value(arg));
            } else if (arg.startsWith("--instance=")) {
                config.setInstanceType(value(arg));
            } else if (arg.startsWith("--vms=")) {
                config.setVmCount(Integer.parseInt(value(arg)));
            } else if (arg.startsWith("--cloudlets=")) {
                config.setCloudletCount(Integer.parseInt(value(arg)));
            } else if (arg.startsWith("--length-mi=")) {
                config.setCloudletLength(Long.parseLong(value(arg)));
            } else if (arg.startsWith("--price-catalog=")) {
                config.setPriceCatalogFile(value(arg));
                config.setPriceCatalogMode("CACHE");
            } else if (arg.equals("--official-prices")) {
                config.setPriceCatalogMode("OFFICIAL_AUTO");
            } else if (arg.startsWith("--official-cache=")) {
                config.setOfficialPriceCacheFile(value(arg));
            } else if (arg.startsWith("--price-cache-ttl-hours=")) {
                config.setOfficialPriceCacheTtlHours(Double.parseDouble(value(arg)));
            } else if (arg.equals("--variable-pricing")) {
                config.setVariablePricing(true);
            } else if (arg.equals("--spot-availability")) {
                config.setSpotAvailability(true);
            } else if (arg.equals("--performance-variability")) {
                config.setPerformanceVariability(true);
            }
        }
    }

    private static void runBuiltIn(String scenario, String[] args) throws Exception {
        switch (scenario.toLowerCase()) {
            case "sim1":
                AwsSimulation1.main(new String[0]);
                break;
            case "sim2":
                AwsSimulation2.main(new String[0]);
                break;
            case "sim3":
                AwsSimulation3.main(new String[0]);
                break;
            case "sim4":
                AwsSimulation4.main(new String[0]);
                break;
            case "sim5":
                AwsSimulation5InstanceSensitivity.main(new String[0]);
                break;
            case "sim6":
                AwsSimulation6SpotInterruptions.main(new String[0]);
                break;
            case "sim7":
                AwsSimulation7VariablePricing.main(new String[0]);
                break;
            case "sim8":
                AwsSimulation8SpotAndVariablePricing.main(new String[0]);
                break;
            case "sim9":
                String repetitions = option(args, "--repetitions=");
                AwsSimulation9PerformanceVariability.main(
                        repetitions == null ? new String[0] : new String[]{repetitions});
                break;
            case "mobile-simple":
                AwsMobileCloudSimpleExample.main(new String[0]);
                break;
            case "mobile-complex":
                AwsMobileCloudComplexExample.main(new String[0]);
                break;
            case "microservices":
                AwsMicroserviceAutoscalingExample.main(new String[0]);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown built-in scenario: " + scenario
                                + ". Use sim1..sim9, microservices, mobile-simple, or mobile-complex.");
        }
    }

    private static void runMicroservices(
            Path configPath,
            boolean csv,
            boolean validateOnly) throws Exception {
        MicroserviceScenarioConfig config = MicroserviceScenarioLoader.load(configPath);
        List<String> errors = MicroserviceScenarioValidator.validate(config);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid microservice scenario:\n - " + String.join("\n - ", errors));
        }
        if (validateOnly) {
            System.out.println("VALID: " + config.getTitle());
            return;
        }
        MicroserviceSimulationReport report =
                new MicroserviceAutoscalingSimulator().run(config);
        if (csv) {
            System.out.println(MicroserviceSimulationReport.csvHeader());
            System.out.println(report.toCsvRow());
        } else {
            System.out.println(report.toPrettyString());
        }
    }

    private static void printProfiles() {
        System.out.println("instance_type,mips_per_pe,pes,ram_mb,bw_mb,image_size_mb,mips_status,calibration_date");
        for (Ec2InstanceProfile profile : Ec2ProfileCatalog.standard().asMap().values()) {
            System.out.println(profile.getInstanceType() + ","
                    + profile.getMips() + ","
                    + profile.getPes() + ","
                    + profile.getRamMb() + ","
                    + profile.getBwMb() + ","
                    + profile.getImageSizeMb() + ","
                    + (profile.isEstimatedMips() ? "ESTIMATED" : "CALIBRATED") + ","
                    + profile.getCalibrationDate());
        }
    }

    private static void printHelp() {
        System.out.println("AWSIM CLI (CloudSim 4.0)");
        System.out.println("  --config=FILE              Run a flat YAML or JSON scenario");
        System.out.println("  --microservices=FILE       Run a request/autoscaling scenario");
        System.out.println("  --scenario=sim1..sim9      Run a built-in batch scenario");
        System.out.println("  --scenario=microservices   Run the built-in autoscaling example");
        System.out.println("  --gui                      Launch the Swing graphical interface");
        System.out.println("  --validate                 Validate without simulating");
        System.out.println("  --list-profiles            Print the EC2-to-CloudSim profile catalog");
        System.out.println("  --csv                      Print a machine-readable result");
        System.out.println("  --region=CODE              Override the region");
        System.out.println("  --instance=TYPE            Override the EC2 instance profile");
        System.out.println("  --vms=N --cloudlets=N      Override workload scale");
        System.out.println("  --length-mi=N              Override Cloudlet length in MI");
        System.out.println("  --price-catalog=FILE       Use a normalized official-price cache");
        System.out.println("  --official-prices          Refresh current official On-Demand prices");
        System.out.println("  --official-cache=FILE      Automatic normalized cache location");
        System.out.println("  --price-cache-ttl-hours=N  Maximum automatic cache age");
        System.out.println("  --performance-variability  Enable seeded runtime variation");
        System.out.println("  --repetitions=N            Repetitions for sim9");
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static String option(String[] args, String prefix) {
        for (String arg : args) {
            if (arg.startsWith(prefix)) {
                return arg.substring(prefix.length());
            }
        }
        return null;
    }

    private static String value(String arg) {
        return arg.substring(arg.indexOf('=') + 1);
    }
}
