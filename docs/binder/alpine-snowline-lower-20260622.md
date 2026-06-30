# 1.4.1 (#1) — Lower alpine snow-cap onset in warm latitude bands

`status: active` · `scope: worldgen` · `date: 2026-06-22` · `evidence id: 20260622-alpine-snowline-lower`

## Symptom
Screenshot: `minecraft:wooded_badlands` at block `1887 / 195 / -3940` had snow only on the
very tippy-top of the peak. Peetsa: snow should start "a little further down the slopes,"
without re-opening the old snow/ice creep into warmer latitudes.

## Diagnosis (from recon `tmp/1.4.1-prep-20260621/snow/snowline-map.md`, verified sound)
- The peak cap is the **alpine surface cap (System B)**, `LatitudeBiomes.alpineSurfaceKind`
  + `AlpineSurfaceMixin` (live path; registered in `globe.mixins.json`; NOT the dead
  `terrain_spline` density-function graph). `wooded_badlands` is a warm, non-snowy biome —
  its peak snow comes only from this altitude cap, not the biome snowy ramp (System A).
- The onset lever is the per-band `snowMinY` switch, `LatitudeBiomes.java:660-666`.
- Both warm-snow-creep guards (`ChunkRegionWarmSnowTrapMixin:44`,
  `ProtoChunkSnowBlockGuardMixin:101`) early-return at `pos.getY() >= ALPINE_ROCK_Y` and
  strip warm-band snow only *below* the rock line. Above it, the only warm suppressor is the
  per-band `snowMinY` (tropical = `Integer.MAX_VALUE`).

## Change
`src/main/java/com/example/globe/world/LatitudeBiomes.java`, `snowMinY` switch — lowered the
warm-leaning band onsets, kept `ALPINE_ROCK_Y = 184` fixed, kept tropical disabled:

| Band | Old onset | New onset | Δ | Climate basis (Earth climatic snowline) |
|---|---|---|---|---|
| POLAR | 184 (`+0`) | 184 (`+0`) | 0 | snowline near sea level; snow on ~all high polar terrain |
| SUBPOLAR | 187 (`+3`) | 186 (`+2`) | −1 | low snowline (~1.5 km); near-full alpine cover |
| TEMPERATE | 194 (`+10`) | 190 (`+6`) | −4 | Alps-belt snowline ~2.8 km — snow well down the slope |
| SUBTROPICAL | 202 (`+18`) | 198 (`+14`) | −4 | dry belt holds Earth's *highest* snowline (~5–6 km) → only upper peaks |
| TROPICAL | none | none | — | equatorial glaciers real but vanishingly rare; deliberate warm-creep safety floor |

Gaps now 0/2/4/8 (geometric) — the snowline rises ever faster toward the dry subtropical
belt, which is the real-world shape. Order stays monotone warm→cold. `snowWarp = (n-0.5)*8`
noise left unchanged (organic, non-flat boundary).

## Why warm-latitude creep stays impossible
- `ALPINE_ROCK_Y` unchanged → both guards' early-out region (`y<184`), the exposed-rock line,
  and the tree/rock shelf are byte-identical. The edit only moves the cap **down within a
  band's existing altitude window**; no latitude that previously had no snow gains any.
- TROPICAL stays `Integer.MAX_VALUE` (no alpine snow at any altitude).
- The LATITUDE gate (`SNOWY_RAMP_START/FULL_DEG` 54/68; guard band predicates) is untouched.
- Effective snow floor is **Y184** regardless of `snowWarp` (the `blockY < ALPINE_ROCK_Y`
  early-return fires first), so the raw `[180..187]` warp window for POLAR never paints below 184.

## Proof (all green) — `tmp/1.4.1-prep-20260621/snow/proof/`
1. `python3 tools/check-biome-tuning-policy.py` → **PASS** (treeline 168/28 + temperate-mountain gate untouched).
2. `bash tools/check_tree_line_port.sh` → **PASS** (both guards still gate on `pos.getY() >= LatitudeBiomes.ALPINE_ROCK_Y`).
3. `./gradlew compileJava` (Java 25) → **BUILD SUCCESSFUL** (`compileJava.log`).
4. `javap -p -c` of the built class → tableswitch arms now `184/186/190/198` + `int 2147483647` (`javap-alpine.txt`) — change is live in compiled bytecode.
5. `java SnowOnsetProof.java` (unit-style logic proof, `snow-onset-proof.txt`) → **PASS**: warm bands dropped, monotone preserved, tropical disabled both before/after, no band's onset raised, rock-line base fixed.

## Residual / next
- **Live in-game eyeball (Peetsa)** is the remaining confirmation: a temperate peak >Y190 should
  show a lower cap (~Y186–193); a tropical peak should stay bare stone. The screenshot peak is
  covered whether it is TEMPERATE or SUBTROPICAL (both lowered); confirm its band if a finer tune is wanted.
- **Art II:** port the `snowMinY` offsets to canonical + the 1.21.1 / 1.20.1 / 1.21.11 chain.
- **legacy-worldgen-policy-pin:** alpine caps are written at chunk generation, so existing
  generated chunks keep their old caps; only newly generated terrain uses the lower onset. No save migration.
- Does not touch the `stony-treeline-desert-followup-20260621` locks (`TREE_LINE_Y=168`, `ALPINE_ROCK_Y=184` both unchanged).
