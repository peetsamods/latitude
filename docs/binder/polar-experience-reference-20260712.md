# Polar Experience — Consolidated Reference (2026-07-12)

**What the pole does, why, and where every dial lives.** One document instead of ~12 rounds of
append-only pass-log entries. Every number below was read from the **current source** on branch
`port/canonical-26.2-pivot` (HEAD `e232d96e`), not copied from the pass log — where they disagree, the
code wins and the drift is called out in the "Source-vs-narrative discrepancies" section at the end.

Companion docs (cross-links):
- `docs/binder/phase5-boundary-experience-plan-20260709.md` — the running pass log (TEST 75→81), round by round.
- `docs/binder/polar-experience-review-20260711.md` — the Creative Director's polar review (findings F1/F2/F3/R1/R2).

Audience: Peetsa reads the plain-language body; a future dev session reads the tables for exact
identifiers, files, and constants.

> **Scope note.** This covers the NORTH/SOUTH polar cap (approach to |lat| 90°). The EAST/WEST border
> storm (sandstorm / whiteout at the projection edge) is a *separate* system and appears here only where
> it shares constants with the polar warning ladder (the "KEEP-SHARED" coupling). Latitude is always
> `|lat|` (both hemispheres identical) via `LatitudeMath.absLatDegExact`.

---

## 1. The player's journey, 85° → 90°

Walk north (or south). Nothing polar happens until **85°**. Everything is symmetric about the equator.
Three independent onsets stack up: **85°** atmosphere (snow/fog/storm-sky/wind/frozen-water), **87°**
the blizzard *look* drives, **87.5°** the first thing that actually *hurts you* (frost + slowness), then
freeze *damage* from ~**88°** worsening to a lethal pole. The warning text fires on its own ladder at
85 / 87 / 89 / 89.7°.

| Latitude | What the player sees / hears / feels | Source of the number |
|---|---|---|
| **< 75°** | Normal weather. (Correctness clamp: at/above **75°** any rain column is force-rendered as snow — never rain at the cap.) | `PolarPrecipitationRule.FORCE_SNOW_DEG = 75.0` |
| **75–85°** | Still visually normal, but **vegetation is already thinning** — grass/ferns/flowers/sugarcane fade out on a frayed noise ramp from **78°** (half-gone ~82°, bare by **86°**). | `PolarVegetationFade.ONSET_DEG = 78`, `FULL_DEG = 86` |
| **85°** (ambient onset) | **Everything atmospheric switches on at once, seamlessly:** gentle snow flurries begin (budget 2 particles/tick); screen/depth fog begins to tighten; the **storm sky** starts grey­ing; the **wind bed** loop starts (a breath); all exposed water becomes eligible to **freeze to ice**. **Warning tier 1** fires once: *"Snow begins to fall. Blizzard conditions ahead -- consider turning back."* | `AMBIENT_ONSET_DEG=85`, `STORM_ONSET_DEG=85`, `PolarWindSound.START_DEG=85`, `PolarWaterFreezeRule.FREEZE_ALL_DEG=85`, `PolarWarningEpisode.TIER_1_DEG=85` |
| **86°** | Sky is **clearly overcast** (storm level already 0.4 — the sun is visibly going). Snow denser, fog heavier. Depth-fog view distance ≈ **143 blocks**. | `stormLevel` reaches 0.4 at 86°; `POLAR_FOG_END_CURVE=0.80` |
| **87°** (blizzard-look onset) | The flakes stop drifting and start **driving sideways** — fall speed + a hard sideways wind ramp up, and a dense low second particle pass kicks in so it reads as a real ground blizzard. **Warning tier 2** fires once: *"The blizzard deepens -- hypothermia is setting in. Turn back while you can."* | `BLIZZARD_ONSET_DEG=87`, `PolarWarningEpisode.TIER_2_DEG=87` |
| **~87.5°** (hazard onset + full storm) | **The cold starts to bite.** Slowness I begins; frost visibly builds on screen. Sky is now **full overcast, sun gone** (storm level hits 1.0). Depth-fog view distance ≈ **75 blocks**. No HP loss yet — this 0.5° is a deliberate grace band. | `HAZARD_ONSET_DEG=87.5`, `STORM_FULL_DEG=87.5` |
| **~88°** (damage onset) | **Freeze DAMAGE begins.** HUD hearts turn **blue** at this exact instant (frost visual crosses vanilla's fully-frozen 140 threshold). First bite ≈ **1 HP every 3 s** (½ heart / 60 ticks). | `DAMAGE_ONSET_PROGRESS=0.2` → 87.5 + 2.5·0.2 = 88.0; `FREEZE_DAMAGE_MIN_HP=1.0`, `FREEZE_DAMAGE_INTERVAL_FAR=60` |
| **~88.33°** | **Slowness II** and **Mining Fatigue** layer in. | `SLOWNESS`/`rampAmplifier` band 2 at progress ≥ 1/3; `MINING_FATIGUE_PROGRESS=1/3` |
| **89°** | Deep hazard. **Slowness III** (~89.17°), **Weakness** climbing, freeze damage now serious (2 HP every ~1.75 s, ~1.1 HP/s and accelerating). **Warning tier 3 (DANGER)** fires once, in **red**: *"DANGER: Lethal blizzard conditions ahead. Turn back."* A subtle **cold edge-vignette pulses** in time with the text. | `TIER_3_DEG=89`; Slowness III at progress ≥ 2/3 ≈ 89.17°; `PolarWarningVignette.TIER_DANGER=3` |
| **89.7°** | **Warning tier 4 (LETHAL)** fires once, in **red, slightly larger** (1.15×): *"Severe hypothermia -- you are freezing to death."* The vignette pulses **deeper** and briefly **warms toward a deep ember**, then lingers faintly while you stay in the lethal zone. Freeze damage ~3 HP/s → 6 HP/s. | `TIER_4_DEG=89.7`; `LETHAL_TEXT_SCALE=1.15`, `LETHAL_PEAK=0.40`, `LETHAL_LINGER=0.08` |
| **90°** (the pole) | **Full whiteout.** Snow budget **60/tick**, wind gale **1.10 blocks/tick**, screen whiteout top-coat at its 0.35 alpha ceiling, depth-fog view distance **~16 blocks** (≈1 chunk), wind loop at its **0.8** howl. Freeze damage **3 HP every 0.5 s (~6 HP/s)** — lethal within seconds. | `SNOW_MAX_COUNT=60`, `blizzardWindMagnitude`=0.10+1.00, `POLAR_FOG_END_NEAR=16`, `MAX_VOLUME=0.8`, `FREEZE_DAMAGE_MAX_HP=3.0` / `FREEZE_DAMAGE_INTERVAL_NEAR=10` |

**Wind / whiteout / fog while sheltered:** none of these snap off when you step under a roof. A graded
enclosure estimate (`exposure01`, §2 Exposure) scales the wind muffle, the whiteout alpha, and the local
particle budget continuously — full in the open, ~0 sealed in, partial at a doorway. The world-scale
storm (grey sky + vanilla snowfall) and the depth fog show through any window because they're
world-space/depth-correct and vanilla occludes them with walls for free.

---

## 2. The systems

Each is a small **pure Core-Logic class** (zero Minecraft imports, unit-tested in a plain JVM) plus a
thin Minecraft-side consumer. This is the project's portability discipline — the math is testable and the
glue is trivial.

### Ambient window (snow + fog envelope)
- **Does:** the 85→90 atmosphere ramp shared by snow density, screen fog, and depth fog.
- **Files:** `core/PolarHazardWindow.java` (`ambientProgress`, `snowCount`, `fogIntensity`); consumed by `GlobeModClient.spawnAmbientPolarSnow` (particles) and `PolarWhiteoutOverlayHud` (screen fog).
- **Key constants:** `AMBIENT_ONSET_DEG=85`, `AMBIENT_FULL_DEG=90`, `SNOW_MIN_COUNT=2`, `SNOW_MAX_COUNT=60`.
- **Design:** a **fixed per-tick budget**, never a catch-up accumulator (the "B-3b anti-backlog law" — a paused/lagging client never dumps a backlog of flakes on resume). Snow spawns on every 4th tick (`getGameTime() & 3`).

### Blizzard visual drive (fall speed + sideways wind + dense second pass)
- **Does:** from 87° turns the gentle flurry into a driven gale — ramps flake fall speed and sideways wind, and gates a dense low second particle pass.
- **Files:** `core/PolarHazardWindow.java` (`blizzardDrive`, `blizzardWindMagnitude`, `blizzardFallSpeed`); `GlobeModClient.spawnAmbientPolarSnow`.
- **Key constants:** `BLIZZARD_ONSET_DEG=87`, `BLIZZARD_FULL_DEG=90`, `BLIZZARD_WIND_BASE=0.10`, `BLIZZARD_WIND_GALE=1.00`, `BLIZZARD_FALL_BASE=0.05`, `BLIZZARD_FALL_GALE=0.25`. Spawn geometry: `SNOW_ENVELOPE=16` wide, main pass fills `py-2..py+14` (triangular, peak eye-level), second pass fills `py-1..py+6` with `SNOW_SECOND_PASS_WIND_MULT=1.9`.
- **Design decision (important):** the wind is spawned **deliberately huge** because vanilla `SnowflakeParticle.tick()` decays horizontal velocity ~5%/tick and pins vertical velocity to its own ~0.081/tick terminal. Cranking the *fall* speed does nothing (it always converges to terminal); the "blizzard" read is carried entirely by the **sideways** wind, which must overshoot to still read as wind-driven after ~20–30 ticks. Decoupled from the hazard onset on purpose (Peetsa: keep the look, only move the mechanics).

### Hazard window + the frost-visual / damage split
- **Does:** the only player-*affecting* band. Slowness/Weakness/Mining-Fatigue amplifiers, the frost visual, and the freeze-damage curve.
- **Files:** `core/PolarHazardWindow.java`; applied server-side in `GlobeMod.borderUxTick` (every `END_SERVER_TICK`); vanilla's own auto-freeze cancelled by `mixin/LivingEntityFreezeDamageMixin.java`.
- **Key constants:** `HAZARD_ONSET_DEG=87.5`, `HAZARD_LETHAL_DEG=90`, `SLOWNESS_MAX_AMP=2` (III at pole), `WEAKNESS_MAX_AMP=1`, `MINING_FATIGUE_PROGRESS=1/3`, `DAMAGE_ONSET_PROGRESS=0.2` (~88°), `FROZEN_THRESHOLD_TICKS=140`, `FROST_POLE_HEADROOM_TICKS=8` (holds to 148), `FREEZE_DAMAGE_INTERVAL_FAR=60`/`_NEAR=10`, `FREEZE_DAMAGE_MIN_HP=1.0`/`_MAX_HP=3.0`.
- **Why the split (plain-language double-damage story):** vanilla only deals freeze damage while an entity is *fully frozen* (`ticksFrozen ≥ 140`), then a **fixed** 1 HP every 40 ticks. Driving that single knob made damage a near-binary flip — the old curve only pushed past 140 in the last ~0.03°, so Peetsa stood at 89° and took nothing. So the mod applies its **own** latitude-scaled damage (interval 60→10 ticks, amount 1→3 HP) that builds from ~88° and worsens to the pole. But an earlier round capped the frost *visual* at **139** (one tick below the threshold) precisely so vanilla's auto-damage never fired — which also killed the **blue frozen hearts** (they only tint blue at `ticksFrozen ≥ 140`). The fix: let the frost visual **cross 140** (blue hearts fire exactly at the ~88° damage onset) and instead **cancel vanilla's own damage call at its source** for in-band players only, so the mod's curve is the *sole* freeze-damage source and the two never double-dip. The frost visual is set **every** tick because vanilla decays `ticksFrozen` ~2/tick out of powder snow.
- **Gate:** `GlobeMod.isInPolarFreezeDamageBand` (survival/adventure player, globe overworld, `|lat| ≥ 87.5°`) — the single gate the mixin reads, mirrored to `borderUxTick`'s own test so they can't drift.
- **Note:** B-4 **removed Blindness** from the hazard (the old hard blind-snap) — the smooth whiteout now carries vision loss.

### Warnings ladder
- **Does:** the four episodic hypothermia messages; each fires once per depth, re-arms only on full retreat below 84°.
- **Files:** `core/PolarWarningEpisode.java` (pure episode state machine); text + render in `client/GlobeWarningOverlay.java`.
- **Key constants:** `RETREAT_REARM_DEG=84`, tier degrees **85 / 87 / 89 / 89.7**; display `POLE_WARN_HOLD_TICKS=200` (~10 s) + `POLE_WARN_FADE_TICKS=20` (~1 s each way).
- **Current text strings (verbatim):**
  - Tier 1 (WARN_1, 85°): `"Snow begins to fall. Blizzard conditions ahead -- consider turning back."`
  - Tier 2 (WARN_2, 87°): `"The blizzard deepens -- hypothermia is setting in. Turn back while you can."`
  - Tier 3 (DANGER, 89°): `"DANGER: Lethal blizzard conditions ahead. Turn back."` (RED)
  - Tier 4 (LETHAL, 89.7°): `"Severe hypothermia -- you are freezing to death."` (RED, 1.15× scale)
- **KEEP-SHARED constraint:** the tier degrees mirror `LatitudeMath.POLAR_STAGE_*_PROGRESS` (0.9444 / 0.9667 / 0.9889 / 0.9967 = 85 / 87 / 89 / 89.7°). Those constants are **shared with the E/W storm axis** (B-3-P3 coupling), so they do **not** move when the player-affecting hazard onset moves. That's why tier 2 fires at 87° while the first mechanic is at 87.5° — the wording ("hypothermia *is setting in*", present-continuous) is honest foreshadowing 0.5° early, not a false claim.
- **Rendering design:** all four tiers get a dark 1px keyline (near-black `0x080609`, `TitleStyle.OUTLINE_OFFSETS_8`) so red *and* white text reads on the brightest screen the game ever draws. Warnings are drawn **NON-bold** on purpose — see Dead-Ends §4.

### Warning vignette (DANGER/LETHAL punctuation)
- **Does:** a subtle dark edge-darkening that pulses in sync with the DANGER/LETHAL text. Only the two serious tiers earn it.
- **Files:** `core/ui/PolarWarningVignette.java` (pure wall-clock envelope); `client/PolarVignetteOverlayHud.java` (draw); armed from `GlobeWarningOverlay`.
- **Key constants:** `DANGER_PEAK=0.25`, `LETHAL_PEAK=0.40`, `HOLD_FRAC=0.5`, `LETHAL_LINGER=0.08`; timing `RISE_MS=250`, `SETTLE_MS=500`, `HOLD_END_MS=9000`, `MELT_MS=1000`; LETHAL warm ember decays by `LETHAL_WARM_DECAY_END_MS=2500`, blended at most `WARM_BLEND_MAX=0.6` toward ember `(70,12,8)`; cold tint `(8,10,16)`; `EDGE_MARGIN_FRAC=0.20` (center ~60% stays clear), `BANDS=16`.
- **Design:** **wall-clock driven** (never game ticks) so it can't rubber-band on a teleport tick-stall. Reduce Motion → static faint level instead of a pulse. Gated on the same `surfaceOk` binary as the text it punctuates (deliberately binary, *not* exposure-graded — it's punctuation, not atmosphere).

### Wind sound bed
- **Does:** a single looping vanilla `ELYTRA_FLYING` wind-rush, volume rising from a breath at 85° to a howl near the pole.
- **Files:** `core/PolarWindSound.java` (pure envelope + hysteresis); `client/PolarWindSoundInstance.java` (looping instance on the `WEATHER` category, so the player's Weather slider mutes it).
- **Key constants:** `START_DEG=85`, `STOP_DEG=84.5` (0.5° hysteresis dead band), `FULL_DEG=90`, `MAX_VOLUME=0.8`, `PITCH=0.85`, `EASE_EXP=2.0` (squared ramp → 85–87° is only a whisper), `MIN_ALIVE_VOLUME=0.0015` (inaudible floor so the sound engine never culls the channel), `SHELTERED_VOLUME_SCALE=0.35` (muffle floor), re-arm cooldown 20 ticks.
- **Design:** the loop does **not** stop when you go indoors — shelter **muffles** it to a fraction (real wind carries through walls); only true deactivation (off-globe / below 84.5° / other dimension) silences it. `windMuffleFactor(exposure01)` blends 1.0↔0.35 continuously.

### Exposure (`exposure01`) — the 13-point enclosure estimate
- **Does:** replaces the old binary "can I see sky one block up" bit with a graded 0–1 estimate for the *presentation* systems.
- **Files:** `core/PolarExposure.java` (pure `fraction`/`whiteoutScale`/`particleBudget`); sampled in `client/GlobeClientState.computeExposure01`/`sampleExposure01`.
- **Geometry:** `SAMPLE_COUNT=13` — center column + a ring at radius 3 (8 points) + the 4 cardinals at radius 5; `exposure01 = seen / 13`. Cached, recomputed only on block-position change or every `EXPOSURE_RECOMPUTE_TICKS=5` ticks (13 heightmap lookups are cheap but not free). Deep underground (`y < sea-2`) short-circuits to 0.
- **Consumers — graded vs deliberately binary:**
  - **Graded** by `exposure01`: wind muffle, whiteout-overlay alpha, local ambient-particle budget. (Under Peetsa's open freestanding arch a single overhead lintel used to flip him "fully indoors"; now the ring still sees sky → ~0.9 → effectively outdoors.)
  - **Deliberately BINARY** (`surfaceOk`): the warning text + the DANGER/LETHAL vignette (they're episode punctuation, not atmosphere) and the world-space storm sky / depth fog (those are vanilla-occluded by real walls, so they need no gate at all).
  - **Untouched:** the server hazard mechanics (freeze/slowness) do not read exposure — the cold doesn't care whether you have a roof.

### Depth fog (genuine render-distance fog)
- **Does:** tightens Minecraft's own cylindrical render-distance fog toward the pole — depth-correct, wall-aware, so heavy exterior haze shows through a doorway while your own near walls stay crisp.
- **Files:** `core/PolarHazardWindow.java` (`polarFogEnd/Start`, `polarFogEndFraction/StartFraction`); `mixin/client/FogRendererPolarSetupMixin.java` (injects `@At("RETURN")` on `FogRenderer.setupFog`, mutates `FogData.renderDistanceStart/End` + tints `color`).
- **Key constants:** `POLAR_FOG_END_NEAR=16` (≈1 chunk of sight at the pole), `POLAR_FOG_START_NEAR=5`, `POLAR_FOG_END_CURVE=0.80`, `POLAR_FOG_START_CURVE=0.45` (START pulls in faster than END so the band widens gradually). Only ever **tightens** vanilla's fog; seam-free at/below 85°; skipped entirely if the camera is in water/lava/powder-snow.
- **Design:** built after discovering the *old* fog mixin was silently dead (Dead-Ends §4). This uses `require`-nonzero binding so it fails loudly if `setupFog` ever renames, instead of silently no-opping.

### Storm sky
- **Does:** greys the sky, fades the sun/moon, and thickens vanilla snowfall by lifting the **client** rain level toward 1.0 — one value driving the whole overcast look via vanilla.
- **Files:** `core/PolarHazardWindow.java` (`stormLevel`); `mixin/client/ClientLevelStormSkyMixin.java` (injects `@At("TAIL")` on `Level.getRainLevel`, `ClientLevel`-only guard so the integrated server's weather is never touched).
- **Key constants:** `STORM_ONSET_DEG=85`, `STORM_FULL_DEG=87.5` (steeper than the 85→90 ambient ramp — the old linear ramp still showed sun at 86°; now it's 0.4 overcast at 86° and full storm/sun-gone by 87.5°).
- **Design:** NOT gated on `surfaceOk` — the storm sky and the vanilla snowfall it drives are world-space (vanilla occludes them with a roof for free), so the storm shows through any window. This is also what makes any precipitation render even in "clear" weather (which is why the rain→snow clamp below matters).

### Forced snow (no rain at the poles)
- **Does:** at/above 75° any RAIN column renders as SNOW — streaks, ground splash, and rain *sound* all in one chokepoint.
- **Files:** `core/PolarPrecipitationRule.java` (`FORCE_SNOW_DEG=75`); `mixin/client/ClientLevelPolarSnowMixin.java` (injects `getPrecipitationAt`, RAIN→SNOW only; NONE stays NONE).
- **Why:** vanilla's noise router places latitude-blind `river`/`ocean` columns (temperature 0.5, precipitation RAIN) even at 90°, and the weather renderer samples a grid of nearby columns — so a polar river renders rain even while you stand on snow. **Client-only, non-destructive** (snow accumulation is a separate server system, out of scope). 75° sits safely poleward of the rainiest cold band (subpolar taiga ends ~67°) and equatorward of the 85° ambience so the two never fight.

### Water freeze (all exposed water freezes at the pole)
- **Does:** at/above 85° every genuinely-freezable exposed water column becomes eligible to turn to ice — including your own bucket-placed water.
- **Files:** `core/PolarWaterFreezeRule.java` (`FREEZE_ALL_DEG=85`); `mixin/BiomePolarWaterFreezeMixin.java` (**Redirect** of just the `warmEnoughToRain` veto inside `Biome.shouldFreeze`, `require=1`).
- **Why:** same latitude-blind `river`/`ocean` (temperature 0.5) problem — `Biome.shouldFreeze` bails immediately on "warm enough to rain", so polar river/ocean water never freezes. The redirect neutralizes **only** that temperature veto; vanilla's genuine water/light/edge/`LiquidBlock` checks all still run, so it never fabricates ice on non-water, and it inherits vanilla's edge-inward freeze cadence (no latitude fade needed). One hook covers both **ongoing** tick-freeze and **worldgen** (`LakeFeature`/`SnowAndFreezeFeature`).
- **Trade-off (by design):** because it hooks the same decision vanilla uses for *all* exposed water, it also freezes player-placed water at the pole — polar bases need a heat source or a sheltered/lit spot, like a real one. This is why it's a flag (a world-modifying kill switch) even though it defaults on.

### Vegetation fade
- **Does:** thins surface grass/ferns/flowers/sugarcane/bushes toward the pole so the cap reads as bare snow/ice.
- **Files:** `core/PolarVegetationFade.java` (`keepChance01` smoothstep + `stripByNoise`); `mixin/PolarVegetationFadeGuardMixin.java` (HEAD-cancellable on `SimpleBlockFeature`/`BlockColumnFeature` place).
- **Key constants:** `ONSET_DEG=78` (full-keep below), `FULL_DEG=86` (bare above), half-stripped ~82°; `SURFACE_MARGIN=5` (only strips within 5 blocks of the world-surface heightmap, so lush-cave vegetation dozens of blocks down is never touched). Trees are **not** touched (the tree-line/extreme-polar guards own those).
- **Design:** frayed on a coherent province ValueNoise2D field (Art VI — a natural fade, never a hard ring). Placement-time only; existing chunks are never rewritten (legacy-worldgen pin). `ONSET_DEG`/`FULL_DEG` are live-tunable via `-D` (see §3) — **but those two are not forwarded in `build.gradle`** (discrepancy noted below).

### Whiteout overlay (the "engulfed" top-coat)
- **Does:** a flat screen-space fill that adds a soft close-in white when you stand exposed and deep in.
- **Files:** `client/PolarWhiteoutOverlayHud.java`; envelope `PolarHazardWindow.fogIntensity` via `GlobeClientState.computePoleWhiteoutFactor`.
- **Key constants:** `MAX_ALPHA=0.35` (was 0.90 — demoted to a top-coat once the depth fog carried the real haze), curve `intensity^1.7` (steepened from 0.65 so mid-latitudes are carried purely by depth fog); storm→white palette `(92,108,132)→(238,242,248)`.
- **Design:** has **no depth**, so it can't tell a near wall from far terrain — that's why it keeps its exposure gate (scaled by `exposure01`), while the depth fog does the wall-aware far haze.

### Particle density (perf scaling)
- **Does:** scales the polar snow budget down in lock-step with the player's vanilla Particles video setting.
- **Files:** `core/ParticleDensity.java`; live setting read in `GlobeModClient.polarSnowDensityTier`.
- **Tiers:** `FULL=1.0` (vanilla ALL), `DECREASED=0.5`, `MINIMAL=0.15` (the floor — still a thin blizzard, never literal zero). Pure multiplicative scale of the fixed budget; composes with the `exposure01` scale; no accumulator.

---

## 3. Flags & dials

### Launch flags (`-D` system properties)

| Property | Default | What it changes (plain words) | Defined in | Forwarded in build.gradle? |
|---|---|---|---|---|
| `latitude.polarWaterFreeze.enabled` | **true** | Freezes all exposed water (incl. player buckets) at/above 85°. Off = byte-identical vanilla freezing. | `LatitudeV2Flags.POLAR_WATER_FREEZE_ENABLED` | **Yes** (line 119) |
| `latitude.polarVegetationFade.enabled` | **true** | Fades surface plants out toward the bare cap (78→86°). Off = plants grow at the pole. | `LatitudeV2Flags.POLAR_VEGETATION_FADE_ENABLED` | **Yes** (line 115) |
| `latitude.polarVegetationFade.onsetDeg` | 78.0 | Latitude where the plant fade begins (full-keep below). | `PolarVegetationFade.ONSET_DEG` | **No** (see discrepancies) |
| `latitude.polarVegetationFade.fullDeg` | 86.0 | Latitude where plants are fully gone. Clamped above onset+0.5. | `PolarVegetationFade.FULL_DEG` | **No** (see discrepancies) |
| `latitude.debugPolarSnow` | false | Logs the ambient snow budget vs `\|lat\|` every ~2 s. | `GlobeModClient` | No |
| `latitude.debugDisableWarnings` | false | Kills the whole warning/whiteout/fog presentation stack (debug). | `GlobeClientState.DEBUG_DISABLE_WARNINGS` | No |
| `latitude.debugDisableFog` | false | Kills the polar depth fog (debug). | `GlobeClientState.DEBUG_DISABLE_FOG` | No |
| `latitude.debugEntryTitles` | false | Logs zone/hemisphere/pole-warning title fires. | `GlobeWarningOverlay` | No |

> The freeze/vegetation flags are the only two polar flags that gate *world-affecting* behavior; both
> default **on** because Peetsa explicitly asked for the behavior, and both are byte-identical when off.
> Every polar *presentation* system (snow, fog, storm sky, wind, whiteout, warnings, vignette) is
> **always-on atmosphere** for globe worlds — no flag, like the E/W screen haze. The warning text +
> vignette additionally respect the in-game `showWarningMessages` config toggle; the vignette also
> respects `reduceMotion` (both from `LatitudeConfig`, not `-D`).

### Compile-time constants worth knowing (NOT flags)

These have no launch switch — change them in source. Grouped by system, all in `core/PolarHazardWindow.java`
unless noted:

- **Snow/ambient:** `SNOW_MIN_COUNT=2`, `SNOW_MAX_COUNT=60`, spawn `SNOW_ENVELOPE=16`, volume band `py-2..py+14`, second-pass band `py-1..py+6`, `SNOW_SECOND_PASS_WIND_MULT=1.9` (last three in `GlobeModClient`).
- **Blizzard wind/fall:** `BLIZZARD_WIND_BASE=0.10`, `BLIZZARD_WIND_GALE=1.00`, `BLIZZARD_FALL_BASE=0.05`, `BLIZZARD_FALL_GALE=0.25`.
- **Depth fog:** `POLAR_FOG_END_NEAR=16`, `POLAR_FOG_START_NEAR=5`, `POLAR_FOG_END_CURVE=0.80`, `POLAR_FOG_START_CURVE=0.45`.
- **Whiteout:** `MAX_ALPHA=0.35`, curve exponent `1.7` (`PolarWhiteoutOverlayHud`).
- **Wind sound:** `MAX_VOLUME=0.8`, `PITCH=0.85`, `EASE_EXP=2.0`, `SHELTERED_VOLUME_SCALE=0.35`, `MIN_ALIVE_VOLUME=0.0015` (`PolarWindSound`).
- **Exposure:** `SAMPLE_COUNT=13`, offset ring r=3 + cardinals r=5, `EXPOSURE_RECOMPUTE_TICKS=5` (`PolarExposure` / `GlobeClientState`).
- **Freeze:** `FROZEN_THRESHOLD_TICKS=140`, `FROST_POLE_HEADROOM_TICKS=8`, interval 60→10, HP 1.0→3.0.
- **Vignette timing/peaks:** see §2. **Storm palette / whiteout tint:** storm `(92,108,132)` → white `(238,242,248)`.
- **Storm-ceiling clouds: GONE.** The TEST 79 cloud-deck darkening was **reverted** (commit `734d710d`). There is no live cloud mixin — the only `ParticleTypes.CLOUD` use in `GlobeModClient` is the *E/W* sandstorm haze ring, unrelated to the polar cap. Do **not** document clouds as a live polar feature.

---

## 4. Dead ends & lessons (do-not-retry)

- **Cloud-deck darkening (TEST 79, vetoed TEST 80).** Darkening the sky's cloud deck to grey read *worse*, not better: **dark** clouds gain contrast against the near-**white** pole, so they stood out instead of blending. Reverted whole (`734d710d`). If the storm ceiling is revisited, the direction is **lighter / washed-out** or a gap-fill fallback, never darker. The storm sky greying is already handled by the rain-level lift (§2 Storm sky) — don't re-add a cloud-color mixin.
- **The 139-tick frost cap (blue-hearts lesson).** Capping the frost visual one tick below vanilla's 140 threshold successfully stopped double-damage, but silently killed the blue frozen hearts (which only tint at `≥140`). Lesson: vanilla's freeze visuals and its freeze *damage* both key off the same 140 threshold — you can't suppress the damage by holding *under* the threshold without also losing the visual cue. Correct fix = cross 140 for the visual, cancel the damage **at its call site** (`LivingEntityFreezeDamageMixin`).
- **Binary shelter check.** One overhead block flipped the player "fully indoors" and silenced the whole storm (wind, particles, whiteout) — Peetsa's open freestanding arch killed everything. Replaced by the graded 13-point `exposure01`. Don't reintroduce a single-column `canSeeSky` gate for the atmosphere systems.
- **The silently-inert fog mixin.** For many rounds the mod's one latitude-fog mixin targeted a **dead method** and did nothing — permissive `require=0` registration masked the dead bind, so "why doesn't fog show through the window" had no fog to begin with. Deleted 3 confirmed-dead fog mixin files and rebuilt on `FogRenderer.setupFog` with **nonzero `require`** so a rename now fails loudly at load. Discipline: never ship a mixin with `require=0` that's meant to do work — a no-op that can't fail is indistinguishable from a bug.
- **Bold + keyline font doubling.** Minecraft fakes bold by drawing every glyph twice offset +1px, and `Font` keeps a component's *own* style color over a passed keyline color. So a RED+BOLD warning with a dark keyline rendered a red-on-red, 16-stroke smeared halo. Fix: keyline is a **plain styleless** silhouette (near-black finally sticks) and the warnings are drawn **non-bold** (bold advances would drift the keyline out of registration). The outline + red + LETHAL's 1.15× scale + the ember vignette carry the weight instead. Don't re-add bold to the polar warnings.
- **Snowflake sideways-velocity decay.** Vanilla `SnowflakeParticle.tick()` decays horizontal velocity ~5%/tick and pins vertical to its own terminal, so a normally-scaled wind vector dies to a straight-down drift within ~1 s. The wind values look absurdly large (gale 1.10 blocks/tick) **on purpose** — they must overshoot to survive the decay. Don't "sanity-normalize" them down; and don't try to make snow fall faster (it converges to vanilla's terminal regardless).

---

## 5. Verification state

- **Proven by tests (pure Core-Logic math, ~337-test suite as of TEST 78):** the seven polar pure classes each have a test — `PolarHazardWindowTest`, `PolarExposureTest`, `PolarWindSoundTest`, `PolarWarningEpisodeTest`, `PolarWaterFreezeRuleTest`, `PolarPrecipitationRuleTest`, `PolarVegetationFadeTest` — plus `core/ui/PolarWarningVignetteTest`. All the latitude curves, thresholds, hysteresis, episode transitions, and the exposure fraction are unit-covered. (Suite count not re-run here — no gradle in this pass; the pass log records 337/337 at TEST 78, later rounds 79–81 were revert/doc/loading-footer with no new polar tests.)
- **Both new 26.2 mixins (`FogRendererPolarSetupMixin`, `LivingEntityFreezeDamageMixin`) were bytecode-bind-verified against the real mapped jar** — the exact discipline that would have caught the dead fog mixin.
- **Live-eyeball only (Peetsa, not automated):** the whole *felt* experience — wind bed loudness ("epic"), blizzard sideways read, whiteout heaviness, storm-sky timing, warning legibility, the indoor/through-a-window storm parity. These are subjective and were tuned round-by-round against his flights (TEST 53→81).
- **Known-open items:**
  - **Leather-armor freeze immunity is vanilla-unchanged** — vanilla lets leather armor negate freeze damage; the mod's curve uses the same `freeze()` damage type, so leather still protects at the pole. Flagged for Peetsa's awareness, not fixed.
  - **Low-end perf glance never done** — the ~2× pole particle ceiling (SNOW_MAX 30→60 + the dense second pass) was flagged for a low-end hardware check that hasn't happened. `ParticleDensity` mitigates it (honors the vanilla Particles setting) but the raw ceiling wasn't measured on weak hardware.
  - **Window-in-a-wall fog parity is "good enough, not perfect"** — the depth fog carries most of the "inside is lighter than outside" gap, but standing sealed-in looking out a small window still reads a touch lighter than standing in the open. Judged acceptable, not chased further.
