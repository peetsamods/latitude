# Worldgen regression prevention — disciplines + checks (2026-06-25)

`status: active` · `scope: worldgen, process, validation`

Retro after the 2026-06-25 round of fixes (bonus chest, amplified plains, temperate badlands/frozen_river,
modded-arid leak). The regressions split into two kinds, and the prevention measures target each:

- **One true regression — bonus chest** — caused by a *blunt revert*: reverting the rejected book/Direction-B
  UI (`git checkout f26d5f58 -- LatitudeWorldLauncher.java`) also reverted the launcher's *correct* explicit
  `WorldOptions(seed, structures, bonusChest)` construction, silently dropping the flag.
- **Latent gaps — plains-on-steep, badlands/frozen_river/modded-arid in the wrong band** — never worked
  correctly; they hid because our validation had blind spots (see below).

## Why they hid — validation blind spots (now addressed)

1. **Map-proof was biome-only + synthetic-terrain.** The headless atlas samples biomes at a fixed Y with
   NULL terrain inputs, so terrain-correlated selection (plains-on-steep) is invisible to it. → addressed by
   the **terrain-aware atlas mode** (below).
2. **Tooling checked for MONOCULTURE (high %), not WRONG-BAND CONTAMINATION (any %).** `overrep_rank.py`
   flags a biome >40% of a band; it never asked "is this biome in a band it has no business being in?" The
   2.4% temperate badlands sat in the atlas data unflagged. → addressed by the **band-correctness check**.
3. **Live-only features have no automated check.** Bonus chest / structures toggle / UI plumbing are only
   verified in-game, so a silent break has nothing to catch it. → addressed by the **pre-stage smoke checklist**.

## The checks + disciplines

### 1. Band-correctness invariant check (NEW, highest leverage) — `tools/atlas/band_correctness_check.py`
Reads an atlas run and asserts per-band FORBIDDEN climate classes stay under a small tolerance — catching
wrong-band leaks at ANY share. Rules: arid (badlands/desert/mesa + modded variants) legal only in
SUBTROPICAL; frozen_land (ice_spikes/frozen_peaks) legal only subpolar+; frozen_river legal subpolar+ with a
small temperate-edge tolerance; tropical stays arid-free (the law). Exits non-zero on violation → wire into
the release gate. **On its first run it caught modded Terralith arid variants leaking into tropical+temperate
that the vanilla-only demotes missed** (fixed via `isAridFamily`, broadening the demote predicates to the
`lat_arid` tags). Run: `python3 tools/atlas/band_correctness_check.py <atlas-run-dir> [--strict]`.

### 2. Terrain-aware atlas mode (IMPLEMENTED, property-gated)
The atlas biome layer normally runs `pick()` with null terrain (synthetic), so terrain gates (the temperate
plains-on-steep gate) don't fire and can't be map-proven. **Opt-in via `-Dlatitude.atlasTerrainAware=true`**:
the exporter then feeds the real `RandomState` + height view (the `world`) and uses a new `ATLAS_TERRAIN`
caller context so `shouldSkipPreviewTerrain` returns false and `hasReliableSurface` is true → terrain gates
fire. Default OFF → normal atlas runs are byte-identical. Implemented as a system property (not a `--flag`)
to avoid 4-file plumbing; pass it to the headless run via env:
```
JAVA_TOOL_OPTIONS="-Dlatitude.atlasTerrainAware=true" python3 tools/atlas/atlas_runner.py generate \
    --step 48 --seed <seed> --size itty --no-viewer-open
```
Edits: `LatitudeBiomes.shouldSkipPreviewTerrain` (ATLAS_TERRAIN→false) + `BiomePreviewExporter` (export() and
HeightStepProcessor: feed real noiseConfig+world + ATLAS_TERRAIN context when the property is set).
**PERF CAVEAT:** real terrain is ~10 `getBaseHeight`/sample → a full itty run is ~20+ min (vs seconds
synthetic). For routine use, run a small/itty world, and a future **lat-window clip** (only sample the
temperate band, where the gate fires) would make it a sub-minute proof — recommended follow-up.

### 3. Targeted reverts, NOT blunt file reverts (discipline)
When killing a rejected feature, **diff what the revert will lose and re-apply the legitimate underlying
logic.** The bonus-chest fix lived in the launcher alongside the rejected UI; `git checkout <commit> -- file`
took both. Practice: before a file-level revert, `git diff <commit> -- <file>` and salvage non-presentational
fixes. The launcher's `WorldOptions` construction should have survived the UI revert.

### 4. Pre-stage smoke checklist for LIVE-ONLY behaviors (discipline)
Before staging a jar that touches world creation / live-only paths, run the live smoke set (or, better, a
headless assertion that the created world's `WorldOptions` carries the flags). Minimum manual set:
- Create world: **Bonus Chest ON** → chest at spawn.
- **Generate Structures OFF** → no villages/strongholds.
- Existing save opens unchanged (Classic byte-identical expectation).
See `docs/release/live-smoke-checklist.md`.

### 5. Symmetric-clamp discipline (worldgen principle)
**Any latitude/biome rule that clamps on one side should prompt "do we need the poleward partner?"** The
missing poleward badlands clamp (we had equatorward only) was exactly this asymmetry; same for frozen_river
(needed an equatorward clamp). When adding a `demoteEquatorial*` / band threshold, check the opposite boundary.

## Status
- #1 band-correctness check: BUILT + passing (after the `isAridFamily` fix). Now also carries a **floor guard**
  (`BAND_FLOORS`: subtropical arid >= 15%) so it catches the *opposite* failure too — a band going too sparse
  (the arid-belt "thin band" regression). Recommend adding to the release hard-gate alongside the existing
  policy scripts. See [[arid-belt-earthlike-20260625]] for the floor rationale.
- #3/#4/#5: documented disciplines (this doc + the smoke checklist).
- #2 terrain-aware atlas: **IMPLEMENTED + VALIDATED (2026-06-25).** Run `20260625-131432` (itty, step48,
  `-Dlatitude.atlasTerrainAware=true`) proved the temperate plains-on-steep gate fires headless: temperate
  **plains 19.7% → 8.3%** and the **hills/meadow/grove family 0% → 12.6%** vs the synthetic-terrain baseline
  (where the gate is inert). The previously live-only plains fix is now map-provable. Perf caveat (lat-window
  clip follow-up) still stands.
