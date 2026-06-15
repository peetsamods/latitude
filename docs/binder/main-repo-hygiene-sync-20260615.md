# Latitude main repo hygiene + archive sync (2026-06-15)

`scope: repo hygiene / continuity` · `status: complete` · `branch: main` · `head: 4fcdee31`

## Why this entry exists

This note records the local hygiene pass on `/Users/joolmac/CascadeProjects/Latitude (Globe)` after Julia rolled work into a fresh thread and narrowed scope to repo sync/cleanup only. The goal was not to resume worldgen work. The goal was to get `main` into a clean, understandable state, preserve evidence safely outside the repo, and leave a trustworthy handoff surface behind.

## What happened

1. **Re-anchored the repo before mutation.** The root was `/Users/joolmac/CascadeProjects/Latitude (Globe)` on `main`. Pre-pull HEAD was `7f55a30f`. There were no tracked modifications, but `git pull --ff-only` was blocked by three untracked files in `run-headless/world/`.
2. **Archived only the pull blockers with Julia approval.** The three blocking files were moved to `/Users/joolmac/CascadeProjects/Latitude-Globe-main-pull-blockers-20260615-103718/`:
   - `level.dat`
   - `level.dat_old`
   - `session.lock`
3. **Fast-forwarded `main`.** After the blockers were moved, `git pull --ff-only` succeeded and advanced `main` from `7f55a30f` to `4fcdee31`.
4. **Classified the remaining untracked groups.**
   - `Manual atlas/` was identified as exported atlas proof bundles copied from headless runs.
   - `run-headless-base/` was identified as a local headless server/world snapshot with logs, world data, and a secret-bearing `server.properties`.
   - `docs/` was split into real Latitude binder notes versus stray Altitude material.
5. **Moved non-canon Altitude docs out of Latitude.**
   - The older Latitude copy of `altitude-mod-roadmap-20260608.md` was preserved at `/Users/joolmac/CascadeProjects/Altitude/docs/binder/from-latitude-globe-20260615/altitude-mod-roadmap-20260608.md`.
   - Its PDF companion moved to `/Users/joolmac/CascadeProjects/Altitude/docs/binder/from-latitude-globe-20260615/altitude-mod-roadmap-20260608.pdf`.
   - `lessons-from-altitude-into-latitude-20260608.md` moved to `/Users/joolmac/CascadeProjects/Altitude/docs/binder/lessons-from-altitude-into-latitude-20260608.md`.
   - The local `tree-line-altitude-report-20260608.md` copy was removed only after confirming it byte-matched Altitude's tracked copy.
6. **Archived local proof/runtime folders out of the repo.**
   - `Manual atlas/` moved to `/Users/joolmac/CascadeProjects/Latitude-Globe-manual-atlas-archive-20260615/`.
   - `run-headless-base/` moved to `/Users/joolmac/CascadeProjects/Latitude-Globe-run-headless-base-archive-20260615/`.
7. **Left the binder in place.** Julia confirmed `docs/binder/` is the running log of Latitude work and should stay in the repo rather than being archived away as dirt.

## End state

- Root: `/Users/joolmac/CascadeProjects/Latitude (Globe)`
- Branch: `main`
- HEAD: `4fcdee31`
- Tracked diff: none
- Remaining untracked surface: `docs/binder/` only, by design

## Evidence status

- The atlas archive preserves 46 files / 6.3M of proof material across two historical exports.
- The `run-headless-base` archive preserves 57 files / 3.1M of local runtime/world material.
- No tracked repo files were modified during the archive moves themselves; the content moves were for untracked local material.

## Boundary / next

This hygiene pass does **not** reactivate any older implementation lane. The current open product question remains the structural one recorded in `1211-province-gap-and-structure-question-20260614.md`: whether 1.21.11 is a backport to reconcile from canonical 26.1.2, or whether the repo's source-of-truth structure needs a deliberate rethink. Until Julia chooses, the correct state is clean `main`, live binder, and no implied follow-on mutation.
