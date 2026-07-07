# Fable 5 Recovery Checkpoint — Slices A/B/C + Biome Audit (2026-07-07)

`status: CHECKPOINT — work STOPPED here per Peetsa's gate (no D/E without explicit authorization)`
Code state at checkpoint: `port/canonical-26.2-pivot` @ `d18149ed` (5 commits this pass: `a2fab073` A,
`2c852db0` B, `1d2402b6` C, `d18149ed` biome audit + forwarding fix, plus this checkpoint's own commit);
Globe main @ `88f9cbad`. Only `run-headless/server.properties` dirty (runtime junk, never staged).

## Gate results

| Slice | Gate | Result |
|---|---|---|
| A — truth restore | grep assertions (7): stale seed-0 coordinate only in correction context; OVERHAUL.md Phase-4 EXECUTED block; Globe HANDOFF names the pivot; banners ×4; L20; freshness rows flipped | **GREEN** (`a2fab073` + Globe `88f9cbad`). Corrected calibration recipe: TYPE seed `2591890304012655616`, UI-size **Regular**, fly to `x=-3300, z=-3636` (~32.7°S), stay west of x≈-2840 |
| B — state + observability | compile+test; armed-S0 vs flag-off byte-identity; single-JVM stale-provider replay probe; warns observable | **GREEN** (`2c852db0`). Replay probe pass=true (provider ends NoOp through the exact seed-0 world-B load order); 5 one-shot warns observed in real logs; S=0.05 values pre/post identical |
| C — Y-aware taper | no slab at any S; no lava voids at r≤30; visible ground response; taper formula exact; coordinate absoluteness; Lane-6 coherence tripwires | **GREEN** (`1d2402b6`). NO slab up to S=1.0 on 3/3 seeds (pre-taper flip at S=0.10 eliminated); NO lava voids (pre-taper r≥10 voids eliminated); land +3/+5/+6 blocks at S=0.3/0.4/0.5; coasts grade (+4/0/−1 across the calibration ramp at S=0.4); formula exact at 16 Y; tripwires green after the sunk-land mirror veto (drowned-land caught by the tripwire and FIXED, gated so flag-off/armed-S0 stay byte-identical — zero grid diffs). **Named live-gate candidate strength: S=0.4** |
| Biome audit | evidence-based verdict, no fixes | **DONE** (`d18149ed`): brains earthlike; live map lawful but moisture-blind (not wired); consumer wiring half-right/half-wrong — 2 CONFIRMED consumer bugs (P1-A tropical dry-law undo, P1-B kAlt+flat-polar snow repaint) that do NOT affect the live config; new prerequisite slice queued before any consumer flip |

Gate G1 of the audit report (taper green headlessly) and G2 (state/observability) are satisfied.
G3 (the ONE scripted live session) has NOT run.

## Catches made by the gates themselves this pass (the proof system worked)

1. The replay probe's first run silently skipped — build.gradle didn't forward the new property (L17
   forwarding class). Fixed; probe then ran and passed.
2. The first "consumer-on" atlas diffed 0.00% — because `climateV2`/`biomeConsumerV2` were NEVER forwarded
   to the forked JVM (same L17 class, second catch). Fixed; the real diff is 1.76% and exposed P1-A/P1-B.
3. The coordinate-absoluteness gate read land01=0.000 at all probes — exposing the preset naming trap:
   `globe:globe_regular` is UI-size **Small** (7500); UI-size **Regular** is `globe:globe_large` (10000).
   The calibration coordinate is valid for UI-Regular (verified exactly on the 10000 field: 0.197/0.581/
   0.913). Future headless verification of live-Regular claims must boot `globe_large`.
4. Three sweep runs were invalidated by a zsh word-splitting quirk feeding "0.05 10" as the strength
   (the defensive parser silently degraded it to 0.0); caught by config-echo inspection, re-run correctly.
   (A one-shot warn on malformed flag values would make this loud — noted for the observability backlog.)

## Explicit NON-claims (per the card's final checkpoint rules)

- **Phase 4 is NOT claimed closed.** It remains mechanically closed + headlessly-recovered; closing it
  requires Slice E (the one scripted live session with a TYPED seed at S=0.4, Sodium boot, R7
  subtropical-snow check at headless-pre-located coords, spawn sanity, Classic second-world switch, and
  the all-thread Spark capture at S>0) ending in the written terrain go/no-go note.
- **Phase 5 is NOT recommended to start.** Gates G1/G2 are green but G3 has not run; additionally the
  biome audit adds a prerequisite to Phase 5's consumer-flip specifically: the "consumer law-compliance"
  slice (P1-A: route reroll repaints through the demote gates; P1-B: temper/replace kAlt with real
  terrain height — now possible post-Phase-4 — and map cooled mountains to altitude families, not
  snowy_plains).
- No live Minecraft run was performed anywhere in this pass. No Modrinth/profile/release action taken.
  Nothing pushed.

## Awaiting Peetsa's explicit authorization

1. **Slice E** — the one bounded live session (Phase 4's actual closing gate). Recipe is fully scripted
   in the audit report §7 + the corrected handoff.
2. **Slice D** — tooling truthfulness (manifest records real sysprops/emitHeight; lock preflight;
   biomes.txt flag echo). Design-only restriction stands until authorized.
3. **Consumer law-compliance slice** (new, from the biome audit) — before any Phase-5 consumer flip.
