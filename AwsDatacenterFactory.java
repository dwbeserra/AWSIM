package awsim;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class AwsDatacenterFactory {
    private AwsDatacenterFactory() {
    }

    public static Datacenter createDatacenter(
            String name,
            int hostCount,
            int hostPes,
            int peMips,
            int ramMb,
            long storageMb,
            int bwMb,
            double costPerSecond,
            double costPerMem,
            double costPerStorage,
            double costPerBw) throws Exception {

        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < hostCount; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int pe = 0; pe < hostPes; pe++) {
                peList.add(createPe(pe, new PeProvisionerSimple(peMips)));
            }
            hostList.add(createHost(
                    i,
                    new RamProvisionerSimple(ramMb),
                    new BwProvisionerSimple(bwMb),
                    storageMb,
                    peList,
                    new VmSchedulerTimeShared(peList)));
        }

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86",
                "Linux",
                "Xen",
                hostList,
                0.0,
                costPerSecond,
                costPerMem,
                costPerStorage,
                costPerBw);

        return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
    }

    private static Pe createPe(int id, PeProvisioner provisioner) throws Exception {
        try {
            Constructor<Pe> constructor = Pe.class.getConstructor(
                    int.class,
                    PeProvisioner.class);
            return constructor.newInstance(id, provisioner);
        } catch (NoSuchMethodException ignored) {
            Constructor<Pe> constructor = Pe.class.getConstructor(
                    PeProvisioner.class);
            return constructor.newInstance(provisioner);
        }
    }

    private static Host createHost(
            int id,
            RamProvisioner ramProvisioner,
            BwProvisioner bwProvisioner,
            long storageMb,
            List<Pe> peList,
            VmScheduler vmScheduler) throws Exception {
        try {
            Constructor<Host> constructor = Host.class.getConstructor(
                    int.class,
                    RamProvisioner.class,
                    BwProvisioner.class,
                    long.class,
                    List.class,
                    VmScheduler.class);
            return constructor.newInstance(
                    id,
                    ramProvisioner,
                    bwProvisioner,
                    storageMb,
                    peList,
                    vmScheduler);
        } catch (NoSuchMethodException ignored) {
            Constructor<Host> constructor = Host.class.getConstructor(
                    RamProvisioner.class,
                    BwProvisioner.class,
                    long.class,
                    List.class,
                    VmScheduler.class);
            return constructor.newInstance(
                    ramProvisioner,
                    bwProvisioner,
                    storageMb,
                    peList,
                    vmScheduler);
        }
    }
}