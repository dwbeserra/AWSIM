package awsim;

public enum AwsRegion {
    US_EAST_1("us-east-1", "US East (N. Virginia)"),
    EU_WEST_3("eu-west-3", "Europe (Paris)"),
    AP_NORTHEAST_1("ap-northeast-1", "Asia Pacific (Tokyo)"),
    AP_SOUTHEAST_2("ap-southeast-2", "Asia Pacific (Sydney)"),
    SA_EAST_1("sa-east-1", "South America (São Paulo)");

    private final String code;
    private final String label;

    AwsRegion(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }
}
