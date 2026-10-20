package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.content.GameContent;
import dev.omega24.agricrops.content.SpecimenItems;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.field.CropState;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
import dev.omega24.agricrops.core.genetics.Specimen;
import dev.omega24.agricrops.persistence.CropNbt;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import javax.annotation.Nullable;

public final class CropSticksBlock extends net.minecraft.block.BlockCrops implements ITileEntityProvider {
    public static final PropertyBool PLANTED = PropertyBool.create("planted");
    public enum Visual implements net.minecraft.util.IStringSerializable {
        WHEAT, BEETROOT, CARROT;
        public String getName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final net.minecraft.block.properties.PropertyEnum<Visual> VISUAL = net.minecraft.block.properties.PropertyEnum.create("crop", Visual.class);
    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);

    public CropSticksBlock() {
        super();
        setTickRandomly(false);
        setRegistryName("agricrops", "crop_sticks"); setUnlocalizedName("agricrops.crop_sticks");
        setHardness(0.5F); setSoundType(SoundType.WOOD); setCreativeTab(CreativeTabs.DECORATIONS);
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0).withProperty(PLANTED, false).withProperty(VISUAL, Visual.WHEAT));
    }

    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, AGE, PLANTED, VISUAL); }
    @Override public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof CropTile) try { return state.withProperty(VISUAL, Visual.valueOf(((CropTile) tile).visual().toUpperCase(java.util.Locale.ROOT))); } catch (IllegalArgumentException ignored) { }
        return state;
    }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(AGE, meta & 7).withProperty(PLANTED, (meta & 8) != 0); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(AGE) | (state.getValue(PLANTED) ? 8 : 0); }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new CropTile(); }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOX; }
    @Override @Nullable public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return NULL_AABB; }
    @Override public net.minecraft.util.BlockRenderLayer getBlockLayer() { return net.minecraft.util.BlockRenderLayer.CUTOUT; }
    @Override protected boolean canSustainBush(IBlockState state) {
        return state.getBlock() == Blocks.FARMLAND || state.getMaterial().isSolid();
    }
    // BlockCrops keeps vanilla farmland from reverting under a supported crop; its own
    // random growth/bonemeal paths are disabled so they cannot bypass paid development.
    @Override public void updateTick(World world, BlockPos pos, IBlockState state, java.util.Random random) {}
    @Override public void grow(World world, BlockPos pos, IBlockState state) {}
    @Override public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean client) { return false; }
    @Override public boolean canUseBonemeal(World world, java.util.Random random, BlockPos pos, IBlockState state) { return false; }
    @Override public ItemStack getPickBlock(IBlockState state, net.minecraft.util.math.RayTraceResult target,
                                           World world, BlockPos pos, EntityPlayer player) { return new ItemStack(this); }

    @Override public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState below = world.getBlockState(pos.down());
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos)
                && (below.getBlock() == Blocks.FARMLAND || below.isSideSolid(world, pos.down(), EnumFacing.UP));
    }

    @Override public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        IBlockState below = world.getBlockState(pos.down());
        return below.getBlock() == Blocks.FARMLAND || below.isSideSolid(world, pos.down(), EnumFacing.UP);
    }

    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos from) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof CropTile) ((CropTile) tile).soil();
            IBlockState below = world.getBlockState(pos.down());
            if (below.getBlock() != Blocks.FARMLAND && !below.isSideSolid(world, pos.down(), EnumFacing.UP)) world.destroyBlock(pos, true);
        }
    }

    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                              EnumHand hand, EnumFacing facing, float x, float y, float z) {
        if (world.isRemote) return true;
        TileEntity entity = world.getTileEntity(pos);
        if (!(entity instanceof CropTile) || !player.canPlayerEdit(pos, facing, player.getHeldItem(hand))) return false;
        CropTile tile = (CropTile) entity;
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() == GameContent.JOURNAL) { FieldNotes.journal(player); return true; }
        if (held.getItem() == GameContent.PROBE) { FieldNotes.probe(player, tile); return true; }
        if (held.getItem() == GameContent.ANALYZER) { FieldNotes.analyse(player, tile); return true; }
        if (tile.crop() == null && held.getItem() == Item.getItemFromBlock(this) && !tile.crossing() && !tile.quarantined()) {
            tile.enableCrossing(false); if (!player.capabilities.isCreativeMode) held.shrink(1); player.sendMessage(tile.inspection()); return true;
        }
        if (tile.crop() == null && tile.crossing() && player.isSneaking() && held.getItem() == Items.DYE && held.getMetadata() == 15) {
            tile.enableCrossing(!tile.backcross()); player.sendMessage(tile.inspection()); return true;
        }
        if (tile.quarantined() || player.isSneaking() || held.getItem() == Items.STICK) {
            player.sendMessage(tile.inspection()); return true;
        }
        if (held.getItem() == Items.SHEARS) {
            Specimen clipping = tile.clip(); if (clipping != null) { give(player, SpecimenItems.of(clipping)); if (!player.capabilities.isCreativeMode) held.damageItem(1, player); }
            else player.sendMessage(new TextComponentTranslation("agricrops.clip.wait")); return true;
        }
        if (held.getItem() == GameContent.COMPOST) {
            if (tile.compost() && !player.capabilities.isCreativeMode) held.shrink(1); player.sendMessage(tile.inspection()); return true;
        }
        if (held.getItem() == Items.WATER_BUCKET) {
            dev.omega24.agricrops.core.field.SoilProfile profile = dev.omega24.agricrops.content.PocContent.soils().get(tile.soil().block());
            if (profile == null || tile.soil().water() >= profile.waterCapacity) return true;
            tile.addResources(1000, 0);
            if (!player.capabilities.isCreativeMode) player.setHeldItem(hand, new ItemStack(Items.BUCKET));
            player.sendMessage(tile.inspection()); return true;
        }
        if (tile.crop() == null && (held.getItem() == GameContent.SPECIMEN || held.getItem() == GameContent.CLIPPING || held.getItem() == GameContent.FOUNDATION)) {
            try {
                Specimen specimen = held.getItem() == GameContent.FOUNDATION ? Specimen.fresh(CropDefinitions.get("agricrops:wheat"), SeedOrigin.CRAFTED, world.rand.nextLong()) : SpecimenItems.read(held);
                if (!tile.soil().block().equals(CropDefinitions.get(specimen.body).soil())) { player.sendMessage(new TextComponentTranslation("agricrops.plant.soil")); return true; }
                tile.plant(CropState.plant(specimen, 0)); if (!player.capabilities.isCreativeMode) held.shrink(1);
            } catch (RuntimeException exception) { player.sendMessage(new TextComponentTranslation("agricrops.seed.invalid")); }
            return true;
        }
        if (held.getItem() == Items.DYE && held.getMetadata() == 15) {
            dev.omega24.agricrops.core.field.SoilProfile profile = dev.omega24.agricrops.content.PocContent.soils().get(tile.soil().block());
            if (profile == null || tile.soil().nutrients() >= profile.nutrientCapacity) return true;
            tile.addResources(0, 2000);
            if (!player.capabilities.isCreativeMode) held.shrink(1);
            player.sendMessage(tile.inspection()); return true;
        }
        if (tile.crop() == null && !held.isEmpty()) {
            for (CropDefinition definition : CropDefinitions.all().values()) if (dev.omega24.agricrops.content.NativeItems.seed(definition, held)) {
                if (!tile.soil().block().equals(definition.soil())) {
                    player.sendMessage(new TextComponentTranslation("agricrops.plant.soil")); return true;
                }
                try {
                    CropState seed;
                    if (held.hasTagCompound() && held.getTagCompound().hasKey(CropNbt.ITEM_KEY)) {
                        seed = CropNbt.read(held.getTagCompound().getCompoundTag(CropNbt.ITEM_KEY));
                        if (!seed.crop().equals(definition.id())) throw new IllegalArgumentException("Specimen/item mismatch");
                    } else seed = CropState.plant(Specimen.fresh(definition, definition.seed().startsWith("minecraft:") ? SeedOrigin.WILD : SeedOrigin.IMPORTED, world.rand.nextLong()), 0);
                    tile.plant(seed);
                    if (!player.capabilities.isCreativeMode) held.shrink(1);
                } catch (RuntimeException exception) {
                    player.sendMessage(new TextComponentTranslation("agricrops.seed.invalid"));
                }
                return true;
            }
        }
        if (tile.crop() != null && held.isEmpty()) {
            java.util.Map<String, Integer> products = tile.harvestProducts();
            if (!products.isEmpty()) {
                products.forEach((id, count) -> give(player, dev.omega24.agricrops.content.NativeItems.product(CropDefinitions.get(id), count)));
                return true;
            }
        }
        player.sendMessage(tile.inspection()); return true;
    }

    @Override public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity entity, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, entity, tool);
        world.setBlockToAir(pos);
    }

    @Override public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ItemStack sticks = new ItemStack(this);
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof CropTile) {
            CropTile tile = (CropTile) entity;
            if (tile.quarantined()) {
                NBTTagCompound blockEntity = new NBTTagCompound(); blockEntity.setTag(CropTile.DATA_KEY, tile.data());
                NBTTagCompound item = new NBTTagCompound(); item.setTag("BlockEntityTag", blockEntity); sticks.setTagCompound(item);
            } else if (tile.crop() != null) {
                drops.add(SpecimenItems.of(tile.crop().specimen()));
            }
        }
        drops.add(sticks);
    }

    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state); world.removeTileEntity(pos);
    }
    private static void give(EntityPlayer player, ItemStack stack) { if (!player.inventory.addItemStackToInventory(stack)) player.dropItem(stack, false); }
}
