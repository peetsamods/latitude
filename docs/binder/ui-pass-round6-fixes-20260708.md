# HUD Studio round 6 — opacity wording restored, CycleButton tooltip-wipe bug fixed (2026-07-08)

`status: FIXED IN SOURCE, awaiting live re-test (TEST 35 candidate)`
Source: Peetsa, follow-up to round 5 — restore "opacity" wording, a reported tooltip bug ("after you
click on a button and go back and hover over it, the tooltip is gone and will not come back"), and a
reminder to keep Normal in the Title Case tooltip (already done in the prior correction commit).

## Finding 1: "opacity" wording restored

Round 5 rewrote three opacity-related tooltips (Inner Transparency, digital Transparency, Text Opacity)
to lead with "see-through" instead of "opacity," on the theory that "opacity" was jargon. Peetsa: it
isn't. Restored the word in all three, keeping the plain-language framing from round 5 elsewhere (the
comma-separated "compass, zone, biome, and coordinate text" list instead of a slash-joined one).

## Finding 2: tooltips vanish permanently after the first click, on any dropdown-style control

**Root cause (confirmed by decompiling the 26.2 `CycleButton` class):** every "dropdown" control in the
Studio (Compass Style, Direction Format, Compass Look, Color Scheme, Attach to Hotbar, Title Case, Show
HUD, and roughly 30 more) is a vanilla `CycleButton`. `CycleButton` has its OWN internal tooltip
mechanism — a private `tooltipSupplier` field, consulted by a private `updateTooltip()` method that
vanilla calls **automatically on every value change** (i.e. every click), with no null-check:
`this.setTooltip(this.tooltipSupplier.apply(this.value))`.

This codebase never uses that mechanism — every tooltip in the Studio is attached externally via a
shared `tooltip(widget, text)` helper calling `AbstractWidget.setTooltip(...)` directly, AFTER the
`CycleButton.Builder` chain finishes (the builder's own `.withTooltip(...)` is never called). That
leaves `tooltipSupplier` at its default no-op (returns `null`). The consequence: the moment you click any
CycleButton once, its internal `updateTooltip()` fires and overwrites the externally-set tooltip with
`null` — permanently, for the lifetime of that widget instance. Hovering afterward can't bring it back,
because the tooltip object itself is gone, not merely hidden. (Widgets whose `onValueChange` callback
happens to call `this.init()` — e.g. Compass Style, Compass Look — appear to "recover" only because
`init()` rebuilds the widget from scratch and re-attaches a fresh tooltip; plain toggles that don't
re-init stay broken for the rest of the Studio session.)

**Fix:** the shared `tooltip()` helper now detects `CycleButton` instances and patches their
`tooltipSupplier` field (via reflection — `CycleButton` exposes no way to set it after construction, and
the builder-time alternative would mean touching all ~30 call sites instead of the one shared helper) to
a constant supplier that always returns the same `Tooltip` object. Every Studio tooltip is static
descriptive text that doesn't depend on the button's current value, so "always return the same tooltip"
is the exactly correct behavior, not an approximation. The button's own auto-refresh now re-applies the
right tooltip on every click instead of wiping it. Reflection failure (e.g. a future mapping rename)
degrades gracefully to the old behavior rather than throwing.

## Finding 3 (reminder, already done): Title Case tooltip lists Normal again

Confirmed already correct from the prior same-day correction commit (`30a5f7fd`): the tooltip reads
"Changes how the title's letters are written: Normal, UPPERCASE, lowercase, or mOcKiNg." — no further
change needed here.

## Verification

- `compileJava` + pure-JVM suite (`cleanTest test`) green.
- NOT live-verified yet (UI class of bug, and specifically hard to prove without a running client):
  hover a CycleButton, click it once (cycling its value), move the mouse off and back — tooltip should
  reappear. Test at least one of each kind: a plain ON/OFF toggle (e.g. Rainbow Text) and a multi-value
  cycle (e.g. Compass Look) to cover both the "doesn't re-init" and "does re-init" cases.
- Worldgen isolation by diff scope: only `client/LatitudeHudStudioScreen.java` touched. Zero `world/`,
  `terrain/`, `mixin/` changes.

`TEST 34.jar` staged (SHA-256 `f656dc5bed9031ea9414435ee1d24e7dd9a0d0edc0a62b776915c582241a1c36`),
superseding `TEST 33.jar`. Same worldgen args as TEST 30-33.
