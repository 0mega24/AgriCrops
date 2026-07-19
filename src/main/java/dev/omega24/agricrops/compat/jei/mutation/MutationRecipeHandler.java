/*
 * AgriCraft JEI crop mutation recipe handler integration module.
 */
package dev.omega24.agricrops.compat.jei.mutation;

import dev.omega24.agricrops.api.v1.mutation.IAgriMutation;
import dev.omega24.agricrops.compat.jei.AgriCraftJEIPlugin;
import javax.annotation.Nonnull;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 *
 *
 */
public class MutationRecipeHandler implements IRecipeHandler<IAgriMutation> {

    @Nonnull
    @Override
    public Class<IAgriMutation> getRecipeClass() {
        return IAgriMutation.class;
    }

    @Nonnull
    @Override
    public String getRecipeCategoryUid(IAgriMutation recipe) {
        return AgriCraftJEIPlugin.CATEGORY_MUTATION;
    }

    @Nonnull
    @Override
    public IRecipeWrapper getRecipeWrapper(@Nonnull IAgriMutation recipe) {
        return new MutationRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(@Nonnull IAgriMutation recipe) {
        return !recipe.getParents().isEmpty();
    }

}
