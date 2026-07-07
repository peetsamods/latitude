# Phase 4 (Terrain Integration Spike) — terrain wrapper execution log

`status: LIVE PASS IN PROGRESS — install-timing bug found + fixed, mechanism confirmed working live,
strength calibration ongoing` · `scope: worldgen` · `date: 2026-07-05, updated 2026-07-06`
`branch: port/canonical-26.2-pivot`

> **2026-07-06 update (read this first):** the mechanically-proof-complete state below (as of 2026-07-05)
> turned out to hide a critical bug that made the wrapper **never install on any freshly created world,
> regardless of seed or strength** — found on Peetsa's very first live create-world test, the exact gate
> this whole phase existed to require. See "Live pass findings (2026-07-06)" below for the full story: a
> second critical bug (install-timing), a real performance regression, and the actual first confirmation
> that the mechanism works. Also two unrelated fixes landed the same day (a Sodium client-crash fix and an
> arid/savanna biome-boundary fray fix) — noted here for the same-day record but tracked as their own,
> separate concerns; see their own commits/tags, not this phase's design doc.

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

## Live pass findings (2026-07-06)

Peetsa's first live create-world test with `latitude.geoV2.enabled=true`/`latitude.terrainV2.enabled=true`
found the wrapper doing **nothing at all** — no visible effect at any strength, including 1000. This was
NOT a strength-calibration problem; it was a second critical, previously-undetected defect.

### Bug: the wrapper never installed on any live-created world (any seed, any strength)

Confirmed directly from the client log: `"[Latitude] Phase 4 terrain bias NOT installed: geo provider is
still the NoOp..."` appeared, followed one line later by the real province-authority seed/radius log that
would have made the check pass. Root cause: the `ChunkMap`-constructor install site (real gameplay) runs
**before** `GlobeMod`'s create-world flow finishes rebuilding `GEO_V2_PROVIDER` for the new world. The
L17/sweeper-finding-#7 safety check (an `instanceof GeoAuthorityProvider` gate, added to stop a seed-0 world
from silently sinking the whole world toward ocean) was checked ONCE at install time — and always saw the
still-uninitialized placeholder, so it permanently refused to install, on **every** freshly created world,
not just seed-0 ones. Neither the mechanical proof gate nor the 6-lens sweeper audit could have caught this:
both exercise `TerrainRouterWrapping.installIfArmed` only via `BiomePreviewExporter`'s dev/atlas path, which
builds its `RandomState` on an already-fully-loaded `ServerLevel` — `GEO_V2_PROVIDER` is already real by
the time that path calls `installIfArmed`, so the exact ordering hazard that broke real gameplay cannot
occur on the harness's own call path at all. Recorded as `LESSONS.md` L18 (main worktree): a proof harness
sharing production code is not enough if it can't reproduce the real call's timing.

**Fix** (`95bca16c`): moved the `instanceof GeoAuthorityProvider` check out of the one-time install gate and
into `GeoTerrainBiasFunction.compute()` itself, re-evaluated on every call rather than snapshotted once.
The wrapper now always installs structurally once the flag/globe gates pass, and simply no-ops per-column
until the real provider is ready — which resolves itself moments after world load, long before any chunk is
generated for a player, and stays a no-op forever on a genuine seed-0 world (preserving the original L17
guarantee via a correctly-timed mechanism). `TerrainRouterWrapping.InstallResult.SKIPPED_NOOP_PROVIDER` was
removed (no longer produced); `INSTALLED` now means "structurally wrapped," not "actively biasing this
call" — see both classes' updated javadoc. This fix **cannot be verified by atlas regression** for the same
structural reason it evaded the original proof gate; `TerrainProofHarness`'s own doc comments were updated
to state this plainly.

### Bug: a real chunk-generation performance regression at any nonzero strength

Once the wrapper actually started installing, Peetsa reported chunk loading behaving noticeably slowly.
Root cause: `GeoAuthority.sample(x,z)` (domain warp, plate-cell lookup, several noise evaluations — real
work) doesn't depend on Y, but `GeoTerrainBiasFunction.compute()` is invoked once per vertical
noise-lattice cell (roughly every 8 blocks of world height, tens of times per column) — every one of those
calls was redundantly re-running the full `GeoAuthority` cost for the identical `(x,z)`. Invisible to the
Phase 4 Spark baseline, which was captured at `strength=0` (a fast path that never reaches this code at
all). **Fix** (`0be832f2`): a per-thread, single-entry last-column memo (`ThreadLocal`, since chunk
generation is multi-threaded) — chunk generation visits a column's Y-levels back-to-back before moving on,
so a single last-value cache catches essentially all the redundancy. Value-only optimization; does not
change any prior byte-identity proof result. Recorded as part of `LESSONS.md` L18's evidence.

### First live confirmation the mechanism actually works

After both fixes, Peetsa confirmed at `strength=10.0` (deliberately extreme, for a stress-test-strength
confirmation, not a usable value) a dramatic land/ocean boundary — a sheer rift canyon with lava exposed at
the bottom where land was pushed sharply up next to ocean pushed sharply down. This is the first real,
positive confirmation that the wrapper reads live geography and reacts to it correctly. `strength=1.0` alone
was already known (from the original mechanical-proof-era finding, R4) to produce a floating landmass on
vanilla density, so the actual usable range is expected well under 1.0 — calibration is ongoing.

### Live-tuning method: use `GeoAuthority` directly, not the displayed biome map

Free-flying at different strength values without a fixed comparison point was reported as hard to judge,
compounded by the slow chunk generation above. Also: since `latitude.biomeConsumerV2.enabled` is off
(default), the *displayed* biome/ocean placement is driven by an entirely separate legacy system,
independent of `GeoAuthority`'s own land/ocean field — picking a "coastline" off the visible map does not
guarantee the terrain wrapper has any reason to do anything there. Recorded as `LESSONS.md` L19. A small
standalone Java scan against the compiled `core.geo.GeoAuthority` class directly (no Minecraft bootstrap
needed — Core Logic is plain Java by construction) located a genuine, gradually-softened coastline at
seed 0, Mercator, regular size, around `x=9600, z=3372` (~30°N): solid land (`land01=1.0`) west of ~x=9256,
a smooth ~500-block transition, solid ocean (`land01=0.0`) east of ~x=9856. Handed to Peetsa as a fixed,
reproducible before/after test point.

> **CORRECTION (2026-07-06, Fable 5 audit P0-2 — supersedes the coordinate above AND the "Next step"
> below).** The coordinate above was derived from a standalone `GeoAuthority(seed=0)` scan, but a live
> world whose typed seed is literally `0` NEVER arms GeoAuthority (`rebuildGeoAuthority()` declines at
> `seed == 0`, permanently, by design) — that coastline exists in no live world, and following the recipe
> as written reproduces the exact "no visible difference at any strength" symptom of the install-timing
> bug this same doc records. Worse: creating a seed-0 world AFTER a real-seed world in the same game
> session silently reuses the earlier world's geography (stale-provider finding, same audit).
> **Corrected recipe: create the world by TYPING seed `2591890304012655616` (Mercator, regular size) and
> fly to `x=-3300, z=-3636` (~32.7°S). `land01` ramps smoothly 0.10→0.99 from ocean (west, x≈-3400) to
> land (east, x≈-2840) over ~560 blocks — stay west of x≈-2840; beyond it is an abrupt island edge, not
> the graduated ramp.** (Re-derived 2026-07-06 with the same standalone scanner at the pinned seed.)
> The "find the value" calibration framing below is RETIRED: the audit measured an empty usable strength
> window on the current Y-uniform formula (ground ≤±1 block below S≈0.10; a detached ceiling slab at
> Y256–319 from S=0.10 on 3/3 seeds; the ocean-ratio escape closes with lava-floored voids) — a Y-aware
> taper is a prerequisite before ANY strength tuning. See `fable5-overhaul-audit-report-20260706.md`
> (P0-1/P0-2) and registry row `20260706-fable5-slice-a-truth-restore`.

## Next step

Strength calibration is now the open item — the mechanism is confirmed working; find the value that reads
as "terrain believably follows the map" rather than "canyons and rifts" (>1.0 is confirmed too strong on
vanilla density) or "no visible difference" (too weak). Once a working strength is found, complete the
live-pass checklist above (mountain range, polar cap, subtropical-peak snow-cap check per R7, badlands/
coastal band check per R3, Classic vs Mercator parity) and end with a plain go/no-go on whether Phase 5
(Boundary Experience) is next, or whether Phase 4 needs another round.
