# HUD Studio tabbed redesign + Custom analog theme + dead-screen removal (2026-07-03)

`status: active` · `scope: rendering` · `date: 2026-07-03` · `evidence id: 20260703-hud-studio-custom-theme`

## What shipped this slice

Committed directly by Peetsa in `90a5fbbe` ("feat(hud): RGB color pickers + tabbed HUD Studio
redesign"), branch `port/canonical-26.2-pivot`:

- **New `CUSTOM` analog compass theme** — 12th value appended to `AnalogCompassTheme`
  (`CompassHudConfig.java`), backed by new user-settable fields `customFaceRgb`, `customRingArgb`,
  `customMutedArgb`, `customNeedleArgb`. Defaults mirror `CLASSIC_GOLD` so a fresh Custom theme
  doesn't render black/broken before the player touches a slider. `CompassHud.analogColors()` and
  `LatitudeHudStudioScreen.themeLabel()` both have an exhaustive `case CUSTOM ->` arm (compiler-
  enforced). This supersedes the theme count recorded in `compass-themes-20260622.md` (11 constants
  at that point) — that doc is left as-is since it's a point-in-time evidence record of the 6-theme
  slice shipped that day; this row is the current count of record (12).
- **HUD Studio restructured into 4 tabs** (Compass / Placement / Title / General), replacing the old
  Target: Compass/Title/Both cycle button. Same themed-card look (bordered panel, gold accents,
  heading) as the Settings and World Creation screens. Widgets are constructed per-tab instead of
  all-at-once-with-visibility-toggles.
- **RGB color pickers** (3 sliders + live swatch) for the new Custom analog theme, plus an RGB
  picker for HUD text color and a previously-unexposed text opacity slider.
- **Removed `LatitudeHudAdjustScreen.java`** (469 lines) — confirmed dead code with zero
  construction call sites anywhere in `src/main/java` (verified via repo-wide grep before and after
  deletion). It was an earlier, narrower HUD-tuning screen superseded by `LatitudeHudStudioScreen`
  (a strict superset: same digital-compass fields plus analog theme, zone/biome/coords display,
  detachable elements). No keybind, ModMenu entry, or mixin ever referenced it — `GlobeModMenu.java`
  is an empty compile-safe stub, and the only settings keybind (F9) opens `LatitudeSettingsScreen`,
  whose "HUD Studio" button opens `LatitudeHudStudioScreen`. No other file required edits since
  nothing imported the dead class.

## Proof (green)

- Repo-wide grep for `LatitudeHudAdjustScreen` returns zero matches post-deletion.
- `JAVA_HOME=<temurin-25> ./gradlew compileJava` → `BUILD SUCCESSFUL` on the current worktree HEAD.
- Confirmed via `git stash`/restore diffing that an unrelated pre-existing compile break (missing
  `case CUSTOM` in an exhaustive switch, mid-edit at the time) was independent of the deletion, and
  was resolved by the time of this row (both `themeLabel()` and `analogColors()` now cover
  `CUSTOM`).

## Residual / next

- `compass-themes-20260622.md`, `gui-bookui-compass-bonuschest-20260622.md`,
  `1.4.1-prerelease-punchlist.md`, and `evidence-registry.md` row `20260622-compass-themes` still
  say "11 themes" — intentionally left unedited (append-only evidence; accurate for that date). This
  row is the canonical current count (12, including Custom).
- Optional cleanup (carried over, not done): the 16×16 compass PNG assets are dead (zero refs).
