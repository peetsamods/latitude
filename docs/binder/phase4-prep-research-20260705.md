# Phase 4 (Terrain Integration Spike) — prep research

`date: 2026-07-05` · `branch: port/canonical-26.2-pivot` · `scope: research only, no code changes`

Per Phase 4's own roadmap entry (`docs/LATITUDE_2_0_OVERHAUL.md`), its "Document review" step is: active
density-function docs, height export status, prior ocean-seam failure records. This note is that review,
done before drafting a kickoff prompt. Nothing here authorizes starting implementation.

## 1. The prior failure — fully built, live-tested, explicitly scrapped

`docs/design/ocean-seam-wrap-plan-20260623.md` records a complete, four-piece E-W wrapping system that
was built, compiled, staged, and **live-tested**: a biome-band ocean edge, a `NoiseRouter`-wrapping
terrain-sink density function (`LatitudeOceanSinkDensityFunction` via a `RandomState` mixin), a
teleport-loop at the X border, and a hard Z-clamp replacing the world border. It worked mechanically —
the ocean sink produced real water at the edges — but live testing showed the teleport-loop **could not
be made seamless**: the carved edge looked artificial (flat shelves/cliffs), clouds visibly jumped on
teleport, and ocean layout differed across the seam, so it always read as a teleport, not a continuation.
**Peetsa: "It's broken... let's scrap the project."** All of it was reverted. `docs/design/horizontal-
wrapping-feasibility-20260623.md` is the companion feasibility analysis explaining why seamless wrapping
isn't achievable at all with a teleport-loop approach.

**This is closed.** `docs/LATITUDE_2_0_OVERHAUL.md` itself already states the resulting rule: "Do not
revive E/W teleport wrapping, ocean seams, or terrain sinking as the 2.0 centerpiece." Any Phase 4 work
must not reintroduce a teleport-loop or terrain-sink-at-the-seam approach — that specific idea has already
been tried, live-tested, and rejected by Peetsa directly. The Mercator 2:1 world shape itself is unaffected
and stays; only the seam-wrapping mechanism was scrapped.

## 2. Current terrain wiring — Latitude's own density functions are dead code

Confirmed directly: `globe:overworld`'s active `final_density` (and `initial_density_without_jaggedness`)
route to `minecraft:overworld/sloped_cheese` — vanilla/Terralith's own terrain, not anything Latitude
owns. Latitude's custom density function files under `data/globe/worldgen/density_function/`
(`base_terrain.json` etc.) exist on disk but are **orphaned** — never referenced by the live noise
router. `base_terrain.json` is literally a no-op (`add 0 + globe:sloped_cheese`).

This confirms, independently of the Biome Consumer land-fraction finding, that **terrain height has never
been under Latitude's control at all** — not "decoupled from GeoAuthority specifically," decoupled from
*everything Latitude does*. Any Phase 4 work that wants terrain to respect GeoAuthority's land/ocean map
is the FIRST time Latitude terrain code would run at all, not a matter of reconnecting something that
used to work.

**The only currently-active terrain-touching mixin** is `NoiseChunkGeneratorCarveMixin` (suppresses cave
carvers in polar caps, gated on the `globe:overworld` settings key via `stable(GLOBE_SETTINGS_KEY)`). That
gating pattern — check the settings holder, no-op on non-globe worlds — is the correct precedent to
mirror for any new terrain hook.

## 3. A related-but-different existing design: the amplitude wrapper

`docs/binder/amplitude-df-wrapper-design-20260622.md` already designed (not implemented) a `RandomState`
mixin wrapping `NoiseRouter.finalDensity` + `initialDensityWithoutJaggedness`, gated the same way as
`NoiseChunkGeneratorCarveMixin`. **Important: this is a different goal from what Phase 4 needs.** The
amplitude wrapper dampens terrain height purely as a function of Y (height above sea level) — a global
knob (`K_AMPLITUDE`) to flatten exaggerated peaks/coastal "reefs" while keeping Terralith's terrain as-is.
It does not read X/Z position or any Latitude geography signal at all.

What Phase 4 actually needs is a **position-dependent** bias — a density adjustment that reads
`GeoAuthority.sample(x,z).land01`/`isOceanIntent` per column and pushes terrain up where the map says
land, down where it says ocean. That's a materially different (and larger) wrapper than the amplitude
one, even though both attach at the same interception point (`RandomState` → `NoiseRouter`) and should
reuse the same safety discipline: a single tunable strength knob defaulting to a no-op, try/catch around
the wrap so any failure falls back to vanilla terrain, and `initialDensityWithoutJaggedness` wrapped too
(or spawn-height-finding disagrees with rendered terrain — a documented risk in that same design note).

The amplitude wrapper itself was never implemented ("PROTOTYPE DEFERRED to a fresh focused session...
Peetsa wants to be at the keyboard to tune K") — it remains a separate, still-open piece of future work,
not something Phase 4 should fold in without a separate decision.

## 4. Height export — exists, disabled by default, incomplete

Height-export infrastructure exists (`-Dlatitude.emitHeight`, `-Dlatitude.atlasTerrainAware`) but is
off by default for performance (byte-identical normal runs), and terrain-aware probing is a slower,
opt-in mode. The roadmap's own Hard Stop list already names this: "Analyzer cannot distinguish biome
water, terrain water, and intended ocean basin" and "Terrain-water proof blocked by missing height
export." Enabling and validating this is Phase 4's own first action-plan item, not yet done.

## 5. Performance — a real, unresolved freeze in the historical record

`docs/binder/spark-profile-analysis-20260701.md` documents a genuine ~3-minute near-total TPS freeze
during E-W edge teleport testing. It was **never conclusively root-caused**: the Spark capture only
sampled the main "Server thread," not the worker-thread pool that actually runs chunk/biome/density
generation, so it could not confirm or rule out Latitude/Terralith code as the cause. The same session
found the test machine at 99.71% physical memory and 83.9% swap use — enough on its own to explain a
multi-minute freeze via GC-on-swapped-memory, independent of any code hotspot. **Both explanations are
still live; neither was eliminated.**

This matters directly for Phase 4: the roadmap's own Hard Stops include "Spark or counters show
generation stalls regressing," and the action plan's last step is "Run all-thread Spark proof." Given
the ambiguity above, that proof needs `--thread *` (all threads, not just main) AND a machine that isn't
already under memory pressure — otherwise a stall could be wrongly blamed on (or wrongly cleared for) new
terrain code when the real cause is unrelated system load.

## What this means for a future Phase 4 kickoff

Not written yet — deliberately held until the current Biome Consumer sweeper audit (in progress) is
resolved, so the working card reflects the actual current state of the code rather than a state that's
about to change under it. When drafted, it should require, at minimum:
- No teleport-loop/seam-wrapping revival (closed, per §1).
- A narrow, position-dependent `RandomState`/`NoiseRouter` wrapper (per §2/§3's interception point and
  safety discipline), separate from and not blocked on the still-open amplitude-wrapper work.
- Height export enabled and validated as a prerequisite proof tool, not an afterthought (per §4).
- An all-thread Spark baseline BEFORE any change, on a machine confirmed not to be under memory pressure,
  and another all-thread capture after, before calling anything green (per §5).
- Explicit acknowledgment that terrain amplitude is LIVE-ONLY verifiable (the atlas is a fixed-Y biome
  map) — this phase needs Peetsa live at the keyboard for the actual go/no-go judgment, not just an
  automated proof gate.
