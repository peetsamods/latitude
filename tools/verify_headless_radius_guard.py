#!/usr/bin/env python3
"""Prove that a headless export on a run dir holding a Latitude world of another size cannot pass silently.

The defect this proof closes: BiomePreviewHeadlessRunner takes the live overworld and re-seeds
LatitudeBiomes with the recipe's seed and radius, but a Latitude world sitting in the run dir has
already seeded the process-global worldgen context at ITS radius (province authority, vanilla /
surface-water / cave coverage plans, the size-aware representation profile persisted at world
creation). The re-seed rebuilds only the province authority, so a run dir left holding a
radius-7500 world produced a radius-10000 map that differed from the clean-run-dir map in 17% of
its samples (47 biomes instead of 50, identical band counts) under HEADLESS_RUN_PASS, with nothing
in the console to show for it.

The runner now refuses that export with

    [latdev][headless] REFUSED: live overworld radius R differs from recipe radius R'

and tools/verify_headless_run.py fails the run on that line. This tool turns one such run into a
three-way verdict against the clean-run-dir control bundle:

  PASS mode=refused     the console carries the refusal and the run gate failed the run (loud)
  PASS mode=identical   the run completed and its biome_ids.png matches the control sample for
                        sample, compared through each bundle's own biome_palette.json
  FAIL mode=silent-drift          the run completed and the map differs from the control: the defect
  FAIL mode=refused-but-exported  the refusal was logged and a bundle was still written
  INVALID               the run left no usable evidence (start failure, crash, no completion marker,
                        missing or unreadable bundle, different recipe/shape); never a pass

Procedure (Loom dev environment, the same recipe on every run, e.g. seed 1, radius 10000, step 16):

  1. Control: on a clean run dir (no world, no level-type),
       ./gradlew runBiomePreview -Platdev.preview.runDir=<clean dir> --args="--seed=1 --radius=10000
           --step=16 --y=64 --layers=biomes --emitbiomeindex=true --bundle=true --out=<control root>"
  2. Prepare a run dir whose world is a smaller Latitude world: give its server.properties
       level-type=globe:globe_regular   (border radius 7500)
       level-name=<name>  level-seed=<seed>
     (prepareBiomePreviewRunDir accepts -Platdev.preview.levelType / levelName / levelSeed), then run
     the SAME recipe there with --out=<candidate root>. The task is expected to fail.
  3. python3 tools/verify_headless_radius_guard.py --log build/latitude-headless/runBiomePreview.log
         --control <control root> --candidate <candidate root>

A root may be the bundle's step directory itself or any ancestor holding exactly one biome_ids.png.

Measured alongside the guard (1.21.1 line): a run dir holding a Latitude world of the SAME radius
and seed as the recipe passes the guard and the run gate, and still drifts about 17% from the
clean-run-dir control with the same three route-managed biomes missing, because that world's
provider-ticket policy and coverage plans are active while a clean run dir exports through a
cleared worldgen context. This tool reports that as silent-drift too. The invariant the control
bundle encodes is "clean run dir"; whether the export should refuse every live Latitude world is
a maintainer ruling, and this guard closes the case that used to pass without a trace.
--candidate must be the --out root of the run whose console --log is; a bundle left there by an
earlier run would be read as this run's evidence.
"""

from __future__ import annotations

import argparse
import json
import re
import struct
import sys
import zlib
from dataclasses import dataclass
from pathlib import Path

TOOLS = Path(__file__).resolve().parent
sys.path.insert(0, str(TOOLS))

import verify_headless_run as run_gate  # noqa: E402

REFUSAL = re.compile(
    r"\[latdev\]\[[\w-]+\] REFUSED: live overworld radius (\d+) differs from recipe radius (\d+)")
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"
EXIT_CODES = {"PASS": 0, "FAIL": 1, "INVALID": 2}


class BundleError(Exception):
    """A bundle that cannot be read or compared is INVALID evidence, not a mismatch."""


@dataclass(frozen=True)
class Bundle:
    step_dir: Path
    width: int
    height: int
    indices: list[int]          # biome_ids.png red channel, row-major
    index_to_id: dict[int, str]  # biome_palette.json
    band_counts: str | None      # authorityBandCounts= from biomes.txt, when present

    def present_ids(self) -> set[str]:
        return {self.index_to_id[index] for index in set(self.indices)}


@dataclass(frozen=True)
class Comparison:
    samples: int
    differing: int
    only_control: list[str]
    only_candidate: list[str]
    band_counts_equal: bool | None

    @property
    def identical(self) -> bool:
        return self.differing == 0 and not self.only_control and not self.only_candidate

    @property
    def identity_percent(self) -> float:
        return 100.0 * (self.samples - self.differing) / self.samples if self.samples else 0.0


def decode_png_red_channel_pure(data: bytes) -> tuple[int, int, list[int]]:
    """(width, height, red channel row-major) of an 8-bit non-interlaced PNG, stdlib only.

    Handles greyscale, RGB and their alpha variants and all five scanline filters, which is what
    javax.imageio writes for the exporter's TYPE_INT_RGB images (adaptive filtering, so every
    filter type can appear).
    """
    if data[:8] != PNG_SIGNATURE:
        raise BundleError("not a PNG file")
    width = height = bit_depth = color_type = interlace = None
    idat = bytearray()
    pos = 8
    while pos + 8 <= len(data):
        length, chunk_type = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if chunk_type == b"IHDR":
            width, height, bit_depth, color_type, _compression, _filter, interlace = struct.unpack(
                ">IIBBBBB", body)
        elif chunk_type == b"IDAT":
            idat += body
        elif chunk_type == b"IEND":
            break
    if width is None or height is None:
        raise BundleError("PNG without an IHDR chunk")
    channels = {0: 1, 2: 3, 4: 2, 6: 4}.get(color_type)
    if bit_depth != 8 or channels is None or interlace != 0:
        raise BundleError(
            f"unsupported PNG (bitDepth={bit_depth} colorType={color_type} interlace={interlace}); "
            "the decoder handles 8-bit non-interlaced greyscale/RGB(A) only")
    stride = width * channels
    raw = zlib.decompress(bytes(idat))
    if len(raw) != (stride + 1) * height:
        raise BundleError(f"PNG image data is {len(raw)} bytes, expected {(stride + 1) * height}")
    previous = bytearray(stride)
    red: list[int] = []
    offset = 0
    for _ in range(height):
        filter_type = raw[offset]
        line = bytearray(raw[offset + 1:offset + 1 + stride])
        offset += stride + 1
        if filter_type == 1:  # Sub
            for i in range(channels, stride):
                line[i] = (line[i] + line[i - channels]) & 0xFF
        elif filter_type == 2:  # Up
            for i in range(stride):
                line[i] = (line[i] + previous[i]) & 0xFF
        elif filter_type == 3:  # Average
            for i in range(stride):
                left = line[i - channels] if i >= channels else 0
                line[i] = (line[i] + ((left + previous[i]) >> 1)) & 0xFF
        elif filter_type == 4:  # Paeth
            for i in range(stride):
                a = line[i - channels] if i >= channels else 0
                b = previous[i]
                c = previous[i - channels] if i >= channels else 0
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                predictor = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + predictor) & 0xFF
        elif filter_type != 0:
            raise BundleError(f"unknown PNG filter type {filter_type}")
        red.extend(line[0::channels])
        previous = line
    return width, height, red


def decode_png_red_channel(path: Path) -> tuple[int, int, list[int]]:
    """Pillow when it is installed (fast), the stdlib decoder above otherwise; same answer either way."""
    try:
        from PIL import Image  # type: ignore
    except ImportError:  # pragma: no cover - depends on the machine
        return decode_png_red_channel_pure(path.read_bytes())
    with Image.open(path) as image:
        rgb = image.convert("RGB")
        width, height = rgb.size
        return width, height, list(rgb.tobytes()[0::3])


def find_step_dir(root: Path) -> Path:
    if not root.exists():
        raise BundleError(f"{root}: does not exist")
    if (root / "biome_ids.png").is_file():
        return root
    hits = sorted(path.parent for path in root.rglob("biome_ids.png"))
    if len(hits) != 1:
        raise BundleError(f"{root}: expected exactly one biome_ids.png below it, found {len(hits)}")
    return hits[0]


def has_bundle(root: Path) -> bool:
    return root.exists() and any(root.rglob("biome_ids.png"))


def load_bundle(root: Path) -> Bundle:
    step_dir = find_step_dir(root)
    palette_path = step_dir / "biome_palette.json"
    if not palette_path.is_file():
        raise BundleError(f"{step_dir}: biome_palette.json is missing; biome_ids.png cannot be read without it")
    try:
        entries = json.loads(palette_path.read_text(encoding="utf-8"))["biomes"]
        index_to_id = {int(entry["index"]): str(entry["biome_id"]) for entry in entries}
    except (ValueError, KeyError, TypeError) as error:
        raise BundleError(f"{palette_path}: unreadable palette ({error})") from error
    width, height, indices = decode_png_red_channel(step_dir / "biome_ids.png")
    unknown = sorted(index for index in set(indices) if index not in index_to_id)
    if unknown:
        raise BundleError(
            f"{step_dir}: biome_ids.png carries indices absent from biome_palette.json: {unknown[:8]}")
    band_counts = None
    summary_path = step_dir / "biomes.txt"
    if summary_path.is_file():
        match = re.search(r"^authorityBandCounts=(.*)$", summary_path.read_text(encoding="utf-8", errors="replace"),
                          re.MULTILINE)
        band_counts = match.group(1).strip() if match else None
    return Bundle(step_dir, width, height, indices, index_to_id, band_counts)


def compare_bundles(control: Bundle, candidate: Bundle) -> Comparison:
    """Sample-for-sample comparison through the palettes: two bundles agree at a sample when the
    biome IDs behind their (possibly different) indices agree."""
    if (control.width, control.height) != (candidate.width, candidate.height):
        raise BundleError(
            f"shape differs: control {control.width}x{control.height} vs candidate "
            f"{candidate.width}x{candidate.height}; not the same recipe")
    control_index_by_id = {biome_id: index for index, biome_id in control.index_to_id.items()}
    remap = [-1] * 256
    for index, biome_id in candidate.index_to_id.items():
        if 0 <= index < 256:
            remap[index] = control_index_by_id.get(biome_id, -1)
    differing = sum(1 for c, k in zip(control.indices, candidate.indices) if remap[k] != c)
    control_ids = control.present_ids()
    candidate_ids = candidate.present_ids()
    band_counts_equal = None
    if control.band_counts is not None and candidate.band_counts is not None:
        band_counts_equal = control.band_counts == candidate.band_counts
    return Comparison(
        samples=len(control.indices),
        differing=differing,
        only_control=sorted(control_ids - candidate_ids),
        only_candidate=sorted(candidate_ids - control_ids),
        band_counts_equal=band_counts_equal,
    )


def judge(lines: list[str], control_root: Path, candidate_root: Path | None) -> tuple[str, str, str]:
    """('PASS' | 'FAIL' | 'INVALID', mode, detail) for one run on a prepared run dir."""
    verdict, detail = run_gate.classify(lines)
    if verdict == "INVALID":
        return "INVALID", "no-evidence", detail
    if verdict == "FAIL":
        refusal = REFUSAL.search(detail)
        if refusal is None or not detail.startswith("headless job refused"):
            return "INVALID", "run-failed", f"the run failed for another reason, so the guard was not exercised: {detail}"
        live_radius, recipe_radius = refusal.group(1), refusal.group(2)
        if candidate_root is not None and has_bundle(candidate_root):
            return ("FAIL", "refused-but-exported",
                    f"the runner refused (live radius {live_radius}, recipe radius {recipe_radius}) "
                    f"but a bundle was still written under {candidate_root}")
        return ("PASS", "refused",
                f"liveRadius={live_radius} recipeRadius={recipe_radius} runGate=FAIL: {detail}")
    if candidate_root is None:
        return "INVALID", "no-candidate", "the run completed, so its bundle must be compared: pass --candidate"
    try:
        control = load_bundle(control_root)
        candidate = load_bundle(candidate_root)
        comparison = compare_bundles(control, candidate)
    except BundleError as error:
        return "INVALID", "unreadable-bundle", str(error)
    summary = (f"samples={comparison.samples} differing={comparison.differing} "
               f"pixelIdentity={comparison.identity_percent:.4f}% "
               f"biomes={len(control.present_ids())}/{len(candidate.present_ids())} "
               f"onlyControl={comparison.only_control} onlyCandidate={comparison.only_candidate} "
               f"bandCountsEqual={comparison.band_counts_equal}")
    if comparison.identical:
        return "PASS", "identical", summary
    return "FAIL", "silent-drift", f"the run passed the run gate but its map differs from the control: {summary}"


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--log", required=True, type=Path,
                        help="captured console of the run on the prepared run dir")
    parser.add_argument("--control", required=True, type=Path,
                        help="clean-run-dir bundle: its step directory or a root holding exactly one biome_ids.png")
    parser.add_argument("--candidate", type=Path, default=None,
                        help="the prepared run's --out root (required when that run completed)")
    parser.add_argument("--task", default="runBiomePreview", help="task name for the report line")
    args = parser.parse_args(argv)

    if not args.log.is_file():
        print(f"RADIUS_GUARD_INVALID mode=no-evidence task={args.task}: console capture {args.log} is missing",
              file=sys.stderr)
        return EXIT_CODES["INVALID"]
    lines = args.log.read_text(encoding="utf-8", errors="replace").splitlines()
    verdict, mode, detail = judge(lines, args.control, args.candidate)
    print(f"RADIUS_GUARD_{verdict} mode={mode} task={args.task} lines={len(lines)}: {detail}",
          file=sys.stdout if verdict == "PASS" else sys.stderr)
    return EXIT_CODES[verdict]


if __name__ == "__main__":
    raise SystemExit(main())
