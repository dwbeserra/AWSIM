package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import awsim.AwsEnvironment;
import awsim.AwsVm;
import awsim.AwsPriceCatalog;
import awsim.AwsSimulationReport;
import awsim.CostBreakdown;

import java.util.List;

public class AwsSimulationRunner {
    public AwsSimulationReport run(String title, AwsDatacenterBroker broker, List<AwsVm> vms, AwsEnvironment environment, AwsPriceCatalog catalog) {
        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        List<Cloudlet> finished = broker.getCloudletReceivedList();
        AwsCostCalculator calculator = new AwsCostCalculator();
        AwsAccountingResult accounting = calculator.calculate(vms, finished, environment, catalog);
        return new AwsSimulationReport(
                title,
                finished,
                accounting.getCostBreakdown(),
                accounting.getTraceMakespanHours(),
                accounting.getAdjustedMakespanHours(),
                catalog.getMetadata());
    }
}
