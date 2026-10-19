# Stage 3 — Genome, species, and inheritance

## Goal

Make genetics a coherent model rather than eight stored bytes plus a separate experimental allele
system.

## Current implementation

- Eight signed standard values are stored in `PlantStats` and survive NBT.
- `CropSpecies` stores base templates and allowed locked-attribute slots.
- Cross mutation blends all eight standard values with adapted upstream arithmetic.
- Locked attributes store two alleles, dominance flags, and a bred-true counter.
- Attribute rules and hard-incompatible pairs are registrable.

## Current remaining gaps

- Mutation Chance does not control genetic donation or mutation probability.
- Inheritance still uses adapted upstream arithmetic rather than documented per-stat policies.
- The analyzer does not clearly separate genotype, expressed phenotype, carrier state, and
  stabilization.
- Missing/legacy genome arrays need an explicit versioned migration policy.

## Work plan

Scope additions from Decision 0002 / the vault breeding contract:

- [ ] Separate wild/imported, crafted, bred and cloned creation paths; preserve identity without rerolls.
- [ ] Implement seed-specific locus lockout/exclusions with documented fresh-seed/backcross recovery.
- [ ] Define bounded positive/neutral/negative inheritance variation and species regression loops.
- [ ] Keep clippings genetically identical without sexual-generation/research inflation.

- [x] Write the initial genome specification: creation, ranges, bipolar direction, crossover,
  locked-trait expression, stabilization, and loss (`docs/genome-specification.md`).
  inheritance, dominance, expression, carrier state, stabilization, and loss.
- [x] Introduce one canonical `GenomeFactory`/service for all seed creation paths.
- [x] Apply species base templates to wild/fresh seeds; preserve inherited mutation/spread stats.
- [ ] Replace adapted three-stat arithmetic with explicit per-stat inheritance policies.
- [x] Route inheritance/spread rolls through the caller/world `Random` source.
- [x] Enforce child-species slots for fresh unlocks and inherited traits.
- [x] Implement and test dropout and bred-true stabilization semantics.
- [x] Treat locked attributes as named numeric effect hooks; Frost Hardiness is the first consumer.
- [ ] Add migration behavior for missing, short, old, or unknown genome NBT.
- [ ] Make analyzer output distinguish genotype, expressed phenotype, carrier traits, and certainty.

## Required tests

- Boundary/clamping and NBT round trips.
- Fresh seed receives its species template on every creation path.
- Deterministic inheritance with fixed random seeds.
- Dominant/recessive crosses, carrier inheritance, dropout, and stabilization.
- Missing species/attribute definitions fail safely and visibly.
- Negative bipolar values remain meaningful rather than being treated as failed magnitude stats.

## Exit criteria

Every stored value has a documented creation path, inheritance rule, mechanical or planned
consumer, player-readable explanation, and deterministic test.
