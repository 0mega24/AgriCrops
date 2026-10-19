# Stage 8 — Instability and weeds

## Goal

Turn genetic instability into a legible risk/reward system rather than a surprising crop deletion
timer.

## Current implementation

- Per growth tick, positive Growth plus Mutation Chance minus positive Resilience produces risk.
- A successful risk roll begins a timed destabilizing state with particles and an OC signal.
- Any right-click cures it and reduces Growth and Mutation Chance.
- Expiry converts the crop to a built-in aggressive weed while carrying the old standard genome.
- Weeds require a rake for normal harvesting and may overtake adjacent plants.

## Work plan

- [ ] Confirm whether weeds represent genetic collapse, invasive plants, contamination, or multiple
  systems; align names and visuals accordingly.
- [ ] Define risk units and expected time-to-event at representative genomes.
- [ ] Decide whether risk rolls on attempted ticks, successful growth, wall time, or resource stress.
- [ ] Make warning strength/time remaining visible through animation, sound, analyzer, comparator,
  scanner, and OC as appropriate.
- [ ] Replace “right-click with anything” with a deliberate cure interaction if the design expects
  tools/resources.
- [ ] Define cure costs, prevention, quarantine, field spread, recovery, and crop-stick damage.
- [ ] Ensure unloaded chunks and sparse random ticks cannot create inconsistent expiry behavior.
- [ ] Decide whether pack-defined weeds can be conversion targets and how they are selected.
- [ ] Test weed takeover probabilities and performance in dense fields.

## Exit criteria

Players can predict, detect, prevent, cure, and recover from instability; risk is proportional to a
valuable breeding choice; and no silent/unfair loss path remains.

