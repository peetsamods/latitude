# Binder Update Contract

## Scope
This file defines when and how Latitude binder records are updated and when work is blocked for stale debt.

## Mandatory update triggers

Create or update evidence in `evidence-registry.md` when any of these occur:
- Code behavior changes in `worldgen`, `rendering`, `compatibility`, `migration`, or `release` scope.
- Savepoint creation attempt for a changed scope.
- PR merge or branch cut touching scope-related behavior.
- Release cut/retry.
- Stale refresh (manual or automatic).
- Notion sync/audit work that changes project-record understanding or closes a binder coverage gap.

## Evidence record requirements
- Evidence rows must include all fields in [`evidence-record-schema.md`](./evidence-record-schema.md).
- Evidence rows must also satisfy the machine schema in [`evidence-schema.json`](./evidence-schema.json).
- `id` MUST follow `YYYYMMDD-topic`.
- `result` and `scope` MUST use schema enums only.
- `commands` and `tmp_path` are required for all entries.
- `canonical_docs_updated` must include absolute repo paths to canonical docs and any other docs updated in the same slice.

## Savepoint workflow requirement
- Do not mark a proof-complete savepoint unless the relevant evidence row exists in `evidence-registry.md` with:
  - matching scope
  - result `pass` or `partial`
  - updated canonical docs list
  - explicit `next_action` if not final

## Supersession protocol
- Rows are immutable after write.
- Never overwrite prior rows.
- Replacements must:
  - create a new row with same scope/topic
  - set `supersedes` to old `id`
  - mark old row `status: superseded`
- Deprecated rows remain in registry for history and diffability.

## Freshness and debt rules
- Touched section stale threshold: older than 14 days.
- Global stale threshold: older than 30 days across the binder.
- When threshold is exceeded:
  - set section/entry status to `stale`
  - block savepoint/release actions until refreshed
- Global stale also requires `index.md` topic refresh before release.

## Canonical path consistency
- All binder references use absolute repo-local paths.
- `index.md` is the single source-of-truth index for topic-to-doc mappings.
- Evidence entries for a topic must not introduce a new canonical source; use the topic row in `index.md`.

## Section refresh procedure
1. Update the relevant entry row in `evidence-registry.md`.
2. Update `index.md` topic `Last reviewed`, `Freshness status`, and `Next review due`.
3. Set `status`:
   - `active` when refreshed within policy
   - `stale` when missing deadline
4. Recheck savepoint/release gate state.

## Review cadence
- Any touched area must be reviewed within 14 days.
- All sections must have an active review at least every 30 days.

## Bug Blaster protocol
- Treat Bug Blaster investigations as mandatory evidence-bearing events.
- Add a row before/at Bug Blaster handoff with:
  - `trigger` in the format `BugBlaster: <issue-or-topic>`
  - `scope` set to the actually affected domain (`worldgen`, `rendering`, `compatibility`, `migration`, or `release`)
  - `commands` and `result` reflecting harness/proof state
  - `tmp_path` pointing to the Bug Blaster evidence payload (screenshots, logs, notes, trace files)
  - `canonical_docs_updated` including impacted canonical source topics
- Do not mark Bug Blaster complete in workflow tooling without this row present.
