package dev.omega24.agricrops.api.v1.species;

import dev.omega24.agricrops.api.v1.attribute.LockedAttribute;
import dev.omega24.agricrops.api.v1.misc.IAgriRegisterable;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * A registered species (crop-mod-design.md §7): a Java object, Forge-registry style, holding a
 * base stat template (the standard §2 stat set's default rolls for a fresh genome of this
 * species) and the list of {@link LockedAttribute} slots this species is capable of unlocking
 * through cross-breeding. A species with an empty slot list only ever carries the standard
 * eight stats — nothing sits locked-and-zero waiting to be revealed.
 *
 * <p>By convention a species id matches the {@link dev.omega24.agricrops.api.v1.plant.IAgriPlant}
 * id it backs (one species per plant), keeping the plant registry and species registry in
 * lock-step without needing a cross-reference field on either side.
 */
public final class CropSpecies implements IAgriRegisterable {

    private final String id;
    private final byte[] baseStats;
    private final List<LockedAttribute> lockedAttributeSlots;

    private CropSpecies(@Nonnull String id, @Nonnull byte[] baseStats, @Nonnull List<LockedAttribute> lockedAttributeSlots) {
        this.id = id;
        this.baseStats = baseStats;
        this.lockedAttributeSlots = lockedAttributeSlots;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * The default value a fresh, unbred genome of this species rolls for the given standard
     * stat, in {@link CropStat#MIN}..{@link CropStat#MAX}.
     */
    public byte getBaseStat(@Nonnull CropStat stat) {
        return this.baseStats[stat.ordinal()];
    }

    /**
     * The {@link LockedAttribute}s this species is capable of unlocking at all. An attribute
     * unlock rule (crop-mod-design.md §7) naming an attribute outside this list for this
     * species is meaningless — the species has no slot for it.
     */
    @Nonnull
    public Collection<LockedAttribute> getLockedAttributeSlots() {
        return this.lockedAttributeSlots;
    }

    public boolean canUnlock(@Nonnull LockedAttribute attribute) {
        return this.lockedAttributeSlots.contains(attribute);
    }

    @Nonnull
    public static Builder builder(@Nonnull String id) {
        return new Builder(id);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof CropSpecies) && this.id.equals(((CropSpecies) obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public static final class Builder {

        private final String id;
        private final byte[] baseStats = new byte[CropStat.values().length];
        private final List<LockedAttribute> slots = new ArrayList<>();

        private Builder(@Nonnull String id) {
            this.id = Objects.requireNonNull(id);
        }

        @Nonnull
        public Builder baseStat(@Nonnull CropStat stat, int value) {
            this.baseStats[stat.ordinal()] = CropStat.clamp(value);
            return this;
        }

        @Nonnull
        public Builder lockedAttributeSlot(@Nonnull LockedAttribute attribute) {
            this.slots.add(Objects.requireNonNull(attribute));
            return this;
        }

        @Nonnull
        public CropSpecies build() {
            return new CropSpecies(this.id, this.baseStats.clone(), Collections.unmodifiableList(new ArrayList<>(this.slots)));
        }

    }

}
