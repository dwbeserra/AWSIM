package awsim;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Ec2ProfileCatalog {
    private final Map<String, Ec2InstanceProfile> profiles = new LinkedHashMap<>();

    public Ec2ProfileCatalog register(Ec2InstanceProfile profile) {
        profiles.put(profile.getInstanceType().toLowerCase(), profile);
        return this;
    }

    public Ec2InstanceProfile get(String instanceType) {
        if (instanceType == null) {
            return null;
        }
        return profiles.get(instanceType.toLowerCase());
    }

    public Ec2InstanceProfile require(String instanceType) {
        Ec2InstanceProfile profile = get(instanceType);
        if (profile == null) {
            throw new IllegalArgumentException("Unknown EC2 instance profile: " + instanceType);
        }
        return profile;
    }

    public Map<String, Ec2InstanceProfile> asMap() {
        return Collections.unmodifiableMap(profiles);
    }

    public static Ec2ProfileCatalog standard() {
        Ec2ProfileCatalog catalog = new Ec2ProfileCatalog();
        for (Ec2InstanceProfile profile : StandardEc2Profiles.all()) {
            catalog.register(profile);
        }
        return catalog;
    }
}
