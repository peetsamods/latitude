# 1.4.1 worldgen correctness — tropical arid LAW fix + alpine snow-floor re-tune

`status: active` · `scope: worldgen` · `date: 2026-06-22`
Raised by Peetsa's live cruise feedback (tropical badlands spawn; no snow on peaks near cloud level).

## A. Tropical arid LAW fix (badlands/desert may never appear in tropical) — MAP-PROVEN
- LAW: Earth geography forbids badlands/desert in the tropical band (0–23.5°).
- ROOT CAUSE: the equatorial-demotion ramps were tuned far below the tropical boundary —
  `BADLANDS_LAT_RAMP_LOW/HIGH = 10/18°` (badlands kept at 18–23.5°), and desert used
  `DESERT_EQUATOR_KEEP_FRAC=0.40` ramping to full by 12° (desert kept across most of the tropics).
  The demotion chokepoint itself (`demoteEquatorialBadlands/Desert` at LatitudeBiomes.java:9183/9186,
  the final warm clamp after `enforceWarmProvinceFamily`) was correct and on-path — only the ranges were wrong.
- FIX (`LatitudeBiomes.java`): moved both ramps to the tropical/subtropical boundary —
  `BADLANDS_LAT_RAMP_LOW=23.5`, `HIGH=32`; added `DESERT_LAT_RAMP_LOW=23.5`, `HIGH=32`, removed
  `DESERT_EQUATOR_KEEP_FRAC` and rewrote `shouldDemoteEquatorialDesert` to mirror badlands
  (demote-all below LOW via `smoothstep` clamping to 0). Tropical badlands/desert → savanna
  (Earth-true tropical dry; law-compliant). Noise-warped boundary preserved (Art VI).
- PROOF (headless atlas, my compiled fix, seed 214214684415956679 R7500 step64, land-only per band):
  `tmp/1.4.1-prep-20260621/lawfix-atlas/`

  | band | land | badlands/desert |
  |---|---|---|
  | tropical | 8922 | **0.00%** |
  | subtropical | 4324 | 22.66% (arid belt — correct) |
  | temperate | 6161 | 2.94% (warm-edge cold deserts, Earth-true) |
  | subpolar / polar | 6218 / 8958 | 0.00% |

  Overall top-20 no longer contains desert or badlands (baseline had desert ~6.8%). LAW satisfied.
- NOTE: demotion-to-savanna adds to tropical savanna (savanna is law-OK); the separate savanna
  over-representation is tracked in `overrep-analysis-20260622.md` (the #3 Java slice).

## B. Alpine snow-floor re-tune (snow now reaches the peaks that generate)
- SYMPTOM (live): peaks top ~Y176–185, clouds at Y190, but `ALPINE_ROCK_Y=184` floor + 190+ onsets
  meant snow essentially never appeared. The 184-pinned change in `alpine-snowline-lower-20260622.md`
  only moved onsets within the ≥184 zone, so it didn't help sub-184 peaks.
- FIX: `ALPINE_ROCK_Y 184→168` (per Peetsa's "lower floor to ~168"). The per-band `snowMinY` offsets
  are defined as `ALPINE_ROCK_Y + N`, so they auto-rebase to POLAR 168 / SUBPOLAR 170 / TEMPERATE 174 /
  SUBTROPICAL 182 / TROPICAL none. Both warm-snow-creep guards key off `ALPINE_ROCK_Y` (string
  unchanged), so warm-creep stays safe; tropical stays disabled.
- PROOF: `check-biome-tuning-policy.py` + `check_tree_line_port.sh` pass; compileJava + build green.
  Snow is altitude-gated (headless atlas is a fixed-y biome map, can't show it) → **live eyeball by
  Peetsa is the remaining confirmation** (temperate peak >Y176 should now cap; tropical peak stays bare).
  Supersedes the absolute-floor part of `alpine-snowline-lower-20260622.md` (onset *shape* unchanged).

## Staged
`Lat 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar` SHA `60d29224484ff67a8e57…` (snow+law+compass+GUI).
Art II: port both worldgen changes to canonical + the version chain. legacy-pin: new chunks only.
