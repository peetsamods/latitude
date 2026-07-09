# TEST 30 UI pass round 3 — three bugs, three root causes (2026-07-08)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 32 candidate)`
Source: Peetsa's screen recording of a TEST 30 HUD Studio + create-screen pass (`Screen Recording
2026-07-08 at 7.44.29 PM.mov`), reviewed frame-by-frame (native-resolution crops, not just the
downscaled overview) to pin down exact geometry before touching code. Three findings, three
independent root causes, all fixed and compiled green. No worldgen files touched.

## Finding 1: "the atlas bounces around... when I change the size... the word ATLAS, everything
## down" (Small → Regular → Large → Ginormous)

**Root cause:** `computeSizeLabelBottom()` reserved vertical space for the size-description text
using ONLY the currently-selected size's wrapped line count (`SIZE_DESCRIPTIONS[selectedSize
.ordinal()]`). Different sizes' descriptions wrap to different numbers of lines at a given panel
width (e.g. "The standard world. A full planet awaits." vs "A world that could take a lifetime to
cross."), so `baseInputBottom` — and therefore `leftPreviewTopY`, the Y position of the "ATLAS"
heading and the map itself — moved every time the selected size's description happened to wrap
differently than the previous one's. `previewHeight` (the box's fixed height once positioned) never
varied by size, so this wasn't the atlas resizing — it was the whole section sliding up/down under a
description block of varying reserved height. My round-2 top-align fix made this MORE visible (it
removed the v-centering that had been softening the effect), but the coupling itself predates round 2.

**Fix:** reserve the WORST-CASE line count across every size's description, not just the selected
one — the same "reserve for the widest/tallest case" pattern already used elsewhere in this codebase
(the compass HUD's `reservedTextWidth`). Short descriptions now leave trailing blank space in their
box instead of letting the box (and everything below it) move.

## Finding 2: "select tape... attach it to hotbar... appears to be off screen [in HUD Studio], but
## in-game it sits correctly"

**Root cause:** a coordinate-space contract mismatch introduced by round 2's content-true dock fix.
`renderAnalogAt`/`renderPreview` expect their `(x, y)` parameter to be the DIAL BOX's top-left —
exactly what the live in-game path (`computeAnalogDialPos`) supplies. But the Studio-preview call
site was instead feeding in `computeAnalogBounds()`'s returned bounds, whose `y` is the CONTENT
top — deliberately content-adjusted in round 2 so the Studio's hitbox/border match what's visually
drawn for looks where content ≠ box (TAPE only). Piping that already-adjusted value back through
`renderAnalogAt`'s own box→content math applied the offset a SECOND time, pushing the strip further
down than intended — Studio-preview only, since the live path never touches that bounds call at all.
Every other look has content == box (no adjustment, so no double-application, so no visible bug) —
which is exactly why this surfaced only on Tape.

**Fix:** the Studio-preview render call site now computes the actual dial-box position the same way
the live path does (`computeAnalogDialPos` for analog, `computeDigitalBounds` for digital), instead
of reusing the content-true hitbox bounds for rendering. `computePreviewBounds()` — now provably
dead code once its one call site was fixed — was deleted rather than left orphaned.

## Finding 3: "adjusting the transparency and the Photoshop background appears... as soon as you
## move the slider at all, it's locked, and it doesn't go away"

**Root cause:** round 2's `transparencyAdjustActive()` gated the checkerboard aid on
`slider.isFocused() || slider.isMouseOver(...)`. Vanilla widget focus is STICKY — `setFocused(true)`
fires on click and nothing in the normal interaction path clears it until a *different* widget takes
focus (switching tabs works because that rebuilds every widget from scratch). So the very first
click-drag on Inner Transparency latched the checkerboard on permanently for the rest of that Studio
session, exactly matching the report.

**Fix:** drop `isFocused()` entirely. `FloatSlider` (used for both Analog Size and Inner
Transparency) now tracks its own click→release lifecycle explicitly via overridden `onClick`/
`onRelease` (`isDragging()`), which is inherently non-sticky. The checkerboard now shows during an
active drag or a plain hover, and disappears the instant neither is true.

## Verification

- `compileJava` green; pure-JVM suite re-run via `cleanTest test` — green.
- Worldgen isolation by diff scope: only `client/create/LatitudeCreateWorldScreen.java`,
  `client/CompassHud.java`, `client/LatitudeHudStudioScreen.java` touched. Zero `world/`, `terrain/`,
  `mixin/` changes — the C-3 grip fix from earlier tonight is untouched.
- NOT live-verified yet (UI-visual class of bug — needs the next jar): atlas position/size holding
  steady across all six world sizes cycled rapidly; Tape + Attach to Hotbar sitting correctly in the
  Studio preview at various Analog Sizes; Inner Transparency checkerboard appearing only while
  hovered/dragging and disappearing cleanly on release and on mouse-away.

`TEST 31.jar` staged (SHA-256 `1c589a159ebaf7ef62cd0df334e8a0699fa926442816a6ae8e73de50c3756371`),
superseding `TEST 30.jar`. Same worldgen args as TEST 30 (S=0.4, oceanStrengthRatio=1.0, gripWidth
default 0.8) — the C-3 wall fix from earlier tonight rides along unchanged; fresh world still
recommended if re-checking terrain, not required for a UI-only re-check.
