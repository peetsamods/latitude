# Tropical equatorial badlands latitude gate — fix report (2026-06-06)

`status: active` · scope: `worldgen` · workflow: `latitude-regression-fix-workflow` (single-axis: badlands)
Branch `feat/1.3.1-cohesive-horizons-26.1.2`, baseline HEAD `ace6e326`.
Completes the discovery row `20260606-tropical-drybiome-equator-discovery`.

## Mandate
Push badlands out of the deep equator (0–10°) — where Earth has none — and concentrate it in
the subtropical arid belt (~15°+). Keep savanna (Earth-true tropical clearings). Do NOT touch
the subtropical desert/badlands belt. Desert tightening is a SEPARATE deferred cycle (Art XI).

## 2nd-seed validation (governance prerequisite)
The discovery was one ocean-heavy seed (`2533348776566713405`, 8.8% badlands @0–5°). A 2nd
seed (`1199119911991199`) showed **0.00%** badlands at 0–5° — so deep-equator badlands is
**seed-dependent leakage**, not structural on every seed. This is precisely why a structural
latitude gate (0% on every seed) is the correct fix rather than relying on the moisture
wet-bias to probabilistically suppress it. The discovery's 8.76% reproduced at HEAD `ace6e326`.

## Root cause
Two blind attempts were byte-identical (Anti-Spiral stop), forcing instrument-by-inspection:
- Equatorial badlands does NOT flow through `badlandsProvinceAuthorityHit` (already false at
  the equator) nor through `pickAridRegionFallback`.
- It is produced by `enforceWarmProvinceFamily`'s WARM_DRY case, which **defaults any non-dry
  pick to `minecraft:badlands` first** (`LatitudeBiomes.java` ~8373) and keeps an incoming
  badlands base. That enforcement is the final province rewrite inside
  `applyFinalSavannaClimateClamp` — the last warm-band clamp, run once per pipeline
  (~2894 Registry / ~3514 Collection) for every `landBandIndex <= BAND_SUBTROPICAL` cell.
  The BAND_TROPICAL primary pick (`LAT_TROPICS_*` tags) is jungle-only; badlands is created
  here, downstream, which is why the upstream attempts had no effect.

## Fix (single chokepoint)
`demoteEquatorialBadlands(...)` applied to `out` inside both `applyFinalSavannaClimateClamp`
overloads, immediately after the `enforceWarmProvinceFamily` rewrite and before the savanna
tier pass (so a demoted savanna still gets Y-tiered). Rewrites a WARM_DRY badlands pick to
`minecraft:savanna` below a smoothstep ramp (`BADLANDS_LAT_RAMP_LOW_DEG=10`, `HIGH=18`); the
keep decision compares a coherent `ValueNoise2D` field (`BADLANDS_LAT_KEEP_SALT`, scale =
radius·0.28) against the gate, so the badlands↔savanna boundary is noise-warped, not a hard
horizontal line (Art VI block-space continuous). Demotes to **savanna** (an Earth-true tropical
dry-warm identity already sanctioned as a WARM_DRY fallback) rather than desert, so the
secondary equatorial desert share is not inflated. Both pipelines gated identically; +78 lines,
fully additive.

## Proof (small/R7500/step16 unless noted; per-biome via `perbiome_sublat.py`)

Discovery seed `2533348776566713405` — % of land, base→fix:
| band | badlands | desert | savanna | jungle |
|---|---|---|---|---|
| 0–5° | 8.76 → **0.00** | 12.44 → 12.44 | 19.91 → 28.68 | 56.56 → 56.56 |
| 5–10° | 0.05 → 0.00 | = | 34.81 → 34.86 | = |
| 10–15° | 1.97 → 0.42 | = | 30.82 → 32.37 | = |
| 15–20° | 13.79 → 12.83 | = | 25.39 → 26.35 | = |
| 20–24° | 12.21 → **12.21** | = | = | = |
| 24–30° | 4.20 → 4.20 | = | = | = |
| 30–35° | 4.53 → 4.53 | = | = | = |

Control seed `1199119911991199`: equator stays 0.00%; subtropical belt 20–35° unchanged
(34.07/28.55/19.32%); only the sub-18° transition thins (10–15: 4.50→0.55).

Whole-world conservation (discovery seed, biome_ids.png pixels):
badlands 13660→10086 (−3574, 73.8% retained), savanna 77456→81030 (+3574), desert 51690→51690
(+0), jungle 55353→55353 (+0). `world_biome_inventory.json` 77 biomes both sides — none
vanished, none new (Art X clean).

Scale-invariance (2× radius R15000/step32, discovery seed): same latitude structure — 0–5°
badlands 0.02% (~7px), 15–20° 6.51%, 20–24° 14.40%, 24–30° 32.38%, 30–35° 18.02%.

## Files
- `src/main/java/com/example/globe/world/LatitudeBiomes.java` — constants + `demoteEquatorialBadlands`/`shouldDemoteEquatorialBadlands` + 2 clamp call sites.
- Proof: `tmp/tropical-badlands-gate-ace6e326/PROOF_SUMMARY.md` + atlas dirs listed there.

## Deferred / open
- Desert secondary-tightening at 0–10° (12.4% @0–5°) — separate single-axis cycle (own savepoint).
- Savepoint (commit + `save/*` tag + push) and Notion log pending Julia approval; scope (ship in 1.4 vs 1.4.x) to confirm.
