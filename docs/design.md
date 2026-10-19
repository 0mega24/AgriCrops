# New Agricrops design boundary

The owner chose to archive the old fork and start this project on main on 2026-10-19. Product
design is maintained in `/home/slidy/Vault/Projects/Agricrops/`, on the Agricrops TV channel:

- `Mechanics and gameplay specification.md`: product loop, eight genes, traits, environment,
  harvest/instability, tools, automation, compatibility and source of truth.
- `Breeding growth and layered soil.md`: regression and parent-species/backcross loops,
  recoverable seed-specific locus restrictions, stage-specific donor/receptive roles, meaningful
  durations, crafted/bred/imported/clone origins, seedling cuttings/rooting and bounded layers.
- `Custom content imports and texture plan.md`: Java definitions/CT authoring, guarded Pam/MA
  adapters, source progression, native presentation, realistic fallbacks and exact asset reports.
- `Information design and field guide.md`: short/expanded/researched/author depth, semantic
  colors, internal NBT, localization, practical notebook tone and discovery-aware documentation.
- `Delivery and acceptance plan.md`: milestone order and evidence required for each release gate.

These are local vault paths, not portable repository content. Collaborators without the vault
should request an export of the relevant approved design revision; do not invent a replacement
contract from the prototype's old comments. The technical summary here preserves key boundaries:

1. Java 8 / Forge 1.12.2; independent pure Java rules and one authoritative definition model.
2. Base survival loop works without optional CraftTweaker/OC; CT authors custom content through
   actual Java-backed builders. No external simulator, parallel parser or web app.
3. Borrow native vanilla crop/seed art and supported mod presentation. Versioned imports preserve
   identity/acquisition gates; a texture match does not prove native mechanics are supported.
4. Preserve lines through cloning; breeding can regress/close a seed-specific path, with explicit
   fresh-line/backcross recovery. Locked traits are not a universal additive upgrade ladder.
5. Biological phases and active durations are independent from visual stages. Parents can need
   different phase windows; cutting/rooting has real costs and cannot inflate sexual generations.
6. Layered soil has real bounded resource ownership and invalidation. A depth field alone is not
   an implemented mechanic. Basic in-game tools explain actionable requirements.
7. Tooltip/journal/tool disclosure is measured/discovered and localized. Client-visible NBT is not
   secret from modified clients; internal storage is not automatically printed as player lore.
8. Source/API presence is not completion. Require tests at the actual breeding/world/action path,
   and separate build, dedicated-server and manual client evidence.

The new standard-value domain currently implements the proposed 0..63 magnitude and −63..63
directional ranges. It validates invalid authoring rather than silently clamping. Final balancing,
locked-locus algorithms, devices and import catalogue still require reviewed concrete definitions.

Next: complete the first vertical lifecycle—one vanilla-presented crop with resource-accounted
growth, inspection, CraftTweaker lifecycle proof, and save/reload—before expanding breadth.
