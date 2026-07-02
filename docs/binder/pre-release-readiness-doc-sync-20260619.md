# Pre-release readiness doc sync (2026-06-19)

`scope: docs coherence` · `status: recorded` · `repo: /Users/joolmac/CascadeProjects/Latitude (Globe)`

## Why this note exists

The release-readiness and scenic checklist surfaces had already been updated to reflect the final Latitude 1.4 / Minecraft 26.1.2 proof state, but the live handoff and binder index still lagged behind. This note records the docs-coherence pass that brought the current-truth docs in this repo back into alignment.

## Current truth recorded

- This checkout remains the main docs/history root for Latitude, not the active 26.1.2 candidate source root.
- The active Latitude 1.4 source of truth is `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2`.
- Canonical branch / HEAD recorded by the release checklist: `feat/custom-biome-expansion-26.1.2` / `e5d092ca7f09a397afc413137f62ea409566e1e7`.
- Candidate and profile jar SHA-256: `972159d1c825dff5803ff1e56eaa881a40c470c4a4e9f9640a06680856545b84`.
- Current readiness status: pre-release-ready proof, release not authorized.
- Final live evidence folder: `/Users/joolmac/CascadeProjects/Latitude-custom-biome-expansion-26.1.2/tmp/readiness-1.4-candidate-20260618-live-20260618-211834`.

## What changed in docs

- `docs/HANDOFF.md` was updated from a paused canonical-decision state to the current pre-release-ready proof state.
- `docs/binder/README.md` now lists this note first as the newest chronology entry.
- The release front door remains `docs/release/checklist.md`.
- The live scenic rerun checklist remains `docs/release/scenic-drive-green-checklist.md`.

## Boundaries preserved

This docs pass did not authorize or perform savepoint, commit, tag, push, upload, publication, cleanup, reset, stash, delete, restore, or live Minecraft control.

Publication, final version naming, release copy, and any savepoint/release actions remain Julia-owned gates.

## Resume pointer

Start from `docs/HANDOFF.md` for current state, then use `docs/release/checklist.md` and `docs/release/scenic-drive-green-checklist.md` for future reruns or release-prep verification.
