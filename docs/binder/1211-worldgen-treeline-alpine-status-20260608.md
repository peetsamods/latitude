# 1.21.11 worldgen-works + tree line / alpine / snow — status (2026-06-08)

`scope: worldgen + client (port line)` · `status: active, UNCOMMITTED past beta.22` · `branch: port/1.4.0-beta-1.21.11`

Full handoff (canonical detail): `<repo>/Latitude-port-1.4.0-1.21.11/docs/porting/HANDOFF-1.21.11-worldgen-treeline-alpine.md`.

## What happened
1. **In-game globe worlds were generating as vanilla** on 1.21.11: the globe `ChunkGeneratorSettings` key does not survive level.dat serialization (reloads unkeyed/inline), so every key-based check failed. Fixed by persisting the radius per-world (`LatitudeWorldState.globeRadius`) + a runtime gate (`GlobeMod.shouldApplyLatitudeWorldgen`) + cheap spawn (`gen.getHeight`) + re-synchronized `OceanDistanceField`. Committed `18f2629f` (not pushed).
2. **Tree line + alpine surface + snow** (new features): `TreeLineVegetationGuardMixin` (suppress trees above Y168, cherry-grove exempt), `AlpineSurfaceMixin` (rock above Y172 + latitude-graded snow, coherent `ValueNoise2D` blobs), warm-band snow guard now allows high-altitude snow, + Latitude loading splashes. Committed `da3e2ecb`/`ab061989`; refinements (blob noise, expanded snow, cherry exemption) UNCOMMITTED (beta.23).
3. **Branch reconciliation**: integrated Codex's official reload-loading fix (`MinecraftClientStartIntegratedMixin`, from `codex/14-existing-world-loading-overlay`) onto this branch verbatim (beta.24); removed the non-working `GlobeStatePayload` activation. `codex/expanded_teleport` (teleport functions) NOT yet integrated.

## Evidence status
- **In-game validated** (Julia): globe worlds work (border/compass/latitude/biomes), reload loads cleanly, tree line consistent, loading splashes. beta.24 (expanded snow + rockier tops + cherry + integrated loading fix) **pending Julia validation**.
- **Atlas/map proof N/A for these**: atlas renders biomes, not terrain/surface — tree line/alpine/snow/amplitude are proven in-game; atlas only confirms biome distribution unchanged.

## Governance notes (Constitution + Release Hard Gate, read 2026-06-08)
- Art II: these worldgen features should land on **canonical `feat/1.3.1-cohesive-horizons-26.1.2`** then port to all MC lines (currently 1.21.11-first for testing velocity — reconcile).
- Art VI: cell-hash ban is biome-selection-specific; alpine fade uses coherent `ValueNoise2D` (blobs).
- Art XI: alpine debug log flag-gated (`-Dlatitude.debugAlpine`).
- **legacy-worldgen-policy-pin** (`save/legacy-worldgen-policy-pin`): tree-line/alpine currently retro-apply to existing globe saves (gated on `ACTIVE_RADIUS_BLOCKS>0`) — must be pinned to worlds created with the feature. OPEN.
- **Amplitude is NOT tunable via globe data**: the globe `terrain_spline` graph is dead (live terrain = vanilla/Terralith `minecraft:overworld/*`). Amplitude needs a runtime DF wrapper. OPEN.

## Open / next
Validate+commit beta.24 · integrate `codex/expanded_teleport` · amplitude knob (runtime wrapper, Auto+manual) · Treeline standalone mod · legacy-pin the new features · port to canonical/1.21.1/1.20.1 · push · break-it bugs (dimension-aware gate, spawn-safety).
