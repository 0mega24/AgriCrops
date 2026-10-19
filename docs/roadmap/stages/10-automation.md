# Stage 10 — Field automation and OpenComputers

## Goal

Provide bounded, useful automation with both sensing and real actuation. A boolean named
`autoWater` is not automation unless a system consumes it and spends a resource.

## Current implementation

- Each crop stick can expose an OpenComputers component and shared analyzer-style methods.
- Crop sticks emit growth, harvest, death, and destabilization signals.
- Crop sticks persist `autoWater` and `autoFertilize` booleans.
- No watering/fertilizing device reads those booleans.
- The previous AgriCraft channel/tank/sprinkler system was removed.
- The scanner performs synchronous cuboid tile scans and offers read-only calls.

## Required design

- Decide whether automation uses physical irrigation, a controller network, robots, adjacent
  devices, or direct OC calls.
- Define energy, fluid, fertilizer, item, range, bandwidth, chunk-loading, and security costs.
- Define behavior without OpenComputers and compatibility with other automation mods.
- Separate permission/configuration flags from commands that perform actions.

## Work plan

- [ ] Implement a water actuator that moves a defined amount from a real source into soil.
- [ ] Implement a fertilizer actuator that consumes registered fertilizer/items.
- [ ] Add harvesting and planting only if approved by product scope.
- [ ] Make scanner/controller commands operate on explicit coordinates with range and ownership
  checks.
- [ ] Add alarm/event subscription behavior without requiring one network node per crop stick where
  that would be too expensive.
- [ ] Version and document the OC API, result schemas, errors, and rate limits.
- [ ] Benchmark maximum scanner area and dense-field event traffic.
- [ ] Provide example programs that are executable content or clearly packaged documentation.
- [ ] Add integration tests with OpenComputers installed and absence tests without it.

## Exit criteria

A survival-built controller can keep a bounded field watered/fertilized by consuming real
resources, respond to alarms, survive reloads, and cannot act outside its authorized range.

