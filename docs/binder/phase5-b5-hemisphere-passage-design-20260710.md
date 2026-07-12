# Phase 5 B-5 — Hemisphere Passage: design (2026-07-10)

`status: DESIGN APPROVED-WITH-AMENDMENTS 2026-07-12 (sweep below); build authorized (B-5 first, then B-6 evator — Peetsa confirmed sequence)`
Worktree `Latitude-b5-hemisphere-passage` (branch phase5-b5-hemisphere-passage, off b6c829d7). Authorized
by Peetsa (B-4 green-lit the passage). Recon (this session) mapped every mechanic to a working in-repo
precedent; file:line pointers below are from that recon.

## Concept (from the plan doc, unchanged)
Approach the E/W world edge (the antimeridian, |x|→xRadius) → fog thickens to opaque → a two-button
"Heavy fog advisory — pass through?" prompt → CROSS = a short opaque blur that masks a synchronous
destination-chunk load, then teleport to the mirrored X on the far side, fog thins, arrival title
"EASTERN/WESTERN HEMISPHERE" → TURN BACK = gently steer + soft-push the player inland. Anti-spam: the
prompt arms once when the player ENTERS the fog band and cannot re-arm until they LEAVE it, so sliding
N/S along the edge never re-prompts. This is EXPLICIT + CONSENSUAL, NOT the scrapped seamless wrap.

## Flag
New house flag `latitude.passageV2.enabled` (default OFF) — the whole feature (fog band, prompt, teleport,
push-back) is inert when off; the current live edge behavior (EW sandstorm haze + warnings) is untouched.
Opt-in for testing; a later default-on decision is Peetsa's. NOT tied to boundaryV2 — the passage works
whatever the edge terrain looks like; the B-2 ocean shore is a visual nicety, not a requirement.

## State model (client-authoritative UI, server-authoritative teleport)
Per-player passage state machine, driven off the already-client-known border distance
(GlobeClientState.distanceToEwBorderBlocks, client/GlobeClientState.java:245-282; border geometry is
vanilla-synced, center 0,0):
- OUTSIDE (dist > FOG_START): nothing.
- APPROACH (FOG_START ≥ dist > PROMPT_AT): the new transition/approach fog overlay ramps (reuse the
  PolarWhiteoutOverlayHud pow(0.65) easing that fixed the pole "WHAM", NOT the old intensity² — a
  dedicated HemispherePassageFogHud, own file, same ctx.fill idiom).
- PROMPT (dist ≤ PROMPT_AT AND armed): open the two-button Screen (modeled on SpawnZoneScreen.java) via
  the pending-open + client.gui.screen()==null tick guard (GlobeModClient.java:195-200). Disarm on open.
- CROSS chosen → C2S PassageAnswerPayload(cross=true) (mirror SetSpawnPickerPayload,
  GlobeNet.java:61-73 + receiver GlobeMod.java:195-197): server force-loads a 3×3+ ring at the mirror
  target (loadSpawnTargetChunkRing pattern, GlobeMod.java:794-805), teleports to (-x, surfaceY, z) with
  momentum/fall zeroed (applySpawnChoice pattern, GlobeMod.java:560-588), then S2C to the client to run
  the arrival: fog held opaque a few ticks over the teleport, thin out, HemisphereTitleOverlay.trigger(
  true, "Eastern/Western Hemisphere", …) (client/HemisphereTitleOverlay.java:54-74).
- TURN BACK chosen (or prompt dismissed): client closes; a gentle inland steer — small outward-normal
  velocity impulse toward center-X applied server-side each tick while in the band with a pending
  turn-back/idle state (NEW code — no push-back exists today; nearest shape is borderUxTick's throttled
  per-player loop, GlobeMod.java:365-415). "Soft" per Peetsa — NOT a hard camera yank; nudge velocity +
  a slight yaw ease, no forced camera lock.
- Re-arm: the prompt re-arms only after the player leaves the fog band (dist > FOG_START + margin) —
  band hysteresis with the DEAD_ZONE+32 floor discipline the sweeper required on the title bands
  (GlobeWarningOverlay.java:336-398). A NEW small pure helper (armed + inBand, no center-crossing
  concept — HemisphereCrossing.evaluateBanded is crossing-shaped and does NOT fit) + unit tests.

## Mirror target
Border is centered at (0,0) sized to xRadius (GlobeMod.java:335-360). "Same latitude, opposite hemisphere"
= negate X, keep Z: `targetX = -playerX, targetZ = playerZ`. Identical in Classic and Mercator (only the
xRadius value differs). Confirmed against LatitudeMath.hemisphereEW (x<0=W / x≥0=E), util/LatitudeMath.java.

## What B-5 deliberately does NOT do (scrapped-wrap landmines, dissolved by explicit framing)
- No seamless illusion: the crossing is a framed trip, so terrain need NOT match across the seam.
- No fake density-function carve: do NOT resurrect LatitudeOceanSinkDensityFunction / the RandomState
  ocean-sink mixin (deleted). Both shores already get real latitude-correct ocean from B-2 when boundaryV2
  is on; the passage rides that, never a hacked NoiseRouter.
- Cloud-snap on teleport is ACCEPTED and MASKED by the opaque transition, not solved.
- No silent per-tick wrap: the old wrapAndClampTick is gone; B-5 is opt-in per crossing by design.

## Passes (tiered workflow, in the worktree)
- B-5-P1 server: flag, netcode payload(s), teleport + chunk-ring + momentum-zero, mirror target, push-back
  tick. (Opus dev → test-writer for the pure re-arm helper → sweeper → reviewer)
- B-5-P2 client: the fog overlay, the prompt Screen, the approach/transition state machine + arrival title
  wiring. (Opus dev → sweeper → reviewer)
- B-5-P3 live: Peetsa flies it (passageV2 on) — fog feel, prompt timing, no re-prompt sliding N/S,
  arrival, push-back feel.
Gate: compile + suite green per pass; a headless can't test teleport/UI, so the real gate is P3 live.
Held in the worktree branch; merged to port/canonical-26.2-pivot only after P3 + Peetsa OK.

## Open design questions for the sweeper to pressure-test
1. Client-authoritative prompt vs server: can the client decide the prompt purely off border distance
   (no S2C trigger needed), sending only the C2S answer? (recon: likely yes.) Any exploit/desync risk?
2. Mounted/passenger players, elytra at speed, ridden boats — teleport carries the mount? (recon: no
   mount-carry code exists; decide accept/deny/dismount.)
3. The synchronous chunk-ring force-load hitch under the fog — is 3×3 enough, or does arriving at speed
   need 5×5? server-thread block duration acceptable?
4. NO/turn-back push-back that isn't a "hard yank" — does a per-tick velocity impulse fight player input
   annoyingly? alternative: one-time impulse + let them fly back.
5. Re-arm band width vs the prompt distance — ensure you can't get stuck oscillating prompt/dismiss.


## Adversarial design sweep — verdict 2026-07-12 (rebased onto 27ce06d6)

**APPROVE-WITH-AMENDMENTS (14).** Core architecture sound; state machine / mirror target / pass split
survive. Corrected pointer table produced (several pointers drifted ~54 lines; two precedents DEAD).
Must-fix trio before code: (1) NETCODE — the SetSpawnPickerPayload/OpenSpawnPicker precedent is
ORPHANED dead code (SpawnZoneScreen deleted this week); build fresh C2S PassageAnswerPayload + S2C
PassageArrivalPayload on the LIVE GlobeStatePayload idiom. (2) TARGET SAFETY — placeSafeY returns null
on fluid; the B-2 shore is boundaryV2-dependent, so mirrored X often = ocean with passage-on/boundary-
off; must null-handle w/ outward ±Z safe-column search. (3) ARRIVE-IN-BAND — targetX=-x lands at the
IDENTICAL border distance; S2C arrival must seed client state in-band+DISARMED or it self-reprompts
forever. Honesty: EW "Turn back." warning strings must be gated/reworded when passageV2 on (the edge
becomes passable; stage-2 text fires in the same band as the prompt). Post-marathon integration:
approach fog = REAL DEPTH FOG (FogRendererPolarSetupMixin discipline; Peetsa vetoed a flat tint this
session), crossing curtain = short opaque screen fill (teleport mask only); both EXPOSURE-INDEPENDENT;
suppress EwSandstormOverlayHud + reconcile clampEwViewDistance inside the band. Q answers: client-
authoritative prompt OK but server MUST re-validate edge distance on the C2S (anti-exploit); dismount
before teleport (no mount-carry), momentum-zero handles elytra; 3x3 FULL ring sufficient under the
curtain (5x5 only if P3 shows pop-in); turn-back = ONE-SHOT gentle impulse (per-tick fights input =
the vetoed yank), host borderUxTick, skip creative/spectator; re-arm hysteresis asymmetric (PROMPT_AT
< FOG_START, re-arm > FOG_START+margin, margin >= DEAD_ZONE 64, floor DEAD_ZONE+32; FOG_START absolute
blocks <= ~15% of smallest xRadius 3750). Also: per-player S2C only (never broadcast); prompt only when
client.gui.screen()==null; guard dead/offline between answer and teleport; creative/spectator free
passage; P3 matrix = passageV2 x boundaryV2 x Classic/Mercator (X-radius, never Z). One-shot crossing
whoosh in P2; approach wind bed deferred. L17: the flag's build.gradle forwarding line lands in the
SAME PASS the flag is born (three prior bites). B-6 NOTE (Peetsa confirmed sequence 2026-07-12): P1
must build the teleport core (mirror math, ring, dismount/momentum, safety search, crossing state) as
a CLEAN LAYER the ceremony sits ON TOP of — the evator reuses the core and swaps the ceremony for
silence + mirrored terrain.
