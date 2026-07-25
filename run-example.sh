#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: ./run-example.sh fully.qualified.MainClass [arguments...]"
  exit 1
fi

if [[ -z "${CLOUDSIM_JAR:-}" ]]; then
  echo "Please export CLOUDSIM_JAR=/path/to/cloudsim.jar"
  exit 1
fi

main_class="$1"
shift
java -cp "target/classes:$CLOUDSIM_JAR" "$main_class" "$@"
