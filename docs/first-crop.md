# First crop slice

This is an early wheat lifecycle, not the full genetics mod. Use a new disposable world.

## Player loop

1. Craft four crop sticks from five sticks: `S S` above `SSS` in a crafting grid.
2. Place on farmland and use vanilla wheat seeds to plant. The original native seed icon is used;
   no replacement wheat item or seed texture is needed.
3. Provide light 9–15 and water. Nearby open water within four blocks horizontally, at plant or
   soil height, supplies 100 units each loaded-world second. A water bucket supplies 2000 units
   and returns an empty bucket. This first supply rule is deliberately simple, not a finished
   irrigation mechanic.
4. Soil starts with 5000 nutrients when first observed; bone meal adds 2000, consuming one item.
   Water and nutrients each cap at 10000. Both care inputs replenish pools, not instant growth.
5. Empty-hand use harvests mature wheat and restarts the retained plant. Sneak-use or hold a
   stick to inspect crop phase, overall progress, pool percentages and the current blockage.
6. Breaking planted sticks calculates one native wheat seed with retained specimen NBT and one
   set of sticks, not an additional mature harvest. Replanting the seed preserves genes/origin
   and resets development. Replacing sticks does not reset position-owned soil fertility.

The built-in four biological phases last 1200 active ticks each: 4800 ticks / four minutes at
neutral growth and 20 TPS, when conditions and resources permit. Updates run once per second;
unloaded time is not credited. Eight borrowed vanilla wheat models are visual stages, not eight
biological phases. Only the mature state uses the final model. Client model baking/visual QA and
a natural-time player playthrough are still required.

Current provisional costs are one water and nutrient unit per development tick, multiplied by
the yield output count. Yield gives `1 + floor(yield / 16)` wheat; Growth raises paid development
per interval from 20 to 40 across its range. Other stored stats are not yet mechanically applied.
These are first-slice coefficients, not claims of finished genetics/balance.

## CraftTweaker authoring starts here

CraftTweaker is optional. The bridge is compiled against `1.12-4.1.20.715`; the base mod loads
without it. See `examples/scripts/first_crop.zs` for the tested builder shape. Scripts construct
the same pending Java definitions as built-ins, resolve item/soil IDs and freeze before gameplay.
Use `.replace()` for an existing crop ID and `.register()` for a new one. Duplicate seed IDs are
rejected rather than ambiguously choosing a crop. Invalid values are not clamped.

Supported fields now: namespaced crop/seed/product/soil IDs, localized crop-name key, four phase
durations, per-development-tick water/nutrient costs, light range and eight baseline stats in enum
order: Growth, Yield, Resilience, Water affinity, Fertility response, Light tolerance, Temperature
tolerance, Mutation chance. Magnitudes use 0..63; directional tolerances use -63..63. Growth time is capped at 288000
active ticks; positive costs at 100; light within 0..15. Metadata/NBT ingredient predicates are not
implemented; matching native items currently requires metadata zero.

Restart after edits. `/ct reload` is not supported for live definitions. Builder validation logs
source file/line and records an error that prevents the definition freeze; registry resolution
also refuses missing items/soil. General ZenScript compilation errors remain CT diagnostics;
check `crafttweaker.log`. Not every CT parser error can be intercepted by this bridge.

Only wheat's native visual mapping exists now. Adding other IDs is experimental and does not
import their native models, tint, age semantics, acquisition gates or special mechanics. Do not
call this generic/Pam/Mystical Agriculture import support. That requires explicit adapter and
presentation contracts, not guessed texture names or silently presenting every crop as wheat.

## Persistence and information boundary

`agricrops_specimen` schema 1 on returned native seeds stores crop ID, origin, active progress and
eight values. The tile stores the same specimen; surface soil pools live in dimension-local
WorldSavedData by block position, not the seed or sticks item. Observed block replacement clears
old pools, never grants fresh fertility. There is one surface cell, not implemented deeper layers.

Unknown/corrupt specimens are refused without consuming the seed. Rejected tile payloads are
retained and can be recovered in a dropped sticks item's `BlockEntityTag`; removed definitions
do not silently turn a line into fresh wheat. Unsupported soil saves retain their payload and
refuse farming instead of quietly making empty/fertile data. This is safety behavior, not a full
migration system; keep backups and never test an old production world directly.

Basic inspection and tooltips use translation keys and practical short messages. A retained seed
is marked unanalysed, not given an exact stat dump. Chunk update tags do not need genes to render;
native inventory seed NBT remains inspectable by modified clients. Analyzer/research reveal
policy, semantic coloring, field guide and complete localization are later work.

## Test and runtime procedure

See `BUILDING.md` for serial headless launch, optional CT and opt-in administrator fixtures.
The fixture accelerates the normal interval action; it does not wait four real minutes, test a
physical player's entire break/pickup path or establish client rendering. Current observed results
and unresolved gates are in `implementation-status.md`.
