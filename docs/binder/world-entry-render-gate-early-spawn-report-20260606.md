# Latitude 1.4 — World-entry Render-gate + Early-spawn Savepoint — 2026-06-06

`status: active` · scope: `client` + `worldgen` · result: `pass` (live-probe verified + savepointed) · evidence id: `20260606-world-entry-render-gate-early-spawn`

## Status
Committed in 3 narrow savepoints on `feat/1.3.1-cohesive-horizons-26.1.2`, atop the bucket-#2 tip `91432ac0`. Compiles clean (`compileJava` BUILD SUCCESSFUL). Tag `save/world-entry-render-gate-early-spawn` @ `508c3231`. Not yet pushed.

- `3882da7c` fix(client): hold world-entry loading overlay until the world is render-ready  **(1a render-gate)**
- `aca09e47` feat(worldgen): set latitude-aware initial spawn before player-spawn pregen  **(1b early-spawn)**
- `508c3231` chore(dev): autonomous create-world probe harness for world-entry verification  **(1d dev harness)**

The dirty "world-entry/first-load client" bucket decomposed into FOUR distinct changes; this savepoint lands three. **1c** (GlobeWarningOverlay entry-title hemisphere-center fix) is **deferred to its own bucket** — still WIP.

## Changes
**1a Render-gate** (`LevelLoadingScreenLatitudeOverlayMixin`): the bespoke Latitude loading overlay used to clear as soon as the player passed its first tick or the loading screen closed — which on heavy modded worldgen could drop the player into an unrendered/void frame. Now the clear is gated on genuine playability: player settled (≥20 ticks), spawn-chunk 3×3 ring loaded, render warmup elapsed (≥2.5s), and render sections compiled & visible (player/feet section visible, or render queue drained with sections>0), with a 15s max-hold fail-safe.

**1b Early-spawn** (`GlobeMod.trySetInitialLatitudeSpawn` + new `MinecraftServerInitialSpawnMixin` + `GlobePending.peek` + `globe.mixins.json`): resolve and set the world's initial spawn to a latitude zone BEFORE the vanilla player-spawn pregen runs, clamping away from the E/W edge warning band. Falls back to vanilla initial spawn on error.

**1d Dev probe harness** (`GlobeModClient`, +901): dev-only (`isDevelopmentEnvironment`-gated) auto-create-world probe — opens create-world, auto-confirms, waits for world entry + captures spawn diagnostics, scans/force-loads chunks, verifies expected biomes. Controlled by `-Dlatitude.debug.autoCreateWorldProbe[.timeoutSeconds/.creative]`. **Inert in production.** This is the harness that produced the proof below.

## Method (live)
```
JAVA_TOOL_OPTIONS="-Dlatitude.debug.autoCreateWorldProbe.timeoutSeconds=240" \
  ./gradlew runClient   # JAVA_HOME=temurin-25, runDir run/ with full provider stack
```
Verified from the client log (`tmp/runclient-probe.log`), full mod stack (BoP + Terralith + Promenade + Biolith). The 45s default timed out on cold first-gen; 240s warm run completed in ~15s. GUI screenshotting was unreliable in a crowded multi-client desktop (a different project's dev client + MCA Selector + MC Launcher also running), so verification is log-based.

## Results (live, probe)
World-entry path, no crash, clean exit (BUILD SUCCESSFUL):
```
title → LatitudeCreateWorldScreen → beginExpedition "New World"
0ms     begin expedition (type=latitude, size=REGULAR, zone=temperate)
14ms    bespoke overlay activated
10551ms client game join
10695ms waiting for playable entry (playerAge=1, renderedSections=0, renderReady=false)
11454ms waiting (playerAge=20, renderedSections=11, playerSectionVisible=true)
12444ms waiting (renderWarmupElapsed=false, readyHoldMs=1750, renderedSections=11)
13204ms FIRST SAFE PLAYABLE TICK (playerAge=55, renderedSections=27, warmup elapsed, player+feet visible) → overlay cleared
```
- **1a render-gate proven**: overlay holds through renderedSections 0→11→27, clears only at the first genuinely-playable tick — no void drop.
- **1b early-spawn proven**: `[Latitude] Early initial spawn set before player-spawn pregen: zone=TEMPERATE x=2322 y=71 z=-4720 radius=10000` — latitude-aware placement at a northern-temperate coordinate, before pregen.
- **No crash** with the full custom-biome mod stack (also corroborates bucket #2 coexistence live).

## Scope notes / residual
- The release-checklist item "First-load message appears on NEW world only (Level Loading/Downloading Terrain)" was **discarded as stale** (Julia, 2026-06-06): the bespoke loading overlay intentionally shows on BOTH new worlds (`beginExpedition`) and existing Latitude saves (`MinecraftClientStartIntegratedMixin`); it is the render-gate, not a one-time new-world message. checklist.md updated.
- **1c deferred**: `GlobeWarningOverlay` makes zone/hemisphere entry-titles world-border-CENTER-relative (were hardcoded to z=0 equator) + large-step (>256 blocks) suppression. Real zone-math fix; needs its own proof (DEBUG_ENTRY_TITLES / live) + savepoint.
- Not visually eyeballed: the overlay's rendered pixels/text (behavior log-proven; pixels not captured).
- Probe log evidence: `tmp/runclient-probe.log`.
