#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${CLOUDSIM_JAR:-}" ]]; then
  echo "Please export CLOUDSIM_JAR=/path/to/cloudsim-4.0.jar"
  exit 1
fi

./build.sh
java -cp "target/classes:$CLOUDSIM_JAR" awsim.AwsimGui "$@"
