package dev.omega24.agricrops.api.v1.attribute;

import dev.omega24.agricrops.api.v1.misc.IAgriRegisterable;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * A locked attribute definition (crop-mod-design.md §7): an extra genome slot that doesn't
 * exist on a species at all until a registered cross-species unlock rule activates it. Unlike
 * {@link CropStat}, the set of {@code LockedAttribute}s is open-population — registered at load
 * time (in practice, mostly via CraftTweaker/ZenScript per design §11) rather than fixed as a
 * Java enum, since packs may want to define their own.
 *
 * <p>Values, once unlocked on a genome, share {@link CropStat}'s {@link CropStat#MIN}..{@link
 * CropStat#MAX} range and byte storage so the aggregation code doesn't need a second numeric
 * scale to reason about.
 */
public final class LockedAttribute implements IAgriRegisterable {

    /**
     * Default "generations bred true" threshold (crop-mod-design.md §7) before a freshly
     * unlocked or resurfaced-recessive attribute stops risking drop-out on the next cross.
     * Placeholder balance number, like the rest of the stat/mutation math — some attributes
     * should be harder to stabilize than others, hence this being overridable per attribute
     * rather than a single global constant.
     */
    public static final int DEFAULT_STABILIZATION_THRESHOLD = 3;

    private final String id;
    private final boolean bipolar;
    private final int stabilizationThreshold;

    public LockedAttribute(@Nonnull String id, boolean bipolar) {
        this(id, bipolar, DEFAULT_STABILIZATION_THRESHOLD);
    }

    public LockedAttribute(@Nonnull String id, boolean bipolar, int stabilizationThreshold) {
        this.id = Objects.requireNonNull(id);
        this.bipolar = bipolar;
        this.stabilizationThreshold = stabilizationThreshold;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * @see CropStat#isBipolar() for what bipolar vs. magnitude-only means for aggregation.
     */
    public boolean isBipolar() {
        return this.bipolar;
    }

    /**
     * How many successive crosses (deliberately preserving this trait, per crop-mod-design.md
     * §7) an instance of this attribute needs to accumulate before it breeds true reliably
     * instead of risking drop-out. See {@link LockedAttributeInstance#getGenerationsBredTrue()}.
     */
    public int getStabilizationThreshold() {
        return this.stabilizationThreshold;
    }

    @Nonnull
    public String getTranslationKey() {
        return "agricrops_tooltip." + this.id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LockedAttribute) && this.id.equals(((LockedAttribute) obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
