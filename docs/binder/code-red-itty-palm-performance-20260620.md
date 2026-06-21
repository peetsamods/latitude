# Code Red Itty Palm/Chunk Performance Note - 2026-06-20

`status: historical-partial`

## Trigger
Julia reported that existing Itty worlds still loaded slowly, some decoration was missing, desert/palm scenes showed pickable but invisible palm leaves, and the world-entry screen appeared vanilla until a brief bespoke flash.

## Candidate Truth
- Root: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`
- Branch/HEAD: `feat/custom-biome-expansion-26.1.2` / `e5d092ca`
- Tag at HEAD: `save/clifftree-expansion-26.1.2`
- Staged profile jar after fix: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/mods/latitude-1.4.1-beta.2+26.1.2.jar`
- SHA-256: `d51eace9e517db5e53c8754e581e44b49ef68a6778b0f367ee60c8eefa5df073`
- Manifest build time: `2026-06-21T00:58:48Z`, commit `e5d092ca7f09a397afc413137f62ea409566e1e7`, dirty `true`

This proof jar has since been superseded for the active Modrinth profile by SHA `e09ea00313307ae84f8e32a0470db523874fe11d8314d133ddadfe9d85bf2458`. Treat the `d51eace9...` live screenshots/logs as mechanism proof for the code-red fixes, not as current-SHA release proof. Current-SHA live gates remain open in `docs/release/current-readiness-audit.md`.

## Fixes Applied
- Promenade palm tint compat: Promenade 5.5.0 registers palm leaves with color `0x007DB22E`, whose zero alpha makes the blocks pickable but invisible on Minecraft 26.1.2. Latitude now registers an opaque client tint `0xFF7DB22E` for `promenade:palm_leaves`, `promenade:snowy_palm_leaves`, `promenade:palm_hanging_leaves`, and `promenade:palm_leaf_pile` at `ClientLifecycleEvents.CLIENT_STARTED`.
- Arid biome admission: Latitude now admits the Terralith hot-dry canyon/oasis/spires/sandstone/ancient-sands biomes and `clifftree:desert_cliff` through `lat_arid_accent`, so canyon/desert terrain is less likely to be collapsed to plain `minecraft:desert`.
- SavannaGate log pressure: default `latitude.savannaGateLogEvery` changed from `2048` to `0`, keeping verbose SavannaGate info summaries opt-in.

An intermediate jar `12581004038628f5178c7047b6d5ee38818b2830204edfa10e36b9a228df85b0` crashed because the tint hook ran before `Minecraft.getBlockColors()` was initialized. That jar was backed up with a non-loadable `.bak` suffix and replaced by `d51eace9...`.

## Proof
- `./gradlew build` passed after the lifecycle-timed tint fix.
- Profile jar proof shows exactly one active loadable Latitude jar and bytecode registration through `ClientLifecycleEvents.CLIENT_STARTED`.
- Exact Java window proof succeeded: `owner=java title=Minecraft* 26.1.2`.
- Latest log for `d51eace9...` includes `[Latitude] Promenade palm tint compat applied to 4 block(s)`.
- Existing Itty world `Atlas Live Itty 2202220220260619002` loaded with the bespoke Latitude loading overlay visible. Log order: `bespoke overlay first render`, render gate waits, `first safe playable tick`, then `bespoke overlay cleared by normal client-ready path`.
- In-world screenshot after entry shows real terrain rendered, not void.
- A later shutdown watchdog crash report appeared after the `d51eace9...` jar was staged: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/crash-reports/crash-2026-06-20_21.23.54-client.txt`. The crash occurred during client shutdown/save (`ChunkMap.saveAllChunks` / `Minecraft.disconnectWithProgressScreen`), so it is tracked with the remaining performance/shutdown risk rather than the earlier palm-tint crash.

## Remaining Risk
Chunk/render loading is improved from the hard stall/void symptom but is not fully green. The live Itty run still logged repeated `Can't keep up` warnings around 2.0-2.2 seconds after entry, and a later shutdown watchdog occurred while saving/quitting the same profile. This historical run used `renderDistance:32` and `simulationDistance:12`; the active e09 proof profile has since been lowered to `renderDistance:16` and `simulationDistance:8`. The historical machine was under heavy pressure during proof, with about 10 GB compressed memory, low free memory, high system load, and another Minecraft Java client also running. The live thread dump sampled the server thread parked for the next tick and an active worker in vanilla `PlacedFeature.placeWithBiomeCheck` / `ChunkGenerator.applyBiomeDecoration`, not in Latitude `previewTerrain` recursion.

## Evidence
- Evidence folder: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/code-red-deep-dig-20260620-203501`
- Key screenshots:
  - `live-lat-launch-d51eace9.png`
  - `live-lat-menu-d51eace9.png`
  - `live-after-itty-doubleclick.png`
  - `live-itty-inworld-after-entry.png`
- Thread sample: `live-lat-thread-dump-33531.txt`
- Latest log: `/Users/joolmac/Library/Application Support/ModrinthApp/profiles/Lat 1.4+26.1.2/logs/latest.log`

## Next Gate
Use the current e09 follow-up note for current-SHA truth: `docs/binder/e09-fresh-small-live-smoke-shutdown-20260620.md`. The remaining gate is a clean single-client Itty/scenic/movement soak on SHA `e09ea003...`; if chunk stalls persist in that cleaner environment, take a fresh thread/JFR sample and identify whether `surfaceDecisionY`/`previewHeight`, placed feature decoration, or another path is dominant. Direct visual palm-canopy proof in a desert/palm scene is still recommended, but the alpha-root cause and runtime tint registration are proven.
