/*
 */
package dev.omega24.agricrops.compat.vanilla;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.adapter.IAgriAdapter;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import dev.omega24.agricrops.farming.PlantStats;
import dev.omega24.agricrops.utility.StackHelper;
import java.util.Optional;
import net.minecraft.item.ItemStack;

/**
 *
 *
 */
public class SeedWrapper implements IAgriAdapter<AgriSeed> {

    @Override
    public boolean accepts(Object obj) {
        return (obj instanceof ItemStack) && resolve((ItemStack) obj) != null;
    }

    @Override
    public Optional<AgriSeed> valueOf(Object obj) {
        if (obj instanceof ItemStack) {
            return Optional.ofNullable(resolve((ItemStack) obj));
        } else {
            return Optional.empty();
        }
    }

    private AgriSeed resolve(ItemStack stack) {
        if (!StackHelper.isValid(stack)) {
            return null;
        }
        final FuzzyStack toResolve = new FuzzyStack(stack);
        Optional<IAgriPlant> plant = AgriApi.getPlantRegistry().all().stream()
                .filter(p -> p.getSeedItems().contains(toResolve))
                .findFirst();
        if (plant.isPresent()) {
            Optional<IAgriStat> stats = AgriApi.getStatRegistry().valueOf(stack.getTagCompound());
            return new AgriSeed(plant.get(), stats.orElseGet(PlantStats::new));
        } else {
            return null;
        }
    }

}
