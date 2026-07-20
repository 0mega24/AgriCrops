/*
 */
package dev.omega24.agricrops.core;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.crop.IAgriCrop;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.api.v1.render.RenderMethod;
import dev.omega24.agricrops.api.v1.requirement.IGrowthRequirement;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.api.v1.util.FuzzyStack;
import dev.omega24.agricrops.farming.growthrequirement.GrowthReqBuilder;
import dev.omega24.agricrops.init.AgriItems;
import dev.omega24.agricrops.reference.Constants;
import com.infinityraider.infinitylib.render.tessellation.ITessellator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nonnull;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An {@link IAgriPlant} defined directly in code (an Agricrops fork-native plant), as
 * opposed to {@link JsonPlant} which wraps an AgriCore JSON definition.
 *
 * <p>Mirrors {@link dev.omega24.agricrops.impl.v1.SoilProfile}: a minimal placeholder set of plants so crop sticks have
 * something to grow, until the design's species/attribute registries land
 * (crop-mod-design §7). All growth/seed/spread chances here are reasonable defaults,
 * not balanced numbers.
 */
public class CodePlant implements IAgriPlant {

    private final String id;
    private final String plantName;
    private final int growthStages;
    private final int tier;
    private final RenderMethod renderMethod;
    private final IntFunction<ResourceLocation> stageTexture;
    private final ResourceLocation seedTexture;
    private final List<ItemStack> products;
    private final IGrowthRequirement growthRequirement;
    private final List<FuzzyStack> seedItems;
    private final List<BiomeDictionary.Type> preferredBiomeTypes;
    private final int optimalElevationMin;
    private final int optimalElevationMax;

    public CodePlant(@Nonnull String id, @Nonnull String plantName, @Nonnull List<String> soilIds,
                    int growthStages, @Nonnull RenderMethod renderMethod,
                    @Nonnull IntFunction<ResourceLocation> stageTexture, @Nonnull ResourceLocation seedTexture,
                    @Nonnull List<ItemStack> products, @Nonnull List<ItemStack> unverifiedSeedItems) {
        this(id, plantName, soilIds, growthStages, renderMethod, stageTexture, seedTexture, products,
                unverifiedSeedItems, Collections.emptyList(), 0, 255);
    }

    public CodePlant(@Nonnull String id, @Nonnull String plantName, @Nonnull List<String> soilIds,
                    int growthStages, @Nonnull RenderMethod renderMethod,
                    @Nonnull IntFunction<ResourceLocation> stageTexture, @Nonnull ResourceLocation seedTexture,
                    @Nonnull List<ItemStack> products, @Nonnull List<ItemStack> unverifiedSeedItems,
                    @Nonnull List<BiomeDictionary.Type> preferredBiomeTypes, int optimalElevationMin, int optimalElevationMax) {
        this.id = Objects.requireNonNull(id);
        this.plantName = Objects.requireNonNull(plantName);
        this.growthStages = growthStages;
        this.tier = 0;
        this.renderMethod = Objects.requireNonNull(renderMethod);
        this.stageTexture = Objects.requireNonNull(stageTexture);
        this.seedTexture = Objects.requireNonNull(seedTexture);
        this.products = Objects.requireNonNull(products);

        // The AGRI_SEED item (properly textured/analyzed once bred/dropped in-mod) stays
        // primary — getSeed()/harvest-seed-drop always hand out this one. Vanilla/compatible
        // items (e.g. Items.WHEAT_SEEDS) are accepted *additionally*, same as upstream
        // AgriCraft: planting one directly on a crop stick works via SeedWrapper resolving it
        // against this list, but yields an unanalyzed (PlantStats() default) genome — an
        // "unverified" seed you then have to run through the Seed Analyzer.
        final List<FuzzyStack> seeds = new java.util.ArrayList<>(unverifiedSeedItems.size() + 1);
        seeds.add(new FuzzyStack(new ItemStack(AgriItems.getInstance().AGRI_SEED)));
        unverifiedSeedItems.stream().map(FuzzyStack::new).forEach(seeds::add);
        this.seedItems = Collections.unmodifiableList(seeds);

        final GrowthReqBuilder builder = new GrowthReqBuilder();
        soilIds.stream()
                .map(sid -> AgriApi.getSoilCompositionRegistry().get(sid))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .forEach(builder::addSoilComposition);
        this.growthRequirement = builder.build();

        this.preferredBiomeTypes = Collections.unmodifiableList(new java.util.ArrayList<>(Objects.requireNonNull(preferredBiomeTypes)));
        this.optimalElevationMin = optimalElevationMin;
        this.optimalElevationMax = optimalElevationMax;
    }

    @Override
    public boolean isWeed() {
        return false;
    }

    @Override
    public boolean isFertilizable() {
        return true;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public String getPlantName() {
        return this.plantName;
    }

    @Nonnull
    @Override
    public String getSeedName() {
        return this.plantName + " Seeds";
    }

    @Nonnull
    @Override
    public Collection<FuzzyStack> getSeedItems() {
        return this.seedItems;
    }

    @Nonnull
    @Override
    public ItemStack getSeed() {
        return this.seedItems.get(0).toStack();
    }

    @Nonnull
    @Override
    public String getInformation() {
        return "A fork-native baseline plant (" + this.plantName + ").";
    }

    @Override
    public double getSpreadChance() {
        return 0.0;
    }

    @Override
    public double getSpawnChance() {
        return 0.0;
    }

    @Override
    public double getGrassDropChance() {
        return 0.0;
    }

    @Override
    public double getGrowthChanceBase() {
        return 0.05;
    }

    @Override
    public double getGrowthChanceBonus() {
        return 0.05;
    }

    @Nonnull
    @Override
    public Collection<BiomeDictionary.Type> getPreferredBiomeTypes() {
        return this.preferredBiomeTypes;
    }

    @Override
    public int getOptimalElevationMin() {
        return this.optimalElevationMin;
    }

    @Override
    public int getOptimalElevationMax() {
        return this.optimalElevationMax;
    }

    @Override
    public double getSeedDropChanceBase() {
        return 0.05;
    }

    @Override
    public double getSeedDropChanceBonus() {
        return 0.0125;
    }

    @Override
    public int getGrowthStages() {
        return this.growthStages;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public void getPossibleProducts(@Nonnull Consumer<ItemStack> products) {
        this.products.forEach(s -> products.accept(s.copy()));
    }

    @Override
    public void getHarvestProducts(@Nonnull Consumer<ItemStack> products, @Nonnull IAgriCrop crop, @Nonnull IAgriStat stat, @Nonnull Random rand) {
        this.products.forEach(s -> products.accept(s.copy()));
    }

    @Nonnull
    @Override
    public IGrowthRequirement getGrowthRequirement() {
        return this.growthRequirement;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getSeedTexture() {
        return this.seedTexture;
    }

    @Override
    public float getHeight(int meta) {
        return Constants.UNIT * 13;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public RenderMethod getRenderMethod() {
        return this.renderMethod;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getPrimaryPlantTexture(int meta) {
        return this.stageTexture.apply(meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getSecondaryPlantTexture(int meta) {
        return null;
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    @Override
    public List<BakedQuad> getPlantQuads(IExtendedBlockState state, int growthStage, EnumFacing direction, Function<ResourceLocation, TextureAtlasSprite> textureToIcon) {
        // Same delegation pattern as JsonPlant: the renderer directly adds quads to the
        // tessellator, so an empty list is returned here.
        if (textureToIcon instanceof ITessellator) {
            dev.omega24.agricrops.renderers.PlantRenderer.renderPlant((ITessellator) textureToIcon, this, growthStage);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof IAgriPlant) && (this.getId().equals(((IAgriPlant) obj).getId()));
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
