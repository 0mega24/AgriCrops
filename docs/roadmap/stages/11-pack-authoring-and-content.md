# Stage 11 — Pack authoring and content delivery

## Goal

Let pack authors define complete Java content with CraftTweaker, import supported native crops,
borrow installed art and ship validated definitions with accurate in-game documentation/assets.
Apply at startup/restart. See Decision 0002 and the vault imports/texture contract; external
simulation is withdrawn.

## Current implementation

- CraftTweaker classes register soil compositions/profiles, attributes/rules, species, mutations,
  and plants.
- Plant builders expose render settings, textures, products, seeds, habitat, loot tiers, and weed
  flags.
- Calls mutate registries immediately.
- Registries reject duplicate IDs with `putIfAbsent`.
- CraftTweaker 1.12 does not support ordinary script reload. Independently, rerunning immediate
  registrations would not reliably replace existing definitions.
- Example scripts live in the repository but are not bundled gameplay content.
- The legacy JSON pipeline remains but cannot represent rewrite-era mechanics.

## Work plan

- [ ] Choose one supported definition lifecycle: immutable startup load, transactional reload, or
  explicit clear/rebuild.
- [ ] If reload is supported, stage definitions, validate the complete graph, then atomically swap
  registries and refresh client textures/JEI safely.
- [ ] Add removals/overrides with clear precedence and undo behavior.
- [ ] Validate IDs, namespaces, referenced blocks/items/textures/species/parents/attributes, numeric
  ranges, loot stages, cycles, unreachable mutations, and incomplete progression.
- [ ] Make the authoring API express every finalized mechanical field, including locked-trait
  effects and automation metadata where appropriate.
- [ ] Produce machine-readable API documentation and version the schema/API.
- [ ] Use Java definitions plus supported CraftTweaker builders/imports; diagnostic exports do not
  require authors to maintain a second content language.
- [ ] Separate tutorial examples, test fixtures, and a supported built-in content pack.
- [ ] Add a command/report that dumps the resolved plant, mutation, and progression graph.
- [ ] Add pack-level automated validation usable in CI without launching a client.
- [ ] Add Java imports/asset/localization reports and in-game stage previews; qualify native
  vanilla/Pam/MA mappings, source gates and propagation restrictions.
- [ ] Expose phase timing/roles, seed origins/clippings, locus lockout/recovery, layered soil and
  discovery-aware localized explanations in the CraftTweaker API.

## Exit criteria

Complete supported custom content/imports can be authored through CraftTweaker. Startup/restart
applies validated definitions; unsupported reload/renderers/mechanics receive explicit diagnostics.
In-game previews/reports identify borrowed art, intended fallbacks and exact missing resources/text.
