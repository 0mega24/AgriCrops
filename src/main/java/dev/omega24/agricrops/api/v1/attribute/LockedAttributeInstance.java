package dev.omega24.agricrops.api.v1.attribute;

import dev.omega24.agricrops.api.v1.stat.CropStat;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;

/**
 * An active locked attribute (crop-mod-design.md §7) on a specific genome: which {@link
 * LockedAttribute} it is, its two inherited {@link AttributeAllele}s, and how many successive
 * crosses it has bred true. Only ever stored on a genome while active — an attribute a plant
 * never unlocked simply has no {@code LockedAttributeInstance}, rather than sitting
 * present-but-zero (design's explicit call-out for keeping NBT small).
 */
public final class LockedAttributeInstance {

    public static final String NBT_ATTRIBUTE_ID = "id";
    public static final String NBT_ALLELE_A_VALUE = "allele_a_value";
    public static final String NBT_ALLELE_A_DOMINANT = "allele_a_dominant";
    public static final String NBT_ALLELE_B_VALUE = "allele_b_value";
    public static final String NBT_ALLELE_B_DOMINANT = "allele_b_dominant";
    public static final String NBT_GENERATIONS_BRED_TRUE = "generations_bred_true";

    private final String attributeId;
    private final AttributeAllele alleleA;
    private final AttributeAllele alleleB;
    private final int generationsBredTrue;

    public LockedAttributeInstance(@Nonnull String attributeId, @Nonnull AttributeAllele alleleA,
                                    @Nonnull AttributeAllele alleleB, int generationsBredTrue) {
        this.attributeId = Objects.requireNonNull(attributeId);
        this.alleleA = Objects.requireNonNull(alleleA);
        this.alleleB = Objects.requireNonNull(alleleB);
        this.generationsBredTrue = Math.max(0, generationsBredTrue);
    }

    @Nonnull
    public String getAttributeId() {
        return this.attributeId;
    }

    @Nonnull
    public AttributeAllele getAlleleA() {
        return this.alleleA;
    }

    @Nonnull
    public AttributeAllele getAlleleB() {
        return this.alleleB;
    }

    public int getGenerationsBredTrue() {
        return this.generationsBredTrue;
    }

    /**
     * Whether this instance has bred true enough generations (per {@link
     * LockedAttribute#getStabilizationThreshold()}) to stop risking drop-out on the next cross.
     */
    public boolean isStable(@Nonnull LockedAttribute definition) {
        return this.generationsBredTrue >= definition.getStabilizationThreshold();
    }

    /**
     * The value this attribute actually contributes to the genome right now: the dominant
     * allele's value when the two alleles disagree on dominance, or the average of both when
     * they agree (both dominant: a true-breeding express; both recessive: a silently-carried
     * trait that still resolves to a value if something reads it directly, e.g. an Analyzer).
     */
    public byte getExpressedValue() {
        if (this.alleleA.isDominant() != this.alleleB.isDominant()) {
            return this.alleleA.isDominant() ? this.alleleA.getValue() : this.alleleB.getValue();
        }
        return CropStat.clamp(Math.round((this.alleleA.getValue() + this.alleleB.getValue()) / 2.0f));
    }

    /**
     * One of this instance's two alleles, picked uniformly at random — how a parent hands off a
     * single allele to offspring during a cross (crop-mod-design.md §7 Mendelian inheritance).
     */
    @Nonnull
    public AttributeAllele randomAllele(@Nonnull Random rand) {
        return rand.nextBoolean() ? this.alleleA : this.alleleB;
    }

    @Nonnull
    public LockedAttributeInstance withGenerationsBredTrue(int generations) {
        return new LockedAttributeInstance(this.attributeId, this.alleleA, this.alleleB, generations);
    }

    public void writeToNBT(@Nonnull NBTTagCompound tag) {
        tag.setString(NBT_ATTRIBUTE_ID, this.attributeId);
        tag.setTag(NBT_ALLELE_A_VALUE, new NBTTagByte(this.alleleA.getValue()));
        tag.setBoolean(NBT_ALLELE_A_DOMINANT, this.alleleA.isDominant());
        tag.setTag(NBT_ALLELE_B_VALUE, new NBTTagByte(this.alleleB.getValue()));
        tag.setBoolean(NBT_ALLELE_B_DOMINANT, this.alleleB.isDominant());
        tag.setInteger(NBT_GENERATIONS_BRED_TRUE, this.generationsBredTrue);
    }

    @Nonnull
    public static LockedAttributeInstance readFromNBT(@Nonnull NBTTagCompound tag) {
        return new LockedAttributeInstance(
                tag.getString(NBT_ATTRIBUTE_ID),
                new AttributeAllele(tag.getByte(NBT_ALLELE_A_VALUE), tag.getBoolean(NBT_ALLELE_A_DOMINANT)),
                new AttributeAllele(tag.getByte(NBT_ALLELE_B_VALUE), tag.getBoolean(NBT_ALLELE_B_DOMINANT)),
                tag.getInteger(NBT_GENERATIONS_BRED_TRUE)
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LockedAttributeInstance)) {
            return false;
        }
        final LockedAttributeInstance other = (LockedAttributeInstance) obj;
        return this.attributeId.equals(other.attributeId)
                && this.alleleA.equals(other.alleleA)
                && this.alleleB.equals(other.alleleB)
                && this.generationsBredTrue == other.generationsBredTrue;
    }

    @Override
    public int hashCode() {
        int hash = this.attributeId.hashCode();
        hash = 31 * hash + this.alleleA.hashCode();
        hash = 31 * hash + this.alleleB.hashCode();
        hash = 31 * hash + this.generationsBredTrue;
        return hash;
    }

}
