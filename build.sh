#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${CLOUDSIM_JAR:-}" ]]; then
  echo "Please export CLOUDSIM_JAR=/path/to/cloudsim.jar"
  exit 1
fi

if [[ ! -f "$CLOUDSIM_JAR" ]]; then
  echo "CloudSim JAR not found: $CLOUDSIM_JAR"
  exit 1
fi

if ! java sun.tools.jar.Main tf "$CLOUDSIM_JAR" | grep -q '^org/cloudbus/cloudsim/core/CloudSim.class$'; then
  echo "The supplied JAR is not a compatible CloudSim distribution: $CLOUDSIM_JAR"
  exit 1
fi

if command -v javac >/dev/null 2>&1; then
  compiler=(javac)
else
  compiler=(java com.sun.tools.javac.Main)
fi

mkdir -p target/classes
find awsim -maxdepth 1 -name "*.java" | sort > target/sources.list
"${compiler[@]}" -cp "$CLOUDSIM_JAR" -d target/classes @target/sources.list

echo "Build completed against CloudSim 4.0 API."
