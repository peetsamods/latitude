# Slice E — the ONE scripted live session (Phase 4's closing gate) — 2026-07-07

> **CORRECTION (2026-07-07, after attempt #1 — READ FIRST):** Peetsa's first run correctly aborted on
> tripwires (hollowed/shattered ocean-side terrain incl. the spawn column; see
> `test27-live-findings-20260707.md` for the full diagnosis). **The retry uses the SAME TEST 27 jar with
> ONE added JVM arg:** `-Dlatitude.terrainV2.oceanStrengthRatio=0.0` — verified headlessly to make every
> ocean-intent/edge-band column byte-identical to unbiased terrain (no hollowing possible) while land
> still lifts. Full args for the retry:
> `-Dlatitude.geoV2.enabled=true -Dlatitude.terrainV2.enabled=true -Dlatitude.terrainV2.strength=0.4
> -Dlatitude.terrainV2.oceanStrengthRatio=0.0`. Also: start Spark with `--thread *` (attempt #1's capture
> was server-thread-only) and check RAM headroom before playing (attempt #1 ran at 99.6% physical + 83%
> swap, which invalidates any perf feel). Steps below are otherwise unchanged.

`status: RETRY READY (attempt #1 aborted on tripwires — see correction above)` · est. 45–60 min · this is the audit report's G3 gate: the
only manual session in the recovery plan. Everything below is pre-verified headlessly; the session
confirms it live and produces the two Spark captures. **STOP on any tripwire; no fixes mid-session; no
strength dialing on a reused world.**

## Truth table (L3 — record of what is being tested)

| | |
|---|---|
| Repo / branch / HEAD | `Latitude-2.0-26.2-pivot` / `port/canonical-26.2-pivot` / `f8f9bd63` |
| Jar | `TEST 27.jar` (= `latitude-2.0-beta.1+26.2.jar`), SHA-256 `fbfb7a2ba4a67b292c0cb4338b52b382004f7e66b57ef0edf427a6023b177ebc` |
| Profile | Modrinth App → `LATITUDE 26.2` (Sodium 0.9.0, Lithium, C2ME, JourneyMap et al. present) |
| JVM args (set in profile Options → Java arguments) | `-Dlatitude.geoV2.enabled=true -Dlatitude.terrainV2.enabled=true -Dlatitude.terrainV2.strength=0.4` |
| Flags deliberately NOT set | `biomeConsumerV2` (stays off — the consumer has 2 confirmed law bugs, separate slice), `oceanStrengthRatio` (default 1.0) |
| Seed protocol | **TYPE** `2591890304012655616` into the seed field (never blank, never 0 — L20) |

## The session, step by step

1. **Launch** (fresh app). Expect a clean boot with Sodium — the old crash is the P1-4 check. If the log
   shows `[Latitude] Sodium is installed but ... E-W section-culling ... INACTIVE`, that is EXPECTED
   (informational, new in Slice B), not a failure.
2. **World A:** Create World → TYPE the seed → shape **MERCATOR** → size **REGULAR (20,000×20,000)**.
   - Spawn sanity: did you spawn on/near land? Note coords.
   - Optional log glance: `Phase 4 terrain bias installed` + `ENGAGED (strength=0.4)` lines.
3. **Coastline believability** — `/tp @s -3300 100 -3636`, then fly the ramp WEST→EAST from x≈-3400 to
   x≈-2850 holding z≈-3636. Headless truth for this exact line: open ocean (land01 0.10) grading to solid
   land (0.99) over ~560 blocks. Look for: a believable graduated coast. Tripwires: cliff-wall coast,
   any ceiling slab, floating shelf, exposed lava at the surface.
4. **Terrain feel:** ~2 min free flight within ±1000 blocks. Chunk generation should feel NORMAL at 0.4
   (the per-column memo is in); note if it doesn't.
5. **R7 warm-band snow glance** — `/tp @s 1375 120 2750` (24.8°S, tallest warm-band grid column,
   bamboo-jungle highlands) and `/tp @s -1375 120 4125` (37.1°S plains). Confirm no snow-capped warm-band
   terrain nearby. Structural note: the taper contributes nothing above Y160 and warm snow lines start at
   168+, so the WRAPPER cannot create warm-band snow; if you see a snow-cap anyway it is pre-existing
   Terralith relief — screenshot + report separately; it does not fail Phase 4.
6. **Spark capture A (S=0.4):** `/spark profiler start --thread *` → fly one straight line ~1,500 blocks
   into ungenerated terrain (~90 s) → `/spark profiler stop` → keep the URL/file.
7. **World B (same launch):** Save & Quit to Title (this exercises the new teardown — log:
   `V2 worldgen statics reset on server stop`). Create World → TYPE the SAME seed → shape **CLASSIC** →
   size Regular. Enter.
   - World-switch check (P1-1 live confirmation): log shows a FRESH `installed` + `ENGAGED` for world B.
   - Edge-ring parity glance: `/tp` out toward a border (x or z ≈ ±9,800) — the edge ocean ramp should
     read acceptably, comparable to Mercator's.
8. **Spark capture B (S=0 control):** Quit the game. Change the strength arg to
   `-Dlatitude.terrainV2.strength=0.0` (leave the other two flags). Relaunch → create a NEW world (TYPE
   same seed, Mercator, Regular — do NOT reuse world A; different strength = different terrain law =
   chunk shear) → same straight-line flight → spark start/stop → keep it.
9. **Record** (L20 discipline): the actual typed seeds, world order, `TEST 27` + SHA above, flags per
   launch, screenshots of anything odd.

## Stop rules

Any tripwire (slab, void, surface lava, wrapper-created warm snow, crash, brutal chunk-gen slowdown) →
STOP, screenshot, bring it back. No mid-session edits, no strength dialing, no world reuse across configs.

## What comes back to the desk

The two Spark artifacts + screenshots + notes → the terrain go/no-go note gets written from them (the
roadmap's Phase-4 closing deliverable: Spark closes P1-3, Sodium boot closes P1-4, the checklist closes
the live-pass residuals). If all green → **Phase 4 CLOSED**; Phase 5 remains gated on the separate
consumer law-compliance slice.
