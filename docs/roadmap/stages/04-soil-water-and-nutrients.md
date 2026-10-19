# Stage 4 — Soil, water, and nutrients

## Goal

Build a persistent resource simulation that creates understandable farming choices and remains
correct when the world changes.

## Current implementation

- Chunk capabilities store position-keyed soil columns.
- Columns persist depth, composition, nutrient capacity/current nutrients, and moisture.
- Registered block profiles provide composition, capacity, retention, and a growth multiplier.
- Successful growth drains nutrients; bonemeal refills nutrients.
- Nearby water tops up moisture and retention reduces decay.
- OC telemetry can read a created column.

## Known gaps

- Profile `growthMultiplier` is never applied to growth.
- Depth is stored but does not participate in behavior.
- Soil replacement does not invalidate/rebuild the existing position-keyed column.
- Automation flags do not refill resources.
- Fresh soil starts full of nutrients regardless of placement/history.
- Missing column fallbacks can temporarily report ideal nutrients/neutral moisture.
- Resource values are almost invisible without OpenComputers.
- All important balance constants are placeholders.

## Work plan

Decision 0002 requires actual layered modifiers, not only a depth field. The vault contract proposes
a bounded three-layer root zone with per-layer pools, root reach, transfer/conservation, exact-depth
hard requirements, capped soft contributions and cache invalidation. Detailed defaults await review.

- [ ] Implement the reviewed bounded layered root-zone contract with real resource ownership.
- [ ] Reconcile stored columns whenever their supporting block/profile changes.
- [ ] Apply the profile growth multiplier exactly once in the growth equation.
- [ ] Give each supported depth/composition an observable effect and player-tool explanation.
- [ ] Define initialization for naturally generated, player-placed, tilled, and transformed soil.
- [ ] Define nutrient inputs, depletion, leaching, regeneration, and capacity changes.
- [ ] Define moisture input, consumption, evaporation, rainfall, biome effects, and retention.
- [ ] Decide whether growth speed/yield increase resource cost explicitly.
- [ ] Add player tools/overlays for soil diagnosis before requiring automation.
- [ ] Add safe invalidation hooks and save migration for changed/removed profiles.

## Required tests

- Capability NBT round trip and chunk unload/reload.
- Soil block replacement updates composition/capacity without preserving impossible state.
- Profile multiplier, nutrients, and moisture each alter growth as specified.
- Values clamp correctly and never underflow/overflow.
- Cross-chunk and dimension behavior is correct.

## Exit criteria

Different soils create observable and balanced choices; every exposed profile field affects play;
and a player can diagnose and improve a poor field without reading source code.

