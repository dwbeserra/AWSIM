package awsim;

public class CostBreakdown {
    private final double ec2Cost;
    private final double ebsCost;
    private final double fsxCost;
    private final double s3StorageCost;
    private final double s3RequestCost;
    private final double transferOutCost;
    private final int interruptionCount;
    private final double availabilityPenaltyHours;

    public CostBreakdown(double ec2Cost, double ebsCost, double fsxCost, double s3StorageCost, double s3RequestCost, double transferOutCost) {
        this(ec2Cost, ebsCost, fsxCost, s3StorageCost, s3RequestCost, transferOutCost, 0, 0.0);
    }

    public CostBreakdown(double ec2Cost, double ebsCost, double fsxCost, double s3StorageCost, double s3RequestCost, double transferOutCost, int interruptionCount, double availabilityPenaltyHours) {
        this.ec2Cost = ec2Cost;
        this.ebsCost = ebsCost;
        this.fsxCost = fsxCost;
        this.s3StorageCost = s3StorageCost;
        this.s3RequestCost = s3RequestCost;
        this.transferOutCost = transferOutCost;
        this.interruptionCount = interruptionCount;
        this.availabilityPenaltyHours = availabilityPenaltyHours;
    }

    public double getEc2Cost() { return ec2Cost; }
    public double getEbsCost() { return ebsCost; }
    public double getFsxCost() { return fsxCost; }
    public double getS3StorageCost() { return s3StorageCost; }
    public double getS3RequestCost() { return s3RequestCost; }
    public double getTransferOutCost() { return transferOutCost; }
    public int getInterruptionCount() { return interruptionCount; }
    public double getAvailabilityPenaltyHours() { return availabilityPenaltyHours; }

    public double getTotalCost() {
        return ec2Cost + ebsCost + fsxCost + s3StorageCost + s3RequestCost + transferOutCost;
    }
}
