# AWSIM Validation Report

Date: 2026-07-25  
Base simulator: official CloudSim 4.0.0 release  
Release archive SHA-256:
`f75894faa7b1e067f659b794d7d255e7f5b7494409c8ed932a4ff618faf331fe`

## Commands

```bash
export CLOUDSIM_JAR=/absolute/path/to/cloudsim-4.0.jar
./run-tests.sh
./run-all-examples.sh
./run-official-price-smoke.sh
```

## Outcome

- all 73 Java sources are contained directly in the single importable
  `awsim/` package folder;
- all production and test sources compiled against the CloudSim 4.0.0 JAR;
- `AWSIM SELF-TEST: PASS`;
- Simulations 1--9 passed;
- the request-driven microservice/autoscaling example passed;
- autoscaling and fixed peak-capacity YAML scenarios passed;
- three auxiliary mobile/hybrid extension examples passed;
- YAML validation and YAML/JSON CSV execution passed;
- GUI default models and validation paths passed the headless self-check;
- every expected Cloudlet completed;
- no VM allocation failed;
- 17 report rows were consolidated;
- 30 seeded performance replications were summarized.

The live official-price smoke test also passed on 25 July 2026:

- On-Demand `c7i.2xlarge` in `us-east-1`: `0.357000 USD/h`;
- one-hour EC2-only scenario: `0.35700000 USD`;
- one-hour EC2/EBS/FSx/S3/transfer scenario: `4.51752434 USD`;
- the multi-service cache records official publication versions for
  `AmazonEC2`, `AmazonS3`, `AmazonFSx`, and `AWSDataTransfer`.

The GUI is implemented with standard Swing and compiles on the same JDK. The
execution environment is headless, so automated verification covers model
construction, validation, and the GUI self-check rather than a manual
pixel-level desktop inspection.

The official 4.0.0 JAR prints `CloudSim version 3.0` in its legacy startup log.
Dependency identity was verified from the official release archive and digest,
not from that stale internal string.

## Regression Coverage

- MI/MIPS analytical runtime and seconds-to-hours conversion;
- one-hour execution through real CloudSim classes;
- per-second EC2 billing with a 60-second minimum;
- deterministic price, performance, and interruption models;
- negative scenario/capacity validation;
- strict normalized-price cache and provenance metadata;
- automatic official-price normalization for controlled EC2, EBS, FSx, S3,
  and transfer fixtures;
- stale/complete cache decisions and aggregated service versions;
- live public AWS Bulk API retrieval for all represented On-Demand services;
- deterministic target-tracking autoscaling and fixed peak-capacity comparison;
- GUI default-model validation;
- trace and availability-adjusted makespan;
- non-billable hybrid/mobile nodes.

## Independent Accounting

`tools/manual_validate.py` independently recomputed the six cost components for
the validation scenario from the machine-readable CSV. The maximum absolute
component difference was `0.0000000042 USD`.

This validates accounting arithmetic and CloudSim integration. It does not
validate absolute application performance or an AWS invoice. Those require
workload-specific MIPS calibration, an official dated price cache, authorized
AWS execution, and a Cost and Usage Report.

## Result Files

- `target/results/consolidated-results.csv`;
- `target/results/AwsSimulation9PerformanceVariability.csv`;
- `target/results/performance-variability-summary.csv`;
- `target/results/manual-validation.txt`;
- `target/results/scenario-microservices-autoscaling.csv`;
- `target/results/scenario-microservices-fixed-peak.csv`;
- `target/results/microservice-comparison.csv`;
- `target/results/scenario-official-prices.csv`;
- `target/results/scenario-official-multiservice.csv`;
- `target/results/official-price-smoke-results.csv`;
- `target/results/gui-self-check.log`;
- individual simulation logs and config-driven CSV outputs.

## Manuscript Consistency Check

- the freshly executed consolidated CSV is the source for all batch values;
- the revised Experiment 7 total is `85.8099 USD`;
- the revised Experiment 8 total is `121.4866 USD`;
- the article contains 48 references, exactly twice the preceding 24-item
  bibliography, with 24 added journal/conference articles from 2021--2025;
- Azin Moradbeikie appears as the second author with the Università degli Studi
  di Trieste affiliation;
- the final PDF has nine pages and the references begin on page 8, leaving a
  seven-page body under the nine-page limit;
- all nine rendered pages were visually inspected; no clipping, overlap,
  margin overflow, broken cross-reference, or unreadable figure was found.
