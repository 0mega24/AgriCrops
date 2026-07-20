package dev.omega24.agricrops.farming.growthrequirement;

import com.google.common.base.Preconditions;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.requirement.BlockCondition;
import dev.omega24.agricrops.api.v1.requirement.ICondition;
import dev.omega24.agricrops.api.v1.requirement.IGrowthRequirement;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import dev.omega24.agricrops.api.v1.soil.ISoilProfile;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Encodes all requirements a plant needs to mutate and grow Uses the Builder class inside to
 * construct instances.
 */
public final class GrowthRequirement implements IGrowthRequirement {

    /**
     * Minimum allowed brightness, inclusive *
     */
    private final int minLight;

    /**
     * Maximum allowed brightness, exclusive *
     */
    private final int maxLight;

    @Nonnull
    private final List<ISoilComposition> soilCompositions;
    @Nonnull
    private final List<ICondition> conditions;

    GrowthRequirement(
            @Nonnull List<ISoilComposition> soilCompositions,
            @Nonnull List<ICondition> conditions,
            int minLight,
            int maxLight
    ) {
        this.soilCompositions = Preconditions.checkNotNull(soilCompositions);
        this.conditions = Preconditions.checkNotNull(conditions);
        this.conditions.sort((a, b) -> Integer.compare(a.getComplexity(), b.getComplexity()));
        if (minLight < maxLight) {
            this.minLight = minLight;
            this.maxLight = maxLight;
        } else {
            this.minLight = maxLight;
            this.maxLight = minLight;
        }
    }

    @Override
    @Nonnull
    public Collection<ISoilComposition> getSoilCompositions() {
        return Collections.unmodifiableCollection(this.soilCompositions);
    }

    @Override
    @Nonnull
    public Collection<ICondition> getConditions() {
        return Collections.unmodifiableCollection(this.conditions);
    }

    @Override
    public int getMinLight() {
        return this.minLight;
    }

    @Override
    public int getMaxLight() {
        return this.maxLight;
    }

    @Override
    public boolean hasValidSoil(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        // Validate
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(pos);

        // Evaluate. Unregistered blocks have no ISoilProfile and stay unsupported (design
        // §3): no silent fallback to a generic profile.
        final Optional<ISoilProfile> profile = AgriApi.getSoilProfileRegistry().get(world.getBlockState(pos.down()));
        if (!profile.isPresent()) {
            return false;
        }

        // An empty composition list means this plant doesn't care which registered soil it's on.
        return this.soilCompositions.isEmpty() || this.soilCompositions.contains(profile.get().getComposition());
    }

    @Override
    public boolean hasValidConditions(@Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        // Validate
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(pos);

        // Evaluate
        return this.getConditions().stream()
                .allMatch(c -> c.isMet(world, pos));
    }

    @Override
    public boolean hasValidLight(@Nonnull World world, @Nonnull BlockPos pos) {
        // Validate
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(pos);

        // Determine the light level of the block, as per the vanilla method used in BlockCrop.
        final int light = world.getLightFromNeighbors(pos.up());
        // Determine if the light level is in the proper range.
        return light >= this.getMinLight() && light <= this.getMaxLight();
    }

    @Override
    public boolean isMet(@Nonnull World world, @Nonnull BlockPos pos) {
        // Validate
        Preconditions.checkNotNull(world);
        Preconditions.checkNotNull(pos);

        // Evaluate
        return this.hasValidSoil(world, pos)
                && this.hasValidLight(world, pos)
                && this.hasValidConditions(world, pos);
    }

    @Override
    @Nonnull
    public Optional<FuzzyStack> getConditionStack() {
        return this.getConditions().stream()
                .filter(c -> c instanceof BlockCondition)
                .map(c -> ((BlockCondition) c).getStack())
                .findFirst();
    }

}
