# 1.4.1 (#4) — Six new analog compass themes (default-safe, append-only)

`status: active` · `scope: rendering` · `date: 2026-06-22` · `evidence id: 20260622-compass-themes`

## Ask
Peetsa (1.4.1 punch list #4): more color schemes/themes for the compass, and a "better-looking,
more immersive" compass that "still looks like it belongs in Minecraft — not better graphics per se."

## What shipped this slice (themes only)
Added 6 analog themes following the recon's append-only 3-edit recipe, default `CLASSIC_GOLD`
untouched so existing players and old `globe_compass_hud.json` configs (enum-by-name) are unaffected:

- `src/main/java/com/example/globe/client/CompassHudConfig.java` — enum `AnalogCompassTheme`
  extended (appended after `MINT_BRASS`; never reordered, cycle order stable).
- `src/main/java/com/example/globe/client/CompassHud.java` — `analogColors()` cases. `face` is
  plain `0xRRGGBB` (alpha re-applied by `analogInnerColor`); ring/muted/needle are full `0xFF` ARGB.
- `src/main/java/com/example/globe/client/LatitudeHudStudioScreen.java` — `themeLabel()` cases
  (exhaustive switch → compiler enforces a label for every theme).

New palettes (face / ring / muted / needle):
- OBSIDIAN_RED `0x14110F / 0xFFB0A8A0 / 0xFF6E6862 / 0xFFE2402E`
- ARCTIC_BLUE `0x16202B / 0xFFCFE8FF / 0xFF7F9DB5 / 0xFF4FC3FF`
- EMERALD `0x122019 / 0xFF7BE0A0 / 0xFF6F9C82 / 0xFFFFD56A`
- ROYAL_PURPLE `0x1A1426 / 0xFFC9A6F0 / 0xFF8C7AA0 / 0xFFFFC04D`
- SUNSET `0x261712 / 0xFFF2A65A / 0xFFB07E62 / 0xFFFF5E5B`
- MONOCHROME `0x1B1B1E / 0xFFD8D8DC / 0xFF80808A / 0xFFF2F2F2`

The HUD Studio theme picker (`CycleButton.withValues(AnalogCompassTheme.values())`) auto-includes
the new themes; no bounds/footprint math touched → no HUD-overlap regression risk.

## Proof (green)
- `./gradlew compileJava` (Java 25) → BUILD SUCCESSFUL (`tmp/1.4.1-prep-20260621/compass-compile.log`).
- `./gradlew build` → BUILD SUCCESSFUL; `javap` of the jar's `AnalogCompassTheme` enum shows **11**
  constants. Staged jar SHA `af0094f5b5e0ad825b370b3003c0b5c1bece3f2427cae19383460e1c4a45a572`
  (snow + compass) into `Lat 1.4+26.1.2/mods/`.

## Held for your input (immersive styling)
The "more immersive, Minecraft-native" part is a design-feel call, so it was rendered as 3
directions (A current minimal · B cardinal ticks + inner bezel · C compass rose with two-tone
needle) for sign-off rather than guessed. All three stay inside `drawAnalogCompass` with no bounds
change (no overlap risk), and avoid anti-aliasing/glow so the look stays pixel-native. Build the
chosen one next.

## Residual / next
- Live eyeball (Peetsa): HUD Studio → Style = Analog → cycle Color Scheme through all 11; check at
  GUI scale 2 and 4 for no overlap (additive themes shouldn't change footprint).
- Optional cleanup (not done): the 16×16 compass PNG assets are dead (zero refs) — remove in a future pass.
- Decision pending: which immersive direction (A/B/C), and whether to extend theming to the DIGITAL compass.
