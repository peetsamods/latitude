# TEST 28 UI pass — round-1 findings and fixes (2026-07-07)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 29 candidate)`
Source: Peetsa's live UI pass on TEST 28 (SHA `e453665d…`), 5 annotated screenshots delivered
mid-pass ("I'll run the UI pass"). Five bugs + two create-screen requests + one confirmation
question. Every fix below is committed on `port/canonical-26.2-pivot`; compile + pure-JVM suite
green. No worldgen-relevant file touched (all changes in `client/`, `client/create/`) — the r=0
recipe and C-2 remain exactly as live-validated.

## Confirmation answered

**"So this is the correct look for the world creation screen, right?"** — YES. Dice + copy seed
buttons, Mercator 2:1 size text with real dimensions ("40,000 × 20,000 blocks"), banded atlas with
23.5/35/50/66.5° gridline labels, spawn-zone list with per-band accents, HUD Studio in Rules: all
the approved U-E design. The two wishes below were the gaps.

## Findings → root causes → fixes

| # | Reported (Peetsa's words) | Root cause (code truth) | Fix |
|---|---|---|---|
| F1 | "wish we could make the atlas a little more centered" | `computePreviewLayout` centered the atlas+label COMPOSITION, so the map sat left of visual center by half the label column | Center the MAP; labels absorb the right-hand slack (degrades to old behavior when the panel is tight). Caption re-centers under the map |
| F2 | "can we have a 'random' setting for spawn location?" | Feature didn't exist | Sixth zone row **Random** (null-band `ZoneRowWidget`): neutral accent, "0–90°" range, "sealed orders" helper; planisphere renders with NO band highlight (renderer now null-tolerant); description panel goes random-aware; `beginExpedition` rolls a concrete band at create time (logged `[lat-ui] Random spawn zone rolled:`) so everything downstream keeps the single-band contract |
| F3 | "scroll bar compass attached to hotbar is too high; when GUI is higher, it is too large" | TAPE draws a strip only `max(12, radius/2)+4` px tall **centered in a diameter×diameter box** (deliberate in U-D "so dock math holds untouched") — the dock ladder docked the phantom box, floating the strip high; bigger dials/GUI scales amplified it | Dock with the look's TRUE content height (`CompassDialRenderer.lookContentHeight`). Tape content (16–22 px at every slider size 16–72) now fits the BESIDE rung's hotbar-row centering — the strip sits ON the hotbar line like a native element. No-op for disc looks (content == box) |
| F4 | "Pressing 'L' to hide settings still shows some" | `updateSidebarVisibility()` hides a hand-maintained list of FIELD widgets; Direction Format + Compass Look (U-C/U-D additions) are LOCALS known only to the scroll tracker — never hidden. ("Reset Compass" was worse: registered with neither mechanism — it also didn't scroll with the panel) | Blanket pass first: every tracked sidebar widget follows the L toggle; named conditional rules re-apply after. Reset Compass now tracked (scrolls + hides) and got a tooltip |
| F5 | "Changing the direction format does nothing" | `cfg.directionMode` is only rendered on the DIGITAL text line; her style is ANALOG (TAPE), whose labels were a hardcoded 8-wind array — the button was genuinely inert for every analog look | Two-part truthfulness fix: (1) TAPE labels honor the format — CARDINAL_4 labels only N/E/S/W, DEGREES swaps letters for bare heading numbers; (2) the Direction Format row now only EXISTS where it has an effect (digital style, or analog+TAPE) — dial looks show facing with their needle, so the row is gone there instead of dead. Compass Look changes re-init the panel so the row appears/disappears with TAPE |
| F6 | "compass analog size can make 'attach to hotbar' fit strangely" | Same phantom-box geometry as F3, scaled by `analogSize` | Same content-true dock fix; Studio preview border + hitbox + drag also content-true now (drag converts content-top back to box-top so it round-trips without drift). Docked ANALOG previews now draw the ghost hotbar too (was digital-only), so "attached" is judged against real geometry |
| F7 | "'attach to hotbar' off + Reset HUD doesn't sit right — kind of in the middle of the screen" | Both resets copy `fresh()`, and U-D's fresh() shipped a 15% "boss-bar nudge" (`offYFrac=0.15`) — Reset deliberately parked the compass ~15% down-screen | Nudge reverted: `fresh()` returns the classic top-center-at-edge default. Boss-bar overlap returns only DURING boss fights (as it always was pre-2.0), and the Studio makes moving it trivial |

## Verification

- `compileJava` green; pure-JVM suite re-run via `cleanTest test` — green (HudLayoutMath itself
  untouched; dock changes live in the client glue, which the suite doesn't cover — live re-test is
  the gate for F3/F6 look).
- Worldgen isolation by diff scope: only `client/CompassHud|CompassDialRenderer|CompassHudConfig|
  LatitudeHudStudioScreen` + `client/create/LatitudeCreateWorldScreen|LatitudePlanisphereRenderer`
  touched. Zero `world/`, `terrain/`, `mixin/` changes.
- NOT live-verified yet: all seven items need the next jar (TEST 29) — same 9-point checklist rows
  plus: tape docked at sizes 16/32/72 × GUI scale 2/3/4; L-toggle on all four tabs; Direction
  Format cycle on Digital + Tape (and row ABSENT on Disc/Ring/Rose/Minimal); Reset HUD / Reset
  Compass landing top-center; Random zone create (check the rolled band in the log line).

## Observed but not raised (recorded, no action)

- Screenshot 2's spawn is a tiny grass islet amid Deep Ocean at 42°S — legal (spawn requires a
  land BIOME column, not a large landmass) and C-2's edge-band exclusion doesn't police islet
  size. Backlog candidate: minimum-landmass spawn quality check, biome-side.

## Deferred / follow-up candidates

- A true SHRUNK dock rung (cap effective dial size when docked on narrow screens) — deferred in
  U-A, still deferred; revisit only if "too large at higher GUI scale" persists after the
  content-true fix.
- Optional facing readout in the analog attached text line (would give Direction Format an effect
  on dial looks too) — feature, Peetsa's call.
- Random-zone roll is UI-time `java.util.Random` (not seed-derived): same seed + Random can land
  different bands on purpose. Flip to seed-derived if reproducibility ever matters.
