package dev.omega24.agricrops.farming;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.species.CropSpecies;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Canonical construction service for crop genomes and seeds.
 *
 * <p>Fresh genomes start from the registered species template. Species-less plants (currently
 * weeds and legacy JSON definitions) safely fall back to the all-zero wild-type genome. Offspring
 * and migrated seeds still pass through {@link #createSeed(IAgriPlant, IAgriStat)} so creation has
 * one validation boundary without replacing their inherited or persisted values.</p>
 */
public final class GenomeFactory {

    private static final GenomeFactory INSTANCE = new GenomeFactory(
            id -> AgriApi.getSpeciesRegistry().get(id));

    private final Function<String, Optional<CropSpecies>> speciesResolver;

    public GenomeFactory(@Nonnull Function<String, Optional<CropSpecies>> speciesResolver) {
        this.speciesResolver = Objects.requireNonNull(speciesResolver, "speciesResolver");
    }

    @Nonnull
    public static GenomeFactory getInstance() {
        return INSTANCE;
    }

    /** Creates an unanalyzed fresh seed using its species base template. */
    @Nonnull
    public AgriSeed createFreshSeed(@Nonnull IAgriPlant plant) {
        return this.createFreshSeed(plant, false);
    }

    /** Creates a fresh seed using its species base template. */
    @Nonnull
    public AgriSeed createFreshSeed(@Nonnull IAgriPlant plant, boolean analyzed) {
        Objects.requireNonNull(plant, "plant");
        return this.createSeed(plant, this.createFreshStats(plant.getId(), analyzed));
    }

    /**
     * Creates a seed around an existing inherited, randomized, or loaded genome.
     */
    @Nonnull
    public AgriSeed createSeed(@Nonnull IAgriPlant plant, @Nonnull IAgriStat stats) {
        return new AgriSeed(Objects.requireNonNull(plant, "plant"),
                Objects.requireNonNull(stats, "stats"));
    }

    /** Creates a fresh genome for a species ID, falling back safely when no species exists. */
    @Nonnull
    public IAgriStat createFreshStats(@Nonnull String speciesId, boolean analyzed) {
        Objects.requireNonNull(speciesId, "speciesId");
        IAgriStat result = new PlantStats(analyzed);
        final Optional<CropSpecies> species = this.speciesResolver.apply(speciesId);
        if (species.isPresent()) {
            for (CropStat stat : CropStat.values()) {
                result = result.with(stat, species.get().getBaseStat(stat));
            }
        }
        return result;
    }
}
