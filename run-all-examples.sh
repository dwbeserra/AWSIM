#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${CLOUDSIM_JAR:-}" ]]; then
  echo "Please export CLOUDSIM_JAR=/path/to/cloudsim-4.0.jar"
  exit 1
fi

./build.sh
mkdir -p target/results

write_result() {
  local destination="$1"
  shift
  local temporary
  temporary="$(mktemp "target/results/.awsim-result.XXXXXX.tmp")"
  "$@" > "$temporary"
  if [[ ! -s "$temporary" ]]; then
    echo "FAIL ${destination##*/} produced an empty report"
    return 1
  fi
  mv "$temporary" "$destination"
}

examples=(
  awsim.AwsSimulation1
  awsim.AwsSimulation2
  awsim.AwsSimulation3
  awsim.AwsSimulation4
  awsim.AwsSimulation5InstanceSensitivity
  awsim.AwsSimulation6SpotInterruptions
  awsim.AwsSimulation7VariablePricing
  awsim.AwsSimulation8SpotAndVariablePricing
  awsim.AwsMicroserviceAutoscalingExample
  awsim.AwsMobileScenarioExample
  awsim.AwsMobileCloudSimpleExample
  awsim.AwsMobileCloudComplexExample
)

for main_class in "${examples[@]}"; do
  short_name="${main_class##*.}"
  result_file="target/results/${short_name}.log"
  write_result "$result_file" ./run-example.sh "$main_class"
  echo "PASS $short_name"
done

write_result target/results/AwsSimulation9PerformanceVariability.csv \
  ./run-example.sh awsim.AwsSimulation9PerformanceVariability 30
echo "PASS AwsSimulation9PerformanceVariability"

write_result target/results/scenario-basic-validation.log \
  ./run-example.sh awsim.AwsCli \
  --config=examples/scenario-basic.yaml --validate
write_result target/results/scenario-spot.csv \
  ./run-example.sh awsim.AwsCli \
  --config=examples/scenario-spot.json --csv
write_result target/results/scenario-price-and-spot.csv \
  ./run-example.sh awsim.AwsCli \
  --config=examples/scenario-price-and-spot.json --csv
write_result target/results/scenario-validation.csv \
  ./run-example.sh awsim.AwsCli \
  --config=examples/scenario-validation.yaml --csv
write_result target/results/scenario-performance.csv \
  ./run-example.sh awsim.AwsCli \
  --config=examples/scenario-performance.yaml --csv
write_result target/results/scenario-microservices-validation.log \
  ./run-example.sh awsim.AwsCli \
  --microservices=examples/scenario-microservices-autoscaling.yaml --validate
write_result target/results/scenario-microservices-autoscaling.csv \
  ./run-example.sh awsim.AwsCli \
  --microservices=examples/scenario-microservices-autoscaling.yaml --csv
write_result target/results/scenario-microservices-fixed-peak.csv \
  ./run-example.sh awsim.AwsCli \
  --microservices=examples/scenario-microservices-fixed-peak.yaml --csv
write_result target/results/gui-self-check.log \
  ./run-example.sh awsim.AwsimGui --self-check
grep -q 'AWSIM GUI SELF-CHECK: PASS' target/results/gui-self-check.log
write_result target/results/manual-validation.txt \
  python3 tools/manual_validate.py target/results/scenario-validation.csv
python3 tools/summarize_results.py target/results

echo "All examples completed. Results: target/results"
