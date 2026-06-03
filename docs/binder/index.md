# Latitude Documentation Binder

`status: active`

## Purpose
Provide one canonical map for Latitude documentation, record-keeping, and proof evidence links.

## Global freshness policy
- A binder entry is `stale` when it exceeds the stale thresholds in `update-contract.md`.
- Stale sections block savepoint/release progression until refreshed with valid evidence.
- Canonical docs are never replaced; they are deprecated with `supersedes` in the evidence registry.

## Canonical topic map (single source of truth)

| Evidence topic | Canonical source-of-truth doc | Allowed sibling sources | Default scope | Last reviewed | Freshness status | Next review due |
| --- | --- | --- | --- | --- | --- | --- |
| worldgen policy and biome behavior | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/biomes-coverage.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/biomes-coverage.md) | worldgen | 2026-06-02 | active | 2026-06-16 |
| rendering and HUD invariant behavior | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/first-load-message.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/first-load-message.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/release/checklist.md) | rendering | 2026-05-31 | active | 2026-06-14 |
| bug blaster investigation and closure | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/design-spec.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/design-spec.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/biomes-coverage.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/biomes-coverage.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/release/checklist.md) | compatibility | 2026-05-31 | active | 2026-06-14 |
| compatibility and migration status | [/Users/joolmac/CascadeProjects/Latitude (Globe)/tmp/audits/MIGRATION_STATUS.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/tmp/audits/MIGRATION_STATUS.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/tmp/audits/26-1-mixin-cluster-research](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/tmp/audits/26-1-mixin-cluster-research) | migration, compatibility | 2026-05-31 | active | 2026-06-14 |
| release and handoff readiness | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/release/checklist.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/release/checklist.md), [/Users/joolmac/CascadeProjects/Latitude (Globe)/CHANGELOG.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/CHANGELOG.md) | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/architecture/fix-history.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/architecture/fix-history.md) | release | 2026-05-31 | active | 2026-06-14 |
| live proof and runtime evidence | [/Users/joolmac/CascadeProjects/Latitude (Globe)/docs/binder/evidence-registry.md](/Users/joolmac/CascadeProjects/Latitude%20(Globe)/docs/binder/evidence-registry.md) | /tmp evidence payload folders listed in registry entries | release, worldgen, compatibility, rendering, migration | 2026-06-03 | active | 2026-06-17 |

## Binder sections
- `evidence-registry.md`: append-only list of proof and savepoint evidence.
- `evidence-record-schema.md`: required evidence-row contract and enums.
- `evidence-schema.json`: machine-readable schema for evidence rows.
- `update-contract.md`: triggers, stale debt handling, and savepoint gate directives.
- `notion-latitude-source-map.md`: Notion-to-binder source map and current gap ledger.

## Evidence ownership
- Project-wide owner: /Users/joolmac
- Scope owner per topic:
  - worldgen: gameplay/worldgen owner
  - rendering: client-rendering owner
  - compatibility: porting/interop owner
  - release: release coordinator
  - migration: migration engineer

## Stale status interpretation
- `active` means within policy windows and usable for savepoint/release checks.
- `stale` means the section must be refreshed before any new proof-complete savepoint.
- `superseded` means replaced by a newer evidence entry.
- `retired` means archival only, not used for current decisioning.

## Entrypoint rule
All binder-related updates must be reflected in `evidence-registry.md` and the relevant topic row in `index.md`.
