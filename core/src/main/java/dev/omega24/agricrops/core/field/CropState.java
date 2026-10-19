package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.core.genetics.StandardGenome;
import java.util.Objects;

/** Persisted specimen and development, deliberately separate from position-owned soil. */
public final class CropState {
    private final String crop;
    private final StandardGenome genome;
    private final SeedOrigin origin;
    private final long progress;

    public CropState(String crop, StandardGenome genome, SeedOrigin origin, long progress) {
        this.crop = CropDefinition.identifier(crop);
        this.genome = Objects.requireNonNull(genome, "genome");
        this.origin = Objects.requireNonNull(origin, "origin");
        if (progress < 0) throw new IllegalArgumentException("Negative development");
        this.progress = progress;
    }

    public static CropState fresh(CropDefinition definition, SeedOrigin origin) {
        return new CropState(definition.id(), definition.baseline(), origin, 0);
    }

    public String crop() { return crop; }
    public StandardGenome genome() { return genome; }
    public SeedOrigin origin() { return origin; }
    public long progress() { return progress; }
    public CropState at(long progress) { return new CropState(crop, genome, origin, progress); }
}
