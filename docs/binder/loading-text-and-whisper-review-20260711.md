# Loading-screen copy pass + whisper-position decision — creative-director review (2026-07-11)

READ-ONLY review. No `src/` changes. Part 2 (whisper position) is a DELEGATED DECISION — Peetsa
handed the call to the creative director; the VERDICT at the end is meant to be implemented verbatim.

Grounded in:
- `mixin/client/LevelLoadingScreenLatitudeOverlayMixin.java` — the bespoke parchment loading card
  (PHRASES array L58–111; featured-block bias L113–156; truthful stage lines set in the tick mixin
  L591 / L615–617; specs line drawn L256–259; hint L262).
- `client/create/LatitudeWorldLauncher.java:103` — where the specs line string is built.
- `client/LatitudeWhisperOverlay.java` — the linger "whisper" line (offset L31, render math L106).
- Sibling positions for the whisper call: zone title default `zoneEnterTitleOffsetY = -40`
  (`core/config/LatitudeConfigData.java:172`), hemisphere title `ANCHOR_OFFSET_Y = -40`
  (`HemisphereTitleOverlay.java:27`), hazard-warning band at `screenH - 68`
  (`GlobeWarningOverlay.java:333`), and the whisper is itself triggered from that same warning file
  (`GlobeWarningOverlay.java:536`).

---

# PART 1 — LOADING SCREEN TEXT PASS

## What actually shows today

While a Latitude world builds, the brown parchment card shows, top to bottom: the gold **LATITUDE**
wordmark; the **specs line** (e.g. `Regular · Wide 2:1 · 40,000 × 20,000 · tropical start`) with a
slow "reading light" that lights one word at a time; the **hint** `Press F9 in-game for HUD options`;
the wandering-needle compass; **one rotating funny phrase** that swaps every 4.8 s with a fade; the
gold progress bar; and a **truthful stage line** underneath it (`Shaping the world…`,
`Placing you on the map…`, `Laying out the nearby land…`, `Painting the horizon…`).

There are **51 funny phrases**. The last 19 are the "featured" block: the card always opens on one of
those, and each swap is ~70% biased back into them (L115, L141). So on a fast load a player often sees
only ONE phrase, and it's almost always a featured one. That means the featured 19 carry nearly all
the visible load — the effective variety is much lower than 51.

## Findings, ranked

### 1 — The featured block is lopsided: two big look-alike clusters crowd out the signature lines (biggest copy problem)
Because the featured 19 do most of the on-screen work, their internal balance matters more than the
raw count of 51. Two clusters dominate them:

- **The mountain/peak cluster — 7 lines saying almost the same thing:** "Trimming the treeline",
  "Talking trees down from the peaks", "Giving the mountaintops a trim", "Capping peaks with snow",
  "Frosting the summits", "Dusting the peaks with powder", "Minding the alpine line". Three of those
  (Capping/Frosting/Dusting) are literally "put snow on peaks" three times; two more (Talking trees
  down / Giving the mountaintops a trim) repeat "Trimming the treeline". The code even has a special
  guard (`globe$isPeakThemed`, L158) to stop two peak lines landing back-to-back — that guard is a
  symptom: there are simply too many peak phrases.
- **The "we host other mods' biomes" cluster — 6+ near-duplicates:** "Welcoming guest biomes",
  "Evicting a few biome squatters", "Finding homes for visiting biomes", "Making room for everyone's
  biomes", "Packing extra biomes for the road", plus "Plentifying biomes". All one idea.

Net effect: a repeat world-creator keeps seeing snow-on-peaks and room-for-biomes, while the mod's
sharpest, most on-brand lines (compass, equator, trade winds, magnetic north) sit in the *non*-featured
majority and rarely surface.

**What Peetsa would see:** thinning each cluster to its 2 best and promoting distinct cartography lines
into the featured tail makes every load feel less repetitive without adding much text.

### 2 — A batch of phrases are generic "funny Minecraft loading" gags that don't say *Latitude*
The expedition/cartography voice is the whole personality of this card. These lines are off that voice —
they're the same mob gags any mod could ship, and they're the tiredest of the community clichés:
"Defusing creepers", "Cloning sheep", "Training parrots", "Waking up foxes", "Herding cows inland",
"Convincing bees to pollinate". They cost nothing to read but they spend a slot that a latitude/compass
line would use to reinforce the brand. Keep a *little* whimsy for levity (Peetsa likes fun) — just cut
the most generic ones and let the mapmaker jokes ("Teaching villagers cartography" is the model) carry it.

### 3 — Two real word problems and several exact near-dupes
- **"Plentifying biomes"** — "plentifying" is not a word. Reads as a typo/AI-ism. Rewrite or cut.
- **"Untangling Terralith's roots"** — name-drops a specific dependency. Two issues: it's meaningless
  to anyone who doesn't run Terralith, and it faintly implies packs are required, which cuts against
  the standing **vanilla-first HARD RULE** (packs are enrichment, never required). Make it pack-agnostic.
- **Exact near-dupes to collapse:** "Planting bamboo groves" + "Watering bamboo groves" (two bamboo
  lines); "Freezing the poles" + "Freezing polar seas" (two frozen-pole lines); "Surveying the horizon"
  + "Stretching the horizon" (two horizon lines — these two are the mildest, different enough to keep).

### 4 — The mod's single best naming hook is under-used: *latitude itself*
The list leans on generic geology (tectonic plates, continents, riverbeds) and mobs, but barely touches
the fantasy the mod is literally named for: navigating by **latitude** — parallels, meridians, the
tropics, taking a sun-sighting with a sextant. That's the richest untapped seam and it's exactly
on-brand. This is my top *additive* note (see suggested new lines below).

### 5 — Specs line: strong, one optional new-player nit
`Regular · Wide 2:1 · 40,000 × 20,000 · tropical start` is scannable and the reading-light animation
draws the eye through it nicely. For a brand-new player the only soft spot is the bare dimensions:
`40,000 × 20,000` has **no unit**, so a newcomer may not know these are blocks. Optional: append the
word once → `40,000 × 20,000 blocks`. Everything else (size name, "Wide 2:1", "tropical start") is clear
enough and teaching the climate-zone vocabulary isn't the loading card's job. **Low priority.**

### 6 — Hint: fine, one word is jargon
`Press F9 in-game for HUD options` — "HUD" is gamer shorthand a first-timer may not parse. Since this
card is often a new player's *first* Latitude screen, naming it in world-terms reads warmer:
`Press F9 in-game to customize your compass & titles` (or `…for on-screen display options`). It already
sits in the muted color so it recedes politely. **Low priority polish, not a defect.**

### Not a problem — leave alone
The **four truthful stage lines** ("Shaping the world…", "Placing you on the map…", "Laying out the
nearby land…", "Painting the horizon…") are excellent: honest, plain-language, and already in the
map/horizon voice the funny phrases should aspire to. **Keep all four unchanged** — they're the tonal
benchmark for the rest of the card.

## Per-phrase verdicts

**KEEP (24 — on-voice and distinct):** Charting the frontier · Following the compass · Crossing climate
bands · Calibrating the equator · Warming the tropics · Freezing the poles · Packing snow boots ·
Surveying the horizon · Stacking tectonic plates · Folding mountain ranges · Teaching villagers
cartography · Nudging continents apart · Polishing compass glass · Stretching the horizon · Carving
river valleys · Mapping trade winds · Tuning the jet stream · Rotating the planet · Aligning magnetic
north · Sorting biomes by latitude · Trimming the treeline · Minding the alpine line · Unrolling more
map · Giving the compass more to point at.

**KEEP for texture/levity (7 — generic but pretty or faintly on-theme):** Planting bamboo groves ·
Planting spruce forests · Hiding ancient ruins · Dusting off badlands · Sprinkling wildflowers ·
Filling oceans carefully · Talking trees down from the peaks *(the one charming peak line — keep it as
the whimsical treeline voice, cut its duller siblings)*.

**CUT (14 — generic mob gags + redundant cluster dupes):** Defusing creepers · Cloning sheep · Training
parrots · Waking up foxes · Herding cows inland · Convincing bees to pollinate · Watering bamboo groves
*(dupe)* · Freezing polar seas *(dupe of Freezing the poles)* · Giving the mountaintops a trim *(dupe)*
· Frosting the summits *(dupe)* · Dusting the peaks with powder *(dupe)* · Finding homes for visiting
biomes *(dupe)* · Packing extra biomes for the road *(dupe)* · Making room for everyone's biomes
*(dupe — keep "Welcoming guest biomes" + "Evicting a few biome squatters" as the two guest-biome lines)*.

**REWRITE (6):**
- "Plentifying biomes" → **"Filling out the biome bands…"** (real words; names the more-biomes-per-band feature)
- "Untangling Terralith's roots" → **"Untangling the guest biomes…"** (pack-agnostic; keeps the whimsy)
- "Capping peaks with snow" → keep as the *single* snow-on-peaks survivor, reworded prettier:
  **"Frosting the highest peaks…"** (one snow line, not three)
- "Welcoming guest biomes" → keep, tighten to **"Welcoming the guest biomes…"** (matches the reworked pair)
- "Evicting a few biome squatters" → keep (funny, and it names the real over-representation trim) — no change
- Specs dims → optionally **"40,000 × 20,000 blocks"** (Finding 5)

Result after keep/cut/rewrite: ~37 phrases, every cluster down to its 2 best, more room in the featured
tail for signature lines.

## New phrases worth adding (expedition / navigate-by-latitude voice)
Lean into the mod's actual name. Strongest first:
- **"Ruling in the parallels…"** / **"Drawing the latitude lines…"** — the literal feature.
- **"Taking a sun-sighting…"** / **"Shooting the sun with the sextant…"** — latitude-by-navigation romance.
- **"Boxing the compass…"** — real nautical term (reciting every point); pure on-brand whimsy.
- **"Setting the prime meridian…"** / **"Drawing the meridian…"** — ties to the hemisphere/E-W feature.
- **"Marking the tropic lines…"** — Tropic of Cancer/Capricorn wink, reinforces the climate bands.
- **"Inking the coastlines…"** / **"Naming the new coastlines…"** — mapmaker flavor.
- **"Unfurling the parchment…"** / **"Weighting down the map corners…"** — ties to the parchment card itself.
- **"Plotting your heading…"** — sits nicely next to the wandering-needle compass on the same card.

**Placement note (so new lines actually show):** only the *last* `FEATURED_PHRASE_COUNT = 19` entries
lead and get the 70% bias. Any new signature line added to the *front* of the array will almost never
appear. Add the new latitude/compass lines to the **tail** of `PHRASES` (or bump
`FEATURED_PHRASE_COUNT`) so they surface.

## Effort sizing (Part 1)
- **S** — Cut the 14, apply the 6 rewrites: pure string edits in one array, no logic change.
- **S** — Add 6–8 new latitude/compass lines to the tail of `PHRASES`.
- **S** — Optional: `blocks` unit on the specs dims (`LatitudeWorldLauncher.java:104` format string) and
  the hint reword (`LevelLoadingScreenLatitudeOverlayMixin.java:262`).
No new draw code, no timing changes — this whole part is copy.

---

# PART 2 — WHISPER POSITION DECISION (delegated to the creative director)

## What renders today
The whisper is a small, italic, translucent line (peak alpha capped at 70%, `MAX_ALPHA` L29) that
appears when a player lingers back across a zone/hemisphere line they just crossed. It draws centered
at **`screenH / 2 + 34`** (`ANCHOR_OFFSET_Y = 34`, L31; render L106). Peetsa: *"it's a little too high.
lower it a bit."*

## The vertical neighborhood (why "a little" is the right instinct, not "a lot")
Everything else that can share the screen with the whisper:
- **Crosshair** — dead center. The whisper at +34 already clears it comfortably. Good.
- **Zone title** and **hemisphere title** — both anchor **40 px ABOVE** center (`-40`). The whisper is
  well below them; no conflict. Good — a murmur belongs opposite the shout.
- **Hazard-warning band** — draws at **`screenH − 68`** (a fixed distance up from the bottom), and the
  whisper is *triggered from the very same file* as those warnings (`GlobeWarningOverlay.java:536`), so
  a boundary murmur and a hazard warning can genuinely both be on screen at a pole. This is the real
  floor: push the whisper too low and it collides with the warning band, stacking two messages.
- **Vanilla held-item name** popup sits near `screenH − 59`; another reason not to crowd the bottom.

Convention in games is that quiet secondary text lives in the **lower third** (~65–75% down) — present
but out of the eye's center path. The whisper at +34 lands only ~62% down on a typical card (a common
GUI height ~270 px → center 135, whisper 169), which reads as mid-screen rather than lower-third. So
Peetsa's read is correct: it *is* a touch high. But the warning band below caps how far down it can
safely go — the answer is a **modest** drop, not a relocation to the bottom.

## The math on the nudge
On a typical GUI height (~270 px): center 135, warning band at 202. On a *small* window (~240 px):
center 120, warning band at 172 — the tightest case. To keep a safe gap above the warning band even on
small windows, the offset should stay around 40–42:
- **+42** → small window: whisper 162 vs warning 172 = 10 px gap (safe); typical: 177 vs 202 = 25 px;
  1080p (height ~360): 222 vs 292 = 70 px. Lands ~66% down = into the lower third, still clearly below
  the crosshair and far below the above-center titles.
- (+48 or lower tips into a ~4 px gap on small windows — too close to the warning band. Rejected.)

So +42 is the sweet spot: an 8 px drop — one clean line of movement, visibly lower, "a little" exactly
as asked — that pushes the murmur into lower-third territory while preserving a safe margin above the
hazard-warning band at every common resolution.

## VERDICT
**Lower the whisper. Change `LatitudeWhisperOverlay.ANCHOR_OFFSET_Y` from `34` to `42`** (whisper draws
at `screenH / 2 + 42`). This drops it one line into the lower-third read Peetsa wanted, while holding a
safe gap above the hazard-warning band (`screenH − 68`) even on small windows so a boundary murmur and
a hazard warning never stack. Do not go lower than 42 — below that the two overlays begin to crowd.
