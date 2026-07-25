#!/usr/bin/env python3
"""Independent arithmetic check for the baseline AWSIM accounting scenario."""

import csv
import math
import sys


def main() -> int:
    path = sys.argv[1] if len(sys.argv) > 1 else "target/results/scenario-validation.csv"
    with open(path, newline="", encoding="utf-8") as handle:
        rows = list(csv.DictReader(handle))
    if len(rows) != 1:
        raise SystemExit(f"expected one CSV result row, found {len(rows)}")
    actual = rows[0]

    vm_runtime_hours = 50.0
    environment_hours = float(actual["trace_makespan_hours"])
    expected = {
        "ec2_usd": 6 * 0.3570 * vm_runtime_hours,
        "ebs_usd": 6 * (200 * 0.080) * (vm_runtime_hours / 730.0),
        "fsx_usd": 0.0,
        "s3_storage_usd": (500 * 0.0230) * (environment_hours / 730.0),
        "s3_requests_usd": ((10_000 + 60) / 1000.0) * 0.0050
        + ((10_000 + 60) / 1000.0) * 0.0004,
        "transfer_out_usd": (30 + 60 * 256 / 1024.0) * 0.090,
    }
    expected["total_usd"] = sum(expected.values())

    print("AWSIM independent accounting validation")
    print("component,manual_usd,awsim_usd,absolute_error_usd")
    max_error = 0.0
    for key, manual in expected.items():
        observed = float(actual[key])
        error = abs(manual - observed)
        max_error = max(max_error, error)
        print(f"{key},{manual:.8f},{observed:.8f},{error:.10f}")
    print(f"max_absolute_error_usd={max_error:.10f}")
    print("scope=accounting arithmetic only; not validation against a real AWS execution")
    return 0 if math.isclose(max_error, 0.0, abs_tol=1e-7) else 1


if __name__ == "__main__":
    raise SystemExit(main())
