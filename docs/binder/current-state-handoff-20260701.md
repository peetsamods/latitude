# Current-state handoff — 2.0 "Longitude", TEST 1 punch-list is the next gate (2026-07-01)

`status: active resume pointer` · `scope: release, rendering, worldgen` · `branch: feat/custom-biome-expansion-26.1.2`

> This is the "start here / resume here" pointer for the **2.0 line on the canonical 26.1.2 root**
> (`/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`). The general-repo `docs/HANDOFF.md`
> and `docs/LESSONS.md` live on the `main` worktree (`/Users/joolmac/CascadeProjects/Latitude (Globe)`, a
> pre-2.0 lineage); this feature branch has the active binder but no top-level handoff, so this dated note
> serves that role until the branches reconcile.

## Truth table
- **Version:** `2.0-beta.1+26.1.2` — public title **"Longitude"** (renamed from `1.4.1-beta.2`; see
  `version-bump-2.0-20260701.md` and [[latitude-versioning-convention]]).
- **Root / branch / HEAD:** `Latitude-custom-biome-expansion-26.1.2` / `feat/custom-biome-expansion-26.1.2` /
  `30db22fc`.
- **Test build:** `TEST 1.jar` staged in Modrinth profile `Lat 1.4+26.1.2` (jar sha256 `28686c02…`). Test
  jars are named `TEST N.jar` going forward — see [[test-jar-naming-convention]]. Modrinth App only, never the
  Mojang launcher — see [[no-mojang-launcher-modrinth-only]].
- **Push/publish:** nothing pushed; commits are local to the feature branch; no Modrinth upload/publication.

## Shipped this session (source-green, live-eyeball pending)
1. **2.0 "Longitude" rename** (`a476e86c`) — `mod_version` bump + CHANGELOG/binder-index retitles.
2. **Longitude reading on the compass** (`9a6e4e41`) — F9-togglable, alongside latitude.
3. **Atlas / World Shape toggle** (`30db22fc`) — create-world choice of **Mercator 2:1** vs **Legacy 1:1**,
   shape-aware atlas preview, `"PLANISPHERE"`→`"ATLAS"` label.
   Details for #2 and #3: `atlas-worldshape-longitude-20260701.md`.

TEST 1 confirmed the World Shape toggle **works end-to-end** (both shapes create playable worlds).

## → Next gate: the TEST 1 live-findings punch-list
`test1-live-findings-20260701.md` catalogs ~11 findings from Peetsa's 2026-07-01 TEST 1 session, each with
repro, code pointer, and suggested priority. That punch-list — not a release step — is the next work. It spans
create-screen polish (Legacy atlas should be a **square**, atlas "looks like a flag", cramped latitude labels,
left/right layout question, recreate-world blank seed), a loading-message randomization/70%-bias request, and
worldgen/structure/perf items (savanna village in a temperate zone, **bog village → forbid**, generation lag,
E/W border storm should be climate-aware, incomplete `/latdev` tester command set).

## Notion dependency (blocked this session)
Two punch-list items reference **@Notion** for authoritative prior state — the full historical `/latdev`
command list, and prior context for the village-biome audit. The Notion MCP connector is **not authorized in
this session**, so those two could not be cross-referenced; they are flagged in the punch-list as
Notion-blocked. Re-authorize Notion (claude.ai connector settings / interactive `/mcp`) before closing them.

## Boundaries (unchanged)
Savepoint/tag/push, Modrinth upload/publication, public version-name finalization, and 1.21.11/1.21.1/1.20.1
backports of the 2.0 line remain separate Peetsa-owned gates. Do not treat "source-green" or "TEST 1 works" as
release authorization.
