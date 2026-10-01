# Version naming

Latitude release labels use `MAJOR.MINOR.PATCH-CHANNEL`:

- Increase major for breaking changes.
- Increase minor for backward-compatible features and reset patch to zero.
- Increase patch for backward-compatible fixes.
- Use `alpha`, `beta`, or `rc` for maturity. Do not add routine numeric channel counters.
- Another immutable build at the same maturity advances the version core according to its changes.
  A move from alpha to beta or release candidate may retain the same core.

Minecraft builds use the established `+<minecraft-line>` metadata, for example
`1.5.2-beta+1.21.1`. Metadata distinguishes artifacts without changing SemVer precedence.
The loader is identified separately by the artifact's mod descriptor and release listing.

Maintained builds share a product version only after the same supported behavioral contracts
are verified on their native Minecraft APIs. Platform-specific implementation details can differ.
Saved-world policy identifiers are compatibility contracts, not product release numbers; do not
renumber them merely to match another Minecraft line.

Published versions are immutable. Correct an existing release by publishing a new version.
