#!/usr/bin/env python3
"""Consolidate AWSIM text reports and summarize the variability experiment."""

from __future__ import annotations

import csv
import os
import re
import statistics
import sys
from pathlib import Path


FIELDS = {
    "Finished cloudlets": ("finished_cloudlets", int),
    "Trace makespan (h)": ("trace_makespan_hours", float),
    "Adjusted span (h)": ("adjusted_makespan_hours", float),
    "EC2 cost": ("ec2_usd", float),
    "EBS cost": ("ebs_usd", float),
    "FSx cost": ("fsx_usd", float),
    "S3 storage cost": ("s3_storage_usd", float),
    "S3 request cost": ("s3_requests_usd", float),
    "Transfer-out cost": ("transfer_out_usd", float),
    "TOTAL cost": ("total_usd", float),
    "Interruptions": ("interruptions", int),
    "Penalty hours": ("penalty_hours", float),
    "Price catalog": ("catalog_id", str),
    "Catalog date": ("catalog_date", str),
}


def parse_value(value: str, converter):
    value = value.strip().lstrip("$")
    return converter(value)


def reports_from_log(path: Path) -> list[dict]:
    lines = path.read_text(encoding="utf-8").splitlines()
    reports: list[dict] = []
    i = 0
    while i + 2 < len(lines):
        if (
            lines[i] == "=" * 30
            and lines[i + 1].strip()
            and lines[i + 2] == "=" * 30
        ):
            row = {"source_file": path.name, "title": lines[i + 1].strip()}
            i += 3
            while i < len(lines) and lines[i].strip():
                if ":" in lines[i]:
                    label, value = re.split(r"\s*:\s*", lines[i], maxsplit=1)
                    if label in FIELDS:
                        output_name, converter = FIELDS[label]
                        row[output_name] = parse_value(value, converter)
                i += 1
            if "total_usd" in row:
                reports.append(row)
        i += 1
    return reports


def percentile(values: list[float], p: float) -> float:
    ordered = sorted(values)
    position = (len(ordered) - 1) * p
    lower = int(position)
    upper = min(lower + 1, len(ordered) - 1)
    weight = position - lower
    return ordered[lower] * (1.0 - weight) + ordered[upper] * weight


def summarize_variability(source: Path, destination: Path) -> None:
    with source.open(newline="", encoding="utf-8") as handle:
        rows = list(csv.DictReader(handle))
    metrics = ("trace_makespan_hours", "total_usd")
    temporary = destination.with_name(f".{destination.name}.{os.getpid()}.tmp")
    with temporary.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["metric", "n", "mean", "sample_sd", "min", "p95", "max"])
        for metric in metrics:
            values = [float(row[metric]) for row in rows]
            writer.writerow(
                [
                    metric,
                    len(values),
                    f"{statistics.mean(values):.8f}",
                    f"{statistics.stdev(values):.8f}",
                    f"{min(values):.8f}",
                    f"{percentile(values, 0.95):.8f}",
                    f"{max(values):.8f}",
                ]
            )
    temporary.replace(destination)


def combine_csv(sources: list[Path], destination: Path) -> None:
    rows: list[dict] = []
    fieldnames: list[str] | None = None
    for source in sources:
        if not source.is_file():
            return
        with source.open(newline="", encoding="utf-8") as handle:
            reader = csv.DictReader(handle)
            if fieldnames is None:
                fieldnames = list(reader.fieldnames or [])
            elif list(reader.fieldnames or []) != fieldnames:
                raise ValueError(f"Incompatible CSV schema in {source}")
            rows.extend(reader)
    if not fieldnames:
        return
    temporary = destination.with_name(f".{destination.name}.{os.getpid()}.tmp")
    with temporary.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
    temporary.replace(destination)


def main() -> None:
    results_dir = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("target/results")
    reports: list[dict] = []
    for log_path in sorted(results_dir.glob("*.log")):
        reports.extend(reports_from_log(log_path))

    columns = ["source_file", "title"] + [item[0] for item in FIELDS.values()]
    output = results_dir / "consolidated-results.csv"
    temporary = output.with_name(f".{output.name}.{os.getpid()}.tmp")
    with temporary.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(reports)
    temporary.replace(output)

    variability = results_dir / "AwsSimulation9PerformanceVariability.csv"
    if variability.is_file():
        summarize_variability(
            variability, results_dir / "performance-variability-summary.csv"
        )
    combine_csv(
        [
            results_dir / "scenario-microservices-autoscaling.csv",
            results_dir / "scenario-microservices-fixed-peak.csv",
        ],
        results_dir / "microservice-comparison.csv",
    )
    combine_csv(
        [
            results_dir / "scenario-official-prices.csv",
            results_dir / "scenario-official-multiservice.csv",
        ],
        results_dir / "official-price-smoke-results.csv",
    )

    print(f"Wrote {len(reports)} report rows to {output}")


if __name__ == "__main__":
    main()
