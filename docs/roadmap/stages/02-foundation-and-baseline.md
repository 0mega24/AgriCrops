# Stage 2 — Foundation and playable baseline

## Goal

Turn the repaired hard fork into a stable, honest baseline on which gameplay work can be measured.

## Work already represented

- Package/mod-id hard fork to `agricrops`.
- RetroFuturaGradle/Gradle 8.9 build migration and CI configuration.
- Vendored AgriCore boundary and prebuilt InfinityLib dependency.
- Headless server-load verification.
- Crop-stick placement repair and removal of the upstream irrigation block family.
- Source registration for nine baseline plant definitions and one weed target.

## Remaining work

- [ ] Finish and document real client QA: launch/create world, place crop sticks and plant are
  verified; breaking, every growth stage, harvest, reload/reconnect, and inventory review remain.
  render every stage, harvest, reload world, and reconnect.
- [ ] Inventory every retained upstream feature and classify it as supported, deprecated, or dead.
- [ ] Remove or quarantine obsolete irrigation configuration, recipes, translations, and worldgen.
- [ ] Decide whether legacy JSON loading is supported; if not, remove rather than leave a
  misleading half-compatible pipeline.
- [ ] Audit the hard-fork rename for old AgriCraft names in UI, commands, metadata, docs, and APIs.
- [ ] Give every survival-facing block/item a recipe or explicitly mark it creative/debug-only.
- [ ] Define a minimal built-in content set based on gameplay roles, not available texture files.
- [x] Add initial smoke tests for mutation child resolution, resources, genome NBT, and fresh
  species templates. Broader Forge-registry and adapter coverage remains a release gate.

## Exit criteria

- Client and dedicated server both pass a written smoke-test checklist.
- No exposed configuration refers to removed functionality.
- Every registered item/block is obtainable as intended.
- The base installation has a coherent early-game loop even without optional integrations.
- CI builds the distributable and runs nonempty tests.
