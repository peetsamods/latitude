# HUD Studio round 7 — Tape pin fix, Rainbow theme, presets/export, per-label text size (2026-07-08)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 35 candidate)`
Source: Peetsa, follow-up to round 6 — confirmed the pin-marker bug is Tape-only, then asked for four new
features in the same message: save/load presets (8 slots), export/import HUD settings, independent
zone/biome/coords text-size sliders (parity with the existing Title Size), and a rainbow compass color
scheme.

## Finding 1: the "+" crosshair floated above the tape strip, Tape-only

Confirmed by Peetsa's screenshot of the undocked Tape compass: the pin marker (the "+" the Studio draws
at a HUD element's Pin & Grow anchor point, so you can see exactly what you're grabbing) sat visibly above
the drawn tape strip instead of on it. Every other look (Disc, Ring, Rose, Minimal) and every other
marker (zone, biome, coords) was correctly placed.

**Root cause:** this is the same bug class as LESSONS L23 (Tape's drawn content is shorter than its
nominal diameter×diameter box — `CompassDialRenderer.lookContentHeight()`), recurring in a consumer that
the original L23 fix pass never touched. `drawPinMarkers()`'s compass-pin branch drew the marker at the
raw Pin & Grow box-space position; every other Tape-aware code path (`computeAnalogBounds`,
`renderAnalogAt`, `applyCompassDrag`) already applies a `(diameter - contentH) / 2` offset to convert
box-space into content-space, but this one had been missed.

**Fix:** `CompassHud.drawPinMarkers()` now applies the identical offset for the ANALOG-style compass pin,
gated so it's a no-op for every look but Tape (where `contentH == diameter` for the rest, the offset is
zero) and a no-op for DIGITAL (no box/content distinction exists there).

## Finding 2: rainbow color scheme

Added `RAINBOW` to `AnalogCompassTheme` (inserted before `CUSTOM` — confirmed via grep that nothing in the
codebase depends on enum ordinal position, and Gson persists themes by name, so a mid-list insertion is
safe). It automatically appears in the existing "Color Scheme" cycle button with no separate UI work
needed. The dial's ring, needle, muted ring, and face colors cycle together through the color wheel over
an 8-second loop (`CompassHud.rainbowColors()`, keyed off wall-clock time via `System.currentTimeMillis()`
— the same idiom already used for other animated effects in this codebase). Ring and needle sit opposite
each other on the color wheel so they stay visually distinct at every point in the cycle.

## Finding 3: 8-slot presets + export/import

New "Presets" tab (between Title and General). Two new classes:

- `CompassHudPreset` — a portable snapshot of "the HUD's look": the full Compass/Labels config plus the
  Title tab's fields. Deliberately excludes the General tab (warnings/preview-text settings aren't part of
  a look someone would want to save or share). Backs BOTH features below with one shape.
- `CompassHudPresetSlots` — the 8 numbered slots, persisted to their own file
  (`globe_compass_hud_presets.json`) separate from the live HUD config, so switching presets can never
  corrupt the file the game reads every frame.

Presets tab: "Export to Clipboard" / "Import from Clipboard" buttons at the top (using the same
`keyboardHandler.setClipboard`/`getClipboard()` API already used for world-seed copying elsewhere in this
codebase), then 8 rows, each a Load button (showing a short summary like "3: Ring / Cyan Steel" or
"3: (empty)"), a Save button, and a small clear ("x") button. Load/Clear are disabled (greyed, unclickable)
for empty slots. Loading or importing calls `this.init()` afterward to refresh every widget, matching the
existing config-cascading pattern used by Reset Compass/Reset Labels/Reset Title.

Applying a preset or import copies fields INTO the existing `CompassHudConfig.get()` singleton via
reflection (`copyAllInstanceFields`) rather than replacing the instance — same "mutate in place" reasoning
as the existing Reset buttons, since other open Studio widgets hold a reference to that exact instance.
Reflection (not a hand-written field list) means a newly-added config field can never be silently missed
by presets/export the way a manual copy function could drift out of sync.

## Finding 4: independent zone/biome/coords text size

Three new sliders in the Labels tab (Zone Text Size, Biome Text Size, Coords Text Size; range 0.5–3.0,
default 1.0 — matching the existing Title Size slider's shape), backed by three new `CompassHudConfig`
fields (`zoneTextScale`, `biomeTextScale`, `coordsTextScale`).

Scope, stated plainly: this fully controls text size in two situations — (1) whenever a label is
*detached* (dragged off the compass to its own spot), and (2) the latitude/longitude readout when it rides
attached to the compass (it was already drawn as its own separate piece of text, so giving it its own size
was a clean change). The one place it's a **partial** fit: when zone AND biome are BOTH set to ride
*attached* to the compass at the same time, they currently render as one fused line of text sharing a
single size (the Zone Text Size slider, in that specific case). Splitting that fused line into two
independently-sized pieces would mean rewriting the compass's text-wrapping and drag-hitbox math (which
currently measures that combined line as a single unit) — a much bigger, riskier change than this round
warranted. Detaching either label (Zone Placement / Biome Placement → DETACH) sidesteps this entirely and
gives it full independent control immediately. The digital (non-analog) compass's single combined text
line is unchanged — it already has its own overall "Scale" control and was out of scope for this round.

## Finding 5 (follow-up, same day): "Rainbow" renamed to "Aurora" + a speed slider

Peetsa liked the theme but flagged two things: "Rainbow" reads as the classic fixed 7-band strobe, not
this smooth continuous hue rotation, and there should be a way to slow it down since faster cycling reads
as headache-inducing for most people. Renamed the UI-facing label to **"Aurora"** (the internal
`AnalogCompassTheme.RAINBOW` enum name is unchanged — renaming the enum itself risked invalidating any
config a player had already saved with it, for a change that's purely cosmetic to a label string). Added a
new `rainbowCycleSeconds` field (seconds per full color-wheel loop, default 24s) and a "Color Cycle Speed"
slider that only appears while Aurora is selected. The slider's own range (12s-90s) is kept gentle on
purpose — even its fast end is well short of anything strobe-like — per Peetsa's explicit ask to bias
toward slower rather than add a wide-open range that could be cranked into something jarring.

## Finding 6 (follow-up, same day): Color Cycle Speed range narrowed

Peetsa: the slider's range (12-90s) was too wide -- past default it was hard to tell any difference, so
most of the slider's length did nothing perceptible. Narrowed to **10-40s** (default unchanged, 24s) so
every point along the slider corresponds to a noticeably different pace. `sanitize()`'s clamp range
tightened to match.

## Verification

- `compileJava` + pure-JVM suite (`cleanTest test`) green.
- `./gradlew clean build` green; `TEST 38.jar` staged (SHA-256
  `6a2c80b620c05e8d385096f3dcda9274325009b0c9b1eb5d653d2e5a8752e983`), superseding `TEST 37.jar`
  (Classic-atlas fix round, `TEST 36.jar` before that, `TEST 35.jar` before that, `TEST 34.jar` before
  that).
- NOT live-verified yet: pin marker on Tape (undocked + docked), Aurora theme cycling visibly at its
  default speed and after dragging the Color Cycle Speed slider, all three new text-size sliders (attached
  and detached for zone/biome, attached and detached for coords), Presets tab save/load/clear across all 8
  slots plus Export → Import round-trip.
- Worldgen isolation by diff scope: only `client/CompassHud.java`, `client/CompassHudConfig.java`,
  `client/LatitudeHudStudioScreen.java`, `client/CompassHudPresetSlots.java`, and one new file
  (`client/CompassHudPreset.java`) touched. Zero `world/`, `terrain/`, `mixin/` changes. Same worldgen
  args as TEST 30-34.
