package awsim;

public final class MicroserviceSpec {
    private final String name;
    private final String instanceType;
    private final int minReplicas;
    private final int maxReplicas;
    private final int initialReplicas;
    private final double requestMi;
    private final double targetUtilization;

    public MicroserviceSpec(
            String name,
            String instanceType,
            int minReplicas,
            int maxReplicas,
            int initialReplicas,
            double requestMi,
            double targetUtilization) {
        this.name = name;
        this.instanceType = instanceType;
        this.minReplicas = minReplicas;
        this.maxReplicas = maxReplicas;
        this.initialReplicas = initialReplicas;
        this.requestMi = requestMi;
        this.targetUtilization = targetUtilization;
    }

    public String getName() { return name; }
    public String getInstanceType() { return instanceType; }
    public int getMinReplicas() { return minReplicas; }
    public int getMaxReplicas() { return maxReplicas; }
    public int getInitialReplicas() { return initialReplicas; }
    public double getRequestMi() { return requestMi; }
    public double getTargetUtilization() { return targetUtilization; }

    public String encode() {
        return name + "|" + instanceType + "|" + minReplicas + "|" + maxReplicas
                + "|" + initialReplicas + "|" + requestMi + "|" + targetUtilization;
    }
}
