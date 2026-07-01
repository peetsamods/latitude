# TEST 1 live-test findings — Peetsa, 2026-07-01 (Atlas/World Shape toggle build)

`status: open punch-list` · `scope: rendering, create-world UI, worldgen, structures, perf` · `build: TEST 1.jar (feat/custom-biome-expansion-26.1.2 @ 30db22fc)`

Peetsa live-tested `TEST 1.jar` (the Atlas / World Shape toggle build) in the Modrinth profile `Lat 1.4+26.1.2`
and reported the batch below via annotated screenshots. **The toggle itself works** — both Mercator 2:1 and
Legacy 1:1 create playable worlds, longitude reads on the compass, and the 2.0 build launches clean. Everything
here is polish/correctness/adjacent-worldgen follow-up, not a regression that blocks the feature's core.

Code pointers were gathered by five read-only recon passes on 2026-07-01. Priorities are **suggested triage for
Peetsa to confirm**, not decided. Line numbers are as of `30db22fc` and will drift.

Legend — Pri: **H** high / **M** medium / **L** low. ⛔ = blocked on an external dependency.

## Status (updated 2026-07-01, TEST 6) — source-green, live-eyeball pending
- ✅ **A2** Legacy atlas square — `13f60b27` (TEST 2)
- ✅ **B1** loading-message randomize + 70% newer-bias + anti-clustering — `b5fd8e1b` (TEST 3)
- ✅ **C2 (forbid half)** villages banned from bog/swamp — `5dc6377e` (TEST 3). Full village-biome **audit** still open (⛔ Notion).
- ✅ **E1** climate-aware E/W storm (snow + grey→whiteout in cold bands), "sandstorm"→"storm" — `b24553d7` (TEST 3) + **escalating, climate-specific warning text** ("Storms and low visibility" → "Whiteout"/"Blinding sandstorm") — `ac3ff0bf` (TEST 5). Ocean edge confirmed good by Peetsa. Fog-*color* tinting still a follow-up.
- ✅ **A5** Re-create copies source seed — `12f53f0a` (TEST 3) + **changing World Size no longer erases the seed** (seedInput backing field) — `65439d26` (TEST 5).
- ✅ **C1** mismatched (savanna) village cancelled by **latitude band** — `cadd7230` (TEST 4)
- ✅ **C3** chunk-gen lag — **REAL FIX**: `pick()` memoized **per column** (was recomputed ~96×/column; only deep cells fall through) — `7d5918ef` (TEST 5). (Earlier `isBiomeId` memoize `1bc2f8a4` kept.) *Needs live confirmation the lag is gone.* If still laggy, next lever = `entriesForTag()` alloc/sort.
- 🔨 **A1** atlas redesign (translucent latitude stripes over a continent outline, framed with the vanilla MC map graphic) — direction given by Peetsa in TEST 4 feedback; **in progress**, not yet built.
- 🐛 **Bug-catcher pass** (adversarial find→verify, 6 dimensions — see `bug-catcher-20260701.md`): 2 confirmed bugs fixed in TEST 6 — existing Classic/legacy saves flipping to Mercator on load (`4a73791a`, save-corruption) and Re-create dropping the source world name (`018e4d0e`). Border-overlay dimension crashed mid-run; re-running + templates self-verified.
- ⏳ **Still open:** A3 (cramped labels), A4 (layout side — needs decision), C2-audit + D1 /latdev list (⛔ Notion).

---

## A. Atlas / create-world screen (the feature we just shipped)

### A1 — Atlas "looks like a flag," lacks inspiration · Pri M
> "it worked, but the Atlas lacks... inspiration. Looks like a flag. lol"

The preview is flat horizontal climate stripes on a solid field, which reads like a flag rather than a map.
Needs an atlas/map treatment (framing, graticule, land/water feel, labels — design TBD).
- `client/create/LatitudePlanisphereRenderer.java` — `renderCompact()` L289–351 (ocean base fill L300–304,
  band strip fills L327–333). This is the aesthetic surface to redesign.

### A2 — Legacy 1:1 atlas should be a SQUARE, currently renders a CIRCLE · Pri H  (bug in the just-shipped feature) · ✅ FIXED `13f60b27` (TEST 2), live eyeball pending
> "Legacy atlas should be a square"

**Fix (2026-07-01, `13f60b27`):** `renderCompact` now uses the rectangular primitives for both shapes — a
square for Legacy 1:1 (halfW == halfH), a 2:1 rectangle for Mercator. The bounding box was already
shape-correct (`computePreviewLayout`); the old code just drew a circle inside the Legacy square box. Removed
the mercator-vs-classic branch. Staged as `TEST 2.jar`. Remaining: Peetsa live-eyeball the square.

The Legacy 1:1 world border is a square (X radius == Z radius), but the preview draws a circle for CLASSIC.
Mercator correctly draws a rectangle.
- `LatitudePlanisphereRenderer.renderCompact()`: `boolean mercator` branch at L297. CLASSIC path uses
  `fillCircle(...)` L303, `fillBandStrip(...)` L331, `drawLatitudeLine(...)` (chords) L345–349.
- Fix direction: add a CLASSIC **square** branch mirroring the MERCATOR rectangle branch — `fillRect` with
  equal width/height, `fillBandStripRect`, `drawLatitudeLineRect` — instead of falling through to the circle.
  (The circle was the old "planisphere" globe aesthetic; it no longer matches a shape-aware Atlas.)

### A3 — Latitude labels/lines "super crammed" · Pri M
> "the latitudes are super crammed in the atlas. make it look nice please."

Six labels (0°, 23.5°, 35°, 50°, 66.5°, 90°) forced into the compact preview's limited height; the spacing
rule only prevents overlap, it doesn't distribute evenly.
- `LatitudeCreateWorldScreen.java` — `computePreviewLabelYs()` L1450–1465; `minGap` collision push L1458–1463.
- Full-atlas label draw (for reference): `LatitudePlanisphereRenderer` L147–177 (`yOff = radius*deg/90`).
- Fix direction: give the atlas more vertical room and/or scale label density; distribute by degree rather than
  only de-colliding.

### A4 — Layout question: World Type/Size — left rail or right? · Pri L  (open design question, not a bug)
> "Should world type/size be on the righthand side, or the left?"

Current split is inconsistent: **World Size** is on the LEFT rail; **World Type** and **World Shape** are on the
right SETTINGS rail. Peetsa is asking whether the four world-shaping controls should be grouped together and on
which side. Needs a Peetsa design decision before any move.
- Left rail: World Name, Seed, World Size, Atlas preview (`LatitudeCreateWorldScreen` init ~L390–414).
- Settings rail: World Type L433–437, World Shape L439–444, Game Mode, Commands, Compass, Structures, Bonus
  Chest, Game Rules, HUD Studio (L445–489).

### A5 — "Re-create world" leaves the seed blank; should copy the source world's seed · Pri M
> "I chose to 'recreate world', but it still is generating a blank seed. It should copy the seed of the world to be recreated."

The bespoke create screen HAS the plumbing — `probeSetWorldInputs()` sets `seedField` from a passed seed
(`LatitudeCreateWorldScreen` L985–987) and `LatitudeWorldLauncher.beginExpedition` calls `wc.setSeed(seed)`
(L108). So the gap is in the **recreate entry point / caller**: the "Re-create" action is not passing the
source world's seed into the bespoke screen (there is no auto pre-fill on screen open). Trace the recreate
button/caller and route the source seed through `probeSetWorldInputs`.

---

## B. Loading screen

### B1 — Peak-themed loading messages clustered (3 in a row); want randomized + ~70% newer-bias · Pri M
> "I got 3 peak-related messages in a row... Make sure they are all randomized. There should be a 70% bias towards the newer messages against the old ones, but ultimately everything should be random."

Root cause found: messages **cycle sequentially** on a 4800 ms timer, and the peak splashes are adjacent in the
array, so you get them back-to-back.
- `mixin/client/LevelLoadingScreenLatitudeOverlayMixin.java` — `PHRASES[]` L43–96 (54 msgs; peak ones adjacent
  at L83–85; "featured"/newer Latitude msgs are the last 19, L78–95). Initial index `globe$pickSeedIndex()`
  L103–113. Sequential advance `globe$drawPhrase()` L354–357; `PHRASE_CYCLE_MS=4800` L115.
- Note: a prior fix changed a **70% featured-roll to 100%** (comment L106–108) to stop feature messages
  disappearing on fast loads — so restoring a 70% bias must not reintroduce that bug.
- Fix direction: replace the sequential cycle with a randomized pick + anti-repeat (no immediate repeat / no two
  same-theme in a row), and reinstate a ~70% weight toward the featured block while keeping it ultimately
  random.

---

## C. Worldgen / structures / performance

### C1 — Savanna village inside a temperate (birch) forest zone · Pri H
> "Savanna village inside a temperate forest zone."

Pipeline timing mismatch (leading hypothesis, high confidence): vanilla picks the village **variant** at
`STRUCTURE_STARTS` from the raw BiomeSource (savanna → `village_savanna`), then Latitude repaints biomes at the
later `BIOMES` phase (→ birch forest). The guard runs after the fact and its climate check misses this case.
- Remap: `mixin/ChunkGeneratorPopulateBiomesMixin.java` L188–327 (`LatitudeBiomes.pick()` at L307);
  `world/LatitudeBiomeSource.java` L103–119.
- Guard (runs too late, at `placeInChunk`): `mixin/StructureBiomeMatchGuardMixin.java` L44–68.
- Detection gap: `world/LatitudeBiomes.java` `structureClimateMismatch()` L1977–2005 — savanna check L1989–1990
  matches biome IDs containing savanna/shrubland/prairie/etc., but `minecraft:birch_forest` contains none, so
  the mismatch isn't caught.
- Fix direction: broaden the mismatch check to tag membership (not substring), or catch "savanna-variant in a
  temperate-forest biome."

### C2 — BOG village — forbid; audit ALL biomes' village allowances · Pri H (forbid) + M (audit) ⛔(audit context on Notion)
> "A BOG village. Forbid this from occurring. Also, go over all the biomes available and audit which ones are currently allowed to generate villages. See what makes sense and what doesn't."

Same remap-after-placement mechanism (bog is a Terralith biome, not vanilla; villages likely land on a
plains/flat raw biome that Latitude then repaints to bog — bog is a flat wetland so it isn't demoted).
`structureClimateMismatch()` has **no swamp/bog handler**, so it's never cancelled.
- `world/LatitudeBiomes.java`: `isFlatWetlandBiome()` L6227–6231 (bog recognized as flat wetland);
  `structureClimateMismatch()` L1977–2005 (no bog/swamp branch).
- Existing guards: `mixin/ExtremePolarVillageGuardMixin.java` L35–58 (only blocks ≥85° polar);
  `mixin/StructureBiomeMatchGuardMixin.java` L44–68.
- Two tasks: (1) add a bog/swamp forbid to the mismatch guard; (2) the **full village-biome audit** — enumerate
  every biome villages can currently land in and decide what's sensible. Peetsa referenced @Notion for prior
  context; **Notion is unauthorized this session**, so the audit's historical context could not be pulled — do
  the audit with Notion re-authorized.

### C3 — Generation lag · Pri H
> "I'm getting some generation lag. Look into it."

Worldgen performance. Prime suspects: the per-chunk biome-remap wrap and terrain cost. See prior perf work
`code-red-itty-palm-performance-20260620.md` and `worldgen-jank-earmark-20260622.md`. Needs a profiling pass on
`TEST 1` (which world size/shape, seed, and location should be captured when reproducing).

---

## D. Dev tooling

### D1 — Only a subset of `/latdev` commands restored · Pri M ⛔(authoritative list on Notion)
> "you only restored some of the `/latdev` commands. Please read over @Notion to see what was there before."

Current state: the **shippable tester subset** has 5 commands (`help`, `here`, `tpband`, `tpedge`, `probe`) in
`LatitudeDevCommands.java` L45–62 (`tpedge`+`probe` were restored 2026-07-01, commit `db4dd160`; the earlier
subset was only `help`/`here`/`tpband`). The **full dev-only set** has 20 commands in
`dev/LatitudeDevCommand.java` L66–138 but is stripped from release builds (reflection-gated, dev env only).
- The "what was there before" reference list is on **@Notion**, which is **unauthorized this session** — could
  not diff current-vs-historical. Re-authorize Notion, pull the prior `/latdev` inventory, and restore the
  tester-facing commands Peetsa expects (candidates from the dev set: `explainHere`, `seamAudit`, `regen`,
  `biomePng`, transect/pregen controls — TBD against the Notion list).

---

## E. E/W border effect

### E1 — Rename "sandstorm"→"storm"; make it climate-aware; soften the warning copy · Pri M
> "adjust the e/w border away from 'sandstorm' and change it to 'storm'. In subpolar and polar regions, instead of sand it should generate snow particles and have more of a cool grey fog that gradually gives way to whiteout conditions. Instead of 'extreme danger ahead' I think something like 'low visibility ahead' is more appropriate."

The E/W border effect is currently **hardcoded sand, uniform across every latitude band** — it does not vary by
climate (poles already have a separate `SNOWFLAKE` path for the N/S cap, which can be reused).
- Text: `client/GlobeWarningOverlay.java` L27–30 — `EW_SAND_WARN_TEMPLATE = "Sandstorms to the %s. Head %s to
  turn back."`, `EW_SAND_DANGER_TEMPLATE = "Extreme danger to the %s. Head %s immediately."`; tier select
  L113–120; direction L241–249.
- Sand particles: `GlobeModClient.java` `ewSandstormClientTick()` L250–273 (`FALLING_DUST` + `Blocks.SAND`;
  pole path uses `SNOWFLAKE` L245).
- Screen tint: `client/EwSandstormOverlayHud.java` L16–18 (tan `RGB(214,186,132)`).
- Fog: `mixin/client/FogRendererEwMixin.java` + `client/GlobeClientState.java` `computeEwFogEnd()` L337–347.
- Tiering: `util/LatitudeMath.java` `hazardStageIndexEW()` L136–142; `GlobeClientState.computeWarningState()`
  L175–231.
- Fix direction: (1) rename sand→storm across strings/identifiers; (2) branch the particle/tint/fog by climate
  band — subpolar/polar → snow particles + cool grey fog escalating to whiteout, temperate/tropical → keep a
  (renamed) dust/haze storm; (3) reword the tier-2 line to "low visibility ahead" (region-appropriate) rather
  than "extreme danger."

---

## Cross-references
- Feature these were tested against: `atlas-worldshape-longitude-20260701.md`.
- Resume pointer: `current-state-handoff-20260701.md`.
- Two items (**C2 audit**, **D1 /latdev list**) are blocked on **Notion re-authorization** — not doable in the
  session that recorded this.
