# Longitude Earthlike World Overhaul - 2026-07-02

`status: parked design plan` | `scope: worldgen` | `branch: feat/custom-biome-expansion-26.1.2` | `HEAD: 880995c8`

> **Front door:** The comprehensive execution-oriented plan distilled from this research now lives at
> `docs/LATITUDE_2_0_OVERHAUL.md`. Keep this binder note as the dated research, iteration, and evidence log.

## Summary

This note records the read-only Latitude 2.0 planning pass for turning the current flat rectangular world into a more earthlike world with cohesive continents, oceans, continuity, and climate logic.

Decision: keep the 2:1 world as the foundation, but refine the concept. It is useful as a longitude-width planet map, but the current implementation is not true Mercator climate or topology. It is a wider finite rectangle. Future docs should call it a "2:1 projected planet" or "Longitude world" unless Julia prefers the Mercator name for UI familiarity.

Do not revive E/W wrap, teleport loop, or ocean-seam terrain sinking as the 2.0 centerpiece. Prior live evidence says that path stayed visually fake and brittle. The real 2.0 move is world-scale geography first, then climate.

This is a design record only. It does not change source behavior, release readiness, jar/profile state, or live proof status.

## Current Red Evidence

Atlas run assessed:

- Run: `20260702-064708`
- Root: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`
- Branch / commit shown in Atlas: `feat/custom-biome-expansion-26.1.2` / `f86944cb`
- Visible app/export: `/Users/joolmac/Desktop/LatitudeAtlasExport_20260702-064708_20260702-065928`
- Seed: `2591890304012655616`
- Atlas aspect: 2:1

Read-only image/component analysis of the current atlas showed the core red:

- Land fraction: about `64.31%`.
- Water fraction: about `35.69%`.
- Largest land component: `1,076,027` pixels, about `95.08%` of all land, spanning the full atlas.
- Water components: `9,385` total, with no dominant connected ocean basin.
- Largest water component: about `8.0%` of water.

Interpretation: the world currently reads as one mostly connected land sheet with fragmented water holes. That is why it feels like a flat rectangular biome map rather than an earthlike planet.

## Main Decision

Keep:

- The 2:1 longitude-width world size.
- The Legacy/Classic protection path.
- Longitude HUD math and Atlas / World Shape UI direction.
- The idea that Z remains the latitude authority and X provides more world room.

Revise:

- The mental model. "Mercator" is not quite right for the actual implementation because Latitude does not stretch `pick()` coordinates as a map projection. It widens playable X.
- The world-shape priority. Wider is not enough; Latitude 2.0 needs a macro geography authority.
- The old design docs that still imply coordinate transform, true wrap, or edge ocean-sink work as current direction.

Retire for 2.0:

- Seamless E/W wrapping as a core feature.
- Teleport-loop wrap as a realism solution.
- Ocean seam plus terrain sink as a hidden-edge solution.
- Any approach where forced biome paint creates ocean without terrain/coast coherence.

## Architecture Direction

Add a macro geography layer before deeper climate work.

Recommended conceptual split:

- `GeoAuthority`: deterministic world-scale geography summary by column.
- `ClimateAuthority`: deterministic climate summary by column.
- `LatitudeBiomes.pick()`: consumer of the summaries, not owner of all climate logic.

`GeoAuthority` should own:

- Macro land/ocean mask.
- Continent identity.
- Ocean basin identity.
- Coast distance.
- Island arcs and secondary archipelagos.
- Antimeridian/seam placement preference.
- Polar ocean, ice, or cap treatment.

`ClimateAuthority` should own:

- Latitude band.
- Base temperature.
- Altitude or terrain-height proxy.
- Ocean/coast distance.
- Upwind ocean exposure.
- Prevailing wind vector by latitude band.
- Moisture and precipitation.
- Rain-shadow modifier.
- Warm/cold ocean-current modifier.
- Continentality.
- Monsoon or wet-season/dry-season seasonality.
- Final climate province used by biome selection.

Noise should decorate and soften local borders. It should not decide the big climate story by itself.

## Earthlike Climate Model

Use a layered, cheap, non-fluid model:

1. Latitude temperature curve.
2. Land/ocean and altitude corrections.
3. Wind belts from latitude.
4. Ocean moisture sources.
5. Wind-driven moisture advection inland.
6. Orographic rain and rain shadows from mountains/ruggedness.
7. Schematic warm/cold coastal current modifiers.
8. Monsoon seasonality where warm landmasses border warm ocean.
9. Fuzzy biome lookup from temperature, precipitation, elevation, and seasonality.

Expected visible results:

- Wet windward coasts and forests.
- Dry leeward interiors and rain-shadow deserts.
- Coherent subtropical dry belts without tropical deserts.
- Stormier midlatitude bands.
- Cold, dry polar interiors.
- Ocean-moderated coasts.
- More believable continent interiors.
- Biome ecotones instead of hard stripes.

## World Shape And Boundary Plan

The 2:1 world is a good base because a full planet map naturally has about twice as much east-west circumference as pole-to-pole distance. However, the world should not feel infinite east/west or like a rectangle with a wall.

Recommended boundary fiction:

- Treat E/W as an antimeridian or projection edge, not a physical land wall.
- Place the edge mostly in deep ocean, polar ocean, ice, or severe-weather transition by macro geography design.
- Use storm, haze, low visibility, whiteout, current drift, or warning escalation as atmosphere, not as fake wrap.
- Avoid putting major continents across the seam unless future engine/topology work can prove continuity.

True wrap remains a separate future topology spike, not the Latitude 2.0 worldgen overhaul.

## Implementation Phases

### Phase A - Measurement Harness

Before behavior changes, add metrics and overlays:

- Land fraction.
- Water fraction.
- Connected land components.
- Largest land component share.
- Major continent count.
- Connected ocean components.
- Largest ocean basin share.
- E/W seam composition.
- Coast-distance histogram.
- Climate continuity across bands.
- Rain-shadow and windward/leeward maps.

Use the current run as red evidence. The first pass is allowed to fail; its job is to make the red measurable.

### Phase B - Macro Geography Authority

Build `GeoAuthority` behind a stable interface and keep it seed/radius/world-shape aware.

First target:

- 3-6 major landmasses.
- 1-3 connected ocean basins.
- Useful island arcs and archipelagos.
- Polar treatment that reads intentional.
- Antimeridian/seam biased toward ocean or weather.

Do not couple this first slice to heavy density-router rewiring unless the prototype proves the biome-only path cannot create coherent coast/terrain behavior.

### Phase C - Climate Authority

Evolve or replace the current `ProvinceAuthority` with `ClimateAuthority`.

First target:

- Same or equivalent biome outcomes where the new authority is disabled.
- One cheap per-column summary reused by biome picking, feature/structure guards, Atlas overlays, and probes.
- No extra per-quart height probes in hot chunk-generation paths.

### Phase D - Terrain Integration Spike

Investigate the minimum safe terrain integration needed for continents and oceans to exist as terrain, not just biome paint.

Guardrail:

- Do not resurrect the scrapped ocean seam sink.
- Do not start with broad density-function rewiring unless a narrow spike proves the active `globe:` density graph is wired and affordable.
- Keep performance risk front and center because TEST 1 already showed severe generation lag.

### Phase E - Boundary Experience

Implement the boundary only after macro geography gives it a believable setting.

First target:

- E/W edge mostly ocean/ice/storm-zone by geography metrics.
- Warning text and visuals use storm / low visibility / whiteout language.
- No claim of seamless wrap.

### Phase F - Proof And Live Gate

Only after Atlas and headless gates are green should this go to live subjective proof.

Required proof stack:

- Static activation audit.
- Direct method probes for deterministic functions.
- Exact-ID Atlas run and overlays.
- Cohesion metrics.
- `band_correctness_check.py`.
- Terrain-aware Atlas where terrain matters.
- E/W seam crops/metrics.
- All-thread Spark profile around chunk generation.
- Live flyover only for final visual feel, boundary feel, and performance sense.

## Acceptance Targets

Suggested first green targets:

- Land fraction roughly `30-45%`.
- Largest landmass below `50-60%` of land.
- At least `3` major landmasses.
- At least one connected ocean basin above `50%` of water.
- E/W seam mostly ocean, ice-ocean, or storm-zone.
- Tropical hard-arid remains effectively zero.
- Subtropical arid belt remains present and coherent.
- Temperate and polar arid leakage stays clamped.
- Rain-shadow maps show windward/leeward asymmetry where mountains exist.
- Coastal current overlays visibly affect coastal climate without breaking band law.
- New climate authority does not add measurable chunk-generation stalls.

## Risks And Guardrails

- Current active root has unrelated dirty runtime/source files. Future implementation must re-read the dirty diff before patching.
- The current binder handoff may lag actual git HEAD. Trust live preflight over stale handoff values.
- Current `OceanDistanceField` is useful but samples existing continentalness; it is not a complete continent/ocean solution.
- Current `ProvinceAuthority` is active and seed/radius-aware, but too coarse for rain shadows, currents, and real continent interiors.
- Active terrain shape appears still largely controlled by vanilla/Terralith terrain fields; custom `globe:` density functions may be reference/orphaned unless deliberately wired.
- Feature and structure coherence must centralize through the new climate summary, not accumulate one-off guards.
- Avoid changing existing saves or Legacy/Classic behavior during the first 2.0 overhaul slices.

## Deep Review Addendum

This section records a second-pass review of the plan against the active code, binder history, and additional research.

### Terminology and Projection Risk

The 2:1 map should be treated as equirectangular-like for design and proof, not as literal Mercator. A true Mercator projection preserves local shape/direction but sends the poles toward infinity and heavily distorts polar area. Latitude's current 2:1 world keeps finite north/south poles, so "Mercator" is a UI/history name, not the technical model.

Future atlas metrics must distinguish:

- Raw atlas-pixel share.
- Latitude area-weighted share, using a cosine-of-latitude weight per row.

Without area weighting, polar rows can be over-counted when the plan talks about planet-scale land/water share. The current red is still valid visually because the player is judging the rectangular atlas, but future "Earthlike land fraction" gates should report both numbers.

### Hot-Path Implementation Problems To Avoid

The active biome-populate hook is performance sensitive. Future work must not add unbounded sampler loops, per-quart BFS, per-pixel fluid simulation, or repeated height probes inside the resolver.

Specific guardrails:

- `ClimateAuthority` must return one memoizable per-column summary.
- `GeoAuthority` and climate fields should use deterministic low-frequency fields, small local neighborhoods, or precomputed/coarse caches.
- `OceanDistanceField` is not safe as the main macro-ocean engine by itself: it synchronizes `cellDistance`, performs bounded BFS, and depends on current climate-sampler continentalness.
- Any ocean/continent authority used in worldgen should expose a cheap `sample(blockX, blockZ)` path and a separate slower analysis path for Atlas/debug tools.
- All caches must be bounded by chunk/run or stable coarse-cell maps; avoid unbounded global maps keyed by every sampled column.

### First Code Slice Contract

The first implementation slice should be no-op scaffolding plus measurement, not behavior change.

Required shape:

- Add data types/interfaces for `GeoAuthority` and/or `ClimateAuthority`.
- Default them to legacy-equivalent outputs.
- Add probes and Atlas overlays for the new fields.
- Add cohesion metrics that can fail on the current atlas without changing worldgen.
- Gate any behavior-changing use behind a named system property or explicit experimental flag.
- Prove Classic/Legacy and current 2.0 output remain unchanged when the experimental flag is off.

Do not start with terrain-density rewiring, biome reroute policy, or boundary UX. Those should wait until the measurement harness can show the exact red and the no-op scaffolding is proven cheap.

### Terrain Ownership Risk

Biome paint alone can make a map look wetter or drier, but it cannot make oceans, continents, coastlines, drainage, or mountains feel physically connected if terrain is still owned by unrelated vanilla/Terralith fields.

Before implementing terrain behavior:

- Prove which `globe:` density functions are actually active in the current preset.
- Prove whether a narrow density hook can affect land/ocean height without reintroducing the scrapped ocean-seam sink failure mode.
- Treat a terrain spike as separate from the climate-authority slice.
- Require all-thread Spark profiling before live proof.

### Tectonic And River Scope

Do not require a full plate-tectonics simulator for Latitude 2.0. It is likely too broad and too slow for the first overhaul. Use tectonic ideas as macro-shape vocabulary: continents, shelves, island arcs, mountain chains, trenches/ridges as optional later terrain features.

The plan should leave room for rivers and drainage basins after macro geography and rainfall exist. Without drainage, realistic precipitation can still feel disconnected from terrain. River/erosion work is a later phase, not part of the first climate authority slice.

### Additional Acceptance Gates

Add these to the Phase A harness before changing behavior:

- Area-weighted land fraction and water fraction.
- Largest area-weighted landmass and ocean-basin shares.
- Seam composition by latitude band, not just whole-edge average.
- Upwind-ocean fetch distance and rain-shadow score overlays.
- Climate-field continuity check: nearby cells should not jump between unrelated temperature/moisture regimes unless terrain or coast explains it.
- Cache/performance counters: number of climate samples, ocean-distance samples, height probes, and cache hit rate per chunk.
- Exact-ID biome and provider-family metrics; never rely on simplified color PNGs alone for acceptance.

### Problems To Flag Before Coding

- The existing plan mentions 3-6 continents, but a finite Minecraft world also needs playable density: too little land could make a small world feel empty. Keep the first target at `30-45%` area-weighted land, but allow Julia to choose a more game-forward `40-50%` later if playability wins over Earth ratio.
- Seam-ocean bias can accidentally create a boring "water moat" if it is uniform. The edge should vary by latitude: open ocean in some bands, ice/polar ocean near poles, storms or currents where appropriate.
- Rain shadows need mountain/ruggedness truth. If terrain authority cannot expose a stable mountain field cheaply, first implement rain-shadow overlays as diagnostics only.
- Monsoons should be a regional/seasonality tag, not a global wetness bonus. A global monsoon knob would turn whole latitude bands too wet.
- Ocean currents should be schematic and basin-relative. Hardcoded east/west current rules can look wrong if continent layout changes.
- The `GlobeShape`/radius state is static. Future authority code must verify it is safe for the actual server/world lifecycle and cannot leak a previous world's seed/radius/shape into a new world.
- Feature and structure filters need to consume the same climate summary as biome selection, or villages/features will drift out of climate again.

## Third-Pass Adversarial Review

This section records a follow-up review focused on hidden assumptions and implementation traps that could still derail the plan.

### Red Baseline Provenance

The red Atlas run was generated from commit `f86944cb`, while this binder note was written while the active repo HEAD was `880995c8` and the tree already had unrelated dirty files. Before any implementation, re-baseline the red metrics against the exact source/JAR/profile being changed. Do not assume the `20260702-064708` atlas is still the current behavior after uncommitted polar/workgen edits or later commits.

### Water Must Be Split Into Three Truths

Future metrics must not collapse every blue-looking cell into one "ocean" concept.

Measure separately:

- Biome water: exact biome IDs such as `minecraft:ocean`, rivers, frozen ocean, warm ocean, modded water biomes.
- Terrain water: actual sea-level water, lake surfaces, flooded caves, and terrain below sea level.
- Ocean-basin authority: the intended macro ocean/continent mask from `GeoAuthority`.

These three can disagree. A future plan is not green if the biome map shows oceans but the terrain is land, if rivers inflate the water component count, or if the macro ocean mask is coherent but final biome selection fragments it.

### Size-Aware Targets

The same continent target should not be forced on every Latitude world size.

Use size-scaled gates:

- Itty/small worlds: fewer major landmasses can be acceptable if they are visually cohesive and playable.
- Regular/large worlds: require more continent/ocean separation and stronger basin statistics.
- Any target like `3-6 major landmasses` must name the world size it applies to.

For each proof run, record radius, step, atlas aspect, seed, source commit, and whether metrics are raw-pixel or area-weighted.

### Atlas Resolution And Component Pitfalls

Connected-component metrics are sensitive to sampling resolution. Thin isthmuses, one-pixel river cuts, frozen-river seams, or palette classification can radically change component counts.

Required discipline:

- Report metrics at the exact atlas step used.
- Use step16 for final evidence when affordable, step64 for fast iteration only.
- Report ocean-family components separately from river/lake components.
- Add a "bridge sensitivity" metric: what happens if one-cell rivers or one-cell isthmuses are ignored.
- Use exact biome IDs, not color classes, for all component families.

### Finite World Boundary Language

"Antimeridian" is useful for atlas and design language, but it can overpromise continuity. In gameplay, Latitude currently has a finite E/W edge, not a true spherical seam. User-facing and binder text should prefer "projection edge" unless a future topology spike proves crossing or continuity.

The edge goal should be:

- less wall-like;
- climate-appropriate;
- visually intentional;
- not falsely seamless.

### Thread Safety And Authority Lifecycle

Worldgen may run on worker threads. Any new authority cache must be immutable, thread-safe, chunk-local, or deliberately bounded. Avoid synchronized global hot locks and unbounded maps keyed by every sampled coordinate.

Before enabling behavior:

- Test two consecutive world loads with different seed/radius/shape in the same JVM if the harness supports it.
- Verify `GeoAuthority`, `ClimateAuthority`, `OceanDistanceField`, `ProvinceAuthority`, and static `GlobeShape` state reset/rebuild correctly.
- Prove no stale seed, radius, or shape leaks into the next run.
- Count samples and cache misses in headless generation.

### Seasonal Climate In A Static-Biome Game

Minecraft biome choice is mostly static. "Seasonality" should initially mean choosing a biome family that implies seasonality, not simulating actual seasons.

Examples:

- Monsoon: wet/dry seasonal biome tags or choices.
- Mediterranean: dry-summer shrubland/woodland/coast choices.
- Continental interior: larger temperature range and dry-interior biome families.

Do not add dynamic seasonal behavior unless it becomes a separate feature with its own rendering/gameplay proof.

### Rivers And Drainage Should Be Deferred But Prepared

Climate realism will eventually expose the lack of drainage if rainfall has no river or basin expression. Research on hydrology-based procedural terrain suggests rivers/drainage are easier to make coherent when the terrain knows about the drainage network, rather than being painted on afterward.

For Latitude 2.0:

- Do not implement rivers in the first authority slice.
- Reserve fields for drainage intent: basin id, flow direction, coast outlet, wetland/lake propensity.
- If terrain integration later proceeds, evaluate drainage-first or coast-growing approaches before retrofitting rivers onto arbitrary terrain.

### Experimental Flag Contract

Behavior-changing code should be explicitly opt-in until proven.

Suggested contract:

- `latitude.geoV2.enabled=false` by default.
- `latitude.climateV2.enabled=false` by default.
- Atlas/probe overlays may exist without enabling behavior.
- When both flags are false, Classic and current Longitude outputs must remain unchanged for the same seed/radius/step.

The exact property names can change during implementation, but the contract cannot: measurement first, behavior off by default, byte/regression proof before opt-in behavior.

### Minimum Third-Pass Next Slice

The next actual implementation should be narrower than "build GeoAuthority."

Recommended first slice:

1. Add an atlas analyzer script/report that computes raw and area-weighted land/water, ocean-family vs river/lake components, largest component shares, seam composition by latitude band, and bridge sensitivity from existing atlas outputs.
2. Run it against `20260702-064708` and at least one fresh current-HEAD run before changing worldgen.
3. Add Atlas viewer readout support only after the command-line report is trusted.

This creates a hard red and protects the project from starting climate/terrain code while the measurement language is still unstable.

## Five Additional Hardening Passes

This section records five consecutive follow-up passes requested after the third-pass review. These are not new implementation authorization. They are plan hardening gates for making the Latitude 2.0 overhaul harder to misread, overbuild, or prematurely code.

### Pass 4 - Measurement And Red-Gate Audit

The current red must be measured by a planet-geography analyzer, not only a biome-variety analyzer.

Observed local facts:

- The red Atlas run `20260702-064708` has exact-ID `step16_` artifacts and `run_manifest.json` records `radiusBlocks=7500`, `step=16`, commit `f86944cb`, seed `2591890304012655616`, and `emitHeight=false`.
- The existing `tools/atlas/cohesion_representation.py` already measures useful biome cohesion/representation. On the red run it reports `land_cells=1131679`, `single_cell_frac=0.0026`, `distinct_land_biomes=87`, and no per-band monoculture.
- That result is compatible with the visual red: a world can have low confetti and high biome variety while still reading as one giant land sheet with fragmented water.

Plan refinement:

- Extend `tools/atlas/cohesion_representation.py` or add a sibling under `tools/atlas/`; do not create an unrelated analyzer in scratch.
- Treat beaches/shores as their own coast class, not water, when measuring macro continents. The current helper's `is_water()` includes beach/shore strings, which is useful for some representation checks but wrong for ocean-basin proof.
- Every analyzer output must include `run_manifest` values, source commit, radius, world size, step, seed, aspect, and whether the statistic is raw-pixel or latitude area-weighted.
- Add synthetic fixture tests before trusting the analyzer: one rectangle landmass, two landmasses split by ocean, one river cutting land, one one-cell isthmus, one beach ring, and one polar-heavy map. The expected component counts and area-weighted shares must be explicit.
- The first "red" report should compare `20260702-064708` against a fresh current-HEAD run before any behavior change, because the red atlas commit and active HEAD already differ.

Minimum metric set:

- Raw and cosine-latitude-weighted land/water/coast shares.
- Land components, ocean-basin components, river/lake components, and coast components reported separately.
- Largest landmass share and largest ocean-basin share, raw and area-weighted.
- Bridge sensitivity: component counts after ignoring one-cell rivers and after closing one-cell isthmuses.
- Seam/projection-edge composition by latitude band.
- Exact-ID biome-family counts; never color-PNG-only.

### Pass 5 - Projection, Topology, And Boundary Audit

The plan should keep three concepts separate:

- Atlas projection: the 2:1 rectangle used to see the world.
- Gameplay boundary: the finite edge the player can reach.
- True topology: actual wrap/spherical continuity across the engine.

Plan refinement:

- Use "2:1 projected planet" or "Longitude world" for technical docs. "Mercator" can remain historical/UI shorthand only if the UI copy does not imply true Mercator math.
- Prefer "projection edge" or "world edge" in implementation docs. Use "antimeridian" only for atlas/design language, and only when it cannot be mistaken for a proven seamless seam.
- Do not define success as "wrap feels seamless." The retired wrap/ocean-sink path already proved that a teleport seam, cloud shift, and artificial shelves read fake in live play.
- Boundary success for 2.0 is: visually intentional, mostly ocean/ice/storm/coast by macro geography, no arbitrary land wall, no false promise of continuity.
- Any future true topology attempt must be a separate engine research spike with its own success/failure contract. It must not be smuggled into GeoAuthority or ClimateAuthority work.

Implication for the 2:1 plan:

The 2:1 projected world is still a good base for Latitude 2.0 because it gives latitude bands enough east-west room and matches the familiar world-map silhouette. It is not sufficient by itself. It must be paired with macro geography and honest boundary design.

### Pass 6 - Climate, Ocean, And Biome-Classification Audit

The plan should not jump from `ProvinceAuthority` straight into a broad climate rewrite. It needs a stable climate-summary contract first.

Recommended first contract:

- `bandIndex` and `absLatitudeDeg`.
- `temperature01`, with latitude, altitude/height-proxy, current bias, and continentality separated for diagnostics.
- `moisture01` or `precipitation01`, with ocean fetch, windward exposure, orographic lift, and rain-shadow contribution separated.
- `prevailingWindX` and `prevailingWindZ` from latitude belt.
- `coastDistanceBlocks` and `oceanBasinId`.
- `continentality01`.
- `seasonality`, initially as a static class such as `NONE`, `MONSOON_DRY_WINTER`, `MEDITERRANEAN_DRY_SUMMER`, or `CONTINENTAL_RANGE`.
- `climateClass`, a small biome-selection class inspired by Whittaker/Koppen-style temperature plus precipitation thresholds, not a hard-coded biome id.
- `diagnosticFlags` for low confidence, missing terrain height, missing ocean basin, or fallback path.

Plan refinement:

- `ProvinceAuthority` can become a legacy adapter or an input layer, but new behavior should flow through one summary object shared by biome selection, features, structures, Atlas overlays, and probes.
- Climate classes should choose biome families; exact biome ID selection remains a downstream, tag-aware step.
- Monsoon is a regional seasonality class caused by land/ocean thermal contrast and wind shift. It must not be a global wetness boost.
- Ocean currents should be basin-relative and schematic. A hard-coded east/west current rule will break as soon as continents move.
- Rain shadows need a terrain or mountain proxy. If no trustworthy height/ruggedness field is available, rain-shadow overlays stay diagnostic-only.
- Koppen-like dry-summer/dry-winter/monsoon labels are useful for biome-family decisions, but do not imply actual dynamic seasons.

Hard guardrail:

Do not add another pile of final clamps inside `LatitudeBiomes.pick()` as the climate model. The whole reason for `ClimateAuthority` is to make climate inspectable and shared instead of scattered across one-off reroutes.

### Pass 7 - Terrain, Hydrology, And Tectonic Audit

The plan must be explicit that terrain ownership is unresolved until proven. Current noise settings still route major terrain fields through `minecraft:overworld/*` entries such as `minecraft:overworld/sloped_cheese`; the prior amplitude wrapper was scoped as a narrow terrain-bias lever, not a continent/ocean engine.

Plan refinement:

- Terrain integration needs its own spike before any promise that oceans, coasts, mountains, or rain shadows are physically represented.
- The spike must prove which density functions are active, which hooks are applied, and whether height export can validate terrain water against biome water and `GeoAuthority` water.
- `GeoAuthority` may expose terrain intent early, but terrain behavior should remain disabled until a narrow hook proves safe and cheap.
- Full plate tectonics is still too broad for Latitude 2.0. Use tectonic vocabulary as cheap macro intent: orogen belts, passive margins, island arcs, trenches/ridges, shelves, and volcanic chains.
- Reserve drainage fields early even if rivers are deferred: `drainageBasinId`, `flowDirection`, `coastOutletId`, `streamPower`, `lakeWetlandPropensity`, and `endorheicBasin`.
- Do not paint rivers after the fact as the first hydrology solution. Research on procedural drainage repeatedly points to river/drainage networks shaping terrain more naturally when planned before final terrain detail.

Terrain go/no-go checks:

- If height export is unavailable, terrain-water validation is blocked, not green.
- If mountain/ruggedness truth is unavailable, rain-shadow behavior is blocked or diagnostic-only.
- If the density hook requires broad `NoiseRouter` reconstruction, require compile proof, headless boot proof, Classic/flag-off proof, and all-thread performance proof before live.

### Pass 8 - Implementation, Performance, And Rollout Audit

The first implementation plan should be smaller and more testable than "build GeoAuthority."

Recommended executable sequence:

1. Analyzer slice: extend/add the Atlas geography analyzer, fixture-test it, and run it on the old red plus a fresh current-HEAD run.
2. No-op summary slice: introduce `GeoSummary` and/or `ClimateSummary` types with legacy-equivalent outputs, disabled by default and visible only through probes/overlays.
3. Overlay slice: show geography/climate fields in Atlas without changing biome selection.
4. Opt-in prototype slice: enable `GeoAuthority` behind `latitude.geoV2.enabled=true`, keeping `latitude.climateV2.enabled=false`.
5. Climate prototype slice: enable `ClimateAuthority` only after geography metrics are stable and cheap.
6. Terrain spike: only after biome/authority metrics are coherent and the terrain hook has a separate go/no-go.

Performance gates:

- The active biome resolver runs many vertical quart cells per chunk and already needed per-column caches to reduce lag. New authority sampling must be at most one summary per `(blockX, blockZ)` column in the resolver path.
- Add counters before behavior changes: authority summaries created, cache hits/misses, climate sampler calls, height probes, ocean-distance samples, and maximum time per chunk if practical.
- No synchronized global BFS or unbounded coordinate maps in the hot path.
- The Spark proof must sample all threads. A main-thread-only profile can show waiting, but not the worker-thread cost of biome/climate/terrain generation.

Rollout stop conditions:

- Stop if the analyzer cannot distinguish biome water, terrain water, and ocean-basin authority.
- Stop if fresh current-HEAD baseline differs materially from the old red and the difference is not explained.
- Stop if flag-off Classic or current Longitude output changes.
- Stop if source/runtime dirty files are still unexplained before implementation.
- Stop if the authority lifecycle cannot prove two consecutive worlds with different seed/radius/shape do not leak static state.
- Stop if the first prototype increases generation stalls or requires live-only proof for deterministic behavior.

Five-pass conclusion:

The plan remains directionally right: keep the 2:1 projected world, retire fake seamless wrapping as the core promise, measure the geography red first, then build macro geography, then climate, then terrain. The refinement is that Latitude 2.0 should begin with a trustworthy analyzer and no-op summary contracts, not with a visible worldgen behavior change.

## Sources Used

Local/repo evidence:

- `docs/binder/current-state-handoff-20260701.md`
- `docs/binder/atlas-worldshape-longitude-20260701.md`
- `docs/binder/test1-live-findings-20260701.md`
- `docs/binder/mercator-phase1-20260623.md`
- `docs/design/revamped-world-shape.md`
- `docs/design/horizontal-wrapping-feasibility-20260623.md`
- `docs/design/ocean-seam-wrap-plan-20260623.md`
- `docs/binder/arid-belt-earthlike-20260625.md`
- `docs/binder/worldgen-regression-prevention-20260625.md`
- `docs/binder/spark-profile-analysis-20260701.md`
- `src/main/java/com/example/globe/world/LatitudeBiomes.java`
- `src/main/java/com/example/globe/world/ProvinceAuthority.java`
- `src/main/java/com/example/globe/world/OceanDistanceField.java`
- `src/main/java/com/example/globe/GlobeMod.java`
- `src/main/java/com/example/globe/mixin/ChunkGeneratorPopulateBiomesMixin.java`
- `tools/atlas/cohesion_representation.py`
- `tools/dev/run_probe.sh`
- Atlas run `20260702-064708`

External climate and world-design references:

- NOAA Global Atmospheric Circulations: <https://www.noaa.gov/jetstream/global/global-atmospheric-circulations>
- Met Office Global Circulation Patterns: <https://weather.metoffice.gov.uk/learn-about/weather/atmosphere/global-circulation-patterns>
- NOAA Ocean Currents: <https://www.noaa.gov/education/resource-collections/ocean-coasts/ocean-currents>
- NOAA Ocean Exploration, ocean effects on land climate: <https://oceanexplorer.noaa.gov/ocean-fact/climate/>
- NOAA/NESDIS Monsoons: <https://www.nesdis.noaa.gov/about/k-12-education/severe-weather/what-monsoon>
- NWS Rain Shadow glossary: <https://forecast.weather.gov/glossary.php?word=RA>
- National Geographic Biomes: <https://education.nationalgeographic.org/resource/biomes/>
- National Geographic Map Projections: <https://education.nationalgeographic.org/resource/selecting-map-projection/>
- Red Blob Games Mapgen4: <https://www.redblobgames.com/maps/mapgen4/>
- AutoBiomes paper: <https://link.springer.com/article/10.1007/s00371-020-01920-7>
- NASA SVS Map Projections Morph: <https://svs.gsfc.nasa.gov/5090/>
- Esri Mercator projection reference: <https://doc.esri.com/en/arcgis-pro/latest/help/mapping/properties/mercator.html>
- WorldWide Telescope spherical projections guide: <https://docs.worldwidetelescope.org/data-guide/1/spherical-projections/>
- Procedural Tectonic Planets paper: <https://hal.science/hal-02136820/file/2019-Procedural-Tectonic-Planets.pdf>
- Whittaker biome diagram guide: <https://gveg.wyobiodiversity.org/application/files/7916/4641/2117/Whittaker_Diagram_Guide.pdf>
- NOAA Koppen-Geiger climate subdivisions: <https://www.noaa.gov/jetstream/global/climate-zones/jetstream-max-addition-k-ppen-geiger-climate-subdivisions>
- Present and future Koppen-Geiger climate classification maps: <https://www.nature.com/articles/sdata2018214>
- Procedural water bodies using artificial drainage basins: <https://cgvr.cs.uni-bremen.de/papers/cgi22/CGI22.pdf>
- Terrain generation using procedural models based on hydrology: <https://www.cs.purdue.edu/cgvlab/www/resources/papers/Genevaux-ACM_Trans_Graph-2013-Terrain_Generation_Using_Procedural_Models_Based_on_Hydrology.pdf>
- Red Blob Games procedural river drainage basins: <https://www.redblobgames.com/x/1723-procedural-river-growing/>
- USGS Understanding Plate Motions: <https://pubs.usgs.gov/gip/dynamic/understanding.html>

## Next Recommended Work

Start with Phase A only: extend/add the Atlas geography analyzer and produce a red report from `20260702-064708` plus a fresh current-HEAD run. Do not implement the new climate or terrain rules until the current red is measurable, repeatable, fixture-tested, and visible in the viewer.

## Ten Final Whole-Project Hardening Passes

This section records ten additional consecutive passes over the full Latitude 2.0 overhaul plan. It integrates the canonical Minecraft 26.2 pivot, portability architecture, earthlike geography/climate design, implementation sequencing, proof strategy, documentation discipline, and failure-stop rules into one Fable 5-ready roadmap.

### Pass 1 - Canonical Version Pivot

Latitude 2.0 should target Minecraft `26.2`, not remain on `26.1.2` and then port later. The 2.0 geography/climate rewrite is too large to build on an already-superseded canonical. Treat `26.1.2` as the last proven 1.4/early-2.0 reference line and `26.2` as the new 2.0 work line.

Add a new pre-phase before all earthlike work:

- Verify Minecraft `26.2`, Fabric Loader, Fabric API, Loom, Gradle, Java, and mapping truth from current upstream metadata.
- Create an isolated canonical-pivot worktree/branch before touching build metadata.
- Change only version/build metadata first.
- Compile and classify API/mapping drift before any worldgen behavior change.
- Run headless Atlas and exact-ID analyzer proof before staging a jar.
- Record the pivot in binder, release gates, and porting docs before any savepoint/tag/push.

Recommended branch/tag shape:

- Branch: `port/canonical-26.2-pivot` or `feat/latitude-2.0-26.2`.
- Commits: `build: retarget Latitude to Minecraft 26.2`, `port: repair 26.2 API drift`, `test: restore 26.2 atlas proof`, `docs: record canonical 26.2 pivot`.
- Local save tags after proof only: `save/canonical-26.2-baseline`, then later `save/lat2-portability-foundation`.

### Pass 2 - Portability Spine

The old porting pain comes from worldgen algorithms, mappings, mixins, and build metadata being tangled together. The 2.0 rewrite must create a portability spine before adding more climate complexity.

Target structure:

- Core logic: pure Java, no Minecraft imports.
- Platform adapters: all `Holder<Biome>`, registry, Fabric, Mojang/Yarn mapping, `Climate.Sampler`, and chunk-generator touchpoints.
- Mixin hooks: thin, named, audited entrypoints that call adapters.
- Data/config: biome-family taxonomy, climate-class tables, fixture seeds, world-size definitions, and analyzer expectations.
- Proof tools: pure JVM tests, synthetic atlas fixtures, headless Atlas integration, live/profile smoke only after deterministic proof.

The practical rule: a future Minecraft version should mostly touch build files, adapters, mixins, and version docs. If a future worker must re-copy climate algorithms into another branch by hand, 2.0 failed its portability goal.

### Pass 3 - Porting Documentation Area

Create a real `docs/porting/` front door. The current project has valuable porting handoffs, but it lacks a single canonical porting operating manual in the active 2.0 line.

Recommended docs:

- `docs/porting/PORTING.md`: current front door, source-of-truth rules, branch/worktree protocol, proof gates.
- `docs/porting/VERSION_MATRIX.md`: Minecraft, loader, Fabric API, Loom, Gradle, Java, mappings, status, last proof.
- `docs/porting/PORTABILITY_ARCHITECTURE.md`: core/adapters/mixins/data/proof boundaries.
- `docs/porting/ADAPTER_MAP.md`: every platform adapter, owned API surface, target-version drift notes.
- `docs/porting/MIXIN_TARGETS.md`: target owner/method/signature, why needed, proof that it applies.
- `docs/porting/PORTING_CHECKLIST.md`: reproducible port-start checklist with stop conditions.
- `docs/porting/KNOWN_DRIFT.md`: current and historical API/mapping drift, including 26.2 repairs.

Binder entries should record chronology; porting docs should record reusable procedure. Do not make future workers reconstruct process from dated binder notes alone.

### Pass 4 - World-Shape Model Correction

Keep the 2:1 projected-planet plan, but correct the promise:

- It is not literal Mercator.
- It is not a true sphere.
- It is not seamless wrap.
- It is a finite 2:1 projected planet with latitude authority on Z and expanded longitude room on X.

Docs/UI can use "Longitude world" or "projected planet." Use "Mercator" only as historical shorthand where it cannot imply true projection math. Use "projection edge" for gameplay boundaries, not "antimeridian," unless the text is about atlas/design language.

Boundary target:

- Projection edges should be mostly deep ocean, polar ocean, ice, storm belts, or intentionally hostile seas.
- Edges should not host major landmasses unless a future topology spike proves continuity.
- Atmospheric boundary treatment should feel like ocean/weather/visibility, not an invisible rectangle wall.
- True wrap remains a separate research spike, not a hidden dependency of Latitude 2.0.

### Pass 5 - Geography Authority Contract

`GeoAuthority` must own macro geography before climate tries to explain anything.

Minimum summary fields:

- `land01` and `isOceanIntent`.
- `continentId` and `oceanBasinId`.
- `coastDistanceBlocks`.
- `shelf01`, `islandArc01`, `archipelago01`.
- `mountainIntent01`, `orogenId`, and `ruggednessIntent01`.
- `seamRisk01` or projection-edge suitability.
- `drainageBasinId`, `flowDirection`, and `coastOutletId` reserved for later hydrology.

Guardrails:

- The authority must be seed/radius/world-shape aware.
- It must be deterministic and cheap.
- It must expose a hot-path `sample(blockX, blockZ)` and a slower analyzer/debug path separately.
- It must not call unbounded BFS, fluid simulation, or repeated height probes in the biome resolver.
- It must be inspectable in Atlas before it changes biome selection.

### Pass 6 - Climate Authority Contract

`ClimateAuthority` should be a summary producer, not another pile of final clamps in `LatitudeBiomes.pick()`.

Minimum summary fields:

- `latitudeDeg`, `band`, `temperature01`.
- `altitudeCooling01` or terrain-height proxy.
- `continentality01`.
- `prevailingWindX`, `prevailingWindZ`.
- `upwindOceanFetchBlocks`.
- `precipitation01`, `windwardLift01`, `rainShadow01`.
- `currentModifier01` for schematic warm/cold ocean currents.
- `seasonalityClass`: static biome-selection class, not dynamic seasons.
- `climateClass`: compact temperature/precipitation/elevation class for biome-family selection.
- `diagnosticFlags`: fallback, missing terrain proxy, missing ocean basin, low confidence.

Climate realism guardrails:

- Rain shadows require mountain/ruggedness truth. If that truth is missing, rain-shadow output stays diagnostic.
- Monsoon is regional and seasonal, not a global wetness bonus.
- Ocean currents are basin-relative, not hard-coded east/west stripes.
- Biome-family choice should use climate class; exact biome ID selection remains tag-aware and provider-aware.
- Feature/structure filters must consume the same climate summary or they will drift out of biome truth again.

### Pass 7 - Measurement And Proof System

Phase A is still the first executable phase. Build measurement before behavior.

Analyzer requirements:

- Raw and cosine-latitude-weighted land/water/coast shares.
- Biome water, terrain water, and ocean-basin authority separated.
- Land components, ocean-basin components, river/lake components, and coast components separated.
- Largest landmass and largest ocean-basin shares, raw and area-weighted.
- Seam/projection-edge composition by latitude band.
- Bridge sensitivity for one-cell rivers and one-cell isthmuses.
- Size-aware gates for Itty/small/regular/large/massive worlds.
- Synthetic fixture tests before trusting real Atlas runs.

Proof discipline:

- Use exact-ID biome artifacts for biome truth.
- Use height export for terrain-water truth when terrain is in scope.
- Use step64 only for fast iteration; use step16 or better for final evidence.
- Every report records seed, radius, step, aspect, source commit, build/profile jar if relevant, and raw-vs-area-weighted status.

### Pass 8 - Implementation Phases And Sub-Waves

Revised roadmap:

Phase -2: Version truth and 26.2 availability gate.

- Review docs: `AGENTS.md`, main/feature binder split, current handoff, lessons, release gates, porting handoffs.
- Objective: prove 26.2 toolchain coordinates and decide whether any upstream blocker prevents pivot.
- Action: current-source verification, version matrix draft, no repo mutation until branch/worktree is chosen.
- Watch-outs: Fabric/API lag, companion mods lag, 26.3 snapshot temptation, Java/Gradle drift.
- Commits/tags/pushes: none until pivot branch.
- Docs: binder note plus `VERSION_MATRIX.md` when implemented.

Phase -1: Canonical 26.2 pivot.

- Objective: make the build compile and run deterministic proof on 26.2 with no earthlike behavior change.
- Action: metadata bump, API/mixin repairs, compile, headless Atlas, exact-ID analyzer, profile smoke only after deterministic green.
- Watch-outs: stale Modrinth jars, mixin signatures, render API changes, server/worldgen HUD issue regressions.
- Commits/tags/pushes: narrow build/port/test/docs commits; local save tag after proof; push only with Julia approval.
- Docs: binder, release gates, porting matrix.

Phase 0: Portability foundation.

- Objective: split pure logic from Minecraft adapters.
- Action: introduce no-op core summaries and adapter boundaries, flag-off by default.
- Watch-outs: over-refactor, accidentally changing output, static lifecycle leaks.
- Commits/tags/pushes: architecture commits only after pure tests and flag-off proof.
- Docs: `PORTABILITY_ARCHITECTURE.md`, `ADAPTER_MAP.md`.

Phase 1: Measurement harness.

- Objective: make the red measurable and repeatable.
- Action: analyzer fixtures, old-red report, fresh current-HEAD report, Atlas overlay plan.
- Watch-outs: color-PNG metrics, river/ocean confusion, resolution-sensitive components.
- Commits/tags/pushes: analyzer/test/docs commit; no behavior tag.
- Docs: binder evidence and analyzer methodology.

Phase 2: GeoAuthority prototype.

- Objective: produce coherent macro land/ocean/continent/ocean-basin intent behind opt-in flag.
- Action: pure core first, overlays second, opt-in biome consumer third.
- Watch-outs: playable land scarcity, seam moat boredom, hot-path cost.
- Commits/tags/pushes: save tag only when metrics improve and flag-off unchanged.
- Docs: GeoAuthority design, metrics deltas.

Phase 3: ClimateAuthority prototype.

- Objective: make climate explainable and shared.
- Action: wind belts, ocean fetch, continentality, seasonality class, diagnostic rain-shadow where terrain truth is not ready.
- Watch-outs: monsoon over-wetting, hard-coded currents, feature/structure drift.
- Commits/tags/pushes: climate opt-in savepoint after metrics and proof.
- Docs: climate model note and acceptance table.

Phase 4: Terrain integration spike.

- Objective: determine whether terrain can follow macro geography safely.
- Action: active density-function audit, height export, minimal hook spike, all-thread Spark proof.
- Watch-outs: resurrecting ocean-sink seam failure, broad `NoiseRouter` surgery, severe generation stalls.
- Commits/tags/pushes: spike branch/tag only if proof green; otherwise blocked report.
- Docs: terrain go/no-go decision.

Phase 5: Boundary experience.

- Objective: make projection edges intentional and less wall-like without claiming seamless topology.
- Action: geography-biased edge, storm/whiteout/current/haze language, live visual proof after Atlas green.
- Watch-outs: false wrap promise, boring ocean moat, land cliffs at edge.
- Commits/tags/pushes: visual/UX commit after live proof.
- Docs: boundary proof note.

Phase 6: Release candidate hardening.

- Objective: make 2.0 candidate believable, performant, and documented.
- Action: matrix seeds/sizes, mod-present proofs, all-thread profiles, live flyovers, docs closure.
- Watch-outs: Atlas green but live ugly; live pretty but exact-ID wrong; proof clutter treated as release truth.
- Commits/tags/pushes: release-candidate tag/push only with Julia approval.
- Docs: handoff, binder, release gates, lessons if a durable rule changed.

### Pass 9 - Failure Modes And Stop Rules

Hard stop if:

- The worktree has unexplained source/runtime dirt in files the slice would touch.
- 26.2 coordinates are unavailable or contradictory.
- `compileJava` fails from broad API drift and the second repair pass cannot isolate a small cause.
- Any mixin target cannot prove it applies in 26.2.
- Flag-off output differs for Classic/current Longitude.
- The analyzer cannot distinguish biome water, terrain water, and intended ocean basin.
- Fresh current-HEAD baseline does not match old-red assumptions and nobody explains why.
- New authority code leaks seed/radius/shape across consecutive world loads.
- Performance counters or Spark show generation stalls regressing.
- Terrain proof requires broad density rewiring before the measurement/authority layers are green.
- A worker tries to fix live visual ugliness by adding one-off biome clamps instead of fixing shared authority logic.

Soft warnings:

- More than two patches in the same symptom family without a shared-cause audit.
- Atlas metrics improve while live screenshots still read flat/rectangular.
- Live screenshots improve while exact-ID analyzer shows wrong-band leakage.
- Porting starts before the portability spine exists.
- 26.3 snapshot work distracts from stable 26.2 canonical.

### Pass 10 - Fable 5 Execution Handoff

Recommended first five Fable 5 tasks:

1. Write `docs/porting/PORTING.md` and `VERSION_MATRIX.md` for 26.2 canonical truth, without source changes.
2. Create/switch to the agreed 26.2 pivot branch/worktree and bump only build metadata.
3. Repair 26.2 compile/mixin/API drift with the smallest possible commits.
4. Add the Atlas geography analyzer with synthetic fixtures and run old-red plus fresh baseline reports.
5. Add no-op `GeoSummary`/`ClimateSummary` contracts behind disabled flags, then prove flag-off output unchanged.

Do not start with visible continent generation. That is the tempting part, but it is the wrong first move. The first green is a proven 26.2 baseline plus a measurement harness that can show the current red honestly.

Final Fable 5 contract:

- Current 1.4/26.1.2 truth is a reference, not the future build base.
- Latitude 2.0 canonical is 26.2 after the pivot proves compile, atlas, and profile smoke.
- Portability is a first-class deliverable, not cleanup.
- The 2:1 world stays, but the promise becomes projected planet plus macro geography, not fake seamless wrap.
- Geography comes before climate; climate comes before terrain; terrain comes before final boundary experience.
- Every behavior-changing layer is opt-in until measured, fixture-tested, and flag-off safe.
- Binder and porting docs are part of done at every phase.
