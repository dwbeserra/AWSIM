# AWSIM Modeling Assumptions

## Supported workload class

AWSIM models finite campaigns of independent or explicitly bound CloudSim
Cloudlets in its scientific mode. It is intended for batch and scientific
what-if analysis.

The separate microservice mode represents a configured linear service chain in
fixed intervals. Each service has a queue, EC2 profile, MI/request, minimum,
maximum and initial replica counts, target utilization, startup delay, and
scale-in/out cooldowns. Completed requests flow to the next configured service.
The model reports throughput, drops, an approximate queue-derived mean response
time and SLO violation fraction, replica statistics, scaling events, and
per-service EC2 cost.

The microservice model reuses AWSIM's units, profile catalog, price provenance,
and per-second/minimum-60-second EC2 billing, but it is intentionally separate
from the finite-Cloudlet broker because CloudSim 4.0.0 has no native ECS/EKS
control plane. It does not model container placement, load balancer internals,
network packets, retries, nonlinear call graphs, AWS scaling metric lag, or
provider capacity events. Its latency and SLO outputs are comparative
approximations, not calibrated production predictions. The mobile examples
demonstrate extensibility; they are not a validated mobile-network model.

## Time and compute

- Cloudlet length is measured in million instructions (MI).
- VM and host processing rates are measured in million instructions per second
  (MIPS) per processing element (PE).
- CloudSim timestamps and Cloudlet execution times are seconds.
- AWSIM converts seconds to hours exactly once for reporting and billing.
- Under constant exclusive capacity, an analytical reference time is
  `length_Mi / allocated_Mips`. CloudSim scheduling determines the actual trace.
- The reported trace makespan is the last finish time minus the first start
  time across completed Cloudlets. The adjusted span adds modeled
  interruption/recovery time.

MIPS is not an AWS specification. AWSIM maps each vCPU to a CloudSim PE but
requires a benchmark-derived MIPS/PE value for absolute runtime prediction.
The built-in profiles are estimates for comparative experiments and record
that status. A study seeking absolute prediction must calibrate the profiles
using the target application, compiler, dataset, and instance generation.

## Billing boundary

Only `AwsVm` objects marked billable contribute EC2 and attached-EBS cost.
Hybrid/mobile client nodes are non-billable. For each billable VM, EC2 and EBS
runtime begins with its first Cloudlet and ends with its last Cloudlet.
Sub-minute positive runtime is billed as one minute; subsequent time is billed
per second. Monthly storage prices are prorated using 730 hours.

S3 storage, requests, and fixed internet egress are scenario inputs. Cloudlet
input/output MiB additionally contribute request counts and egress. FSx is
environment-level shared storage and is prorated over the adjusted scenario
span.

In microservice mode, an active replica is billed per second with a 60-second
minimum from activation until scale-in or simulation end. A pending replica is
not billed until its configured startup delay completes.

## Official price boundary

`OFFICIAL_AUTO` uses current regional public AWS Price List Bulk API CSV files
for On-Demand dimensions and stores a small versioned cache. The cache is
refreshed when stale, incomplete, or for a different region. The public Bulk API
does not contain current Spot prices, so Spot experiments must use a dated
explicit cache derived from the official Spot Price History API. AWSIM does not
model taxes, credits, negotiated discounts, Savings Plans, Reserved Instance
amortization, free tiers, or every tier of each AWS service.

## Stochastic extensions

Price, performance, and Spot interruption models are optional and seeded.
Identical inputs and seeds produce identical outputs. The performance model
multiplies Cloudlet length by a bounded Gaussian factor. The Spot model samples
interruptions from a Poisson process and adds restart overhead plus an expected
half-checkpoint interval of lost work per interruption. These are scenario
models, not predictions of future AWS prices or interruptions.

## Validation boundary

The self-test suite checks units, deterministic seeding, validation failures,
strict and automatic official-price normalization using controlled service
fixtures, cache freshness/completeness, version aggregation, deterministic
autoscaling, a fixed peak-capacity comparison, GUI default-model validity, and
a one-hour CloudSim accounting case. An independent Python implementation
recomputes every cost component from the CSV trace. This validates arithmetic
and integration; it does not replace a calibrated deployment comparison on
AWS.
