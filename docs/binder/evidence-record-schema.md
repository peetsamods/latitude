# Latitude Binder Evidence Record Schema

## Required evidence row fields

Every evidence row in `evidence-registry.md` must contain all fields below in the table row order.

1. `id`
2. `date`
3. `owner`
4. `trigger`
5. `scope`
6. `commands`
7. `result`
8. `tmp_path`
9. `canonical_docs_updated`
10. `next_action`
11. `status`
12. `supersedes`

## Canonical conventions
- `id` format: `YYYYMMDD-topic`  
  - Use suffix `-a`, `-b`, etc. only if multiple entries share the same date and topic.
- `date` is ISO date `YYYY-MM-DD`.
- `owner` is a short identifier (e.g., `lat-core`, `release`, `rendering`, `compat`).
- `trigger` is the exact event that required the evidence entry.
- `tmp_path` must be a repo-local path to the evidence payload folder.
- `canonical_docs_updated` must use absolute repo paths and list at least one canonical source path for completed work.
- `next_action` is required and should be actionable text or `none`.
- `supersedes` must be empty (`-`) for first row of a topic, and set to prior `id` for replacements.

## Enum constraints

### `result`
- `pass`
- `partial`
- `fail`
- `blocked`

### `scope`
- `worldgen`
- `rendering`
- `compatibility`
- `release`
- `migration`

### `status`
- `active`
- `stale`
- `superseded`
- `retired`

## Validation rules
- No enum drift: unknown values are invalid.
- No row overwrite: rows are append-only.
- Supersession:
  - do not edit existing rows
  - mark old row `status: superseded`
  - set new row `supersedes: <old-id>`
- Canonical path rule:
  - each topic row in the registry must reference the canonical source-of-truth doc defined in `index.md`.
- Freshness gate:
  - if any touched section is stale, savepoint/release is blocked until refreshed.
