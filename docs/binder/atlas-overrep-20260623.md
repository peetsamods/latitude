# Atlas overrep analysis — 2026-06-23 (post-worldgen-pass)

`status: evidence` · `scope: worldgen/overrep` · `date: 2026-06-23`

First atlas run after the 1.4.1 worldgen pass. Seed `2591890304012655616`, small world (radius 7500),
step 64, y=64. Jar SHA `c09ba971` (staged `Lat 1.4+26.1.2`). Run dir: `run-headless/latdev/atlas-runs/20260623-081837`.

## Per-band land composition (top biomes, land-only)

### TROPICAL (0°–23.5°)  land=9365
| % | Biome | Flag |
|---|-------|------|
| 36.4% | minecraft:jungle | warn |
| 28.6% | minecraft:savanna | |
| 21.7% | minecraft:bamboo_jungle | |
| 8.6% | biomesoplenty:tropics | |
| 1.0% | biomesoplenty:lush_savanna | |
| 0.8% | biomesoplenty:wasteland_steppe | |
| 0.8% | minecraft:sparse_jungle | |
| 0.8% | minecraft:swamp | |

**Tropical arid (desert + badlands): 0.0%** ✅ LAW HOLDS

### SUBTROPICAL (23.5°–35°)  land=4711
| % | Biome | Flag |
|---|-------|------|
| 30.2% | minecraft:savanna | warn |
| 17.5% | biomesoplenty:prairie | |
| 14.4% | minecraft:desert | |
| 5.7% | biomesoplenty:lush_savanna | |
| 3.9% | minecraft:badlands | |
| 3.6% | minecraft:jungle | |
| 2.8% | biomesoplenty:wasteland_steppe | |
| 2.5% | minecraft:plains | |
| 2.4% | biomesoplenty:shrubland | |
| 2.3% | biomesoplenty:tropics | |

Vanilla savanna: **30.2%** (down from 53% pre-1.4.1). Total savanna family
(savanna + prairie + lush_savanna + savanna_plateau) ≈ 53%, but vanilla share
significantly reduced and now diluted by BoP prairie (17.5%) — the new primary pick.

### TEMPERATE (35°–50°)  land=6082
| % | Biome | Flag |
|---|-------|------|
| 18.2% | minecraft:plains | |
| 15.0% | minecraft:forest | |
| 11.3% | minecraft:birch_forest | |
| 7.6% | biomesoplenty:seasonal_forest | |
| 6.5% | minecraft:dark_forest | |
| 5.4% | minecraft:taiga | |
| 4.4% | minecraft:meadow | |
| 3.5% | minecraft:flower_forest | |
| 2.3% | promenade:blush_sakura_grove | |

**No dominant biome above 20%. Excellent diversity. No flags.**

### SUBPOLAR (50°–66.5°)  land=5516
| % | Biome | Flag |
|---|-------|------|
| 32.3% | minecraft:taiga | warn |
| 8.0% | minecraft:snowy_plains | |
| 7.8% | minecraft:old_growth_spruce_taiga | |
| 5.6% | terralith:siberian_grove | |
| 5.6% | biomesoplenty:tundra | |
| 5.3% | biomesoplenty:snowy_coniferous_forest | |
| 4.0% | terralith:siberian_taiga | |
| 3.3% | minecraft:snowy_taiga | |
| 3.1% | biomesoplenty:coniferous_forest | |
| 3.0% | minecraft:grove | |
| 2.9% | biomesoplenty:snowy_maple_woods | |
| 2.8% | promenade:glacarian_taiga | |

Taiga 32.3% is borderline warn but climatically correct for subpolar — taiga IS the dominant Earth subpolar
biome. Good variety (11 biomes with >2% share). No change from design intent.

### POLAR (66.5°–90°)  land=9787
| % | Biome | Flag |
|---|-------|------|
| 43.2% | minecraft:snowy_plains | SUSPECT (>40%) |
| 11.6% | terralith:siberian_grove | |
| 9.0% | terralith:snowy_shield | |
| 7.5% | minecraft:ice_spikes | |
| 6.1% | minecraft:frozen_peaks | |
| 6.1% | minecraft:jagged_peaks | |
| 5.7% | minecraft:snowy_slopes | |
| 5.0% | biomesoplenty:snowy_coniferous_forest | |
| 3.0% | terralith:wintry_lowlands | |

snowy_plains: **43.2%** (down from 67% pre-1.4.1 enrichment). Threshold 40% not yet met but
significantly improved. ice_spikes: **7.5%** (down from 27%). Polar is now genuinely varied.

Residual blocker: snowy_plains still uses `pickPolarWithFrontShoulder` + the non-flat vanilla fallback
path. Needs further tag enrichment or a fallback rewrite to break below 40%.

## Sub-latitude tropical humidity check

| Latitude zone | Humid | Savanna | Arid | Other |
|---------------|-------|---------|------|-------|
| 0°–5° | 70.2% | 28.8% | 0.0% | 1.0% |
| 5°–10° | 78.6% | 20.8% | 0.0% | 0.6% |
| 10°–15° | 73.8% | 25.2% | 0.0% | 0.9% |
| 15°–20° | 63.6% | 35.4% | 0.3% | 0.8% |
| 20°–23.5° | 43.7% | 47.6% | 7.3%* | 1.4% |
| 23.5°–30° | 15.6% | 65.2% | 16.3% | 2.9% |
| 30°–35° | 0.0% | 41.4% | 31.1% | 27.6% |

*7.3% "arid" at 20–23.5° = BoP wasteland/steppe from trans_arid tags, NOT desert/badlands.
True desert + badlands = 0.0% throughout 0–23.5°.

**Equator humidity floor (>60% at 0-15°): PASS** ✅
**Tropical law (0% desert/badlands at 0-23.5°): PASS** ✅

## What improved vs pre-1.4.1
- Polar snowy_plains: 67% → 43.2% (threshold 40% — close, one more tag-enrichment pass should close it)
- Polar ice_spikes: 27% → 7.5%
- Subtropical vanilla savanna: 53% → 30.2% (warn; BoP prairie now 17.5% as primary variety)
- Tropical arid: was measured at ~5% pre-fix; now 0.0% ✅
- Temperate: consistently diverse (was also fine pre-1.4.1)

## Remaining residuals
1. **Polar snowy_plains 43.2%** — 3.2pp above 40% threshold. Need more polar-tagged biomes or a
   fallback surgery on `pickPolarWithFrontShoulder`. Good candidates: add more from
   lat_polar_secondary or lat_polar_accent tiers.
2. **Subtropical total savanna-family ~53%** — vanilla 30% is acceptable, but prairie (17.5%) is
   also a savanna-adjacent grass biome. If targeting vanilla savanna < 25%, need more non-savanna
   subtropical biomes (e.g. Terralith warm-subtropical biomes, if they exist in 26.1.2).
3. **Tropical savanna 28.6%** — climate-honest (WARM_DRY province demote is correct). Earmarked;
   fixing requires humid wet-bias (risky, deferred).

## Single-seed caveat
One seed, small world. Variance across seeds is expected (polar snowy_plains ranged 43-52% in
prior measurements). A second seed run is recommended to establish a tighter confidence band.
