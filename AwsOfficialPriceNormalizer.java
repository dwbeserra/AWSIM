package awsim;

import awsim.AwsRegion;
import awsim.PurchaseOption;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Service-specific selectors for the dimensions represented by AWSIM.
 */
final class AwsOfficialPriceNormalizer {
    private final AwsRegion region;
    private final AwsOfficialPriceCache.Requirements requirements;
    private final Properties values;
    private final List<String> publicationVersions = new ArrayList<>();

    AwsOfficialPriceNormalizer(
            AwsRegion region,
            AwsOfficialPriceCache.Requirements requirements,
            Properties values) {
        this.region = region;
        this.requirements = requirements;
        this.values = values;
    }

    void readEc2(Reader reader) throws IOException {
        AwsPriceCsvReader.OfferMetadata metadata = AwsPriceCsvReader.read(reader, row -> {
            if (!row.is("TermType", "OnDemand")) {
                return;
            }
            String instance = row.get("Instance Type");
            if (requirements.getInstanceTypes().contains(instance)
                    && row.is("Product Family", "Compute Instance")
                    && row.is("Operating System", "Linux")
                    && row.is("Tenancy", "Shared")
                    && row.is("Pre Installed S/W", "NA")
                    && row.is("CapacityStatus", "Used")
                    && row.is("operation", "RunInstances")
                    && row.is("Unit", "Hrs")
                    && row.price() > 0.0) {
                values.setProperty(
                        "ec2." + region.getCode() + "."
                                + PurchaseOption.ON_DEMAND.name() + "." + instance,
                        decimal(row.price()));
            }

            if (requirements.needsEbs() && row.is("Volume API Name", "gp3")) {
                String prefix = "ebs.gp3." + region.getCode() + ".";
                if (row.is("Unit", "GB-Mo") && row.get("usageType").contains("VolumeUsage.gp3")) {
                    values.setProperty(prefix + "storagePerGbMonth", decimal(row.price()));
                } else if (row.is("Unit", "IOPS-Mo")) {
                    values.setProperty(prefix + "extraIopsMonth", decimal(row.price()));
                } else if (row.is("Unit", "GiBps-mo")) {
                    values.setProperty(prefix + "extraThroughputMonth", decimal(row.price() / 1024.0));
                } else if (row.is("Unit", "MiBps-Mo")) {
                    values.setProperty(prefix + "extraThroughputMonth", decimal(row.price()));
                }
            }
        });
        remember("AmazonEC2", metadata);
    }

    void readS3(Reader reader) throws IOException {
        AwsPriceCsvReader.OfferMetadata metadata = AwsPriceCsvReader.read(reader, row -> {
            if (!row.is("TermType", "OnDemand")) {
                return;
            }
            String prefix = "s3.standard." + region.getCode() + ".";
            if (row.is("Product Family", "Storage")
                    && row.is("Storage Class", "General Purpose")
                    && row.is("Unit", "GB-Mo")
                    && "0".equals(row.get("StartingRange"))) {
                putFirst(prefix + "storagePerGbMonth", row.price());
            } else if (row.is("Unit", "Requests")
                    && row.is("Group", "S3-API-Tier1")) {
                values.setProperty(prefix + "putPerThousand", decimal(row.price() * 1000.0));
            } else if (row.is("Unit", "Requests")
                    && row.is("Group", "S3-API-Tier2")) {
                values.setProperty(prefix + "getPerThousand", decimal(row.price() * 1000.0));
            }
        });
        remember("AmazonS3", metadata);
    }

    void readFsx(Reader reader) throws IOException {
        AwsPriceCsvReader.OfferMetadata metadata = AwsPriceCsvReader.read(reader, row -> {
            if (!row.is("TermType", "OnDemand")
                    || !row.is("File system type", "Lustre")
                    || !row.is("operation", "CreateFileSystem:Lustre")) {
                return;
            }
            String prefix = "fsx.lustre." + region.getCode() + ".";
            String usage = row.get("usageType");
            if (row.is("Unit", "GB-Mo")
                    && row.is("Storage type", "SSD")
                    && row.is("Deployment option", "Persistent")
                    && row.is("Throughput capacity", "125")) {
                values.setProperty(prefix + "storagePerGbMonth", decimal(row.price()));
            } else if (row.is("Unit", "MiBps-Mo")
                    && row.is("Product Family", "Provisioned Throughput")) {
                values.setProperty(prefix + "throughputPerMbpsMonth", decimal(row.price()));
            } else if (row.is("Unit", "IOPS-Mo")
                    && row.is("Product Family", "Provisioned IOPS")) {
                values.setProperty(prefix + "metadataPerIopsMonth", decimal(row.price()));
            } else if (row.is("Unit", "GB-Mo") && usage.endsWith("BackupUsage")) {
                values.setProperty(prefix + "backupPerGbMonth", decimal(row.price()));
            }
        });
        remember("AmazonFSx", metadata);
    }

    void readTransfer(Reader reader) throws IOException {
        AwsPriceCsvReader.OfferMetadata metadata = AwsPriceCsvReader.read(reader, row -> {
            if (row.is("TermType", "OnDemand")
                    && row.is("Transfer Type", "AWS Outbound")
                    && row.is("From Region Code", region.getCode())
                    && row.is("To Location", "External")
                    && row.is("Unit", "GB")
                    && "DataTransfer-Out-Bytes".equals(row.get("usageType"))
                    && "0".equals(row.get("StartingRange"))
                    && row.price() > 0.0) {
                values.setProperty(
                        "transfer." + region.getCode() + ".internetPerGb",
                        decimal(row.price()));
            }
        });
        remember("AWSDataTransfer", metadata);
    }

    void requireComplete() {
        List<String> missing = new ArrayList<>();
        for (String instance : requirements.getInstanceTypes()) {
            require(missing, "ec2." + region.getCode() + "."
                    + requirements.getPurchaseOption().name() + "." + instance);
        }
        if (requirements.needsEbs()) {
            String prefix = "ebs.gp3." + region.getCode() + ".";
            require(missing, prefix + "storagePerGbMonth");
            require(missing, prefix + "extraIopsMonth");
            require(missing, prefix + "extraThroughputMonth");
        }
        if (requirements.needsS3()) {
            String prefix = "s3.standard." + region.getCode() + ".";
            require(missing, prefix + "storagePerGbMonth");
            require(missing, prefix + "putPerThousand");
            require(missing, prefix + "getPerThousand");
        }
        if (requirements.needsFsx()) {
            String prefix = "fsx.lustre." + region.getCode() + ".";
            require(missing, prefix + "storagePerGbMonth");
            require(missing, prefix + "throughputPerMbpsMonth");
            require(missing, prefix + "metadataPerIopsMonth");
            require(missing, prefix + "backupPerGbMonth");
        }
        if (requirements.needsTransfer()) {
            require(missing, "transfer." + region.getCode() + ".internetPerGb");
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Official AWS Price List did not contain required dimensions: " + missing);
        }
        values.setProperty("metadata.catalogId",
                "aws-bulk-" + String.join("+", publicationVersions));
    }

    private void remember(String service, AwsPriceCsvReader.OfferMetadata metadata) {
        String version = metadata.get("Version");
        String publicationDate = metadata.get("Publication Date");
        publicationVersions.add(service + "-" + (version == null ? "unknown" : version));
        if (publicationDate != null) {
            values.setProperty("metadata." + service + ".publicationDate", publicationDate);
        }
    }

    private void require(List<String> missing, String key) {
        if (!values.containsKey(key)) {
            missing.add(key);
        }
    }

    private void putFirst(String key, double value) {
        if (!values.containsKey(key)) {
            values.setProperty(key, decimal(value));
        }
    }

    private String decimal(double value) {
        return String.format(Locale.ROOT, "%.12f", value);
    }
}
