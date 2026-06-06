# Latitude 1.4 "Cohesive Horizons" Tropical/Equator Rebalance — 2026-06-05

`status: active` · scope: `worldgen` · result: `partial` (map-validated, not yet savepointed) · evidence id: `20260605-tropical-rebalance`

## Status
Map-validated across 2 seeds and the full tiny/small/regular/large world-size matrix. **Working tree only — nothing committed/tagged/pushed** (per Julia: "validate now, savepoint later"; the Art. II/XII strict per-slice commit gate is relaxed). Branch `feat/1.3.1-cohesive-horizons-26.1.2`, atop `2571c841`. Compiles clean.

## Scope
Fixes the long-standing tropical worldgen complaints for the 1.4 release and one regression discovered mid-work. Operated under `latitude-regression-fix-workflow`; correctness measured by maps, not vibes.

## Goal
- Equator/tropical band reads as mostly-humid **but varied** (not desert-choked, not a jungle monoculture), realistic and immersive for both vanilla and custom-biome setups.
- Deserts live in the subtropics (Earth ~15-30°), with a believable desert+badlands mix.
- De-confetti: coherent biome regions, no marooned specks.
- Scale-invariant; Article V invariants intact.

## Problem (baseline, HEAD 2571c841)
- The warm-side province classifier `ProvinceAuthority.classifyWarm` derived moisture from **latitude-independent** openness/humidity noise, so `WARM_DRY` desert scattered uniformly across every warm latitude — the equator measured **40% arid / 34% desert**, only 31% jungle. Physically backwards (the equatorial ITCZ is Earth's wettest zone).
- Confetti from the 64-block tier-identity roll and a per-block `hash64` in `chooseBadlandsVariant` (an Article VI cell-hash violation).
- Codex's earlier WARM_DRY work (isolated worktree, never merged here) had over-corrected and thinned both desert and badlands — to be avoided.

## Changes (2 source files; documented as separate axes per Art. XI)
1. **`ProvinceAuthority.classifyWarm` — Earth-analog latitude wet-bias** (root cause). `moisture += TROPICAL_WET_BIAS * smoothstep(1 - latDeg/23.5)` for `latDeg < 23.5`; `TROPICAL_WET_BIAS = 0.20`. Zero at/above the tropical/subtropical boundary, so subtropical and cold-side provinces are untouched. `effectiveRadius == ACTIVE_RADIUS_BLOCKS`, so the latitude term matches band math.
2. **`enforceWarmProvinceFamily` WARM_WET — variety preservation** (both overloads; the Art. X anti-monoculture fix). The WARM_WET branch was collapsing every non-jungle pick — including admitted custom biomes (`biomesoplenty:tropics`) and tropical wetlands — into plain jungle. Now it preserves jungle-family **plus** `isCustomBiome` **plus** swamp/mangrove, converting only out-of-place vanilla biomes to the jungle core.
3. **Tier coherence** `TIER_COHERENCE_BLOCKS 64→160`, `FALLBACK_COHERENCE_BLOCKS 64→128` (de-confetti).
4. **Badlands province** widen (`badlandsProvinceAuthorityHitModern` primary gate `0.40→0.52`; `BADLANDS_OUTSIDE_PROVINCE_THRESHOLD 0.22→0.34`) — believable desert+badlands mix.
5. **`chooseBadlandsVariant`** (registry overload) — replaced a per-block `hash64` (Art. VI cell-hash → 1-px wooded-badlands confetti) with a continuous `ValueNoise2D` field (`BADLANDS_VARIANT_PATCH_SCALE_BLOCKS=384`). Coherent wooded/eroded sub-regions.

## Method / commands
`JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console=plain runBiomePreview --args='--seed <S> --radius <5000/7500/10000/15000> --step <16/32/48/64> --y 64 --emitBiomeIndex true --bundle'`, analyzed with `tools/atlas/band_balance_analyze.py`, `tools/atlas/longitudinal_variety.py`, and (confetti) `tools/atlas/distinct_render.py` + `tools/atlas/embedded_speck.py`. Provider stack BoP + Terralith + Promenade loaded throughout.

> **Confetti method note (Julia 2026-06-05):** the natural-color `biomes.png` is NOT valid for confetti judgment (jungle/bamboo/sparse all render the same green and hide intermixing). Confetti must be read from the exact `biome_ids.png` (distinct color per biome) at a fine step (step16+), via the embedded-speck metric.

## Metrics (final state, wet-bias 0.20 + WARM_WET variety)
- **Tropical desert overabundance:** equator (inner 0-12°) ARID `33% desert (baseline)` → `~8-11%`; tropical band jungle `31% → 57-67%` across sizes.
- **Art. X equator monoculture (regression caught and fixed):** an interim wet-bias 0.30 produced inner-equator `5 distinct biomes / 68% jungle` with `biomesoplenty:tropics` disappearing (0%). Final state restores **10 distinct biomes / 34% largest share / `biomesoplenty:tropics` ~13%** — jungle-dominant but varied (jungle, bamboo, sparse, savanna clearings, wetlands, custom tropical, rare desert).
- **Confetti (rigorous, step16, band-resolved, cross-family = objectionable):** seed1 `0.47%` of land, seed2 `0.45%`; uniform across bands (temperate/subpolar busiest ~0.5-0.77% from legitimate BoP/Terralith forest/taiga richness). Original-complaint pairs negligible: `jungle`-in-arid 4-16, `taiga`-in-plains/forest 10-19, `old_growth_pine_taiga` specks **0**. Dominant regions coherent (jungle largest single region ~7,400px; badlands ~6 components).
- **Believable arid belt (large R15000 subtropics):** desert 28% + badlands 20% + savanna 39%.

## Geography & proportions (scale invariance — Art. I/V)
Candidate E, seed1 — tropical jungle% / tropical ARID% / inner-equator jungle%:
- tiny R5000: `56.7 / 20.8 / 71.0`
- small R7500: `67.5 / 17.1 / 74.5`
- regular R10000: `66.6 / 18.4 / 71.3`
- large R15000: `61.0 / 21.2 / 75.8`

Subpolar/polar jungle `0%` at all sizes (Art. V invariant: no jungle in subpolar/polar). No drift, no compression, no monoculture at any size. Subtropical/temperate bands unchanged by the latitude bias.

## Watch-items / open risks
- **Pre-existing Art. VI violation (NOT introduced here):** `pickFrom(...)` (the `FALLBACK_COHERENCE_BLOCKS` path) still uses `floorDiv` + `hash64(cellX, cellZ)` cell-hashing. Flagged for its own single-axis slice.
- Badlands presence is seed-sensitive (thin on some seeds, abundant on others); both desert and badlands are always present.
- `itty` (R3750) size not separately validated; band proportions are degree-based so they should hold.
- The `TROPICAL_WET_BIAS` constant (0.20) is the single dial for equatorial lush-vs-arid balance.

## Verdict
PARTIAL — fully map-validated, pending Julia review and the savepoint sweep (commit + annotated `save/*` tags per axis + push).

## Evidence
- Registry row: `20260605-tropical-rebalance` in `docs/binder/evidence-registry.md`
- Payload: `/Users/joolmac/CascadeProjects/Latitude (Globe)/tmp/audits/20260605-tropical-rebalance/atlas-evidence.txt`
- Preserved atlas runs: `run/latdev/atlas/{_baseline_2571c841, _e1, _e2, _e2_s16, _e_small, _e_regular, _e_large}`; viewer-ready run `run-headless/latdev/atlas-runs/20260605-081057`
- Distinct-color renders: `<rundir>/biome_ids_distinct.png`

## Addendum — Canonical vs `ca81036f` atlas/provenance audit (2026-06-05, evidence `20260605-canonical-ca81036f-audit`, result `pass`)
Triggered by an atlas export (`20260605-172827`) that the Atlas desktop app generated from the **isolated** worktree `codex/atlas-world-map-p0-e19fc1cc` @ `ca81036f` (NOT canonical), which showed a severe lush-tropical-fantasy: tropical 92% jungle / 0% savanna / 5.5% arid, **subtropical 48% jungle** (dry belt inverted), jungle bleeding to ~40°N. Audited the canonical dirty tree at the same geometry (seed `2591890304012655616`, R7500, step16).

Result — **FIXED in canonical WIP** (`ca81036f` → canonical):
- Tropical (geometric): jungle 92.2%→**67.3%**, savanna 0.0%→**13.8%**, desert/dry 5.5%→**18.0%**.
- Subtropical **placement** band: jungle **48.0%→0.0%**, desert/dry 7.9%→**55.0%**, savanna →41.1% (the inversion is corrected; subtropics are now the Hadley dry+savanna belt).
- 35–40°N jungle-family pixels: **3,228 → 0**; jungle poleward extent ~40°N → **gone by 30°N**. Zero temperate placement-band jungle in BOTH builds (the 37°N jungle was always warped-subtropical, never a temperate-pool leak).
- Vanilla-only confirms the fix is in vanilla worldgen logic (canonical tropical jungle 64.8% / savanna 15.2% / desert 19.1%).
- NOT atlas/legend drift (placement-band color map verified identical: `#F5A623`=subtropical, `#3FAF5A`=temperate). NOT a band-warp scale artifact. NOT a raw proof gap.
- Residual (both builds, out of scope): `ice_spikes` ≈ 9% polar over-representation — separate polar-accent issue.
- Confound noted: canonical stack = BoP+Terralith+Promenade (79 biomes) vs `ca81036f` BoP-only (63); controlled via the vanilla-only pass.
- Payload: `tmp/audits/20260605-canonical-ca81036f-audit/audit.txt`. Canonical run: `run/latdev/atlas/seed_2591890304012655616/Run_2571c841/R7500/step16`.

## Savepoint (2026-06-05, evidence `20260605-warm-band-savepoint`)
The proven warm-band rebalance core is now savepointed: branch `feat/1.3.1-cohesive-horizons-26.1.2`, old HEAD `2571c841` → new HEAD **`8c73beab`**, annotated tag **`save/warm-band-rebalance`**, **pushed to origin**. Narrow 2-file bucket (`ProvinceAuthority.java` + `LatitudeBiomes.java` rebalance hunks); committed snapshot compile-verified in a clean worktree. Custom-biome source-policy scaffolding and the tags/docs/tooling buckets intentionally left dirty for separate savepoints. Warm-band balance now carries full Art. X provenance (branch + commit + save tag + push + atlas-audit PASS).
