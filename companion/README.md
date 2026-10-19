# Agricrops Field Lab

> **Withdrawn, 2026-10-19:** the owner removed the external simulator from the product.
> This directory preserves historical prototype source. It is not a release dependency or
> development milestone; the old Gradle companion tasks have been removed. No npm command here
> is required for Agricrops. See `docs/decisions/0002-java-in-game-scope.md`.

## Historical prototype description (not current onboarding)

The companion is a local static web application and deterministic TypeScript engine. Its current
v0.1 slice can edit/validate `agricrops-model-v1` JSON, visualize mutation routes, parse the
declarative `mods.agricrops.Mutations.addMutation(...)` ZenScript call, and replay a seeded
single-plot growth simulation with moisture, nutrients, light, and biome temperature.

```sh
npm ci
npm test
npm run build
npm run dev
```

Run those commands from `companion/`, or use `./gradlew companionTest companionBuild` from the
repository root. The production static files are written to `companion/dist/`.

This is an executable foundation, not simulator v1 completion. Configuration parsing, the full
ZenScript authoring surface, farm grids, crosses, locked-attribute inheritance, harvests,
instability/weeds, report export, and Java golden-fixture parity remain on the delivery sequence in
`docs/companion-simulator.md`.
