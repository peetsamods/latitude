"""Controls for the radius-guard proof.

The defect it closes: a headless export on a run dir holding a Latitude world of another size
passed the run gate while producing a map 17% different from the clean-run-dir map. The proof must
say PASS only for a refusal or a sample-for-sample identical map, FAIL for silent drift, and
INVALID for anything that is not evidence.
"""

from __future__ import annotations

import contextlib
import io
import json
import random
import struct
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

TOOLS = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(TOOLS))

import verify_headless_radius_guard as guard  # noqa: E402

BOOT = [
    "[12:00:01] [main/INFO]: Loading Minecraft 1.21.1 with Fabric Loader 0.17.3",
    "[12:00:20] [Server thread/INFO]: Done (9.812s)! For help, type \"help\"",
]
EARLY_INIT_7500 = ("[12:00:19] [Server thread/INFO]: [Latitude] Early init: province authority seeded "
                   "before spawn-chunk generation (seed=1 radius=7500)")
REFUSED = ("[12:00:21] [Server thread/ERROR]: [latdev][headless] REFUSED: live overworld radius 7500 "
           "differs from recipe radius 10000 (the run dir holds a Latitude world of another size)")
STARTING_10000 = ("[12:00:21] [Server thread/INFO]: [latdev][headless] starting export seed=1 worldSeed=1 "
                  "radius=10000 steps=[16] y=64 ...")
STOPPING = "[12:00:31] [Server thread/INFO]: [latdev][headless] stopping server"

REFUSED_RUN = BOOT + [EARLY_INIT_7500, REFUSED, STOPPING]
COMPLETED_RUN = BOOT + [EARLY_INIT_7500, STARTING_10000, STOPPING]


def png_chunk(chunk_type: bytes, body: bytes) -> bytes:
    return (struct.pack(">I", len(body)) + chunk_type + body
            + struct.pack(">I", zlib.crc32(chunk_type + body) & 0xFFFFFFFF))


def encode_png(rows: list[list[tuple[int, int, int]]], filter_type: int = 0) -> bytes:
    """Minimal RGB PNG encoder applying one scanline filter to every row (0..4)."""
    height = len(rows)
    width = len(rows[0])
    channels = 3
    previous = bytes(width * channels)
    body = bytearray()
    for row in rows:
        line = bytes(value for pixel in row for value in pixel)
        filtered = bytearray()
        for i, value in enumerate(line):
            a = line[i - channels] if i >= channels else 0
            b = previous[i]
            c = previous[i - channels] if i >= channels else 0
            if filter_type == 0:
                predictor = 0
            elif filter_type == 1:
                predictor = a
            elif filter_type == 2:
                predictor = b
            elif filter_type == 3:
                predictor = (a + b) >> 1
            else:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                predictor = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
            filtered.append((value - predictor) & 0xFF)
        body.append(filter_type)
        body += filtered
        previous = line
    ihdr = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    return (guard.PNG_SIGNATURE + png_chunk(b"IHDR", ihdr) + png_chunk(b"IDAT", zlib.compress(bytes(body)))
            + png_chunk(b"IEND", b""))


def write_bundle(root: Path, grid: list[list[str]], palette_order: list[str] | None = None,
                 band_counts: str = "tropical=4 polar=5", filter_type: int = 0) -> Path:
    """A synthetic atlas step directory: biome_ids.png (R=G=B=index), biome_palette.json, biomes.txt."""
    step_dir = root / "seed_1" / "Run_test" / "R10000" / "step16"
    step_dir.mkdir(parents=True, exist_ok=True)
    ids = palette_order or sorted({biome for row in grid for biome in row})
    index_of = {biome: index for index, biome in enumerate(ids)}
    rows = [[(index_of[biome],) * 3 for biome in row] for row in grid]
    (step_dir / "biome_ids.png").write_bytes(encode_png(rows, filter_type))
    palette = {"biomes": [{"index": index_of[biome], "biome_id": biome, "displayColor": "#000000"} for biome in ids]}
    (step_dir / "biome_palette.json").write_text(json.dumps(palette), encoding="utf-8")
    (step_dir / "biomes.txt").write_text(
        f"seed=1\nradiusBlocks=10000\nstepBlocks=16\nauthorityBandCounts={band_counts}\n", encoding="utf-8")
    return step_dir


GRID = [
    ["minecraft:plains", "minecraft:forest", "minecraft:ocean"],
    ["globe:steppe", "minecraft:plains", "minecraft:ocean"],
    ["minecraft:desert", "globe:steppe", "minecraft:river"],
]


class PureDecoderTest(unittest.TestCase):
    def test_roundtrips_every_filter_type(self) -> None:
        rng = random.Random(20260908)
        rows = [[(rng.randrange(256), rng.randrange(256), rng.randrange(256)) for _ in range(7)] for _ in range(5)]
        expected = [pixel[0] for row in rows for pixel in row]
        for filter_type in range(5):
            with self.subTest(filter=filter_type):
                width, height, red = guard.decode_png_red_channel_pure(encode_png(rows, filter_type))
                self.assertEqual((width, height), (7, 5))
                self.assertEqual(red, expected)

    def test_agrees_with_pillow_when_pillow_is_installed(self) -> None:
        try:
            import PIL  # noqa: F401
        except ImportError:
            self.skipTest("Pillow not installed; the stdlib decoder is the only path here")
        rows = [[(x * 7 % 256, x, y) for x in range(9)] for y in range(4)]
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "ids.png"
            path.write_bytes(encode_png(rows, 4))
            self.assertEqual(guard.decode_png_red_channel(path), guard.decode_png_red_channel_pure(path.read_bytes()))

    def test_rejects_what_it_cannot_read(self) -> None:
        with self.assertRaises(guard.BundleError):
            guard.decode_png_red_channel_pure(b"not a png")
        rows = [[(1, 2, 3)]]
        interlaced = bytearray(encode_png(rows))
        interlaced[8 + 8 + 12] = 1  # IHDR interlace byte
        with self.assertRaises(guard.BundleError):
            guard.decode_png_red_channel_pure(bytes(interlaced))


class JudgeTest(unittest.TestCase):
    def setUp(self) -> None:
        self._directory = tempfile.TemporaryDirectory()
        self.root = Path(self._directory.name)
        self.control = self.root / "control"
        self.candidate = self.root / "candidate"
        write_bundle(self.control, GRID)

    def tearDown(self) -> None:
        self._directory.cleanup()

    def test_identical_map_passes_through_the_palette(self) -> None:
        # Same map, palette indices assigned in a different order and rows filtered differently:
        # the comparison is by biome ID behind the index, not by raw pixel value.
        write_bundle(self.candidate, GRID, palette_order=list(reversed(sorted({b for row in GRID for b in row}))),
                     filter_type=4)
        verdict, mode, detail = guard.judge(COMPLETED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("PASS", "identical"))
        self.assertIn("samples=9 differing=0 pixelIdentity=100.0000%", detail)
        self.assertIn("bandCountsEqual=True", detail)

    def test_one_differing_sample_is_silent_drift(self) -> None:
        drifted = [list(row) for row in GRID]
        drifted[1][0] = "minecraft:forest"
        write_bundle(self.candidate, drifted)
        verdict, mode, detail = guard.judge(COMPLETED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("FAIL", "silent-drift"))
        self.assertIn("differing=1", detail)
        self.assertIn("pixelIdentity=88.8889%", detail)

    def test_missing_biome_is_reported_by_id(self) -> None:
        # The observed defect shape: the same band counts, three biomes gone.
        drifted = [["minecraft:plains" if biome == "globe:steppe" else biome for biome in row] for row in GRID]
        write_bundle(self.candidate, drifted)
        verdict, mode, detail = guard.judge(COMPLETED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("FAIL", "silent-drift"))
        self.assertIn("onlyControl=['globe:steppe']", detail)
        self.assertIn("biomes=6/5", detail)

    def test_refusal_is_the_loud_failure(self) -> None:
        verdict, mode, detail = guard.judge(REFUSED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("PASS", "refused"))
        self.assertIn("liveRadius=7500 recipeRadius=10000", detail)
        self.assertIn("runGate=FAIL", detail)

    def test_refusal_that_still_exported_is_a_failure(self) -> None:
        write_bundle(self.candidate, GRID)
        verdict, mode, _ = guard.judge(REFUSED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("FAIL", "refused-but-exported"))

    def test_the_run_gate_alone_cannot_see_the_defect(self) -> None:
        # Negative control on the instrument: the pre-fix console (7500 seeded, 10000 exported,
        # completion marker) is a PASS to the run gate. Only the runner's refusal line, or the
        # sample-for-sample comparison, can catch the mismatch.
        self.assertEqual(guard.run_gate.classify(COMPLETED_RUN)[0], "PASS")

    def test_unrelated_failure_does_not_exercise_the_guard(self) -> None:
        crashed = BOOT + ["[12:00:25] [Server thread/ERROR]: [latdev][headless] export failed", STOPPING]
        verdict, mode, _ = guard.judge(crashed, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("INVALID", "run-failed"))

    def test_no_completion_evidence_is_invalid(self) -> None:
        self.assertEqual(guard.judge(BOOT, self.control, self.candidate)[:2], ("INVALID", "no-evidence"))

    def test_completed_run_without_candidate_is_invalid(self) -> None:
        self.assertEqual(guard.judge(COMPLETED_RUN, self.control, None)[:2], ("INVALID", "no-candidate"))

    def test_different_shape_is_not_the_same_recipe(self) -> None:
        write_bundle(self.candidate, [row[:2] for row in GRID[:2]])
        verdict, mode, detail = guard.judge(COMPLETED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("INVALID", "unreadable-bundle"))
        self.assertIn("shape differs", detail)

    def test_missing_palette_is_invalid(self) -> None:
        step_dir = write_bundle(self.candidate, GRID)
        (step_dir / "biome_palette.json").unlink()
        self.assertEqual(guard.judge(COMPLETED_RUN, self.control, self.candidate)[:2], ("INVALID", "unreadable-bundle"))

    def test_index_absent_from_palette_is_invalid(self) -> None:
        step_dir = write_bundle(self.candidate, GRID)
        palette = json.loads((step_dir / "biome_palette.json").read_text(encoding="utf-8"))
        palette["biomes"] = palette["biomes"][:-1]
        (step_dir / "biome_palette.json").write_text(json.dumps(palette), encoding="utf-8")
        verdict, mode, detail = guard.judge(COMPLETED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("INVALID", "unreadable-bundle"))
        self.assertIn("absent from biome_palette.json", detail)

    def test_missing_control_is_invalid(self) -> None:
        write_bundle(self.candidate, GRID)
        verdict, mode, _ = guard.judge(COMPLETED_RUN, self.root / "nowhere", self.candidate)
        self.assertEqual((verdict, mode), ("INVALID", "unreadable-bundle"))

    def test_two_bundles_under_one_root_is_ambiguous(self) -> None:
        write_bundle(self.candidate / "a", GRID)
        write_bundle(self.candidate / "b", GRID)
        verdict, mode, detail = guard.judge(COMPLETED_RUN, self.control, self.candidate)
        self.assertEqual((verdict, mode), ("INVALID", "unreadable-bundle"))
        self.assertIn("found 2", detail)


class MainExitCodeTest(unittest.TestCase):
    def run_tool(self, lines: list[str] | None, control: Path, candidate: Path | None) -> tuple[int, str]:
        with tempfile.TemporaryDirectory() as directory:
            log = Path(directory) / "capture.log"
            if lines is not None:
                log.write_text("\n".join(lines) + "\n", encoding="utf-8")
            argv = ["--log", str(log), "--control", str(control)]
            if candidate is not None:
                argv += ["--candidate", str(candidate)]
            out, err = io.StringIO(), io.StringIO()
            with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
                code = guard.main(argv)
            return code, out.getvalue() + err.getvalue()

    def test_exit_codes_follow_the_three_verdicts(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            write_bundle(root / "control", GRID)
            write_bundle(root / "same", GRID)
            drifted = [list(row) for row in GRID]
            drifted[0][0] = "minecraft:ocean"
            write_bundle(root / "drifted", drifted)

            code, output = self.run_tool(REFUSED_RUN, root / "control", root / "empty")
            self.assertEqual(code, 0)
            self.assertIn("RADIUS_GUARD_PASS mode=refused", output)

            code, output = self.run_tool(COMPLETED_RUN, root / "control", root / "same")
            self.assertEqual(code, 0)
            self.assertIn("RADIUS_GUARD_PASS mode=identical", output)

            code, output = self.run_tool(COMPLETED_RUN, root / "control", root / "drifted")
            self.assertEqual(code, 1)
            self.assertIn("RADIUS_GUARD_FAIL mode=silent-drift", output)

            code, output = self.run_tool(BOOT, root / "control", root / "same")
            self.assertEqual(code, 2)
            self.assertIn("RADIUS_GUARD_INVALID mode=no-evidence", output)

            code, output = self.run_tool(None, root / "control", root / "same")
            self.assertEqual(code, 2)
            self.assertIn("console capture", output)


if __name__ == "__main__":
    unittest.main()
