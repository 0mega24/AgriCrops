package dev.omega24.agricrops.api.v1.attribute;

import dev.omega24.agricrops.api.v1.stat.CropStat;

/**
 * One of the two allele slots a {@link LockedAttributeInstance} carries for a given locked
 * attribute (crop-mod-design.md §7: "give each locked-attribute slot two allele slots... with a
 * dominance flag, same shape as Forestry's bee chromosomes"). A dominant allele expresses
 * immediately when paired against a recessive one; a recessive allele is carried silently and
 * can resurface in a later cross.
 */
public final class AttributeAllele {

    /**
     * The allele carried when a lineage never had this attribute at all (e.g. a parent that
     * doesn't express or carry the trait contributes this instead of leaving a slot empty), so
     * an attribute already present in the other parent still has a real (if reduced) chance to
     * express or be carried forward.
     */
    public static final AttributeAllele ABSENT = new AttributeAllele(0, false);

    private final byte value;
    private final boolean dominant;

    public AttributeAllele(int value, boolean dominant) {
        this.value = CropStat.clamp(value);
        this.dominant = dominant;
    }

    public byte getValue() {
        return this.value;
    }

    public boolean isDominant() {
        return this.dominant;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttributeAllele)) {
            return false;
        }
        final AttributeAllele other = (AttributeAllele) obj;
        return this.value == other.value && this.dominant == other.dominant;
    }

    @Override
    public int hashCode() {
        return this.value * 31 + (this.dominant ? 1 : 0);
    }

}
