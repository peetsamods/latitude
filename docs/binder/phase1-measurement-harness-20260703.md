# Phase 1 (Measurement Harness) — geography analyzer + red/baseline comparison

`date: 2026-07-03` · `branch: port/canonical-26.2-pivot` · `scope: Phase 1 only, per docs/LATITUDE_2_0_OVERHAUL.md`

Objective: make the current red measurable and repeatable, per the Phase 1 entry in
`docs/LATITUDE_2_0_OVERHAUL.md` and the "Measurement First" analyzer requirements earlier in that
document. No geography/climate behavior changed this phase — pure analysis tooling.

## What was added

`tools/atlas/geography_analyzer.py` — a CLI over an atlas run directory (same exact-ID
`biome_ids.png`/`biome_palette.json`/`biomes.txt` bundle that `band_correctness_check.py` and
`overrep_rank.py` already trust as biome truth). No scipy dependency (not installed in this
environment) — connected components use a from-scratch two-pass union-find.

Computes, per the doc's analyzer requirements list:

- Raw land/water/coast shares, and **cosine-latitude-area-weighted** shares (a flat 2:1 raster
  over-represents polar area; each row is weighted by `cos(latitude)` to approximate true on-a-
  sphere area).
- **Biome water vs terrain water, kept separate.** "Water" here is strictly ocean/river-tagged
  biomes from the exact-ID export. The report prints an explicit note that terrain water (actual
  height below sea level) needs an `--emitHeight` run and is out of scope until one exists — this
  phase does not conflate the two.
- Land components, ocean-basin components, river components, coast components (4-connected),
  each with **raw and area-weighted largest-component share**.
- **Bridge sensitivity**: every land/ocean-basin labeling is re-run at 8-connectivity, and the
  count/largest-share delta between 4-conn and 8-conn is reported — a direct proxy for how much a
  single diagonal-only isthmus or river gap is holding components together or apart.
- **Projection-edge composition by band**: the outer ~2% of columns on both the west and east edge
  (this world does not wrap), broken into land/ocean/river fraction per latitude band — the metric
  the Phase 5 (Boundary Experience) "less wall-like" goal will eventually be graded against.
- **Size-aware gate**: maps the run's radius to the nearest of the five canonical world sizes
  (Itty/Small/Regular/Large/Massive) and flags when the sampling step is coarser than step16 (i.e.
  component counts should be treated as provisional, per the Proof Rules' "step64 is iteration
  only" rule).

`tools/atlas/test_geography_analyzer.py` — 19 synthetic-fixture `unittest` tests, run **before**
trusting the analyzer against any real Atlas run (per the doc's explicit "synthetic fixture tests
before trusting real Atlas runs" rule). Covers: biome-family classification (including the
deliberate choice that beach/shore/mangrove count as land, not water — see below), coast-adjacency
correctness, connected-component correctness at both 4- and 8-connectivity (including a hand-built
diagonal-bridge isthmus and a checkerboard case), cosine-latitude weighting (equator weight = 1.0,
pole weight ≈ 0.0, a polar-only-water grid's weighted share shrinking below its raw share),
band-of-latitude mapping, projection-edge composition, and the size-gate lookup. All 19 pass.

### One deliberate deviation from `band_correctness_check.py`

That script's `is_water()` lumps beach/shore into "water" for its own purpose (wrong-band
contamination doesn't care about coastline shape). This analyzer's `classify_biome()` keeps
beach/shore/mangrove as **land** — they're solid ground touching water, and classifying them as
water would push the computed "coast" ring one cell too far inland. Documented in both the module
docstring and a dedicated test (`test_beach_and_shore_are_land`).

## Old-red vs fresh-HEAD-baseline comparison

Same seed (`2591890304012655616`), same radius (`7500` = canonical "small"), same step (`16`),
same 2:1 Mercator aspect (`-Dlatitude.atlasXAspect=2.0` — the flag `tools/atlas/atlas_runner.py`
already applies by default; a plain `runBiomePreview` invocation without it silently falls back to
a 1:1 square render, which would have made this comparison invalid — caught and re-run correctly).

| metric | old-red (`20260702-064708`, `f86944cb`, 26.1.2) | fresh baseline (`1389051a`, 26.2 pivot) |
|---|---|---|
| land (raw / area-weighted) | 64.31% / 64.18% | 63.14% / 63.02% |
| water (raw / area-weighted) | 35.69% / 35.82% | 36.86% / 36.98% |
| land components | 1032 | 1101 |
| **largest land component share (raw)** | **95.08%** | **94.71%** |
| ocean-basin components | 462 | 468 |
| largest ocean-basin share (raw) | 9.39% | 9.35% |
| river components | 10938 | 11758 |
| coast components | 65695 | 70599 |

**Interpretation: the current red is confirmed and essentially unchanged.** This independently
reproduces the doc's stated old-red numbers ("Land fraction around 64%... Largest land component
around 95% of all land... No dominant connected ocean basin") almost exactly, and shows the fresh
26.2-pivot baseline is statistically the same shape — one dominant, near-total land sheet (~95%)
with no dominant ocean basin (largest basin share under 10%) and thousands of small water/river
fragments. This is expected: Phase 0 was pure no-op scaffolding with a proven flag-off byte-
identical guarantee, so no worldgen output should have moved, and the small numeric deltas here
(1032→1101 land components, different biome-pack composition) come from this 26.2 worktree not
having the same custom-biome-pack jars loaded as the 26.1.2 run (`worldBiomeInventoryCount` 93 vs
47) — a mod-loadout difference, not a Phase 1 measurement artifact.

Full JSON/text reports for both runs were written to a scratch directory during this session and
are not committed (regenerate on demand with the exact commands below — the point of this analyzer
is that both runs are fully reproducible from seed+radius+step+source-commit, not that their output
needs to live in the repo).

Reproduce:

```
# old-red (needs the 26.1.2 worktree checked out at f86944cb or later with matching worldgen)
python3 tools/atlas/geography_analyzer.py <path>/run-headless/latdev/atlas-runs/20260702-064708 --prefix step16_

# fresh baseline at any commit on this branch
JAVA_HOME=$(/usr/libexec/java_home -v 25) env -u JAVA_TOOL_OPTIONS JAVA_TOOL_OPTIONS="-Dlatitude.atlasXAspect=2.0" \
  ./gradlew --no-daemon --console plain runBiomePreview \
  --args="--seed 2591890304012655616 --radius 7500 --step 16 --emitBiomeIndex true --out <dir>"
python3 tools/atlas/geography_analyzer.py <dir>/seed_2591890304012655616/Run_<commit>/R7500/step16
```

## Atlas overlay plan

Per the Phase 1 action plan's "Add Atlas overlay plan after CLI report is trusted": planned in
`docs/design/atlas-geography-overlay-plan-20260703.md`. Deliberately not implemented this phase —
the CLI report already answers the current measurement need, and an overlay has much more value
once Phase 2 gives it real (non-neutral) `GeoSummary` output to render.

## Proof gate

- Analyzer correctness: 19/19 synthetic fixture tests pass (`python3 -m unittest
  test_geography_analyzer -v` from `tools/atlas/`).
- Cross-check against documented old-red claims: land/largest-component numbers match
  `docs/LATITUDE_2_0_OVERHAUL.md`'s stated old-red interpretation almost exactly (independent
  confirmation the analyzer measures what the doc already believed).
- No worldgen behavior touched this phase — `compileJava`/Atlas proof from the Phase 0 slice is
  still the last worldgen-affecting proof point; nothing here needed a new one (Phase 1 doc review
  and its own Commits/tags/pushes rule call for "analyzer/test/docs commit, no behavior tag").

## What Phase 1 deliberately did not do

- No GeoAuthority/geography algorithm — pure measurement of the existing red.
- No Atlas overlay implementation (planned only, see above).
- No terrain-height / true terrain-water analysis (needs an `--emitHeight` run first).
- No downsampling or performance tuning of the union-find beyond what step16 already needs (~4s
  wall-clock per run at this resolution — fast enough that no optimization was warranted).
- Did not tag or push. Local commit only.

## Next

Phase 2 (GeoAuthority Prototype) is next per `docs/LATITUDE_2_0_OVERHAUL.md` — and per
`docs/binder/model-effort-strategy-20260702.md`, that phase genuinely warrants Opus at high/xhigh
effort (from-scratch macro-geography algorithm design, highest blast radius of the whole roadmap),
unlike Phases -2/-1/0/1 which were all bounded Sonnet-appropriate work. Recommend a future thread
either switch `/model` to Opus for that phase, or delegate the core algorithm design to an Opus
sub-agent per that strategy doc's protocol — plainly stating which path it's taking before starting,
rather than silently proceeding on whatever model happens to be set. Nothing here needs Peetsa's
input before that starts.
