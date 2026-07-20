package dev.omega24.agricrops.api.v1.attribute;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * One rule registered against a species pair (crop-mod-design.md §7): crossing that pair gives
 * a roll, at {@link #getChance()}, to unlock {@link #getAttributeId()} on the resulting cross
 * seed's genome (if neither parent already carries it). Multiple rules may exist for the same
 * species pair, one per attribute that pair can unlock.
 */
public final class AttributeUnlockRule {

    private final String attributeId;
    private final double chance;

    public AttributeUnlockRule(@Nonnull String attributeId, double chance) {
        this.attributeId = Objects.requireNonNull(attributeId);
        if (chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("Attribute unlock chance must be in 0.0..1.0, was " + chance);
        }
        this.chance = chance;
    }

    @Nonnull
    public String getAttributeId() {
        return this.attributeId;
    }

    /**
     * The normalized p-value chance of this rule firing on a qualifying cross.
     */
    public double getChance() {
        return this.chance;
    }

}
