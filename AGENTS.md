# Agricrops development

- This is the new Java implementation. The old fork is on
  `codex/archive-agricrops-fork-2026-10-19`; do not restore its entire architecture onto main.
- Product source of truth: `/home/slidy/Vault/Projects/Agricrops/`, Agricrops TV channel.
  Read `docs/design.md` and the relevant vault contract before implementing mechanics.
- `core/` contains independently testable Java 8 rules. It must not import Minecraft/Forge,
  CraftTweaker, OpenComputers or client-rendering classes.
- Root `src/` adapts the rules to Forge 1.12.2. No external simulator/Node/npm product.
- New authoring uses Java-backed CraftTweaker definitions/imports. Borrow native art where
  appropriate and preserve source-mod identity/progression; do not guess resource suffixes.
- Verify meaningful behavior at its real boundary. Helper tests alone do not prove breeding,
  saved world state, imports or tool visibility. Keep `docs/implementation-status.md` honest.
- Start with `./gradlew :core:test --no-daemon --max-workers=1`. Builds do not launch Minecraft.
  Live client/server qualification is deliberate and serialized; use normal shutdown and retain
  bounded resource settings following the reported host panic.
- `.legacy-local/` holds recoverable old worlds/reports/dependencies and a Git bundle. Keep it
  ignored, do not delete it, publish it or mix archived mod outputs into new artifacts.
