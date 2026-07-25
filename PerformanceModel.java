package awsim;

/**
 * Maps a calibrated baseline Cloudlet length to a realized length in MI.
 * Changing the length lets CloudSim 4.0's native scheduler propagate
 * performance variability into completion times and cost accounting.
 */
public interface PerformanceModel {
    long realizedLengthMi(int cloudletId, long baselineLengthMi, Ec2InstanceProfile profile);
}
