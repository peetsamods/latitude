# Phase 5 B-10 — Polar Outfitting (Polar Suit, leather demotion, snow goggles, Cold Protection status effect) — DESIGN

`status: design`  ·  `2026-07-18`  ·  branch `phase5-b7-pole-passage` @ `836d8c39` (recon HEAD)  ·  worktree
`/Users/joolmac/CascadeProjects/Latitude-b5-hemisphere-passage`

> **Read-only design.** No `src/` was touched producing this doc. **Line numbers below are as of committed HEAD
> `836d8c39`; two build crews are editing `src/` concurrently, so treat every `file:line` as a landmark, not an
> address — re-`grep` the named symbol before editing.**

> **Owner orders (Peetsa, 2026-07-18, verbatim fragments).** "a recipe for a 'polar suit' armor that can fully
> protect against the elements. When this is worn, no warning messages at all appear. It has to be worn as a set
> though; having only a few pieces on will reduce damage but a whole set will completely negate it. I think
> leather armor should only provide partial protection against the cold. When leather armor is worn, a message
> can appear in the warning zones that says 'Your leather armor provides some protection against the bitter
> cold.' Whereas a polar suit would just show a status effect inside the inventory menu that cold protection is
> provided. I think when the snow goggles are worn, the vignette effect is removed as well."
>
> **Sequencing law (orchestrator, owner-flagged):** leather demotes to partial ONLY when the suit ships — never
> a protection gap.

---

## 1. Plain-language summary (for Peetsa)

Right now, a full set of plain **leather armour completely cancels** the pole's freeze damage — leather is a
free "I win" button at the coldest place in the world. This phase fixes that and gives you a real reason to
prepare for an expedition.

**What you'll craft.** A four-piece **Polar Suit** — a fur-lined hood, parka, leggings and mukluk boots — sewn
from a new crafted material, **Insulated Hide** (leather + wool + string; a whole set is a real gathering
project, the way an expedition should be). Plus a cheap early item, **Snow Goggles** (glass lenses on a leather
strap).

**What the suit does.**
- **A whole set** = the cold cannot hurt you. Zero freeze damage, **and not a single warning message or dark
  screen-edge flash** — the pole is just quiet. The only sign it's working is a **"Cold Protection" status
  effect sitting in your inventory screen** (exactly as you asked). You still walk slow and frost over — the
  world still feels frozen — you just don't take damage or get nagged.
- **A few pieces** (1–3) = the cold hurts you **less** (each piece shaves off a chunk of the damage) but the
  warnings and the screen-edge vignette **still fire** — because the place is still genuinely deadly, and it
  would be a lie to hide that until you've earned the full set.

**What changes for leather.** Leather now gives **partial** protection only — it softens the damage but can
**never** fully cancel it, and it **never** silences the warnings. When you walk into the cold wearing leather,
you get your line once: **"Your leather armour provides some protection against the bitter cold."** This demotion
ships **in the same update as the suit**, so there's never a moment where the old leather trick is gone but the
suit doesn't exist yet.

**Snow Goggles.** Worn on the head, they **remove the warning vignette** (the dark pulsing screen-edge frame) —
the text warnings still appear, but the frame stops. (Down the line the goggles also become your "clear sight"
kit — cutting the snow out of your line of view — but that bigger sight rework is a separate follow-up, see §8.)
Because goggles and the suit hood both go in the head slot, the **suit hood has the goggle visor built in** — so
a full-suit traveller gets clear sight for free, and the standalone goggles are the cheap early option for
someone who hasn't sewn the suit yet.

**One honest rule underneath all of it:** the same score that decides how much damage you take is the same score
that decides whether you're silenced — they can never drift apart into a lie (this is the project's existing
"one evaluator, one truth" law, extended).

Everything here is **off by default** behind one switch (`latitude.polarOutfitting.enabled`); with the switch
off, leather behaves exactly like it does today (no gap), and the new items exist but can't be crafted.

---

## 2. Where this sits in the code today (receipts)

### 2.1 The current cold-protection evaluator (the thing being reworked)

- **`core/ColdProtection.java`** (`src/main/java/com/example/globe/core/ColdProtection.java`) — pure math, the
  count→behaviour map. Today it takes ONE integer: the number of armour slots (0–4) carrying an item in the
  vanilla `freeze_immune_wearables` tag.
  - `damageMultiplier(int pieces)` = `(MAX_PIECES - pieces)/MAX_PIECES` → `1.00 / .75 / .50 / .25 / .00` for
    0..4 pieces (`ColdProtection.java:44`).
  - `negatesFreezeDamage(int pieces)` = `pieces >= 4` (`:51`) — **this is the predicate everything keys on**:
    full set ⇒ damage negated.
  - `protectionLevel(int pieces)` (`:58`) — the level the warning-text swap reads.
  - **Today leather == suit**: a full leather set (leather is the only default member of
    `freeze_immune_wearables`) trips `negatesFreezeDamage` and gets full negation. This is exactly what the
    owner wants demoted.

- **Server shim** — `GlobeMod.coldProtectionPieceCount(ServerPlayer)`
  (`src/main/java/com/example/globe/GlobeMod.java:820`): iterates `COLD_ARMOR_SLOTS` (HEAD/CHEST/LEGS/FEET),
  counts `stack.is(ItemTags.FREEZE_IMMUNE_WEARABLES)` (`:824`). Consumed by the freeze-damage application at
  `GlobeMod.java:591` and `:653` (`* (float) ColdProtection.damageMultiplier(coldProtectionPieceCount(player))`).
- **Client twin** — `client/PolarColdClient.coldProtectionPieces(Minecraft)`
  (`src/main/java/com/example/globe/client/PolarColdClient.java:59`) + `protectionFull(Minecraft)` (`:75`) —
  reads the OWN player's armour identically so client presentation and server mechanic can never disagree.

### 2.2 The warning ladder + the text swaps (the "silence law" surface)

- **`core/PolarColdCues.java`** (`src/main/java/com/example/globe/core/PolarColdCues.java`) — the pure five-rung
  ladder decision:
  - Rungs `APPROACH(80)` / `HYPOTHERMIA(85)` / `BLIZZARD(87)` / `DANGER(88)` / `LETHAL(89.7)`
    (`PolarColdCues.java:51-64`). `APPROACH` is pinned to `PolarHazardWindow.AMBIENT_ONSET_DEG` (80).
  - `evaluateLadder(absLatDeg, highestFired, protectionFull)` (`:138`) — fires the deepest newly-reached rung;
    **currently `protectionFull` only suppresses the HYPOTHERMIA rung** (`:147`, honesty: full leather = no
    frostbite = no "bite" line), advancing `highestFired` so it never retries. **APPROACH/BLIZZARD/DANGER/LETHAL
    all still fire under full protection today.**
  - `lethalTextProtected(protectionFull)` (`:158`) — when full protection, the LETHAL rung swaps its text.
  - `REMOVAL_WHISPER_TEXT = "Hypothermia is imminent."` (`:101`) + `removalWhisperFires(...)` (`:169`): whisper
    once on the falling edge of full protection while in the cold zone (≥85).
- **Overlay wiring** — `client/GlobeWarningOverlay.java`:
  - `POLE_DANGER_TEXT` / `POLE_LETHAL_TEXT` / `POLE_LETHAL_PROTECTED_TEXT = "The bitter cold envelops you."`
    (`GlobeWarningOverlay.java:69-85`).
  - `poleTextForRung(rung, protectionFull)` (`:233`) picks the LETHAL text via `lethalTextProtected` (`:243`).
  - The per-tick evaluation reads `PolarColdClient.protectionFull(client)` (`:270`), fires the removal whisper
    (`:276`), then runs `evaluateLadder` (`:287`) and persists `poleWarnHighestTier`.

### 2.3 The vignette (what the goggles remove)

- **`core/ui/PolarWarningVignette.java`** — pure wall-clock envelope; **only DANGER (tier 3) and LETHAL (tier 4)
  earn a vignette** (`PolarWarningVignette.java:32-34, 99-107`); every other tier returns 0.
- **`client/PolarVignetteOverlayHud.java`** — the visible edge-darkening; a provable no-op whenever no
  DANGER/LETHAL episode is armed (reads the armed episode state from `GlobeWarningOverlay`). This is **the
  "vignette effect" the owner names** (distinct from `PolarWhiteoutOverlayHud`, the full-screen storm atmosphere,
  which is NOT a warning and is out of scope here).

### 2.4 Content-registration reality (the new-territory finding)

- **The mod has ZERO custom Java-registered game objects.** No `Item`, no `ArmorMaterial`, no `MobEffect`, no
  `CreativeModeTab`, no `Registry.register(...)` for game content anywhere in `src/main/java` (grep for
  `ArmorMaterial|MobEffect|new Item(|Registry.register.*ITEM` → empty).
- The **only** first-party registered content today is **data-driven**: `globe:polar_barrens`, a biome that
  exists purely as `src/main/resources/data/globe/worldgen/biome/polar_barrens.json` with **no Java registration
  code** (`LatitudeV2Flags.java:254-302` documents it "registers UNCONDITIONALLY" — i.e. the datapack JSON is
  always present; Java only conditionally *places/consumes* it).
- **Items cannot be data-only in Fabric.** A biome is a data registry (JSON is enough); an item is a code
  registry — the `Item` object MUST be created and registered in Java. Textures / models / lang / recipes /
  equipment-layer assets are data, but the item, the armour material, the equippable behaviour, and the
  MobEffect are all Java. **This phase is the mod's first Java content registration.** See §9 (risk).
- **Flag pattern** — `core/LatitudeV2Flags.java` (`src/main/java/com/example/globe/core/LatitudeV2Flags.java`):
  every flag is `static final … System.getProperty(…, default)` read once at class-init (`:38-416`), each with
  a matching `build.gradle` forwarding line in the SAME pass (the **L17 discipline**; barrens example at
  `build.gradle:158-167`, pole-passage at `:138`).

### 2.5 Parked siblings (the outfitting family — scope boundary)

From `docs/binder/phase5-b7-pole-passage-design-20260713.md`:
- **S7 FUTURE NOTE (`:656-660`):** "leather-as-drysuit is a v1 placeholder that reads wrong in water" → a future
  **DRYSUIT** item (leather protects on LAND, drysuit protects IN WATER), parked with the **expedition-tonic**
  potion (`ColdProtection.java:22-24` reserves it as "an additional multiplier here"). "both are the future
  'polar outfitting' item family."
- **S12 GOGGLES SPEC (`:751-763`):** goggles relax the fog-distance caps + reshape the whiteout into a **RING**
  (clear centre, frost at the rim) + **per-spawn cone filter** for blizzard particles
  (`dot(spawnDir,lookDir) >= cos θ` suppresses in-view spawns). "No shaders needed anywhere in the goggle stack."
- **B-10 open (`:842-847`):** "POLAR SUIT armor set … full set = total cold negation + NO warning messages + an
  inventory status effect (Cold Protection); partial set = reduced damage; recipes + first-armor asset plan.
  LEATHER DEMOTES to partial-only WITH the suit ship (never before). SNOW GOGGLES worn = warning vignette
  removed. Family: suit (body) + goggles (sight) + drysuit (water) + expedition tonic (future)."

---

## 3. Design question 1 — the Suit, the material, and the unified protection curve

### 3.1 The suit (recommendation)

Four pieces, standard vanilla armour slots:

| Slot | Item id | Fiction |
| --- | --- | --- |
| HEAD | `globe:polar_hood` | Fur-lined hood **with a built-in goggle visor** (see §6 — resolves the head-slot clash) |
| CHEST | `globe:polar_parka` | Insulated parka |
| LEGS | `globe:polar_leggings` | Padded over-trousers |
| FEET | `globe:polar_boots` | Fur mukluks |

**Look / material pick — fur-lined leather over an insulating wool layer.** Weighed against alternatives:
- *Wool + leather (chosen).* Both are **temperate-gatherable** (cows, sheep) BEFORE you march north — critical,
  because the suit's whole purpose is to be crafted *in order to* survive the pole. Vanilla-first (no pack
  needed), reads unmistakably as expedition cold-weather gear.
- *Fur-lined leather + iron (rejected for v1).* Iron pushes it toward combat plate and muddies the "warmth, not
  defence" identity; adds nothing thematically.
- *Packed-ice / cold materials (rejected).* Cold-themed blocks are the wrong metaphor — you insulate *against*
  cold, you don't wear it.

**Crafting story — "sew the coat before you go."** A crafted intermediate gives the set real expedition weight
(a full set should be an accomplishment, per the owner) without a rare-material gate:

- **Insulated Hide** (`globe:insulated_hide`, intermediate) — shaped: a leather panel stitched with wool and
  string. Recommended recipe (tunable):
  ```
  L W L        L = leather   W = white wool   S = string
  W S W    →   2 × globe:insulated_hide
  L W L
  ```
  (4 leather + 4 wool + 1 string → 2 hide.)
- **Suit pieces** — the vanilla armour crafting pattern, in Insulated Hide:
  - Hood: 5 hide · Parka: 8 hide · Leggings: 7 hide · Boots: 4 hide → **24 hide for a full set**
    (= 48 leather + 48 wool + 12 string). A genuine gathering project; **numbers are a live-tunable dial**, dial
    down if playtest says it's a grind.

**Armour material (`globe:polar` ArmorMaterial).** Defence = **leather-tier** (the point is cold, not combat —
it must not trivialize fighting), modest durability, `repairIngredient` = Insulated Hide, standard equip sound.
Exact 26.2 `ArmorMaterial` record shape is the risk surface — see §9.

### 3.2 The unified ColdProtection score (how suit + leather MIX)

Replace the single "freeze-immune piece count" with a **weighted score over the four slots**, where a **suit
piece is worth more than a leather (other freeze-immune) piece**:

| Piece in a slot | Weight |
| --- | --- |
| Polar-suit piece (`globe:polar_suit` item tag) | **0.25** |
| Any other `freeze_immune_wearables` piece (leather, datapack-added) | **0.125** |
| Empty / non-freeze-immune | 0 |

Then:
- `damageMultiplier = 1 − clamp01(totalWeight)` — how much freeze damage still lands.
- `fullyProtected ⇔ suitPieces == 4` (equivalently `totalWeight ≥ 1.0`, which **only** a full suit can reach —
  any leather substitution drops below 1.0 because leather is half-weight). This single predicate drives **all
  three** total-protection effects: damage 0, warning silence, and the status effect. *One evaluator, one truth.*

Worked examples:

| Outfit | totalWeight | Freeze damage taken | Fully protected? |
| --- | --- | --- | --- |
| Bare | 0 | 100% | no |
| 4 leather | 0.50 | **50%** (capped — leather can never do better) | **no** |
| 2 suit + 2 leather | 0.75 | 25% | no |
| 3 suit + 1 leather | 0.875 | 12.5% | no (**must complete the set**) |
| **4 suit** | 1.00 | **0%** | **yes** → silence + status effect |

This table **is** the answer to "how suit and leather mix": leather always helps but is strictly capped below
the set; only the full suit crosses into total protection. The suit's own partial curve (1–3 suit pieces = strong
partial) falls straight out of the same weights (0.25/0.50/0.75).

**Pure-core change (spec):** `ColdProtection` gains a two-argument form,
`damageMultiplier(int suitPieces, int leatherPieces)` and `boolean fullyProtected(int suitPieces)` (= `>=4`);
the old single-arg methods can delegate (suitPieces=0) or be replaced. Server + client shims each grow a
`polarSuitPieceCount` read (a new `ItemTags`/`globe:polar_suit` tag membership check) beside the existing
`freeze_immune_wearables` count, and pass both to the core. **The suit items are ALSO members of vanilla
`freeze_immune_wearables`** (so the suit correctly grants vanilla powder-snow immunity too); the `globe:polar_suit`
tag is what distinguishes them for the higher weight, so a suit piece counts as a suit piece, not double-counted
as leather.

---

## 4. Design question 2 — the Warning Matrix

The owner's silence law generalizes the current "hypothermia-only suppression" to a full-outfit matrix. Rows are
outfit states (mutually exclusive by `fullyProtected` / any-leather / bare); the goggles column is **independent**
and composes with every row.

| Outfit state | APPROACH/BLIZZARD/DANGER/LETHAL text | HYPOTHERMIA "bite" line | Leather line | Vignette (edge frame) | Cold-Protection status effect |
| --- | --- | --- | --- | --- | --- |
| **Full suit** (fullyProtected) | **SILENCED (all)** | SILENCED | — | **none** (free: no rung ⇒ no episode ⇒ no vignette) | **shown** |
| **Partial suit** (1–3 suit, no leather) | fire normally (honesty) | fires | — | fires (DANGER/LETHAL) | none |
| **Leather** (any leather, not full suit) | fire normally | fires | **fires once/zone-entry** | fires | none |
| **Bare** | fire normally; LETHAL = honest "freezing to death" | fires | — | fires | none |
| **+ Goggles** (any row above) | *unchanged* | *unchanged* | *unchanged* | **REMOVED** | *unchanged* |

Key decisions:
1. **Full suit = total silence** (new). `evaluateLadder` gains a `fullSuit` flag: when true it suppresses *every*
   rung's FIRE while still advancing `highestFired` (so nothing retries), and the removal whisper owns "you just
   lost your suit here." This is the generalization of today's HYPOTHERMIA-only suppression (`PolarColdCues.java:147`)
   to all rungs.
2. **Partial suit stays honest.** 1–3 suit pieces reduce damage but the place is still lethal, so the rungs still
   describe it. No status effect, no special line — the reduced damage is the reward; the warnings are the truth.
3. **The leather line** (`"Your leather armour provides some protection against the bitter cold."`) — a new
   whisper (LatitudeWhisperOverlay family, like the removal/frozen-wounds whispers), fired **once** on first
   crossing the frostbite onset (`FROSTBITE_ONSET_DEG` 85 — where cold begins to bite and protection first
   matters) while wearing leather and NOT fully suited; re-arms on retreat below `RETREAT_REARM_DEG`
   (`PolarColdCues.java:96`), exactly like the ladder. It **accompanies** the honest HYPOTHERMIA rung (leather
   wearers still feel the bite — they just get a reassuring note), it does not replace it.
4. **The LETHAL protected-text swap RETIRES** (finding). `POLE_LETHAL_PROTECTED_TEXT = "The bitter cold envelops
   you."` (`GlobeWarningOverlay.java:85`) exists because today a full-leather player negates damage yet still sees
   LETHAL — so the line must not claim they're "freezing to death." Under the new matrix, the **only** state that
   negates damage is the full suit, and the full suit **silences LETHAL entirely** — so this swap line has **no
   remaining consumer**. Recommendation: retire it (delete the branch + `lethalTextProtected`), or keep it as
   dead-safe with a comment. Either way, `lethalTextProtected` is no longer wired to any live path. The verbatim
   DANGER/LETHAL bare-player lines stay **untouched** (owner-locked copy).
5. **"DANGER verbatim-locked" is preserved** — the matrix only changes *whether* a rung fires (by outfit), never
   the *words* of the bare-player rungs.

---

## 5. Design question 3 — the Cold Protection status effect

The owner wants the full suit's only feedback to be an **inventory-menu status effect**, not a chat/HUD message.

**Recommendation: a registered beneficial `MobEffect` `globe:cold_protection`.**
- **Beneficial**, **hidden particles**, **ambient**-style (so it renders as a clean inventory icon with no
  swirling particle cloud on the player). No attribute modifiers — it is purely an *indicator*; the actual
  damage negation is the `ColdProtection` score, not the effect (the effect must never become the mechanism, or
  it could be dispelled/milk'd off and desync from the armour truth).
- **Equip-driven, server-applied, self-refreshing.** Each server tick (or on the existing polar tick cadence),
  if `fullyProtected(player)` → apply/refresh `cold_protection` with a short duration (e.g. 2 s / 40 ticks) and
  no ambient particles; the natural lapse when the set breaks removes it within that window. This mirrors how
  the existing polar debuffs are applied server-side. It shows in the inventory effects panel and the HUD effect
  bar exactly like any vanilla effect — which is precisely the "status effect inside the inventory menu" the
  owner described.
- **First custom effect icon needed** — `assets/globe/textures/mob_effect/cold_protection.png` (18×18). This is
  the mod's first effect icon; programmer-art acceptable for v1 (see §7).

Why an effect and not a HUD glyph: the owner said "a status effect inside the inventory menu," and vanilla's
effect registry gives that surface for free (inventory panel + effect HUD + `/effect` interop) with a single
registration.

---

## 6. Design question 4 — the Goggles interaction

**Owner ask:** worn snow goggles remove the **warning vignette** (the `PolarVignetteOverlayHud` edge frame),
keeping the text warnings per the matrix.

**The head-slot clash (and its resolution).** Both the suit **hood** and standalone **goggles** want the HEAD
slot; you can't wear both. Resolution (recommended):
- **The suit hood has the goggle visor built in.** The vignette-removal (and, later, the S12 sight kit) fires for
  a player wearing **`globe:polar_hood` OR `globe:snow_goggles`** in the head slot. A full-suit traveller gets
  clear sight for free; the standalone goggles are the cheap early-game option for someone who hasn't sewn the
  suit yet (sight relief WITHOUT the warmth — a real, honest trade).
- This means a lone-goggles player wears goggles *instead of* a suit hood → only 3 suit pieces possible → not
  fully protected → still gets text warnings + damage. Correct and intended: goggles are sight, not warmth.

**Implementation shape.** A client read `PolarColdClient.wearsGoggleSight(mc)` = head slot is
`globe:snow_goggles` or `globe:polar_hood`. `PolarVignetteOverlayHud` early-returns (draws nothing) when it's
true. The **text ladder is untouched** — goggles gate only the vignette render, so the matrix's text column is
unaffected (goggles compose with every row). Purely client-side, no netcode (own-player armour is
client-visible, like `coldProtectionPieces`).

**Relationship to S12.** The S12 sight kit (fog-cap relax + whiteout RING + blizzard-particle cone filter,
`phase5-b7-pole-passage-design-20260713.md:751-763`) **composes on the same worn-goggle read** but is a larger
rendering build. Recommendation (§8): B-10 ships the goggles ITEM with its v1 power = vignette removal only; the
fog/particle sight kit rides a follow-up slice keyed on the identical read, so nothing is wasted.

**Goggles recipe** (cheap, early): glass panes for lenses on a leather strap —
```
_ _ _
G L G    →   globe:snow_goggles      (G = glass_pane, L = leather, S = string)
S _ S
```
(2 glass_pane + 1 leather + 2 string.) Tunable; the point is that it's early and forgiving, unlike the suit.

---

## 7. Design question 5 — Assets and scope

**First custom items in the mod's history.** The asset bill (all under `src/main/resources/assets/globe/` +
`data/globe/`):
- **Item models + inventory textures** — `models/item/*.json` + `textures/item/*.png` for hood/parka/leggings/
  boots/goggles/insulated_hide (16×16 icons).
- **Worn armour layer textures + equipment asset** — the 26.2 equipment pipeline: an equipment JSON under
  `assets/globe/equipment/polar.json` referencing humanoid layer textures under
  `textures/entity/equipment/humanoid/polar.png` (+ leggings layer). **This is the volatile 26.2 surface — see
  §9.**
- **Effect icon** — `textures/mob_effect/cold_protection.png` (18×18).
- **Lang** — new keys appended to the existing `assets/globe/lang/en_us.json` (item names, effect name, the
  leather line — actually the leather line lives as a warning-copy constant in `GlobeWarningOverlay`, matching
  the existing rung copy pattern, not lang).
- **Recipes + tags** — `data/globe/recipe/*.json` (insulated_hide, 4 suit pieces, goggles) + a `globe:polar_suit`
  item tag + adding the suit items to `minecraft:freeze_immune_wearables` via
  `data/minecraft/tags/item/freeze_immune_wearables.json`.

**Programmer-art first pass is acceptable** (state it): the mechanic (protection curve, matrix, status effect,
goggles) is what needs proving; a recoloured leather-armour palette + a simple goggle icon + a snowflake effect
icon are fine for the first flight. A polished art pass can follow once Peetsa signs off on behaviour.

**v1 scope fence:**
- **IN:** the 4-piece suit, Insulated Hide, snow goggles (vignette-removal power only), the Cold Protection
  effect, the leather demotion, the warning matrix, recipes, creative-tab entry.
- **OUT (later):** the **drysuit** (water-cold, parked S7); the **expedition tonic** potion (parked S1); the full
  **S12 goggle sight kit** (fog-cap relax + whiteout ring + particle cone filter — a separate rendering slice on
  the same worn-goggle read). Goggles ship **in this phase** (their vignette power is cheap and owner-requested)
  but their *sight* power is deferred — recommended so the item and its head-slot resolution land now and the
  bigger render work isn't a B-10 blocker.

**Creative tab / unlock.** Add the six items to a single `globe:latitude` creative tab (the mod's first custom
tab — or append to an existing vanilla tab if simpler for v1). Recipe unlock: standard recipe-book advancements
(unlock insulated_hide on obtaining leather; unlock suit pieces on obtaining insulated_hide) — no bespoke gating.

---

## 8. Design question 6 — Sequencing and the flag family

**One phase, atomic, no gap.** The leather demotion, the suit, the warning matrix, and the status effect ship
**together** under a single master flag. This is the only way to satisfy the sequencing law (leather demotes ONLY
when the suit exists):

- **`latitude.polarOutfitting.enabled`** (master, default **OFF** — staged-ON branch-local for the flight per the
  B-6/B-7/B-8 precedent at `LatitudeV2Flags.java:184,301`, "REVISIT BEFORE MERGE").
  - **OFF** ⇒ **leather keeps today's full-negation behaviour exactly** (`negatesFreezeDamage` at 4 pieces),
    warnings behave as today, the new items are registered-but-inert (no recipe, not in creative tab, grant only
    vanilla freeze-immunity, no mod cold-protection weight, no status effect). **No protection gap.**
  - **ON** ⇒ leather demotes to the 0.125 weight (partial, capped 0.5), the suit provides the 0.25 weight + total
    protection at 4/4, the warning matrix applies, the leather line fires, the status effect applies.
- **Goggles** ride the **same master flag** (they're one family). Only split them behind a sub-flag
  (`latitude.polarOutfitting.goggles`) if the S12 sight kit needs to lag the vignette power on a different
  cadence — not needed for v1 (v1 goggles = vignette removal only, cheap).

**Items register UNCONDITIONALLY; only behaviour/obtainability is flag-gated** (recommendation — mirrors the
barrens precedent `LatitudeV2Flags.java:292-297`). Rationale: **registries must be consistent across
sessions**. If item registration were flag-gated, a world saved with a suit piece in a chest/inventory and then
reopened flag-off would reference a **missing item** (item → air, permanent loss / log spam). So the `Item`s and
the `MobEffect` register every launch; the FLAG gates the recipes (data recipes can be conditionally disabled, or
simpler: recipes always load but the *behaviour* — cold weight, matrix, effect, leather demotion — is what the
flag switches, and the creative-tab visibility). **Byte-identity is not even in question for items** — items are
not worldgen, so "flag-off = byte-identical" is trivially satisfied on the worldgen axis; the only thing flag-off
must preserve is **today's leather freeze behaviour**, which it does by leaving `ColdProtection` on its
single-arg full-negation path.

**L17 discipline:** the master flag (and any dial) is born WITH its `build.gradle` client-run forwarding line in
the same pass (pattern at `build.gradle:138` / `:158`). Since B-10 is presentation/items (no worldgen), only the
`runClient` forwarding is needed, not `runBiomePreview`.

**Proof gates for this phase** (presentation/items, not worldgen):
1. Pure `ColdProtectionTest` extended for the two-arg curve + `fullyProtected` (the mixing table in §3.2 becomes
   assertions).
2. Pure `PolarColdCuesTest` extended for the full-suit total-silence path + the leather-line fire/re-arm
   predicate.
3. Flag-OFF behaviour test: full leather still negates (today's contract preserved).
4. Live flight (the real gate — inventory status effect visibility, the leather line timing, goggles vignette
   removal, the head-slot hood-visor behaviour all need eyes): craft a set on a fresh polar world, walk the
   85→90 ladder bare / leather / partial-suit / full-suit / goggles.

---

## 9. Biggest 26.2 item-registration risk

**The mod has never registered a Java game object, and 26.2's item/armour/equipment API is the part that has
churned hardest across recent Minecraft versions.** Concretely, the unknowns to de-risk against the actual
mapped 26.2 jar *before* committing to an API shape:

1. **Item identity in `Item.Properties`.** Since 1.21.2, items must carry their registry key in their
   `Properties` (`setId(ResourceKey)`) before construction, and registration order/format changed. Getting this
   wrong throws at registry-freeze (hard crash on load), not at compile.
2. **`ArmorMaterial` is a data-carrier record + an equipment ASSET, not the old `Layer` texture path.** 26.2
   armour worn-textures resolve through an **equipment asset** (`ResourceKey<EquipmentAsset>` on the material →
   `assets/globe/equipment/polar.json` → humanoid layer textures under
   `textures/entity/equipment/humanoid/`), and armour behaviour is an **`EQUIPPABLE` data component** rather than
   a distinct `ArmorItem` subclass (the `ArmorItem` class was folded into `Item` + component in recent versions).
   The exact record fields (defense-per-`ArmorType` map, toughness, knockback resistance, `Holder<SoundEvent>`
   equip sound, `equipAssets` key, repair-ingredient tag) must be read off the 26.2 jar — this doc names the
   shape from cross-version knowledge but **the field list and constructor are the single most likely thing to be
   subtly wrong**.
3. **`MobEffect` registration + client icon binding.** First custom effect; the beneficial/particle-hiding
   flags, the ambient rendering, and the effect-icon texture path convention (`textures/mob_effect/…`) need
   confirming against 26.2.
4. **Creative tab registration** API shape (Fabric `FabricItemGroup` vs vanilla `CreativeModeTab.builder`).

**Mitigation:** the first build step should be a throwaway "register one item + one effect, see it in-game"
spike against the live 26.2 jar (decompile `minecraft-merged-deobf-26.2.jar` for the exact `Item.Properties` /
`ArmorMaterial` / `MobEffect` signatures, as the terrain-wrapper work did for density functions), *then* build
the six items on the proven pattern. Do not design the full asset bill against assumed signatures. Everything in
`core/` (the `ColdProtection` reweighting, the `PolarColdCues` matrix, the leather-line predicate) is pure Java
with zero MC imports and can be built + unit-proven **independently of and ahead of** the risky registration
work — recommend building the core first so the mechanic is proven while the registration spike de-risks the
plumbing.

---

## 10. Open owner dials (settle at build/flight)

- Insulated-hide recipe yield + per-piece hide counts (§3.1) — grind vs accomplishment.
- Leather-piece weight 0.125 (full-leather cap 0.5) — how weak should demoted leather feel?
- Leather line onset: frostbite 85 (recommended) vs the 80 APPROACH rung.
- Goggles: same master flag (recommended) vs own sub-flag.
- Status-effect refresh cadence + whether it shows an ambient particle (recommend none).
- Suit defence tier: leather-tier (recommended) vs slightly-above.
- Creative tab: own `globe:latitude` tab vs append to a vanilla tab for v1.
- Retire `POLE_LETHAL_PROTECTED_TEXT` outright vs keep dead-safe (§4.4).

---

## 11. Recommendation (3 sentences)

Ship **one atomic B-10 phase** behind `latitude.polarOutfitting.enabled` (default off, no protection gap): a
four-piece **Polar Suit** sewn from a new **Insulated Hide** (leather + wool + string), a unified `ColdProtection`
score where a suit piece (0.25) outweighs a leather piece (0.125) so **only a full 4/4 suit reaches total
protection** — zero freeze damage, total warning + vignette silence, and a registered beneficial **Cold
Protection `MobEffect`** as the sole inventory-visible feedback — while leather demotes to a capped-partial
softener that always keeps the warnings honest and earns a once-per-zone reassurance line. **Snow goggles** ship
the same phase (head slot, with the suit hood carrying a built-in visor to dodge the slot clash), their v1 power
being warning-vignette removal only, with the fuller S12 sight kit and the drysuit/expedition-tonic deferred. The
**material/recipe pick is fur-lined leather (leather + wool), fully temperate-gatherable before the march north**,
and the **biggest risk is that this is the mod's first-ever Java content registration** against 26.2's
hard-churned `Item.Properties` / `ArmorMaterial` / `EQUIPPABLE`-component / equipment-asset pipeline — de-risk
with a one-item spike against the decompiled 26.2 jar before building the full asset bill.

**Doc path:** `docs/binder/b10-polar-outfitting-design-20260718.md`
