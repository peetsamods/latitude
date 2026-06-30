# Arid belt — Earth-like design + anti-regression spec (2026-06-25)

`status: active` · `scope: worldgen, subtropical band, arid (badlands/desert)` · `staged jar: d39de6a2`

The subtropical arid belt (badlands / desert / mesa) read as a **thin sliver** — arid was ~0% out to ~28°,
peaked only ~28–32% in the narrow 30–35° slice, and savanna dominated the whole band. Peetsa: *"the arid
region looks too sparse… a thin band across the world… should be more earthlike."* This documents the
Earth-analog target, the two levers used, the verified result, and the **guards that keep it from regressing
in either direction** (back to sparse, or over-correcting into a tropics-desert / temperate-desert leak).

## Earth analog (the target)

Real subtropical deserts sit ~**20–35°** (Sahara, Arabian, Mojave/Sonoran, Australian, Kalahari, Atacama),
fading to **savanna/steppe on the equatorward side** (Sahel) and **Mediterranean/grassland on the poleward
side**. So the design target for Latitude's 0–90° hemisphere:

- **Tropics (0–23.5°): arid-FREE** — the "no desert in the jungle" law. Non-negotiable (this was the original
  v1.4 bug; see [[v1p4-tropical-drybiome-equator-overhaul]]).
- **Subtropical (23.5–35°): the arid belt** — a real, wide band, desert/badlands *significant* in the core,
  with a savanna fringe on the equatorward edge and a grassland transition on the poleward edge (variety; Art X).
- **Temperate+ (35°+): arid-FREE** — badlands/desert clamped out (the 2026-06-25 poleward-clamp fix).

## The two levers (both in `LatitudeBiomes.java`, both -D tunable, both atlas-provable)

### Lever A — WIDEN: the equatorward demote ramp
`BADLANDS_LAT_RAMP_HIGH_DEG` / `DESERT_LAT_RAMP_HIGH_DEG` = the latitude above which arid is *allowed* (below
it, `demoteEquatorialBadlands/Desert` rewrite arid→savanna to protect the tropics). LOW stays **23.5°** (the
tropics line, the law). HIGH was **32.0** → arid couldn't appear until ~28–30°, so the belt was just the
poleward third of the band.

- **Changed HIGH 32.0 → 27.0** (`-Dlatitude.aridRampHigh`, default `"27.0"`). Arid now phases in from ~24–26°.
- This *permits* a wider belt but does not by itself fill it — the ladder (Lever B) decides what gets *routed*
  to arid. Both are needed together.

### Lever B — DENSIFY: the subtropical dry-ladder routing (`pickTropicalGradientNoSwamp`)
Within subtropical, a smoothstep "dry ladder" assigns each column a `step` 0–3 by position in the band
(`u`=0 equatorward 23.5°, `u`=1 poleward 35°; `step 0`≈poleward 31–35°, `step 3`≈equatorward 23.5–28°):

| step | latitude (approx) | BEFORE → pool | AFTER → pool |
|------|-------------------|---------------|--------------|
| 0 (poleward core) | 31–35° | `LAT_ARID` (full badlands/desert), shoulder→trans | unchanged |
| **1** | **29–31°** | `LAT_TRANS_ARID_TROPICS_1` (softer, scrub) | **`LAT_ARID`** (full arid) |
| 2 | 28–29° | `LAT_TRANS_ARID_TROPICS_2` (savanna/scrub) | unchanged |
| 3 (equatorward fringe) | 23.5–28° | `LAT_TRANS_ARID_TROPICS_2` (savanna) | unchanged |

- **Only step 0 used to reach the full arid pool** → arid was a thin poleward slice; the rest of the band was
  savanna-heavy transition. **Routing step 1 → `LAT_ARID`** extends the desert core down to ~29° while keeping
  steps 2–3 as the savanna/scrub **equatorward fringe** (the Sahel analog → variety, Art X-safe).
- The **`coldShoulderArid`** divert (step 0, `u >= SUBTROPICAL_ARID_SHOULDER_U`) is **deliberately kept**: it
  softens the most-poleward edge to a grassland transition (the desert→Mediterranean fade), preventing a hard
  desert wall against temperate.

## Verified result (atlas 20260625-134139, small world, step 64; arid = badlands+desert+mesa)

| latitude | BEFORE (thin) | AFTER (Earth-like) |
|----------|---------------|--------------------|
| 23.5–26° | ~0%   | 2.3%  (fringe phasing in) |
| 26–28°   | ~3.8% | **21.8%** |
| 28–30°   | ~15%  | **21.7%** |
| 30–32°   | ~31.7%| 31.9% |
| 32–35°   | ~28%  | 28.3% |
| 35–37°   | ~0.3% | 0.3% (temperate clean) |

A continuous ~**26–35° belt at ~22–32%** instead of a 30–35° sliver. **`band_correctness_check` PASS**
(0 failures, 0 warnings): tropical arid 0% (law intact), temperate arid ~0% (poleward clamp intact),
**subtropical arid 21.67% ≥ the new 15% floor**.

> Note: the core is *arid-significant with a savanna fringe* (~22–32% arid; savanna still present), NOT a
> desert monoculture — by design (Art X + Peetsa's "keep some savanna for variety"). If a denser, more
> desert-*dominant* core is wanted later, the next lever is reducing the **humidity diversion** in poleward
> subtropical (the `LAT_SUBTROPICAL_HUMID` re-route inside the ladder) and/or raising the `LAT_ARID` primary
> tag weight — both riskier (Art X), so left for a live-eyeball decision rather than pre-emptively pushed.

## Anti-regression guards (so we don't slide back — either direction)

1. **Floor guard (NEW, `band_correctness_check.py`):** `BAND_FLOORS = {"subtropical": {"arid": 0.15}}` — if a
   future clamp/ramp change starves the belt back below 15%, the checker WARNs. This is the symmetric partner
   to the existing wrong-band *ceiling* checks (it catches "too sparse", they catch "leaked into wrong band").
2. **Ceiling guards (existing):** tropical arid ≤ 0.5% + temperate arid ≤ 1% (FAIL) — widening the belt must
   never re-open the tropics-desert bug or the temperate badlands leak. Both still 0% / ~0%.
3. **The law (`demoteEquatorial*`, LOW=23.5°):** widening moved HIGH (27.0), **never LOW** — arid still cannot
   cross 23.5° into the tropics regardless of ramp tuning.
4. **-D knobs for safe iteration without recompiling:** `-Dlatitude.aridRampHigh` (belt width; lower = wider,
   floor at 23.5°), `-Dlatitude.aridPolewardRampLow/High` (poleward temperate clamp). Tune vs atlas + the
   checker; never ship a value that fails the checker.

## How to re-verify after any worldgen change touching the belt
```
JAVA_HOME=$(/usr/libexec/java_home -v 25) python3 tools/atlas/atlas_runner.py generate \
    --step 64 --seed 2591890304012655616 --size small --no-viewer-open
python3 tools/atlas/band_correctness_check.py run-headless/latdev/atlas-runs/<newest>
# require: === PASS ===  (tropical arid 0, temperate arid ~0, subtropical arid >= 15% floor)
```

## Provenance
- Lever A: `BADLANDS_LAT_RAMP_HIGH_DEG` / `DESERT_LAT_RAMP_HIGH_DEG` 32.0 → `parseDouble(..."aridRampHigh","27.0")`.
- Lever B: `pickTropicalGradientNoSwamp` `case 1 ->` (both pick bodies) `LAT_TRANS_ARID_TROPICS_1_*` → `LAT_ARID_*`.
- Floor guard: `tools/atlas/band_correctness_check.py` `BAND_FLOORS`.
- Related: [[v1p4-tropical-drybiome-equator-overhaul]] (the tropics law), the poleward-arid clamp +
  band-correctness check in `docs/binder/worldgen-regression-prevention-20260625.md`.
