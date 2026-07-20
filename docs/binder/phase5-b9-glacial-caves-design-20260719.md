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
