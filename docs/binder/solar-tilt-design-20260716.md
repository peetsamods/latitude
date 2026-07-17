# Solar Tilt + Seasons — design (Phase 5B-adjacent)

`status: design (not started)`
`branch: phase5-b7-pole-passage @ 6a68aea9 (recon HEAD; two build crews editing src concurrently — line numbers cited from committed HEAD, noted volatile where the file is dirty)`
`author: Solar-Tilt designer (read-only recon; no src touched)`
`owner ask: Peetsa 2026-07-16 — "implement a solar altitude as one travels further from the equator… permanently set one pole to 'summer' and the other 'winter'… the solar altitude could apply everywhere on the map." + two live follow-ups (functional/mob layer; seasons).`

---

## 1. Plain-language owner summary — what you SEE (and what changes)

Today Latitude changes the *map* (biomes) and, near the poles, the *weather* (blizzard, whiteout,
frostbite). The sun itself is still vanilla: it climbs straight overhead at noon no matter where you
stand. This feature makes **the sun's path follow your latitude**, and then lets that path **swing
with the seasons** — the single biggest "this is a real planet" upgrade left in the sky.

**What you'd see standing still and looking up:**

- **At the equator** — the sun climbs to nearly straight overhead at noon, same as vanilla. Home base
  feels unchanged.
- **Travelling poleward (say 40–60°)** — noon stops reaching the top of the sky. The sun traces a
  *lower, flatter arc*; shadows get longer; mornings and evenings have a long golden slant. This band
  (~40–80°) is the **payoff zone** — far enough for the tilt to read clearly, not yet swallowed by the
  polar whiteout.
- **The summer pole** — the sun **never sets**. It circles low around the whole horizon, around the
  clock: a permanent golden "midnight sun."
- **The winter pole** — the sun **never rises**. A long blue-grey twilight and then dark, with the
  stars wheeling overhead even at "noon" on the clock. **Polar night.**
- **Which pole is which flips with the seasons** (see below). Deep in the polar cap (85°+) the existing
  storm sky + whiteout still take over and grey everything out — the sun's personality is the *approach*,
  the storm is the *wall*.

**Seasons (owner follow-up 2).** The tilt isn't frozen. Over a game-year (default **360 game-days**, so
**180 days between solstices** exactly as you asked) the sun swings north, back to the equator, south, and
back. Around the **solstices** the poles are at their most extreme (full midnight-sun / full polar-night);
around the **equinoxes** the whole planet briefly behaves like plain vanilla (sun overhead at the equator,
a normal day/night everywhere). Set the year length to **0** and it *freezes* — that gives you your
original "one pole is summer forever" world as a special case, one config value.

**Monsters (owner follow-up 1).** Yes — and this is the fun part. Because the *real* clock (crops, farms,
your bed, torch light) stays vanilla, we add a thin, honest **"effective sun"** rule that mobs also obey:

- In the **winter-pole polar-night band**, the surface is *dark to monsters even at global noon* — so
  the polar night genuinely crawls with mobs around the clock, and skeletons/zombies out there **don't
  burn** in the (absent) sun. It becomes real hostile country.
- In the **summer-pole midnight-sun band**, the sky-exposed surface is *too bright to spawn on even at
  global midnight* — the endless daylight keeps the open snow clear (caves are unaffected — they're not
  sky-lit), and any undead caught in the open **do** burn under the visible midnight sun.

**The honest seam (say it plainly).** We are **not** rewriting Minecraft's light engine or clock. Wheat
still grows on the world clock; your torches light the same; the F3 clock still ticks one global time.
What we change is (a) how the sky is *drawn* and (b) two narrow mob rules (spawn-in-the-dark and
undead-sunburn) in the polar bands. Those bands are already fog-crushed hostile wasteland, so the seam is
tiny and reads as *atmosphere*, not as a bug.

Everything is behind **one flag, default OFF** (`latitude.solarTiltV2.enabled`) until you fly it.

---

## 2. Scope fence — v1 (with both owner addenda folded in)

**IN:**
- Sun + moon + **stars** path tilt by latitude (the whole celestial sphere tilts together).
- Fixed axial tilt **δ (delta)**, generalised to a **time-varying δ(day)** for seasons.
- Midnight-sun / polar-night **visuals** (sky darkening, gloom, golden midnight-sun tint, stars at "noon").
- **Effective-sun functional layer** (server): surface monster **spawn** rule + undead **sun-burn** rule in
  the polar bands. Optional flavor: **bed refusal** in the midnight-sun band.
- Seasons via one dial (`yearLengthDays`); **frozen mode** (`yearLengthDays <= 0`) reproduces the
  fixed-tilt "permanent summer pole" design exactly.
- Everything gated behind one flag, **default OFF**.

**OUT (explicit non-goals — each a "future phase family" at most):**
- **No light-engine work.** Actual block/entity light levels stay on the global clock. (This is the seam.)
- **No per-region / per-latitude clock.** One global `dayTime` stays global (§10 explains why).
- **No seasonal worldgen / no biome / ice / temperature migration.** The cold bands (frostbite 85°,
  powder-snow, sea-freeze) stay **latitude-fixed year-round**. Story: *the polar ice is eternal; only the
  light changes.* Summer at the north pole is a bright cold, not a thaw.
- **No crop / growth / farming coupling.** Wheat, phantoms, raids, villager schedules all stay vanilla.
- **No orbital progression beyond δ(day).** No moon orbit changes, no eclipses, no day-length change in
  ticks (a MC day is always 24000 ticks; only the sun's *height* changes, not the clock's *speed*).
- **No temperature coupling** to the tilt.

**Flag split — position: keep ONE flag** (`latitude.solarTiltV2.enabled`) for visual + functional +
seasons together. Rationale: the functional layer is *derived from the same evaluator* that drives the
visuals (§8), so a world where the sky says "polar night" but mobs disagree would be an incoherence, not a
feature. A single kill-switch keeps them honest. (If live testing shows the mob layer needs to bake longer
than the sky, a `latitude.solarTilt.mobRules.enabled` sub-flag can be *added* later without restructuring —
but v1 ships as one.)

---

## 3. Recon receipts — the 26.2 pipelines this rides

All paths under `src/main/java/com/example/globe/` unless noted; MC classes decompiled from
`~/.gradle/caches/fabric-loom/26.2/minecraft-client.jar` (`minecraft_version=26.2`, `gradle.properties`).

### 3a. The sky render seam (the make-or-break) — **CLEAN, de-risked**

The 26.2 render rework did **not** bury the celestial transform. `net.minecraft.client.renderer.SkyRenderer`
still exposes a composable `PoseStack`:

- **`public void renderSunMoonAndStars(PoseStack, float sunAngle, float moonAngle, float, MoonPhase, float, float)`**
  — public, takes the shared `PoseStack`. This is the direct analogue of `FogRenderer.setupFog → FogData`
  that the project already hooks. (bytecode receipt below)
- Private per-body: `renderSun(float, PoseStack)`, `renderMoon(MoonPhase, float, PoseStack)`,
  `renderStars(float, PoseStack)` — each handed the same stack.
- **Vanilla celestial transform** (bytecode of `renderSunMoonAndStars`): `pushPose()` →
  `mulPose(Axis.YP.rotationDegrees(-90.0f))` → per body `pushPose()` → `mulPose(Axis.XP.rotation(angle))`
  (note: `Axis.rotation(F)` = **radians**, not `rotationDegrees`) → `renderSun/Moon/Stars` → `popPose()`.
  So: **yaw −90° about Y, then each body revolves about the X axis by its angle.** After the −90° yaw this
  is the classic vanilla "equatorial" arc — sun revolves about the world **north-south** axis, passing
  through the zenith at noon (δ = 0, latitude = 0).
- **`renderSun` places the sun quad at `translate(0f, 100f, 0f)` then `scale(30f, 1f, 30f)`** — the sun sits
  at local **+Y**, 100 blocks up, a 30×30 quad in the local X-Z plane. (Matters for the declination
  calibration, §5.)
- The precomputed angles live in `SkyRenderState { float sunAngle; float moonAngle; float starAngle;
  float rainBrightness; float starBrightness; int skyColor; int sunriseAndSunsetColor; … }`, filled by
  `SkyRenderer.extractRenderState(ClientLevel, float, Camera, SkyRenderState)`. These are **scalar hour
  angles** — they can't encode a tilt or a declination, so the intervention must be at the `PoseStack`,
  not at the angle. (Confirmed why below.)

**Injectable?** Yes: `renderSunMoonAndStars` is public; the bodies still receive a live `PoseStack` (the
geometry is in `GpuBuffer`s but the *orientation* is per-frame `PoseStack`, not baked). A mixin on
`SkyRenderer` composes extra rotations exactly like `FogRendererPolarSetupMixin` composes into `FogData`.

### 3b. How the project already touches sky/fog (compose *with* these)

- **`mixin/client/ClientLevelStormSkyMixin.java`** (@HEAD L28–67) — `@Inject(method="getRainLevel",
  at=TAIL, cancellable)` on `Level`, guarded to the client's own `ClientLevel`. Lifts client rain level
  toward 1.0 over the polar window so **vanilla** greys the sky and fades the sun/moon. Full overcast by
  ~87.5° (`PolarHazardWindow.stormLevel`, L59). **This is what "wins" at 85°+.**
- **`mixin/client/FogRendererPolarSetupMixin.java`** (@HEAD L41–98) — `@Inject(method="setupFog",
  at=RETURN)` on `FogRenderer`; tightens `FogData.renderDistanceStart/End` and tints `FogData.color`
  toward the storm→white palette. Depth-correct, wall-aware.
- **`core/PolarFogLaw.java`** (working tree — S10 fog law v2, **uncommitted, volatile**) — the continuous
  latitude→fog-END-cap curve: **512 blocks at 80° → 4 blocks at 90°** (`POLAR_FOG_CURVE`, L56–65), plus
  `earlyOvercast01` (sky greys from **81°**, L161) and gust logic. This is the visibility wall that hides
  the deep-pole sky.

**Net for solar tilt:** the sun's *personality* reads in **~40–85°** (approach). From **85°** the storm
sky + whiteout dominate; by **~87.5°** the sun is gone to overcast; by **90°** you see ~4 blocks. So the
tilt runs everywhere (cheap) but is *visually* the approach act, and dissolves into the storm cap.

### 3c. The functional-layer hooks (server) — all real 26.2 methods, receipts

**Surface monster spawn light check** (`net.minecraft.world.entity.monster.Monster`, static methods):
- **`isDarkEnoughToSpawn(ServerLevelAccessor, BlockPos, RandomSource)`** — the darkness gate. Bytecode
  shows it reads `getBrightness(LightLayer.SKY, pos)`, `getBrightness(LightLayer.BLOCK, pos)`, and
  **`getMaxLocalRawBrightness(pos)`** (which folds in `getSkyDarken()` — the day/night sky-light dimming),
  compared against `DimensionType.monsterSpawnLightTest()`. **This is the method a mixin overrides** to make
  the polar-night surface "dark" at global noon, or the midnight-sun surface "bright" at global midnight.
- **`checkSurfaceMonstersSpawnRules(EntityType, ServerLevelAccessor, EntitySpawnReason, BlockPos,
  RandomSource)`**, `checkMonsterSpawnRules(…)`, `checkAnyLightMonsterSpawnRules(…)` — the spawn predicates
  that *call* `isDarkEnoughToSpawn`. Overriding `isDarkEnoughToSpawn` reaches all light-gated surface
  monsters at once. (`checkAnyLight…` — creepers etc. — deliberately ignores light; we leave it alone.)

**Undead sun-burn** (`net.minecraft.world.entity.Mob`):
- **`private boolean isSunBurnTick()`** — the gate. Bytecode reads `level.canSeeSky(pos)`,
  `getLightLevelDependentMagicValue()` (brightness), `isInWaterOrRain()`, a `RandomSource.nextFloat()`,
  **and** `level.environmentAttributes().getValue(<EnvironmentAttribute>, position())` — 26.2 routes the
  "is it sunlit here" question through a new **`net.minecraft.world.attribute.EnvironmentAttributeSystem`**.
- `private void burnUndead()` calls `isSunBurnTick()`; `protected EquipmentSlot sunProtectionSlot()` is the
  helmet-saves-you slot. **Target `isSunBurnTick()`** (`@ModifyReturnValue`/`WrapMethod`): force `false` in
  the polar-night band (no sun to burn in), force `true` in the sky-exposed midnight-sun band.

**The global clock accessor (drives BOTH sides, no netcode):**
- **`net.minecraft.world.level.Level.getOverworldClockTime() : long`** — the synced overworld day-time in
  ticks (26.2 reworked time into `net.minecraft.world.clock.WorldClock` / `WorldClocks`; the old
  `getDayTime()` name is gone — cite `getOverworldClockTime`, **note volatility**: confirm the exact name
  at impl time against the WorldClock rework). Also `LevelAccessor.getGameTime() : long` (monotonic).
  Both client and server read the same value → identical δ(day) with **zero packets** (§7).
- `Level.getSkyDarken() : int` — the vanilla sky-light dimming the spawn check already consults.

**Bed refusal (optional flavor):**
- `Player.startSleepInBed(BlockPos) : Either<Player$BedSleepingProblem, Unit>` — the sleep entry point.
  `Player$BedSleepingProblem` has `OTHER_PROBLEM` (carries a `Component` message). A mixin can return an
  `OTHER_PROBLEM` with "The sun never sets here." in the midnight-sun band. **Recommend EXCLUDE v1** (§8c).

### 3d. Precedents this reuses

- **Flag home:** `core/LatitudeV2Flags.java` — every feature flag is
  `Boolean.parseBoolean(System.getProperty("latitude.X.enabled", "false"))` (L41 geoV2, L139 boundaryV2,
  L157 passageV2, L185 polePassageV2). Add `latitude.solarTiltV2.enabled` (default **"false"**) here.
- **Latitude math / convention:** `util/LatitudeMath.java` — `degreesFromZ(border, z)` → [−90..90],
  `absLatDegExact`, `hemisphere(border, z)` returns **`'N'` when `z < centerZ`** (L197–200), i.e.
  **north = −Z, south = +Z** (also matches F3 "Towards negative Z (North)"). Latitude radius handles
  Classic/Mercator shape already.
- **Client latitude read:** `client/GlobeClientState.java` — `isGlobeWorld()`, `evaluate(mc)` →
  `Eval{ …, float exposure01 }` (continuous sky-exposure estimate, L~330), the same
  `LatitudeMath.absLatDegExact(border, player.getZ())` the storm/fog mixins use.
- **Per-world persisted state (for seed-random birth phase):** `world/LatitudeWorldState.java` extends
  `SavedData` with a `SavedDataType` codec and `getGlobeRadius()/setGlobeRadius()` (L42, L96–106). A
  persisted `worldBirthSeasonPhase` int would clone this exact pattern.

---

## 4. The tilt math — `SolarTilt` (pure core, zero MC imports, unit-testable)

New file `core/SolarTilt.java`, same "Core Logic layer, plain-JVM testable" discipline as `PolarFogLaw`.
Everything below is one self-contained astronomy kernel.

### 4a. Conventions

- **Signed latitude** `φ` in degrees, **north positive**. Source: `-LatitudeMath.degreesFromZ(border, z)`
  (negate because north = −Z, and we want north = +φ). Equator φ = 0, north pole φ = +90, south pole −90.
- **Declination** `δ(day)` in degrees — the sub-solar latitude (which latitude the sun is directly over at
  noon). `δ > 0` ⇒ **north hemisphere is summer.**
- **Hour angle** `H` in radians, 0 at local solar noon, +π at local solar midnight. Derived from the
  vanilla time-of-day fraction (§4d).
- World frame for the render vector: **+X = east, +Y = up, +Z = south** (MC + project convention).

### 4b. The two owner-facing quantities (the "law")

**Solar noon elevation** (degrees above the horizon at local noon):
```
noonElevationDeg(φ, δ) = 90 − |φ − δ|
```
- Equator, solstice (φ=0, δ=+30): 90 − 30 = **60°** (sun 30° off zenith even at the equator at solstice).
- 60° latitude, solstice, summer side (φ=+60, δ=+30): 90 − 30 = **60°**… wait — that's noon *elevation*;
  see the table §11 for the full grid. The point: the further |φ − δ|, the lower noon.

**Midnight-sun / polar-night onset latitude** (where the sun stops rising or setting):
```
|φ| > 90 − |δ|         ⇒ the sun's whole daily circle is on one side of the horizon
   summer side (sign φ == sign δ): MIDNIGHT SUN (never sets)
   winter side (sign φ != sign δ): POLAR NIGHT  (never rises)
```
- At solstice, δ = ±30 ⇒ onset at **|φ| = 60°** (everything poleward of 60° is midnight-sun or polar-night).
- At equinox, δ = 0 ⇒ onset at |φ| = 90° (only exactly at the pole) — so near the equinoxes there is **no**
  midnight sun / polar night anywhere. The planet behaves vanilla-ish.

### 4c. The full elevation (the one evaluator the whole feature reads)

Standard equatorial→horizontal projection:
```
sin(altitude) = sin φ · sin δ  +  cos φ · cos δ · cos H
solarElevationDeg(φ, δ, H) = asin( clamp(that, −1, 1) ) · 180/π
```
Checks (derives the two facts above):
- **Noon** (H = 0): sin(alt) = cos(φ − δ) ⇒ alt = 90 − |φ − δ|. ✓
- **Midnight** (H = π): sin(alt) = −cos(φ + δ) ⇒ midnight sun (alt > 0) ⇔ φ + δ > 90. ✓

**Render direction vector** (proven equivalent, §5) in world axes (E, up, S):
```
east  =  cos δ · sin H
up    =  cos φ · cos δ · cos H  +  sin φ · sin δ
south =  sin φ · cos δ · cos H  −  cos φ · sin δ
```

### 4d. Hour angle from the vanilla clock

`H = 2π · (timeOfDayFrac − 0.5) + H_offset`, where `timeOfDayFrac = (getOverworldClockTime() mod 24000) /
24000` and `H_offset` is a **one-line calibration constant** set so that the mod's H = 0 lands on the same
instant as vanilla's noon-zenith. (Calibrated live: at the equator with δ = 0 the mod sun must sit exactly
where vanilla puts it — see §5 regression guard.) For the render we can also just **reuse vanilla's
`sunAngle`** as H directly and only add the tilt + δ (cheaper, guarantees phase parity); the functional
layer computes H from `getOverworldClockTime` so server and client agree without reading render state.

### 4e. Seasons — δ(day) (owner addendum 2)

```
DELTA_MAX_DEG      = 30.0     // the axial tilt amplitude — see §11 "delta pick"
YEAR_LENGTH_DAYS   = 360.0    // default; 180 game-days between solstices (owner's schedule)
day    = getOverworldClockTime() / 24000.0            // continuous (double) so δ drifts smoothly
δ(day) = DELTA_MAX_DEG · cos( 2π · day / YEAR_LENGTH_DAYS )
```
- **Convention (state it):** **world-day 0 = northern summer solstice** — `cos(0) = 1` ⇒ δ = +DELTA_MAX ⇒
  north pole midnight sun, south pole polar night. `day 90` = autumn equinox (δ=0), `day 180` = northern
  *winter* solstice (δ = −DELTA_MAX, poles swapped), `day 270` = spring equinox, `day 360` = back to
  northern summer. (Cosine chosen so day-0 = solstice = the visually strongest state; a sine convention
  would put day-0 at an equinox — we prefer opening on drama and it makes the frozen default = full tilt.)
- **Frozen mode:** `YEAR_LENGTH_DAYS <= 0` ⇒ δ = `DELTA_MAX · cos(FROZEN_PHASE)`, a constant. Default
  `FROZEN_PHASE = 0` ⇒ δ = +DELTA_MAX ⇒ **permanent northern summer** = the owner's original
  "one pole summer forever." Frozen mode with a random `FROZEN_PHASE` sign is where seed-random "which pole
  is summer" lives (§9).

---

## 5. Render approach — composing the tilt into 26.2's sky

**Injection point:** mixin on `SkyRenderer`, `@Inject`/`@WrapOperation` around
`renderSunMoonAndStars(PoseStack, …)` (§3a). Gated: `LatitudeV2Flags.SOLAR_TILT_ENABLED` &&
`GlobeClientState.isGlobeWorld()` && overworld; no-op otherwise (seam-free, like the fog mixin).

**Two composed rotations (this is the whole geometry):**

1. **Latitude axis-tilt — ONE rotation, shared by sun+moon+stars.** At `@At("HEAD")` of
   `renderSunMoonAndStars`, before vanilla's `YP(-90)`, `poseStack.mulPose(Axis.XP.rotationDegrees(φ))`
   (rotation about the world **east-west** axis by the signed latitude). Proven: pre-multiplying the whole
   vanilla celestial sphere by a fixed world-X rotation **tips the revolution axis** from horizontal
   (equator) up to altitude φ (the celestial pole rises to latitude φ). Sun, moon and stars all tilt as one
   sphere — correct. *(Sign/axis of XP vs ZP verified in the live calibration pass; the algebra says
   world-X = the east-west tilt axis.)*

2. **Declination δ — a SECOND fixed rotation, per body.** The axis-tilt alone keeps the sun on the
   celestial *equator* (δ = 0): that gives lower arcs but the sun would only skim the horizon exactly at
   90°, never a *summer* midnight sun before the pole and never a true polar night. The declination is what
   makes the two poles differ — so it is **non-optional** for the owner's ask. It cannot be folded into the
   shared pre-tilt: the δ≠0 daily circle is a **different-radius** small circle than vanilla's equatorial
   great circle, and no single global rotation maps one onto the other. It is injected as a fixed **−δ**
   rotation in each body's local frame (the sun rests at local +Y per `renderSun`'s `translate(0,100,0)`,
   so δ is a small rotation about the sun's local **Z** axis before it draws).

**Proof the composition is exact.** Target world direction `s = Rx_EW(φ) · w`, where
`w = (cos δ · sin H, cos δ · cos H, −sin δ)` is the vanilla revolution acting on a base pre-tilted by −δ:
`w = R_worldZ(H) · Rx_EW(−δ) · ŷ`. Expanding `Rx_EW(φ) · w` reproduces the §4c (east, up, south) vector
term-for-term. Since vanilla's `YP(-90)·XP(H)` **is** `R_worldZ(H)` acting on ŷ (bytecode §3a; sun rests at
+Y), the only additions are the outer `Rx_EW(φ)` (rotation 1) and the inner `Rx_EW(−δ)` (rotation 2). ∎

**Implementation choice (the #1 technical risk — call it out):** rotation 2 is the fiddly part. Two ways:
- **(A) minimal-nudge:** shared `Rx_EW(φ)` at HEAD + a fixed `−δ` pre-rotation wrapped into each of
  `renderSun/renderMoon/renderStars`. Smallest diff; must get the local axis right against the +Y quad.
- **(B) direction rebuild:** ignore vanilla's per-body `XP(angle)` and build each body's pose straight from
  the §4c unit vector via a `@WrapOperation` on the body `mulPose`. Unambiguous, trivially unit-testable
  against the vector, immune to axis-guessing — **recommended** if (A)'s calibration fights the pipeline.

**Regression guard (non-negotiable):** at the **equator with δ = 0** the composed pose must reproduce
vanilla **bit-for-bit** (sun at zenith at noon, identical arc). That single screenshot-diff is the seam
proof — same discipline as "Classic byte-identical" worldgen.

**Moon & stars (keep simple v1):** both share rotation 1 (the whole sphere tilts — a circumpolar star wheel
over a tilted pole is *the* polar-night payoff, essentially free). The **moon** gets the same tilt and rides
the opposite side of the sky as vanilla; its **phase stays vanilla** (`MoonPhase` untouched) — no
opposite-phase-per-pole cleverness in v1. Stars get rotation 1; whether stars also take the δ offset is
cosmetically irrelevant (they're a full-sphere field) — skip it, tilt only.

**Deferred-pipeline note:** SkyRenderer builds `GpuBuffer`s for the star/sun/moon geometry, but the
**orientation is still a live per-frame `PoseStack`** passed to the render methods — so composition works.
The scalar `SkyRenderState.sunAngle/moonAngle/starAngle` route is a **dead end** for us (scalars can't carry
a tilt/declination); the `PoseStack` is the only surface. Perf: **one extra quaternion multiply per body per
sky frame** (three total) — negligible.

---

## 6. Brightness & colour — polar-night gloom + midnight-sun gold (client, no light engine)

**Hard rule:** no light-engine touch. The player's *block* brightness (what `getMaxLocalRawBrightness`
returns, what makes torches necessary) stays on the global clock. We only repaint the **sky pass**. Concrete
26.2 render-state hooks (all client, all presentation):

- **`SkyRenderState.skyColor` / `sunriseAndSunsetColor`** and the already-hooked **`FogRenderer` color**
  (`FogRendererPolarSetupMixin`) — push toward a **deep blue-grey night wash** in the polar-night band even
  when the global clock says day. This darkens the *sky and horizon*, not the ground.
- **`SkyRenderState.starBrightness`** — lift toward 1.0 in polar night so **stars show at "noon."** The
  single most convincing polar-night cue and it costs nothing (it's a float the sky pass already reads).
- **`SkyRenderState.rainBrightness`** — already driven indirectly by the storm mixin at 85°+.
- **Midnight-sun gold:** in the summer band **below the 85° storm takeover**, tint the fog/sky colour warm
  (low golden-hour palette) and **keep the sun visible** (do *not* let it grey out) — the low circling sun
  is the whole point.

**No "sky gamma multiplier" that dodges the light engine exists** cleanly — the honest set is
`skyColor` + `starBrightness` + fog colour + the existing `PolarWhiteoutOverlayHud`. So: the polar-night
**sky** goes dark, but **blocks stay lit per the global clock**. That is the accepted, documented seam
(§10) — it reads as "eerie bright-dark twilight," which is fine.

**Composition with storm/whiteout (position — recommend BOTH poles keep storm grey at 85°+):** the sun's
personality is the **approach (≈40–85°)**. From 85° the storm sky (`ClientLevelStormSkyMixin`) greys
everything and by ~87.5° the sun is gone; the fog law crushes visibility to ~4 blocks by 90°. **Let the storm
win in the deep cap** — it already reads as "the wall," and fighting it would mean un-greying a sky the pole
feature deliberately greyed. So: **summer approach = golden low sun → dissolves into storm-grey/whiteout cap;
winter approach = dim low sun → polar-night gloom → same storm-grey/whiteout cap.** The tilt keeps running
underneath (cheap), just visually dominated past ~85°. This keeps the sun feature and the pole feature from
stepping on each other.

---

## 7. No-netcode receipt (why seasons + effective-sun cost ~nothing)

Both the client (sky) and the server (spawns/burn) compute **δ(day)** and the **effective sun** from the
**same vanilla-synced clock**, `Level.getOverworldClockTime()` (§3c). MC already replicates the day-time to
every client every tick — that's how the vanilla sun/clock stay in sync. So:
- δ(day) is a **pure function of the shared clock** → client and server get **identical** values with **zero
  new packets**. Seasons are "free" beyond the fixed tilt: one `cos` per frame/tick.
- **Sleeping / time-skip advances seasons with the clock** — a heavy-sleeper server ages the *year* faster
  (a night's sleep = ~½ a game-day = a chunk of the season). **Documented seam, acceptable** — it's the same
  clock everything else already trusts; if anything it makes seasons feel connected to play.

---

## 8. The "effective sun" functional layer — ONE evaluator, two consumers (owner addendum 1)

A single pure predicate in `SolarTilt`, read by **both** the sky visuals **and** the server mob rules — so
they can never disagree:
```
// pure, core, no MC imports
double solarElevationDeg(double signedLatDeg, double dayCount, double timeOfDayFrac)   // §4c
boolean effectiveSunUp(double signedLatDeg, double dayCount, double todFrac)
      = solarElevationDeg(...) > SUN_UP_THRESHOLD_DEG;     // recommend −0.833° (disc-edge) ; 0 is fine too
```
"Effective sun exposure" for the mob rules = `effectiveSunUp(...) && skyExposedAtPos`. The `skyExposed`
term (via `Level.canSeeSky(pos)` server-side / `GlobeClientState.exposure01` client-side) is what keeps
**caves out of it** — a cave column is never sky-lit, so both rules below are inert underground.

### 8a. Winter-pole polar-night band — monsters around the clock
- **Spawns:** mixin `Monster.isDarkEnoughToSpawn` (§3c). When `!effectiveSunUp` **and** the position is
  sky-exposed **and** `|φ| > 90−|δ|` on the winter side → treat as **dark enough** (bypass the sky-light
  term; **still respect BLOCK light** so a torch-lit polar base stays safe). Net: the polar-night surface
  spawns hostiles at global noon.
- **Sun-burn:** mixin `Mob.isSunBurnTick` → force **false** in this band (no sun overhead to burn undead) —
  so zombies/skeletons roam the twilight without igniting.

### 8b. Summer-pole midnight-sun band — surface stays safe
- **Spawns:** same `isDarkEnoughToSpawn` mixin — when `effectiveSunUp` **and** sky-exposed **and** in the
  midnight-sun band → return **not dark** (veto surface monster spawns even at global midnight). **Caves
  unaffected** (not sky-exposed). Net: the endless-daylight snow stays clear; the dungeon-crawl doesn't.
- **Sun-burn (position — recommend YES):** force `isSunBurnTick` **true** for sky-exposed undead under the
  visible midnight sun. Cheap, and it's the consistent read ("the sun is up, so it burns"). Recommend
  include.

### 8c. Bed refusal (optional flavor) — **recommend EXCLUDE v1**
Intercept `Player.startSleepInBed` and, in the midnight-sun band, return
`Either.left(BedSleepingProblem.OTHER_PROBLEM)` with a custom "The sun never sets here." `Component`
(§3c). Nice touch, but it's extra player-facing surface and a minor annoyance (can't skip the endless day).
**Exclude from v1**, ship behind a later `latitude.solarTilt.midnightSunNoSleep` toggle if the owner wants
it.

### 8d. Band boundaries migrate (seasons) — no hysteresis, no cleanup (addendum 2 pt 3)
The evaluator is **stateless per check** — it reads today's δ(day) each time. As the seasons move the bands
day to day: a mob that spawned in yesterday's polar-night spot which is *outside* the band today simply
**isn't re-spawnable** there; it lives out its normal vanilla lifetime/despawn. Nothing to migrate, no
hysteresis band needed, no per-mob "born in polar night" flag. Near the equinoxes the bands shrink to
nothing (δ→0 ⇒ onset→90°) and both rules quietly go inert everywhere but the exact poles — correct and
self-cleaning. **State it: zero persistent mob state.**

---

## 9. Seed-random summer pole / world-birth season phase (§4e tie-in)

The base brief's "seed-random which pole is summer" is **absorbed by seasons**: it becomes
`worldBirthSeasonPhase` — a per-world offset added to `day` before δ(day), so different worlds open on
different points in the year (and, in **frozen** mode, on different δ signs = different summer poles).

**Recommendation for v1:** ship a **deterministic calendar** — world-day 0 = northern summer solstice, no
world state, fully testable. Add `worldBirthSeasonPhase` as a **fast-follow**, persisted in
`LatitudeWorldState` exactly like `globeRadius` (`world/LatitudeWorldState.java`, SavedData codec, L42/96) —
either a fixed config or a seed-derived random offset stamped once at world birth. In **frozen** mode
(`yearLengthDays <= 0`), a seed-random *sign* on `FROZEN_PHASE` is the clean home for "this world's north is
the summer pole / that world's south is" — the owner's original idea, now a special case of one field.

---

## 10. Honest seams — write it for the player, and why each is small

1. **The functional clock is unchanged.** Crop growth, farmland, phantoms, raids, villager schedules, the F3
   clock, the clock *item* — all still one global `dayTime`. We changed the sun's *look* and two *mob rules*,
   not time. Seam is small because the affected bands (polar night / midnight sun) are **already
   fog-crushed, frostbitten, hostile country** you don't farm in.
2. **Block light stays global.** In polar night the *sky* goes dark but your torches, and the light level on
   the ground, are the same as global day — so mobs spawn (our rule) but you can still *see* to fight them,
   and your base stays lit. Reads as "eerie perpetual twilight," not a bug. (Deliberate: no light-engine
   work, §2.)
3. **Beds stay vanilla** (outside the optional §8c flavor).
4. **The moon** takes the tilt but keeps its vanilla phase; **stars** wheel around the tilted pole (a feature,
   not a seam).
5. **Multiplayer is free.** Two players at different latitudes see different suns because the tilt is a pure
   **client render** keyed on each client's own latitude — no server sun state, works out of the box. The
   *functional* layer is server-side and position-keyed, so it's consistent for everyone.
6. **F3 vs the visual.** F3 shows one global time; the *visible* sun height won't match "noon = overhead"
   anymore away from the equator. That's the whole point; document it so it doesn't read as desync.

---

## 11. Considered & rejected (with receipts) — and the delta pick

**Why time-changing mods (Custom-Time-Cycle style) are the wrong tool (one honest paragraph, per addendum).**
Minecraft keeps **one global `dayTime` per dimension** (`Level.getOverworldClockTime()`, §3c) — there is no
per-region or per-column clock. Advancing/retarding that clock to fake a low sun **shifts the whole world's
time at once, including the equator**, and desyncs everything time-driven (villager sleep, phantom timers,
raids, the clock item, moon phase). It changes *when* it is everywhere; it cannot change *where the sun is in
the sky* as a function of latitude. Rejected.

**Per-CLIENT time-offset trick — considered, rejected (allowed as hybrid only if the matrix path failed; it
didn't).** Offsetting a client's *perceived* `dayTime` by latitude to reuse vanilla's own sky/brightness
rendering. Flaw: it only slides the sun **along its existing arc** (changes the **hour angle**), it does
**not tilt the arc** — the sun still climbs to the same peak height (zenith) at some moment, so you get **no
lower arc, no midnight sun, no polar night**, only a phase shift. It also desyncs every client time-driven
effect (moon phase, sky-colour transitions, the clock item) from the server. The **tilt-matrix path (§5) is
both correct and cleanly injectable in 26.2** (recon confirmed the public `renderSunMoonAndStars(PoseStack)`
seam), so no hybrid is needed. Rejected.

**The delta pick.** Real Earth axial tilt = **23.5°** (arctic circle at 66.5°). At Minecraft render scale a
23.5° swing is real but subtle, and it puts the midnight-sun/polar-night onset at 66.5° — deep in the
storm-greyed cap where you can barely see the sky. To make the drama **read in the visible approach band**,
pick **DELTA_MAX = 30.0°** (shipped default):
- Onset of midnight-sun / polar-night at **exactly 60°** ("above 60° the sun stops behaving") — a memorable,
  round threshold that sits in the **visible** part of the map (well equatorward of the 85° storm wall).
- Equator solstice noon = 60° elevation (a 30°-off-zenith seasonal swing even at home — noticeable, not
  absurd).
- Clean, dramatic, not cartoonish.
- **Live-tuning dial:** `latitude.solarTilt.deltaMaxDeg` (default 30; real-world purists → 23.5; max
  theatrical ≈ 35). One number, hot enough to tune on the owner's keyboard.

---

## 12. Proof plan

**Pure-math unit tests (`SolarTiltTest`, plain JVM — the ground truth):**
- **Elevation table by latitude** at solstice (δ=+30) and equinox (δ=0), noon (H=0) and midnight (H=π):

  | φ (signed) | δ=+30 noon | δ=+30 midnight | δ=0 noon | reads as (solstice) |
  |---|---|---|---|---|
  | 0° (equator) | 60° | −60° | 90° | sun 30° off zenith |
  | +30° | 90° | −30° | 60° | zenith at noon (φ=δ) |
  | +40° | 80° | −20° | 50° | high summer arc |
  | +60° | 60° | **0°** | 30° | midnight sun onset |
  | +75° | 45° | +15° | 15° | **midnight sun** (never sets) |
  | +90° (N pole) | 30° | +30° | 0° | **midnight sun**, circling at 30° |
  | −60° | 0° | −60° | 30° | polar-night onset (sun grazes horizon at noon) |
  | −75° | −15° | −75° | 15° | **polar night** (never rises) |
  | −90° (S pole) | −30° | −90° | 0° | **polar night** |

  (Assert each cell to ±0.01°; these rows *are* the spec.)
- **Onset thresholds:** assert midnight-sun/polar-night boundary = `90 − |δ|` for δ ∈ {0, 15, 23.5, 30}.
- **δ(day) table:** day 0 → +30 (N summer solstice); day 90 → 0 (equinox); day 180 → −30 (S summer
  solstice); day 270 → 0; day 360 → +30. Quarter points day 45 → +21.2, day 135 → −21.2.
- **Band-migration math:** polar-night extent (winter-side latitudes with noon elevation < 0) at day 0 (all
  φ < −60 in the south), day 90 (none), day 180 (all φ > +60 in the north) — proves the bands swap poles and
  vanish at the equinox.
- **Frozen-mode equivalence:** `yearLengthDays = 0, FROZEN_PHASE = 0` reproduces the fixed δ=+30 table
  bit-for-bit (the "permanent summer pole" special case).
- **Direction-vector ↔ elevation consistency:** `asin(up-component) == solarElevationDeg` for a grid of
  (φ, δ, H).
- **Effective-sun predicate:** `effectiveSunUp` flips exactly at the elevation threshold; `skyExposed=false`
  makes both mob rules inert regardless of latitude.

**Render / live screenshot checklist (the calibration + look proof):**
1. **Equator, δ=0, noon** — sun at zenith, **pixel-identical to vanilla** (regression guard, §5).
2. **~60° latitude, clear** — visibly lower, flatter noon arc; long shadows.
3. **Summer pole (midnight sun)** — sun circling low around the horizon at global midnight; golden tint;
   below the 85° storm takeover.
4. **Winter pole (polar night)** — no sun at global noon; blue-grey gloom; **stars visible**; then storm-grey.
5. **Equinox anywhere** — near-vanilla day/night everywhere (bands gone).
6. **Functional:** at the winter pole at global noon, mobs spawn on exposed surface and undead don't burn; at
   the summer pole at global midnight, exposed surface stays mob-free but a nearby cave still spawns.

**Perf:** one `cos` per tick for δ(day); one quaternion multiply per body per sky frame (×3); the evaluator
is a few trig ops on already-rate-limited spawn attempts and per-undead sunburn ticks. No per-tick world
scans, no new packets. Expect **unmeasurable** overhead — sanity-check with the existing Spark capture path.

---

## 13. Flag & config summary

All in `core/LatitudeV2Flags.java` (System-property pattern, §3d), default OFF:
- `latitude.solarTiltV2.enabled` = **false** (the one master kill-switch: visual + functional + seasons).
- `latitude.solarTilt.deltaMaxDeg` = 30.0 (axial tilt amplitude; §11).
- `latitude.solarTilt.yearLengthDays` = 360 (0/negative ⇒ frozen; §4e).
- `latitude.solarTilt.frozenPhaseDeg` = 0 (frozen-mode phase = which pole is summer; §9).
- `latitude.solarTilt.midnightSunNoSleep` = false (optional bed refusal; §8c).
- (fast-follow) `worldBirthSeasonPhase` persisted in `LatitudeWorldState` (§9).

---

## 14. Open owner dials (decide before / during P3)

- **deltaMax:** 30° (recommended) vs 23.5° (real) vs ~35° (max theatrical).
- **yearLength:** 360 (recommended) vs frozen (`0` = permanent summer pole, your original ask).
- **Bed refusal in midnight sun:** include vs exclude (recommended exclude v1).
- **Seed-random birth phase:** deterministic v1 (recommended) vs seed-random at birth.
- **Summer-pole undead burn:** yes (recommended) vs no.
- **δ=0 calendar origin:** day-0 = northern-summer-solstice (recommended) vs day-0 = equinox.

---

## 15. Build sequence (suggested)

- **P1 (core):** `SolarTilt` + `SolarTiltTest` (all §12 math). No MC. Green suite. Nothing user-visible.
- **P2 (render):** `SkyRenderer` mixin (rotation 1 + rotation 2), fog/sky colour + star-brightness gloom,
  storm-composition gate at 85°. Regression-guard screenshot (§12.1) is the gate.
- **P3a (functional):** `Monster.isDarkEnoughToSpawn` + `Mob.isSunBurnTick` mixins reading the shared
  evaluator. Live proof §12.6.
- **P3b (seasons):** δ(day) from `getOverworldClockTime`; band-migration live check across a fast-forwarded
  year.
- **P4 (owner live):** dial `deltaMaxDeg` / `yearLengthDays` on the keyboard; pick defaults; flip default-off
  decision.

Worldgen is **untouched** (byte-identical) throughout — this is presentation + two mob rules only.


## ADVERSARIAL DESIGN SWEEP — APPROVE-WITH-AMENDMENTS (2026-07-16, BINDING)
All 26.2 receipts verified signature-exact (javap vs the loom jars) — the cited hooks are real,
including Level.getOverworldClockTime() surviving the WorldClock rework. AMENDMENTS:
A1 (HIGH) The §12 spec table has TWO WRONG CELLS contradicting §4c's own formula: phi=-75 delta=+30
midnight = -45 (not -75); phi=-90 midnight = -30 (not -90 — a pole's elevation is CONSTANT).
Recompute the whole table from §4c before any test is written; add the invariant: at |phi|=90,
elevation == +/-delta for ALL hour angles. (§4b also carries a drafting artifact to strip.)
A2 (HIGH) FUNCTIONAL BAND NARROWER THAN VISUAL — RULED YES: new dial
latitude.solarTilt.functionalMinDeg default 74.5 (the extreme-cap no-villages line) gates BOTH the
spawn override and the burn rules; visuals keep the 60-deg onset. Receipts: SUBPOLAR 50-66.5 /
POLAR 66.5-90; villages vetoed only >= 74.5; at 60-74.5 the winter dark weeks would siege livable
country (a 63-deg village under 24/7 spawns for weeks). Owner may widen the dial after flying.
A3 (MED) renderSunriseAndSunset glow band: v1 SUPPRESSES the vanilla horizon glow poleward of the
visual onset (it would paint dawn-gold at the wrong compass point and fire at vanilla dusk during
polar night). Rotating it with the tilt = future polish.
A4 (MED) NO carve-out from the S10 early-overcast ramp — bands coexist: payoff zone 40-80 clear,
81->85 progressively grey (owner-ordered), storm wins >= 85. Gold tint via fog/sky color only;
never un-grey rainLevel.
A5 (LOW-MED) Build crew verifies ClientLevel's synced day-time jitter (<= 1s -> delta drift
negligible -> zero-netcode holds); seam notes: /time set backward rewinds the season;
doDaylightCycle=false freezes it.
A6 (LOW) Rotation option B (direction-vector pose rebuild via @WrapOperation on the per-body
mulPose) is PRIMARY: per-body targets are clean in the bytecode and B is headless-testable
(compose == vanilla at phi=0, delta=0 in plain JVM); the equator screenshot-diff stays the live P2
gate (the atlas cannot render sky).
