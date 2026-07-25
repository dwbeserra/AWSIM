package awsim;

public class Ec2InstanceProfile {
    private final String instanceType;
    private final double mips;
    private final int pes;
    private final int ramMb;
    private final long bwMb;
    private final long imageSizeMb;
    private final String calibrationSource;
    private final String calibrationDate;
    private final boolean estimatedMips;

    public Ec2InstanceProfile(String instanceType, double mips, int pes, int ramMb, long bwMb, long imageSizeMb) {
        this(
                instanceType,
                mips,
                pes,
                ramMb,
                bwMb,
                imageSizeMb,
                "AWSIM illustrative MIPS estimate; benchmark calibration required",
                "2026-06-10",
                true);
    }

    public Ec2InstanceProfile(
            String instanceType,
            double mips,
            int pes,
            int ramMb,
            long bwMb,
            long imageSizeMb,
            String calibrationSource,
            String calibrationDate,
            boolean estimatedMips) {
        this.instanceType = instanceType;
        this.mips = mips;
        this.pes = pes;
        this.ramMb = ramMb;
        this.bwMb = bwMb;
        this.imageSizeMb = imageSizeMb;
        this.calibrationSource = calibrationSource;
        this.calibrationDate = calibrationDate;
        this.estimatedMips = estimatedMips;
    }

    public String getInstanceType() { return instanceType; }
    public double getMips() { return mips; }
    public int getPes() { return pes; }
    public int getRamMb() { return ramMb; }
    public long getBwMb() { return bwMb; }
    public long getImageSizeMb() { return imageSizeMb; }
    public String getCalibrationSource() { return calibrationSource; }
    public String getCalibrationDate() { return calibrationDate; }
    public boolean isEstimatedMips() { return estimatedMips; }
}
