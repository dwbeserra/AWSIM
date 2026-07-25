package awsim;

import awsim.CostBreakdown;

public final class AwsAccountingResult {
    private final CostBreakdown costBreakdown;
    private final double traceMakespanHours;
    private final double adjustedMakespanHours;

    public AwsAccountingResult(
            CostBreakdown costBreakdown,
            double traceMakespanHours,
            double adjustedMakespanHours) {
        this.costBreakdown = costBreakdown;
        this.traceMakespanHours = traceMakespanHours;
        this.adjustedMakespanHours = adjustedMakespanHours;
    }

    public CostBreakdown getCostBreakdown() {
        return costBreakdown;
    }

    public double getTraceMakespanHours() {
        return traceMakespanHours;
    }

    public double getAdjustedMakespanHours() {
        return adjustedMakespanHours;
    }
}
