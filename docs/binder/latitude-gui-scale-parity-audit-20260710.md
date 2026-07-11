# Latitude GUI-Scale Parity Audit — 2026-07-10

**Auditor:** GUI-scale parity lane (read-only over `src/`).
**Branch:** `port/canonical-26.2-pivot`.
**Mission (Peetsa):** "Check for GUI parity, such that if a higher/lower GUI ratio is used by the
player, it doesn't break everything. Everything should still be easy to use."
**Scope:** every Latitude-owned bespoke screen + in-game HUD overlay. No `src/` edits, no gradle, no
world-gen. Static analysis only (see "What could only be assessed statically" at the end).

---

## 0. The one fact that frames everything (read this first)

Minecraft's **GUI Scale** setting (Auto / 1 / 2 / 3 / 4) does **not** rescale each widget — it changes
the **effective UI resolution**. `Screen.width`/`Screen.height` and `Window.getGuiScaledWidth/Height()`
are already in *gui-pixels*. At scale 4 on a 1080p monitor the whole UI is a **480×270** canvas; at
scale 1 on that same monitor it is **1920×1080** with everything looking tiny.

**Vanilla clamps the effective resolution to a floor of 320×240** (`Window.calculateScale` refuses any
scale that would drop `guiScaledWidth < 320` or `guiScaledHeight < 240`, *regardless of the user's
chosen setting*, as long as the OS window is at least that big). So the practical stress band Latitude
must survive is:

| Case | Effective gui-pixels | Notes |
|---|---|---|
| **Hard floor** | **320 × 240** | small window / forced high scale — the true worst case |
| Scale 4 @ 1080p | 480 × 270 | the case Peetsa named |
| Scale 2 @ 1080p (typical) | 960 × 540 | comfortable |
| Scale 1 @ 1080p / 4K | 1920×1080 / 3840×2160 | everything physically tiny (this is *vanilla* behavior, not a Latitude bug) |

**Headline result:** the two big bespoke screens are, on the whole, **well defended**. The create
screen actively drops to a tabbed layout below 530 gui-px and scroll-clips every pane; the compass and
its detached labels store position as **screen fractions** and re-clamp every frame, so they survive a
scale change cleanly. The real problems are narrower and specific — one signature overlay stores its
position in **absolute pixels** and never re-clamps (drifts off-screen after a scale change), and HUD
Studio's sidebar is a **fixed 208 px** that never shrinks. No finding rises to "the default scale is
unusable," so there are **0 CRITICAL**; the ranking below is honest about that.

---

## Severity counts

| Severity | Count |
|---|---|
| CRITICAL (unusable at a common scale) | **0** |
| HIGH | **3** |
| MEDIUM | **5** |
| POLISH | **4** |

---

## Top 5 one-liners

1. **HIGH — Zone-enter title drifts off-screen after a GUI-scale change.** It's stored as an *absolute*
   pixel offset from center and never re-clamped at gameplay render, unlike the compass which uses
   fractions (`ZoneEnterTitleOverlay.java:79-81`, offset type `int` at `LatitudeConfigData.java:68-72`).
2. **HIGH — HUD Studio sidebar is a hard-coded 208 px** with no narrow-screen fallback; at the 320-px
   floor it eats 66% of the width and the centered Done/Cancel overlaps it (`LatitudeHudStudioScreen.java:57, 973-984`).
3. **HIGH — `scaledUi()`/`compactUi()` are no-op passthroughs** (`return px`) — the create screen's
   entire "scale" vocabulary is a stub; it survives purely because it keys off the already-gui-scaled
   `this.width/height`, which is correct but means the names lie (`LatitudeCreateWorldScreen.java:671-677`).
4. **MEDIUM — Scaled title text is never horizontally clamped.** A long title at scale 3 on a 320-px
   screen overflows both edges (zone-enter *and* hemisphere) (`ZoneEnterTitleOverlay.java:90-106`,
   `HemisphereTitleOverlay.java:120-127`).
5. **MEDIUM — Warning line isn't wrapped/fitted.** `drawCenteredWarning` centers a single unbroken
   string clamped only to `x>=4`; a long pole/storm warning runs off a 320-px screen
   (`GlobeWarningOverlay.java:378-383`).

---

## What is already defensive (so we don't "fix" what works)

Cite these as the good pattern to copy:

- **Compass + detached zone/biome/coords labels are fraction-anchored and re-clamped every frame.**
  Positions are `offXFrac`/`offYFrac` fed through `HudLayoutMath.pinX/pinY(…, screenW/H)` and then
  `x = clamp(x, 0, screenW - boxW)` (`CompassHud.java:231-236, 283-294`). Fractions are
  resolution-independent, so a scale change re-derives a sensible pixel position and re-clamps — the
  element stays on-screen and proportionally placed. **This is the reference design.**
- **Attach-to-hotbar dock is geometry-derived, not stored pixels.** `HudLayoutMath.dock(...)` places the
  compass beside the real hotbar/offhand/attack-indicator boxes and clamps; it recomputes from
  `screenW/H` each frame (`CompassHud.java:224-236`). Scale-safe by construction.
- **Create screen has a genuine responsive fallback.** Below `COMFORTABLE_THREE_COL_W = 530` gui-px it
  drops the three-column layout for a tabbed one (`:68, 422-424`); every pane scissor-clips and has its
  own scrollbar, so vertical cramping degrades to scrolling, never to clipped-off controls.
- **All bespoke text goes through bounded/ellipsizing helpers.** `drawBoundedText` /
  `drawCenteredBoundedText` *return false and draw nothing* when the rect is too short, and
  `ellipsizeToWidth` truncates with "…" (`:2109-2141`). The atlas has a shrink-to-fit loop that simply
  omits the map rather than clipping if it can't fit (`:1608-1617`). The LATITUDE wordmark falls back to
  a plain line if the 1.5× draw won't fit (`:1914-1916`).
- **Swatch dropdown is fully scale-aware.** `computeGeom()` measures space below vs. above, **up-flips**
  when there's more room above, caps `visibleRows` to what fits, and adds an internal scrollbar — so the
  13-entry Color Scheme list fits and scrolls even at 240 px tall (`SwatchDropdown.java:237-255`).
- **Both screens re-run `init()` on resize** (Screen contract) and recompute layout every frame in
  `extractRenderState`, so changing GUI scale *while a screen is open* re-lays-out correctly — no stale
  geometry.
- **Full-screen atmosphere fills are inherently scale-safe.** Polar whiteout and EW haze fill
  `0,0 → guiWidth,guiHeight` (`PolarWhiteoutOverlayHud.java:74`); the whisper line is center-anchored and
  clamped `x>=2` (`LatitudeWhisperOverlay.java:92`).

---

## HIGH findings

### H1 — Zone-enter title stores an ABSOLUTE pixel offset and is never re-clamped at render
`ZoneEnterTitleOverlay.render()` computes `cx = screenW/2 + zoneEnterTitleOffsetX`,
`cy = screenH/2 + zoneEnterTitleOffsetY` with **no clamp** (`:79-81`). The offsets are persisted as
`int` pixels (`LatitudeConfigData.java:68-72`; defaults `0, -40`). HUD Studio's drag *does* clamp — but
only to the *editing* screen (`LatitudeHudStudioScreen.java:1271-1272` stores `newCx - width/2` after a
screen-relative clamp). So the sequence: drag the title toward an edge at **GUI 1** (large canvas) →
stored offset is large (say +700) → later switch to **GUI 4** (480 wide) → title center lands at
`240 + 700 = 940`, entirely off the 480-px screen. The title is a **signature feature** (announces every
zone crossing) and it vanishes silently, persisting across sessions with no way for the player to know
why. Contrast the compass, which survives the same sequence because it's fraction-based.
- **Trigger honesty:** requires a deliberate reposition *then* a scale change. Not a default-scale
  problem — which is why it's HIGH, not CRITICAL. But the failure mode (total, silent, sticky) is bad.
- **Fix (S):** clamp at render. After computing `cx/cy`, clamp so the measured styled title box
  (`styledWidth()` × `scale`, `lineHeight × scale`) stays fully on-screen — `cx = clamp(cx, halfW,
  screenW - halfW)` etc. Best: convert `zoneEnterTitleOffsetX/Y` to fractions of `screenW/H` at
  save-time (the compass pattern) so it also stays *proportionally* placed; that's an M if you migrate
  the persisted field, S if you just clamp the pixels.

### H2 — HUD Studio sidebar is a fixed 208 px with no narrow-screen adaptation
`sidebarWidth = 208` is a hard constant (`LatitudeHudStudioScreen.java:57`) and there is **no** `width <`
branch anywhere in the screen (grep-confirmed) — unlike the create screen's tabbed fallback. Consequences
at the 320-px floor: the sidebar card (`sidebarWidth + 4 = 212`) covers **66%** of the width, leaving a
~108-px strip of live preview; and the **centered** Done/Cancel group (`groupX = (width-200)/2`,
`:973-984`) spans 60→260 while the sidebar occupies 8→220 — they horizontally overlap. Even at the named
480-px case the Done/Cancel group (140→340) overlaps the sidebar (8→220). It's not *unusable* — `L`
toggles the sidebar off and the preview underneath spans the full screen, and the buttons draw on top so
they stay clickable — but it's cramped and visually collides, worst at small effective resolutions.
- **Fix (M):** make `sidebarWidth = min(208, width - <preview-min, e.g. 120>)` and clamp the tab-label
  scale / row widget widths to it; and/or bottom-anchor Done/Cancel to the sidebar column (left-aligned
  at `panelX`, width `widgetW`) instead of screen-center so they never underlap the card. A quick S
  interim: only center Done/Cancel when `width` is wide enough that the group clears the sidebar,
  otherwise left-align them in the rail.

### H3 — `scaledUi()` / `compactUi()` are no-op stubs (`return px`)
`private int scaledUi(int px){ return px; }` and `compactUi(px){ return scaledUi(px); }`
(`LatitudeCreateWorldScreen.java:671-677`). Every "scaled" spacing constant on the create screen
(`scaledUi(42)`, `scaledUi(38)`, etc.) is therefore a **raw gui-pixel literal**. This is *not currently a
bug* — the screen adapts because its top-level split keys off the already-gui-scaled `this.width/height`
and everything below is relative — but the naming actively misleads a future maintainer into thinking
there is a DPI/scale-compensation layer that does not exist, and `isCompact()` (`:388-390`, returns
`this.width < 480`) is **dead code** (never called). The risk is a future edit that *assumes* these do
something and introduces a real regression.
- **Fix (S):** either inline the literals and delete the stubs + dead `isCompact()`, or give them a real
  body (e.g. tighten spacing when `this.width < 480`) and use it. Doc-comment the "we key off gui-scaled
  width, there is no per-pixel scaling" invariant so nobody re-adds a phantom scale layer.

---

## MEDIUM findings

### M1 — Scaled title text has no horizontal clamp (overflow at high scale × narrow screen)
`drawTitleLineAt` translates+scales the pose and centers the string at the local origin
(`ZoneEnterTitleOverlay.java:90-106`); scale ranges to 3.0 (`GlobeWarningOverlay.java:308`). A wide title
("Subtropical Frontier", a long biome name) at scale 3 on a 320-px screen is ~far wider than 320 and
spills off both edges. Applies to the hemisphere title too (`HemisphereTitleOverlay.java:120-127`,
center-anchored so no drift, but same overflow). Transient, cosmetic, but it can render the title
unreadable at the floor.
- **Fix (M):** clamp the *effective* scale down when `styledWidth × scale > screenW - margin` (fit-to-width),
  or shrink-to-fit like the atlas loop. Cheapest S: cap scale so the widest expected title fits 320.

### M2 — Warning line isn't wrapped or width-fitted
`drawCenteredWarning` centers one unbroken `Component` clamped only to `x >= 4`
(`GlobeWarningOverlay.java:378-383`); pole/storm warnings can be long. On a 320-px screen the tail runs
off the right edge. `warnY = screenH - 68` is floored at 18 (`:333-336`) so vertical is fine; only
horizontal overflows.
- **Fix (S/M):** wrap to `screenW - 8` (multi-line, stacking upward from `warnY`), or ellipsize. Vanilla
  action-bar text has the same limitation, so this is quality-of-life, not correctness.

### M3 — HUD Studio bottom controls are height-anchored with fixed gaps; thin margin at 240 px tall
`sidebarViewportBottom = max(panelY+24, height-60)`, `resetY = height-52`, Done/Cancel at `height-28`,
sidebar card bottom `height-22` (`:292, 959, 974, 1024`). At the 240-px floor these resolve to
188 / 212 / 218 respectively — Done's top (212) sits 6 px *above* the card bottom (218), a minor overlap
that exists at **every** width (not scale-specific) but is most visible when the sidebar is tall. Works,
looks slightly stacked.
- **Fix (S):** drop the card bottom to `height-30` (above Done) or move Done to `height-24` so the 6-px
  overlap disappears.

### M4 — Digital compass at max scale (3.0) can clip off a narrow screen
`scaledBoxW = ceil(boxW × scale)` then `x = clamp(x, 0, screenW - scaledBoxW)`
(`CompassHud.java:213, 235`). If `scaledBoxW > screenW` (long compass line × scale 3 on 320 px), the
clamp pins `x = 0` and the right end clips off. Self-inflicted (extreme user setting × narrow screen) and
the left/important end stays visible, so low urgency.
- **Fix (M):** when `scaledBoxW > screenW`, reduce the effective scale to fit, or note the ceiling in the
  slider tooltip. Or leave as-is (documented user extreme).

### M5 — Create-screen atlas can silently vanish on very short panels
The preview fit loop returns `null` (draws nothing) when the composition can't fit at radius ≥ 18
(`LatitudeCreateWorldScreen.java:1608-1617`), and the whole block is gated on
`leftPreviewBottomY - leftPreviewTopY >= 30` (`:1394`). At the 240-px floor in tabbed mode the World
pane is short enough that the atlas may not draw at all — correct *degradation* (no clip), but the
signature planisphere just disappears with no placeholder.
- **Fix (S):** when the atlas can't fit, draw a tiny "map hidden — widen the window" hint instead of
  empty space (parallels the existing `renderPlanisphereDisabled` placeholder).

---

## POLISH findings

- **P1 — Fixed-pixel decorative art doesn't scale with content.** Sparkle motes (1–2 px,
  `:1981-1992`), the wordmark's flanking diamond rules (`:2000-2004`), RulesIcons at `ICON_SIZE = 16`
  (`:2661`), grid decoration `GRID_STEP = 16` (`:2295`). These are the *same relative size as vanilla
  chrome* at every scale (fine), but at scale 1 on a 4K panel they're physically tiny — inherent to MC,
  noted for completeness, no action.
- **P2 — Tab-label scale is a magic float.** `TAB_LABEL_SCALE = 0.85f`
  (`LatitudeHudStudioScreen.java:41`) assumes the 5 labels fit the 208-px strip; if H2's dynamic width
  lands, this must derive from the actual per-tab width or labels will overrun. Couple them.
- **P3 — `RulesIconRow` label ellipsizes but On/Off does not.** At the narrowest 3-col rail
  (`MIN_RAIL_W = 130`) the label is fit via `ellipsizeToWidth(label, labelMaxW)` (`:2786`) while the
  right-side state word is drawn at a fixed offset (`:2793`). Fine at 130, but verify the label never
  collides the state at exactly `railW = 130`; consider reserving the state width first.
- **P4 — Non-integer drag positions.** Title drag keeps a `double` offset
  (`titleOffsetXf/Yf`) rounded to int only on store (`:1052-1053, 1271-1272`); label drags are int
  throughout. No sub-pixel drift observed (everything rounds before `ctx.fill/text`), but the mixed
  int/double bookkeeping is a latent inconsistency worth unifying.

---

## Parallel-safe fix-pass plan

Findings cluster cleanly by file, so up to three lanes can run without touching the same source:

- **Lane A — Overlay position/clamp hardening** *(overlay files; no screen files)*
  H1 + M1 + M2. Files: `ZoneEnterTitleOverlay.java`, `HemisphereTitleOverlay.java`,
  `GlobeWarningOverlay.java`, and the persisted field in `LatitudeConfigData.java` /
  `LatitudeConfig.java` (only if migrating the title offset to fractions). Self-contained — the fraction
  migration is the only cross-file touch and it's additive.
- **Lane B — HUD Studio layout** *(one file)*
  H2 + M3 + P2. File: `LatitudeHudStudioScreen.java` only. Dynamic sidebar width, Done/Cancel anchoring,
  card-bottom gap, tab-label scale coupling. Independent of Lane A and Lane C.
- **Lane C — Create-screen cleanup** *(one file)*
  H3 + M5 + P3. File: `LatitudeCreateWorldScreen.java` only. Delete/rename the `scaledUi/compactUi`
  stubs + dead `isCompact`, add the atlas-hidden placeholder, verify the rail-label/state collision.
  Independent.
- **Sequential after A–C:** M4 (`CompassHud.java` — overlaps nothing above but is optional) and P4
  (int/double unification, touches both HUD Studio + overlays, so do it last to avoid conflicts).

Suggested order if serial: **A → B → C** (A fixes the only real off-screen loss; B fixes the most-visible
cramping; C is naming/polish).

---

## What could only be assessed statically (no live render)

Everything here was read from source; **none of the geometry was verified on a running client.** A live
GUI-scale session should confirm:
- Whether H1's off-screen title actually disappears (and whether the compass truly stays put) across a
  scale switch on a *saved* world with a repositioned title.
- Whether H2's sidebar/preview split at 480 and 320 gui-px is merely cramped or genuinely blocks
  interaction (e.g. an element you can't reach without `L`).
- Whether M1/M2 overflow is legible-enough in practice at scale 4 vs. genuinely cut off.
- Whether the create screen's tabbed fallback at the 480→530 boundary flips cleanly (no half-state).
- Font metrics: all width math uses `font.width(...)`, correct at every scale, but only a render confirms
  the ellipsize thresholds land where expected.

---

## 5-minute live GUI-scale test checklist (for Peetsa)

Do this on a **saved Latitude world** (so the HUD and a real zone-crossing are in play). Options →
Video Settings → **GUI Scale**.

1. **Set GUI Scale = 2 (baseline).** Open **Create World** (the Latitude screen). Note it's the wide
   three-column layout. Open **HUD Studio** (Rules → HUD Studio, or F9 in-world). Drag the **zone title**
   preview a good way toward the top-left corner. Click **Done**.
2. **Set GUI Scale = 4.** Re-open **Create World** → it should now be the **tabbed** layout (World /
   Spawn Zone / Rules), everything reachable, nothing clipped off the right edge. Tab through all three.
   Open **HUD Studio** → is the sidebar swallowing the screen? Can you still hit **Done/Cancel** and the
   top-right **grid/snap** button? Press **L** to hide the sidebar and confirm the preview fills the
   screen. **Open the Color Scheme dropdown** (Compass tab, Analog) → does the 13-entry list fit /
   scroll / flip above the row instead of running off the bottom?
3. **Back in the world at scale 4:** walk across a **zone boundary**. Does the zone title appear
   **on-screen** — or did the reposition from step 1 push it off? *(This is the H1 check.)* Walk toward a
   **pole** until the whiteout + warning line show → is the warning text fully on-screen or cut off the
   right? *(M2.)*
4. **Set GUI Scale = 1.** Everything is now physically tiny (expected). Confirm the create screen and
   HUD Studio are still *laid out correctly* (just small) — no overlaps, no controls off the edge, the
   LATITUDE wordmark still centered.
5. **Set GUI Scale = Auto** and confirm it lands wherever your window puts it with no leftover breakage.

**What "pass" looks like:** at every scale you can reach and read every control; the compass and its
labels stay docked sensibly; the zone/hemisphere titles stay on-screen. **The one you're hunting** is
step 3 — a repositioned title that vanishes at a smaller scale is finding **H1**.
