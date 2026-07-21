# Phase 5 B-9: Glacial Caves & Crevasses — design (2026-07-19)

STATUS: SWEPT — adversarial design sweep 2026-07-19 returned ACCEPT-WITH-FIXES (1 blocker +
5 required fixes, all amended below; the blocker's architect decision = mechanism (b), see
"The attachment seam"). CLEARED FOR P1 BUILD.
OWNER DIRECTION (Peetsa 2026-07-19): "get started on glacial caves/crevasses" — B-10 polar
gear is PARKED (he co-designs it when back). The S-section pattern from the B-7 doc applies
(owner flight findings append here as they arrive).

## The owner's vision (verbatim bank, collected across flights)

- TEST 110: polar "caverns are giant voids" — future crevasses should be "narrow and winding
  ice labyrinths." (B-7 doc ~L978: locked verbatim.)
- "could glacial caves have a small chance of spawning a shipwreck amidst the frozen ice?"
  (B-8 doc: B-9 idea locked — V1 polar wrecks is a SEPARATE queued pass; V2 entombed wrecks
  belongs to B-9's cave story.)
- Glacial-cave semi-ice lakes WITH FISH (the freeze law deliberately leaves deep water alone —
  real mechanism quoted below, sweep finding 5).
- B-7 doc ~L717: polar underground is "surface biome (plain caves) until B-9 Glacial Caves
  takes the slot" — the underground biome identity is reserved for this phase.

## Ground truth (recon + adversarial sweep, 2026-07-19, code/bytecode-verified)

1. **Polar Barrens is a real data biome** selected by the populate mixin at |lat| >= 82
   (fray-edged), full-column. BUT its JSON `carvers` list is DEAD WIRING for attachment
   purposes — see 3.
2. **Pipeline order (26.2): noise → surface → carvers → features.** The glacier body
   (buildSurface TAIL) is built FIRST; carvers then cut THROUGH the finished ice (the glacier
   mixin's own javadoc says exactly this). Ice-walled carved space therefore depends on the
   carver's REQUIRED `replaceable` tag permitting glacier blocks — and
   `#minecraft:overworld_carver_replaceables` contains `packed_ice` and `#minecraft:snow`
   (snow_block, powder_snow, snow layer) but NOT plain `minecraft:ice`. See "replaceable
   decision" below — the omission is load-bearing and we KEEP it.
3. **`applyCarvers` resolves each seed-chunk's carver list from the RAW `biomeSource` FIELD**
   (`getNoiseBiome` at the chunk-min-corner quart, quart-Y 0) — not the wrapped
   `getBiomeSource()` getter, not the populate-time resolver, not chunk biome storage. The
   raw multi_noise source can never emit a `globe:` biome, so carvers attached to
   `globe:polar_barrens` in JSON would NEVER fire (sweep BLOCKER). Attachment must be
   code-side, in the seam we already own.
4. **`NoiseChunkGeneratorCarveMixin` blanket-cancels carving ONLY on
   `stable(globe:overworld)` worlds (the legacy 15000-radius settings KEY) at |z| >= 14500.**
   It is dormant on Regular-Wide (key mismatch AND radius), and does NOT fire on Massive
   (20000, different key) even though |z| 14500 = 65.25 deg is reachable there. THE IN-CAP
   LEG MUST BE PRESERVED VERBATIM — settings-key-scoped, `stable(globe:overworld) && |z| >=
   14500` — NOT extended to the 6-key any-globe gate, or Massive worlds silently lose
   carving poleward of 65 deg (sweep finding 6).
5. **The giant voids are NOISE caves** (cheese/spaghetti in the delegated router DFs), not
   carvers — no data knob reaches them (amplitude-wrapper problem class). P2.
6. **Polar terrain reality (atlas 16-bit heightmaps, radius 10000, two runs):** land surface
   at 82-90 deg has median Y ≈ 71-78, p95 ≈ 115-126, and ~50% of the band is SEA (<= 63).
   The S20/S21 flight ground at Y124 was p95-class plateau, not typical. All Y bands below
   are anchored to this distribution.
7. **Freeze-law facts (real constants, replacing the misquote):**
   `LAND_WATER_FREEZE_DEPTH_BLOCKS = 40` is the WORLDGEN solid-freeze floor below the water
   surface; `ROOFED_FREEZE_REACH_BLOCKS = 16` is the TICK-path roof-descent reach below the
   MOTION_BLOCKING top. What protects carver-placed pools at gen time is ORDERING (the
   S14(b) freeze pass runs at the surface stage; carving runs AFTER it, so carver water is
   never seen by worldgen freeze at all). At tick time: pools within 16 blocks of the roof
   may grow a surface SKIN (wanted — that IS the "semi-ice" look); deeper pools stay liquid
   (sources; the settled sweep claims only flowing water; shouldFreeze tests only
   heightmap-top columns).

## P1 scope — CREVASSES + GLACIAL TUNNELS (carver pass, new chunks only)

Two configured carvers using vanilla carver TYPES with our data configs (vanilla-first: no
custom carver code), under `data/globe/worldgen/configured_carver/` (path sweep-verified),
wrapper form `{"type": ..., "config": {...}}`. ALL required fields present — a parse failure
breaks world creation: base `probability, y, yScale, lava_level, replaceable`; cave adds
`horizontal_radius_multiplier, vertical_radius_multiplier, floor_level`; canyon adds
`vertical_rotation, shape{distance_factor, thickness, width_smoothness,
horizontal_radius_factor, vertical_radius_default_factor, vertical_radius_center_factor}`.

### `globe:crevasse` (canyon type)
Narrow, deep, winding open-top slots in the glacier. Ice walls through the glacier body
(replaceable covers packed_ice + snow family), stone below the sole — honest bedrock crevasse.
- probability ~0.14 (owner dial; calibration context: vanilla canyon is 0.01, cave 0.15 —
  this is deliberately a fissured-glacier density, tuned at first flight)
- y: absolute uniform ~66..112 (anchored to the REAL surface distribution: catches median
  71-78 through p95 plateaus; starts above local terrain simply bite partway — variance)
- cut depth ~20-40 → bottoms Y ~26..92. BOTTOMS BELOW SEA LEVEL 63 WILL POND via aquifers
  (aquifers are ON in all our noise settings). THIS IS WANTED: pooled crevasse floors are
  the fish-lake seed; within 16 of the roof they may grow the semi-ice skin (ground truth 7).
- lava_level: -56 (below everything our Y band can reach — no lava windows in a glacier)
- replaceable: `#minecraft:overworld_carver_replaceables`

### `globe:glacial_tunnels` (cave type)
The labyrinth. Cave-type carver with tightened radii (horizontal ~0.7x, vertical ~0.5x) so
tunnels read as narrow winding corridors, not rooms; cave-carver branching supplies the maze.
- probability ~0.12, y: uniform ~30..90 (glacier body under high terrain + upper stone under
  median terrain; dips below 63 pond via aquifer — same wanted outcome)
- lava_level -56; replaceable `#minecraft:overworld_carver_replaceables`; floor_level per
  vanilla cave convention

### The replaceable decision (sweep finding 3 — DECIDED)
We use `#minecraft:overworld_carver_replaceables` AS-IS, i.e. plain `minecraft:ice` is NOT
carvable. Consequences, all accepted as features:
- The frozen SEA surface skin (plain ice over liquid) is STRUCTURALLY IMMUNE to arcs that
  stray over sea columns — the sacred under-ice fiction cannot be breached by B-9.
- A crevasse slicing a frozen river/lake leaves the 1-block ice skin spanning the slot: an
  ICE BRIDGE over the crevasse. Real glaciology; keep. Frozen cascades likewise survive.

### The attachment seam (the BLOCKER fix — mechanism (b), one mixin)
`NoiseChunkGeneratorCarveMixin` grows a single order-preserving carver-list filter at the
ONE `getCarvers()` call site inside `applyCarvers` (site count sweep-verified; MixinExtras
`@WrapOperation` available and bundled):
1. LEGACY LEG, VERBATIM SEMANTICS: on `stable(globe:overworld)` && seed-chunk |z| >= 14500 →
   strip `minecraft:*` entries. Block-identical to today's blanket cancel (empty-list
   equivalence verified at block level; the only difference is an empty serialized
   carving_mask in mid-generation proto-chunk NBT — invisible at FULL status, accepted).
   The HEAD cancel is then retired.
2. B-9 LEG: when `latitude.glacialCavesV1` is ON and the seed chunk is BARRENS-BAND LAND →
   APPEND the `globe:crevasse` + `globe:glacial_tunnels` holders (registry lookup via the
   region's registry access) AFTER the raw list. Appending preserves the per-carver seed
   (`seed + list-index`): vanilla carvers keep indices 0..n-1 and their exact streams; flag
   OFF appends nothing and is byte-identical.
   - Barrens-band = the SAME latitude + fray decision as the biome/glacier
     (PolarBarrensBand + the existing fray noise — one shared decision, Art VI clean),
     sampled ONCE per seed chunk at a fixed deterministic position (chunk min corner, the
     same position vanilla's carverBiome memoizes).
   - LAND = the sea-column exclusion must be EXPLICIT (sweep findings 1+9). Seed-chunk
     protochunks may be pre-noise (no heightmaps), so the check must not touch blocks.
     Dev picks the cheapest deterministic worldgen-safe probe; candidate: raw biomeSource
     sample at a surface-band quart (~Y63) → ocean-family tag => skip. The replaceable
     decision above is the structural backstop even where the probe misses.
3. The biome JSON is NOT changed — its carver list stays the inherited vanilla trio
   (`minecraft:cave`, `cave_extra_underground` [exists in 26.2, sweep-verified], `canyon`),
   and stays dead wiring for globe carvers. A comment in the mixin records that attachment
   is code-side BY NECESSITY (ground truth 3).
- Bleed: one biome/band sample per seed chunk + the -8..8 seed-chunk arc reach = +/-128
  blocks ≈ +/-1.15 deg at radius 10000 — same order as the 82→84 fray itself. Arcs can
  reach ~1 deg equatorward of the barrens edge (bare-stone slots where the glacier thins).
  Accepted per the B-8 fray philosophy; flagged as owner taste at first flight.
- Flag: `latitude.glacialCavesV1`, static-final sysprop per the LatitudeV2Flags pattern
  (class-init precedes worldgen threads — ten flags already read in worldgen mixins), DEFAULT
  OFF; branch-local flight staging flips ON with the standing REVISIT BEFORE MERGE marker.

## Laws that bind this pass
- **Byte-identity, honestly scoped (sweep finding 8): gate-1 CANNOT see this seam** — the
  atlas never executes applyCarvers. Flag-off identity rests on: (a) the pure filter unit
  tests (legacy leg strips minecraft:* only under the legacy key+radius; B-9 leg appends
  nothing when off; order preservation), (b) the standing gate-1 re-proof (which DOES cover
  the touched file's class-load side + everything else), (c) the SELF-FLY as the
  end-to-end carve-exercising proof. Stated plainly: unit tests + live flight carry the
  carver wiring; gate-1 carries the rest.
- **New chunks only** (say so in the flight card).
- **Sacred exemptions stand structurally**: ocean/frozen-sea untouched (replaceable omits
  plain ice; land probe skips sea seed chunks); deep pools stay liquid by ordering + the
  16-block tick reach cap (ground truth 7).
- **Vanilla-first**: vanilla carver types, config-only data.
- **Art VI**: placement randomness = vanilla's own seeded carver streams; band/fray decision
  reuses the existing shared noise; no new noise fields.

## P2 bank (design-sketched, NOT in P1)
- **Void taming**: DF-router work (fill bias on cheese entrances poleward of 82) — amplitude-
  wrapper mechanism class, HIGH RISK, own pass, owner at keyboard.
- **`globe:glacial_caves` underground biome** (claims the reserved slot): depth-conditioned
  swap below barrens columns in the populate mixin (per-quartY pick exists) — cave fog mood,
  ambience, mob control, biome-scoped decoration.
- **Ice dressing**: blue-ice accents, powder-snow floor pockets, icicle forms; creative-
  director review after P1 flies.
- **Fish lakes proper**: guaranteed pockets + fish spawns in tunnel pools.
- **Entombed wrecks (V2)**: rides polar wrecks v1's template pool.

## Proof plan (P1)
1. Suite green: existing + new pure filter tests (strip/append/order/flag/key-scoping) +
   carver JSON schema self-check (all required fields present per the verified codec).
2. Boot-load gate: world creation on a dev client/headless run IS the datapack parse gate.
3. Gate-1 byte-identity re-proof (flag-off atlas vs standing baseline) — scope per Laws.
4. ORCHESTRATOR SELF-FLIES first (standing law): fresh world, flag on, 84-88 deg;
   photograph (a) an open-top ice-walled crevasse slot, (b) a narrow tunnel section,
   (c) a ponded pool in a deep cut (liquid or semi-ice skin), (d) if found: an ice bridge
   spanning a slit across a frozen river. Then stage TEST 113.

## Open questions for Peetsa (parked, non-blocking for P1)
1. Crevasse density taste (P1 ships 0.14 vs vanilla canyon 0.01 — a fissured glacier).
2. Bare-stone slots bleeding ~1 deg equatorward of the barrens edge: keep as fray, or
   tighten the band gate?
3. Should crevasses ever breach INTO the big voids (dramatic reveals), or should P2 void
   taming close voids entirely?
4. Wreck-in-ice timing: with wrecks v1, or its own later beat?

## P1 BUILT + SELF-FLOWN (2026-07-19, same day)

Built by one dev crew (03c2c62d), swept ACCEPT-WITH-FIXES (one REQUIRED-FIX applied: the
legacy strip now removes EVERYTHING non-globe — cancel-identity extends to third-party
datapack carvers; 3 tests updated), suite 776/0/0. Gate-1 flag-off atlas re-proof PASS
(19 data files byte-identical to baseline 20260717-193525, seed 20260714; only durationMs
differs — also covers the S21 veg 72/80 move, which was awaiting its re-proof).

ORCHESTRATOR SELF-FLIGHT (fresh world "New World" cheats-on, Regular Wide, spectator,
84-85 N): crevasse slots VERIFIED cutting the glacier surface — winding open-top cracks
with terraced ice walls (top-down + rim frames); a ponded crevasse floor VERIFIED with
water channel + freeze-skin patches claiming its surface (the semi-ice look, exactly the
ordering + 16-block-reach prediction); slot cutting through a snowy hill exposing dirt
strata (canyon arc, not noise cave). HONEST BASELINE frame: the giant noise voids still
gape under the glacier sole (ice-ceiling cavern with lava at ~Y50) — untouched by P1,
the P2 void-taming target, as designed. Glacial tunnels ride the same proven append seam
(both holders resolve or neither; crevasse firing proves the seam) — distinct tunnel
portrait deferred to owner flight. Lane notes: the bespoke create screen defaults
commands OFF (Rules tab toggle needed — LAN Allow-Commands did NOT grant host op this
session); world names: "B9 Crevasse Proof" (dead, no commands) + "New World" (the proof
world). TEST 113 staged: SHA 2fae35f8…, markers GlacialCarverLaw / glacialCavesV1 /
both carver JSONs in jar.

## S-BANK (Peetsa 2026-07-19, mid-TEST-114-round): POLAR LIFE & PERIL — next round's slate

Owner verbatim, on the caves round-1 plan: "I love this!" Then four new asks (locked verbatim):
- "collapsing snow underfoot? like a trap, where you walk over the snow and it collapses into
  a crevasse... a way to make it not ultra annoying and unfair" (over land AND inside caves)
- "maybe we can add avalanches?"
- "polar bears wandering the polar barrens, and they aggro?... can't be too annoying, just a
  lurking risk"
- "snow foxes to the polar barrens — foxes tinted white. what might they eat?"

DESIGN ANSWERS (banked for the next round, NOT in the TEST 114 diff — round scope is frozen
mid-sweep):
1. COLLAPSING SNOW BRIDGES, V1 = powder-snow roofed crevasses (worldgen-only): where the
   crevasse carver cuts the surface, occasionally roof the slot's top with powder_snow flush
   with the snowfield. Vanilla physics IS the trap (entities sink through powder snow), the
   subtle texture difference is the LEARNABLE TELL (fairness: crevasse fields are readable,
   like real ones), LEATHER BOOTS already walk on powder snow in vanilla = the gear counter
   is free and ties into B-10 cold gear, and roofed crevasses get a guaranteed snow-cushion
   floor (the fall costs position/warmth, rarely the run). Cave interior version = the same
   powder plugs over lower tunnel levels. V2 = custom cracking chain-collapse with audio
   telegraph (drama pass, later).
2. AVALANCHES: PARKED as its own pass. The cheap versions (falling-block spam / scripted
   particle wall) would cheapen the fantasy; collapsing snow delivers "the snow itself is
   dangerous" at a tenth the cost first. Revisit after V1 lands.
3. POLAR BEARS: add to barrens/glacial spawn lists at LOW weight near fish-lake/coastal
   conditions + a small latitude-conditioned "hungry bear" behavior law: in the food-scarce
   Barrens a bear's neutral-to-hostile radius widens (~16 blocks) with the vanilla warning
   roar first — lurking risk, avoidable, low density, drops fish. (A vanilla bear was already
   sighted on a 79S floe in the TEST 113 video — the Barrens presence + real threat is the new
   part.)
4. SNOW FOXES: vanilla foxes ALREADY have the white arctic variant, auto-selected in snowy
   biomes — pure spawn-list data. What they eat: the Barrens' own RABBITS (already in the B-8
   spawn list — a real predator-prey loop), scavenged fish near the lakes and bear kills, and
   vanilla's fox dive-into-snow pounce reads exactly as the real lemming-hunt-under-the-snow
   behavior, for free.

## TEST 114 ROUND SHIPPED + SELF-FLOWN (2026-07-20 overnight)

Full round from the owner's TEST 113 video review (10-agent analysis fleet -> 3 dev crews ->
combined sweep ACCEPT-WITH-FIXES, all applied; suite 818/0/0; gate-1 byte-identity PASS).
SELF-FLIGHT (fresh world "T114 Proof", commands via Rules tab): world CREATION passed the
datapack parse gate (12 new JSONs + programmatic NBT); /locate biome globe:polar_barrens
resolves (344 blocks, real biome); WATER v6 LIVE at the 86N rig — hunter zipper (2-14/window)
+ spread-converter (spreadFroze 4-6/window) froze wide pours into complete ice sheets in
~70s with NO respread (the ratchet is dead); storm whisper renders at the NEW bottom anchor;
polar dusk-red horizon band during night (bandReach gloom working); blue-ice STRATA exposed
in cliff faces; glow lichen punctuating dark caverns + inherited vanilla mineshaft
(underground-stays-alive honored). OPEN FINDING (honest): the settled-sweep BACKSTOP never
claimed live (sweptSettled/sweptFroze = 0 all flight; heartbeat prints, unit tests green) —
isolated pours on virgin snow away from ANY ice fall back to vanilla source-skin cadence
(~minutes) then cascade; crevasse-country pours (the owner's case) are fully covered by
hunter+converter since ice adjacency is everywhere there. NEEDS its own instrumented session
before any "sweep works" claim. Unverified visually (code+sweep-verified only): spawn-in big
title, glint whiteness (night flight), night monster spawns, cache placement. TEST 114
staged: SHA e7bfb8d2…, markers tickFrontFreezes / FlowingFluidSpreadConvertMixin /
glacial_caves.json / frozen_cache.json.

## OVERNIGHT FENCEPOST FIX + THE SWEEP HUNT CONTINUES (2026-07-20 ~01:00, autonomous)

Inspection (airtight, 6 suspects examined) found the settled sweep's depth gate rejected every
ONE-DEEP landed sheet: OCEAN_FLOOR heightmap sits one above the topmost solid, so bed-resting
water sat exactly AT the exclusive worldgen floor. FIXED: tickLandWaterFreezeFloorY (bed term
oceanFloorY-1) in the three tick consumers only; worldgen floor verbatim (glacier byte-identity);
deep-cap term untouched (B-9 reservoir). Suite 819/0/0 (pad fencepost pinned), pushed f19bb9ba,
TEST 115 staged (SHA see registry). RE-FLY HONESTY: hunter+converter cascades fired again
(load-burst 62 claims; fresh pad edge-falls seeded by natural blue-ice strata) but sweptSettled
STAYED 0 — the sweep pipeline still dies BEFORE the settled probe on some other pre-probe
condition the inspection did not enumerate. The per-condition-counter instrumentation session
(the pending chip) remains the right next step; my clean-pad live tests kept getting contaminated
by edge-falls finding natural ice (which is also why real-world pours freeze fine — ice
adjacency is everywhere in the barrens). Player-visible impact of the open gap: none observed
for falls/pours near any ice; isolated 1-deep sheets on virgin snow rely on vanilla source
cadence then cascade.

## SETTLED-SWEEP ZERO-CLAIMS: DIAGNOSED AND CLOSED (2026-07-20, instrumented headless session)

The queued per-condition-counter chip ran, fully headless (no live flight needed). Verdict:
**there is no second pre-probe rejector.** The TEST-114 zero WAS the freeze-floor fencepost
(already fixed overnight, `f19bb9ba`); the TEST-115 zero was the hunter/converter RACE on
ice-contaminated pads, i.e. the expected shape of a working pipeline, not a rejection. The
sweep, as shipped, claims settled isolated sheets — recorder-proven on a COPY OF THE OWNER'S
OWN TEST 113 WORLD.

**Method** (all TEMP-DIAG, committed once for the record as `a7110706`, then removed):
- Per-gate rejection counters in the sweep driver — one `[LAT][SWEEPPROBE]` line per recorder
  window: `cols/front/surfSrc/roofSrc/dry/notLanded/barrensOff/ocean/floor/probed/pendF/pendW`
  plus first-reject coordinate details — behind the existing `-Dlatitude.debugFreeze` gate.
- A player-free headless rig (`gradle runFreezeProof`): boots the ordinary headless server,
  builds the exact live scene at 86 deg N — a walled virgin-snow basin with ONE source, plus a
  bare source poured on the NATURAL surface (free runoff) — then censuses
  src/flow/ice/pending-per-key every 5 s for 2 min and writes
  `run-headless/latdev/freeze-sweep-proof.txt`. Playerless block-ticking verified against 26.2
  bytecode (`tickChunk` rides `forEachBlockTickingChunk` → entity-ticking range, so a
  `setChunkForced` 3x3 ring ticks with no player; `pause-when-empty-seconds=0` disables the
  empty-server pause). Also bytecode-verified en route: `FlowingFluid.tick` does NOT reschedule
  at equilibrium (the S20 settled premise holds in 26.2 vanilla).

**The three runs.**
1. Scratch vanilla world, border resized to globe (10000): basin froze in the first active
   window — `flowTicks=67 hunterFroze=36 sweptSettled=3 sweptFroze=3`; the settled probe
   correctly HELD during active spread (`pendF=23`) and passed at rest. Sweep works.
2. COPY of the owner's TEST 113 world (MERCATOR 2:1, zRadius=10000, rig at z=9556 = 86.004N,
   ice_spikes country): first window `flowTicks=198 passedFalling=8 hunterFroze=91
   sweptSettled=5 sweptFroze=5 spreadFroze=1`; SWEEPPROBE `floor=0 front=0 ocean=0
   notLanded=0, probed=46, pendF=41`. Basin AND natural pour fully ice within seconds;
   sources correctly last (S21d). **`sweptFroze > 0` on the owner's own world — the proof the
   chip asked for.**
3. Same world, the sweep's floor call TEMP-reverted to the pre-TEST-115 worldgen floor (the
   `62bfc` snapshot behaviour): the recorder NAMES the rejector — `floor=~244/window,
   probed=0, sweptFroze=0` for 22 consecutive windows, basin liquid the entire 2 minutes,
   detail `floorAt=...,base147,ws148,of147`: **base == oceanFloor == the exclusive floor.**
   The fencepost, reproduced and attributed. (The natural pour froze anyway via
   converter+adjacent ice, `spreadFroze=4` — exactly the live "contaminated pads freeze fine"
   pattern.)

**Why TEST 115 still read zero (the race arithmetic).** Every freeze's neighbour updates
schedule fresh fluid ticks, so the hunter re-checks contaminated cells at flow-tick latency
(~5 ticks) while the sweep's round-robin visits a column every 32 ticks. Wherever ANY ice is
adjacent — every TEST-115 pad, per the re-fly notes — the hunter/converter claim the sheet
first (run 2: hunter 91 vs sweep 5 in one window). A live `sweptSettled=0` in an ice-adjacent
session is expected, not pathological. The sweep's constituency is the truly isolated sheet,
and that case is proven working above.

**Cleanup.** All TEMP-DIAG pieces (PolarSweepProbe, mixin call sites, FreezeSweepProofHarness,
runner hook, `freezeProof` run config, the world-t113 copy, server.properties edits) removed in
the commit that carries this section; shipped code is byte-identical to `f19bb9ba` — the round
changes ZERO shipped behaviour, so worldgen byte-identity holds trivially. Suite 819/0/0 at
both commits. Raw run logs kept locally in `run-headless/latdev/freeze-sweep-run*.log`
(untracked); resurrect the tooling any time from `a7110706`. The next live flight needs no
sweep hunt — if instrumentation is ever wanted live anyway, cherry-pick `a7110706` onto the
staging branch and fly with `-Dlatitude.debugFreeze=true`.

## S23 (Peetsa 2026-07-20 morning, TEST 115 flight + video in ~/CascadeProjects/Proofs)

OWNER VERBATIM: "Freezing water is awesome now! BUT the source water always stays liquid.
Can we make the source freeze after all the other children of the source freeze? So that it
happens last?" + collapsing-snow refinement: "the falling should trigger by player walking
over" (the powder floor pockets read as PRE-fallen snow — the trap must be the intact ROOF)
+ horizon fix confirmed ("the horizon is fixed now, so that's great") + glints "a lot
better" + NEW GLINT-AS-CLOCK idea: during polar night the day-bright landscape confuses;
proposal — snow/ice GLINT during clock-DAY in polar night (and during clock-day in midnight
sun) as the diegetic time-of-day tell.

S23 BUILT SAME MORNING (bf9af829, TEST 116 staged): the driver now CLAIMS an eligible open
source on its 32-tick cadence strictly AFTER the six-neighbour flowing scan clears (S21(d)
LAST law) + settled probe + shared ocean/floor eligibility. Sea skin + B-9 reservoir
untouched; barrens-off byte-identical. sourceFroze instrument channel added. LIVE-PROOF
NOTE: the owner was at the keyboard (my control lane yields to his session) — his own pour
is the verification round; recorder channel ready if needed. GLINT-AS-CLOCK: design
conversation open (owner asked for thoughts) — proposal drafted in the reply, build after
his nod. COLLAPSE: V1 roof design already walk-triggered (powder roof, sink-through);
refinement banked — open crevasse floors should NOT carry pre-fallen powder pockets (move
pocket dressing to tunnels only) so the trap fiction stays clean.

## S24 (Peetsa 2026-07-20, same morning): GLINT CLOCK YES + THE ICE GOES DOWN

OWNER: "Yes to the glint clock. And please put the darkening landscape on the docket for
later, not for this pass." + glacial caves: "they still don't look good, and the ice doesn't
extend down very far. It just becomes regular cave." VIDEO CONFIRMED (frames f0073 vs
f0121/f0125): the crevasse through the GLACIER BODY reads beautifully (blue-ice walls,
snow flecks) — then the sole ends and the caves below are plain stone/dirt caverns under a
HUD label proudly reading "Polar · Glacial Caves". The identity layer shipped; the rock
matrix didn't. DOCKET (parked, owner-directed): polar-night LANDSCAPE darkening (the
lightmap seam) — explicitly NOT this pass.

S24 build slate: (1) PERMAFROST STRATUM — noise-graded packed-ice veining below the glacier
sole (existing wobble noise, Art VI; partial density so ore homes survive); (2) glacial ice
BLOBS (ore-type packed_ice/blue_ice features) through the whole glacial_caves band so deep
caverns read glacial in every wall; (3) frost floor carpet (snow layers + ice patches,
underground placement); (4) icicle density raised (not visible in the owner's frames);
(5) powder drift pockets OUT of open crevasse floors (S23 bank — trap fiction); (6) GLINT
CLOCK — glintWeight follows the sun's clock everywhere (noon peak, clock-night zero), band
extended equatorward to the solar onset during polar night / midnight sun, above-82
snowfall crossfade unchanged.

## S25 (Peetsa 2026-07-20, TEST 117 flight): nine findings + the Snowfields opener

OWNER VERBATIM (locked): (1) "I still can't find any crevasses. Can we add a lat dev locate
command?" (2) "I don't see any polar bears or Arctic foxes in polar storm country" (the
banked Life & Peril fauna — build now). (3) "freezing water is a bit inconsistent... should
at least start from eighty degrees, if not a little sooner depending on what you think."
(4) spawn zone Polar should be "random between the boundary of polar and subpolar and...
maybe seventy nine. Because it's quite a long distance from beginning of polar to polar
storm country, but what do you think?" (5) glacial caves "should extend down further into
the sub y zero zone... it still seems like it ends pretty abruptly." (6) damage in water:
"great. That's fine now." (7) "The warning message for your wounds are frozen inside the
glacial caves is glitchy, it'll re-trigger." (8) slushy cave water — "very small ice blocks
clustered together in the water to really show that it's cold... do you have any ideas?"
(9) approach to polar storm country "just feels like dirt with snow on top. It's very
uninspiring... let's plan on doing something with that... another custom biome called,
like, the snowfields."

S25 BUILD SLATE: A. /latdev locateCrevasse (deterministic carver-start prediction — the
carver's per-chunk seeded roll is replayable without loading chunks). B. Polar bears (low
weight, hungry-bear widened aggro ~16 w/ warning roar, coastal/fish-lake bias) + arctic
foxes (vanilla white variant auto-selects in snowy biomes; they hunt the barrens rabbits)
in polar_barrens + glacial_caves spawns. C. Tick freeze front 82 -> 80 (the polar-country
rung: ambient snowfall onset, villages end, strays-only — one coherent line; fray width
preserved; ocean + ALL worldgen paths keep 85). D. Zone-aware Polar spawn band widened
66.5-74 -> uniform 66.5-79 (owner call; wood scarcity above ~76 accepted as expedition
fantasy — create screen already warns). E. glacial_caves swap + ice blobs + dressing
extended below Y0 to world bottom (deep_dark exemption stands). F. PolarWounds re-trigger
hysteresis (the frozen-wounds warning must fire once per lock, not oscillate underground).
G. SLUSH v1: worldgen ice-floe speckles dotting cave pool surfaces (MC-scale "small ice
chunks") + client frost-mote particles drifting over glacial_caves water. H. SNOWFIELDS
(the 74/75-82 approach band): DESIGN ROUND ONLY — concept pitch to the owner first.

## S25c (Peetsa 2026-07-20, same day): survey intent-vs-realized + trap/roster/pillar follow-ups

OWNER: (1) "I used the survey latdev command and it recorded that I was in an ocean basin
away from land when I was actually just in the middle of snowy plains" (78N screenshot) —
ROOT CAUSE: the survey feeds the narrator GeoAuthority.isOceanIntent; on geoV2-armed worlds
intent vs realized terrain diverge (THE standing open calibration finding — the C-2
veto/over-flooding family). Fix in flight: "where you are" facts now derive from the
REALIZED column (actual water/biome), geo intent keeps only the deep-story lines; the
nearest-land line drops when intent=ocean but realized=land. NOTE: the owner's live session
runs geoV2/climateV2/consumer/terrain/boundary ARMED — intent-vs-realized divergence will
keep surfacing until the calibration pass; the survey now stops amplifying it. (2) Traps:
owner hunted for the powder-roof crevasse traps — they were BANKED, never shipped
(orchestrator communication miss, logged); Crew 4 builds them now. (3) Glacial-caves
roster (owner: "should be strays and... recommendations?"): ACCEPTED ROSTER = stray 85 /
skeleton 15, nothing warm-blooded; surface rules untouched. (4) Pour-pillar fix: hunter +
spread-converter gain the fluid-LEVEL GRACE (claim only level <= 5) so pours spread before
the freeze closes in — the owner's own TEST 110 lifecycle model, now literal.

## S26 (Peetsa 2026-07-20): MUSIC FADES OUT AT THE DAMAGE LINE

OWNER VERBATIM: "let's have music fade completely out by the first latitude where damage
begins to happen. So only the sound of the wind is present. Music can resume inside caves,
but you must have it fade in/out." Design: the music volume factor eases 1 -> 0 across an
approach band ending at FROSTBITE_ONSET_DEG (85, the first damage rung); at/poleward of 85
the exposed surface is music-silent (wind only). SHELTER/CAVES restore it: the factor
returns to 1 when sheltered (the standing B-7 skylight-shelter rule / underground), with
smooth eased fade both directions (~4 s), never a hard cut. Client-only (a MUSIC-source
volume multiplier seam), user sound options untouched, flag family polePassage/barrens.

## S27 (Peetsa 2026-07-20, TEST 118 flight): the live-proof reckoning + sparkle/midnight-sun

OWNER VERBATIM: caves "still broken, populated by all your usual monster subjects"; glacial
caves "still only go a certain depth before it just turns to stone"; "No icicles"; locate
crevasse "teleports me somewhere, but there's no crevasse to be found"; "No falling through
powder at all"; sparkle: NOT happy with the firework spark — "go back to the amethyst
sparkle, but instead desaturate it so it's not purple"; midnight sun: "it still becomes a
night sky at midnight with the sun out... should be a true midnight sun where the sun only
sort of goes into a twilight dusky state."

ORCHESTRATOR RECKONING (logged as law): icicles have failed to appear in TWO owner flights
and were NEVER live-verified by the orchestrator; blobs/traps/locator likewise shipped on
schema-verification only. The live-instrument law extends: NO worldgen feature ships again
without an orchestrator self-fly on FRESH chunks. Standing hypothesis to TEST FIRST: the
stale-chunk trap (his world's explored areas predate the features; the locator predicts
current-jar generation which old chunks contradict) — but each feature must be proven to
fire in fresh chunks before any stale-chunk explanation is offered. S27 slate: (1) self-fly
diagnostic (fresh world: icicles/blobs/traps/locator/roster per-feature verdicts), (2) fix
what the flight proves broken, (3) globe frost-glint particle — the amethyst-style glow
sparkle shape with a DESATURATED custom color (custom client particle; WAX_OFF's tint is
hardcoded), (4) MIDNIGHT SUN TWILIGHT FLOOR — during the midnight-sun band the sky never
darkens past dusk (the clock-driven night dome is the bug; the sun is up).

## S27 DIAGNOSTIC FLIGHT (orchestrator, 2026-07-20 ~13:00, fresh world, INCOMPLETE — honest record)

Lane obstacles burned most of the flight: a recurring Xbox-profile dialog ate every chat
open (transient dev-auth; cleared by client restart) and /latdev locateCrevasse is NOT
REGISTERED in the dev-env command tree (the dev/shippable /latdev split — Crew 2's handoff
note; the owner's TEST jar has it, the dev lane cannot test it. FINDING: unify the trees).
VERIFIED at 83-85S on fresh chunks: (1) NO crevasse slots visible across multiple wide
surface views at 83S and along 85S; (2) at 85S/7E the frozen SEA + spike fields render
correctly; (3) UNDERGROUND AT 83S Y35: a COLOSSAL sky-breached noise VOID (open sky + sea
horizon visible from Y35, giant bare-stone arches, zero glacial identity) — the noise-void
problem is WORSE than believed and is the dominant underground experience in fray country.
NOT VERIFIED (ran out of lane): icicles, ice blobs, traps, roster, locator accuracy.
STRUCTURAL DIAGNOSIS consolidating the owner's 3 rounds of cave complaints: everything
glacial is fray-gated at 82-84 — exactly the accessible band where he (and I) always test —
so the reliable glacial experience only exists 84+, patchy below, and the giant voids drown
what dressing there is. DECISIONS THIS FORCES (proposed): (a) the underground identity
(caves swap + carvers + dressing) goes SOLID from 82 (surface biome fray stays, for looks);
(b) VOID-TAMING un-parks — it is the experience-killer, needs its own pass with the owner
at the keyboard per the design law; (c) feature live-verification moves to the owner's lane
(his locator works) with a short verification script, until the dev-tree split is fixed.

## S29 (Peetsa 2026-07-20 night, TEST 120 flight)

OWNER VERBATIM: "None of this is working. Locate crevasse and teleport just puts me in the
same spot that I was in and there is no falling through the snow down into a deep crevasse.
To make it easier just for dev, can you turn on a simple color filter for the trap
crevasses -- maybe typing a command causes them to glow green?" + "Can we ramp up the wind
noise a little bit earlier? Maybe 83? And just a little more intense at 90 (a little)." +
"Frost glint looks GREAT, lock that down."

ORCHESTRATOR DIAGNOSIS (code-read, not yet live-verified): locateGlacialCarver predicts a
carver's SEEDED START CHUNK and tpxz teleports to the SURFACE at that exact column. The
crevasse carver's actual carved arc can run up to CARVER_ARC_REACH_CHUNKS (8) chunks from
the start and, per the carver's own Y range design note, frequently does not breach the
surface at the start column itself -- the tool prints "walk/dig the area if not at the
marker" but a player has no way to act on that blind. This is a REAL UX/mechanism gap, not
necessarily a math bug (findNearest's spiral search read correct on inspection). ACCEPTED
LAW EXTENSION: this is now the Nth round shipped on locate/traps/icicles without a live
verification landing successfully -- no more code-derived fixes ship without the
orchestrator watching them work first via the owner's OWN jar-launch lane (dev client
cannot even register these commands -- the dev/shippable /latdev split makes this the ONLY
route). LOCATE/MARK COMMAND (the owner's ask, built): scans REAL GENERATED BLOCKS in a
radius (not seed prediction) for (a) powder_snow trap-roof blocks and (b) open-air
crevasse-shaped columns, marks each with a green particle beacon on demand -- ground truth,
zero prediction uncertainty. WIND: onset 85 -> 83, intensity-at-90 nudged up (small, per
"a little"). GLINT: LOCKED, no further changes without an explicit owner reopen.

## S30 (Peetsa 2026-07-20 eve, SKETCH): THE REAL COLLAPSE — "we're having a miscommunication"

OWNER SKETCH (verbatim intent, drawing supplied): an UNSUSPECTING player walks normal-looking
snow; the snow beneath GIVES WAY — labeled TRAP with the sub-surface region loosening
(arrows down), then "SNOW FALLS": the roof tumbles as falling chunks WITH the player into
the void. "And even sometimes you can drop down into a deep glacial cave (onto powder snow
so you don't take fall damage)." V1's static powder lid (sink-through, visible texture) is
NOT the vision — the trap is an EVENT: crack -> collapse -> tumble -> cushion.

S30 BUILD (supersedes the V1 lid): (1) GEN: roof sandwich = snow_block TOP (indistinguishable
from the snowfield) over 1-2 hidden powder_snow marker blocks over the void; powder cushion
at the floor stays; a fraction of spans probe downward and punch a narrow connecting shaft
when more carved void (tunnel) lies within ~16 blocks below the crevasse floor — the "deep
drop into a glacial cave", cushion moved to the true bottom. (2) RUNTIME EVENT: trigger =
player standing on snow_block whose direct-below is powder_snow with >=6 air below that (the
exact sandwich signature — no false positives on solid ground); telegraph ~0.5 s of cracking
sounds + snow particles; then the contiguous roof span (flood-fill, cap ~24 blocks) collapses
STAGGERED outward from the trigger over ~10 ticks — top snow_blocks become FallingBlockEntity
chunks (the sketch's tumbling squares) EXCEPT the 1-2 blocks directly above the triggering
player's column (those break to particles so the falling snow cannot entomb/suffocate the
player mid-fall), powder markers deleted with particles, whump + crack audio. Falling snow
lands as real blocks = the debris pile. Deterministic trigger + distance-ordered stagger (no
RNG). Flag family glacialCavesV1; gen half new-chunks-only; gate-1 flag-off untouched.

## S30 BUILT + COMMITTED — TEST 121 STAGED (2026-07-20 night)

Built per the sketch spec above by one dev crew, swept-quality review inline, suite
925/0/0, committed `12794f2d` + pushed. New pure law `core/SnowCollapseLaw.java`
(trigger signature snow_block / powder_snow below / >=6 air; flood-fill cap 24 columns;
Chebyshev-ring stagger fire = now + 10-tick telegraph + min(ring,10); anti-entomb
Manhattan<=1 collar) + driver `world/SnowCollapseRuntime.java` (claim-first idempotency —
markers removed before any scheduling; telegraph GLASS_BREAK+SNOW_BREAK cracks +
SNOWFLAKE/POOF; FallingBlockEntity tumbling chunks; collar columns break to particles,
never entities; whump SNOW_BREAK pitch 0.55 + POWDER_SNOW_HIT; queue cap 4096; hangs on
GlobeMod.borderUxTick; creative/spectator skipped). Gen sandwich in
`world/PowderCrevasseRoofFeature.java` (top snow_block flush with the snowfield, ONE
hidden powder_snow marker beneath, void below; cushion at floor; ~30% deep-drop spans
probe down and punch a 2x2 shaft to a >=4-air cave void within 16 blocks below the
crevasse floor, cushion at true bottom). `/latdev markGlacial` trap signal re-keyed to
the sandwich signature.

ORCHESTRATOR SELF-FLY (honest record): attempted TWICE on a SELFFLY-S30 jar (SHA
8f52acd57d02623a, world "S29 SelfFly" + fresh chunks ~84 S). BLOCKED both times by the
Minecraft/Xbox privacy-settings dialog ("Changes to Xbox settings may take some time to
apply") which re-fires on EVERY chat open in this automation lane and swallows every
typed command — a Mojang account-service prompt outside the mod and outside my control
(same wall that ended the S27 diagnostic flight). What IS live-proven from the earlier
S29/S30 flights: locateCrevasse teleported me INSIDE a real crevasse on fresh chunks;
markGlacial found 561 real trap roofs (V1-lid signature — the S30 sandwich generates
only in NEW chunks and is itself unwitnessed); real powder_snow confirmed underfoot; cold ladder
lethally real (died in ~4 s at 87 S in survival). What is NOT yet witnessed by anyone:
the collapse EVENT itself (crack -> tumble -> cushion) and a deep-drop. Per the S27
live-proof law this is declared UNVERIFIED-LIVE and the owner flight is the verifying
flight: walk any snowfield span at 83-84 (sub-lethal cold) — markGlacial paints the
roofs green first if wanted.

Gate-1 flag-off atlas re-proof: run at `12794f2d` (seed 20260714, regular, step 32,
2:1 aspect, 5 layers) — PASS, run 20260720-201917 vs baseline 20260717-193525, only durationMs differs. TEST 121
staged (SHA `8f52acd57d02623a`, differs from TEST 120; markers SnowCollapseLaw +
sandwich verified in-jar). Also in the round: S29 wind onset 83 / max 0.88, markGlacial
command, frost glint LOCKED per owner.

## S31 (Peetsa 2026-07-20 night, TEST 121 flight): "absolutely no change AT ALL" — DIAGNOSED, ALL THREE REAL

OWNER REPORT (verbatim): "I flew TEST 121 and noticed absolutely no change AT ALL. Wind did
not start sooner, did not increase in volume at 90. /latdev markGlacial did not work — it
said there were 0 matches in an 8-chunk radius, even though I was in the snowy barrens. I
never encountered a trap."

He was right on all three counts. Diagnosed on a HEADLESS RIG (loom dev server, fresh
globe_large world seed 987654, RCON console, forceloaded fresh 256-chunk areas at 83-85 S —
the lane the Xbox dialog cannot touch), fixed in `066f171f`:

1. **Traps never generated — for anyone, since caves r1.** The placed feature
   `powder_crevasse_roof` had placement `[in_square, biome]` — the `minecraft:biome` filter
   samples at the UN-LIFTED origin Y, deep underground, where the biome has been
   `globe:glacial_caves` since the r1 depth swap. The filter failed everywhere the
   underground is glacial, i.e. almost the whole barrens band: measured ONE feature run in
   256 fresh polar chunks (heartbeat recorder). This also retroactively explains the owner's
   TEST 118/120 "couldn't find the traps": they were never in his world. And the "561 trap
   roofs" from my S29 flight is now suspect as powder-pocket false positives on the old
   V1-lid signal — the honest current-signature count was the true zero he saw. FIX: add
   `minecraft:heightmap` (WORLD_SURFACE_WG) before the biome check (the vanilla
   surface-feature pattern). AFTER: 35/256 fresh chunks placed 8-27 sandwiches each, deep
   drops rolled, one sandwich block-verified in-world (snow_block/powder/air at 827,104,9217).

2. **markGlacial had an off-by-one hiding every sandwich.** `ChunkAccess.getHeight` returns
   the TOP-BLOCK Y (one below `Level.getHeight`'s first-air convention the scan grid
   documents). The roof probe therefore started ON the powder marker and walked DOWN — it
   could never see the snow cap one block above. The single roof it did find had a snow
   layer shifting everything up one. FIX: +1 on grid fill. Same 17x17-chunk area rescanned:
   trap roofs 1 -> 499. (His live "0 matches" was thus BOTH bugs stacked: nothing generated,
   and the scanner couldn't have seen it anyway.)

3. **The wind change was real but humanly inaudible — a tuning miss, mine.** S29 moved
   START 85->83 but kept the squared ease anchored at START: volume at 84 was
   MAX*(1/7)^2 = 0.018 (nothing), and 0.8->0.88 at the pole is under 1 dB. FIX: two-piece
   envelope — linear attack 0 -> 0.10 (a real whisper) across 83->84, then the same squared
   ease riding that floor to a FULL 1.0 at 90 (+2 dB over the original 0.8, clearly
   audible). Hysteresis/muffle/shelter behavior untouched; tests re-anchored symbolically.

Also landed with the round: `/latdev markGlacial [radius [x z]]` coordinate form (works
from a dedicated-server console + remote spot checks); the shippable /latdev tree registers
as `/latdev2` in dev environments (CLOSES the S27 dev/shippable split finding); permanent
flight recorders behind `-Dlatitude.debugCollapse` ([LAT][COLLAPSE] per-chunk census +
heartbeat, [LAT][CARVEGATE] append-exit naming — the carve-append itself was proven healthy:
crevasses/slots abound, 13k+ deep columns per 289 fresh chunks).

STILL OWNER-VERIFY: the collapse EVENT itself (crack -> tumble -> cushion) needs a real
player standing on a roof — walk fresh snowfield at 83-84, `/latdev markGlacial 4` paints
the roofs green. Wind verdict by ear at 83-84 and at 90.

## S32 (Peetsa 2026-07-21, TEST 122 screenshots): the fragmentation WAS the trap feature — RIM-BRIDGE LAW

OWNER REPORT + 5 screenshots at 84 N: "Trap roofs are now located, however, they are not
showing on the world... I do see a lot of fragmentation generation though, and it looks like
these were attempts at a snow roof that got corrupted. Do you see the fragmented hanging
blocks of snow?" — plus "Loading screen compass: how do we make the N look like it's not in
the way?"

HIS DIAGNOSIS WAS EXACTLY RIGHT. The V2 sandwich placed each roofed COLUMN at its own
windowed-max reference minus one: flush on flat snowfield, floating mid-air on the rough
sloped glacier in his shots (the windowed max is an uphill surface); and the >=10-below-max
candidacy trips on ordinary steep slopes, so floating blocks sprayed across hillsides.

FIXES (05f87d17, TEST 123):
1. **Rim-bridge law**: a span roofs ONLY when both bounding rim columns are known and within
   2 blocks of each other (a genuine slot through continuous snowfield — a slope's fake slot
   has rims ~10 apart and dies), and the whole span roofs FLAT at the lower rim's top-block Y
   — flush with both rims by construction, floating impossible. Depth re-checked per column
   against the REAL bridge Y. Census on a fresh barrens patch: slope chunks with 42-126
   candidates now place ZERO; 11 chunks placed 2-10 flush bridges; sandwich + rim anchoring
   block-verified at 1063,80,9588. Roofs are now INVISIBLE BY DESIGN (that is the trap) —
   the green markers are how you see them.
2. **Lingering markers** ("not showing on the world"): markGlacial beacons re-emit every 10
   ticks for 60 s (fresh set per scan, cap 400) — time to close chat and walk to them.
3. **Loading N** moved outside the dial, above the ring — face belongs to rose + needle.

FIELD FINDING (documented, not a bug): large polar tracts are glacier-over-
minecraft:deep_frozen_ocean (surface Y ~77 over sea columns; nearest true barrens was 249
blocks from the first census point). Crevasses cut there (carve probe reads the raw source
= land-ish) but the trap biome filter correctly excludes them (sacred-sea law). Traps
live ONLY in true polar_barrens patches — finding one is part of the hunt.

STILL OWNER-VERIFY: the collapse EVENT (needs a real walk), wind by ear (S31 retune rides
along in TEST 123).

## S33 (Peetsa 2026-07-21, TEST 123 flight): the markers were telling the truth — about the wrong thing

OWNER: "The green sparkles show now correctly, as you see in the picture; however, there is
no roof in the trap — it's just an empty space." Plus, on the S32 loading-screen N: "I'm not
sure I necessarily want it above. Maybe the top point of the compass rose should just be
shorter, pointing to a red N outlined in a shade-darker color. Apply the same rule to the
in-game compass."

MARKER ROOT CAUSE (legibility, not worldgen). markGlacial emits TWO signals and drew both in
the SAME green: trap roofs and open crevasses. The census ratio is ~1000:1 slots-to-roofs
(7240 vs 11), and the slot marker was a TALL column while the trap marker was a 3-block stub
— so the rare prize was drowned by, and indistinguishable from, thousands of holes. Every
sparkle the owner walked to was an open crevasse behaving exactly as designed. FIX: GREEN =
trap roof ONLY, as a solid 8-block pillar; BLUE soul-flame = open crevasse, short, cap 40;
chat + summary name each kind; a zero-trap scan now explains WHERE traps generate (new chunks,
true barrens land, not glacier-over-frozen-ocean). Rescan on a real patch: 26 green / 54 blue.

PERSISTENCE PROVEN (ruling out "the roof evaporates"): sandwich verified at 1298,61,9924
(snow_block / powder_snow / air), then save-all flush + FULL server restart + re-read — all
three blocks intact, rescan still finds them. Roofs survive save/load.

NORTH GLYPH LAW (new, shared): core/ui/CompassNorth holds the rule both hand-drawn roses
consume — north arm SHORTENED to stop 2 px below the glyph and point at it; N in a fixed red
(cartographic convention, not theme-derived) with a 4-way outline in that red darkened 45%,
outline suppressed below scale 0.7 where it blobs (the TEST 119 complaint). Applied to the
in-game dial AND the loading card; S32's above-the-ring N reverted per the owner.

933/0/0. Client + dev command only — no gate-1 this round (TEST 119 precedent). TEST 124.
STILL OWNER-VERIFY: the collapse EVENT (walk a GREEN pillar in survival at 83-84).

## S34 (Peetsa 2026-07-21, TEST 124 flight): "I'm not happy" — compass reverted, and the pillars were invisible by RANGE

OWNER: "The compass rose is cut off, the roofs are absent, I'm not seeing any green
sparkles. Put the compass back to how it was, with the N just above the compass."

COMPASS: the S33 shortened-arm + red-outlined-N experiment READ AS DAMAGE at real scale (his
closeup: the north arm looked amputated). Fully retired on both dials (782b29cb): HUD dial
back to the TEST 119 settled look verbatim; loading card back to the full rose with the N
floating just above the ring — the S32 placement, now owner-chosen ("with the N just above
the compass"). CompassNorth law + tests deleted with the experiment. LESSON: he asked for
"shorter", the implementation cut the arm to a stub; when a tuning ask lands wrong, revert
fully rather than iterate on top.

MARKERS — the second invisibility bug in a row, this one RANGE: his own screenshot PROVES the
scan works (chat: "GREEN trap roofs=66 | BLUE open crevasses=3106" with trap coordinates
listed) while the field shows nothing. The plain ServerLevel.sendParticles overload renders
only to players within ~32 blocks; an r=8 scan reports traps up to ~136 blocks out — the
green pillars were real, emitting, and outside render range at exactly the distances traps
live at. (The blue/old-green crevasse sparkles were visible before purely because crevasses
are EVERYWHERE, so some were always within 32 blocks — which is also why the markers "worked"
in every earlier test.) Fix: the (overrideLimiter=true, alwaysVisible=true) overload — force-
rendered at long range like vanilla's. Plus each GREEN chat line now carries a clickable
[teleport] (locateCrevasse pattern): finding a trap is one click.

928/0/0, TEST 125 (SHA 33749f98f411d0e3). No worldgen — no gate-1 (TEST 119 precedent).
OWNER-VERIFY: markGlacial → click a GREEN [teleport] → survival-walk the pillar → collapse.
