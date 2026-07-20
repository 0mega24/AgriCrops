package dev.omega24.agricrops.api.v1.attribute;

import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Maps species-id pairs to the {@link LockedAttribute}s they can unlock on a cross, plus the
 * hard-incompatibility flag that blocks a cross outright (crop-mod-design.md §7). Keyed by
 * species id rather than {@link dev.omega24.agricrops.api.v1.species.CropSpecies} directly so
 * rules can be registered (e.g. from CraftTweaker) before both species are necessarily loaded.
 */
public interface IAttributeUnlockRegistry {

    /**
     * Registers a rule: crossing {@code speciesA} and {@code speciesB} gets a
     * {@code chance}-weighted roll to unlock {@code attributeId} on the cross seed's genome, if
     * neither parent already carries it. Multiple rules may be registered for the same pair.
     *
     * @return {@literal false} if {@code chance} is outside 0.0..1.0, {@literal true} otherwise.
     */
    boolean addRule(@Nonnull String speciesA, @Nonnull String speciesB, @Nonnull String attributeId, double chance);

    /**
     * All unlock rules registered for the given (unordered) species pair, or empty if none.
     */
    @Nonnull
    List<AttributeUnlockRule> getRules(@Nonnull String speciesA, @Nonnull String speciesB);

    /**
     * Flags a species pair as hard-incompatible: no cross seed forms between them at all,
     * regardless of stats or luck, distinct from a pair that simply has no unlock rule.
     *
     * @return {@literal true} if this pair wasn't already flagged incompatible.
     */
    boolean markIncompatible(@Nonnull String speciesA, @Nonnull String speciesB);

    boolean isIncompatible(@Nonnull String speciesA, @Nonnull String speciesB);

    /**
     * Rolls the full locked-attribute outcome of crossing two genomes, per crop-mod-design.md
     * §7's dominant/recessive + stabilization rules: attributes already active on either parent
     * are inherited (one randomly chosen allele per parent, generations-bred-true incremented
     * only when both parents already carried it), and any registered rule for this species pair
     * that neither parent already carries gets its own independent unlock roll. Returns the
     * complete set of {@link LockedAttributeInstance}s the offspring genome should carry —
     * callers merge this straight into the offspring's {@link IAgriStat} via {@code
     * withLockedAttributes}. Returns an empty list if the pair is {@link
     * #isIncompatible(String, String) incompatible} (callers should already have blocked the
     * cross before reaching here; this is a defensive fallback).
     *
     * @param parentA nullable: a parent with no genome (e.g. an unverified seed) simply
     * contributes no inherited attributes.
     */
    @Nonnull
    List<LockedAttributeInstance> rollCross(@Nonnull String speciesA, @Nullable IAgriStat parentA,
                                             @Nonnull String speciesB, @Nullable IAgriStat parentB,
                                             @Nonnull Random rand);

}
