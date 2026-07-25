package awsim;

public class MobileDeviceProfile {
    private final String name;
    private final double mips;
    private final int pes;
    private final int ramMb;
    private final long bwMb;
    private final long imageSizeMb;
    private final double batteryWh;
    private final MobileDeviceRole role;

    public MobileDeviceProfile(String name, double mips, int pes, int ramMb, long bwMb, long imageSizeMb, double batteryWh, MobileDeviceRole role) {
        this.name = name;
        this.mips = mips;
        this.pes = pes;
        this.ramMb = ramMb;
        this.bwMb = bwMb;
        this.imageSizeMb = imageSizeMb;
        this.batteryWh = batteryWh;
        this.role = role;
    }

    public String getName() { return name; }
    public double getMips() { return mips; }
    public int getPes() { return pes; }
    public int getRamMb() { return ramMb; }
    public long getBwMb() { return bwMb; }
    public long getImageSizeMb() { return imageSizeMb; }
    public double getBatteryWh() { return batteryWh; }
    public MobileDeviceRole getRole() { return role; }
}
