package awsim;

import org.cloudbus.cloudsim.Cloudlet;
import awsim.AwsPriceCatalogMetadata;

import java.text.DecimalFormat;
import java.util.List;

public class AwsSimulationReport {
    private final String title;
    private final List<Cloudlet> finishedCloudlets;
    private final CostBreakdown costBreakdown;
    private final double traceMakespanHours;
    private final double adjustedMakespanHours;
    private final AwsPriceCatalogMetadata catalogMetadata;

    public AwsSimulationReport(String title, List<Cloudlet> finishedCloudlets, CostBreakdown costBreakdown, double makespanHours) {
        this(
                title,
                finishedCloudlets,
                costBreakdown,
                makespanHours,
                makespanHours,
                AwsPriceCatalogMetadata.unspecified());
    }

    public AwsSimulationReport(
            String title,
            List<Cloudlet> finishedCloudlets,
            CostBreakdown costBreakdown,
            double traceMakespanHours,
            double adjustedMakespanHours,
            AwsPriceCatalogMetadata catalogMetadata) {
        this.title = title;
        this.finishedCloudlets = finishedCloudlets;
        this.costBreakdown = costBreakdown;
        this.traceMakespanHours = traceMakespanHours;
        this.adjustedMakespanHours = adjustedMakespanHours;
        this.catalogMetadata = catalogMetadata;
    }

    public String getTitle() { return title; }
    public List<Cloudlet> getFinishedCloudlets() { return finishedCloudlets; }
    public CostBreakdown getCostBreakdown() { return costBreakdown; }
    public double getMakespanHours() { return adjustedMakespanHours; }
    public double getTraceMakespanHours() { return traceMakespanHours; }
    public double getAdjustedMakespanHours() { return adjustedMakespanHours; }
    public AwsPriceCatalogMetadata getCatalogMetadata() { return catalogMetadata; }

    public String toPrettyString() {
        DecimalFormat df = new DecimalFormat("0.0000");
        StringBuilder sb = new StringBuilder();
        sb.append("\n==============================\n");
        sb.append(title).append("\n");
        sb.append("==============================\n");
        sb.append("Finished cloudlets : ").append(finishedCloudlets.size()).append("\n");
        sb.append("Trace makespan (h) : ").append(df.format(traceMakespanHours)).append("\n");
        sb.append("Adjusted span (h)  : ").append(df.format(adjustedMakespanHours)).append("\n");
        sb.append("EC2 cost           : $").append(df.format(costBreakdown.getEc2Cost())).append("\n");
        sb.append("EBS cost           : $").append(df.format(costBreakdown.getEbsCost())).append("\n");
        sb.append("FSx cost           : $").append(df.format(costBreakdown.getFsxCost())).append("\n");
        sb.append("S3 storage cost    : $").append(df.format(costBreakdown.getS3StorageCost())).append("\n");
        sb.append("S3 request cost    : $").append(df.format(costBreakdown.getS3RequestCost())).append("\n");
        sb.append("Transfer-out cost  : $").append(df.format(costBreakdown.getTransferOutCost())).append("\n");
        if (costBreakdown.getInterruptionCount() > 0 || costBreakdown.getAvailabilityPenaltyHours() > 0.0) {
            sb.append("Interruptions      : ").append(costBreakdown.getInterruptionCount()).append("\n");
            sb.append("Penalty hours      : ").append(df.format(costBreakdown.getAvailabilityPenaltyHours())).append("\n");
        }
        sb.append("TOTAL cost         : $").append(df.format(costBreakdown.getTotalCost())).append("\n");
        sb.append("Price catalog      : ").append(catalogMetadata.getCatalogId()).append("\n");
        sb.append("Catalog date       : ").append(catalogMetadata.getRetrievedAt()).append("\n");
        if (catalogMetadata.isIllustrative()) {
            sb.append("Catalog warning    : illustrative prices; not a live AWS quotation\n");
        }
        return sb.toString();
    }

    public static String csvHeader() {
        return "title,finished_cloudlets,trace_makespan_hours,adjusted_makespan_hours,"
                + "ec2_usd,ebs_usd,fsx_usd,s3_storage_usd,s3_requests_usd,"
                + "transfer_out_usd,total_usd,interruptions,penalty_hours,catalog_id,catalog_date";
    }

    public String toCsvRow() {
        return csv(title) + ","
                + finishedCloudlets.size() + ","
                + number(traceMakespanHours) + ","
                + number(adjustedMakespanHours) + ","
                + number(costBreakdown.getEc2Cost()) + ","
                + number(costBreakdown.getEbsCost()) + ","
                + number(costBreakdown.getFsxCost()) + ","
                + number(costBreakdown.getS3StorageCost()) + ","
                + number(costBreakdown.getS3RequestCost()) + ","
                + number(costBreakdown.getTransferOutCost()) + ","
                + number(costBreakdown.getTotalCost()) + ","
                + costBreakdown.getInterruptionCount() + ","
                + number(costBreakdown.getAvailabilityPenaltyHours()) + ","
                + csv(catalogMetadata.getCatalogId()) + ","
                + csv(catalogMetadata.getRetrievedAt());
    }

    private static String number(double value) {
        return String.format(java.util.Locale.ROOT, "%.8f", value);
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
