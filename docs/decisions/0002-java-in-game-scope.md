# Decision 0002: Java mod, in-game learning, and CraftTweaker content

- Date: 2026-10-19
- Status: owner-directed scope; detailed mechanic proposals remain under review
- Supersedes: external companion/simulator parts of Decision 0001 and the earlier roadmap

## Direction

The owner removed the out-of-game simulator. Agricrops is an all-Java Minecraft mod with
CraftTweaker/ZenScript custom-content authoring. Java deterministic tests and development
reports remain verification tooling; no web application, local HTTP service, TypeScript
engine, Node/npm requirement, or simulator-parity release gate is planned.

Required design areas now explicitly include:

- Vanilla seeds/crops use their native art; custom/imported crops borrow installed models,
  sprites, tints and localization where appropriate, with realistic fallbacks and a precise
  missing-asset report rather than a new PNG set for every species/genome.
- Versioned Java imports for crops such as Pam's HarvestCraft and Mystical Agriculture,
  with explicit item/state/produce identity, source progression requirements and restrictions.
- Non-linear breeding, standard-stat regression, authored parent-species/backcross loops,
  seed-specific trait lockout and recoverable new-line acquisition.
- Phase-specific parent roles/windows and actual growth timing, distinct from render stages.
- Seedling clippings/rooting and explicit wild/imported, crafted, bred and cloned seed rules.
- Multiple soil layers, bounded root reach, real conserved resources and world-change invalidation.
- Progressive tooltips/analyzer/journal depth, semantic colors, internal NBT kept out of normal
  UI, localization and practical field-notebook tone.
- In-game tools explaining requirements and discovery-aware documentation of mutations/traits.

## Source of truth

The project vault owns the current design and proposed policies:

- [Mechanics specification](</home/slidy/Vault/Projects/Agricrops/Mechanics and gameplay specification.md>)
- [Breeding, growth and layered soil](</home/slidy/Vault/Projects/Agricrops/Breeding growth and layered soil.md>)
- [Custom content/imports/textures](</home/slidy/Vault/Projects/Agricrops/Custom content imports and texture plan.md>)
- [Information design/field guide](</home/slidy/Vault/Projects/Agricrops/Information design and field guide.md>)
- [Delivery/acceptance plan](</home/slidy/Vault/Projects/Agricrops/Delivery and acceptance plan.md>)

These links refer to the owner's local vault, not portable repository files. Detailed defaults
(three-layer baseline, update cadence, locus policies and costs) are proposals, not implemented
or automatically owner-approved rules. The fresh implementation recommendation also remains
distinct from implementing that replacement.

## Consequences

The withdrawn `companion/` source and simulator proposal remain historical material rather than
being deleted. Remove its Gradle/npm onboarding and active release requirements. Runtime code
does not gain the newly planned mechanics through this documentation update.

CraftTweaker executes the actual scripts; do not build a parallel parser. Validate pending
definitions and lifecycle ordering before publishing content. Definitions apply at startup/restart
on the supported 1.12 workflow; ordinary `/ct reload` is not supported by CraftTweaker 1.12.
See [the version-specific documentation](https://docs.blamejared.com/1.12/en/Getting_Started/).

NBT hidden from official tooltips is not secret if sent to clients. Shared reveal policies cover
tooltips, models, probes, JEI, scanner and OC; genuinely server-only facts require separate storage
and authorized responses. No external tool should be required to learn ordinary farming rules.
