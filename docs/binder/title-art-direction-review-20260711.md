# Zone-Title Art-Direction & Readability Review — 2026-07-11

**Scope:** creative/readability evaluation of the zone-enter title (the big "SUBPOLAR 66°N"
that appears when you cross a climate band — the mod's signature moment). READ-ONLY: this doc
proposes, it changes no code. Grounded in the actual rendering path
(`ZoneEnterTitleOverlay.java`, `core/ui/TitleStyle.java`, `core/config/LatitudeConfigData.java`,
`GlobeWarningOverlay.java`), the shipped defaults, and three of Peetsa's live screenshots
(bright-sky savanna, snowy whiteout, low-sun snow).

Plain language throughout; the technical term is in (parentheses) only where it can't be avoided.

---

## 1. Current state — what actually draws today

The title is **one line of text**, e.g. `SUBPOLAR 66°N` (zone name + latitude, built in
`GlobeWarningOverlay.buildZoneEnterTitle`, one string, one size, one color). Shipped defaults:

| Ingredient | Default | Where |
|---|---|---|
| Fill color | warm off-white / ivory `0xF3ECDD` (`OFF_WHITE`) | `LatitudeConfigData:43,90` |
| Case | ALL CAPS | `:160` |
| Letter spacing | +1 px | `:166` |
| Size (scale) | 1.8× (range 1.0–3.0) | `:82` |
| On-screen time | 6.0 s (range 2–10) | `:79` |
| Drop shadow | **ON** — soft dark cast to lower-right, stamps at (1,1)+(2,2) px, alpha 0.35/0.18 | `:130`, `TitleStyle:125,132` |
| Outline (dark keyline around letters) | **OFF** (available 1–4 px, default black) | `:107,112,118` |
| Glow halo (soft dark aura) | **OFF** (intensity slider 0.2–2.0, default 0.75) | `:141,149` |
| Glimmer (one-shot shine sweep) | **ON** — starts tick 2, lasts ~0.7 s | `:155`, `TitleStyle:137,142` |
| Fade in / out | ~0.5 s each, **straight-line** (not eased) | `ZoneEnterTitleOverlay:16,75-79` |
| Position | 40 px above screen center, draggable | `LatitudeConfigData:169,172,175` |
| Color presets | WHITE / GOLD / RED / CYAN / GREEN / CUSTOM / RAINBOW / AURORA / OFF_WHITE | `:39` |

The hemisphere title and the HUD Studio preview render through the **same** path
(`drawTitleLineAt`), so any change here improves all three at once.

**Motion timeline today** (6 s title = 120 ticks at 20/s):
`fade in 0→10t` · `glimmer sweep 2→16t` · `hold 16→110t` · `fade out 110→120t`.
Note the glimmer starts (tick 2) while the title is still ~20% faded-in — the shine partly
plays on a title that isn't visible yet.

---

## 2. Findings — ranked (readability first)

### FINDING R-1 (CRITICAL): the default off-white fill is near-invisible on bright backgrounds — and worst exactly where the signature fires most

I measured the fill against the real backgrounds in Peetsa's screenshots using the standard
contrast ratio (how many times brighter the lighter of two colors is; text wants ~3:1 minimum
for large type, 4.5:1 for comfort):

| Background | Off-white fill contrast | Verdict |
|---|---|---|
| Snow / whiteout `~0xF0FBFB` | **1.11 : 1** | invisible — same brightness as the snow |
| Bright noon sky `~0xBFD4FF` | **1.27 : 1** | almost invisible |
| Bright sky-blue `~0x9DBEFF` | **1.59 : 1** | very weak |
| Low-sun / sunset `~0xE8B27A` | **1.61 : 1** | very weak |
| Savanna grass `~0x8AA24A` | 2.43 : 1 | marginal |
| Dark cave `~0x151515` | 15.52 : 1 | excellent |

The cruel part: the **POLAR and SUBPOLAR bands are snow** (Peetsa's whiteout screenshot), and
those are the most dramatic crossings in the whole mod — and that's the 1.11:1 row, where the
ivory text and the snow are the same brightness. The only thing rescuing it today is the faded
drop shadow, and that shadow is (a) only on the **lower-right** of each letter, so the top-left
edges have no dark backing, and (b) semi-transparent (0.35/0.18), so on a bright field it reads
as thin grey, not a solid edge. A directional drop shadow is a *depth cue for text that is
already legible* — it is the wrong tool for making light-on-light text legible.

**Why an outline is the fix — and it's nearly free.** The mod already ships the machinery (a
1–4 px dark keyline around every letter), it's just **defaulted OFF**. A 1 px dark keyline gives:

| Background | 1 px dark keyline contrast |
|---|---|
| Snow / whiteout | **19.9 : 1** |
| Bright sky-blue | **11.2 : 1** |
| Sunset | **11.1 : 1** |
| Savanna | 7.4 : 1 |
| Dark cave | 1.15 : 1 (disappears — but here the off-white fill is already 15.5:1) |

The keyline and the fill cover **opposite** background brightnesses: the dark keyline carries
bright backgrounds (snow, sky, sunset), the light fill carries dark backgrounds (caves, night).
Together they're bulletproof. And the keyline reads cleanly against the ivory fill itself
(black vs off-white = 17.9:1; a dark warm brown = 15.6:1), so it looks like a crisp engraved
map label, not a smudge. **This is the single highest-impact change in this review.**

→ See recommendation **A** (turn the outline ON by default).

### FINDING R-2 (HIGH): the drop shadow should stay, but as a depth cue on top of a legible base — not as the sole contrast mechanism

Once the keyline exists, the soft directional shadow does exactly the job it's good at — a gentle
"lit from upper-left" lift that gives the floating label some dimensionality. Keep it ON. It just
shouldn't be carrying legibility alone (which is what it's silently doing today). No change beyond
R-1; noted so the shadow isn't removed by mistake.

### FINDING T-1 (MEDIUM): ALL-CAPS tracked type is the *right* instinct — the miss is that the degrees compete with the zone name

ALL CAPS + letter spacing at 1.8× in Minecraft's blocky font genuinely reads like a confident
engraved map label (think "PACIFIC OCEAN" spaced across an atlas) — this is on-brand, keep it.
The weakness is that `SUBPOLAR 66°N` is **one uniform run**: the zone name and the coordinate
shout at each other at equal size and weight. Real cartographic labels put the place name big and
the coordinate small underneath. A two-line lockup (zone name large; degrees smaller, lighter,
below) instantly reads as bespoke rather than "a string with a number stuck on the end."
→ recommendation **D**.

### FINDING M-1 (MEDIUM): the choreography is right in shape, but two easy tweaks make it feel premium instead of mechanical

The overall shape — fade in, one shine, hold, fade out, fires **once** and never loops — is
exactly right for a signature moment; the restraint is good. Two concrete problems:

1. **The fades are straight-line (linear).** Linear fades feel like a light switch. An ease-out
   entrance (rushes in, then settles) and an ease-in exit (lingers, then leaves) feel deliberate
   and expensive, for zero extra cost. → recommendation **E**.
2. **The shine fires too early** (tick 2, title ~20% visible), so the best flourish partly plays
   on a title you can't see yet. Delay the shine to land as the title finishes appearing. → **E**.

An optional third touch — a tiny "set-down": the title starts 3% larger and settles to full size
over the half-second entrance, like a stamp pressing onto a map — is very on-theme and cheap. → **E**.

### FINDING C-1 (HIGH for identity): the title shares almost nothing visually with the rest of the mod's look

The parchment loading screen and the create screen both use one specific brand gold —
`0xE8B64A`, literally the color of the "LATITUDE" wordmark on the loading screen
(`LevelLoadingScreenLatitudeOverlayMixin:36,253`) and the create-screen title
(`SpawnZoneScreen:19,61`) — inside a shared parchment map frame (the "Chartroom / Pillar 6"
language). The zone title borrows **none** of it; the ivory fill is the only faint echo. The
cheapest, strongest tie-in is a **hairline gold rule** (a thin 1-px gold line, that same
`0xE8B64A`) tucked just under the title — it instantly reads "map cartouche" and links the moment
to the loading screen and create screen the player already saw. → recommendation **C**.

### FINDING P-1 (POLISH / future): per-zone personality belongs in a small accent, never the whole title

Tinting the entire title by climate band (icy title in POLAR, red in a hot band, etc.) is
tempting but a trap: it fights the off-white cartographic identity and, worse, re-opens the
readability hole (a saturated red title on snow is back to invisible). The disciplined version is
to keep the fill off-white always and let the band color live **only in the hairline accent**
(the gold rule from C-1 becomes a band-tinted rule — icy blue for POLAR, warm gold for EQUATOR,
green for TEMPERATE, etc.). Subtle, safe, and it reinforces the actual meaning ("you crossed into
a *new climate*"). Low clutter because it's a thin line, not body text. → recommendation **F**
(after A/C/D/E land).

### FINDING X-1 (note): default position sits the title in the bright zone

Default offset is 40 px **above** center. In Peetsa's shots the horizon/sun glare band sits near
center, so above-center often lands the ivory text squarely in the brightest sky. Not worth a
default change on its own (it's draggable, and R-1's keyline fixes legibility everywhere), but if
R-1 were somehow declined, nudging the default *down* slightly would help. Flagged only for
completeness.

---

## 3. Recommendations (each with effort size and what Peetsa would see)

Effort key: **S** = a default/constant flip, **M** = a modest new draw or string split,
**L** = real new rendering work.

### A — Turn the outline ON by default: 1 px, dark. **[S]** — *the hero change*
Flip `zoneEnterTitleOutline` to `true`, keep thickness 1. Optionally set the outline color to a
very dark warm brown (`~0x1A130B`) instead of pure black so it whispers "ink on parchment" rather
than "UI black" — at 1 px the hue barely registers, so pure black is a fine safe choice too.
**What Peetsa would see:** the title stops dissolving into snow and bright sky. On the whiteout
screenshot it goes from a ghost you squint at to a crisp ivory label with a clean dark edge —
readable at a glance in every biome, day or night, with no plate or box cluttering the screen.

### B — Keep the drop shadow ON. **[S — no-op, do-not-remove]**
With A in place the soft shadow becomes a pure depth cue (its correct job). Leave it.
**What Peetsa would see:** the label feels gently raised off the world, "lit from the upper-left,"
instead of pasted flat.

### C — Add a hairline gold rule under the title. **[S–M]**
Draw a 1-px-tall line in the brand gold `0xE8B64A`, centered, width ≈ the title's width (or a
touch narrower), a few px below the text, at the title's current fade alpha so it fades in/out
with the words. (`ZoneEnterTitleOverlay` already knows the title's measured width via
`styledWidth`, and already draws at a known center — this is one `ctx.fill` in the same transform.)
**What Peetsa would see:** the floating words become a proper little map caption — the same gold
as the "LATITUDE" loading-screen logo draws a confident underline beneath the zone name. The whole
moment suddenly looks like it belongs to the same mod as the parchment screens.

### D — Two-line lockup: zone name big, degrees small beneath. **[M]**
Split `SUBPOLAR 66°N` into two lines — `SUBPOLAR` at full size, `66°N` at ~60% size on the line
below (optionally in the gold from C, which doubles as hierarchy). Requires splitting the built
string (the join point is a single space in `buildZoneEnterTitle:635`) and a second centered draw
line; the hemisphere channel already deals in stacked titles, so the infrastructure is partly
there. Guard the fit-to-width clamp so the smaller second line never forces a shrink.
**What Peetsa would see:** instead of a place and a number shouting at the same size, the zone name
reads as the headline and the coordinate sits quietly under it — exactly how a real map labels a
region. This is the biggest "generic → bespoke" jump for the type itself.

### E — Make the motion feel intentional. **[S]**
Three small changes, all constants/curves in the existing code:
- Ease the fade **in** (ease-out: quick then settle) and fade **out** (ease-in: linger then
  leave) instead of the current straight line (`ZoneEnterTitleOverlay:75-79`).
- Move the shine's start from tick 2 → ~tick 8 (`TitleStyle.GLIMMER_START_TICK`) so the glint
  crosses a fully-appeared title, not a half-faded one.
- Optional flourish: start the title ~3% larger and settle to full size across the entrance
  (multiply `drawScale` by an eased 1.03→1.0).
**What Peetsa would see:** the title arrives with a soft "settle," the shine sweeps across it right
as it finishes appearing (not before), and it eases away rather than blinking off — the difference
between a title that *lands* and one that just toggles.

### F — Per-zone accent on the rule only (after A/C/D/E). **[M]**
Make the C hairline rule take the crossed band's color (reuse whatever palette the atlas/compass
already keys per band) instead of a fixed gold; keep the text off-white.
**What Peetsa would see:** crossing into the poles, the underline glows cold blue; into the
tropics, warm gold — a quiet signal that the *climate* changed, without ever risking the
readability of the words themselves.

---

## 4. Suggested "Title v2" default recipe

Ship these as the fresh-config defaults (all knobs stay user-editable):

| Ingredient | v1 (today) | **v2 (proposed)** | Rec |
|---|---|---|---|
| Fill | off-white `0xF3ECDD` | **off-white `0xF3ECDD`** (unchanged — it's a good parchment ivory) | — |
| **Outline** | OFF | **ON, 1 px, dark warm brown `~0x1A130B`** | **A** |
| Drop shadow | ON | ON (now a depth cue, not the crutch) | B |
| Glow halo | OFF | OFF (stays an optional "epic" look) | — |
| Case | ALL CAPS | ALL CAPS | — |
| Letter spacing | +1 | +1 (or +2 for a hair more "engraved" feel) | T-1 |
| Size | 1.8× | 1.8× | — |
| Glimmer | ON, start tick 2 | ON, **start ~tick 8** | E |
| Fades | linear | **eased** (out-in / in-out), optional 1.03→1.0 settle | E |
| **Gold hairline rule** | none | **1 px `0xE8B64A` under the title** | C |
| Two-line degrees | one line | **future: zone big / degrees small below** | D |
| Per-zone accent | none | **future: band-tint the rule only** | F |

**If only one thing ships: recommendation A** (default the 1 px dark outline ON). It's a single
flag flip, needs no new code, and it's the difference between the signature moment being legible
everywhere versus vanishing into snow and bright sky — including at the poles, where the mod's
most dramatic crossings happen. Recommendation C (the gold hairline) is the best *second* change:
also cheap, and it's what makes the title finally look like the same mod as the parchment screens.

---

*Read-only creative review. No `src/` changes made. Contrast ratios computed with the standard
relative-luminance formula against sampled background colors from Peetsa's 2026-07-10 screenshots.*
