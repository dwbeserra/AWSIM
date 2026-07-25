package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;

/**
 * Cloudlet with explicit data-size units for AWS accounting.
 *
 * <p>CloudSim 4.0 does not model S3 or Internet egress. AWSIM therefore keeps
 * input/output sizes in MiB as accounting metadata instead of inferring their
 * unit from CloudSim's generic file-size fields.
 */
public class AwsCloudlet extends Cloudlet {
    private final long inputSizeMiB;
    private final long outputSizeMiB;

    public AwsCloudlet(
            int cloudletId,
            long cloudletLengthMi,
            int pesNumber,
            long inputSizeMiB,
            long outputSizeMiB,
            UtilizationModel cpu,
            UtilizationModel ram,
            UtilizationModel bw) {
        super(
                cloudletId,
                cloudletLengthMi,
                pesNumber,
                inputSizeMiB,
                outputSizeMiB,
                cpu,
                ram,
                bw);
        this.inputSizeMiB = inputSizeMiB;
        this.outputSizeMiB = outputSizeMiB;
    }

    public long getInputSizeMiB() {
        return inputSizeMiB;
    }

    public long getOutputSizeMiB() {
        return outputSizeMiB;
    }
}
