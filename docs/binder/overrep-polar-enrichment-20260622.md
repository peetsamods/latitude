# 1.4.1 #3 over-representation — polar pool enrichment (data-only) + savanna finding

`status: active` · `scope: worldgen` · `date: 2026-06-22` · staged jar `5cd255717e88cd2d`

## What changed (data-only, low risk)
Enriched the starved polar tags so the flat-polar-shelf pool (and weighted primary) stop collapsing:
- `lat_polar_primary` 1→6: added `snowy_plains`, `snowy_taiga`, `grove`, `terralith:snowy_shield`,
  `terralith:siberian_taiga` (kept `snowy_slopes`).
- `lat_polar_secondary` +4: `terralith:wintry_forest`, `wintry_lowlands`, `siberian_grove`,
  `biomesoplenty:snowy_coniferous_forest` (modded entries `required:false`).
- `lat_polar_accent` unchanged (`ice_spikes`).
No Java change. (`flatPolarShelfPool` was filtering the old pool down to just `ice_spikes`, which is
why the flat shelf was ice-heavy and the non-flat fallbacks carried `snowy_plains`.)

## Measured (headless atlas, seed 214214684415956679 R7500 step64, land-only within-band)
| biome | polar BEFORE | polar AFTER |
|---|---|---|
| snowy_plains | ~67% | **52.4%** |
| ice_spikes | ~27% | **5.4%** |
| terralith:snowy_shield | — | 13.0% |
| terralith:siberian_grove | — | 10.9% |
| terralith:wintry_lowlands / bop:snowy_coniferous_forest | — | ~4–5% each |

Clear improvement and much more variety; `ice_spikes` over-rep essentially solved. `snowy_plains` is
down but still above the ~40% target — the residual is the hardcoded non-flat polar fallbacks
(`pickPolarWithFrontShoulder`, ~L4731-4760) which list `snowy_plains`/`snowy_taiga`/`grove` and
interact with the shoulder-ramp (the ramp keys on the pick staying `snowy_slopes`). Diversifying those
safely needs the modded ids via a pool pick (not `pickFrom`, which throws on absent ids) AND care not
to break the shoulder logic — deferred (higher risk, diminishing returns).

## Bigger finding: savanna is now the dominant over-rep
Same atlas: subtropical `savanna` **52.6%**, tropical `savanna` **43.7%** (tropical humid family ~55%,
under the 60% target). The tropical savanna is inflated by the **2026-06-22 tropical-arid LAW fix**,
which demotes tropical badlands/desert → savanna (100%). Law still holds (tropical arid = 0%), but the
demote target should be diversified (savanna + sparse_jungle/jungle-leaning) so tropical savanna drops
and the humid floor rises. Subtropical savanna is the `enforceWarmProvinceFamily` WARM_MEDIUM coercion
(custom→savanna) — the deeper Java slice from `overrep-analysis-20260622.md`. **Recommended next #3 target.**

## Dead end (do not retry): tropical demote-target tweak
Attempted a "contained" humid-dominant-tropics fix by changing the law demote target
(`demoteEquatorial*` → mostly `sparse_jungle` instead of savanna in the deep tropics). Atlas showed it
**did not work** (sparse_jungle stayed 0.6%, savanna only 43.7%→38.4%) AND it **regressed the law**
(tropical desert leaked back to ~4%). Conclusion: tropical savanna is NOT produced by the
badlands/desert demote path — it comes from `enforceWarmProvinceFamily` (WARM_MEDIUM→savanna) and the
hardcoded `pickOpenTropicalFallback`/`pickAridRegionFallback`. **Reverted** to the working law-fix
(savanna demote; tropical arid 0.00%). The only real lever for tropical+subtropical savanna is the
enforcement-layer Java slice — there is no contained demote-only fix.

Art II: port the tag enrichment to the version chain. legacy-pin: new chunks only.
