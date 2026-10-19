# Decision 0001: standalone mod with optional authoring

- Status: proposed for owner acceptance
- Date: 2026-09-16

> Scope amendment: [Decision 0002](0002-java-in-game-scope.md), 2026-10-19, withdraws the external
> simulator and selects Java/in-game tooling with CraftTweaker content/imports. Simulator-related
> text below is historical. This does not imply acceptance of every earlier proposed mechanic.

## Context

The fork currently mixes placeholder built-in plants with a broad ZenScript registration surface.
That leaves contributors unable to tell whether missing base progression is intentional or a bug.
The requested companion simulator also needs a stable answer about which content and rules are
authoritative.

## Decision

Agricrops will be a playable standalone mod with an optional pack-authoring layer. Built-in content
will provide a deliberately small complete progression. CraftTweaker can extend or replace that
content for a pack but will not be a dependency for normal play. OpenComputers remains optional.

The companion simulator will consume a versioned resolved model exported from the same rule and
definition layer used by the mod. ZenScript remains an authoring input, not the simulator's internal
data model.

## Consequences

- The vertical slice is prioritized over adding more registry breadth.
- Base-jar progression and non-optional player feedback are release gates.
- Pack definitions require validation and an explicit reload lifecycle.
- Pure rule code and shared conformance fixtures must be extracted before simulator results can be
  called authoritative.
- The simulator can initially support the Agricrops ZenScript subset and generated config schema;
  arbitrary CraftTweaker programs are out of scope.
