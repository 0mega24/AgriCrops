# Stage 5 — Environment and adaptation

## Goal

Make crop location and inherited adaptation meaningful across biome, elevation, light, and
temperature without creating opaque failure states.

## Current implementation

- Plants may specify biome dictionary tags and an elevation band.
- Biome match is binary; elevation tapers outside the preferred band.
- Their average modifies growth and Resilience softens the penalty.
- Legacy growth requirements expose min/max light.
- Light Tolerance and Temp Tolerance are stored but unused.

## Work plan

- [ ] Specify phenotype curves for light and temperature, including negative/zero/positive bipolar
  values.
- [ ] Separate environmental facts (current light/temp) from species preference and genetic
  adaptation.
- [ ] Decide how biome tags, rainfall, temperature, dimension, season mods, altitude, skylight,
  block light, roofs, and artificial lamps interact.
- [ ] Replace the unweighted biome/elevation average with a documented composition rule.
- [ ] Decide whether bad conditions prevent growth, slow growth, increase cost/risk, reduce output,
  or combine those outcomes.
- [ ] Make Resilience's scope explicit; avoid using it as a universal answer to every pressure.
- [ ] Expose current value, ideal range, each modifier, and final result through analyzer/UI and OC.
- [ ] Define extension hooks for seasons/climate mods without hard dependencies.

## Required tests

- Shade and sun genotypes receive opposite, symmetric outcomes where intended.
- Temperature behavior works across cold/hot/neutral environments and dimensions.
- Generalists remain viable without being universally optimal.
- Modifier composition cannot produce negative or unbounded growth chance.

## Exit criteria

All environmental stats are live, bipolar semantics are honored, and players can understand why a
crop performs differently in two locations.

