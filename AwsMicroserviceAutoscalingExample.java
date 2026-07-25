package awsim;

import awsim.MicroserviceAutoscalingSimulator;
import awsim.MicroserviceScenarioConfig;
import awsim.MicroserviceScenarioLoader;
import awsim.MicroserviceSimulationReport;

import java.nio.file.Path;

public final class AwsMicroserviceAutoscalingExample {
    private AwsMicroserviceAutoscalingExample() {
    }

    public static void main(String[] args) throws Exception {
        MicroserviceScenarioConfig config = args.length == 0
                ? new MicroserviceScenarioConfig()
                : MicroserviceScenarioLoader.load(Path.of(args[0]));
        MicroserviceSimulationReport report =
                new MicroserviceAutoscalingSimulator().run(config);
        System.out.println(report.toPrettyString());
    }
}
