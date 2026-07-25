package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class AwsDatacenterBroker extends DatacenterBroker {
    public AwsDatacenterBroker(String name) throws Exception {
        super(name);
    }

    public void bindCloudletsRoundRobin(List<? extends Cloudlet> cloudlets, List<? extends Vm> vms) {
        if (vms == null || vms.isEmpty()) {
            throw new IllegalArgumentException("VM list cannot be empty");
        }
        int i = 0;
        for (Cloudlet cloudlet : cloudlets) {
            Vm vm = vms.get(i % vms.size());
            bindCloudletToVm(cloudlet.getCloudletId(), vm.getId());
            i++;
        }
    }

    public void bindCloudletsSequentialBlocks(List<? extends Cloudlet> cloudlets, List<? extends Vm> vms, int blockSize) {
        if (vms == null || vms.isEmpty()) {
            throw new IllegalArgumentException("VM list cannot be empty");
        }
        if (blockSize <= 0) {
            throw new IllegalArgumentException("blockSize must be > 0");
        }
        for (int i = 0; i < cloudlets.size(); i++) {
            Vm vm = vms.get((i / blockSize) % vms.size());
            bindCloudletToVm(cloudlets.get(i).getCloudletId(), vm.getId());
        }
    }
}
