/*
 * AgriCraft JEI crop mutation recipe handler integration module.
 */
package dev.omega24.agricrops.compat.jei.produce;

import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.compat.jei.AgriCraftJEIPlugin;
import javax.annotation.Nonnull;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 *
 *
 */
public class ProduceRecipeHandler implements IRecipeHandler<IAgriPlant> {

    @Nonnull
    @Override
    public Class<IAgriPlant> getRecipeClass() {
        return IAgriPlant.class;
    }

    @Override
    public String getRecipeCategoryUid(IAgriPlant recipe) {
        return AgriCraftJEIPlugin.CATEGORY_PRODUCE;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull IAgriPlant recipe) {
        return new ProduceRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(@Nonnull IAgriPlant recipe) {
        return true;
    }

}
