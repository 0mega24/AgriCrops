package dev.omega24.agricrops.core.genetics;

import dev.omega24.agricrops.core.content.CropDefinition;
import java.util.Objects;

public final class Specimen {
    public final String body;
    public final StandardGenome genes;
    public final SeedOrigin origin;
    public final ProductMix products;
    public final TraitGenome traits;
    public final long lineage;
    public final int generation;
    public Specimen(String body, StandardGenome genes, SeedOrigin origin, ProductMix products, TraitGenome traits, long lineage, int generation) {
        this.body = CropDefinition.identifier(body); this.genes = Objects.requireNonNull(genes); this.origin = Objects.requireNonNull(origin);
        this.products = Objects.requireNonNull(products); this.traits = Objects.requireNonNull(traits); this.lineage = lineage;
        if (generation < 0 || generation > 1000000) throw new IllegalArgumentException("Invalid generation"); this.generation = generation;
    }
    public static Specimen fresh(CropDefinition definition, SeedOrigin origin, long lineage) {
        return new Specimen(definition.id(), definition.baseline(), origin, ProductMix.single(definition.id()), TraitGenome.empty(), lineage, 0);
    }
    public Specimen clipping() { return new Specimen(body, genes, SeedOrigin.CLIPPING, products, traits, lineage, generation); }
    public boolean freshDonor() { return generation == 0 && (origin == SeedOrigin.WILD || origin == SeedOrigin.CRAFTED || origin == SeedOrigin.IMPORTED); }
}
