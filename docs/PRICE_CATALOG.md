# Price Catalog Provenance

AWSIM has three deliberately distinct price modes.

## Illustrative catalog

`ExampleAwsPriceCatalog` is self-contained and reproducible. Its metadata is
printed in every report. Values are examples for controlled what-if studies and
must not be presented as a live AWS quotation.

## Automatic official prices

`OFFICIAL_AUTO` streams the current regional CSV offer files from the public
AWS Price List Bulk API. No AWS account or credentials are required. AWSIM
filters only the dimensions required by the scenario and writes a compact
properties cache atomically. The cache records retrieval time, region,
currency, and the publication version/date of every service file used.

For scientific batch scenarios, automatic retrieval currently covers:

- Linux/shared On-Demand EC2 compute;
- gp3 EBS storage, extra IOPS, and extra throughput;
- persistent SSD FSx for Lustre storage, throughput, metadata IOPS, and backup;
- S3 Standard storage plus Tier 1 and Tier 2 request prices;
- first-tier internet data transfer out.

Microservice scenarios request only the On-Demand EC2 dimensions needed by
their service chain. A complete cache is reused until
`officialPriceCacheTtlHours` expires; a stale, corrupt, wrong-region, or
incomplete cache is refreshed. Missing official dimensions fail closed, and a
partially downloaded cache never replaces the previous file.

The Bulk API does not publish current Spot prices. `OFFICIAL_AUTO` therefore
rejects Spot scenarios rather than silently substituting On-Demand or
illustrative values. Use `CACHE` with a dated export obtained from the official
EC2 Spot Price History API.

## Explicit normalized cache

`CACHE` loads a small Java properties file derived from an AWS Price List Bulk,
Query, or Spot Price History API response. It is useful for frozen experiments,
offline execution, and Spot studies.

A normalized file must contain:

- `metadata.catalogId`, `metadata.source`, `metadata.retrievedAt`, and currency;
- an EC2 key for every region, purchase option, and instance type used;
- complete regional EBS, FSx, S3, and transfer dimensions used by the scenario.

See `examples/normalized-price-cache.properties.example` for the exact key
schema. The loader fails closed if a required value is missing. A fallback can
only be enabled explicitly through the Java API; the CLI does not silently
mix illustrative and official data.

Recommended manual normalization procedure:

1. download a versioned AWS offer/index document over HTTPS;
2. filter by exact region, operating system, tenancy, capacity status, usage
   type, storage class, and price dimension;
3. convert units to USD/hour, USD/GB-month, USD/request tier, or USD/GB;
4. write a versioned properties file and retain the original source URL/date;
5. review the file for zero, tiered, free-tier, and missing values;
6. run `--validate` and a CSV scenario before using the catalog in a study.

Automatic smoke test:

```bash
export CLOUDSIM_JAR=/absolute/path/to/cloudsim-4.0.jar
./run-official-price-smoke.sh
```

AWS Price List documentation:

- https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/price-changes.html
- https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/using-price-list-query-api.html
- https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/using-the-aws-price-list-bulk-api.html
