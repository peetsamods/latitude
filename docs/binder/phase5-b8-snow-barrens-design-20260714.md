# Phase 5 B-8 — Snow Barrens design (2026-07-14)

`status: design — under review, no code written`
`branch: phase5-b7-pole-passage`
`author: Snow Barrens designer lane (read-only recon; src/ untouched)`

---

## Plain-language summary (for Peetsa)

You said: *"I want us to shift away from the regular snowy plains at the poles… maybe a new biome
called something like 'Snow barrens' — powder snow, snow blocks, and ice with snow carpet across the
whole land. The whole dirt+snow stuff is cramping my style."*

Here is what the pole is today and what I recommend.

**Today, past ~74.5° everything collapses to one biome: vanilla `snowy_plains`.** Our worldgen forces
it there on purpose (it's an anti-monoculture clamp that pushes trees, groves, and mountains out of the
deep cap). `snowy_plains` is a *grass-and-dirt* biome that vanilla dusts with a thin snow layer — so on
any slope the snow slides off and you see bare dirt/grass, exactly the look that's cramping your style.
We already strip the *flowers and grass tufts* up there (they're gone by 86°), but the **ground blocks
stay dirt/grass and the name stays "Snowy Plains."**

**My recommendation: build the real biome — `Snow Barrens` — not just a paint job.** A first-party
Latitude biome with its own name, its own ground (snow blocks + packed/blue ice + powder-snow pockets,
snow carpet everywhere, zero dirt showing), no trees, no grass, no flowers, and a stray/polar-bear/rabbit
spawn list. It becomes the dominant biome in the polar core (I suggest **past ~86°**, ramping to fully
dominant by 88°, blended on the same wispy noise every other Latitude border uses — no hard ring).

**Why the real biome and not just re-skinning the surface:** re-skinning leaves the HUD saying "Snowy
Plains" forever — and the *name* is half of what you asked to change. A registered biome fixes the name
**everywhere at once** (Latitude HUD, JourneyMap, and the F3 debug screen) and lets us control the ground
blocks, the spawns, and the fog/water color as one coherent identity. The surface tricks from the paint-job
idea become *how we build the barrens' ground* — we get both.

**Three honesty notes up front:**
1. This is a **worldgen** change — the highest-risk class (it's what got B-6 parked). So it ships the
   disciplined way: behind an off-by-default flag, proven **byte-identical when the flag is off**, and
   validated by a before/after atlas run — not by hoping.
2. It only affects **newly generated chunks.** Your existing polar caps stay `snowy_plains`; there will be
   a visible seam where old explored cap meets new barrens. We verify on a **fresh world** first (the way
   you test anyway) and we do **not** rewrite old chunks (the standing legacy-worldgen rule).
3. A first-party biome is **not** an external pack — it ships inside Latitude, always present, needs
   nothing installed. It works whether you have zero packs or a stack of them. (More on the vanilla-first
   law below — it's satisfied.)

**Open question only you can answer:** where does "Snow Barrens" begin — **85°** (loud story: the living
land ends the moment the killing cold and the first warning begin) or **86°** (invisible seam: barrens
begin exactly where the grass finishes fading, so there's no double transition)? I recommend **86°**. And:
is the name literally **"Snow Barrens"** (I'll register `globe:snow_barrens` → shows as "Snow Barrens")?

---

## The owner's order (verbatim, 2026-07-14)

> "I want us to shift away from the regular snowy plains at the poles. This may mean creating a new biome
> called something like 'Snow barrens' which is basically powder snow, snow blocks, and ice with snow
> carpet across the whole land. The whole dirt+snow stuff is cramping my style."

Screenshot context: `snowy_plains` at ~87.5° with dirt showing through on slopes + ice spikes; a HUD
readout of `minecraft:snowy_plains`. **The biome NAME is explicitly part of the ask.**

---

## 1. What the pole is today (recon receipts)

### 1.1 Placement: past 74.5° it's a `snowy_plains` monoculture, on purpose

Latitude does not use vanilla's climate-noise biome matcher for the cap. Our consumer (`LatitudeBiomes`)
resolves the polar band itself and then **clamps** it hard:

- `EXTREME_POLAR_CAP_MIN_DEG = 74.5` — the "extreme polar cap" begins here
  (`LatitudeBiomes.java:2778`).
- `clampExtremePolarCapOutput(...)` — any "soft cold leak" (grove, taiga, any `*forest*`/`*taiga*`) in the
  cap is rewritten to `minecraft:snowy_plains` (`LatitudeBiomes.java:6928-6940`, leak test
  `isExtremePolarSoftColdLeak` at `:6896-6915`).
- `clampFinalPolarNonMountainAlpineOutput(...)` — any alpine pick (`snowy_slopes`/`frozen_peaks`/
  `jagged_peaks`) on non-mountain terrain in the cap is *also* rewritten to `snowy_plains` unless the
  column has genuine `polarMountainAuthority` (tall/rugged terrain) (`LatitudeBiomes.java:6942-6957`,
  authority gate `:6828-6841`).
- The base polar pick is `pickPolarWithFrontShoulder(...)` (`LatitudeBiomes.java:5637+`), drawing from the
  `globe:lat_polar_primary|secondary|accent` tags (`:2689-2691`). Its non-mountain fallbacks are all
  `snowy_plains`/`snowy_taiga`/`grove` (`:5701-5735`).

Net effect above ~74.5° on flat/rolling terrain (the overwhelming majority of the cap): **`snowy_plains`**,
with two accents that survive the clamp:
- `ice_spikes` — a coherent accent capped at roughly its ~6% share (`POLAR_ICE_KEEP_THRESHOLD = 0.45`,
  `LatitudeBiomes.java:2719-2724`). These are the ice spikes in the screenshot.
- `snowy_taiga` — the Earth-like boreal fade, but only in the **lower** polar; it's gone by the tree-line
  fade end, which is *pinned to 74.5°* (`POLAR_TREELINE_FADE_DEG = 74.5`, `POLAR_BOREAL_SHARE = 0.6`,
  `LatitudeBiomes.java:2785-2797`). So past 74.5° taiga is clamped away too.

So the owner's ~87.5° screenshot is deep in the zone where the answer is *always* `snowy_plains`
(+ occasional `ice_spikes`, + `snowy_slopes`/peaks only on real mountains). This is the monoculture.

### 1.2 Why it looks like "dirt + snow"

`snowy_plains` is a **grass/dirt** biome; vanilla lays a 1-block snow *layer* on top. On any slope the
layer doesn't fully cover and the grass/dirt shows — the exact complaint. We already thin the small
vegetation up there:

- `POLAR_VEGETATION_FADE_ENABLED` — default **TRUE**; strips grass/fern/flower/bush/sugarcane on a frayed
  ramp, **onset 78°, fully bare by 86°** (`LatitudeV2Flags.java:216-230`; band values forwarded at
  `build.gradle:129-130`).

But the fade only removes *decorations*. The **ground blocks stay grass/dirt**, and the **biome id stays
`minecraft:snowy_plains`**. That's the gap Snow Barrens fills: right ground, right name.

### 1.3 The HUD name — important nuance

The owner's screenshot reads `minecraft:snowy_plains` (raw, namespaced). That is **not** the Latitude
HUD — both Latitude readouts strip the namespace and title-case the path:

- `BiomeSamplerTools.biomeDisplayName(...)` → `"…".split(":")[1]`, underscores→spaces, title-case
  (`BiomeSamplerTools.java:654`), used by `CompassHud` (`CompassHud.java:1233-1234, 1291-1292`).
- `GlobeWarningOverlay.biomeName(...)` → `titleCase(path)` (`GlobeWarningOverlay.java:188-200`).

So the Latitude HUD already shows **"Snowy Plains"**, not the raw id. The raw `minecraft:snowy_plains`
readout comes from **vanilla F3 debug** (or JourneyMap's id line). This matters for the two paths:

- A **registered `globe:snow_barrens`** → Latitude HUD shows **"Snow Barrens"** automatically (namespace
  stripped, path title-cased — no lang file needed); F3 shows `globe:snow_barrens`; JourneyMap shows
  "Snow Barrens" if we ship the lang key (below). **The name changes everywhere.**
- A **surface-only re-skin** (biome stays `snowy_plains`) → HUD *stays* "Snowy Plains", F3 *stays*
  `minecraft:snowy_plains`. **The name never changes.** This is why the paint-job path fails the ask.

### 1.4 The surface-rewrite precedent (this is not new machinery)

The mod already reshapes *surfaces* by latitude/altitude with a `ProtoChunk.setBlockState` mixin —
exactly the tool a Snow Barrens ground would use:

- `AlpineSurfaceMixin` — `@ModifyVariable` on `ProtoChunk.setBlockState`, gated on
  `LatitudeBiomes.ACTIVE_RADIUS_BLOCKS > 0`; above `ALPINE_ROCK_Y` (=168, `LatitudeBiomes.java:856`) it
  rewrites `grass_block/dirt/coarse_dirt/podzol/mycelium/gravel` → `STONE` or `SNOW_BLOCK` via
  `alpineSurfaceKind(x,y,z,radius)` (`AlpineSurfaceMixin.java:30-62`, kind logic `LatitudeBiomes.java:865+`).
- `ProtoChunkSnowBlockGuardMixin` — worldgen-time guard that *prevents* snow/`snow_block` in warm bands and
  fixes grass-in-ocean cells (`ProtoChunkSnowBlockGuardMixin.java`), and `ChunkRegionWarmSnowTrapMixin`
  catches `POWDER_SNOW`/snow at the write API (`ChunkRegionWarmSnowTrapMixin.java:39`).
- `TreeLineVegetationGuardMixin` — the latitude tree-line guard (companion surface rule).

**Takeaway:** "reshape the polar ground to snow/ice by latitude" is an established, low-risk pattern here.
Snow Barrens' *surface* is a straight extension of `AlpineSurfaceMixin`; its *identity* (name/spawns/color)
is the new part, and that's what a registered biome buys.

### 1.5 Biome resolution: a mod biome slots in with zero special-casing

The consumer resolves biomes by **id string** against the live registry:

- `biome(biomes, id)` → `Identifier.parse(id); biomes.get(ident).orElseThrow()` (`LatitudeBiomes.java:5151-5154`).
- `entryById(collection, id)` → matches by full `Identifier` (`LatitudeBiomes.java:7383-7392`).

Both resolve **any** id present in `Registries.BIOME`, including a datapack/first-party biome
`globe:snow_barrens`. The mod already ships only biome **tags** under `data/globe/tags/worldgen/biome/…`
and consumes vanilla + pack biomes — it registers **no biome definitions of its own today**. Adding one
JSON biome definition is the new step; the picker needs only the new id string, no structural change.

Mod id is `globe` (name "Latitude"), per `fabric.mod.json` → natural biome id **`globe:snow_barrens`**.

---

## 2. The two paths

### Path A — Surface dressing (no new biome)

A latitude-gated surface rule (a `SnowBarrensSurfaceMixin`, sibling of `AlpineSurfaceMixin`) that, past
~86°, rewrites exposed `grass_block/dirt/podzol/…` → `snow_block`, lays `snow` carpet on every walkable
top, seeds `powder_snow` pockets and `packed_ice`/`blue_ice` sheets on a coherent noise field. **Biome
stays `snowy_plains`.**

- Pros: smallest change; no registry addition; trivially flag-gated & byte-identical off.
- **Fatal con: the name never changes** (§1.3) — fails the explicit ask. Also can't restrict mob spawns
  or set fog/water color (those are biome properties, not surface properties).

### Path B — Registered biome `globe:snow_barrens` (RECOMMENDED)

A first-party Latitude biome with full identity, placed by **our consumer** as the dominant polar-core
biome past the chosen degree. Path A's surface tricks become *how we build its ground.*

- **Identity:** id `globe:snow_barrens`; lang `biome.globe.snow_barrens = "Snow Barrens"`
  (`assets/globe/lang/en_us.json` already exists) for JourneyMap/translation consumers; Latitude HUD reads
  "Snow Barrens" automatically.
- **Climate fields (JSON):** `temperature` well below 0 (e.g. `-0.7`, frozen-family) and low `downfall`
  so vanilla's *runtime* treats it as frozen (snow falls not rain, water freezes, powder-snow persists),
  `precipitation`/foliage-free. **These fields do NOT decide where it spawns** — our consumer owns
  placement; they only drive runtime feel + color.
- **Effects:** cold fog/water/sky colors (icy blue-white), snow particles ambient, quiet/cold mood; no
  music override needed (the polar wind bed already plays, §4).
- **Features:** **no trees, no grass, no flowers.** Include snow/ice surface features and (optionally)
  ice-spike-like formations, OR — preferred, matching existing architecture — leave the JSON feature list
  minimal and lay the ground via the `SnowBarrensSurfaceMixin` (Path A's mechanism) so the barrens' look is
  code-controlled and composes with the existing snow guards.
- **Spawns:** restrict to cold fauna only — `stray`, `polar_bear`, `rabbit` (white); no passive
  farm animals, no warm mobs. (Spawn-list restriction is the ONLY mob change — §6.)
- **Placement:** insert `globe:snow_barrens` as the clamp target and pool primary in the extreme cap —
  i.e. `clampExtremePolarCapOutput` returns `globe:snow_barrens` instead of `snowy_plains` past the chosen
  degree (`LatitudeBiomes.java:6936`), and the polar non-mountain fallbacks (`:5723`, `:6956`) point to it.
  `ice_spikes` stays as the coherent accent (or is absorbed — owner's call, §6); real mountains still get
  `snowy_slopes`/peaks via `polarMountainAuthority`.

**Recommendation: Path B**, with Path A's surface rule as B's ground implementation. It is what was asked
(the name changes), it's the only path that can own spawns + color, and the surface half is a proven pattern.

---

## 3. Design decisions (recommendation + rationale)

### 3.1 Vanilla-first law — the position

The law (CLAUDE.md / memory `vanilla-first-overhaul-constraint`): *the 2.0 overhaul must work
vanilla-biomes-only AND with custom packs; packs are enrichment, never required.*

**Position: a first-party biome satisfies the law.** The law's target is **external dependencies** — never
forcing the user to install BoP/Terralith/etc. `globe:snow_barrens` ships **inside Latitude**: always
present, needs no user action, works with zero packs and with any stack of packs. It is Latitude providing
its **own** content — the same way the mod already provides its own worldgen logic, HUD, tags, and surface
mixins. It is "Latitude enriching itself," not "Latitude requiring a third party."

**The one honest tension:** it *is* a departure from the strict reading "vanilla biome ids only." A Latitude
world would contain one `globe:` biome. We resolve this two ways: (a) it's first-party, so the promise that
*matters* — no external install — holds; (b) it's **flag-gated**, so the strict vanilla-only polar cap
(`snowy_plains`) remains available and is the **proven byte-identical default** until the owner flips it.
The flag makes the departure opt-in and reversible, which is exactly how every prior structural change here
has been introduced.

### 3.2 The band — where do the Barrens begin?

Anchor to an existing rung, don't invent one. Candidates, with the current cold/veg ladder
(this branch — note the reference doc predates the B-7 82° move):

| Deg | What already happens there | Story if Barrens start here |
|---|---|---|
| 74.5° | Extreme cap begins; taiga/grove clamped out | Too low — would erase the Earth-like boreal→tundra fade the mod builds; **reject** |
| 82° | Ambient snow onset — world whitens (`AMBIENT_ONSET_DEG=82`, `PolarHazardWindow.java:302`) | Barrens as soon as it starts snowing — a touch early; snowy_plains 82–86 still has *some* honest tundra feel |
| **85°** | First warning fires; all water freezes; frostbite band opens (`FROSTBITE_ONSET_DEG=85`, `:216`; `FREEZE_ALL_DEG=85`) | **Loud story:** "the living land ends where the killing cold begins." A single memorable line: *past 85° = Snow Barrens.* |
| **86°** | Vegetation fully bare (`PolarVegetationFade FULL_DEG=86`) | **Invisible seam (RECOMMENDED):** barrens begin exactly where the grass finishes fading → no double transition; snowy_plains at 86°+ is *already* treeless+grassless, so it was a barren snowy_plains lacking only the right blocks + name |
| 87–88° | Blizzard look (87), hazard/frost+slowness (87.5), freeze damage + lethal core (88) | Too high — leaves an ugly bare-dirt `snowy_plains` collar at 86–88° right where the owner's screenshot was |

**Recommendation: onset 86°, ramping to full dominance by ~88°, frayed on a coherent ValueNoise field**
(the same idiom as `POLAR_BOREAL_SALT`/`POLAR_ICE` accents — a soft wispy edge, never a hard ring). Below
86° stays `snowy_plains`/`snowy_taiga` (the boreal→tundra gradient). This covers the 87.5° screenshot with
full barrens and produces zero visible double-transition. **85° is the clean alternative** if the owner
prefers the louder "past 85° the land dies" line — it dovetails with the first warning and the water-freeze
line. **This is the one dial only the owner can set** (§ open questions).

Expose the onset as a `-D` knob (`latitude.snowBarrens.onsetDeg`, default 86.0) like every other Latitude
threshold, for live tuning.

### 3.3 Existing worlds — new chunks only, honest seam, no retro-conversion

- Barrens generate in **newly generated chunks only.** Already-generated polar caps stay `snowy_plains`
  (**legacy-worldgen-policy pin** — placement-time only, blocks on disk are never rewritten; same rule
  `POLAR_VEGETATION_FADE_ENABLED` and `EDGE_STRUCTURE_VETO_ENABLED` already state,
  `LatitudeV2Flags.java:227, 205-210`).
- Consequence: a **visible seam** at the explored/unexplored boundary in polar regions on an existing
  world — old snowy cap meets new white barrens. **Document it; don't hide it.**
- **Verify on a fresh world first** (the owner tests fresh worlds anyway; memory
  `latitude-2p0-longitude-test1-punchlist`). No auto-conversion of old chunks — that would be a
  world-rewrite, out of scope and against the pin.

### 3.4 Hazard synergy — reuse what B-7 built; no new mechanics in v1

The Barrens sit **on top of** the B-7 cold system, which already peaks in the same latitudes:

- **Powder-snow pockets as hidden traps.** Vanilla: leather boots let you walk *on* powder snow;
  without them you sink and freeze faster. Fresh snow carpet visually hides the pockets → genuine polar
  hazard, for free, from the ground design. (The mod already reasons about powder-snow footing:
  `HemispherePassageService.isPowderSnowLanding` rejects powder-snow teleport landings,
  `HemispherePassageService.java:363-365; B-7 doc §5.4 powder-snow probe rejection.`) **Cap pocket size**
  so the Barrens are traversable, not a death-carpet.
- **Frostbite + the campfire ritual already cover survival.** Frostbite band `[85,88)`
  (`PolarHazardWindow.java:216-220`); cold protection via the vanilla `freeze_immune_wearables` tag —
  leather (`ColdProtection.java:6-7, MAX_PIECES=4:33`); S4 shelter pause + S6 campfire-heal ritual (B-7).
  Barrens add **no new damage or survival mechanic** — they are the *place* those mechanics happen, given
  a matching name and ground.
- **Ambient:** the polar **wind bed** (`PolarWindSound`, from 85°) + blizzard already provide the audio;
  no Barrens-specific sound in v1.
- **Water:** `POLAR_WATER_FREEZE_ENABLED` (default TRUE, `FREEZE_ALL_DEG=85`, `LatitudeV2Flags.java:250-251`)
  already freezes exposed water past 85°, so the Barrens' ice reads consistently and player pools freeze —
  no extra work.

**v1 is tight: terrain + name + features + spawn-list. No new mechanics.**

### 3.5 Proof plan

Two gates, mirroring every prior worldgen slice.

**A. Atlas before/after (the placement proof).** Tooling is `tools/atlas/atlas_runner.py` (macOS path;
`Atlas.ps1` is Windows-only — memory `atlas-run-macos`). It forwards `-D` flags into the headless run via
`--sysprop` (`atlas_runner.py` `generate` subcommand; sysprops appended to the Gradle JVM), and emits
`biomes.png` + `legend.json` + `world_biome_inventory.json` (`REQUIRED_BUNDLE_FILES`). Default render is
Mercator 2:1 (`--aspect 2.0`).

```
# Flag OFF (must be byte-identical to pre-change HEAD — the hard gate):
python3 tools/atlas/atlas_runner.py generate --step 32 --size regular \
    --sysprop latitude.snowBarrens.enabled=false

# Flag ON (barrens must be dominant past the chosen degree):
python3 tools/atlas/atlas_runner.py generate --step 32 --size regular \
    --sysprop latitude.snowBarrens.enabled=true
```

(Equivalently `JAVA_HOME=<temurin-25> ./gradlew runBiomePreview` with the same `-Dlatitude.*` props;
build.gradle already forwards each flag through the biomePreview run — the L17 "born with its build.gradle
line" discipline, see the block at `build.gradle:102-134`.)

- **Flag OFF ⇒ `world_biome_inventory.json` identical to HEAD** (no `globe:snow_barrens`, polar share
  unchanged). This is the **byte-identity-off hard gate.**
- **Flag ON ⇒** `globe:snow_barrens` is the dominant polar biome at/above the chosen degree, `snowy_plains`
  share there collapses, everything equatorward unchanged. Per-band composition via
  `tools/atlas/band_correctness_check.py` / `geography_analyzer.py` / `overrep_rank.py`.

**B. Suite.** `./gradlew test` (the standard suite the prior slices ran green — e.g. the B-3/B-5 suites).
Add unit coverage: the onset-degree ramp (below onset → not barrens; above → barrens), the coherent-noise
fray, and that `globe:snow_barrens` resolves via `biome()`/`entryById`.

**C. Live (owner):** a fresh Wide world, walk to the pole, confirm HUD reads "Snow Barrens," ground is
snow/ice with no dirt, no trees/grass, seam behavior as documented.

### 3.6 Scope fence — v1 non-goals

**In v1:** ONE biome (`globe:snow_barrens`); polar core only (past the chosen degree); its ground
(snow_block + snow carpet + powder-snow pockets + packed/blue-ice sheets via the surface mixin); no trees /
grass / flowers; spawn list restricted to cold fauna; the onset-degree + composition dials; the flag +
byte-identity-off + atlas proof.

**Explicit non-goals (do NOT build in v1):**
- No glacier/ravine carving or any terrain-height change (surface *blocks* only — the AlpineSurface pattern
  never moves terrain height).
- No aurora / sky feature / new particles beyond the existing polar snow+blizzard.
- No new blocks (vanilla `snow_block`, `snow`, `powder_snow`, `packed_ice`, `blue_ice`, `ice` only).
- No new survival/damage mechanic (rides B-7's cold system unchanged).
- No structures, and no structure-placement changes (the edge-structure veto is a separate system).
- No second biome / no sub-variants (no "ice desert" vs "snow field" split) — one identity.
- No retro-conversion of existing chunks.
- No default-on flip in this pass — default OFF; the ship default is the owner's post-live call (B-5/B-7
  precedent).

---

## 4. Technical shape (for the future dev session)

1. **Flag** — add to `LatitudeV2Flags` (pattern `:38-51`): `SNOW_BARRENS_ENABLED` (default **false** —
   worldgen-changing, so default-off + byte-identity-off, unlike the correctness-fix flags that default
   true). Optional `SNOW_BARRENS_ONSET_DEG` (default 86.0) and `SNOW_BARRENS_FULL_DEG` (default 88.0),
   parsed defensively. **Born with its `build.gradle` forwarding line in the same pass** (L17;
   `build.gradle:102-134`).
2. **Biome JSON** — `src/main/resources/data/globe/worldgen/biome/snow_barrens.json`: frozen climate,
   icy effects, no-tree/no-grass feature list, cold-only spawners. Loads into the dynamic `Registries.BIOME`
   at world load; the consumer resolves it by id automatically (§1.5).
3. **Lang** — `assets/globe/lang/en_us.json`: `"biome.globe.snow_barrens": "Snow Barrens"`
   (JourneyMap/translation; Latitude HUD doesn't need it but keep it for correctness) — **same-pass index
   discipline.**
4. **Placement** — in `LatitudeBiomes`, gate on `SNOW_BARRENS_ENABLED && ACTIVE_RADIUS_BLOCKS>0 &&
   latDeg>=onset`: point the extreme-cap clamp target (`clampExtremePolarCapOutput` `:6936`) and the polar
   non-mountain fallbacks (`:5723`, `:6956`) at `globe:snow_barrens` on a coherent fray between onset and
   full. Flag-off = the current `snowy_plains` returns (byte-identical). Keep `ice_spikes` accent + real
   `polarMountainAuthority` peaks.
5. **Surface** — `SnowBarrensSurfaceMixin` (clone `AlpineSurfaceMixin` shape, `:30-62`): in barrens columns,
   rewrite exposed `grass_block/dirt/coarse_dirt/podzol/mycelium/gravel` → `snow_block`; lay `snow` carpet;
   seed capped `powder_snow` pockets + `packed_ice`/`blue_ice` on coherent noise. Composes with the existing
   snow guards; gated identically. Flag-off = mixin's first check returns immediately (byte-identical).
6. **Proof** — §3.5. Flag-off byte-identity is the release gate.

---

## 5. Open questions for the owner

1. **Band onset — 85° or 86°?** I recommend **86°** (invisible seam with the grass fade; snowy_plains is
   already bare there). **85°** is the louder "past 85° the land dies" line. *(Only-owner dial.)*
2. **Name — literally "Snow Barrens"?** I'll register `globe:snow_barrens` → HUD/JourneyMap read
   **"Snow Barrens"**, F3 reads `globe:snow_barrens`. Confirm the spelling, or pick another
   ("Frozen Barrens", "Polar Barrens", "Ice Barrens").
3. **`ice_spikes` — keep as a coherent accent inside the Barrens, or absorb them** (barrens grow their own
   ice formations, no `minecraft:ice_spikes` at all)? I lean **keep the accent** (less change, familiar
   landmark), but absorbing gives a purer single identity.
4. **Spawns — `stray` + `polar_bear` + `rabbit` only?** Confirm the list (e.g. include `snow_golem`? exclude
   `rabbit`?).

---

## Cross-links

- `docs/binder/polar-experience-reference-20260712.md` — single source of truth for the polar experience
  (note: its ambient-onset table predates this branch's B-7 move to 82°).
- `docs/binder/phase5-b7-pole-passage-design-20260713.md` — B-7 pole passage + the S1–S6 cold/frostbite/
  campfire mechanics the Barrens ride on; the "zero worldgen, unlike B-6" discipline statement.
- `docs/binder/phase5-boundary-experience-plan-20260709.md` — Phase 5 running pass log.
- B-6 (parked) — the worldgen-change failure class this design answers with flag + byte-identity-off +
  atlas proof rather than avoidance (`LatitudeV2Flags.java:175-177`; git `f5539e35`).


## OWNER DECISIONS (Peetsa, 2026-07-14) — BINDING, supersede open questions in §5
1. ONSET: 86 deg (the invisible seam — starts where the vegetation fade finishes), ramp to full by
   88 on the usual coherent noise. 
2. NAME: **"Polar Barrens"** — biome id `globe:polar_barrens` (NOT snow_barrens; the HUD/JourneyMap/F3
   title-case the id, so the id itself carries the chosen name). Update every id reference in this
   doc's build sections accordingly at build time.
3. ICE SPIKES: kept as a RARE COHERENT ACCENT inside the Barrens (landmark fields in the waste).
4. SPAWNS: stray + polar_bear + rabbit only.
Phase name stays B-8; flag `latitude.snowBarrens.enabled` may keep its name or become
`latitude.polarBarrens.enabled` — build crew picks ONE and forwards it in build.gradle same pass
(L17 law); the id and display name are law.


## ADVERSARIAL DESIGN SWEEP — APPROVE-WITH-AMENDMENTS (2026-07-14, BINDING build recipe deltas)
Amendments 1-3 BLOCKING (the doc's §4.4-4.5 recipe as written would be silently erased live,
unpaintable by the atlas, and feature-dead):
A1 PLACEMENT: do NOT retarget the clamp/fallback literals (census wrong anyway — every site has a
Registry/Collection twin + polarSnowyBase :8034 is the dominant deep emitter). Instead: ONE
flag-gated FINAL OVERRIDE per pick twin, inserted AFTER applyClimateCompatReroll (:3785 and
:4495), rewriting ONLY minecraft:snowy_plains -> globe:polar_barrens on the 86->88 coherent fray.
No static lat_* tag membership EVER (would leak barrens to 70 deg + change flag-off pools). This
placement makes ice_spikes accent + mountain alpine survive by construction (owner decision #3
mechanism, free).
A2 QUARANTINE + POOLS: the vanilla-first quarantine (isCustomBiome :7492, quarantineUnknownCustom
LandBiome :6236, runs :3719/:4437 AFTER the old insert points) must admit the id: add to
allowedExtraBiomeIdsForBand(BAND_POLAR) (:6138) flag-gated. ATLAS: activate the two stubs
expandSourceCandidatePool (:566) + rememberSourcePolicyBiomeRegistry (:570) (flag-on append) and
route BiomePreviewHeadlessRunner sourcePool (:1038,:1214) through the expansion so atlas pool ==
live registry reach (kills the structural false-green). FEATURES: admit the id to the features
mixin's index (ChunkGeneratorGenerateFeaturesBiomeSetMixin) or freeze_top_layer never runs.
A3 JSON: features = freeze_top_layer ONLY (ground look = PolarBarrensSurfaceMixin substitutions;
carpet CANNOT come from a surface mixin); copy snowy_plains carvers; spawners monster=[stray],
creature=[polar_bear,rabbit], rest empty + spawn_costs {}; add to minecraft:spawns_white_rabbits;
REGISTER UNCONDITIONALLY (conditional registration would corrupt flag-on-then-off worlds; flag-off
registry visibility in F3/locate is inherent and documented, not a bug).
A4 NAMING/FLAGS: rename all snow_barrens refs -> polar_barrens (JSON filename, lang key
biome.globe.polar_barrens, mixin, tests); flags latitude.polarBarrens.enabled (default OFF) +
.onsetDeg default = PolarVegetationFade.FULL_DEG (KEEP-SHARED, live-tunable) + .fullDeg 88.0;
build.gradle L17 lines same pass.
A5 PROOF: 3 pinned-seed atlas runs (HEAD baseline / flag-off / flag-on; off vs baseline must be
byte-identical bundles) + fixed-seed region-hash or feature-index-no-rebuild assertion for the
RNG-shift risk; teach BiomeBandPolicy (:60), band_correctness, palette legend the new id SAME PASS
(indexing law). ATLAS RUNS ARE ORCHESTRATOR-OWNED (top-level session), never subagent-owned.
A6 HONESTY: coasts stay snowy_beach, rivers frozen_river ("zero dirt" is inland-only — flight
brief says so); 87.5 deg is ~75% barrens on the fray (dirt patches until 88 — owner chose the
invisible seam); id contains no snow/ice token so path-based cold classifiers ignore it (verified
harmless today, noted for future).
