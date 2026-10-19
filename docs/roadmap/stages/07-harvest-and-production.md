# Stage 7 — Harvest, yield, and production

## Goal

Make plant growth stages and genetic investment create production choices instead of a single
wait-and-right-click loop.

## Current implementation

- Plants may expose multiple minimum-stage loot tiers.
- The highest eligible tier is selected at harvest.
- A tier may reset growth or leave the stage unchanged.
- Yield determines the number of loot-table trials.
- Plants without tiers use a mature-only compatibility tier.

## Known gaps

- Built-in content does not meaningfully demonstrate staged harvesting.
- Yield jumps by one whole loot roll every three positive points and ignores negative values.
- Growth speed, yield, water, nutrient cost, and weed risk do not form a fully designed economy.
- Product quality, quantity, seed production, and partial harvest behavior are not clearly separated.
- Harvest events exist in source but normal event posting is incomplete/disabled.

## Work plan

- [ ] Define distinct harvest archetypes: early forage, repeated picking, destructive final harvest,
  seed harvest, and quality-gated products.
- [ ] Specify how Yield affects amount, chance, quality, and seed output.
- [ ] Couple production to resource use and/or instability according to the approved design.
- [ ] Define partial harvest regrowth and prevent zero-cost repeated tier exploitation.
- [ ] Restore a supported cancellable/observable harvest event API.
- [ ] Show current eligible tier and next-tier benefit in player-facing tools.
- [ ] Add built-in crops that exercise each supported harvest archetype.
- [ ] Verify compatibility with automated farmers and item transport.

## Exit criteria

At least three production strategies are viable, staged harvesting cannot be trivially exploited,
and yield/resource/risk tradeoffs are documented and tested.

