# Biome Geography Audit — is Latitude broken, not wired, or working? (2026-07-07)

`status: final` · bounded audit per Peetsa's working card (no fixes; evidence + verdict + smallest next
proofs) · code state: HEAD `1d2402b6` (post Slices A/B/C) + the build.gradle V2-flag forwarding fix
committed with this doc · evidence: session scratchpad `biome-audit/` (exact-ID atlases),
`geoclimate-probe.csv`/`geoclimate-7500.csv` (12.5k-sample authority-field probes), `audit/biome-path-map.md`
(code-path trace with file:line)

## Plain-English verdict

**Latitude is not broken — but the two halves are in very different states.** The climate/geography
BRAINS (GeoAuthority + ClimateAuthority) produce genuinely earthlike fields: textbook latitude belts, a
real Sahara-like arid belt at 20–30°, wet windward coasts drying inland, rain shadows behind mountains,
strong tropical-highland cooling, cold polar caps. But in the config you actually play
(`biomeConsumerV2=off`), **none of that moisture logic reaches the visible biome map** — the map you see
is drawn by the older latitude-band + province machinery (which is lawful and looks good, but has no rain
shadows and no windward/leeward logic, by construction). Flipping the consumer ON wires the climate brain
to the map — and today's measurement shows that wiring is ~2% of the map, HALF right (real climate
corrections the stripe logic can't make) and HALF wrong (it re-introduces desert/badlands into the
10–20° tropics against the project's own dry law, and paints flat-polar snow biomes onto cooled warm-band
mountains — the L14 failure class). The consumer needs three named fixes before it can ever ship; nothing
in today's live config is affected by them.

## 1. Which path actually picks the visible biome (per config)

Full trace with file:line: `audit/biome-path-map.md` (session scratchpad). Summary:

| Config | Visible biome driven by | V2 influence on the MAP | Rain shadows |
|---|---|---|---|
| 1 (live: geoV2+terrainV2 S=0.4, consumer off) | Latitude bands + ProvinceAuthority + arid ladder + tag pools (pre-2.0 machinery; NOT flag-gated) | Only via TERRAIN: land01 moves ground height; two waterline vetoes (raised-land + Slice-C sunk-land mirror) re-align ocean-family membership with biased terrain | **NOT WIRED** |
| 2 (candidate: +climateV2+biomeConsumerV2, ocean sub-flag off) | Same machinery + `applyClimateCompatReroll` (~L9972): repaints only where the coarse ClimateClass grossly contradicts the chosen biome | Climate classes reach the map at class-mismatch sites only (1.76% of cells); reroll reads the CLASS ENUM, not the continuous precip/lift/shadow fields | Wired **coarsely** |
| 3 (all V2 off) | Pre-2.0 machinery on vanilla/Terralith terrain | none | NOT WIRED |

ProvinceAuthority (`classifyWarm`, thresholds 0.38/0.62) and OceanDistanceField drive ALL configs —
they are not flag-gated.

## 2. The authorities' own fields are earthlike (pure-JVM probe, 12,561 samples, UI-Regular geometry)

- **Latitude belts (land only)**: temp01 0.85→0.01 monotone equator→pole; precip01 0.67 (0–10°) →
  **0.084 at 20–30° (a hard Sahara-belt minimum)** → 0.57 (40–60°) → 0.09 (70–80°). Textbook.
- **Coast→interior**: precip 0.42/0.30/0.21 at coastDist 0–500/500–1500/1500–3000 with continentality
  0.10/0.72/1.00. Clean maritime→continental drying.
- **Tropical highlands**: flat tropics temp01 0.97 vs mountain-intent tropics 0.615 (altCool 0.61) —
  strong highland cooling (the disclosed Phase-3 `kAlt` residual is VISIBLE here; see finding P1-B).
- **Rain shadows**: shadowed land (rainShadow01>0.3) is drier than the land mean (0.298 vs 0.343); a
  concrete 40.5° transect shows wet windward coast (precip 1.00→0.65) drying over the ridge (0.35) with
  marked shadow immediately downwind. One quirk: `windwardLift01` reads displaced toward the NEXT ridge
  rather than the first windward face (aggregate lift on mountain cols 0.087 < flat 0.232) —
  `PLAUSIBLE-RISK`, cosmetic to consumers that read precip01 (which behaves), flagged for Phase-5-era
  eyes.
- Polar note: precip01 rises to 0.41 at 80–90° (Earth's poles are dry deserts) — harmless today (all
  frozen), worth a look when precipitation ever drives snow depth. `PLAUSIBLE-RISK`, minor.

## 3. The visible map under the LIVE config is lawful (exact-ID atlas, R7500 step64)

Band composition (config 1): equator jungle+savanna+warm oceans; 20–30° savanna 34% + desert 14%; 30–40°
plains/desert/savanna; 40–50° forest+plains; 50–60° taiga; 60+ snowy. **Tropical dry LAW: badlands+desert
= 0.02% at 0–20°** (the law's structural guarantee holds). Sharp-transition inventory (from the path map):
the arid ladder's step 0↔1 is frayed (ff713f57), steps 1↔2/2↔3 remain near-sharp (correlated dither only,
known P2); band edges are jittered ~1408 blocks (plausible ecotones); province cuts ride 1536–1792-block
noise (coherent borders, not artifacts).

## 4. The consumer config measured (config 2 vs config 1, same seed/step): 973/55,225 cells change (1.76%), all at 10–40°

| Change class | Cells | Verdict |
|---|---|---|
| jungle/bamboo_jungle → desert/badlands at **10–20°** | ~432 | **CONFIRMED BUG (P1-A):** re-introduces tropical dry biomes the project's own LAW demotes (0.02% → 2.01% dry share at 0–20°). The reroll runs AFTER the law gates and repaints from the raw class, undoing them. |
| savanna/jungle → snowy_plains/snowy_slopes/grove at 10–38° | ~233 | **CONFIRMED BUG (P1-B):** the L14 class live again — ClimateAuthority's terrain-DECOUPLED altitude proxy (`kAlt`) over-cools mountain-intent columns (e.g. 38.5°: temp01 0.35, altCool 0.48) and the repaint picks FLAT-POLAR families (snowy_plains) instead of altitude families. Two compounding defects: proxy too strong; mapping picks the wrong family. |
| jungle → desert at 25.5° (precip01 0.00) | ~90 | **CLIMATE-CORRECT (working):** true arid-belt columns the stripe logic had painted jungle; the moisture model rightly overrides. |
| desert → forest/flower_forest/plains at 30–40° (e.g. 34.7°, precip01 0.70, ocean fetch 3150 blocks) | ~87 | **CLIMATE-CORRECT (working):** wet windward coasts the stripe logic can't see — this is the fetch/rain-shadow logic doing exactly what it was built for. |

Sampled-column evidence (visible biome both configs + authority fields per column) is in this doc's
commit-referenced scratchpad tables; sanity columns (equator savanna, polar snowy_plains, 30° belt
savanna) are unchanged across configs.

## 5. Findings (deduplicated, with smallest next proofs)

- **P1-A — Consumer undoes the tropical dry law** (`CONFIRMED`, headless exact-ID). Smallest fix shape
  (NOT implemented — needs authorization): route reroll repaints through the same demote gates the base
  path uses (or veto arid-class repaints below the 23.5° ramp). Smallest proof: this exact two-atlas diff
  re-run; acceptance = dry share 0–20° back ≤0.1% with the 25.5°-class corrections retained.
- **P1-B — kAlt over-cool + polar-flat repaint on warm-band mountains** (`CONFIRMED`). Fix shape: (i)
  temper/replace the altitude proxy with REAL terrain height — Phase 4's wrapper makes actual height
  available for the first time; (ii) map cooled-mountain classes to altitude families (grove/snowy_slopes)
  not snowy_plains. Proof: same diff; acceptance = zero snowy_plains cells below 45° + Lane-6 tripwire T3
  stays green.
- **P2-A — windwardLift01 placement quirk** (`PLAUSIBLE-RISK`): displaced toward the next ridge; precip01
  unaffected. Eyes-on when a consumer first reads lift directly.
- **P2-B — polar precip high** (`PLAUSIBLE-RISK`, minor).
- **P2-C — arid ladder steps 1↔2/2↔3 still near-sharp** (carried from the audit report P2-5; visual
  salience unmeasured).
- **P0-class tooling catch fixed IN this pass:** `climateV2`/`biomeConsumerV2` flags were never forwarded
  to the forked headless JVM (the L17 forwarding class, second occurrence today) — the first "config 2"
  atlas silently ran with the consumer OFF and produced a 0.00% diff (the tell that caught it).
  build.gradle now forwards every LatitudeV2Flags enable. Without this fix, any past-or-future headless
  claim about consumer behavior was untestable.

## 6. Does this change the A/B/C sequencing or the Phase-4 live gate?

**No.** Slices A/B/C stand as completed (the live config's map is unaffected by the consumer findings).
The Phase-4 live gate (Slice E) tests terrain under the live config and is not blocked by P1-A/P1-B. The
consumer findings define a NEW bounded slice ("Consumer law-compliance: P1-A + P1-B") that must go green
BEFORE Phase 5 ever flips `biomeConsumerV2` on — added to the path as a prerequisite of the consumer
flip, not of Phase-4 closure. Rain-shadow visibility on the live map remains "not wired" by design until
that slice lands.
