# 1.4.1 (#3) — overrepresentation: locked ranking + root cause is the Java enforcement layer

`status: active` · `scope: worldgen` · `date: 2026-06-22` · `evidence id: 20260622-overrep-analysis`
Analysis only — NO code/data edited, NO atlas re-run. Artifacts: `tmp/1.4.1-prep-20260621/overrep/`
(`matrix-ranking.md`, `pool-availability.md`, `enrichment-proposal.md`).

## Locked offender ranking (30-cell matrix, 5 seeds × 6 sizes, land-only denom)
Size-**invariant** (growing the world does NOT fix these — they're structural):
- `minecraft:snowy_plains` — **66.7% of polar** land (min/mean/max 51.7/66.7/80.6), 30/30 over the 40% gate; 19% overall. Polar monoculture.
- `minecraft:savanna` — **47.2% of subtropical** (25/30 over 40%), plus a **21.8% tropical leak**; overall ~11.6% borderline.
- `minecraft:ice_spikes` — **27% of polar** (uniform), an outsized rare-variant second head (never >40%, no hard gate).
- (watch) desert/badlands subtropical block; tropical humid-family floor averages 54.7% (under the 60% target), dragged down by the savanna/desert leak.
- BY-DESIGN, must NOT suppress: jungle/bamboo_jungle/tropics (equator); taiga family (subpolar); shared forest mix (temperate).

## CRITICAL finding (adversarial verify, verdict: sound-with-corrections)
**The headline offenders are produced by hardcoded Java selection/enforcement, NOT by the starved tag pools — so the data-only tag enrichment is largely INERT on them.** This is why #3 has resisted fixing. Confirmed levers (re-verify exact line numbers before editing — file is ~9500 lines):
- `enforceWarmProvinceFamily` (~L8581): in WARM_MEDIUM coerces any non-savanna-family pick back to `minecraft:savanna`; in WARM_DRY coerces anything not vanilla desert/badlands back to vanilla. Custom ids are honored ONLY in WARM_WET. → proposed `terralith:*`/`biomesoplenty:lush_*` arid+savanna additions get **rewritten to vanilla**.
- `pickTropicalGradient` early-returns `pickAridRegionFallback` at ~L3854 for WARM_DRY (the dominant arid territory) → `lat_arid_*` tag pool is **not consulted** there.
- Tropical savanna/desert leak = hardcoded `pickOpenTropicalFallback` (~L7281) / `pickAridRegionFallback`, gated by ProvinceAuthority — **independent of the `lat_trans_arid_tropics_*` tags**, so trans-band dilution cannot raise the tropical humid floor.
- `snowy_plains` polar monoculture = hardcoded remap in `pickPolarWithFrontShoulder` (~L4881: `snowy_slopes`→{slopes,plains,taiga,grove}) + flat-polar-shelf injection — a remap artifact, not `lat_polar_primary` starvation (snowy_plains isn't even in that tag today).
- `frozen_peaks`/`jagged_peaks` are flat-shelf-banned (`isFlatPolarShelfBannedMountainPick`) → weak as accent dilution on bulk polar terrain.

What data-only CAN still do (verified): polar cold siblings (`terralith:snowy_shield`/`siberian_taiga`/`wintry_*`) DO feed `flatPolarShelfPool`/`allowedLandPool`, so they partially split the polar head; the `meadow`→warm-dry-accent swap fixes a climate smell. But it will NOT clear the savanna/desert/tropical gates.

## Corrected path forward (needs Julia: thresholds + approach)
1. **Java slice is the real fix** (the elusive root): (a) extend `isDesertFamily`/`isBadlandsFamily` + the WARM_MEDIUM/WARM_DRY branches of `enforceWarmProvinceFamily` to honor coherent custom arid/savanna ids (so Terralith/BoP arid biomes survive); (b) diversify the `pickPolarWithFrontShoulder` remap so `snowy_slopes` doesn't collapse to ~67% `snowy_plains`; (c) revisit the hardcoded `pickOpenTropicalFallback`/`pickAridRegionFallback` to cut the tropical arid leak. All Art X-guarded (never demote humid heads), proven per-province.
2. **Data-only enrichment** (the 8-file patch in `enrichment-proposal.md`) is still worth landing for the polar pool + coherence + future pack-users, but expect modest movement and pair it with #1 above.
3. **Proof** must diff per-province (WARM_DRY/WARM_MEDIUM/WARM_WET) headless before/after, not just within-band shares, or `enforceWarmProvinceFamily` coercion is invisible.

## Acceptance gate (propose to Julia; mirrors `tools/atlas/overrep_rank.py:33-35`)
max overall ≤12%, max within-band ≤40%, tropical humid family ≥60% — her sign-off required, plus the data-only-first vs Java-first sequencing decision.

## Governance
Art II: this worktree is the canonical 26.1.2 line — make the fix here first, then port (data-only = mechanical copy; Java = per-branch). legacy-pin: generation-time → new chunks only (changelog: pre-1.4.1 explored areas keep old distribution). Art VI: Java change must stay map-proof (no new floorDiv/cell-hash). Art X: humid-equator heads preserved; raise the floor only by shrinking the leak.
