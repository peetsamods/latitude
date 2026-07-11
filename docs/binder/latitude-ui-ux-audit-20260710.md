# Latitude UI/UX + Accessibility Audit — 2026-07-10

**Branch:** `port/canonical-26.2-pivot` · **HEAD:** `424986ca` · **Scope:** all bespoke Latitude
screens (READ-ONLY on `src/`). Vanilla screens excluded.

**Goal (Peetsa):** move the menus from "menu-hell" to *"ooh this is fun! so easy!"* — a real
accessibility + UX pass grounded in recognized game/web-UI principles. Peetsa is a non-programmer
and a self-described motion-sensitive person; findings are written in plain language with the
principle named.

**Surfaces audited**
- `client/create/LatitudeCreateWorldScreen.java` (2470 lines) — world create: name/seed/shape/size,
  atlas planisphere preview, spawn-zone list, Rules rail (World Type, Game Mode, Commands, Starting
  Compass, HUD Studio, Generate Structures, Bonus Chest, Game Rules), 3-column ↔ tabbed fallback.
- `client/LatitudeHudStudioScreen.java` (2001 lines) — 5 tabs (Compass/Labels/Title/Presets/General),
  many CycleButtons + sliders, drag-and-drop HUD editing, undo/redo, preset slots.
- `client/SpawnZoneScreen.java` (105 lines) — in-world server-driven starting-latitude picker.
- In-game HUD configurability surface: `client/ClientKeybinds.java`, `GlobeModClient.java`.
- Supporting: `client/create/LatitudePlanisphereRenderer.java`, `LatitudeWorldLauncher.java`,
  `CompassHudConfig.java`, `RainbowText.java`.

## Severity counts

| Severity | Count |
|---|---|
| CRITICAL | 3 |
| HIGH | 7 |
| MEDIUM | 8 |
| POLISH | 6 |
| **Total** | **24** |

Effort sizing: **S** ≈ ≤½ day · **M** ≈ 1–2 days · **L** ≈ 3+ days.

---

## CRITICAL

### C1 — The 13-state "Color Scheme" cycle button is un-navigable (blind cycling)
**Where:** `LatitudeHudStudioScreen.java:295-305` (created), `:1686-1701` (`themeLabel` enumerates
13 values: Pale Gold, Red & Ivory, Cyan Steel, Mint Brass, Obsidian & Red, Arctic Blue, Emerald,
Royal Purple, Sunset, Monochrome, Classic Gold, Aurora, Custom).
**Principle:** *Control affordance / recognition over recall (Nielsen #6).* A MC `CycleButton`
advances **one state per click with no overview**. To reach "Sunset" the user may click up to 12
times; overshooting by one costs a **full 13-click lap** back around. There is no way to see all
options at once or jump directly. This is the single worst control in the mod and it governs the
compass's whole look — the highest-value setting is the hardest to operate.
**Recommendation:** Replace with a **swatch grid / pop-open palette**: a button that opens a small
panel of 13 labeled color chips (each drawn with `ctx.fill`, the same primitive the sidebar swatches
already use at `:1287-1298`), click-to-pick, closes on selection. Each chip shows the theme's actual
face+ring colors so it is a live preview, not a name. Keep "Custom" as the last chip that reveals the
RGB sliders. **Size: M.**

### C2 — Snap-to-grid is undiscoverable (buried, mislabeled)
**Where:** `LatitudeHudStudioScreen.java:577-586` — a `CycleButton` on the **Labels** tab whose row
label is **"Dragging"** and whose values read **"Snap to Grid" / "Free Move"**.
**Principle:** *Discoverability / match between system and the real world.* Peetsa himself could not
find snap-to-grid. Nothing about "Labels → Dragging" signals "grid snapping for HUD placement," and a
user dragging the *compass* (a Compass-tab concept) would never look on the Labels tab. The setting
that most affects the core drag interaction is hidden two layers deep under a non-obvious noun.
**Recommendation:** (1) Rename the row to **"Grid Snap"** with an explicit on/off; (2) surface it in
the drag surface itself — when a drag begins, draw a faint one-line hint ("hold to move · snapping to
8px grid") near the grabbed element, and/or a small always-visible grid toggle chip in a persistent
Studio toolbar rather than inside a tab. (3) Longer term, move all drag-behavior controls (snap,
grid size, "Title Draggable") into a single **"Placement"** group visible regardless of tab.
**Size: S** (rename + hint) / **M** (persistent toolbar).

### C3 — HUD Studio is nearly undiscoverable in-game
**Where:** `ClientKeybinds.java:23-28` (bound to **F9**), consumed in `GlobeModClient.java:184-193`.
The create-world screen's entry is buried in the Rules rail scroll list (`:571-574`). There is **no
pause-menu button** and no on-screen hint that F9 exists.
**Principle:** *Discoverability / recognition over recall.* A keybind the user must already know
about (or hunt for in Controls → "globe" category) is invisible to a new player. The one place a
non-expert reliably looks — the pause menu ("Options…", "Achievements…") — offers no Latitude entry.
The mod's flagship customization surface is effectively hidden after world creation.
**Recommendation:** Add a **"Latitude HUD Studio"** button to the pause menu (mixin into
`PauseScreen` the same way create-screen is redirected), and add a one-time toast/tip on first world
entry ("Press F9 to customize your compass & HUD"). Keep F9 as the power-user shortcut.
**Size: M.**

---

## HIGH

### H1 — Toggling a control instantly collapses/expands the layout (no transition)
**Where:** HUD Studio `updateSidebarVisibility()` (`:1382-1442`) is called synchronously from every
display/placement toggle — e.g. `Display Zone in HUD` (`:464`), `Zone Placement` (`:475`), `Display
Biome` (`:491`), `Coords Placement` (`:528`), `Dragging` (`:582`). Rows for Text Size / Placement
appear or vanish instantly, shifting everything below them. Worse, picking Custom theme (`:302`),
Tape look (`:272`), Compass Style (`:239`), or Custom title color (`:669`) calls a **full
`this.init()` rebuild** — the whole sidebar snaps to a new row set in one frame.
**Principle:** *Motion & change / respect the vestibular system (WCAG 2.3.3, "Animation from
interactions").* Peetsa explicitly flagged sudden layout jumps as a "sensitive person" concern.
Abrupt appearance/disappearance and content jumping under the pointer is disorienting and can
trigger nausea.
**Recommendation:** (1) Add a short height/opacity ease when rows enter/leave (the create screen
already proves the pattern with `advanceScrollAnimation`/`easeScroll`, `LatitudeCreateWorldScreen.java:1311-1332`
— port the same easing helper). (2) Prefer **disable-in-place over remove**: grey out a now-irrelevant
row instead of deleting it so nothing below reflows. (3) Gate all of this behind a **Reduce Motion**
setting (see C-pass / M5). **Size: M.**

### H2 — No "Cancel" / discard path in HUD Studio
**Where:** `LatitudeHudStudioScreen.java:860-866` — the only exit is **"Done"**, which *saves*.
Config writes also happen live on nearly every change (`CompassHudConfig.saveCurrent()` throughout).
There is no way to back out of an editing session and restore the state from when it was opened.
**Principle:** *Error recovery / user control and freedom (Nielsen #3 — "emergency exit").* Undo/redo
(`:749-767`) only covers preset Load/Import, not the dozens of live slider/cycle edits. A user who
experiments and dislikes the result must manually reverse every change or hit a coarse "Reset" that
also wipes settings they *did* want.
**Recommendation:** Snapshot the full `CompassHudConfig` + `LatitudeConfig` HUD fields in `init()`
(the `CompassHudConfig.fresh()`/capture machinery already exists — see `CompassHudPreset.captureCurrent()`
at `:722`). Add a **"Cancel"** button next to "Done" that restores the snapshot. Optionally defer
`saveCurrent()` until "Done" so Cancel is a pure in-memory revert. **Size: M.**

### H3 — Destructive preset actions have no confirmation or protection
**Where:** `LatitudeHudStudioScreen.java:786-793` (**Save** overwrites a slot silently),
`:795-803` (**"x"** clears a slot silently). Per-tab **Reset** buttons (`:425`, `:593`, `:705`) and
**Reset HUD** (`:847`) also wipe settings with no confirm.
**Principle:** *Error prevention (Nielsen #5) / confirmation of destructive, irreversible actions.*
Saving into an occupied slot destroys the previous preset with one click; the tiny "x" sits right
next to the frequently-clicked Load button (mis-click risk, see M4). None of these are undoable
(the undo stack covers Loads, not Saves/Clears/Resets).
**Recommendation:** (1) For Save-over-occupied and Clear, require a second click ("Save" → "Sure?")
or a small inline confirm, matching vanilla's "hold to confirm" idiom. (2) Extend the existing
undo/redo stack to cover Save/Clear/Reset, or at minimum show a 3-second "Undo" affordance after a
destructive action. **Size: M.**

### H4 — Silent success/failure on Export & Import (no feedback)
**Where:** `LatitudeHudStudioScreen.java:721-728` (**Export to Clipboard** — copies, shows nothing),
`:730-741` (**Import from Clipboard** — on invalid clipboard "does nothing," per its own tooltip).
**Principle:** *Feedback / visibility of system status (Nielsen #1).* A button that produces no
visible or audible result leaves the user unsure it worked. Import failing silently is worse — the
user pastes a bad string and cannot tell whether nothing happened or the import "took."
**Recommendation:** Flash a transient confirmation line ("Copied HUD look to clipboard" / "HUD look
applied" / "Clipboard didn't contain a valid HUD look") drawn for ~2s, reusing the transient-text
pattern from H4/H2. Play the vanilla UI click on success. **Size: S.**

### H5 — Zone rows give no click sound; feedback is inconsistent with keyboard
**Where:** `LatitudeCreateWorldScreen.java:2383-2386` — `ZoneRowWidget.onClick` calls `select()`
with **no** `playDownSound`. The keyboard path `keyPressed` (`:2388-2397`) **does** call
`this.playDownSound(...)`. So selecting a spawn zone with the mouse is silent, but with the keyboard
it clicks.
**Principle:** *Feedback / consistency (Nielsen #1, #4).* Every other button in the mod (vanilla
`Button`) clicks on press; the most important choice on the create screen — where you start — is the
one that doesn't. The mouse/keyboard mismatch is a latent accessibility inconsistency.
**Recommendation:** Call `this.playDownSound(Minecraft.getInstance().getSoundManager())` in
`onClick` before `select()`. Consider a subtle selection flash (the gold highlight already exists at
`:2417-2423`; a brief brighten on newly-selected would reinforce it). **Size: S.**

### H6 — Everything is a stepper or a cycle; no overview of multi-state choices
**Where:** Create screen uses ◀▶ steppers for **World Type** (3 states, `:520-531`), **Game Mode**
(3, `:526-531`), **World Shape** (2, `:477-484`), **World Size** (6, `:488-497`). HUD Studio uses
CycleButtons for Direction Format (3), Compass Look (5), Background Color (4), Text Color (5), Title
Color (7), Title Case (4), Show HUD (3), Preview Text (3).
**Principle:** *Control affordance / recognition over recall.* Steppers and cycles hide the option
set: you cannot see that there are 6 sizes or which of 7 title colors exist without clicking through.
For small enumerations (2–4) a **segmented control** (all options visible, one click to pick) is
strictly better; for ordered ranges (World Size) a labeled position indicator ("4 / 6 · Regular")
tells the user where they are.
**Recommendation:** Convert 2–5-state pickers to **segmented buttons** (a row of small labeled cells,
selected one highlighted gold — same fill primitives as the zone bar at `:1425-1440`). Keep steppers
only for World Size (ordered) but add a "n / 6" position hint and a dotted progress row. Text/Title
color pickers should reuse the C1 swatch-grid pattern. **Size: L** (touches many rows; can be phased).

### H7 — Low-contrast muted text on the dark panel fails readability thresholds
**Where:** `MUTED = 0xFF8C8078` used pervasively for helper/body/label text on `PANEL_BG =
0xFF3A302A` (HUD Studio `:24-26`; create screen `:56-58`, e.g. zone helper text `:2454`, settings
row labels `:1496-1504`, size descriptions `:1547`). `DISABLED_COLOR = 0xFF605850` (`:128`) for
greyed rows is lower still.
**Principle:** *Readability / contrast (WCAG 1.4.3).* `#8C8078` on `#3A302A` is roughly a **2.7:1**
contrast ratio — below the 4.5:1 minimum for body text (and below 3:1 for large text). Helper copy
("Dense jungles, warm rivers…", climate descriptions) is exactly the explanatory text a new player
most needs to read, and it is the least legible. On the classic MC dirt background the theme panels
help, but the muted-on-dark body text is still under threshold.
**Recommendation:** Lighten body/helper text to ≥ `WARM_WHITE`-adjacent (`0xFFC9BCA8` ≈ 4.6:1) and
reserve `MUTED` for genuinely secondary captions only. Never use `MUTED` for the *primary* helper
sentence of a control. Keep `DISABLED_COLOR` for disabled state only, where low contrast correctly
signals "inactive." **Size: S.**

---

## MEDIUM

### M1 — World Shape labels are stale vs. the agreed rename
**Where:** `LatitudeCreateWorldScreen.java:126` —
`WORLD_SHAPE_NAMES = { "Mercator 2:1", "Legacy 1:1" }`.
**Principle:** *Consistency & standards / match the real world.* The binder records the agreed
player-facing rename to **"Wide 2:1"** and **"Square 1:1"** (drop "Mercator"/"Legacy" jargon) —
`phase5-boundary-experience-plan-20260709.md:212`. Non-programmers don't parse "Mercator"; "Wide/
Square" describe the shape. The code still ships the old jargon.
**Recommendation:** Rename to `{ "Wide 2:1", "Square 1:1" }` and audit any other "Mercator"/"Legacy"
user-facing strings (worldDimsLabel comments, atlas caption). **Size: S.**

### M2 — Boolean states use three different verb pairs across the same screen
**Where:** HUD Studio: most toggles read **ON/OFF** (e.g. `:336`, `:366`, `:459`); Zone/Biome/Coords
Placement read **FOLLOW/DETACH** (`:470`, `:497`, `:523`); Dragging reads **Snap to Grid/Free Move**
(`:577`). The create screen's rail uses plain **ON/OFF** toggle Buttons (`:532-560`).
**Principle:** *Consistency & standards (Nielsen #4).* Three vocabularies for on/off state force the
user to re-learn "which word means enabled" per control. FOLLOW/DETACH is arguably clearer than
ON/OFF for placement — but then ON/OFF elsewhere is the inconsistent one.
**Recommendation:** Pick one convention per *concept*: use descriptive verb pairs everywhere a
boolean has a real-world meaning (Follow/Detach, Snap/Free, Show/Hide) and drop bare ON/OFF, OR
standardize on ON/OFF + a descriptive row label. Don't mix within a screen. **Size: S.**

### M3 — Two different widget patterns for the same on/off concept across screens
**Where:** HUD Studio implements booleans as `CycleButton<Boolean>` (`:336` etc.); the create
screen's rail implements the same booleans as plain `Button` that flips its own message
(`:532-560`). Visually and behaviorally they differ (a cycle vs. a toggle).
**Principle:** *Consistency & standards.* The same mental model ("flip this switch") has two looks.
A user who learns the create-screen toggles then meets cycle-style booleans in the Studio.
**Recommendation:** Adopt one shared "toggle" visual — ideally a segmented ON/OFF or an illuminated
switch (ties into the iconography work, B-pass) — and use it on both screens. **Size: M.**

### M4 — Preset row hit-targets are crowded; "x" abuts "Load"
**Where:** `LatitudeCreateWorldScreen`… (HUD Studio) `:770-806` — each preset row packs Load
(`loadW`), Save (44px), and Clear "x" (20px) with only 3px gaps. The destructive "x" sits one gap
from the Save button and two from Load.
**Principle:** *Target size & spacing (WCAG 2.5.5 target size ≥ 24×24 CSS-px; Fitts's Law).* A 20px
destructive control jammed against frequently-used ones invites mis-clicks — and per H3 the clear is
unconfirmed and un-undoable.
**Recommendation:** Widen the "x" to ≥ 24px, add more separation from Save, and/or move Clear to a
row-hover reveal so it isn't a standing click target. Pair with the H3 confirm. **Size: S.**

### M5 — Seed dice/copy buttons are undersized
**Where:** `LatitudeCreateWorldScreen.java:454-472` — the reroll (⚄) and copy (⧉) buttons are sized
`seedBtnW = fieldH` (≈16px square).
**Principle:** *Target size & spacing (WCAG 2.5.5).* 16px is below the comfortable 20px MC button
floor and the 24px WCAG target. They carry tooltips (good) but small icon-only controls are hard to
hit, especially at high GUI scale on small screens.
**Recommendation:** Grow to at least the 20px button height used elsewhere, or give them a bit of
horizontal padding. **Size: S.**

### M6 — Icon-in-label glyphs render tiny and are easy to miss
**Where:** Seed dice `⚄` / copy `⧉` (`:454`, `:464`); stepper arrows `◀/▶`
throughout; HUD Studio undo/redo which *had* to be hand-drawn scaled because the unicode arrow renders
"comically small" (`:743-745`, `drawButtonGlyph` scale **1.9×** at `:965-983`).
**Principle:** *Icon+label best practice / target legibility.* The undo/redo case is documented
proof that MC's unicode font renders control glyphs too small to read; the seed dice/copy and the
◀▶ steppers ship the *same* tiny-glyph problem the undo/redo fix worked around.
**Recommendation:** Standardize on the **`drawButtonGlyph` scaled-draw approach** (cited precedent)
for all icon-only controls, or move to a texture-atlas icon sheet. This is the technical enabler for
the B-pass iconography work. **Size: M.**

### M7 — Tab strips are text-only and slightly undersized
**Where:** HUD Studio tabs (`TAB_LABEL_SCALE = 0.85f`, `:37`; `drawTabStrip` `:1300-1335`) and the
create-screen tabbed fallback (`:2236-2268`) draw label-only tabs; HUD Studio scales the label to
0.85 to fit "Placement."
**Principle:** *Icon+label best practice / readability.* Scaling label text below 1.0 to fit is a
readability smell; text-only tabs also miss the recognition boost icons give.
**Recommendation:** Add a small icon per tab (compass / label / title / stack / gear) beside the
word, and widen tabs so labels render at full scale (ties to B-pass). **Size: M.**

### M8 — SpawnZoneScreen relies on color-coded labels for climate
**Where:** `SpawnZoneScreen.java:34-47` — zone buttons are colored via `ChatFormatting` (GREEN,
YELLOW, DARK_AQUA, AQUA, WHITE) with the name + a tooltip carrying the degree range.
**Principle:** *Readability / colorblind-safe signaling (WCAG 1.4.1 — don't use color alone).* The
color is decorative-plus-informative (it maps to climate), but the button face text itself doesn't
show the degree band — that's tooltip-only, so a player who doesn't hover, or who is colorblind, gets
name-only differentiation.
**Recommendation:** Put the degree range on the button face (as the create-screen zone rows already
do, `:2442-2447`) so climate is conveyed by text + range, not color alone. Keep tooltips for the
"what you'll find" flavor. **Size: S.**

---

## POLISH

### P1 — Transparency checkerboard aid only appears on hover/drag
**Where:** `LatitudeHudStudioScreen.java:882-888` — the checkerboard behind Inner Transparency shows
only while the slider is hovered or dragged. Good call (avoids visual noise), but the *first-time*
user may never learn it exists. **Principle:** discoverability. **Rec:** a one-line hint on first
open of the Compass tab. **Size: S.**

### P2 — "Reserved Width" / "Zone/Biome Order" effects are invisible until an edge case
**Where:** `:513-521` (order only matters when both are attached), `:564-572` (reserved width).
`updateSidebarVisibility` even hides the order row unless both labels are attached (`:1423-1425`) —
correct, but the user never sees *why* it appeared/vanished. **Principle:** feedback / discoverability.
**Rec:** when the row is hidden, optionally show it greyed with a "shown when both labels ride the
compass" note rather than removing it (also helps H1). **Size: S.**

### P3 — Reset buttons don't say what era they restore
**Where:** Reset Compass/Labels/Title/HUD (`:425`, `:593`, `:705`, `:847`). They restore
`CompassHudConfig.fresh()` defaults. **Principle:** feedback. Tooltips are decent, but a post-reset
transient confirmation ("Compass reset to defaults") would close the loop (pairs with H4). **Size: S.**

### P4 — Create screen has no seed-0 / whitespace guard surfaced to the user
**Where:** `beginExpedition` (`:1139-1167`) passes the raw seed through. (Prior audit
`ui-audit-20260707.md` flagged the seed-0 guard as missing.) **Principle:** error prevention.
**Rec:** trim + validate and show inline feedback if the seed is unusable, rather than silently
coercing. **Size: S.** (Cross-reference the earlier audit rather than duplicating scope.)

### P5 — Horizontal pane-strip scroll is a hidden interaction
**Where:** `:2287-2301` — when 3 columns don't fully fit, a thin gold horizontal scrollbar appears
below the panels. It's easy to miss and the interaction (shift-scroll / drag a 6px-tall bar) is
non-obvious. **Principle:** discoverability / target size. **Rec:** prefer the tabbed fallback more
aggressively (raise `COMFORTABLE_THREE_COL_W`) so users rarely hit the cramped horizontal-scroll
state; if kept, make the bar taller and add left/right nudge affordances. **Size: S.**

### P6 — "Create World" button label vs. "expedition" internal vocabulary
**Where:** Button reads "Create World" (`:588`); methods/logs say `beginExpedition` /
"expedition" (`:1139`, `:1152`). User-facing is fine; the mixed vocabulary is a maintenance smell and
risks a future label reverting to "Begin Expedition." **Principle:** consistency. **Rec:** pick one
term for user-facing copy and keep it stable. **Size: S** (or leave as-is; lowest priority).

---

## What already works (keep / build on)

- **Themed panels over the dirt background** (gold accents, `PANEL_BG`/`PANEL_BORDER`) read far better
  than raw MC widgets — the visual system is coherent.
- **Smooth-scroll easing** on the create screen (`advanceScrollAnimation`/`easeScroll`, `:1311-1332`)
  is exactly the motion-respecting pattern H1 needs ported into the Studio.
- **CycleButton tooltip-after-click bug is already fixed** via reflection (`patchCycleButtonTooltip`,
  `:1753-1762`) — tooltips survive clicks. Good.
- **Undo/redo for preset loads** (`:749-767`) and the **hand-drawn scaled glyph** helper
  (`drawButtonGlyph`, `:965-983`) are the right primitives — extend them (H3, M6).
- **Every control has a plain-language tooltip** — strong foundation for the icon+label+tooltip pattern.
- **Reserved-width / worst-case layout reservation** (`computeSizeLabelBottom`, `:689-695`) already
  prevents the atlas from "bouncing" when size changes — the anti-jank discipline exists; extend it.

## Recommended accessible control pattern (for the iconography ask)

Peetsa wants an **icon-based Rules sidebar** (compass icon that illuminates for Starting Compass,
village outline for Generate Structures, chest-with-glitter for Bonus Chest…). The accessible pattern
for each such control is **all five of**:

1. **Icon** — code-drawn (via the `drawButtonGlyph` scaled-draw precedent, `:965-983`) or a
   texture-atlas sprite. **Never** a bare MC unicode glyph (proven too small — undo/redo comment
   `:743-745`, seed glyphs M6).
2. **Short text label** beside/under the icon (icon-only fails colorblind + recognition; WCAG 1.4.1).
3. **Tooltip** with the plain-language explanation (already the house style).
4. **Illuminated / lit state** to show ON vs OFF (a brightened icon + gold glow), *plus* a text or
   shape cue so state isn't color-only (WCAG 1.4.1) — e.g. lit compass + "On".
5. **Click sound + a brief state flash** on toggle (feedback; fixes the H5-class silence).

Drawing constraints to honor: icons must be **code-drawn or texture-atlas** (no reliance on font
glyphs); reuse `ctx.fill` swatch primitives (`:1287-1298`) and the 1.9× scaled-draw helper; keep the
lit-state animation gentle and behind Reduce Motion (M5/C-pass).

---

## Proposed 3-pass implementation sequence (parallel-safe by file)

Passes **A** and **B** touch disjoint files and can run **concurrently**. Pass **C** is cross-cutting
and must run **after** A and B land (it re-touches both screens plus config), so it is **sequential**,
not concurrent with A/B.

### Pass A — HUD Studio controls (file: `LatitudeHudStudioScreen.java` only)
- C1 swatch-grid Color Scheme picker (+ Text/Title color pickers, H6 subset).
- C2 rename "Dragging" → "Grid Snap" + drag hint.
- H2 Cancel/discard (snapshot + restore).
- H3 preset Save/Clear confirm + extend undo stack.
- H4 Export/Import transient feedback.
- P1/P2/P3 hints & post-action confirmations.
*Self-contained; no other file changes required.*

### Pass B — Create-screen sidebar iconography (file: `LatitudeCreateWorldScreen.java` only, plus a
new `RulesIcon`/atlas helper if desired — a **new** file, no overlap)
- Iconography sidebar per the 5-part pattern above (icon + label + tooltip + lit state + sound).
- H5 zone-row click sound.
- H6 segmented controls for World Type / Game Mode / World Shape (create-screen half).
- M1 Wide/Square rename.
- M4 preset-row spacing / M5 seed-button sizing (create-screen half).
- M8 degree-on-face is in SpawnZoneScreen — assign to Pass B only if B also owns SpawnZoneScreen;
  otherwise keep M8 in Pass C. (SpawnZoneScreen is a separate small file → safe either way.)
*Depends on M6's scaled-glyph helper; that helper can be lifted from the HUD Studio precedent without
editing the Studio file (copy the static method or extract to the new helper file).*

### Pass C — Cross-cutting polish + Reduce Motion (files: `LatitudeConfigData.java`/config,
`SpawnZoneScreen.java`, and a shared motion/easing helper — plus a **second, sequential** revisit of
both screens for H1/M2/M3/M7)
- **Reduce Motion** setting (new config field) gating H1 easing, the atlas Random sweep, band shimmer,
  rainbow text, and any lit-state pulse.
- H1 row enter/leave easing in the Studio (port `easeScroll`).
- M2 unify boolean verb vocabulary; M3 unify toggle widget; M7 tabbed icons + full-scale labels.
- M8 SpawnZoneScreen degree-on-face; P4 seed guard; P5 horizontal-scroll threshold; P6 vocabulary.
*Because H1/M2/M3/M7 re-edit both screens, run C **after** A+B merge to avoid file conflicts.*

---

## Cross-references
- Prior surface audit + overhaul design: `ui-audit-20260707.md`,
  `../design/hud-layout-overhaul-design-20260707.md` (pin+grow model, F9→Studio consolidation).
  This audit is the **accessibility/UX** lens; that one is the **layout-primitive** lens — complementary.
- Shape rename decision: `phase5-boundary-experience-plan-20260709.md:212`.
- Plain-language reporting rule: MEMORY `peetsa-plain-language-reports`.
