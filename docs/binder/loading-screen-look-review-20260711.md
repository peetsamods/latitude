# Loading-screen LOOK review — visual hierarchy of the Latitude loading card

**Date:** 2026-07-11
**Reviewer:** Creative Director (read-only art-direction pass)
**Surface:** `src/main/java/com/example/globe/mixin/client/LevelLoadingScreenLatitudeOverlayMixin.java`
(bespoke loading overlay). Compared against `LatitudeCreateWorldScreen.drawLatitudeWordmark`
(`LatitudeCreateWorldScreen.java:1991`).
**Status:** PROPOSED — read-only. No `src/` changes. This is the LOOK follow-up to the copy pass
(`loading-text-and-whisper-review-20260711.md`, same day).

Peetsa's ask: she likes the scrolling phrases, but wants the *other* text looked at — the specs line
("Wide 2:1 · Regular · 40,000 × 20,000 …") and whether the **LATITUDE** title should be emphasized.

---

## 1. Current-state summary — what actually renders today (grounded)

The overlay covers the vanilla chunk grid with an opaque-ish dark card, then paints text on it. Every
element sits on the **same dark substrate**, so backdrop worst-cases (bright sky, polar whiteout, caves,
sunset) barely reach the text — a genuine strength (see §2). Top-to-bottom (pane origin `paneY`,
pane up to 340×200):

| # | Element | Where (L#) | Treatment today |
|---|---------|-----------|-----------------|
| — | Atlas frame | L263 | Shared `LatitudePlanisphereRenderer.drawAtlasFrame` — the SAME parchment frame the create screen uses. Family tie already exists. |
| — | Brown pane + border | L266–268 | `PANE_BG 0xE62C2420` (~90% alpha), `PANE_BORDER 0xFF5C4A3A` |
| — | Grid graticule | L271, L556 | `GRID_COLOR 0x14504840` = **~8% opacity** warm-grey lattice, 16px step |
| 1 | **LATITUDE** title | L276 | `GOLD 0xFFE8B64A`, **standard 9px font, shadow on, centered** — plain text |
| 2 | Specs "passport" line | L281 | `WARM_WHITE 0xFFEDE0D0`, body size, with the reading-light word-wave |
| 3 | F9 hint | L285 | "Press F9 in-game for HUD options", `MUTED 0xFF8C8078`, no shadow |
| 4 | Compass (hero) | L291 | r≈28, Sunset scheme + Rose shape, animated wandering coral needle |
| 5 | Rotating phrase | L295 | `WARM_WHITE`, fades in/out over 4.8s |
| 6 | Progress bar | L304–308 | 3px, `GOLD` fill on `0xFF1A1410` track, ≤160px |
| 7 | Stage line | L314 | `MUTED`, no shadow |

Specs string built at `LatitudeWorldLauncher.java:103`:
`"%s · %s · %,d × %,d · %s start"` → e.g. **"Regular · Wide 2:1 · 40,000 × 20,000 · Tropical start"**.
All four `·`-segments render at one warm-white weight; only the reading-light wave lifts one at a time.

**The create-screen wordmark, by contrast** (`drawLatitudeWordmark`, L1991): 1.5× scale, +2 letter-spacing,
dark-bronze letterpress under-layer, a slow gold **bloom breath** (`RulesIcons.glow`, 3.4s), **4 twinkling
sparkle motes**, and **flanking gold rules with diamond tips**. It exists *specifically* because Peetsa
said (TEST 29, cited in the method's own doc-comment) "I thought 'LATITUDE' at the top was going to look a
little more **special**."

---

## 2. Findings, ranked (readability first)

**Readability is fundamentally SOUND here** — because the opaque pane (`0xE62C2420`, ~90% alpha) is a
constant dark substrate, none of the real-world backdrops threaten the text. Estimated contrast on the
composited pane: WARM_WHITE ≈ 11:1, GOLD ≈ 8:1 (plus shadow), MUTED ≈ 3.9:1 (fine for a secondary hint).
This is good defensive design and nothing below is a legibility emergency. The findings are hierarchy and
coherence, which is exactly what Peetsa asked about.

**F1 — HIGH — the mod's most-seen nameplate is its LEAST special one (incoherence with the create screen).**
The create screen — configured once — gets the glowing, letterspaced, rule-flanked wordmark. The **loading
screen — seen on EVERY world entry, the brand's true front door — gets plain 9px gold text** (L276). That's
backwards, and it's the exact instinct Peetsa already acted on for the create screen. Visually the eye is
pulled first by the big animated coral **compass**, then the gold progress bar, then the moving phrase —
the *title* loses to everything. On its own loading screen the nameplate should not be the quietest word.

**F2 — HIGH — two different golds for the same wordmark across the two screens.**
Loading `GOLD = 0xFFE8B64A` (L36) vs create-screen `GOLD = 0xFFD4A74A` (`LatitudeCreateWorldScreen.java:57`).
Same brand mark, two golds. Any reuse must pick ONE. (The zone-title review the same day also standardized
on `0xE8B64A` — that's the value to converge on.)

**F3 — MEDIUM — the "passport" line is under-leveraged; every token reads at one weight.**
The specs line is the one piece of *true, personal* information on the screen — this is YOUR world, these
are its real dimensions. Today "Regular", "Wide 2:1", "40,000 × 20,000" and "Tropical" all read identical
warm-white. The **shape token especially ("Wide 2:1") is the headline of the whole 2:1 pivot** — and Peetsa
herself flagged (comment at `LatitudeWorldLauncher.java:96`) that the dims alone don't say which shape they
belong to. Gilding the *qualitative* tokens (shape + zone) while keeping the *numerals* warm-white would
colour-code "what kind of world" vs "how big," and tie the passport to the gold identity. The reading-light
wave still works — `LoadingWave.shade` operates on any base RGB, so gold segments simply wave in gold-space.

**F4 — MEDIUM — the F9 hint sits inside the identity lockup, where it doesn't belong.**
The hint (L285) is glued directly under the passport at `paneY+36`, so the top ~45px reads
Title → Passport → *utility instruction*. It's the least important text on the screen but it's crowding the
most important group. It isn't a brightness fight (it's properly muted) — it's a **grouping** problem. Move
it to the bottom "mechanics" zone with the progress bar and stage line, so the top is a clean identity
lockup.

**F5 — LOW — the graticule earns its concept but barely registers; a cheap identity tie is going unused.**
The 8%-opacity warm-grey lattice (L556, `0x14504840`) is conceptually perfect — a map graticule is core
Latitude identity — but at that opacity over dark brown it's nearly invisible, and it's a neutral grey when
it could be a faint **gold/parchment** tint for almost free. Nudge alpha to ~12% (`0x14`→`0x1E`) and tint
toward gold. Purely optional polish; it harms nothing today.

**Non-findings (working well, leave alone):** the compass hero placement and animation; the progress bar's
gold tie; the truthful stage line; the reading-light word-wave itself (a lovely signature — keep it); the
shared atlas frame (already the right coherence move).

---

## 3. Recommendations (effort-sized)

**R1 (F1+F2) — Elevate the LATITUDE title to the create-screen wordmark, as a shared helper. Effort: M.**
*What Peetsa would see:* the same glowing, letterspaced, rule-flanked LATITUDE from the create screen now
greets her on every loading screen. Implementation direction: `drawLatitudeWordmark` is currently a private
method on `LatitudeCreateWorldScreen`; lift it into a shared static helper (the precedent is right there —
`LatitudePlanisphereRenderer.drawAtlasFrame` is already shared between exactly these two screens). Both
screens then call it 1:1 with one unified `GOLD`. **Quieter variant recommended for loading:** keep the
scale (~1.4–1.5×), letterpress, bloom breath and flanking rules, but **drop the sparkles to 0–2** (from 4)
— the loading card already animates three things (needle, phrase fade, word-wave); full twinkle would tip
into clutter. Honor Reduce Motion exactly as the create screen does (freeze bloom/lift to mid, suppress
sparkles).

**R2 (F3) — Gild the passport's qualitative tokens; keep numerals white. Effort: M.**
*What Peetsa would see:* "**Wide 2:1**" and "**Tropical**" glow gold; "Regular" and "40,000 × 20,000" stay
warm-white — the world's *character* reads gold, its *measurements* read white. Implementation: pass a
per-segment base colour into the wave draw (`globe$drawSummaryWave`, L366) — segments 1 (shape) and 3 (zone)
get GOLD as their `baseRgb`, segments 0 (size) and the numerals stay WARM_WHITE; separators unchanged. Make
sure the Reduce-Motion early-return (L367) draws the same static gold/white split, not flat warm-white.

**R3 (F3, alternative/companion) — A tiny all-caps eyebrow above the passport. Effort: S.**
*What Peetsa would see:* a small dim-gold "YOUR WORLD" (letter-spaced, ~muted-gold `0x99E8B64A`) sits just
under the wordmark, framing the specs line as a passport without adding a heavy plate. Cheapest way to add
one rung of hierarchy. Pairs well with R2; skip if R2 alone feels sufficient. (A parchment *tag/plate*
behind the specs was considered and is NOT recommended — the pane already has a frame + graticule; a plate
would over-clutter.)

**R4 (F4) — Relocate the F9 hint to the bottom mechanics zone. Effort: S.**
*What Peetsa would see:* the top of the card is a clean Title → passport lockup; the "Press F9…" hint moves
down to live with the progress bar and stage line where utility text belongs.

**R5 (F5) — Warm and slightly strengthen the graticule. Effort: S.**
*What Peetsa would see:* the faint map grid behind everything picks up a whisper of gold and reads just
barely more present — more "chart paper," less "grey static." Alpha `0x14`→`0x1E`, tint toward gold.
Optional.

---

## 4. v2 hierarchy recipe (the coherent combination to ship)

Ordering, scale, colour — top to bottom, grouped into **identity / hero / mechanics**:

**IDENTITY lockup (top, tight):**
1. **LATITUDE wordmark** — shared helper, ~1.4–1.5× scale, +2 tracking, letterpress `0xFF3A2410`, gentle
   gold bloom breath (3.4s, 0.35–0.85), flanking gold rules with diamond tips, **sparkles 0–2**. Unified
   `GOLD 0xFFE8B64A`. Reduce Motion → frozen mid, no sparkles.
2. *(optional)* Eyebrow **"YOUR WORLD"** — all caps, tiny, letter-spaced, dim gold `0x99E8B64A`.
3. **Passport specs** — reading-light wave KEPT; **shape + zone tokens gold** (`0xFFE8B64A` base),
   **size word + numerals warm-white** (`0xFFEDE0D0` base); separators dim as today. Static gold/white
   split under Reduce Motion.

**HERO (middle):**
4. **Compass** — unchanged (the brand mascot; correctly the strongest visual).
5. **Rotating phrase** — unchanged (warm-white, fade).

**MECHANICS (bottom):**
6. **Progress bar** — unchanged (gold on dark track).
7. **Stage line** — unchanged (muted).
8. **F9 hint** — moved here, muted, directly under the stage line.

**Grid:** faint gold-tinted graticule (`~0x1E5A4A38`) behind all of it.

Net effect: the eye now lands **wordmark → passport → compass → phrase → progress/stage/hint** — brand
first, your-world second, the animated compass as the held signature moment, mechanics quietly last.

---

## 5. Single highest-impact change

**Give the loading screen the create screen's real LATITUDE wordmark (shared helper, one unified gold, a
quieter sparkle count) — so the mod's most-seen surface finally leads with its own nameplate instead of
plain gold text.**
