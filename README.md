# AWSIM

AWSIM is a CloudSim 4.0.0 extension for reproducible AWS-oriented cost studies.
Its scientific mode uses real `org.cloudbus.cloudsim` objects for finite
Cloudlet campaigns and adds regional EC2, EBS, FSx for Lustre, S3,
data-transfer, Spot-interruption, price-variation, and performance-variation
models. A separate request-driven mode represents linear microservice chains
and target-tracking autoscaling while reusing AWSIM's instance mappings, price
catalogs, units, and provenance.

## Single-folder CloudSim import

All Java sources are intentionally flattened into the single `awsim/` folder.
The folder contains the complete production code, executable examples, GUI,
CLI, and `AwsimSelfTest`; there is no Maven-style source tree to reconstruct.
To import AWSIM into an existing CloudSim 4.0.0 project, copy `awsim/` as one
package under that project's Java source root and keep the package name
`awsim`. The supplied scripts demonstrate the equivalent direct-JAR build.

## Scope

The CloudSim mode targets finite campaigns of Cloudlets and explicit VM-binding
policies. The microservice mode is a deterministic, interval-based queue and
replica model with service-specific request demand, replica bounds, target
utilization, startup delay, and scale-in/out cooldowns. It is not an ECS/EKS
control-plane emulator, a packet-level network simulator, or a provider-capacity
model. See `docs/MODELING_ASSUMPTIONS.md` before interpreting absolute runtime,
latency, or reliability results.

## CloudSim 4.0.0 build and test

Set `CLOUDSIM_JAR` to the official CloudSim 4.0.0 release JAR. A full JDK is
recommended; the build script also supports a runtime containing the
`jdk.compiler` module.

```bash
export CLOUDSIM_JAR=/absolute/path/to/cloudsim-4.0.jar
./build.sh
./run-tests.sh
./run-all-examples.sh
./run-official-price-smoke.sh
```

The all-example runner compiles production and test sources, runs every
scenario, validates YAML/JSON inputs, performs an independent accounting check,
checks the GUI model without requiring a desktop, and writes raw results under
`target/results`. The separate official-price smoke test downloads or reuses
current public AWS Price List Bulk API offer files and requires network access.
Consolidate batch results with:

```bash
python3 tools/summarize_results.py target/results
```

## CLI

```bash
./run-example.sh awsim.AwsCli --help
./run-example.sh awsim.AwsCli --list-profiles
./run-example.sh awsim.AwsCli --scenario=sim3
./run-example.sh awsim.AwsCli \
  --config=examples/scenario-basic.yaml --validate
./run-example.sh awsim.AwsCli \
  --config=examples/scenario-performance.yaml --csv
./run-example.sh awsim.AwsCli \
  --config=examples/scenario-basic.yaml \
  --price-catalog=/path/to/normalized-price-cache.properties --csv
```

The configuration loader accepts flat YAML or JSON and rejects unknown keys.
The validator checks regions, profiles, host/VM capacity, workload dimensions,
storage parameters, variability bounds, and binding policies before CloudSim
starts.

Microservice scenarios use the same CLI:

```bash
./run-example.sh awsim.AwsCli \
  --microservices=examples/scenario-microservices-autoscaling.yaml --validate
./run-example.sh awsim.AwsCli \
  --microservices=examples/scenario-microservices-autoscaling.yaml --csv
```

## GUI

On a graphical desktop:

```bash
./run-gui.sh
```

The dependency-free Swing interface has separate tabs for scientific batch and
microservice/autoscaling scenarios. It validates inputs, runs simulations in a
background worker, displays reports, and saves YAML. Headless installations
should use the CLI; `AwsimGui --self-check` validates the GUI's default models
without opening a window.

## Examples

| Example | Question exercised |
| --- | --- |
| `AwsSimulation1` | On-Demand EC2, EBS, S3, and egress baseline |
| `AwsSimulation2` | Explicit bindings and Spot recovery penalty |
| `AwsSimulation3` | Same workload in five AWS regions |
| `AwsSimulation4` | Larger campaign with shared FSx and S3 |
| `AwsSimulation5InstanceSensitivity` | Runtime/cost trade-off across three sizes |
| `AwsSimulation6SpotInterruptions` | Seeded interruptions with checkpoints |
| `AwsSimulation7VariablePricing` | Seeded price shocks |
| `AwsSimulation8SpotAndVariablePricing` | Combined price and interruption risk |
| `AwsSimulation9PerformanceVariability` | Thirty seeded performance replications |
| `AwsMicroserviceAutoscalingExample` | Request-driven service chain with target tracking |
| `scenario-microservices-fixed-peak.yaml` | Fixed peak capacity comparison |
| `scenario-official-prices.yaml` | Current On-Demand EC2 price smoke test |
| `scenario-official-multiservice.yaml` | Current EC2, EBS, FSx, S3, and egress prices |
| `AwsMobile*` | Hybrid-extension examples; not a validated network model |

## Units and provenance

Cloudlet length is MI, capacity is MIPS per PE, and all CloudSim timestamps are
seconds. AWSIM converts trace seconds to billing hours exactly once. Instance
profiles map vCPUs to PEs; their MIPS/PE values must be benchmark-calibrated for
absolute prediction.

Every report contains the catalog identifier, source date, currency, and a
warning when prices are illustrative. `ExampleAwsPriceCatalog` is reproducible
but not a live quotation. In `OFFICIAL_AUTO` mode, AWSIM streams current
regional CSV offer files from the public AWS Price List Bulk API, selects the
required On-Demand EC2/EBS/FSx/S3/transfer dimensions, records each official
publication version, and atomically writes a small normalized cache. A complete
cache is reused until its configurable TTL expires. `CACHE` mode loads a strict
versioned cache and is also the required route for Spot prices because the
public Bulk API does not publish current Spot prices. Missing dimensions fail
closed. Details are in `docs/PRICE_CATALOG.md`.

## Reproducibility artifacts

- `run-tests.sh`: deterministic regression/integration tests;
- `awsim/`: the single importable Java source package (73 source files);
- `run-all-examples.sh`: clean execution of all Java and config scenarios;
- `run-official-price-smoke.sh`: current official multi-service price retrieval;
- `run-gui.sh`: graphical batch and microservice front end;
- `tools/manual_validate.py`: independent recomputation of CSV accounting;
- `tools/summarize_results.py`: consolidated scenario and variability tables;
- `REVIEWER_TRACEABILITY.md`: review-comment-to-change matrix.

Official dependencies and pricing references:

- https://github.com/Cloudslab/cloudsim/releases/tag/cloudsim-4.0
- https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/using-the-aws-price-list-query-api.html
- https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/using-the-aws-price-list-bulk-api.html
