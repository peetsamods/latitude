# HANDOFF — Backport 1.4 worldgen (Stage 1) to 1.21.11 / 1.21.1 / 1.20.1 as `1.4.0-beta.1`

**Author:** prior session (2026-06-06), for an autonomous overnight thread. **No human input available — do not block on questions; make sensible documented defaults and keep going.**
**Canonical repo:** `/Users/joolmac/CascadeProjects/Latitude (Globe)`. **Declare workflow `latitude-regression-fix-workflow`.** Read `[[latitude-governance]]`, `[[latitude-versioning-convention]]`, `[[1p4-release-status-handoff]]` (memory) before starting.

---
## 0. Mission & autonomy contract

Port **only Stage 1 = the 1.4 "Cohesive Horizons" WORLDGEN pass** ("Bucket 1") to the three older Minecraft versions, ship-prep each as **`1.4.0-beta.1+<mc>`** (Modrinth **Beta** channel — but see §7: publishing is deferred to Julia). Order: **1.21.11 first, then 1.21.1, then 1.20.1** (easiest → hardest).

**You MAY (no input needed):** create branches/worktrees off the per-version baselines; transplant worldgen logic; switch JDK per version; compile; run the headless atlas + metric scripts; commit; **push the feature branch**; write binder evidence rows + a Notion Running Log entry; write reports.

**You MUST NOT:** touch `main` or `feat/1.3.1-cohesive-horizons-26.1.2`; push **tags**; create GitHub **releases**; upload to **Modrinth**; force-push; delete others' branches/worktrees. Leave those for Julia (§7).

**Anti-Spiral (Art X):** STOP after **2 failed attempts** on any single blocker. Do not thrash. Document the blocker precisely, leave that version in a clean committed state, and **move on to the next version**. A documented partial is worth far more than a broken push.

**Proof-gate everything:** never claim a version "done" without the headless-atlas metrics meeting §5 targets. If you can't prove it, it isn't done — say so.

---
## 1. Scope — what Stage 1 INCLUDES vs EXCLUDES

**INCLUDE (Bucket 1 — worldgen logic, mostly version-agnostic Java in `LatitudeBiomes.java` + `ProvinceAuthority.java`):**
- **Warm-band rebalance** (commit `8c73beab`, tag `save/warm-band-rebalance`): `ProvinceAuthority.classifyWarm` Earth-analog latitude wet-bias (`TROPICAL_WET_BIAS = 0.20`, `TROPICAL_LAT_END_DEG = 23.5`); WARM_WET variety preservation in `enforceWarmProvinceFamily` (keep jungle + custom + swamp/mangrove, not monoculture); tier coherence `TIER_COHERENCE_BLOCKS 64→160`, `FALLBACK_COHERENCE_BLOCKS 64→128`; badlands sub-province widen + `chooseBadlandsVariant` coherent `ValueNoise2D` (scale 384).
- **Tropical equator overhaul** (commits `91ffa425` + `10e10ffd`): `demoteEquatorialBadlands` (badlands→savanna below smoothstep ramp `BADLANDS_LAT_RAMP_LOW_DEG=10`→`HIGH=18`, coherent keep-noise `BADLANDS_LAT_KEEP_SALT`) and `demoteEquatorialDesert` (PARTIAL: `DESERT_EQUATOR_KEEP_FRAC=0.40`, ramp to keep-all by `DESERT_LAT_RAMP_HIGH_DEG=12`), both applied at the END of `applyFinalSavannaClimateClamp` (both overloads), AFTER the `enforceWarmProvinceFamily` rewrite, BEFORE the savanna-tier pass.
- **Polar ice_spikes cap** (commit `3f8e0366`, tag `save/polar-ice-spikes-cap`): coherent cap in `sanitizeLandBiome` (`keepPolarIceSpike`, `POLAR_ICE_KEEP_THRESHOLD=0.45`), convert excess to `snowy_plains`.
- **Art VI `pickFrom` coherent noise** (commit `da1bc0dd`, tag `save/artvi-pickfrom-coherent-noise`): replace floorDiv+hash64 cell pick with argmax over N coherent `ValueNoise2D` fields.

**EXCLUDE (Bucket 2 — defer to a later full-1.4 stage):**
- Custom-biome source-wrap mixins (`ChunkGeneratorBiomeSourceMixin`, `…PopulateBiomesMixin`, `BiomeSourceAccessor`) + BoP/Terralith/Promenade tags. **Do NOT port these in Stage 1.**
- World-entry client (render-gate overlay, early-spawn, entry-title hemisphere). **Exclude.**
- The dev auto-create-world probe (already dev-only; not relevant).

Source of truth for the exact algorithm: `git show <commit>` on `feat/1.3.1-cohesive-horizons-26.1.2`, plus `docs/design-spec.md` §2.4 and the binder reports `docs/binder/tropical-equatorial-{badlands-gate,desert-thinning}-report-20260606.md`.

---
## 2. The mapping wrinkle (critical)

26.1.2 (where 1.4 was authored) uses **official Mojang mappings**; **all three targets use Yarn**. So you canNOT cherry-pick the 26.1.2 commits. Instead **transplant the algorithm** into each target's existing (Yarn-based, 1.3.0) `LatitudeBiomes.java` / `ProvinceAuthority.java`:
- The pure-logic parts port near-verbatim: `ValueNoise2D`, `smoothstep`, biome **string IDs** (`"minecraft:savanna"` etc.), the demote helpers, the moisture math, the constants. These don't depend on mappings.
- Only the thin MC-API touchpoints differ (`Holder`/`RegistryEntry`, `Registry`, biome lookup). The target branch's 1.3.0 code already shows the correct Yarn forms — mirror them.
- Diff each target's `LatitudeBiomes`/`ProvinceAuthority` against the 26.1.2 versions to see what 1.4 logic is missing, then add it using the target's existing idioms.

---
## 3. Per-version baselines & toolchains (verified 2026-06-06)

| MC | Baseline (verify first) | Mappings | JDK (`org.gradle.java.home`) | Notes |
|---|---|---|---|---|
| **1.21.11** | latest tag `v1.3.0+1.21.11-r2` (no clear branch — branch off the tag; also check `origin/fix/latitude-band-scaling-1.21.11`) | Yarn | 21 | Closest to 26.1.2 API. Start here. |
| **1.21.1** | `port/1.3.0-1.21.1` (worktree `/Users/joolmac/CascadeProjects/Latitude-litho-compat-1.21.1`); confirm it's the latest 1.3.0 head vs the `hotfix/1.21.1-*` / `save/1.21.1-*` tags | Yarn (1.21.1+build.3) | 21 | loader 0.17.3, fabric 0.115.6 |
| **1.20.1** | `port/1.3.0-1.20.1` @ `189054a1` = `v1.3.0+1.20.1-r1` (worktree `/Users/joolmac/CascadeProjects/Latitude-issue5-1.20.1`) | Yarn (1.20.1+build.10) | 17 | loader 0.16.14, fabric 0.92.6. HARDEST: refmap regime (`latitude-refmap.json`); older API. Attempt last; expect to hand back to Julia. |

**Always branch off the verified baseline** into a NEW branch `port/1.4.0-beta-<mc>` (e.g. `port/1.4.0-beta-1.21.11`). Use a fresh git worktree per version so builds don't collide. Confirm each `gradle.properties` for the JDK/mappings before building; use that JDK (`env -u JAVA_TOOL_OPTIONS JAVA_HOME=<jdk>`). Do NOT assume temurin-25 (that's the 26.1.2 line only).

---
## 4. Per-version procedure

For each version (in order):
1. **Verify baseline** (latest 1.3.0 release head for that MC); create worktree + branch `port/1.4.0-beta-<mc>`.
2. **Bump version:** `gradle.properties` `mod_version = 1.4.0-beta.1+<mc>`.
3. **Transplant** the §1 INCLUDE logic into `LatitudeBiomes.java` + `ProvinceAuthority.java` (and any helper the 1.4 logic needs), adapting to that version's Yarn idioms (§2). Keep it a clean diff; don't drag in Bucket 2.
4. **Compile:** `env -u JAVA_TOOL_OPTIONS JAVA_HOME=<jdk> ./gradlew compileJava --console=plain`. Iterate to green.
5. **Validate (§5).** If targets miss, debug ONCE more (Anti-Spiral: max 2 attempts), else document + move on.
6. **Build:** `./gradlew build` → jar in `build/libs/`. (1.20.1/1.21.x produce a remapped jar + refmap — verify refmap present, unlike 26.1.2.)
7. **Savepoint:** commit (narrow), local annotated tag `save/1.4.0-beta-worldgen-<mc>` (do NOT push the tag), **push the branch**. Add a binder evidence row.
8. Move to next version.

---
## 5. Proof method & targets (map-based, per version)

Use the headless atlas (NO chunk pregen): `env -u JAVA_TOOL_OPTIONS JAVA_HOME=<jdk> ./gradlew runBiomePreview --console=plain --args="--latdevBiomePng --seed=<S> --size=small --radius=7500 --step=16 --emitbiomeindex=true --out=/tmp/<dir>"` then `python3 tools/atlas/sublatitude_dry_wet.py <stepdir>` and `python3 tmp/tropical-badlands-gate-ace6e326/perbiome_sublat.py <stepdir>` (copy that script into each worktree, or run from canonical). Biome index = `biome_ids.png` red channel + `biome_palette.json` (NOT natural `biomes.png`).

Validate on **2 seeds** (e.g. `2533348776566713405` + `1199119911991199`). **Targets** (qualitative — do NOT expect byte-identical to 26.1.2, different MC/biome set):
- 0–5° badlands ≈ **0%**; deep-equator desert thinned (~halved) vs the pre-port baseline.
- Subtropical arid belt (20–35°) badlands/desert **broadly unchanged** (the gate is off there).
- Equator stays **jungle-dominant, not a monoculture** (jungle not inflated to ~>80%; variety present).
- Whole-world: demoted badlands/desert ≈ converted to savanna; **no biome vanishes; inventory count stable** (Art X).
- Confetti reduced (coherent patches) from the tier-coherence bump.
Capture before/after numbers in the binder report. If a target can't be met, that's a STOP+document, not a force-through.

---
## 6. Governance / logging

- Narrow per-version savepoints (commit + local `save/*` tag). Binder evidence row per version (`docs/binder/evidence-registry.md`, id `YYYYMMDD-1.4-beta-backport-<mc>`, enums, `commands`+`tmp_path`) + a short report `docs/binder/1.4-beta-backport-<mc>-report-<date>.md`.
- One Notion **Running Log** entry at the end summarizing all versions attempted/landed/blocked (page id `bac96324-aef5-4b1a-83d0-3799ea91af2a`, newest-on-top, template format). Touch Commit Index only for branches actually pushed.
- Versioning per `[[latitude-versioning-convention]]`: `1.4.0-beta.1+<mc>`, Beta channel, no "preview" string.

---
## 7. Definition of done + what to leave for Julia

**Per version, "done" =** branch `port/1.4.0-beta-<mc>` pushed, compiling, headless-atlas-validated to §5 targets, jar built (`1.4.0-beta.1+<mc>`), local `save/*` tag, binder evidence row.

**Leave for Julia (do NOT do autonomously):** pushing release tags; creating GitHub **pre-releases** (Beta); **Modrinth** uploads + Beta-channel listing; the real-GPU render check (N/A for Stage 1 anyway). Write her a crisp "morning report" (a top-level `docs/porting/1.4-beta-backport-RESULTS.md`): per version — status (landed/partial/blocked), branch, jar path + SHA-256, key metric deltas, and exact next action.

**Realistic expectation:** 1.21.11 is the likely clean win; 1.21.1 probable; **1.20.1 may hit the Yarn/refmap wall — attempt last, and if blocked after 2 tries, document precisely and stop.** Better to deliver 1–2 solid validated betas + a clear 1.20.1 blocker writeup than to thrash all three.
