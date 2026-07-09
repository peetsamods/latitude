# HUD Studio round 11 — "Undo Last Load" in the Presets tab (2026-07-09)

`status: SUPERSEDED by round 12 — the one-shot text button became a ↶/↷ undo/redo icon pair; see
ui-pass-round12-undo-redo-selband-sweep-20260709.md. Never shipped standalone (TEST 43 folded into TEST 44).`
Source: Peetsa — wants a way to recover if a preset slot gets loaded (or a clipboard import applied) by
accident, restoring the HUD to whatever it looked like right before.

## What it does

A new "Undo Last Load" button in the Presets tab, placed right after Export/Import and before the 8 slot
rows. Disabled (greyed) whenever there's nothing to undo. Clicking it restores the HUD to exactly what it
was immediately before the last Load-a-slot or Import-from-clipboard action — one level only, no redo, no
deeper history; consumed on use, so clicking it twice in a row does nothing the second time.

## Implementation

`CompassHudPreset.applyToLive()` is the single choke point every Load-a-slot AND Import-from-clipboard
call already passes through (`CompassHudPresetSlots.loadFrom()` calls it; the Import button calls it
directly). Rather than have each of those call sites remember to snapshot the current state first — the
exact "hand-maintained sibling list rots" failure class round 9's sweep found and fixed (LESSONS L27) — the
snapshot is taken automatically at the TOP of `applyToLive()` itself, before anything is overwritten. That
one change protects both actions (and any future caller) with no per-button wiring needed.

- New static `CompassHudPreset.undoSnapshot` (in-memory only — an accidental load is a this-session mistake,
  not something worth persisting to disk like the numbered slots).
- `hasUndo()` / `undoLastLoad()` — `undoLastLoad()` calls the snapshot's own `applyToLive()` to restore it,
  guarded by a `restoringUndo` flag so that restore doesn't re-snapshot itself (which would otherwise
  silently turn Undo into a no-op the moment it's used, or worse, hide whether there was ever anything to
  undo).
- `LatitudeHudStudioScreen`: one new button, wired the same way as every other Presets-tab control
  (`tooltip()`, `trackSidebarWidget()`, `this.init()` after use to refresh all widget state incl. the
  button's own enabled/disabled read of `hasUndo()`).

Scope: deliberately limited to Load-a-slot and Import, per Peetsa's request ("if a preset is loaded
accidentally"). The Reset Compass/Labels/Title/HUD buttons are a separate button family and were not
wired into the same undo buffer.

## Verification

- `compileJava` + pure-JVM suite green; `./gradlew clean build` green.
- `TEST 43.jar` staged (SHA-256 `25993594cba39066397761a98cc4083a9ad6c3cc459c9907964be2cf7bc9d184`),
  superseding `TEST 42.jar`.
- NOT live-verified yet: Undo Last Load starts disabled; Save a slot, tweak the look, Load a different
  slot, confirm Undo restores the pre-Load tweak; confirm clicking Undo twice in a row is a no-op the
  second time (button re-disables); confirm Import from Clipboard also arms Undo.
- Worldgen isolation: `client/CompassHudPreset.java` + `client/LatitudeHudStudioScreen.java` only. Zero
  `world/`, `terrain/`, `mixin/` changes.
