# Polar Approach Experience — Creative Director Review (2026-07-11)

**Status:** READ-ONLY, PROPOSED. No `src/` changes. Options for Peetsa to pick from.
**Surface:** the complete 84°→90° polar escalation as one composition + warning-message copy.
**Reviewer note:** rain-at-pole is treated as FIXED (parallel precipitation→SNOW patch poleward of ~75°).

---

## 1. Current-state summary (what actually renders today, grounded)

Six independent systems layer up as you walk toward the pole. All are pure functions of
absolute latitude, all symmetric N/S:

| System | File | Onset → full | What it does |
|---|---|---|---|
| Ambient snow particles | `GlobeModClient.java:239–357` (`spawnAmbientPolarSnow`) | 85° (2 flakes/tick) → 90° (30, +30 second pass) | near-field snowflakes; wind + fall speed ramp on the **blizzard drive** (0 at 87° → 1 at 90°), plus a dense low "ground blizzard" second pass inside 87–90° |
| Storm sky | `ClientLevelStormSkyMixin.java` + `PolarHazardWindow.stormLevel` | 85° → **87.5°** (steep) | lifts the client rain level → vanilla greys the sky, fades the sun, and thickens real vanilla snowfall. Sun gone by 87.5° |
| Whiteout fog fill | `PolarWhiteoutOverlayHud.java` | 85° → 90° | full-screen fill, grey-blue storm tint → near-white (238,242,248) at the pole, `intensity^0.65` ease, **max 0.90 alpha** |
| Warning text ladder | `GlobeWarningOverlay.java:24–31,149–206` + `PolarWarningEpisode.java` | 85 / 87 / 89 / 89.7° | four episodic lines, each fires ONCE, holds ~10 s (`POLE_WARN_HOLD_TICKS 200`), fades ~1 s; re-arms only on full retreat below 84°. DANGER/LETHAL are RED+BOLD |
| Vignette pulse | `PolarVignetteOverlayHud.java` + `PolarWarningVignette.java` | DANGER 89° / LETHAL 89.7° only | edge-darkening cold near-black; DANGER crest 0.25, LETHAL 0.40 + 0.08 linger; 250 ms rise / 500 ms settle / ~9 s hold / 1 s melt; center ~60% stays clear; honors Reduce Motion (static hold) |
| Freeze/slow mechanics | `PolarHazardWindow.java:30–91` | 87° slowness → 88° mining fatigue → 89.7°+ freeze damage → 90° death | continuous slowness/weakness ramp; steady per-tick frozen-ticks so hearts stay blue and freeze damage actually lands |

**The single biggest fact about this surface: it is completely SILENT.** A repo-wide grep for
`playSound` / `SoundEvents` across `src/main/java/com/example/globe/` returns nothing. The most
dramatic place in the mod makes no sound of its own.

---

## 2. PART 1 — Warning copy alternatives

Constraints honored: tier 1 names snow onset (doubles as the weather explanation); DANGER/LETHAL
stay unmistakably urgent (they pair with red text + the vignette pulse); plain language, no gore.

| Tier (°) | **Set A — Expedition Log** (nautical/cartographer) | **Set B — Stark Minimal** | **Set C — Sharpened Current** |
|---|---|---|---|
| **1 · 85° WARN** (snow onset) | "Snow begins to fall. Few press on past these latitudes and return — consider turning back." | "Snow. The cold is rising. Turn back while it's easy." | "Snow begins to fall — the cold is setting in. Turn back while it's easy." |
| **2 · 87° WARN** (slowing) | "The cold is in your bones now. Every step comes slower." | "Colder. You're slowing." | "The cold sinks deeper. You're growing slow and weak." |
| **3 · 89° DANGER** (RED+BOLD) | "DANGER — killing cold ahead. Turn back now." | "DANGER. Deadly cold. Go back." | "DANGER! Lethal cold ahead. Turn back now." *(unchanged)* |
| **4 · 89.7° LETHAL** (RED+BOLD) | "The cold has you. You are freezing." | "Freezing. The cold is killing you." | "The cold is freezing you solid." |

### Recommendation — a HYBRID, and I'll defend it

Ship **Set A's two approach lines (85° + 87°)** paired with **Set C's two red lines (89° + 89.7°)**.

Reasoning: the mod's whole identity is the cartographer/expedition voice — parchment, the compass,
"box the compass," latitude-as-navigation. The 85° and 87° lines are the *calm before the danger*;
that is exactly where flavor belongs, and Set A's "Few press on past these latitudes and return"
makes the pole feel like a place explorers *speak of* — it earns memorability for free, in words.
But at the red-alert moment (89°/89.7°) flavor must yield to clarity: the player needs to read
"turn back NOW" in a fraction of a second on the brightest, busiest screen the game ever draws, so
the two red lines should stay the sharp, functional imperatives of Set C (the current DANGER line is
already right — keep it). Set B reads well but is a touch generic for a mod with this much voice;
Set A's red lines ("killing cold") are good but slightly less instantly-scannable than Set C's.

If Peetsa wants ONE coherent single-voice set instead of the hybrid, my pick is **Set A** end-to-end.

---

## 3. PART 2 — The whole look, storyboarded

Walking poleward at a steady pace (both hemispheres identical):

| Lat | Sky | Whiteout fill | Snow | Text | Vignette | Body |
|---|---|---|---|---|---|---|
| **84°** | clear/normal | none | none | none | none | none — fully explorable, ladder re-armed |
| **85°** | *just* starting to grey (stormLevel 0) | 0 (nothing yet) | 2 flakes/tick | **WARN_1 fires** | none | none |
| **86°** | clearly overcast (stormLevel 0.4, sun fading) | ~0.31 alpha visible haze | ~8 | (WARN_1 still holding) | none | none |
| **87°** | near-full storm (0.8), sun almost gone | ~0.50 | ~13 | **WARN_2 fires** | none | **slowness begins** |
| **88°** | full overcast, no sun | ~0.63 | ~19 + ground-blizzard pass starts, wind ramps | (quiet) | none | + mining fatigue |
| **89°** | full storm | ~0.76 | ~25, driven gale | **DANGER fires** RED | **pulse 0.25** | freeze ramping |
| **89.7°** | full storm | ~0.85 | ~29 | **LETHAL fires** RED | **pulse 0.40** + linger | freeze near-max |
| **90°** | full storm | 0.90 max | 30 + 30, full gale | (LETHAL held) | linger 0.08 | **freeze death** |

### Does it escalate monotonically? — Mostly YES, with two soft spots

- **It never eases off.** Every continuous layer (sky, fog, snow, hazard) climbs monotonically with
  latitude. Good. The steep storm-sky curve (full by 87.5°) is a smart choice — it kills Peetsa's
  "sunny sky while snowing" complaint early and front-loads the *mood* into the 85–87° approach so
  that stretch is never visually quiet even between text beats.
- **Soft spot 1 — the 85° promise outruns the picture.** At exactly 85° WARN_1 says "snow begins to
  fall," but the whiteout fill is still 0, the storm sky is still 0, and only 2 flakes/tick spawn.
  For the first second or two the words describe a storm the eyes can't see yet. Minor, self-corrects
  within ~1° of walking, but it's the one moment the composition slightly over-promises.
- **Soft spot 2 — DANGER and LETHAL are almost the same beat.** They fire 0.7° apart. Near the pole
  that is a very short walk, and both are treated identically to the eye (RED + BOLD + a vignette
  pulse). Two "signature" red flashes that close together risk blurring into one muddy event rather
  than reading as two distinct rungs — the climax spends its two biggest cards almost simultaneously.

### Redundancy / busy-ness

- **85–87° is NOT too quiet** — the sky, fog and snow all ramp through it continuously. Fine.
- **89–90° is busy, appropriately** — it's the climax; busy is earned. The concern isn't quantity,
  it's that DANGER and LETHAL don't feel *different enough* from each other (see soft spot 2).
- The vignette + red text + whiteout stack at 89.7° reads as **terrifying-leaning-muddy**, and the
  muddiness has a specific, fixable cause — see Finding 1.

### Sound — the biggest missing sense, full stop

There is no audio story at all. The pole is the mod's emotional peak and it is dead silent except
for vanilla footsteps. A single ramping wind bed (volume tied to the same 85→90 progress the fog
uses) would do more for memorability than any visual tuning below. Silence is *the* gap.

---

## 4. Findings, ranked (readability first)

**F1 — READABILITY: the red DANGER/LETHAL text sits on the brightest backdrop the game ever draws, in its weakest color.** *(High)*
`poleTextForStage` (`GlobeWarningOverlay.java:154–155`) styles DANGER/LETHAL as plain `RED + BOLD`
with **no shadow or outline**. At 89–90° that text is centered over the near-white whiteout fill
(238,242,248 at up to 0.90 alpha). Minecraft RED (`0xFF5555`) on near-white is roughly a **2.7:1**
contrast ratio — legible but weak, and it's the single most important text in the whole mod. Worse,
the vignette darkens the *edges* but deliberately keeps the center ~60% clear — exactly where the
text lives — so the one effect that could make the words pop pointedly avoids them. The terrifying
moment is quieter than it should be *because* the whiteout succeeds.

**F2 — SOUND: the entire escalation is silent.** *(High — biggest memorability lever)*
No `playSound` anywhere in the globe client. See §3.

**F3 — MOTION/PACING: DANGER and LETHAL are near-identical beats 0.7° apart.** *(Medium)*
Same color, same weight, same vignette gesture, back-to-back. The climax's two rungs read as one.

**F4 — the 85° text over-promises the visuals for ~1–2°.** *(Low)*
WARN_1 announces snowfall before the fill/sky/density have visibly arrived. See §3 soft spot 1.

**F5 — no art-direction tie to the parchment/gold identity anywhere in the polar experience.** *(Low)*
Every other surface (loading wordmark, create screen, titles) shares the gold `0xE8B64A` /
ivory language; the pole is palette-orphaned (cold blues/whites only). Not wrong — cold *should* be
cold — but there's no cheap thread back to the mod's voice except the words themselves (which is part
of why the copy voice in Part 1 matters so much here).

---

## 5. Recommendations (effort-sized)

- **R1 (S) — Give DANGER/LETHAL text a dark backing so red reads on white.** *What Peetsa would see:*
  the red warning suddenly pops and feels urgent instead of washing out. Cheapest: add
  `ChatFormatting`-style shadow isn't enough on white — instead draw a 1–2 px dark text backing
  plate or a dark outline behind the pole warning line only. Reuse the outline idiom already written
  (but shipped OFF) for the zone-enter title (`core/ui/TitleStyle`) — the exact same "dark keyline
  makes light-vs-dark backgrounds both safe" trick, applied to this line. **This is the fix for F1.**
- **R2 (M, needs one sound asset OR a vanilla-sound loop) — A ramping wind bed 85→90°.** *What Peetsa
  would see/hear:* a low wind that rises as she nears the pole and howls at 90°, then fades as she
  retreats. Cheapest no-asset path: loop an existing vanilla sound event at latitude-scaled volume
  from the same client tick that spawns snow (`GlobeModClient` ambient block); a bespoke blizzard
  loop asset is the richer version. **This is the fix for F2 and the highest-impact item overall.**
- **R3 (S) — Differentiate LETHAL from DANGER.** *What Peetsa would see:* the final rung feels like a
  distinct, worse moment, not a repeat. Options: give LETHAL a heavier/faster vignette rise, a brief
  screen-edge frost-blue tint instead of near-black, or a one-shot low sound sting (pairs with R2).
  Or space the thresholds (e.g. DANGER 88.5°, LETHAL 89.7°) so they aren't a 0.7° double-tap.
- **R4 (S) — Nudge WARN_1 to 85.3–85.5° or seed 1–2 flakes at 84.7°** so the "snow begins" line lands
  the instant snow is actually visible. Closes F4.
- **R5 (M, future) — One warm thread back to the identity:** e.g. the DANGER/LETHAL text backing (R1)
  in a very dark warm-brown rather than cold-black, or a thin gold hairline under the pole warning
  line matching the title/loading treatment. Ties the pole to the atlas voice without warming the
  *world* (which must stay cold). Closes F5.

---

## 6. "v2 recipe" — the coherent combination I'd ship

- **Copy:** Set A approach lines (85°/87°) + Set C red lines (89°/89.7°). *(the hybrid, §2)*
- **Text legibility:** dark keyline/backing ON for the two red pole lines (R1), reusing the title
  outline idiom.
- **Sound:** ramping wind bed, volume on the 85→90 fog progress, retreat-symmetric (R2).
- **Climax differentiation:** LETHAL gets a faster/deeper vignette rise + a one-shot sting so it's a
  distinct beat from DANGER (R3); optionally widen the DANGER/LETHAL gap.
- **Timing:** WARN_1 lands with the first visible flakes (R4).
- **Identity thread:** dark-warm-brown text backing instead of cold-black (R5) as the one cheap tie
  to the parchment/gold language.
- Everything else (steep storm sky, `^0.65` fog ease, 30+30 snow budget, freeze mechanics, episodic
  fire-once ladder, Reduce Motion static vignette) is already right — leave it.

---

## 7. THE single highest-impact change

**Add a wind sound that rises from 85° to a howl at 90° and fades on retreat — the pole is the mod's
emotional peak and right now it is completely silent, and no visual tuning will make it as
unforgettable as finally being able to *hear* the storm.**
