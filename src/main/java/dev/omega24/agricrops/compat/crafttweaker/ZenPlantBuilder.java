package dev.omega24.agricrops.compat.crafttweaker;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.item.WeightedItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.plant.CropLootTier;
import dev.omega24.agricrops.api.v1.render.RenderMethod;
import dev.omega24.agricrops.core.CodePlant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * Fluent ZenScript-facing plant definition (crop-mod-design.md §11 step 8) - mirrors
 * {@code DefaultPlants}'s {@code forkTextured(...)} Java pattern, but as chained method calls
 * since ZenScript has no constructor-with-fifteen-positional-args idiom. Nothing is registered
 * until {@link #register()} is called.
 */
@ZenClass("mods.agricrops.plants.PlantBuilder")
@ZenRegister
public class ZenPlantBuilder {

    private final String id;
    private final String name;

    private List<String> soilIds = Collections.emptyList();
    private int growthStages = 4;
    private RenderMethod renderMethod = RenderMethod.CROSSED;
    private String stageTextureBase = null;
    private String seedTexture = null;
    private List<ItemStack> products = Collections.emptyList();
    private List<ItemStack> unverifiedSeedItems = Collections.emptyList();
    private List<BiomeDictionary.Type> preferredBiomeTypes = Collections.emptyList();
    private int elevationMin = 0;
    private int elevationMax = 255;
    private final List<CropLootTier> lootTiers = new ArrayList<>();
    private boolean weed = false;
    private boolean aggressive = false;

    ZenPlantBuilder(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Soil composition tags this plant can grow on (crop-mod-design.md §3), e.g.
     * {@code ["tilled", "sand"]} - registered via {@code mods.agricrops.Soils.addComposition}
     * (or one of the fork's built-ins: dirt/grass/tilled/sand/gravel/clay/stone/soul_sand).
     * Leave unset/empty for "any registered soil is acceptable."
     */
    @ZenMethod
    public ZenPlantBuilder soil(String[] soilIds) {
        this.soilIds = Arrays.asList(soilIds);
        return this;
    }

    @ZenMethod
    public ZenPlantBuilder growthStages(int growthStages) {
        this.growthStages = growthStages;
        return this;
    }

    /**
     * How the crop is rendered: CROSSED (default, X-shape like a flower), HASHTAG (like
     * vanilla wheat), STEM (vine + fruit cube, like pumpkin/melon), TALL_CROSSED,
     * TALL_HASHTAG. Case-insensitive; invalid values fall back to CROSSED with a warning.
     */
    @ZenMethod
    public ZenPlantBuilder renderMethod(String renderMethod) {
        try {
            this.renderMethod = RenderMethod.valueOf(renderMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            CraftTweakerAPI.logError("agricrops: unknown render method '" + renderMethod + "' for plant " + this.id + ", falling back to CROSSED.");
            this.renderMethod = RenderMethod.CROSSED;
        }
        return this;
    }

    /**
     * Points this plant's growth-stage textures at an EXISTING texture set already vendored
     * in the mod (or any other installed mod's resources) instead of requiring new art -
     * {@code textureBase} is the resource location up to the stage number, e.g.
     * {@code "agricrops:blocks/crop_wheat"} resolves to {@code crop_wheat1.png"},
     * {@code "crop_wheat2.png"}, etc. up to {@link #growthStages}. {@code seedTexture} is the
     * full resource location for the seed item icon, e.g. {@code "agricrops:items/seed_wheat"}.
     */
    @ZenMethod
    public ZenPlantBuilder textures(String textureBase, String seedTexture) {
        this.stageTextureBase = textureBase;
        this.seedTexture = seedTexture;
        return this;
    }

    /**
     * The item(s) this plant drops at its (single, default) harvest tier - see
     * {@link #lootTier(int, boolean, WeightedItemStack[])} for staged loot.
     */
    @ZenMethod
    public ZenPlantBuilder products(IItemStack[] products) {
        this.products = toItemStacks(products);
        return this;
    }

    /**
     * Vanilla/other-mod items that can additionally be planted directly on a crop stick as an
     * "unverified" seed for this plant (yields a fresh, unanalyzed genome - same as upstream
     * AgriCraft's seed-wrapper behavior). The mod's own analyzed seed item is always accepted
     * regardless of this list.
     */
    @ZenMethod
    public ZenPlantBuilder unverifiedSeeds(IItemStack[] seeds) {
        this.unverifiedSeedItems = toItemStacks(seeds);
        return this;
    }

    /**
     * Biome dictionary tags this plant prefers (crop-mod-design.md §5), e.g.
     * {@code ["SANDY", "DRY"]} - see {@code net.minecraftforge.common.BiomeDictionary.Type}
     * for the full tag list. Empty (default) means "no preference" (generalist).
     */
    @ZenMethod
    public ZenPlantBuilder biome(String[] biomeTypes) {
        final List<BiomeDictionary.Type> resolved = new ArrayList<>(biomeTypes.length);
        for (String type : biomeTypes) {
            resolved.add(BiomeDictionary.Type.getType(type));
        }
        this.preferredBiomeTypes = resolved;
        return this;
    }

    /**
     * Optimal elevation band (crop-mod-design.md §5), Y min/max inclusive. Default 0..255 (no
     * preference).
     */
    @ZenMethod
    public ZenPlantBuilder elevation(int min, int max) {
        this.elevationMin = min;
        this.elevationMax = max;
        return this;
    }

    /**
     * Adds a staged loot tier (crop-mod-design.md §6): once the crop reaches
     * {@code minStage}, harvesting rolls {@code drops} (CraftTweaker's own weighted-item-stack
     * type - build one with {@code someItem.percent(25.0)} or {@code someItem.weight(1.0)}).
     * {@code resetsGrowth} controls whether reaching this tier resets the crop back to stage 0
     * (normally only the final/max-stage tier should). Call multiple times for multiple tiers -
     * the highest {@code minStage} not exceeding the crop's current stage wins at harvest time.
     * If never called, the plant falls back to the single-tier-at-max-stage default (harvest at
     * full growth, using {@link #products(IItemStack[])}).
     */
    @ZenMethod
    public ZenPlantBuilder lootTier(int minStage, boolean resetsGrowth, WeightedItemStack[] drops) {
        final WeightedItemStack[] dropsCopy = drops.clone();
        this.lootTiers.add(new CropLootTier(minStage, (products, crop, stat, rand) -> {
            for (IItemStack drop : WeightedItemStack.pickRandomDrops(rand, dropsCopy)) {
                products.accept(CraftTweakerMC.getItemStack(drop));
            }
        }, resetsGrowth));
        return this;
    }

    /**
     * Marks this plant a weed (crop-mod-design.md §8): can only be harvested with a rake, and
     * (if also {@link #aggressive(boolean)}) spreads to/overtakes neighboring crops. The fork's
     * own weed-risk/destabilizing mechanic only ever converts crops into the one built-in
     * {@code agricrops:weed} plant - this flag is for pack devs who want their OWN standalone
     * weed-like plant, not a replacement for that conversion.
     */
    @ZenMethod
    public ZenPlantBuilder weed(boolean weed) {
        this.weed = weed;
        return this;
    }

    /**
     * Whether this plant overtakes neighboring non-matching crops when mature (crop-mod-design.md
     * §8) - traditionally paired with {@link #weed(boolean)}, but independently settable.
     */
    @ZenMethod
    public ZenPlantBuilder aggressive(boolean aggressive) {
        this.aggressive = aggressive;
        return this;
    }

    /**
     * Builds and registers the plant. No-op (with a logged error) if required fields
     * (textures, at least one product) were never set.
     */
    @ZenMethod
    public void register() {
        if (this.stageTextureBase == null || this.seedTexture == null) {
            CraftTweakerAPI.logError("agricrops: plant '" + this.id + "' never had .textures(...) called, skipping registration.");
            return;
        }
        if (this.products.isEmpty()) {
            CraftTweakerAPI.logError("agricrops: plant '" + this.id + "' never had .products(...) called, skipping registration.");
            return;
        }
        final String textureBase = this.stageTextureBase;
        final ResourceLocation seedLoc = new ResourceLocation(this.seedTexture);
        final CodePlant plant = new CodePlant(this.id, this.name, this.soilIds, this.growthStages,
                this.renderMethod, stage -> new ResourceLocation(textureBase + (stage + 1)), seedLoc,
                this.products, this.unverifiedSeedItems, this.preferredBiomeTypes, this.elevationMin, this.elevationMax,
                this.lootTiers, this.weed, this.aggressive);
        CTHelper.apply("Registering agricrops plant: " + this.id, () -> {
            if (!AgriApi.getPlantRegistry().add(plant)) {
                CraftTweakerAPI.logWarning("agricrops: plant '" + this.id + "' is already registered, skipping.");
            }
        });
    }

    private static List<ItemStack> toItemStacks(IItemStack[] stacks) {
        final List<ItemStack> result = new ArrayList<>(stacks.length);
        for (IItemStack stack : stacks) {
            result.add(CraftTweakerMC.getItemStack(stack));
        }
        return result;
    }

}
