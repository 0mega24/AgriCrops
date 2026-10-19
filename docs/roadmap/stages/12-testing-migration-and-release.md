# Stage 12 — Testing, migration, performance, and release

## Goal

Make releases trustworthy for long-lived modded worlds and large fields.

## Current state

- The repository builds successfully with RFG.
- Current source has 14 Java test methods; retained 2026-10-01 XML records 13 passing tests.
- The 2026-10-19 design audit did not run a new heavy Java build or Minecraft process.
- Headless server startup was previously documented as successful.
- Basic planting/rendering was recorded; the full client lifecycle/visual matrix remains open.
- Genome and soil formats exist without an explicit schema version/migration service.
- Version remains `0.1.0` despite large semantic changes.

## Work plan

- [ ] Create pure deterministic tests for formulas, inheritance, registries, validation, and NBT.
- [ ] Add Forge integration tests or scripted fixtures for crop ticks, block replacement, chunk
  reload, crossbreeding, harvesting, discovery, and optional-mod loading.
- [ ] Maintain a manual client acceptance matrix with screenshots/logs for rendering and UX.
- [ ] Version genome, crop-tile, soil-column, discovery, and pack-definition data.
- [ ] Implement migrations or explicitly reject unsupported saves with backups and clear errors.
- [ ] Benchmark crop ticks, water searches, scanner cuboids, capability serialization, and OC event
  traffic at realistic field sizes.
- [ ] Verify dedicated server with no client-only class leakage and with each optional dependency
  combination.
- [ ] Add release notes that distinguish source/API additions from completed player features.
- [ ] Package required licenses/notices and update metadata, README, changelog, and compatibility.
- [ ] Define semantic-versioning expectations for API, data, and saves.
- [ ] Prove Java phase-specific reproduction, regression/lockout/recovery, clippings/origins and
  layered soil through real lifecycle fixtures, not only isolated helpers.
- [ ] Qualify native vanilla/Pam/MA art/imports, fallback reports and source progression gates.
- [ ] Verify localized progressive tooltip/journal/tool depth and reveal policy across official
  UI/probe/JEI/OC. External simulator release gates are cancelled per Decision 0002.

## Release gates

- [ ] Clean checkout builds in CI.
- [ ] Nonempty unit/integration suites pass.
- [ ] Dedicated server reaches ready state and completes a scripted crop lifecycle.
- [ ] Client checklist passes on a supported Java/runtime environment.
- [ ] No critical/progression-blocking issue remains open.
- [ ] Built-in content progression is completable from a new survival world.
- [ ] Upgrade and rollback instructions are documented.
- [ ] Master tracker and every stage status match verified behavior.

## Exit criteria

A tagged artifact is reproducible, tested, migration-aware, accurately documented, and safe enough
to recommend for a persistent world.
