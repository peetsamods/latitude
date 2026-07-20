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
