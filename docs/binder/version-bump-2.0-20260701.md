# Version rename: 1.4.1-beta.2 → 2.0-beta.1 (2026-07-01)

`status: decided` · `scope: release, governance` · `decided by: Peetsa, 2026-07-01`

## Decision
`mod_version` (gradle.properties) renamed from `1.4.1-beta.2+26.1.2` to `2.0-beta.1+26.1.2`. Everything else
(`fabric.mod.json`'s `${version}` template, the build jar name, the manifest `Implementation-Version`) derives
from this single property and needed no separate edit.

## Rationale (Peetsa's reasoning, session discussion)
- **Mercator is a new world type**, not a tweak — a structural, user-facing change of the kind flagship mods
  usually reserve for a major version, not a patch release.
- **The custom-biome integration system** (`globe:lat_*` tag admission layer) and the **tropical-arid geography
  law overhaul** are each substantial enough to be their own minor release; together with the arid-belt
  widening, polar Earth-like tree line, province-wavelength/contiguous-region work, alpine smoothing, and the
  headless-verification tooling landed this session, the aggregate reads as a new era, not a bugfix train.
- **The accidental partial `1.4` push to GitHub** (an unintentional early publish, not a real release) means a
  version bump also cleanly separates "that leaked partial build" from "the real, tested release" — a
  communication benefit strict semver math doesn't capture on its own, but a legitimate reason in its own
  right.

## What changed, what didn't
- Changed: `gradle.properties` `mod_version`; `CHANGELOG.md` top-level section header (`## Latitude 1.4 —
  Cohesive Horizons` → `## Latitude 2.0 — Cohesive Horizons`, plus the stray embedded `1.4.1` version numbers
  in subsection labels and the "Historical released entries" footer); `docs/binder/index.md`'s stale
  "Current 1.4.1 Candidate Notes" section retitled to mark it explicitly historical (it's pinned to savepoint
  `c9da0f93`, ~16+ commits behind current HEAD, and was already stale independent of the version number).
- Deliberately NOT rewritten: dated evidence docs (`docs/release/1.4.1-prerelease-punchlist.md`,
  `1.4.1-overnight-summary-20260622.md`, `world-creation-bookui-impl-spec-20260622.md`) — these are timestamped
  records of decisions made when the version was still called that; rewriting them would be revisionist.
  `docs/release/checklist.md`, `current-gates.json`, and `modrinth-description-1.4.md` are release-readiness
  snapshots pinned to the same stale `c9da0f93` savepoint and explicitly gated behind Julia's sign-off in their
  own text ("Julia chooses the public version name") — they need a fresh readiness pass regardless of version
  number, not a mechanical string swap. Do not treat them as current.
- Git tags: the already-pushed `save/1.4.1-mercator-worldgen-headless-26.1.2` tag is NOT renamed (rewriting a
  pushed tag is a destructive, generally-bad-practice operation on shared history) — it correctly describes the
  state it was created at. New tags going forward use the `save/2.0-beta.1-...` convention.

## Carry-forward
The versioning convention's core rule stands unchanged: **a version number must mean the same feature set on
every MC version** — see [[latitude-versioning-convention]] for the multi-MC-version backport pattern
(`<modver>-beta.N+<mc>` staged, promoted to `<modver>+<mc>` at parity). That pattern now applies under the
`2.0` banner instead of `1.4` for any future port to 1.21.11/1.21.1/1.20.1.
