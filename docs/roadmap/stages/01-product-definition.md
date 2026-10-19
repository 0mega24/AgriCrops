# Stage 1 — Product definition and design recovery

## Goal

Make the intended game, audience, and boundaries explicit before treating the implementation
sequence as a roadmap. This stage owns the meaning of “Agricrops,” not Java architecture.

## Current state

- Source comments cite a missing `crop-mod-design.md` through at least sections 2–12.
- `docs/product-design.md` now provides a repository-owned replacement baseline.
- The root README now describes this fork and its pre-release limitations honestly.
- Decision 0001 selects a standalone farming mod with optional pack authoring, pending owner
  acceptance.

## Required decisions

- [x] Choose: standalone mod with an optional authoring layer.
- [x] Define the player fantasy and the first 30 minutes, 5 hours, and late-game loop.
- [x] Define genetics as predictable at the rule level, probabilistic per offspring, and
  progressively discoverable.
- [x] Define supported optional integrations and what remains useful without them.
- [x] State that upstream/fork save compatibility is unpromised until a migration matrix exists.
- [x] Define non-goals to prevent every registry idea becoming scope.

## Deliverables

- [x] Write a repository-owned replacement for the missing design document.
- [x] Add a glossary for plant, species, genome, stat, allele, locked attribute, mutation, spread,
  soil profile, soil column, harvest tier, discovery, scanner, and actuator.
- [x] Add player journeys and concrete examples rather than only formulas/APIs.
- [x] Map each design promise to one roadmap stage and one acceptance test.
- [x] Replace the upstream README with an honest Agricrops overview and status statement.
- [x] Record the product-mode decision in the repository decision log.
- [x] Record owner removal of the external simulator and the Java/in-game/CT direction in Decision 0002.
- [ ] Review concrete expanded mechanics and presentation proposals in the Agricrops vault contracts.

## Exit criteria

- A new contributor can explain what the base jar does without CraftTweaker or OpenComputers.
- Every headline feature has a user-visible outcome and an explicit non-goal.
- The owner approves the document as the source of truth.
- Master-tracker statuses are reconciled against the approved design.

All repository deliverables are present. Owner acceptance of `docs/product-design.md` and Decision
0001 is the remaining exit gate; until then Stage 1 remains Functional rather than Verified.
