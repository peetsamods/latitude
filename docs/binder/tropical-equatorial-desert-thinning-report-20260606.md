# Tropical deep-equator desert thinning — fix report (2026-06-06)

`status: active` · scope: `worldgen` · workflow: `latitude-regression-fix-workflow` (single-axis: desert)
Branch `feat/1.3.1-cohesive-horizons-26.1.2`, on top of the badlands gate (commit `91ffa425`).
Second axis of the equator overhaul (`20260606-tropical-drybiome-equator-discovery`); follows
`20260606-tropical-equatorial-badlands-gate`.

## Mandate
Earth's deep equator is rainforest; true hot desert there is essentially absent (Horn of Africa
~3–5°N is the lone exception). 0–5° still read ~12.4% desert. Tighten it toward "rare" — but
**do not eliminate** (Earth keeps a little) and **do not touch** the subtropical desert belt.

## Rejected lever (attempt 1): `ProvinceAuthority.classifyWarm` deep-equator wet-bias
Added a narrow `DEEP_EQUATOR_WET_BIAS = 0.06` (fade by 10°) to the warm moisture. Falsified: it
converted savanna→**jungle** (WARM_MEDIUM→WARM_WET) before touching desert — discovery 0–5°
desert 12.44→12.18 (−0.26) but jungle 56.56→**67.79** (+11.2), drifting toward the WARM_WET
monoculture the warm-band rebalance fought. Root reason: equatorial desert cells sit deep in
WARM_DRY (low moisture), not at the 0.38 WARM_DRY/MEDIUM margin, so a uniform moisture bias
crosses the 0.62 MEDIUM/WET boundary first. Reverted; the moisture classifier is the wrong lever.

## Accepted lever (attempt 2): `demoteEquatorialDesert` at the final-clamp chokepoint
Mirrors the badlands gate but **partial**. In `applyFinalSavannaClimateClamp` (both overloads),
right after `demoteEquatorialBadlands`: a coherent `ValueNoise2D` field (`DESERT_LAT_KEEP_SALT`,
scale `radius·0.28`) retains `DESERT_EQUATOR_KEEP_FRAC = 0.40` of WARM_DRY desert at the equator,
the keep-fraction ramping to 1.0 (keep all) by `DESERT_LAT_RAMP_HIGH_DEG = 12`; the demoted
complement → `minecraft:savanna`. Noise-warped boundary (Art VI). Targeting desert at the clamp
(not the classifier) leaves the equatorial jungle/savanna balance — and the monoculture guard —
untouched.

## Proof (small/R7500/step16; baseline = badlands-gate-only)
Discovery seed `2533348776566713405`, % of land:
| band | desert | savanna | jungle | badlands |
|---|---|---|---|---|
| 0–5° | 12.44 → **7.51** | 28.68 → 33.60 | 56.56 → 56.56 | 0.00 |
| 5–10° | 15.72 → 14.73 | 34.86 → 35.85 | = | 0.00 |
| 10–15°+ | unchanged (ramp ends 12°); 15°+ subtropical belt unchanged |

Control seed `1199119911991199`: 0–5° desert 7.27→0.00, jungle 64.47 unchanged; desert still
present at 5–10° (6.32%) and full at 10°+ — not globally eliminated (seed-natural variation).

Whole-world conservation (discovery, badlands-gate → +desert): desert 51690→49806 (−1884,
retains **96.4%**), savanna 81030→82914 (+1884), badlands 10086→10086 (+0), jungle 55353→55353
(+0). Inventory 77 both sides — none vanished/new (Art X clean). **Jungle +0 on both seeds = no
monoculture** (the decisive win over the rejected classifier lever).

Scale-invariance (2× radius R15000/step32, discovery): 0–5° desert 3.68% (vs 7.91% badlands-only),
jungle 58.15% stable, badlands 0.02%, belt 15°+ intact.

## Files
- `src/main/java/com/example/globe/world/LatitudeBiomes.java` — `DESERT_LAT_*` constants + `demoteEquatorialDesert`/`shouldDemoteEquatorialDesert` + 2 clamp call sites.
- Proof: `tmp/tropical-badlands-gate-ace6e326/PROOF_SUMMARY.md`.

## Open / notes
- `DESERT_EQUATOR_KEEP_FRAC = 0.40` lands 0–5° at ~7.5% (discovery) / 0% (control). If Julia wants
  the discovery seed nearer ~5%, lower the frac (~0.30) — but that pushes more seeds to 0%.
- Savepoint pushed + Notion logged on Julia approval (local commit/tag only for now).
