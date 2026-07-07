# TEST 27 live findings — Slice E attempt #1 (2026-07-07)

`status: session correctly ABORTED on tripwires; diagnosis complete; one-arg retry recipe ready`
Session: Peetsa, TEST 27.jar (SHA `fbfb7a2b…`), Mercator UI-Regular, TYPED seed `2591890304012655616`,
flags geoV2+terrainV2 S=0.4 (r default 1.0 — the JVM-args Spark tab confirms the exact config; L20
discipline satisfied). Evidence: Peetsa's annotated screenshots (`~/Documents/test 27.pdf`), Spark
capture `spark.lucko.me/EbQurnucOO` (02:06 PM, 4ms interval), and headless reproduction at the exact F3
coordinates (session scratchpad `test27-repro/`, S=0 vs S=0.4 vs S=0.4+r=0).

## Finding 1 — "Spawned in this massive broken cavern" (F3: -17208 / 60 / 4714, 42°S) — CONFIRMED, diagnosed

Two stacked defects:
- **Bias-hollowing (the big one):** the spawn column reads `land01 = 0.000` — it sits in the projection
  EDGE BAND (|x| = 17,208 of 20,000 = 86% > EDGE_START 0.80), where geography deliberately ramps to
  ocean-intent (design R1). At S=0.4 the full NEGATIVE bias (−0.1 density) therefore applies — and a
  uniform subtraction doesn't just lower a surface: this column's whole underground sits at +0.04..+0.11
  density (measured), so −0.1 flips it wholesale. Headless stack diff: S=0 = ONE solid mass [-64..98];
  S=0.4 = FOUR fragments with **63 blocks of internal void** — a "massive broken cavern," verbatim.
  The same signature reproduces at the coast probes (0→37 and 7→49 gap blocks). Positive-bias land
  columns show the OPPOSITE (gaps 3→1, 16→0): adding density fills voids; subtracting shatters.
- **Spawn placement decoupling (Lane 1 F5/PR2, now LIVE-CONFIRMED):** spawn's land test is biome-driven
  (consumer off ⇒ old map), and the old map paints land biomes in the edge band — so spawn happily chose
  an edge-band column whose BIASED terrain is a sunk, shattered hollow, and placed Peetsa at Y60 (below
  sea level) inside it.

Why the Slice-C gate missed it: the coherence/structural gate checked slabs (≥Y160) and lava, and I
classified multi-range stacks as "ordinary cave layering" WITHOUT diffing gap counts against the S=0
baseline — the data showed 3→5 ranges and I read it as benign. Tripwire lesson: assert the DELTA.

## Finding 2 — "Lagging chunk loading" — ENVIRONMENT-CONFOUNDED, retest needed

The Spark capture shows the MACHINE, not just the mod, in distress: physical RAM 23.9/24 GB (99.6%),
swap 5.8/7 GB (83%), G1 young GC every ~980 ms, MSPT median 9.66 but **max 3,280 ms**, system CPU 88-94%
while the MC process used ~40%. Under memory exhaustion + swap, chunk pacing judgments are invalid (the
2026-07-01 capture had the same confound). Additionally the profile tree contains ONLY "Server thread" —
the profiler was started without `--thread *`, so worker-pool time (where GeoAuthority runs) is invisible
and P1-3's capture protocol was not met. Plausible contributor worth noting: shattered/hollowed coastal
terrain is lighting-engine-hostile, so Finding 1 itself may have amplified generation cost. Verdict:
UNVERIFIED as wrapper cost; retest under the r=0 recipe, freer RAM, `--thread *`.

## Finding 3 — "Shouldn't rain shadow be the opposite?" (F3: -3349 / 115 / -3651, 33°N) — NOT A BUG: NOT WIRED

Answered by the biome-geography audit (2026-07-07): in this config (`biomeConsumerV2=off`) NO moisture
logic reaches the visible map — rain shadows/windward-leeward are computed by ClimateAuthority and
discarded. The desert massif at 33°N is the pre-2.0 arid-BELT machinery (20-35° band), placed with no
knowledge of the coast or wind. The climate model itself believes exactly what Peetsa expects (field
probe: wet windward coasts precip 0.65-1.00, drying over ridges to 0.35, shadow downwind) — and the
measured consumer-on diff shows wet-fetch coastal deserts repainting to forest (87 cells) the moment the
consumer is wired. That wiring is gated behind the "consumer law-compliance" slice (2 confirmed law bugs
to fix first). So: the model agrees with him; the map can't hear the model yet.

## Finding 4 — "Absent continental shelf — massive drop-off and hollow" (F3: -3090 / 68 / -3405, 31°N) — TWO PARTS

- **The hollow** = Finding 1's mechanism at the coast (measured 0→37 gap blocks at this exact column).
- **The missing shelf** = formula shape: smoothstep(sd) crushes the bias to ~nil across the ramp middle
  (at land01 0.58 the push is +0.007) and concentrates all response at the ramp ends, so coasts read
  cliff-then-deep with no shallow apron. GeoAuthority already computes a `shelf01` field — currently
  unconsumed by the wrapper. Design item, not a quick knob.

## Immediate mitigation — VERIFIED headlessly, live-testable with the SAME jar

**`-Dlatitude.terrainV2.oceanStrengthRatio=0.0`** (add to the existing args; keep S=0.4). The gain term
zeroes the entire negative side: every ocean-intent/edge-band column is **byte-identical to S=0 baseline**
(0 density-sample diffs across all probes; spawn column solid again; gaps 0-delta) while land columns
still lift (+8 deep, +4/+2 coastal) and still heal their own gaps. No slab (taper), no hollowing (no
negative bias), land-follows-geography intact. What it gives up: active ocean DEEPENING — deferred to a
properly-designed asymmetric regime.

## Open items spawned by this session

1. **Slice C-2 (needs authorization): ocean-side "bathymetry" regime.** Uniform negative offsets are the
   wrong tool for sinking terrain (proven above). Primary design candidate: clamp-style regime for
   ocean-intent columns (terrain ceiling Y*(land01, shelf01) — carve above, never touch below), replacing
   additive subtraction; consumes `shelf01` to give real continental shelves; smooth at the coast
   crossing; own headless gate incl. GAP-DELTA assertions (the tripwire this round taught us).
2. **Spawn placement fix (small, separate):** exclude the projection edge band from spawn search and/or
   consult geography land-intent when terrainV2 is armed.
3. **Coherence tripwire upgrade:** add "interior gap blocks vs baseline" to the standing gate assertions.
4. Spark protocol for the retry: `/spark profiler start --thread *`, machine below ~80% RAM.

## Slice E status

Attempt #1: correctly aborted on tripwires (stop rules honored). Gate NOT passed; Phase 4 stays open.
Retry recipe: same TEST 27 jar, same seed/shape/size, args
`-Dlatitude.geoV2.enabled=true -Dlatitude.terrainV2.enabled=true -Dlatitude.terrainV2.strength=0.4
-Dlatitude.terrainV2.oceanStrengthRatio=0.0` — full script otherwise unchanged
(`fable5-slice-e-live-script-20260707.md`, which now carries a correction block pointing here).
