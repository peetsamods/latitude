# 1.4.1 #3 over-representation — WARM_MEDIUM enforcement softening (subtropical savanna)

`status: active` · `scope: worldgen` · `date: 2026-06-22` · staged jar `95288560b05faf68`

## Change
- **Java** (`enforceWarmProvinceFamily`, WARM_MEDIUM arm): now preserves custom biomes
  (`if (isCustomBiome(pick)) return pick;`, mirroring WARM_WET) instead of coercing every non-savanna
  pick to vanilla `savanna`. **WARM_DRY deliberately NOT changed** — the tropical-arid LAW relies on
  the downstream demote catching VANILLA badlands/desert; preserving a custom arid there would slip a
  desert-like biome past the law into the tropics.
- **Data**: `lat_trans_arid_tropics_1_primary` + `_2_primary` (the subtropical savanna pools) enriched
  with `biomesoplenty:lush_savanna` + `biomesoplenty:prairie` (`required:false`).

## Measured (atlas, seed 214214684415956679 R7500 step64, within-band land)
- **Subtropical `savanna` 52.6% → 42.9%**; belt now diverse: `bop:prairie` 10%, `bop:lush_savanna` 5%,
  `savanna_plateau` 2.8% (warm-grassland family spread instead of a single-biome monoculture).
- LAW intact: tropical arid **0.00%**. No new monoculture (largest new entry prairie 10%).
- **Tropical `savanna` ~43% unchanged** — it is the law-demote (dry-tropical WARM_DRY provinces →
  savanna), NOT the WARM_MEDIUM coercion, so this slice does not move it. That is climate-honest (dry
  tropical = savanna). Making the equator humid-DOMINANT would require lowering the tropical WARM_DRY
  province share (ProvinceAuthority wet-bias) — historically rejected for jungle-monoculture risk; left
  as a deferred, higher-risk lever.

## Status
Subtropical savanna over-rep materially improved + diversified (53→43, multi-biome). Residual: subtropical
savanna still ~43% (just above the 40% target — could add more warm-grassland customs, diminishing
returns); tropical savanna ~43% is the climate-honest dry-province law-demote. Build + gates green; staged.
Live eyeball recommended. Art II: port the tag + Java change to the version chain.
