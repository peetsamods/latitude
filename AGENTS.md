# Latitude repository instructions

`AGENTS.md` is the repository policy authority for every human or automated
contributor. `CLAUDE.md` is its compatibility mirror. Keep the two files
byte-identical, and keep that policy text identical on every maintained branch.

## Privacy and legal safety

Treat every tracked file, Git object, branch, tag, commit or tag message, build
report, artifact, and public communication as potentially public.

Never place these categories in tracked content, Git metadata, names, artifacts,
or public communication:

- personal identity, email addresses, machine usernames, device names, absolute
  local paths, private world names, seeds, coordinates, or run identifiers;
- operator-only records, conversational chronology, temporary proof output,
  worlds, logs, crash output, profiler output, or local runtime state;
- credentials, tokens, cookies, authorization headers, private keys, or
  credential-bearing URLs;
- decompiled or extracted Minecraft source, mappings dumps, or proprietary
  reference material;
- third-party code, data, text, or assets without documented provenance and
  redistribution permission.

Use `maintainer` or `tester` for roles. Use `Peetsa` only when public authorship
is necessary. Cite decisions as `maintainer ruling, YYYY-MM-DD`. Store
operator-only material in the established external notes tree; do not record its
private location in this repository.

Before every commit, tag, push, release, publication, history rewrite, or
worktree rescue, run the private repository safety gate configured by
`repoSafety.scanner` and `repoSafety.patterns`. Stop if the gate or its private
pattern file is unavailable or returns nonzero. Never use `--no-verify`, weaken
a rule, or add an exception merely to make the current change pass. A passing
gate is evidence only; it does not authorize the action it checked.

## Repository preflight

Before editing, record:

    git rev-parse --show-toplevel
    git status -sb
    git branch --show-current
    git rev-parse HEAD
    git tag --points-at HEAD
    git config --get core.hooksPath

Existing or unexplained changes are protected. Do not edit, stage, restore,
stash, delete, overwrite, or absorb them. Stop when the root, branch, commit,
tags, hook wiring, or protected state differs from the task boundary.

Rehearse genuinely risky or difficult-to-recover operations in an isolated
sandbox before touching a live checkout, branch, remote, deployment, account,
dataset, or operator workflow. A successful rehearsal is proof, not authority.

## Product and proof authority

Read `README.md`, `docs/porting/PORTING.md` for version-migration work, and only
the design or release policy directly relevant to the task. Product and
contributor documentation belongs in Git. Operator process records do not.

Use the branch-native build and test tasks. Compilation, pure-math tests,
headless world generation, built artifacts, staged profiles, live runtime
binding, and human observation are separate proof surfaces. Never promote one
into another.

Before treating a JAR or source archive as shareable, verify its embedded
identity, provenance, licenses, archive contents, and complete privacy scan.

## Branches and worktrees

Use these conventions:

- port branch: `port/<minecraft>-<loader>`
- fix branch: `fix/<minecraft>-<loader>/<purpose>`
- compatibility branch: `compat/<minecraft>-<loader>/<purpose>`
- release branch: `release/<product-version>+<minecraft>-<loader>`
- worktree folder: `.worktrees/<loader>/<minecraft>/<product-version>`

Maintainer ruling, 2026-08-29: the product version is required in every
worktree folder name. This supersedes the former convention that omitted product
versions. Put the task purpose in the branch name rather than adding another
uncontrolled worktree suffix.

Keep one canonical checkout and one worktree for each genuinely active line or
task. Do not create a duplicate-purpose worktree. Record ownership and intended
lifetime outside Git. No worktree, branch, tag, or saved change is removed
automatically.

## External actions

Commits, tags, pushes, releases, publications, history rewrites, ruleset changes,
and deletions are separate authorization boundaries. Successful proof or one
authorized boundary does not authorize the next.
