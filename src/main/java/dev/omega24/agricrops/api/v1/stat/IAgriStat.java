package dev.omega24.agricrops.api.v1.stat;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Interface for representing a plant's stat genome (crop-mod-design.md §2). Stats are immutable
 * objects, to aid in overall safety.
 *
 * <p>Replaces the old flat growth/gain/strength model with a keyed lookup over the widened
 * {@link CropStat} set, each stored as a signed byte in {@link CropStat#MIN}..{@link
 * CropStat#MAX}.
 */
public interface IAgriStat {

    /**
     * Retrieves the stat's serialization ID.
     *
     * @return the stat's id.
     */
    @Nonnull
    default String getId() {
        return this.getClass().getCanonicalName();
    }

    /**
     * Determines if the given instance has been analyzed.
     *
     * @return if the seed stats are analyzed
     */
    boolean isAnalyzed();

    /**
     * Fetches the value of the given stat associated with this instance, always in
     * {@link CropStat#MIN}..{@link CropStat#MAX}.
     *
     * @param stat the stat to fetch.
     * @return the value of that stat for this genome.
     */
    byte get(@Nonnull CropStat stat);

    /**
     * Sets the analyzed parameter.
     *
     * @param analyzed
     * @return the new stat.
     */
    @Nonnull
    IAgriStat withAnalyzed(boolean analyzed);

    /**
     * Sets the given stat to the given value (clamped to {@link CropStat#MIN}..{@link
     * CropStat#MAX}).
     *
     * @param stat the stat to set.
     * @param value the new value for that stat.
     * @return the new stat.
     */
    @Nonnull
    IAgriStat with(@Nonnull CropStat stat, int value);

    /**
     * Writes the stat to an NBTTagcompound. Make sure to set the stat id value when implementing
     * this.
     *
     * @param tag The tag to serialize to.
     * @return if the transcription was successful.
     */
    @Nonnull
    boolean writeToNBT(@Nonnull NBTTagCompound tag);

    /**
     * Writes the stat for display.
     *
     * @param consumer The sink to add the lines to.
     * @return If the writing was successful.
     */
    @Nonnull
    boolean addStats(@Nonnull Consumer<String> consumer);

    @Override
    public boolean equals(@Nullable Object obj);

    @Override
    public int hashCode();

}
