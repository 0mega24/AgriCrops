// mods.agricrops.Mutations - the actual cross-breed relation (crop-mod-design.md §6/§7):
// which parent plants, placed in crop sticks around an empty cross-crop, can produce which
// child. Distinct from mods.agricrops.Attributes' unlock rules, which only govern whether a
// cross ALSO unlocks a locked attribute - the cross itself still needs a mutation entry (or
// it simply can't happen at all, same as any two unrelated vanilla plants).

import mods.agricrops.Mutations;

// 5% chance per attempt for wheat + carrot (planted adjacent) to produce a frostberry
// cross-crop instead.
Mutations.addMutation("my_pack:frostberry_mutation", 0.05, "my_pack:frostberry", ["wheat", "carrot"]);
