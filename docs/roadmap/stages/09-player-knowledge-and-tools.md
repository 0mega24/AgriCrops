# Stage 9 — Player knowledge, discovery, and tools

## Goal

Make the simulation playable without logs, source code, or mandatory computer scripting.

## Current implementation

- The inherited Seed Analyzer exposes standard stats and marks seeds analyzed.
- Journals store discovered plant IDs.
- A world-level ledger mirrors species learned through a journal.
- Crop sticks emit a growth-stage comparator signal.
- A Crop Scanner block can scan an area through OpenComputers.
- The scanner has no GUI and no recipe in the current resources.

## Work plan

Required presentation additions from Decision 0002 / the vault information-design contract:

- [ ] Define normal/expanded/analyzer/author information depth, semantic colors plus text and tone.
- [ ] Keep internal NBT out of ordinary UI; client-synced NBT is not a secrecy guarantee.
- [ ] Share discovery/reveal rules across item/model/probe/JEI/scanner/OC surfaces.
- [ ] Generate guide structure from definitions with authored localized hints and explanations.
- [ ] Diagnose layer/root reach, reproductive windows, seed origins/clippings and closed trait paths.

- [ ] Redesign analyzer output around the completed genotype/phenotype model.
- [ ] Show current soil, moisture, nutrients, environment, modifiers, risk, harvest tier, and reasons
  growth is slow/blocked.
- [ ] Define per-player, per-team, and global discovery ownership.
- [ ] Track mutation discoveries, parent clues, attribute discoveries, and stabilized lines.
- [ ] Make the journal teach the actual fork mechanics and update legacy translations.
- [ ] Give the Crop Scanner a survival recipe and useful non-OC interaction, or explicitly classify
  it as an OC peripheral and name/document it accordingly.
- [ ] Return structured, versioned scanner data including genome summary, soil state, habitat,
  harvest readiness, risk, and alarms.
- [ ] Add accessibility-conscious visual/audio cues and localized text.
- [ ] Define debug/admin inspection separately from normal discovery progression.

## Exit criteria

A player can diagnose a field, learn breeding goals, recognize a valuable offspring, and understand
failure without external documentation. Discovery persists with clearly defined ownership.

