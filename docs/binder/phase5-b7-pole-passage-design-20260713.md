# Phase 5 B-7 — Pole Passage: design (2026-07-13)

`status: DESIGN — awaiting adversarial sweep; NO feature code written`
Worktree `Latitude-b5-hemisphere-passage`, branch `phase5-b7-pole-passage` off `port/canonical-26.2-pivot`
HEAD `f5539e35`. Recon (this session) verified every claim below against the CURRENT pivot code;
file:line pointers are from that recon (pointer table at the bottom).

## Owner decisions this design is bound by (Peetsa, 2026-07-13)

- **B-6 evator is ROLLED BACK / PARKED** after its maiden voyage: the B-5 UX tightening (fog onset
  2.5°) left the 175–177.5° mirror band standing naked in clear air, plus a confusing ocean arrival.
  Nothing in B-7 may resurrect mirror-band worldgen or decoration strips.
- **"B-5 gameplay was good enough… B-7 for the poles to have B-5 gameplay."** B-7 = the PROVEN B-5
  ceremony at the N/S poles: advisory → fog → consensual prompt → curtain teleport → arrival title.
  NOT the silent evator.
- **Peetsa's standing B-7 rule:** "teleport onto land if traveling by land, water if traveling by
  water." (§6 resolves this against the polar water-freeze law he also shipped.)
- The polar warning ladder is Peetsa's own copy; DANGER (89°) is verbatim, DO NOT TOUCH (comment at
  `GlobeWarningOverlay.java:29`). §7 flags the honesty tension and offers OPTIONS only.

## 1. Concept

Walking poleward, the existing polar experience (snow 85° → blizzard 87° → slowness/frost 87.5° →
freeze damage 88° → whiteout deepening to a lethal 90°) IS the approach ceremony — B-7 adds no new
fog and no new worldgen. Deep inside the hazard band a one-shot two-button prompt offers the
crossing; "Pass through" raises the proven B-5 curtain, the server mirror-teleports the player to
the FAR MERIDIAN on the same side of the world (over the pole: longitude flips 180°, latitude stays
polar, heading reverses), and the curtain melts onto a survivable arrival line just outside the
freeze-damage band — emerging from the whiteout heading equatorward, hearts un-blueing as you walk.
"Turn back" = the B-5 one-shot gentle nudge, aimed equatorward. Consensual, prompt-gated, honest.

**B-7's structural advantage (state this loudly): pure presentation + teleport.** Worldgen is
untouched — byte-identical with the flag on OR off, no per-world capture (the B-6 requirement that
forced fresh worlds), no atlas gate, works on EVERY existing world including Peetsa's live ones. The
proof surface is the unit suite + P3 live feel only.

## 2. Flag

`latitude.polePassageV2.enabled`, **default OFF** (`LatitudeV2Flags` idiom, `core/LatitudeV2Flags.java:156`).
Born WITH its `build.gradle` client-run forwarding line in the SAME pass (L17 discipline — the
forwarding block is `build.gradle:102-114`; `passageV2`'s line is L114; three prior bites documented at
`LatitudeV2Flags.java:154,174`). Flag-off: the pole prompt/teleport/curtain/title/nudge/`axis=POLE`
netcode/Wide hard-stop clamp never exist — byte-identical FOR THE CROSSING SURFACE. [F2 honesty rescope,
P1 sweep 2026-07-13: the original line here claimed the whole "polar hazard/warning experience" was
byte-identical flag-off — no longer true once the S3/S4/S6 stipulations landed. The S3 cold pacing
(frostbite band 85–88 + its F3 frost cue, ambient onset 85 → 82), the S4 shelter pause, and the S6
frozen-wounds heal lock are GLOBAL, deliberately un-gated rebalances that ship with this pass regardless
of the flag.] NOT tied to `latitude.passageV2.enabled`
(EW), but P1 should share the server receiver — see §5 netcode. Default-flip is Peetsa's post-P3 call,
same as B-5's history (`LatitudeV2Flags.java:157`).

## 3. Geometry — the Z-edge sibling of EdgeGeometry

### 3.1 Verified sign conventions and primitives

- **North = negative Z**: `LatitudeMath.hemisphere` returns 'N' iff `z < centerZ`
  (`util/LatitudeMath.java:197-200`); the N/S hemisphere title uses side −1 = "Northern Hemisphere"
  (`GlobeWarningOverlay.java:630-633`). VERIFIED.
- Latitude: `|z| / latitudeRadius × 90°` (`LatitudeMath.java:110-119`); the pole LINE is
  `|z| = latitudeRadius` — synced to the client as `GlobeStatePayload.latitudeZRadius`
  (`GlobeNet.java:36-52`, `LatitudeMath.setLatitudeZRadius` L62), so pole geometry is **already
  lerp-immune** the way B-5's intended-X-radius redesign made the EW lines (no new handshake field
  needed — B-7 gets the TEST-86 protection for free).
- Distance-to-pole exists: `LatitudeMath.poleRemainingBlocks(border, z) = latitudeRadius − |z|`
  (L134-138). It ignores `centerZ`; recommend the new geometry class use
  `distanceToPole(zRadius, centerZ, z) = max(0, zRadius − |z − centerZ|)` for hygiene/symmetry with
  `EdgeGeometry.distanceToEdge` (`core/EdgeGeometry.java:187-189`) — identical on real worlds
  (globe borders are centered 0,0, `HemispherePassageService.java` reads `border.getCenterX()`).
- Blocks per latitude degree = `zRadius / 90` — 41.7 (Itty 3750), 83.3 (Small 7500), 111.1
  (Regular 10000). Note: on Wide worlds this EQUALS the EW blocks-per-longitude-degree
  (`xRadius/180` with aspect 2.0), so both ceremonies share block-scale on Wide; on Classic the polar
  degrees are 2× coarser than EW degrees. Precedent for degree-first Z sizing:
  `HemisphereCrossing.deadZoneBlocks(latitudeRadius/90)` (`GlobeWarningOverlay.java:616-617`).
- **The pole line is NOT the vanilla border on Wide worlds.** The square border is sized to the X
  radius (`EdgeGeometry.java:14-17`), so on Wide, `|z|` can physically exceed `zRadius` — there is no
  wall at 90°, only clamped max-lethality forever (`hazardProgressZ` clamps at 1.0,
  `LatitudeMath.java:157-161`). Today the Wide pole is an unmarked, endless 6-HP/s death plain; B-7
  gives it an ending. (This also connects to the parked "pole hard-stop" decision in the roadmap
  queue.) On Classic the vanilla wall does sit at the pole line. The geometry below works for both;
  `distanceToPole` clamps at 0 beyond the line, so a beyond-the-line survivor can still be prompted.

### 3.2 `PoleGeometry` (new pure core class, sibling of `EdgeGeometry`)

Anchors in LATITUDE degrees (pole = 90), resolved per world from the Z radius with the same
floor + strict-ordering discipline as `EdgeGeometry.resolve` (`EdgeGeometry.java:219-251`).
The polar ladder constants (85/87/89/89.7, `LatitudeMath.java:42-45`) and every `PolarHazardWindow`
onset (85/87/87.5/88, `core/PolarHazardWindow.java:46,146-158,193,331`) are KEEP-SHARED and **do not
move**; B-7's lines interleave between them:

| anchor | deg | dist-from-pole (deg) | Itty 41.7 b/° | Small 83.3 b/° | Regular 111.1 b/° | rationale |
| --- | --- | --- | --- | --- | --- | --- |
| (existing ladder WARN_1/2, DANGER, LETHAL) | 85 / 87 / 89 / 89.7 | — | — | — | — | untouched, the "advisory" is already built (§7) |
| `REARM_DEG_POLE` | 88.5 | 1.5 | 62.5→**floored 104** | 125→**floored 130.7** | 166.7 | strict walk-out re-arm; floors: `prompt + DEAD_ZONE 64` then `+ ORDER_STEP` (same `EdgeGeometry.java:157-168` constants) |
| `PROMPT_DEG_POLE` | **89.2 (recommended, §4)** | 0.8 | 33.3→**floored 40** | 66.7 | 88.9 | past the 89° DANGER rung so the ladder line lands FIRST, then the counter-offer; `PROMPT_MIN_DIST_BLOCKS 40` floor |
| `ARRIVAL_DEG_POLE` | ~~88.0~~ **89.5 (S5, supersedes)** | 0.5 | 20.8 | 41.7 | 55.6 | [S5 2026-07-13 — the binding tail section supersedes this row's original 88.0 rationale] deep-ceremony arrival: emerge INTO the far blizzard (~2.27 HP/s unprotected) and trek OUT; the only reprieve is the ~45-tick post-crossing cold grace (the curtain window); the frozen-hearts thaw happens during the escape at ~88°, not at touchdown |
| `EDGE_REPROMPT_DEG_POLE` | 89.9 | 0.1 | floored 8 | 8.3 | 11.1 | the SEEDED walk-to-the-wall auto-re-prompt sibling (`EdgeGeometry.java:151`), floor 8 blocks |

- **No FOG anchors.** The polar whiteout/depth-fog already ramps (ambient onset)→90
  (`PolarHazardWindow.java:253-303`, `FogRendererPolarSetupMixin`) — it IS the ceremony fog, free.
  B-7 must NOT add a `FogRendererPassageSetupMixin` sibling; the composition question B-5 solved
  (both fogs min-compose at a corner, `FogRendererPassageSetupMixin.java:40-42`) stays solved by
  adding nothing. (S3 later moved the ambient onset 85 → 82 — a global cold-pacing change, not a
  B-7 fog anchor.)
- Ordering chain (dist-from-pole) **[S5 REVISED]**: `edgeReprompt < arrival < prompt < rearm <
  (ladder, fixed)` — with floors respected on every size (Itty: 8 < 20.8 < 40 < 104). The original
  paragraph here (arrival 2.0° equatorward of the prompt, the Itty-vs-Regular two-regime split) is
  SUPERSEDED by the S5 tail: arrival (0.5°) is now INSIDE prompt AND rearm on EVERY size, so the
  machine runs ONE uniform SEEDED_DISARMED regime everywhere; no-self-prompt at the arrival column
  is guaranteed by the disarmed SEED (state law, `HemispherePassage.java:32-38`), not geometry, and
  the "prompt < arrival" assertChain invariant is retired for the pole axis.

### 3.3 The crossing transform (over-the-pole continuity)

**[P3 fix 2026-07-14: antipodal meridian, not mirrorX — owner-observed on the TEST 97 maiden
flight.]** The original transform below claimed `mirrorX` "flips longitude 180°" — FALSE: `mirrorX`
(`x → −x`) flips the longitude SIGN (`L → −L`, the EAST/WEST antimeridian formula), so Peetsa
crossed at x≈+530 (13°E) and landed at x=−530 (13°W) instead of the antipodal 167°W (log 16:08:05).
Walking over a POLE lands you on the ANTIPODAL meridian: `L → L+180`, in blocks
`targetX = x − sign(x)·xRadius` (`PoleArrivalSearch.antipodalX`, sign(0)=+1 by convention → the
prime meridian maps to the WEST edge; ±180° are the same meridian). A near-prime-meridian departure
therefore targets the E/W BORDER CORNER — the A2 corner X-clamp already bounds the target and every
search candidate, pulling it inward of the whole EW ceremony band (verified path, service L259-264).
`mirrorX` stays EXACTLY as-is for the EW axis, where it is correct. Corrected transform, for a
player at `(x, z)` near the ±Z edge (center 0,0):

- `targetX = antipodalX(x, centerX, xRadius) = x − sign(x−centerX)·xRadius` — longitude flips 180°
  (the true antipodal/far meridian); E/W hemisphere still flips (|dx| → xRadius−|dx|, opposite side).
- `targetZ = sign(z − centerZ) × zForLatitudeDeg(ARRIVAL_DEG_POLE, zRadius)` — SAME pole side,
  pulled to the arrival line (`LatitudeMath.zForLatitudeDeg`, L189-195).
- **`arrivalYaw = playerYaw + 180°`** — walk north over the pole, emerge walking SOUTH on the far
  meridian. Sphere continuity: a straight line over the pole reverses the heading; the antipodal
  re-entry from the pole realises exactly yaw+180. This is a REAL DELTA vs B-5, whose teleport keeps
  yaw (`HemispherePassageService.java:162-163` passes `player.getYRot()` through) — correct for the
  EW mirror (an eastbound crosser keeps walking east), wrong for the pole. Pitch kept; momentum
  zeroed as today (L164-165).
- Hemisphere-title side effects: X flips → the E/W debounce sees a big step; `MAX_STEP_BLOCKS` 256
  re-seeds without firing (`core/HemisphereCrossing.java:51`) — already exercised live by every B-5
  crossing. Z barely changes → N/S title state untouched. No new title-machine work.

## 4. The lethal-pole interplay — prompt placement, survival math, curtain cost

### 4.1 The damage curve (server, recon-verified)

Freeze damage is the mod's own latitude-scaled curve, applied every `worldTime % interval` tick in
`borderUxTick` (`GlobeMod.java:493-503`), interval 60t→10t and amount 1.0→3.0 HP over 88°→90°
(`PolarHazardWindow.java:146-188`); vanilla's fixed freeze auto-damage is suppressed in-band
(`LivingEntityFreezeDamageMixin.java:50-68`). Facts that shape the design:

- **Armor is irrelevant**: `minecraft:freeze` is in `bypasses_armor` — "iron armor" buys nothing.
  Leather ALSO buys nothing here (the mod's curve never consults `canFreeze()`; `ticksFrozen` is set
  unconditionally, `GlobeMod.java:489-491`). What DOES mitigate: Protection enchants (EPF; Prot IV ×4
  ≈ −64%), Resistance, golden-apple absorption, and saturated natural regen (up to ~2 HP/s while
  hunger 20 + saturation lasts; ~1.5 saturation points per HP healed, so one golden carrot funds
  roughly 9–10 HP of healing). Slowness I/II/III at 87.5/88.33/89.17° (`PolarHazardWindow.java:50-76`)
  stretches every meter of the walk.
- DPS ladder (exact, Java `Math.round` semantics): 88.0° = 0.33 HP/s · 88.5° = 0.63 · 89.0° = 1.14 ·
  89.2° = 1.47 · 89.5° = 2.27 · 89.7° = 3.18 · 90.0° = 6.0.

### 4.2 Walking the curve (integrated; walk 4.317 / sprint 5.612 m/s × slowness tier; entry from the 88.0° damage onset to the prompt, +2 s prompt read, +~1.15 s curtain at origin latitude; arrival at 88.0° = zero exit damage)

| prompt option | Itty walk/sprint | Small walk/sprint | Regular walk/sprint | verdict |
| --- | --- | --- | --- | --- |
| (a) 89.5° — "deep dash" | 29.3 / 24.2 HP | 51.4 / 41.2 HP | 66.1 / 52.5 HP | **death sentence unprotected** on Small+ (1.7–3.3× a health bar raw); demands Prot IV (×0.36 → ~19 HP sprint on Regular) + food discipline. Drama tier only. |
| **(c) 89.2° — RECOMMENDED** | 17.2 / 14.3 HP | 29.8 / 24.0 HP | 38.2 / 30.5 HP | a REAL dash: unprotected sprint ≈ 1–1.5 bars raw, brought to survivable-but-scary by food regen (saturated regen offsets ~2 HP/s of the ≤1.5 HP/s band, i.e. net-positive until ~89.4° while saturation lasts) or any Protection. Iron-armor-plus-food player: makes it with margin on Small, tight on Regular, dies if they dawdle. Peetsa's "the crossing is the reward for surviving the dash," with numbers that reward rather than execute. |
| (b) 88.5° — early/safe | 5.3 / 6.3 HP | 8.6 / 10.6 HP | 10.8 / 13.4 HP | cheap, but only 0.5° into the bite — the dash barely exists; the prompt fires before the DANGER rung, muddling the ladder's story. |

Recommendation: **`PROMPT_DEG_POLE = 89.2`** — 0.2° past the DANGER rung so the sequence reads
DANGER ("Turn back.") → player pushes on anyway → the prompt answers with the choice. It also keeps
the prompt clear of the LETHAL rung (89.7) and its 1.15×-scale red line (`GlobeWarningOverlay.java:63-65`).
Round trip note: a decliner at 89.2° walks back out through the same band (~symmetric cost); the
"turn back" nudge (§5.3) gives the first shove. All numbers are flat-terrain lower bounds; deep
snow/powder snow and Slowness III make real runs worse — P3 calibrates feel, and the anchor is one
constant if Peetsa wants it moved.

### 4.3 Arrival at ~~88.0°~~ 89.5° [S5 SUPERSEDES THIS SECTION — see the binding tail], heading equatorward

**[S5 UPDATE 2026-07-13 — the escape-trek numbers.]** `ARRIVAL_DEG_POLE = 89.5` (was 88.0; the
original paragraph below is kept for the survival-math derivation only). The crosser lands at
~2.27 HP/s unprotected under Slowness III behind the arrival curtain; the ~45-tick post-crossing
cold GRACE (S5c, both damage bands + the F3 frost cue suppressed) covers the ceremony window, then
the blizzard owns them. Escape trek, flat-terrain lower bounds: **~2.2–2.6 HP/s unprotected through
89.5→89.0 under Slowness III, easing to 0 by 88.0** (the lethal curve's onset), **then the S3
frostbite gauntlet (1.0→0.25 HP/s) down to 85 — unless sheltered (S4 pauses damage) or protected
(S1 freeze-immune pieces scale it)**. The frozen-hearts thaw now happens mid-escape (~88°), not at
touchdown. With yaw+180 (§3.3) the player is already FACING equatorward. Arrival at 90° itself
(6 HP/s behind an opaque curtain) stays rejected.

<em>(Original 88.0 rationale, superseded:)</em> the exact `DAMAGE_ONSET_PROGRESS` line
(`PolarHazardWindow.java:146-149`): standing chip 0.33 HP/s, zero one step equatorward; hearts flip
from blue as you walk out; whiteout 0.6, blizzard drive 0.33 — "emerge from the thinning whiteout."

### 4.4 Does freeze damage pause during the ceremony? NO — and it's fine (with numbers)

Nothing pauses: the prompt screen is explicitly `isPauseScreen() == false`
(`HemispherePassagePromptScreen.java:122-125` — required so the integrated server can process the
answer), and the server damage cadence runs regardless of any client-side curtain
(`GlobeMod.java:497-503`; the curtain is pure client render, `HemispherePassageClient.java:36-42,247-354`).
Cost at the recommended 89.2° prompt: ~2 s of reading + ~0.3-1 s to teleport under the curtain
≈ **3–5 HP of ceremony tax**; the 850 ms fade-out then happens AT the arrival line (~0.3 HP). That is
acceptable drama — the cold literally gnaws while you decide — and it self-limits: the synchronous
3×3 FULL ring load stalls the server thread, and a stalled thread doesn't advance `worldTime`, so a
slow chunk load does NOT bill extra damage ticks. Verdict: **no grace period needed at 89.2**; note
for the sweeper that a one-boolean server-side "crossing in flight" damage skip is the cheap fallback
if P3 says the tax reads as unfair (do NOT build it preemptively). At an 89.5 prompt the same tax is
~7 HP — one more reason for 89.2.

## 5. State model, netcode, server

### 5.1 Client: a second instance of the SAME phase machine

`HemispherePassage.evaluatePhase` (`core/HemispherePassage.java:168-205`) is already a pure function
of `(phase, dist, canOpenPrompt, promptAt, rearmAt, edgeRepromptAt)` — it does not know which axis
feeds it. B-7 instantiates it a second time with pole distances. Concretely in
`HemispherePassageClient` (`client/HemispherePassageClient.java`):

- Extract the per-axis mutable state (a `phase` field + gesture edge-trackers) into a tiny private
  holder instantiated twice (`ewArm`, `poleArm`); the static singleton fields at L59-77 become two
  instances. The CURTAIN stays a singleton (one crossing at a time; both arms already skip evaluation
  while `curtain != NONE`, L138-142 — keep that shared gate for both).
- Pole distance: a `GlobeClientState.distanceToPoleBlocks(z)` sibling of
  `distanceToEwBorderBlocks` (`client/GlobeClientState.java:209-213`) reading the synced
  `latitudeRadius` — lerp-immune per §3.1.
- `canOpenPrompt` reuses the same surface-only + no-screen gate (`isDeepUnderground`,
  `GlobeClientState.java:417`; server twin `GlobeMod.java:626-632`) — under-ice caves at the pole
  must not prompt, same law as B-5's "underground there is only the wall."
- Degenerate niceness: on real sizes the arrival (2.0°) lands OUTSIDE `rearmAt` (1.5°), so
  `evaluatePhase` re-arms next tick and the machine lives in plain ARMED/DISARMED; SEEDED_DISARMED /
  EDGE_PROMPTED become reachable only on Itty (floors, §3.2) — reuse the full machine anyway, zero
  new core states, and the walk-back-to-the-wall auto-re-prompt still exists where it's meaningful.
- Re-prompt gesture sibling: `facingPolewardZ(playerZ, centerZ, yaw, minCos)` with
  `lookDirZ(yaw) = cos(toRadians(yaw))` (the Z counterpart of `lookDirX = −sin`,
  `HemispherePassage.java:246-263`); poleward = outward sign of `z − centerZ`. Same 60° cone, same
  shared rate limit, same `LatitudeConfig.borderRepromptGesture` toggle
  (`HemispherePassageClient.java:214-245`).
- `PassageDebug` gains an axis tag on every event (the flight-recorder discipline that solved B-5's
  re-arm mystery must be born WITH B-7, not retrofitted).

### 5.2 Netcode: extend the existing payloads with an axis field (RECOMMENDED), not new payloads

- C2S `PassageAnswerPayload(cross)` → `PassageAnswerPayload(cross, axis)`;
  S2C `PassageArrivalPayload(arrivalX)` → `PassageArrivalPayload(axis, arrivalX, arrivalZ)`
  (`GlobeNet.java:80-112`). VAR_INT axis: 0 = EW, 1 = POLE. Client and server ship in the same jar
  (Fabric custom payloads are mod-version-locked), so widening the codec is a lockstep-safe change.
- Why not new payload types: one registered pair, ONE server receiver (`GlobeMod.java:220-222`), one
  guard chain with an axis branch — the anti-spoof, alive/online, overworld, underground, and
  cooldown guards (`GlobeMod.java:536-592`) are axis-independent and must not fork into a second
  copy that can drift. `PASSAGE_LAST_CROSS_TICK` cooldown stays per-player GLOBAL across axes
  (crossing spam protection, not an axis resource).
- `HemispherePassageClientState` (`client/HemispherePassageClientState.java:14-55`) carries
  `(axis, arrivalX, arrivalZ)`; `HemispherePassageClient.onArrival` routes the seed to the right
  arm and picks the right title.
- Flag gating in the receiver: axis EW requires `PASSAGE_V2_ENABLED`; axis POLE requires
  `POLE_PASSAGE_V2_ENABLED` — reject otherwise (flag-off = every pole answer ignored, B-5 idiom
  `GlobeMod.java:537-539`).

### 5.3 Server: `HemispherePassageService` generalization

`crossHemisphere`/`resolveArrival` (`HemispherePassageService.java:80-169`) are X-edge-specific in
exactly four places; each needs a pole sibling (recommend an `Axis` enum parameter or a parallel
`resolvePoleArrival`, dev's call — shared probe budget/loop machinery either way):

1. **Target** [P3 fix 2026-07-14: antipodal meridian, not mirrorX]:
   `targetX = round(PoleArrivalSearch.antipodalX(playerX, centerX, xRadius))` (L → L+180; the original
   `mirrorX` here was the EW L → −L formula, live-disproven on TEST 97);
   `targetZ = sign(z) × zForLatitudeDeg(89.5)` (S5) — vs B-5's arrival-abs-X + kept Z (L90-97).
2. **Search axes swap**: primary search runs **±X at the fixed arrival Z** ("keep longitude as close
   as possible" becomes "keep latitude exactly, walk the arrival parallel"), step 16, budget-shared;
   last-resort nudge runs **equatorward along Z** (toward center) — the mirror image of B-5's ±Z
   search + inland X nudge (L108-140). Same `PROBE_BUDGET 40` / reserve discipline (L59,111-112).
3. **Do NOT reuse `latitudeSafeMaxAbsZ`** (L176-183) — it exists to keep EW arrivals OUT of the polar
   cap (`warnStart − 500` ≈ 83°); the pole arrival is IN the cap by design. Its B-7 twin is an
   **X clamp**: keep the ±X search (and the mirrored target) equatorward of the EW passage band —
   `|targetX| ≤ xRadiusIntended − (ewGeo.rearmDist + margin)` — so a corner-of-the-world pole crossing
   can never dump the player inside the EW prompt/fog band and stack ceremonies (the exact discipline
   B-5 applies on the other axis, quoted at L176-179).
4. **Yaw**: pass `player.getYRot() + 180f` for axis POLE (L162-163 currently hardcodes kept-yaw).
   Dismount-first, momentum-zero, fall-reset all unchanged (L160-165).

Server re-validation reuses `serverAcceptsCross(dist, promptAt)` UNCHANGED
(`HemispherePassage.java:216-221` — already axis-agnostic) with
`dist = distanceToPole(...)` and `promptAt = PoleGeometry.resolve(zRadius).promptDist()`; same
32-block slack. The turn-back nudge sibling aims equatorward: `dir = −sign(z − centerZ)` on Z
(`applyPendingPassageTurnBack`, `GlobeMod.java:599-611`, is X-specific at L607).

### 5.4 Arrival Y and the probe primitive

`placeSafeY` (`GlobeMod.java:1017-1035`) is reused verbatim for the land case (3×3 FULL ring +
fluid/air rejection). One polar-specific hardening for the sweeper: the probe does not screen
**powder snow** (a block, not a fluid — `getFluidState` passes; a player dropped onto it sinks in
and freezes faster). Recommend the pole probe reject a `powder_snow` ground block and step on. Snow
layers are fine (motion-blocking, spawn lands on top).

## 6. Land/water-matched arrival — Peetsa's rule vs Peetsa's frozen pole

**Finding that reshapes this requirement:** `POLAR_WATER_FREEZE_ENABLED` (default TRUE,
`LatitudeV2Flags.java:223-224`) freezes ALL exposed water at/above 85°
(`PolarWaterFreezeRule.FREEZE_ALL_DEG`, enforced by `BiomePolarWaterFreezeMixin`) — Peetsa's own
2026-07-12 correctness fix. The entire B-7 ceremony band (85–90°) therefore has **no open water**:
no boat reaches the 89.2° prompt, no swimmer departs there; the "sea" at both the prompt and the
88.0° arrival line is walkable ice sheet, and `placeSafeY` already succeeds on ice (ice is not a
fluid, heightmap tops it). Peetsa's rule was written for the EW edge, where B-5's shore can be real
ocean; at the pole his own freeze law has already answered it.

**Recommended design — match by SURFACE CLASS, not fluid state:**
- Departure sensing (server, at answer time): classify the departure column's biome ocean-family vs
  land via the existing no-chunk-gen biome probe idiom (`isLandBiome`, `GlobeMod.java:990-1007`,
  `BiomeTags.IS_OCEAN/IS_RIVER`) — a boat/swimmer/ice-sheet traveler reads "ocean," a tundra walker
  reads "land."
- Arrival preference: order the ±X arrival-parallel search to try SAME-CLASS columns first (probe the
  cheap biome source at each candidate before spending a FULL-ring `placeSafeY`), fall back to
  any-safe when the preferred class exhausts a sub-budget (e.g. 24 of the 40 probes). Sea-route
  travelers emerge on the far side's frozen sea; land travelers on far-side land. Safety stays law:
  NEVER fail a crossing over medium — mismatch beats no-op.
- **`placeSafeWaterY` (a water-surface landing variant) is NOT built for B-7** — dead weight while
  the freeze law holds. Documented accepted edge: with `polarWaterFreeze` explicitly OFF, liquid
  columns fail `placeSafeY` and the search walks to the next column — safe, possibly
  medium-mismatched, niche-flag-only. If Peetsa ever wants true open-water arrivals (or B-5 later
  adopts medium-matching at the EW shore, where it would really bite), the variant slots into the
  same preference hook.
- **Boats/mounts: keep B-5's dismount** (`stopRiding`, `HemispherePassageService.java:160-161`) — no
  vehicle-carry machinery, no ghost entities, parity with the shipped crossing. The boat stays at the
  departure shore; on the far side the sea is ice anyway. The "arrives swimming" case B-5 worried
  about cannot occur at the pole while the freeze law holds.

## 7. Warning-ladder honesty + advisory + copy (OPTIONS for Peetsa — no unilateral changes)

Ladder (verbatim, `GlobeWarningOverlay.java:45-52`): WARN_1 85° "Snow begins to fall. Blizzard
conditions ahead -- consider turning back." · WARN_2 87° "The blizzard deepens -- hypothermia is
setting in. Turn back while you can." · DANGER 89° "DANGER: Lethal blizzard conditions ahead. Turn
back." (**verbatim-locked**) · LETHAL 89.7° "Severe hypothermia -- you are freezing to death."

The tension: with a crossing available, "Turn back." at 89° is the same honesty problem B-5 solved at
the EW edge (B-5's fix: retire the two-tier "Turn back." banner for one advisory + let the prompt be
the counter-offer). But these four rungs are Peetsa's own lines and the DANGER line is untouchable.
Options, his call at P3:

- **O1 (recommended default): change NOTHING.** "Turn back" remains true — it IS the survivable
  choice; the prompt 0.2° later is the consensual counter-offer, exactly like real-world "do not
  proceed" signage before a mountain pass that nonetheless has a road. Zero text churn, zero risk to
  the KEEP-SHARED constants (only copy could ever change; the degrees never move regardless).
- **O2 (minimal reword, flag-gated like B-5's honesty pass):** only when `polePassageV2` is ON,
  append a second clause to **LETHAL only** (the one rung poleward of the prompt, and NOT the locked
  DANGER line), e.g. "Severe hypothermia -- you are freezing to death. The crossing is your only way
  on." Presented as opt-in copy for Peetsa to write/veto.
- **O3 (new advisory line):** a white banner in the EW-advisory family (`EwBannerEnvelope`,
  copy anchor ~86°, between WARN_1 and WARN_2), family text: "Approaching the Pole. A crossing lies
  beyond the whiteout." CAUTION: the polar band already fires four center-screen lines in 5°; a fifth
  risks text spam — recommend AGAINST unless Peetsa asks for an explicit early heads-up that the pole
  is passable. (The EW advisory precedent: one white fading line, `GlobeWarningOverlay.java:73-74`.)

Prompt copy (screen `HemispherePassagePromptScreen.java:58-61,85-90` parameterized): title
"Blizzard advisory" (family of B-5's "Heavy fog advisory"); body proposal:
"Beyond the whiteout lies the far side of the {North|South} Pole." Buttons unchanged
("Pass through" / "Turn back") — same muscle memory, ESC = turn back.

Arrival title (the hemisphere-title system has NO pole title today — N/S fires only at the EQUATOR,
`GlobeWarningOverlay.java:630-642`): fire on the E/W channel (`HemisphereTitleOverlay.trigger(true, …)`,
the channel B-5's arrival already uses, `HemispherePassageClient.java:309-314` — factually right too:
a pole crossing flips the E/W hemisphere). Copy options for Peetsa: **T1 "Beyond the North Pole"**
(recommended; noun-phrase family match with "Northern Hemisphere") · T2 "Across the North Pole" ·
T3 "The Far Side of the Pole". South variants mirror.

## 8. Structures near the pole — verdict: no new veto needed

`ExtremePolarVillageGuardMixin` (`mixin/ExtremePolarVillageGuardMixin.java:26-58`) already cancels
**villages** (path prefix "village") at/above 74.5° (`LatitudeBiomes.EXTREME_POLAR_CAP_MIN_DEG`,
`world/LatitudeBiomes.java:2778`, block test L6857-6861) — the only structure that would read as
absurd in the death band. Everything else that can appear in the 88–90° biome set (igloos, the odd
outpost) is rare, polar-authored, and — inside a band that deals 0.3–6 HP/s — an igloo is a mercy
shelter, not an immersion break. The EW `EdgeStructureVeto` exists for a DIFFERENT reason ("the storm
belt is wild, empty land" — a visible village silhouette in the fog wall, `core/EdgeStructureVeto.java:4-8`);
the polar cap already reads authored. Recommendation: **no pole structure veto in B-7**; verify at P3
with eyes, and if Peetsa wants parity later, `EdgeStructureVeto`'s pure shape generalizes to a Z-band
(`inPoleBand`) in an afternoon — but that WOULD be a worldgen flag (own switch, like
`EDGE_STRUCTURE_VETO_ENABLED`, `LatitudeV2Flags.java:185-186`), forfeiting a sliver of B-7's
"zero worldgen" purity. Keep it out of scope.

## 9. What B-7 deliberately does NOT do

- **No mirror-band worldgen, no decoration strips, no per-world capture** — the B-6 landmines. The
  far side is whatever the world already generated there; the whiteout (not matching terrain) sells
  the transition, exactly like B-5's fog does.
- **No new fog/overlay system** — the polar ambient/whiteout/depth-fog IS the ceremony (§3.2).
- **No silent crossing** — prompt-gated, consensual, B-5 law.
- **No boat/mount carry** — dismount-keep (§6).
- **No warning-ladder constant moves** — 85/87/89/89.7 are KEEP-SHARED with the EW axis
  (`LatitudeMath.java:36-45`); copy changes are Peetsa-gated options only (§7).
- **No damage-pause machinery** at the recommended prompt depth (§4.4) — flagged as a P3 fallback,
  not built.
- **No hard-stop wall at the Wide pole line** — out of scope here, but §3.1 notes B-7 de-fangs the
  unmarked death-plain by giving it an exit; the parked "pole hard-stop" decision can ride on P3
  observations.

## 10. Pass plan (tiered workflow, this worktree)

- **B-7-P1 core + server** (Opus dev → test-writer → sweeper → reviewer):
  `PoleGeometry` (pure, floors + ordering chain + `distanceToPole`, unit tests incl. Itty floor
  cases); `HemispherePassageService` axis generalization (§5.3: target/search-swap/X-clamp/yaw+180,
  powder-snow probe hardening); netcode axis field + receiver routing + pole validation; flag
  `latitude.polePassageV2.enabled` default OFF **+ its build.gradle forwarding line SAME PASS (L17)**;
  turn-back Z nudge; surface-class arrival preference (pure ordering logic unit-tested).
  Gate: suite green; worldgen untouched by construction (no atlas run needed — assert no worldgen
  files in the diff instead).
- **B-7-P2 client** (Opus dev → sweeper → reviewer): second arm instance + pole distances;
  prompt-screen copy parameterization; arrival routing/seed by axis; pole arrival title; poleward
  gesture sibling; PassageDebug axis tags. Gate: suite green + orchestrator live self-verification
  via the Modrinth control lane + flight recorder (the B-5 P3-round-3 discipline: fly the repro
  before Peetsa does — dash to 89.2 in creative→survival swap, cross, verify arrival line/yaw/title,
  decline path, gesture, Itty floors).
- **B-7-P3 Peetsa live**: prompt depth FEEL (is 89.2 the right dash?), ceremony damage tax verdict
  (§4.4 fallback decision), arrival emergence (ice-sheet vs land preference feel), title + prompt
  copy picks, ladder-honesty option pick (§7), Wide-pole soft-edge observation. Merge to
  `port/canonical-26.2-pivot` only after P3 + Peetsa OK (B-5 precedent).

## 11. Open questions for the sweeper

1. **Prompt-vs-DANGER co-firing**: at 89.2 the DANGER rung (89.0) fires ~22 blocks earlier
   (Regular). On Itty the prompt floor (40 blocks = 89.04°) lands nearly ON the rung — is a
   same-tick warning+screen collision possible there, and does it need an ordering guarantee
   (warning first) or is coexistence fine (overlay vs Screen)?
2. **Corner of the world**: §5.3's X-clamp keeps pole ARRIVALS out of the EW band — but a player
   standing in BOTH bands (Wide corner) has two armed machines; the shared screen/curtain gate
   serializes prompts, first-to-fire wins. Any sequence where answering one prompt strands the other
   arm in a wrong phase?
3. **Both-flags interplay**: pole crossing while the EW machine is DISARMED/SEEDED — the X mirror
   moves the player to the same EW border distance (mirrorX preserves |x−centerX|), so the EW arm's
   in-band state survives a pole crossing. Verify no EW self-prompt/ghost-re-arm cases (the arrival
   seeds only the POLE arm).
4. **`serverAcceptsCross` slack at pole scale**: 32 blocks ≈ 0.77° on Itty Z (vs 0.29° on Regular) —
   spoof-window acceptable, or should slack be degree-derived on the pole axis?
5. **Beyond-the-line prompt (Wide)**: `distanceToPole = 0` past 90° keeps the prompt openable for a
   still-alive walker at 6 HP/s — intended (mercy exit) or should the prompt require
   `|z| ≤ zRadius + margin`?
6. **Powder-snow probe rejection** (§5.4) — confirm the block-state check list (powder_snow only, or
   also e.g. cactus-class hazards irrelevant at the pole?).
7. **Ceremony tax honesty**: is charging ~3–5 HP while the prompt is open acceptable without any
   on-screen acknowledgment, or should the prompt body hint it ("The cold does not wait.")? (Copy =
   Peetsa, mechanism = sweeper.)
8. **Creative/spectator**: cross allowed, damage-exempt, nudge-exempt (B-5 parity,
   `GlobeMod.java:533-534,587-589`) — confirm no pole-specific wrinkle.
9. Saturated-regen assumption in §4.2 (2 HP/s while saturation lasts) — worth a live sanity check at
   P2 self-verification so the survival table Peetsa reads is honest.

## 12. Corrected recon pointer table (pivot HEAD f5539e35)

| concern | pointer |
| --- | --- |
| N=−Z sign; latitude/degrees; pole distance; zForLatitudeDeg | `src/main/java/com/example/globe/util/LatitudeMath.java:197-200,110-119,134-138,189-195` |
| KEEP-SHARED ladder constants 85/87/89/89.7 | `util/LatitudeMath.java:36-45`; EW twin `hazardStageIndexEW` L175-181 |
| EW geometry class to sibling (anchors/floors/resolve/ordering) | `src/main/java/com/example/globe/core/EdgeGeometry.java:119-168,219-251` |
| phase machine, gesture predicates, mirrorX, serverAcceptsCross | `src/main/java/com/example/globe/core/HemispherePassage.java:138-205,229-278,216-221` |
| teleport service (target, budgeted search, dismount, yaw, Z-cap) | `src/main/java/com/example/globe/HemispherePassageService.java:59-70,80-183` |
| polar hazard curve (onset/damage/frost/ambient/fog/storm/blizzard) | `src/main/java/com/example/globe/core/PolarHazardWindow.java:46-58,108-188,192-233,253-323` |
| server damage application + in-band gate + vanilla suppression | `src/main/java/com/example/globe/GlobeMod.java:467-525,669-681`; `mixin/LivingEntityFreezeDamageMixin.java:50-68` |
| passage answer handling, guards, turn-back nudge, underground | `GlobeMod.java:220-222,536-611,626-632` |
| placeSafeY + chunk ring | `GlobeMod.java:1017-1048`; biome probe idiom L990-1007 |
| netcode payloads + handshake radii | `src/main/java/com/example/globe/GlobeNet.java:36-58,80-112` |
| client driver (arm, gesture, curtain, arrival, title, whoosh) | `src/main/java/com/example/globe/client/HemispherePassageClient.java:36-97,121-188,214-245,247-354` |
| prompt screen (copy, buttons, isPauseScreen=false) | `src/main/java/com/example/globe/client/HemispherePassagePromptScreen.java:58-61,85-125` |
| arrival client state stub | `src/main/java/com/example/globe/client/HemispherePassageClientState.java:14-55` |
| ladder copy + honesty note + EW advisory + hemisphere titles | `src/main/java/com/example/globe/client/GlobeWarningOverlay.java:20-52,73-74,600-654,685-689` |
| client distances / underground / edge geometry accessors | `src/main/java/com/example/globe/client/GlobeClientState.java:177,209-216,417` |
| flags + L17 forwarding discipline | `src/main/java/com/example/globe/core/LatitudeV2Flags.java:138-224`; `build.gradle:102-114` |
| EW structure veto (pure band) / polar village guard / freeze-all-water | `src/main/java/com/example/globe/core/EdgeStructureVeto.java:60-101`; `mixin/ExtremePolarVillageGuardMixin.java:26-58` + `world/LatitudeBiomes.java:2778,6857-6861`; `LatitudeV2Flags.java:205-224` |
| fog composition precedent (why NO new fog mixin) | `mixin/client/FogRendererPassageSetupMixin.java:40-42`; `mixin/client/FogRendererPolarSetupMixin.java` |
| title-crossing debounce (teleport step guard, degree-first dead zone) | `src/main/java/com/example/globe/core/HemisphereCrossing.java:39-63`; `GlobeWarningOverlay.java:600-626` |
| polar experience single source of truth | `docs/binder/polar-experience-reference-20260712.md` |
| B-5 design + shipped history this design rhymes with | `docs/binder/phase5-b5-hemisphere-passage-design-20260710.md` |


## Adversarial design sweep — APPROVE-WITH-AMENDMENTS (2026-07-13) + Peetsa stipulations folded

SWEEP (all 9 questions answered; ~30 pointers spot-checked correct; survival math independently
re-derived and HONEST — 89.2 prompt = 1.47 HP/s exact, dash ~38 HP walking on Regular, label the table
"with food + Protection"): A1 yaw+180 is the same absolute-yaw mechanism B-5 already uses (+ wrap
degrees; watch the half-opaque-curtain camera whip at P2 self-fly — whiteout stacking masks ~80%);
A2 the corner X-clamp must bound EVERY ±X search candidate, not just the initial target; A3 the
turn-back nudge must be AXIS-KEYED (pole declines nudge along Z; map or twin sets; same creative/
spectator exemption); A4 placeSafeY must reject powder_snow landings (fluids-only today) + scan 1-2
below top; A5 corner gesture tie: arm the larger facing-dot axis (or accept the serialized double-
prompt — dev picks, states); A6 survival table honesty labels. Q-answers: DANGER/prompt coexist fine
(different layers; 1.67 blocks apart on Itty — P3 feel check); corner serialization safe via HOLD-not-
burn; mirrorX preserving EW distance is neutralized by the clamp (pin the corner test: EW phase ARMED,
no EW prompt post-cross); keep flat 32-block slack; ceremony tax ~4.6 HP self-limiting (stalled server
thread bills no ticks); shared 60t cooldown stays shared; netcode axis extension lockstep-safe (mixed-
version already unsupported globally); pole geometry lerp-immune FOR FREE (synced latitudeZRadius —
no new handshake field); frozen_ocean classifies ocean under ice so the surface-class probe works;
ARRIVAL 88.0 affirmed over 87.9 — the blue hearts THAW as you walk out. ARRIVAL-REGIME INVARIANT to
pin in P1 tests: on Itty arrival(83)<rearm(104) = SEEDED path live; on Regular arrival(222)>rearm(167)
= re-arms to ARMED next tick; BOTH self-prompt-free (arrival > promptAt everywhere).

PEETSA STIPULATIONS (owner, 2026-07-13, folded as design law):
S1 COLD PROTECTION: a ColdProtection evaluator (vanilla freeze_immune_wearables tag — leather by
default, datapack-extensible per the vanilla-first law; piece-count scaling, full set = freeze damage
negated, slowness KEPT so the pole still resists) feeds BOTH the damage curve scaling AND the warning
text: protected players see "The bitter cold envelops you." instead of the LETHAL line (one evaluator,
one truth — the honesty law; the DANGER line stays his verbatim). Potions: v1 = armor tag only
(no vanilla cold-resist exists; future "expedition tonic" idea noted).
S2 THE WALL + UNDER-ICE: prompt suppressed for under-ice/underwater players (extend the surface gate:
in-water OR no-sky suppresses at the pole), and Wide worlds get a MOD-BUILT HARD STOP at the pole line
— a server-side motion clamp (pure movement logic, zero worldgen, vanilla-border-like; NOT the
scrapped per-tick teleport wrap) so an ineligible traveler hits a wall exactly like east/west.
SYNTHESIS with the sweep's mercy-exit: the clamp stops everyone; the crossing (surface, prompted,
possibly protected) is the door THROUGH it — beyond-the-line rescue applies to eligible players at
dist 0 pressed against the clamp. Classic worlds already have the vanilla border as the wall.

S3 TWO-STAGE COLD PACING (Peetsa decision 2026-07-13, "Two-stage cold" picked over literal
lethal-from-85 and keep-current): the polar approach starts earlier without touching the flight-
tested endgame. Snow visual onset 85->82 deg (world whitens before any text); first warning rung
85->82 deg (approach wording) [owner-revised 2026-07-14 from the P2 draft's 83: placed AT the
82-deg snow onset so the first words land with the first snowflakes; verbatim copy "Entering polar
storm country. Proceed with caution." replaces the old WARN_1 snow line; the ladder's retreat
re-arm moved 82->81 with it, keeping the same 1-deg hysteresis width]; NEW FROSTBITE damage band
[85,88): gentle ramp ~0.25->1.0 HP/s, interval-based like the lethal core, the 85-deg rung = real
damage onset (honesty law) [copy owner-revised 2026-07-14: verbatim "The cold begins to bite."
replaces the draft "Hypothermia sets in."; the removal whisper "Hypothermia is imminent." is
unchanged], ColdProtection (S1) multiplies BOTH bands (full leather = zero damage in both); blizzard-
building rung 87, DANGER 89, LETHAL 89.7, lethal core [88,90] curve ALL UNCHANGED — the B-7 prompt-
zone survival math the sweep verified stays true verbatim. GUARDS: PolarWaterFreezeRule keeps 85
(the ice sheet is world-visible — decouple the constant if it references AMBIENT_ONSET_DEG; moving
it would be a worldgen seam, forbidden here); PolarPrecipitationRule keeps its own anchors (verify
the ambient move does not drag any worldgen-facing rule). Split: frostbite band + guards = P1
(server); ladder rungs/wording + snow onset 82 = P2 (client).
S2 ADDENDUM (invisible-wall feedback, Peetsa 2026-07-13): the Wide pole clamp must announce itself
— server plays an ice-chime at the rate-limited contact point + actionbar line, underwater "Pack
ice, frozen to the seafloor, bars the way." / cave-or-surface "The ice of the world's end bars the
way."; P2 adds frost particles at contact and evaluates a faint glacial keyline plane within a few
blocks (vanilla-border-grid analogue). Classic worlds already show the vanilla border wall there.
[P2 polish 2026-07-14, sweep INFO adopted: the frost presentation is flag-gated WITH the wall it
presents (latitude.polePassageV2.enabled) — flag-off there is no clamp, so no frost may suggest
one; the actionbar/chime are server-side behind the same flag already. The glacial keyline plane
was deferred at P2 — then SUPERSEDED at P3 by an owner order off the TEST 97 maiden flight
("There needs to be the diagonal lines like vanilla"): BUILT as PoleWallRenderer, a vanilla-
forcefield-texture diagonal-stripe plane at z = ±zRadius (Wide worlds, same flag, ~40-block
approach fade, glacial blue-green tint), submitted through the 26.2 custom-geometry path.]

S1 ADDENDUM (hypothermia rung vs cold protection, Peetsa 2026-07-13, P2 scope): the 85-deg
"Hypothermia sets in" rung fires ONLY when protection is below FULL (honesty law: full leather =
zero frostbite = no hypothermia = no warning; partial protection still bleeds, so the rung shows).
Equipping full protection in-zone earns silence — nothing new fires. REMOVAL-REACTIVE WHISPER: on
losing full protection while >=85 deg, whisper "Hypothermia is imminent." (whisper presentation
style, black outline family), one-shot; it REPLACES the rung re-fire at that moment (no back-to-back
double message); re-arms on leaving the zone or regaining full protection. LETHAL 89.7 swap stays
as S1 wrote it (protected: "The bitter cold envelops you."); DANGER 89 verbatim line untouched (it
describes the place, not the body). Client evaluates ColdProtection locally for the OWN player
(armor slots + freeze_immune_wearables tag are client-visible); P1 core exposes protectionLevel —
no P1 change required.

S4 SHELTER RULE FOR COLD DAMAGE (Peetsa 2026-07-13, P1 scope): cold DAMAGE (frostbite band AND
lethal core — one rule, one story: walls stop the bleeding) PAUSES when the player is genuinely
sheltered. Slowness/Mining Fatigue/atmosphere UNCHANGED (the cold seeps indoors; only the damage
stops — also keeps under-pole tunnel traversal paced). Predicate (server, cheap, trap-proof):
sheltered iff raw SKY LIGHT at eye position <= 3. This is graded enclosure — the single-overhead-
log trap (Peetsa's explicit callout; the old warning-banner-under-a-tree bug class) reads ~11-13
skylight (diffuse light floods in sideways) = still exposed; a sealed hut/cave/snow burrow reads
0-2 = sheltered. Follows PolarWhiteoutOverlayHud's graded-exposure philosophy (never binary
canSeeSky for gameplay — that predicate stays only where conservative-deny is correct: prompt
gates). MANDATORY TEST: the log-trap case itself (solid block 1 above head, open sides -> NOT
sheltered, damage continues) + sealed-box case (sheltered, damage paused) + threshold boundary.
KNOWN EDGE (accepted, P3 feel item): a pure-glass roof passes skylight -> glass igloo counts as
exposed ("the cold bites through the glass") — story-defensible, rare at the poles, revisit only
if live feel demands. Balance note: this makes dig-in-and-survive real counterplay (snow-cave
expedition fantasy) and keeps the warning ladder honest (no LETHAL text while safe in a hut —
warnings already suppress NEW triggers under cover per B-3).

S5 ARRIVAL DEEPENED TO 89.5 (Peetsa 2026-07-13, "cinematic + no damage reprieve — still trekking
for their lives"): pole arrival moves 88.0 -> 89.5 deg on the far meridian, heading equatorward.
You emerge INTO the mirrored blizzard (at ~2.2-2.6 HP/s unprotected, Slowness III) and fight your
way out; the frozen-hearts thaw now happens during the ESCAPE (~88 deg), not at touchdown — the
reprieve is gone by design. GUARD-RAIL: cold damage is suppressed ONLY while the arrival curtain
is up (~2 s, the ceremony window) so a low-health crosser cannot die inside the cutscene; the
moment the curtain lifts, the blizzard owns them. CONSEQUENCES (all simplifying): arrival (0.5 deg
from pole) is now INSIDE prompt (0.8) and rearm (1.5) on EVERY world size -> the two-regime split
(Itty seeded vs Regular re-arm) COLLAPSES to one uniform SEEDED_DISARMED regime everywhere — the
sweep's dual-regime pins are superseded; new pins: uniform seeded regime on all sizes; no self-
prompt at arrival is guaranteed by SEEDING (state law), not geometry — the assertChain invariant
"prompt < arrival" is retired for the pole axis and replaced by the seeded-arrival assertion.
Walking out re-arms past 88.5; turning back then prompts at 89.2 (fresh journey); while still
seeded, only the 89.9 one-shot edge re-prompt speaks. Medium matching unchanged (sea at 89.5 is
frozen solid — water-family arrival stands on the ice sheet). Net forward-carry: consent at 89.2,
emerge at 89.5 far side — the crossing carries you through the worst 0.6 deg; story-true.

S6 FROZEN WOUNDS (Peetsa 2026-07-13, "big idea", closes the S4 shelter-refill cheese): while
SHELTERED (S4 predicate) inside the polar cold zone (|lat| >= 85) and NOT near warmth, ALL healing
is cancelled — hearts stay frozen at their current level ("only the warmth of a fire mends them":
food, natural regen, potions, gapples all wait). NEAR WARMTH (within a ~4-block box, lit
NON-SOUL campfire / fire / lava incl. lava cauldron / LIT furnace-family) healing works normally —
the snow-cave campfire ritual IS the mechanic; pack fire or bleed. Torches/lanterns/candles are
light not heat; SOUL fire/campfire give no warmth (story detail). OUTDOORS unchanged (the flight-
tested eat-vs-cold race stands — the lock is the INDOOR rule; no cumulative pool, zero new
persistent state: lock = pure predicate sheltered && inZone && !nearWarmth applied at heal time).
Below 85 deg: normal healing, always. PRESENTATION (P2): frozen-hearts tint PERSISTS while heal-
locked indoors (hearts look frozen while they cannot mend — replaces the F3 sheltered-cue-pause in
exactly this state); one-shot whisper on first blocked heal "Your wounds are frozen. Only warmth
can mend them."; near warmth the tint clears (optional soft whisper on thaw). SERVER (P1): pure
warmth-scanner core (block-set -> boolean) + thin shim; heal hook cancels/caps heals under the
lock; client derives the same three predicates locally for presentation (skylight, zone, warmth
scan) — NO new netcode.

## STATUS 2026-07-14: BUILT THROUGH P2+POLISH — TEST 97 STAGED, MAIDEN POLE FLIGHT PENDING
P1 server (c72aed96, suite 498, sweeps ACCEPT x2) + P2 client (6d12c86f, suite 519, delta sweep
ACCEPT) + owner copy polish (rung 82 "Entering polar storm country. Proceed with caution.", 85
"The cold begins to bite.", frost flag-gated with its wall) + branch-local flight staging flip
(fc2279b5, B-6 precedent, REVISIT BEFORE MERGE). TEST 97 staged SHA e30223e6... (superset-verified
vs pivot f5539e35; markers: polar-storm-country + cold-begins-to-bite + pack-ice + polePassageV2 +
antimeridian). P3 = Peetsa maiden pole flight; EW TEST-96 regression rides along (EW proven
byte-identical through both delta sweeps). Ship default = Peetsa's post-P3 call.

## STATUS 2026-07-14 PM: P3 FIX ROUND COMMITTED (1b6ca726) — TEST 98 STAGED
Maiden-flight (TEST 97, world "Test 97" MERCATOR z3750) findings all fixed: antipodal crossing
(mirrorX was wrong for a pole — owner repro 13E->167W pinned), wall stops creative (vanilla parity,
spectator-only), vanilla-forcefield stripe wall visual on the exact clamp line, deep blizzard ~10x
87->89+ fall 1.75x, aim-gated gesture (open-air clicks only, both axes), far-meridian arrival
subtitle. Delta sweep #3 ACCEPT, suite 528/528. TEST 98 SHA 57c147f0... staged (markers: far
meridian, PoleWallRenderer, storm country, pack ice, polePassageV2). Geometry truth told to owner:
a pole is a point — crossing = far side of the SAME pole on the antipodal meridian; repeating-band
map extension REJECTED (endless-wrap worldgen rebuild, B-6 class). Owner may still flag the
crossing off post-flight if it does not earn its place. B-8 Polar Barrens design swept in parallel.
