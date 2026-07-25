package awsim;

public final class MobileProfiles {
    public static final MobileDeviceProfile SMARTPHONE_CLIENT = new MobileDeviceProfile(
            "smartphone-client", 800, 4, 6144, 500, 4096, 18.0, MobileDeviceRole.CLIENT);
    public static final MobileDeviceProfile TABLET_CLIENT = new MobileDeviceProfile(
            "tablet-client", 1000, 8, 8192, 700, 8192, 28.0, MobileDeviceRole.CLIENT);
    public static final MobileDeviceProfile MOBILE_EDGE_SERVER = new MobileDeviceProfile(
            "mobile-edge-server", 2500, 8, 16384, 2000, 16384, 60.0, MobileDeviceRole.MOBILE_SERVER);

    private MobileProfiles() {
    }
}
