# Round 12 — undo/redo icons + selected-band left→right glow (2026-07-09)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 44)`
Source: Peetsa, two requests off the TEST 43 screenshot:
1. Replace the verbose "Undo Last Load" text button with a compact undo **arrow icon**, and add a **redo**
   icon next to it.
2. On the Atlas, when a specific band is selected (not Random), make its highlight shimmer with the same
   Gaussian glow — but radiating **left→right** across the band, in the band's own highlighted colors.

## 1. Undo → undo/redo icon pair (extends round 11)

Round 11's one-shot undo became a proper one-level undo↔redo toggle:
- `CompassHudPreset` now keeps `undoSnapshot` AND `redoSnapshot` (both in-memory). A fresh Load/Import arms
  Undo and clears any Redo branch (auto-captured at the `applyToLive()` choke point, unchanged). `undoLastLoad()`
  swaps live state with the undo point and arms Redo; `redoLastLoad()` swaps back and re-arms Undo — the
  classic single-step toggle between "before" and "after" the load. Both drive `applyToLive()` through a
  `suppressHistoryCapture` guard so the restore doesn't re-snapshot itself.
- `LatitudeHudStudioScreen`: the single full-width "Undo Last Load" button became two compact side-by-side
  icon buttons — `↶` (undo) and `↷` (redo) — each half the row width, each disabled when its stack is empty,
  wired identically to every other Presets-tab control. Meaning lives in the tooltips now, not the label.

  NOTE: the icons are the Unicode curved-arrow glyphs U+21B6 / U+21B7 (Arrows block, covered by MC's
  legacy-unicode font). If they render as boxes on some setup, swap to an alternative pair (e.g. `↺`/`↻`) or
  hand-draw them — trivial one-line change.

## 2. Selected-band left→right glow (companion to the round-10 Random sweep)

When a band is picked, that band's strip now shimmers with a glow crest sweeping left→right, instead of a
static bright fill — the round-10 Random idea turned on its side:
- New `LatitudePlanisphereRenderer.fillSelectedGlowStrip()` draws the selected strip in short vertical
  column slices; each column's brightness/opacity follows a horizontal Gaussian crest centered on an
  advancing `front` (fraction across the width), floored at `SELECTED_BASE_GLOW = 0.5` so the band ALWAYS
  reads clearly selected, rising to the full selected pop (`glow == 1`, the exact old static look) as the
  crest passes. Reuses the same bright/alpha mapping as the Random sweep and the same `SWEEP_FADE_FRAC`
  seam-fade envelope, so it breathes at the loop instead of popping.
- Both mirrored hemispheres of the selected band get the same sweep. The static gold edge outline on the
  selected band is unchanged. Non-selected bands stay the muted wash; Random (no selection) is unchanged.
- Three cosmetic constants: `SELECTED_SWEEP_PERIOD_MS = 2600`, `SELECTED_SWEEP_SIGMA = 0.18` (crest width
  as a fraction of the band width), `SELECTED_BASE_GLOW = 0.5` (highlight floor).

Cost: a handful of extra `fill` calls (2px column slices) for the ONE selected band, only on the create
menu — negligible.

## Follow-up (same day, TEST 45): bolder glow + bigger icons

Off the TEST 44 screenshot Peetsa flagged two things:
- **Selected-band shimmer too subtle** — wanted it bolder, like Random, with a stronger glow. Lowered the
  highlight floor `SELECTED_BASE_GLOW` 0.5 → 0.35 (more crest-vs-base contrast), raised the peak brightness
  gain to `SELECTED_BRIGHT_GAIN = 0.45` (crest peaks ~1.45×, bolder than Random's 1.30×), and pushed the
  crest to fully opaque (`0xFF`, was `0xE6`). The band still holds a clear selected floor between crests.
- **Undo/redo icons "comically small"** — the arrows DID render (good, not boxes), but Minecraft draws them
  through its thin unicode-fallback font at button-message size, so they looked tiny in a tall button. Fixed
  by giving the two buttons EMPTY labels and drawing the `↶`/`↷` glyphs myself, scaled ~1.9× and centered on
  each button in `drawButtonGlyph()` — the same manual scaled-text approach the tab-strip labels already use,
  drawn after `super.extractRenderState()` so the big glyphs sit on top of the button backgrounds.

## Follow-up 2 (same day, TEST 46): unselected bands go transparent

Peetsa: when a specific zone is picked, the OTHER bands should be uncolored — not black/white, just fully
transparent so the atlas map graphic (ocean + continents) shows through — and only color in when their zone
is selected. Changed the band loop so that when a specific band is selected, every non-selected band is
skipped entirely (no fill) instead of drawing the old muted `0x86` color wash. The faint latitude graticule
and the selected band's gold edges still draw, so the map keeps its reference lines and the picked band
still reads clearly. Random (no selection) is unchanged — all bands still animate the outward pulse.

## Verification

- `compileJava` + pure-JVM suite green; `./gradlew clean build` green.
- `TEST 46.jar` staged (SHA-256 `aeba2d735e7eedd52fb7627e740fc246588e1a704a409038ac09c9fb02b92729`),
  superseding `TEST 45.jar` → `TEST 44.jar` → `TEST 43.jar`.
- NOT live-verified yet: (a) the ↶/↷ icons render (not boxes) and are disabled/enabled correctly — Load a
  slot arms ↶; clicking ↶ reverts and arms ↷; clicking ↷ re-applies; a fresh Load after an undo clears ↷.
  (b) Picking a zone shows the highlight sweeping left→right and staying clearly selected; Random still
  does the outward vertical sweep.
- Worldgen isolation: `client/CompassHudPreset.java`, `client/LatitudeHudStudioScreen.java`,
  `client/create/LatitudePlanisphereRenderer.java`. Zero `world/`, `terrain/`, `mixin/` changes.
