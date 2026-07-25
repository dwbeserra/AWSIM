# Changes applied to the AWS CloudSim Extension

## Existing files modified (minimal-change policy)

1. `awsim/AwsEnvironment.java`
   - Added optional `AvailabilityModel` support.
   - Kept the old constructor for backward compatibility.

2. `awsim/StandardEc2Profiles.java`
   - Expanded the seeded instance-profile list.
   - Added `all()` to support catalog-based lookup.

3. `awsim/ExampleAwsPriceCatalog.java`
   - Added seeded prices for the new instance profiles.
   - Preserved the original pricing interface.

4. `awsim/CostBreakdown.java`
   - Added optional interruption count and availability penalty hours.
   - Kept the old constructor.

5. `awsim/AwsSimulationReport.java`
   - Added optional report lines for interruptions / penalty hours.
   - Existing report fields preserved.

6. `awsim/AwsCostCalculator.java`
   - Added availability-aware billing logic.
   - Added adjusted makespan calculation.
   - Preserved the old `computeMakespanHours(List<Cloudlet>)` method.

7. `awsim/AwsSimulationRunner.java`
   - Now uses the adjusted makespan overload.

8. `awsim/AwsSimulation2.java`
   - Reworked host capacity so the example is coherent and no longer depends on artificial VM allocation failure.
   - Added optional Spot availability penalty model in the environment.

## New files added

### Scenario configuration / CLI
- `awsim/AwsScenarioConfig.java`
- `awsim/SimpleScenarioConfigLoader.java`
- `awsim/AwsScenarioExecutor.java`
- `awsim/AwsCli.java`
- `examples/scenario-basic.yaml`
- `examples/scenario-spot.json`

### Price variability / externalized catalog hook
- `awsim/PriceDimension.java`
- `awsim/PriceShockModel.java`
- `awsim/NoPriceShockModel.java`
- `awsim/GaussianPriceShockModel.java`
- `awsim/VariableAwsPriceCatalog.java`
- `awsim/AwsPriceListApiCatalog.java`

### Availability modeling
- `awsim/AvailabilityModel.java`
- `awsim/AvailabilityOutcome.java`
- `awsim/NoAvailabilityModel.java`
- `awsim/SpotInterruptionAvailabilityModel.java`

### Profile catalog / mapping
- `awsim/Ec2ProfileCatalog.java`
- `awsim/AwsInstanceProfileMapper.java`

### Mobile-device representations
- `awsim/MobileDeviceRole.java`
- `awsim/MobileDeviceProfile.java`
- `awsim/MobileProfiles.java`
- `awsim/MobileNode.java`
- `awsim/MobileNodeFactory.java`
- `awsim/AwsMobileScenarioExample.java`

### Additional example
- `awsim/AwsSimulation5InstanceSensitivity.java`

## Validation performed here

- Every production and test source is compiled against the official CloudSim
  4.0.0 release JAR.
- The deterministic self-test, every example, configuration-driven execution,
  independent accounting checker, and live official-price smoke tests pass.
- See `VALIDATION_REPORT.md` for the exact commands, coverage, values, and
  remaining deployment-validation boundary.


## Additional examples added after the first revised package

New example classes added with minimal impact on the existing codebase:
- `awsim.AwsMobileCloudSimpleExample`
  - simple hybrid mobile + cloud scenario;
  - mobile tablet-like nodes execute lightweight tasks locally;
  - cloud VMs execute heavier tasks.
- `awsim.AwsMobileCloudComplexExample`
  - more complex hybrid scenario with smartphone-like clients, mobile-edge servers, cloud VMs, FSx and S3.
- `awsim.AwsSimulation6SpotInterruptions`
  - focuses on service interruptions / availability penalties for Spot instances.
- `awsim.AwsSimulation7VariablePricing`
  - focuses on seeded stochastic price variability without availability penalties.
- `awsim.AwsSimulation8SpotAndVariablePricing`
  - combines Spot availability penalties and stochastic price variability in a single scenario.

New example configuration files added:
- `examples/scenario-mobile-simple.yaml`
- `examples/scenario-price-and-spot.json`

### Code modification policy followed
No existing core class had to be changed to support these new examples.
The new functionality requested by the user was implemented only through:
- new example classes;
- new configuration example files;
- reuse of the extension points already introduced in the revised package (`MobileNode`, `SpotInterruptionAvailabilityModel`, `VariableAwsPriceCatalog`, `AwsScenarioExecutor`).


## Compatibility and bug-fix adjustments (June 2026)
- Replaced API calls from `submitGuestList(...)` to `submitVmList(...)` to match the CloudSim API used by the project.
- Replaced `cloudlet.getGuestId()` with `cloudlet.getVmId()` and `cloudlet.getExecFinishTime()` with `cloudlet.getFinishTime()` in `AwsCostCalculator`.
- Added surrogate hourly prices for mobile profiles (`smartphone-client`, `tablet-client`, `mobile-edge-server`) to avoid null pricing failures in hybrid scenarios.
- Added overloaded `replicate(startId, ...)` methods to `AwsVmFactory` and `MobileNodeFactory` and updated the mobile examples to allocate non-overlapping VM IDs across heterogeneous pools.
- Improved `ExampleAwsPriceCatalog.ec2Hourly(...)` to throw an explicit configuration error instead of a `NullPointerException` when a price is missing.

## July 2026 reviewer and usability revision

### GUI and external configuration

- Added `awsim/AwsimGui.java`, a standard Swing front end with scientific-batch,
  microservice/autoscaling, and results tabs.
- Added validation, background execution, human-readable reports, and YAML
  saving without requiring users to edit Java.
- Added `run-gui.sh` and a headless GUI model self-check.

### Automatic official prices

- Added `OFFICIAL_AUTO` to batch and microservice scenario configuration.
- Added `AwsOfficialPriceCache`, `AwsOfficialPriceNormalizer`, and
  `AwsPriceCsvReader`.
- Current regional public AWS Price List Bulk API CSV files are streamed,
  filtered to required On-Demand dimensions, versioned, and atomically cached.
- Supported automatic dimensions are EC2, gp3 EBS, persistent SSD FSx for
  Lustre, S3 Standard requests/storage, and first-tier internet egress.
- Stale, incomplete, corrupt, and wrong-region caches are refreshed.
- Spot is rejected in automatic mode because the Bulk API does not publish
  current Spot prices; strict dated caches remain supported.

### Microservices and autoscaling

- Added a deterministic request-driven linear service-chain model.
- Each service has a queue, instance profile, MI/request, minimum/maximum/
  initial replicas, and target utilization.
- The controller models startup delay, scale-in/out cooldowns, scaling events,
  queue limits, drops, throughput, approximate response/SLO metrics, and
  per-service EC2 billing.
- Added target-tracking and fixed peak-capacity YAML examples and regression
  comparisons.

### Validation additions

- Added controlled official-price fixtures for EC2, EBS, FSx, S3, and transfer.
- Added cache freshness/completeness and publication-version tests.
- Added deterministic autoscaling and autoscaling-versus-fixed-capacity tests.
- Added live official EC2 and multi-service retrieval smoke scenarios.
