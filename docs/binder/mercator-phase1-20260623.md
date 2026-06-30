# Mercator world type — Phase 1 (wider world, more biomes) — 2026-06-23

`status: implemented (source-proven, live-pending)` · `scope: worldgen + client + ui` · `date: 2026-06-23`

Phase 1 of the long-deferred "Revamped world shape". **There is NO user toggle** (Peetsa 2026-06-23: "just
make it 2.0, nobody needs square"): ALL NEW Globe worlds are Mercator. `GlobeShape.CLASSIC` survives only for
backward-compat (pre-feature saves stay square). Mercator = a 2:1 world (X half-extent = 2× the Z/latitude
radius). **Peetsa chose "wider world, more biomes" over the literal Mercator coordinate stretch** — so the biome map is NOT stretched; `pick()` is
unchanged and a Mercator world simply has 2× the E-W area, giving each latitude band ~2× more biome
regions. `ASPECT = 2.0` fixed. Default Classic (opt-in). Design: `docs/design/revamped-world-shape.md`;
build checklist + pivot banner: `docs/design/mercator-phase1-impl-plan.md`.

## What shipped (real symbols)
- **Foundation:** `LatitudeBiomes.GlobeShape{CLASSIC,MERCATOR}` enum + `ACTIVE_GLOBE_SHAPE` volatile +
  `setGlobeShape/getGlobeShape/isMercator/shapeFromString/shapeToString` + `MERCATOR_ASPECT=2.0` +
  `getActiveXRadiusBlocks()` (= round(zRadius*ASPECT) in Mercator, = zRadius in Classic). No `effectiveX`
  transform (the stretch was removed).
- **Persistence:** `LatitudeWorldState` gains `globe_shape` via `Codec.STRING.optionalFieldOf("globe_shape","classic")`
  (existing saves deserialize to "classic" — no DataFixer, no level.dat change). `get()` calls
  `ensureGlobeShape()` → pushes to the live cache. `setGlobeShape()` also pushes to `LatitudeBiomes`.
- **No create-world UI toggle.** A "World Shape" stepper was built then REMOVED per Peetsa ("just make it
  2.0"). Create screen + launcher + `GlobePending` reverted to `f26d5f58`. New worlds become Mercator in
  `GlobeMod.initLatitudeBiomesForWorld`: `if (gameTime < 100 && "classic".equals(persisted)) setGlobeShape("mercator")`.
  Existing/legacy saves (gameTime ≥ 100, or already-persisted shape) are untouched → stay square (legacy-pin).
- **Border (square, sized to X):** `GlobeMod.setGlobeBorder` sizes the square `WorldBorder` to `2*X_RADIUS`
  (= `4*Z_RADIUS` in Mercator; Minecraft borders are square-only). Latitude authority + pole start stay on
  `Z_RADIUS`. Publishes `Z_RADIUS` via `LatitudeMath.setLatitudeZRadius`.
- **Latitude-denominator decoupling (the subtle part):** the square border is now X-sized, so
  `util.LatitudeMath`'s Z-latitude functions (which divided by the border half) would report half-latitude
  in Mercator. Fixed with `latitudeZRadiusOverride` (0 ⇒ use border half ⇒ Classic byte-identical), consumed
  by a new `latitudeRadius(border)`; `latNormFromZ` + `poleRemaining*` now use it, which transitively
  corrects `degreesFromZ/absLat*/latitudeDegrees/formatLatitudeDeg/zoneFor/zoneKey`. New `hazardProgressZ`
  for the pole; `hazardProgress` (X/EW) unchanged.
- **Pole hazard:** `GlobeMod.borderUxTick` + client `GlobeClientState` pole sites (L182/235/351/444) use
  `hazardProgressZ` (fires at the geographic pole `|Z|=Z_RADIUS`, interior to the X border). EW (X) sites
  unchanged.
- **Client sync:** `GlobeNet.GlobeStatePayload` gains `int latitudeZRadius` (VAR_INT). Server sends
  `getActiveRadiusBlocks()` on JOIN; client (`GlobeModClient`) calls `LatitudeMath.setLatitudeZRadius(...)`
  on receive and resets to 0 on DISCONNECT. Works in singleplayer and multiplayer.
- **Spawn:** `resolveSpawnChoice`/`findLandSpawn` split bounds — X search over `X_RADIUS` (wider), Z-jitter +
  latitude targeting + pole clamp on `Z_RADIUS`; `clampSpawnAwayFromEwWarning` uses `X_RADIUS`.

## Proof
- `./gradlew compileJava` + `build` green; staged jar SHA `602000c6` (profile `Lat 1.4+26.1.2`).
  (Earlier `6c7b4dea` was the with-toggle build, superseded by the no-toggle one.)
- `tools/check-biome-tuning-policy.py` PASS; `tools/check_tree_line_port.sh` PASS.
- **Classic byte-identity (key safety gate): PASS.** Headless atlas seed 2591890304012655616 small/step64:
  `step64_biome_ids.png` SHA-256 = `37904cda…` — **byte-identical** to the pre-change baseline
  (run 20260623-081837), topBiomes identical. Mercator changes introduce zero Classic regression
  (`pick()` is untouched; override defaults to 0).
- **Mercator is live-only verifiable.** The headless atlas renders the Classic biome function on a square;
  it cannot exercise `GlobeMod.setGlobeBorder` (border widen, pole pin, spawn, HUD sync). Mercator biome-map
  correctness is guaranteed by `pick()` being unchanged (same biome at any (X,Z); the world is just bigger).

## Live-pending (Peetsa, Modrinth App `Lat 1.4+26.1.2` only — HARD RULE, never Mojang launcher)
1. Create a **Mercator** world → confirm 2:1 face (E-W ~2× the N-S), more biome variety across each band,
   EW storm wall at the east-west edge, pole hazard at the geographic pole (not the far border), spawn on
   land at the right latitude, HUD latitude/zone reads correctly (pole shows ~90°, not ~45°).
2. Create a **Classic** world → confirm visually unchanged.
3. Open a **pre-existing save** → loads as Classic (square), unchanged, no border resize. (Critical: existing
   worlds must NOT silently convert to 2:1.)
4. Create a couple of NEW worlds → confirm every one is Mercator automatically (no shape option on the screen).

## Notes / deferred
- Art II: port to 1.21.11 / 1.21.1 / 1.20.1 after canonical Phase 1 is live-proven.
- Phase 2 (continent classifier) + Phase 3 (Level-2 climate) remain deferred. ODF stays native (Phase 1).
- Biome-mod test-set expansion is a separate test-infra task (see impl-plan §5); not blocking.
