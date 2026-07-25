package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import awsim.Ec2InstanceProfile;
import awsim.PerformanceModel;
import awsim.AwsCloudlet;

import java.util.ArrayList;
import java.util.List;

public final class AwsCloudletFactory {
    private AwsCloudletFactory() {
    }

    public static Cloudlet createCloudlet(int id, long length, int pes, long fileSizeMb, long outputSizeMb, int brokerId) {
        UtilizationModel utilization = new UtilizationModelFull();
        Cloudlet cloudlet = new AwsCloudlet(
                id,
                length,
                pes,
                fileSizeMb,
                outputSizeMb,
                utilization,
                utilization,
                utilization);
        cloudlet.setUserId(brokerId);
        return cloudlet;
    }

    public static List<Cloudlet> many(int count, long length, int pes, long fileSizeMb, long outputSizeMb, int brokerId) {
        List<Cloudlet> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(createCloudlet(i, length, pes, fileSizeMb, outputSizeMb, brokerId));
        }
        return list;
    }

    public static List<Cloudlet> many(
            int count,
            long baselineLengthMi,
            int pes,
            long fileSizeMb,
            long outputSizeMb,
            int brokerId,
            Ec2InstanceProfile profile,
            PerformanceModel performanceModel) {
        List<Cloudlet> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            long realizedLength = performanceModel.realizedLengthMi(
                    i,
                    baselineLengthMi,
                    profile);
            list.add(createCloudlet(
                    i,
                    realizedLength,
                    pes,
                    fileSizeMb,
                    outputSizeMb,
                    brokerId));
        }
        return list;
    }
}
