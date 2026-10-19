/*
 */
package dev.omega24.agricrops.compat.vanilla;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.adapter.IAgriAdapter;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.seed.AgriSeed;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import dev.omega24.agricrops.farming.GenomeFactory;
import dev.omega24.agricrops.items.ItemAgriSeed;
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
        // ItemAgriSeed is a higher-priority adapter and owns this dynamic item. In particular,
        // do not let an untagged generic stack match the identical AGRI_SEED entry included in
        // every plant's seed list: that made the creative-tab icon resolve to an arbitrary plant
        // instead of ItemAgriSeed's seed_unknown fallback.
        if (stack.getItem() instanceof ItemAgriSeed) {
            return null;
        }
        final FuzzyStack toResolve = new FuzzyStack(stack);
        Optional<IAgriPlant> plant = AgriApi.getPlantRegistry().all().stream()
                .filter(p -> p.getSeedItems().contains(toResolve))
                .findFirst();
        if (plant.isPresent()) {
            Optional<IAgriStat> stats = AgriApi.getStatRegistry().valueOf(stack.getTagCompound());
            return stats
                    .map(stat -> GenomeFactory.getInstance().createSeed(plant.get(), stat))
                    .orElseGet(() -> GenomeFactory.getInstance().createFreshSeed(plant.get()));
        } else {
            return null;
        }
    }

}
