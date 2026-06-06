# Latitude 1.4 — Custom-biome Source-wrap + Promenade Admission Savepoint — 2026-06-06

`status: active` · scope: `worldgen` · result: `pass` (map-validated + savepointed) · evidence id: `20260606-custom-biome-sourcewrap-promenade-savepoint`

## Status
Committed in 3 narrow per-subsystem savepoints on branch `feat/1.3.1-cohesive-horizons-26.1.2`, atop `ffe8d5cb` (which is atop the warm-band `8c73beab`). Compiles clean. Tag `save/custom-biome-sourcewrap-promenade` at bucket tip `91432ac0`. Not yet pushed.

- `d133f63b` fix(worldgen): correct biome-source wrap descriptor + custom-biome-source robustness
- `0965687c` feat(worldgen): admit Promenade biomes into temperate/subpolar bands
- `91432ac0` chore(dev): source-path proof harness + authority-band report + preview size remap

This realizes the "custom-biome source-policy / biome tags / dev tooling" follow-up buckets named in `20260605-warm-band-savepoint`.

## Scope & changes
**(A) Source-wrap robustness** (`ChunkGeneratorBiomeSourceMixin`, `ChunkGeneratorPopulateBiomesMixin`, `LatitudeBiomeSource`, `LatitudeBiomes`):
- **Descriptor fix** — the `@Inject` constructor injectors used Yarn-form descriptors (`world/biome/source/BiomeSource`) that never bind under this project's official Mojang mappings (`com.mojang:minecraft`; every other mixin uses `world/level/biome/...`). With `require=0` they were silent no-ops. Corrected to `world/level/biome/BiomeSource`.
- **Getter-only wrap** — dropped `@Mutable`; no longer reassigns the shadowed `biomeSource` field, wraps via `globe$wrappedBiomeSource` only (logs `getterOnly=true`).
- **Deferred-wrap robustness** — `globe$resolvedPossibleBiomes()` defers wrapping (returns null → retry later) when `possibleBiomes()` throws on an unbound registry value (`IllegalStateException "unbound value"`) or a Biolith `BiomeCoordinator.getBiomeLookupOrThrow` `NoSuchElementException` during `WorldDimensions.checkStability/bake`.
- **Double-wrap unwrap** — populate path resolves `originalSupplier` to `LatitudeBiomeSource.original()` so terrain biomes are not read through an already-Latitude-overridden source.
- **Inert source-candidate-pool seam** — `LatitudeBiomes.expandSourceCandidatePool` (identity) + `rememberSourcePolicyBiomeRegistry` (no-op): documented hook for future source-side admission. **No behavior in 1.4.**

**(B) Promenade tag admission** (2 tag JSONs, `required:false` → absent-mod-safe):
- `lat_subpolar_primary` += `promenade:glacarian_taiga`
- `lat_temperate_secondary` += `promenade:blush_sakura_grove`, `promenade:cotton_sakura_grove`

**(C) Dev tooling** — `LatitudeDevCommand.emitSourcePathProofIfEnabled` (flag-gated source-vs-populate proof), `BiomePreviewExporter` `authorityBandCounts` report, `BiomePreviewHeadlessRunner` size-alias remap (small=7500/regular=10000/large=15000). No worldgen behavior change.

## Method
Headless atlas, full provider stack (BoP + Terralith + Promenade + TerraBlender + GlitchCore + lithostitched), `JAVA_HOME=$(/usr/libexec/java_home -v 25)`:
```
env -u JAVA_TOOL_OPTIONS ./gradlew runBiomePreview \
  --args="--latdevBiomePng --seed=2591890304012655616 --size=small --radius=7500 --step=16 --emitbiomeindex=true"
```
- **FULL** = current WIP (run `run-headless/latdev/atlas-runs/20260606-090645`).
- **BASELINE** = pristine `8c73beab` (7 worldgen+devcmd files reverted via saved copies, restored after).
- Compared `biomes.txt` summaries + `world_biome_inventory.json`. Wrap activation checked via `-Dlatitude.debugWorldgenPath=true`.

## Results
**(2a) source-wrap refactor is biome-map-neutral.** BASELINE vs FULL: `authorityBandCounts` **byte-identical** (tropical=229270, subtropical=116609, temperate=144689, subpolar=157622, polar=231654); every base-game biome count identical (ice_spikes 79881/9.08%, jungle 57029, desert 55349, savanna 46445, all oceans, bamboo_jungle, river, …). The structure/surface BiomeSource wrap **gate-rejects in headless preview** (`settings_unresolved`, `settingsReady=false`) under *both* descriptors — headless preview places no structures/surface rules, so the wrap correctly does not engage there; the change makes the wrap machinery correct (mojmap binding + Biolith deferral) without altering the terrain biome map. Real-server structure/surface alignment is the intended effect but is not headlessly observable.

**(2b) Promenade admission controlled (Art X clean).** The *only* deltas BASELINE→FULL:
- inventory count 76 → 79 (the 3 Promenade biomes)
- `snowy_plains` −729, `taiga` −1307 (cold subpolar → `glacarian_taiga`, placement_band=subpolar ✓)
- `plains` −641, `forest` −41 (temperate → sakura groves, placement_band=temperate ✓)

~2718 samples ≈ **0.31% combined world share**, each Promenade biome below the top-20 (<1.79%); within-band ≈ glacarian 1.3% of subpolar, sakura groves 0.47% of temperate. Coherent regions (discovery_hits 1849–2956), no monoculture, no base biome disappeared, no tropical/subtropical/ocean biome touched.

## Residual / follow-up
- Real-server structure/surface wrap activation (vs the headless `settings_unresolved` gate-reject) is not provable headlessly — would need a live client world load. Low risk: biome map proven neutral; worst case the wrap stays inert as before.
- Pre-existing `ice_spikes` ~9.08% polar over-representation persists (own bucket, unchanged here).
- Proof workspace: `tmp/custom-biome-sourcewrap-promenade-proof-ffe8d5cb/` (baseline gen + logs + saved file copies).
