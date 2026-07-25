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
  temporary="$(mktemp "target/results/.awsim-official.XXXXXX.tmp")"
  "$@" > "$temporary"
  if [[ ! -s "$temporary" ]]; then
    echo "FAIL ${destination##*/} produced an empty report"
    return 1
  fi
  mv "$temporary" "$destination"
}

write_result target/results/scenario-official-prices.csv \
  ./run-example.sh awsim.AwsCli \
  --config=examples/scenario-official-prices.yaml --csv
echo "PASS official EC2 price retrieval"

write_result target/results/scenario-official-multiservice.csv \
  ./run-example.sh awsim.AwsCli \
  --config=examples/scenario-official-multiservice.yaml --csv
echo "PASS official EC2/EBS/FSx/S3/transfer price retrieval"
python3 tools/summarize_results.py target/results
echo "Results: target/results/scenario-official-*.csv"
