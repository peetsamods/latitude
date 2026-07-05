# Phase 4 (Terrain Integration Spike) — terrain wrapper execution log

`status: mechanically proof-complete, LIVE PASS PENDING` · `scope: worldgen` · `date: 2026-07-05`
`branch: port/canonical-26.2-pivot`

## What this covers

The full Phase 4 execution from kickoff prompt (`docs/LATITUDE_2_0_OVERHAUL.md`, "Kickoff Slice Prompt
(Phase 4 only)") through a mechanically-proven implementation. This is the FIRST time Latitude terrain
height has been under the mod's own control — every prior phase (0-3, Biome Consumer) only touched biome
*selection*. Design of record: `docs/design/terrain-wrapper-design-20260705.md` (locked r2).

**Bottom line: mechanical proof is green. The feature is NOT yet done — the roadmap's own hard requirement
that terrain amplitude is live-only-verifiable still applies in full.** Nothing here substitutes for
Peetsa generating and walking a real world.

## Sequence of work (five commits, four `save/phase4-*` tags)

1. **Design lock** (`cd7af947`) — a judge-panel design workflow's 3-candidate fan-out failed outright
   (structured-output retry-cap exceeded on all 3); the synthesis step recovered solo by decompiling the
   real `minecraft-merged-deobf-26.2.jar` and produced a fact-grounded r1 design. Because the failed
   fan-out meant no genuine adversarial diversity had occurred despite the plausible result, a SEPARATE
   4-lens adversarial review + independent-verify pass was run against r1 and found 2 high/critical
   CONFIRMED defects: a unit-mismatch bug (wrapping both `finalDensity` and `preliminarySurfaceLevel` with
   one additive term when they are in incommensurable units — density vs. Y-block level) built on a false
   premise (spawn/heightmap does not read `preliminarySurfaceLevel`), and a gating method
   (`GlobeMod.isGlobeWorld(RandomState)`) that does not exist. Revision r2 wraps only `finalDensity`,
   corrects the gate, corrects an overstated smoothness claim, and adds a residual-risk register (R1-R6).
   See `docs/design/terrain-wrapper-design-20260705.md` and `LESSONS.md` L16 (main worktree).
2. **Implementation** (`d7e71a0e`) — `GeoTerrainBiasFunction` (the `DensityFunction.SimpleFunction`
   wrapper), `TerrainRouterWrapping` (shared install-gate helper), `RandomStateAccessor`/
   `RandomStateRouterTerrainMixin` (the `ChunkMap`-constructor-TAIL interception, chosen over the design's
   primary `RandomState`-constructor target because that site has no reliable positive globe check in
   scope), three new flags on `LatitudeV2Flags` (`latitude.terrainV2.enabled`/`.strength`/
   `.oceanStrengthRatio`, all default true-no-op). Mid-implementation, a real gap was caught before commit:
   `RandomState.create(...)` is called from TWO places, not one — vanilla's `ChunkMap` AND this mod's own
   `BiomePreviewExporter` dev/atlas tooling — so a `ChunkMap`-only gate would have left the atlas/proof-
   harness path permanently unwrapped, making any headless mechanical proof pass for the wrong reason. Fixed
   via a shared helper called from both sites.
3. **Mechanical proof harness** (`f7d7d655`) — `TerrainProofHarness` (dev-only headless probe). Its own
   structural check (`generator.getBaseColumn`, not just a direct `compute()` sample) caught a second real
   defect: `DensityFunction.SimpleFunction`'s default `mapChildren` ("return this") hid the wrapped delegate
   from `NoiseChunk`'s cache/interpolation-substitution visitor, so an installed-but-strength=0 run was NOT
   byte-identical to a never-installed baseline in *structure* despite matching in direct sampled *value* —
   exactly residual risk R5. Fixed by overriding `mapChildren` to recurse via the visitor. Reported
   "34/34 PASS" across Classic and Mercator. **Also surfaced the K-scaling finding**: at strength=1.0,
   K=0.25 on vanilla density produced a floating landmass from Y~106 to the world ceiling — a live-pass
   calibration concern (R4), not a code defect.
4. **Sweeper audit** (workflow `wf_2de5fa57-e76`, 6 lenses, 32 agents) — found the "34/34 PASS" claim from
   step 3 was **not trustworthy**, and one **critical** defect the mechanical proof never exercised:
   - The harness's own non-globe/globe-gate-isolation leg crashed with an unguarded `Integer.parseInt` on
     an empty-string-forwarded property (Gradle's `vmArg` forwarding sends `""`, not "absent", for an
     unpassed knob), and separately booted an *actually-armed globe world* (stale committed
     `server.properties`) while labeling itself excluded — meaning that leg never validly ran.
   - **Critical:** `TerrainRouterWrapping`'s gate checked only `LatitudeV2Flags.GEO_V2_ENABLED` (a boolean)
     as proof `GeoAuthority`'s land/ocean field was live. `LatitudeBiomes.rebuildGeoAuthority()` only builds
     a real `GeoAuthorityProvider` when `seed != 0 && zRadius > 0` — on a seed-0 world, the flag says "on"
     but the provider silently stays the NEUTRAL no-op (`land01=0.0` everywhere), so the wrapper would have
     pushed the **entire world** downward (100%-ocean bias). Recorded as `LESSONS.md` L17.
   - 23 of 26 raw findings survived independent verification (an unusually high confirmation rate).
5. **Fix + real re-verification** (`e1008128`) — every HIGH/MEDIUM finding fixed for real (not
   cosmetically); LOW findings fixed or disposed with a stated reason. The critical fix: gate on
   `geoProviderForTerrain() instanceof GeoAuthorityProvider`, refusing to install (`InstallResult.
   SKIPPED_NOOP_PROVIDER`) regardless of the flag if the provider is still the no-op. The harness now
   records a **definitive** `installResult`/`wrapperInstalled`/`geoProviderClass` per run instead of
   scraping a once-per-JVM install log, and **hard-fails** (`harnessFailure`) if a leg's actual world
   classification (radius, generator preset, `isGlobeOverworld`) doesn't match what it claims to be testing
   — closing the "passes for the wrong reason" gap outright, not just patching the one crash. **Full
   mechanical proof gate re-run for real** on Classic and Mercator (7 legs each: baseline, installed-S=0
   value+structure, armed, geoV2-off, non-globe with self-verified classification, seed-0, seed-0 baseline)
   — all legs pass with raw JSON evidence spot-checked directly by the coordinator (not trusted from a
   summary alone), in `run-headless/latdev/proof/` (gitignored, regenerable). A new residual risk R7 was
   added to the design doc: the absolute-Y alpine snow-line (`LatitudeBiomes.alpineSnowMinY`) can put a
   cold snow cap on a subtropical peak once the bias lifts it past ~Y182 — a specific, higher-visibility
   instance of the "cold biome in a warm latitude" failure class this project has been burned by before,
   now an explicit named item on the live-pass checklist.

## Current mechanically-verified state

- Flag-off (`latitude.terrainV2.enabled=false`): byte-identical, provably (never-install path).
- Flag-on, strength=0.0: byte-identical in both sampled *value* and *pipeline structure* (the mapChildren
  fix closed the structural gap).
- Flag-on, geoV2 off: byte-identical (the flag-only gate would have been a trap here too, but `GEO_V2_
  ENABLED` off already means `GEO_V2_PROVIDER` is the NoOp, and the additional `instanceof` check makes this
  doubly explicit).
- Flag-on, seed=0: byte-identical (the critical fix — gate refuses to install on a NoOp provider,
  confirmed via a real seed-0 test showing land fraction unchanged from the seed-0 never-install baseline).
- Non-globe world, flags on: byte-identical (globe-gate genuinely excludes it, self-verified by the
  harness, not inferred).
- Armed (strength>0) on a real globe world: land columns shift toward solid, ocean columns shift toward
  air, neutral (land01≈0.5) columns match the formula's analytical prediction exactly; surface-Y and
  `getBaseHeight` move in the same direction (spawn agrees with rendered surface); three Lipschitz
  transects (interior coast, edge/pole ramp band, domain-warp snap crossings) show no cliff; land-fraction/
  ocean-authority-veto shift is monotone with geography (no bad flips).
- Dev environment has zero third-party mods installed, so every leg above was already the vanilla-only
  configuration (design §6.4); a separate Terralith-present comparison was not available on this machine
  (no compatible 26.2 jar found) — an honest, disclosed gap, not a silent skip.

## What is NOT yet proven (the live pass)

Per the design's own honesty (§6.5) and the roadmap's own framing of this phase: **terrain amplitude is
live-only verifiable.** The atlas is blind to terrain height entirely; every check above is a density-value
or structural assertion, not an aesthetic judgment. The live-pass checklist, consolidated from the design
doc:
- Coastlines: believable, no artificial shelves/cliffs, character preserved (K/strength dialed at the
  keyboard — start LOW given the vanilla floating-landmass finding at strength=1.0).
- Deep ocean, deep interior, a mountain range, and a polar cap — looked at directly.
- **Specifically eyeball a lifted mountain in a WARM (subtropical) band for anachronistic snow** (R7) —
  this is a distinct, higher-visibility check from the general badlands/coastal surface-band check (R3).
- Badlands/coastal absolute-Y surface bands (terracotta, beach fill, coarse_dirt) for detachment from the
  new waterline (R3).
- The `isMercator()` gating decision (§3): does the edge/pole ocean-ring artifact look acceptable on both
  Classic and Mercator, or does one shape need a different gate/strength?
- Whether spawn-height-finding still agrees with rendered terrain at the chosen live strength.

## Residual risks carried into the live pass (design doc §9, R1-R7)

R1 (edge/pole ocean ring is intentional-direction, disclosed not hidden), R2 (`preliminarySurfaceLevel`
coarse estimate lags the biased surface, by design), R3 (absolute-Y surface bands), R4 (K may need to be
much lower than the starting 0.25, confirmed real on vanilla), R5 (closed — the mapChildren fix), R6
(vanilla `depth` climate parameter unwrapped, shore-band caveat), R7 (subtropical alpine snow-line, new
this pass).

## Next step

Peetsa's live pass, generating and walking a real small world with `latitude.terrainV2.enabled=true` and a
LOW starting `latitude.terrainV2.strength` (given the vanilla floating-landmass finding at 1.0 — start
well below that and dial up), per the checklist above. End with a plain go/no-go: does terrain now respect
the land/ocean map without looking broken, and is Phase 5 (Boundary Experience) next, or does Phase 4 need
another live-tuning round first.
