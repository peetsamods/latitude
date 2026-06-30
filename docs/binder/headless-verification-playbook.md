# Headless verification playbook — proving worldgen behavior without playing the game

`status: active` · `scope: validation methodology, for any agent (Codex/Claude) working this repo`

Codex/Claude often can't reliably pilot the real Minecraft client to "live test" a change — GLFW doesn't
accept synthetic input reliably, and the project's HARD RULE is Modrinth-App-only (never the Mojang
launcher). That used to mean worldgen and world-creation changes were "live-eyeball only" — no automated
check, ship it and hope Peetsa notices a problem. This playbook is the alternative: most of what *looks*
like "you have to play the game to know" is actually a deterministic function or a real-but-headless server
run, and both are checkable by an agent with zero human-in-the-loop. Use this BEFORE concluding "this needs
a live test" — it usually doesn't, or only a small residue does.

The core insight: **worldgen is math that happens to run during a game, not the game itself.** Pull the math
out and call it directly; boot the real server engine without a window when you need integration; reserve
"ask Peetsa to look" for genuinely subjective or rendering-only questions.

## The four techniques, cheapest first

### 1. Read the source (always do this first)
Half of "regressions" are just a wrong line of code, and reading it is more reliable than eyeballing the
result in-game. Before building any check, find the exact code path and read it like a reviewer:
- Does the value/flag actually reach where it's supposed to (e.g. does a UI checkbox's boolean actually flow
  into the `WorldOptions` constructor, or does some other code path silently use a different object that
  doesn't carry it)?
- Are there leftover strings/wording from before a rename (`grep -rn` the literal old word across `src/`)?
- Is the constant/threshold the value you think it is, right now, in this file — not what a memory or
  changelog claims it is?
Cite file:line. If you can quote the exact line that does the right (or wrong) thing, you've proven it —
no build required.

### 2. Call the actual function directly (deterministic logic)
If a behavior is decided by a `public static` (or otherwise reachable) pure-ish function — given the same
inputs it always returns the same answer — you can sweep its input space and tabulate the answers instead of
observing the result live. This project's alpine snow-cap logic is the canonical example:
`LatitudeBiomes.alpineSurfaceKind(blockX, blockY, blockZ, radius)` decides snow/rock/unchanged. Instead of
flying to a mountain in five latitude bands and looking at the snow line, a dev-only audit mode called that
SAME compiled method ~100 times per (latitude, altitude) cell and built the onset table directly:
```
band          onsetY  snow%@Y[166 170 174 178 182 186 190]  belowY168
TEMPERATE        174    0   0  50 100 100 100 100           clean
```
**Critical discipline:** call the REAL compiled method, never a from-scratch reimplementation of the spec in
a script. A Python re-derivation only proves you can read the comment; calling the actual code catches a
typo, an off-by-one, or a stale comment. If the function isn't `public`, that's fine — it's still reachable
from a dev-only class in the same package/module, or via a thin public wrapper added for the audit.

**Turnkey path — no new Java needed (try this FIRST):** `tools/dev/run_probe.sh` wraps a generic, reusable
dev-only mode (`BiomePreviewHeadlessRunner`'s `latdev.probe`) that reflectively sweeps ANY static method
across a parameter grid and writes a CSV. It boots the same real globe world the alpine audit used, calls
`setWorldSeed`/`setActiveRadiusBlocks` automatically if the target class exposes them (LatitudeBiomes does),
and invokes the real method via reflection — so you get the "call the actual compiled logic" guarantee
without writing a bespoke `runXAndStop` method first. Example (this reproduces the alpine table above):
```
tools/dev/run_probe.sh \
  --target com.example.globe.world.LatitudeBiomes#alpineSurfaceKind \
  --types  int,int,int,int \
  --names  x,y,z,radius \
  --grid   "x=0..3000:30;y=160..192:2;z=2333;radius=5000" \
  --out    run-headless/latdev/probe-report.csv
```
`--grid` entries are `name=spec` separated by `;`, where spec is a single value, a comma list, or a range
(`start..end` or `start..end:step`). Run `tools/dev/run_probe.sh` with no args (or read its header comment)
for the full flag list. Then read the CSV with a quick Python aggregation (see the snow-onset example in
`docs/binder/headless-live-test-20260630.md`) — don't eyeball raw rows for anything beyond a handful.

**Fallback — write a bespoke audit mode** only when the question needs more than "sweep a static method's
inputs and tabulate" (e.g. it needs to walk real chunk/terrain state, aggregate across many objects, or do
something the reflection probe's primitive-only param types (`int|long|double|float|boolean`) can't express).
See `BiomePreviewHeadlessRunner.java`'s mode-dispatch pattern (`onServerStarted` checks a system property like
`-Dlatdev.alpineAudit=true` and routes to a `runXAndStop` method) and add a new property-gated mode there,
wiring the trigger as a `vmArg` on the relevant Loom run-config in `build.gradle` (see `biomePreview`).

Either way, keep ALL audit/probe code under `com.example.globe.dev` — that package is excluded from the
shipped jar (verify with `unzip -l <jar> | grep -c com/example/globe/dev/` → must be 0 before staging; a
dev-only addition should also leave the shipped jar's player-facing classes byte-for-byte unchanged).

### 3. Boot the real headless server (integration, real terrain, real placement)
For anything that needs the actual engine — real terrain heights, real surface painting, real feature/
structure placement, a real biome map — this project already has a dedicated-server dev harness:
`BiomePreviewHeadlessRunner` hooks Fabric's `ServerLifecycleEvents.SERVER_STARTED`, boots a genuine
`globe:*` world preset (no client, no window), does its work on the server thread, writes a report, halts.
This is the SAME generation code that runs when you play — just headless. Existing modes: biome atlas export
(`runBiomePreview`), seed search, band audit, and now the alpine snow audit. The "terrain-aware atlas" mode
is the same idea applied to biome selection: feed REAL terrain heights instead of a fast synthetic flat
sample, so terrain-correlated gates (e.g. "no plains on steep cliffs") actually fire and become visible in
the output PNG.

Use this when: the question involves real terrain shape, chunk generation to `ChunkStatus.FULL`, or anything
that a pure-function call (technique 2) can't reach because it depends on noise/terrain context that isn't
just lat/lon/Y.

Gradle invocation pattern (see `docs/binder/headless-live-test-20260630.md` for a worked example):
```
env -u JAVA_TOOL_OPTIONS JAVA_HOME=$(/usr/libexec/java_home -v 25) ./gradlew --no-daemon --console plain \
  -Platdev.preview.levelName=<name> -Platdev.preview.levelSeed=<seed> \
  -Platdev.preview.levelType=globe:<preset> \
  -D<your-trigger-property>=true \
  runBiomePreview
```
Caveat: a dedicated server load does NOT replay the client's world-creation flow (e.g. bonus-chest placement
is gated on `MinecraftServer.setInitialSpawn()`, which only fires for genuinely NEW worlds, and the
dedicated-server `server.properties` has no bonus-chest field to force it). Note that gap explicitly rather
than silently skipping it.

### 4. The headless atlas / map-proof (biome placement at scale)
For "is biome X over/under-represented, did this band-leak get fixed, does the new ramp give the latitude
spread we wanted" — render a top-down biome map (`tools/atlas/atlas_runner.py`) and measure it
programmatically (`tools/atlas/band_correctness_check.py`, `overrep_rank.py`, custom per-latitude binning
scripts). This is map-based proof (binder Art VI) — always prefer it over a verbal claim about distribution.
Add new invariant checks here (e.g. the arid-belt "floor" guard) so future regressions in either direction
get caught automatically, not just the one you happened to look for.

## What is genuinely NOT headless-checkable — say so explicitly
Be honest in your report about residue rather than implying full coverage:
- **Whether real-world terrain near a given spawn actually reaches an altitude threshold** — the logic can
  be proven (technique 2), but if no chunk near spawn generates a peak ≥ the threshold, you can't *show* the
  effect, only that it would fire if terrain reached there. Say this plainly instead of overclaiming.
- **The real client world-creation flow** (button clicks, the bonus-chest UI checkbox driving the actual
  client-side `createWorld` call) — only fires through the GLFW client, not a dedicated server.
- **Subjective/visual judgments** — "does this look right," "does the world feel wider," "is this compass
  pretty" — these are not proof targets at all; don't try to fake them with a metric.
List these as a short "needs Peetsa in-game (Modrinth App)" section at the end of any verification report,
distinct from the things you DID prove. Never call something "verified" when it was actually "the logic that
would produce verification is sound, but the precondition for it firing wasn't observed."

## Quick checklist for "can I avoid live-testing this?"
1. Is this just "does this value/flag reach this call site" → read the source (technique 1).
2. Is the decision made by a single function of inputs you can enumerate → call it directly (technique 2).
3. Does it need real terrain/chunk context → boot the headless server (technique 3).
4. Is it about distribution/representation across the map → render + measure the atlas (technique 4).
5. Only if none of those apply (pure rendering, pure feel, real client UI flow) → it's a genuine live-test
   item. Name it explicitly and move on; don't try to fake it.

See also: `docs/binder/headless-live-test-20260630.md` (worked example of all four techniques run in one
session), `docs/binder/worldgen-regression-prevention-20260625.md` (the validation-blind-spot retro that
motivated technique 4's tooling), `docs/binder/arid-belt-earthlike-20260625.md` (atlas-based map-proof
example with a regression-floor guard).
