package dev.omega24.agricrops.items;

import com.agricraft.agricore.core.AgriCore;
import dev.omega24.agricrops.AgriCraft;
import dev.omega24.agricrops.reference.AgriNuggetType;
import dev.omega24.agricrops.utility.OreDictUtil;
import com.infinityraider.infinitylib.item.IAutoRenderedItem;
import com.infinityraider.infinitylib.item.ItemBase;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import com.infinityraider.infinitylib.utility.IRecipeRegisterer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

public class ItemNugget extends ItemBase implements IAutoRenderedItem, IRecipeRegisterer {

    public ItemNugget() {
        super("agri_nugget");
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> varients) {
        if (tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (AgriNuggetType type : AgriNuggetType.values()) {
                // Gate: only expose a nugget when its metal exists in the ore dictionary
                // (i.e. the mod providing that ingot is present). Avoids orphan,
                // recipe-less nuggets in a bare world; still shows them under GTNH etc.
                if (!OreDictUtil.getFirstOre(type.ingot).isPresent()) {
                    continue;
                }
                ItemStack stack = new ItemStack(this, 1, type.ordinal());
                varients.add(stack);
            }
        }
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return AgriNuggetType.getNugget(stack.getMetadata()).getUnlocalizedName();
    }

    @Override
    public String getModelId(ItemStack stack) {
        return AgriNuggetType.getNugget(stack.getMetadata()).nugget;
    }

    @Override
    public String getBaseTexture(ItemStack stack) {
        return AgriNuggetType.getNugget(stack.getMetadata()).texture;
    }

    @Override
    public List<ResourceLocation> getAllTextures() {
        List<ResourceLocation> textures = new ArrayList<>(AgriNuggetType.values().length);
        for (AgriNuggetType type : AgriNuggetType.values()) {
            textures.add(new ResourceLocation(type.texture));
        }
        return textures;
    }

    @Override
    public void registerRecipes(IForgeRegistry<IRecipe> registry) {
        for (AgriNuggetType type : AgriNuggetType.values()) {
            // Gate the whole nugget on its metal being present in the ore dictionary,
            // so orphan (unusable) nugget types aren't registered at all in a bare world.
            ItemStack ingot = OreDictUtil.getFirstOre(type.ingot).orElse(ItemStack.EMPTY);
            if (ingot.isEmpty()) {
                continue;
            }

            // 1) Ore Dictionary registration.
            AgriCore.getLogger("agricrops").info("Registering in Ore Dictionary: {0}", type.nugget);
            ItemStack oneNugget = new ItemStack(this, 1, type.ordinal());
            OreDictionary.registerOre(type.nugget, oneNugget);

            // 2) Recipe: nine nuggets -> one ingot.
            {
                AgriCore.getLogger("agricrops").info("Adding a recipe to convert nine {0} into one {1}", type.nugget, type.ingot);
                final ResourceLocation group = new ResourceLocation(AgriCraft.instance.getModId(), "combine_nugget");
                final ResourceLocation name = new ResourceLocation(AgriCraft.instance.getModId(), "combine_nugget_" + type.name().toLowerCase());
                final ShapedOreRecipe recipe = new ShapedOreRecipe(
                        group,
                        ingot,
                        "nnn",
                        "nnn",
                        "nnn",
                        'n', type.nugget
                );
                recipe.setRegistryName(name);
                AgriCore.getLogger("agricrops").info("Registering nugget recipe: {0}!", recipe.getRegistryName());
                registry.register(recipe);
            }
        }
    }

}
