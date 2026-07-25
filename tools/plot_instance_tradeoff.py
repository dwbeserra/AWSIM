#!/usr/bin/env python3
"""Create the paper's vector instance-sensitivity figure from consolidated CSV."""

from __future__ import annotations

import csv
import sys
from pathlib import Path

import matplotlib.pyplot as plt


def main() -> None:
    source = Path(sys.argv[1])
    destination = Path(sys.argv[2])
    with source.open(newline="", encoding="utf-8") as handle:
        rows = [
            row
            for row in csv.DictReader(handle)
            if "Instance sensitivity" in row["title"]
        ]

    labels = [row["title"].rsplit(" - ", 1)[-1] for row in rows]
    makespan = [float(row["trace_makespan_hours"]) for row in rows]
    cost = [float(row["total_usd"]) for row in rows]

    plt.rcParams.update(
        {
            "font.family": "serif",
            "font.size": 8,
            "axes.labelsize": 8,
            "xtick.labelsize": 7,
            "ytick.labelsize": 7,
        }
    )
    fig, ax = plt.subplots(figsize=(3.45, 2.35))
    ax.scatter(makespan, cost, s=38, color="#1f5f8b", edgecolor="white", linewidth=0.7)
    offsets = [(4, 5), (4, -13), (-54, 5)]
    for x, y, label, offset in zip(makespan, cost, labels, offsets):
        ax.annotate(
            label,
            (x, y),
            xytext=offset,
            textcoords="offset points",
            fontsize=7,
        )
    ax.set_xlabel("Trace makespan (h)")
    ax.set_ylabel("Total cost (USD)")
    ax.grid(True, color="#d9d9d9", linewidth=0.5)
    ax.set_axisbelow(True)
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.margins(x=0.12, y=0.15)
    fig.tight_layout(pad=0.35)
    destination.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(destination, bbox_inches="tight")
    fig.savefig(destination.with_suffix(".png"), dpi=400, bbox_inches="tight")


if __name__ == "__main__":
    main()
