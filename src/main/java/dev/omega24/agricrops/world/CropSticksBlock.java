package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.field.CropState;
import dev.omega24.agricrops.core.genetics.SeedOrigin;
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
    private static final AxisAlignedBB BOX = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375);

    public CropSticksBlock() {
        super();
        setTickRandomly(false);
        setRegistryName("agricrops", "crop_sticks"); setUnlocalizedName("agricrops.crop_sticks");
        setHardness(0.5F); setSoundType(SoundType.WOOD); setCreativeTab(CreativeTabs.DECORATIONS);
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0).withProperty(PLANTED, false));
    }

    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, AGE, PLANTED); }
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
        if (tile.quarantined() || player.isSneaking() || held.getItem() == Items.STICK) {
            player.sendMessage(tile.inspection()); return true;
        }
        if (held.getItem() == Items.WATER_BUCKET) {
            if (tile.soil().water() == dev.omega24.agricrops.core.field.SoilCell.CAPACITY) return true;
            tile.addResources(2000, 0);
            if (!player.capabilities.isCreativeMode) player.setHeldItem(hand, new ItemStack(Items.BUCKET));
            player.sendMessage(tile.inspection()); return true;
        }
        if (held.getItem() == Items.DYE && held.getMetadata() == 15) {
            if (tile.soil().nutrients() == dev.omega24.agricrops.core.field.SoilCell.CAPACITY) return true;
            tile.addResources(0, 2000);
            if (!player.capabilities.isCreativeMode) held.shrink(1);
            player.sendMessage(tile.inspection()); return true;
        }
        if (tile.crop() == null && !held.isEmpty() && held.getMetadata() == 0) {
            String item = held.getItem().getRegistryName().toString();
            for (CropDefinition definition : CropDefinitions.all().values()) if (definition.seed().equals(item)) {
                if (!tile.soil().block().equals(definition.soil())) {
                    player.sendMessage(new TextComponentTranslation("agricrops.plant.soil")); return true;
                }
                try {
                    CropState seed;
                    if (held.hasTagCompound() && held.getTagCompound().hasKey(CropNbt.ITEM_KEY)) {
                        seed = CropNbt.read(held.getTagCompound().getCompoundTag(CropNbt.ITEM_KEY));
                        if (!seed.crop().equals(definition.id())) throw new IllegalArgumentException("Specimen/item mismatch");
                    } else seed = CropState.fresh(definition, SeedOrigin.WILD);
                    tile.plant(seed);
                    if (!player.capabilities.isCreativeMode) held.shrink(1);
                } catch (RuntimeException exception) {
                    player.sendMessage(new TextComponentTranslation("agricrops.seed.invalid"));
                }
                return true;
            }
        }
        if (tile.crop() != null && held.isEmpty()) {
            CropDefinition definition = CropDefinitions.get(tile.crop().crop());
            int count = tile.harvest();
            if (count > 0) {
                ItemStack product = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(definition.product())), count);
                if (!player.inventory.addItemStackToInventory(product)) player.dropItem(product, false);
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
                CropDefinition definition = CropDefinitions.get(tile.crop().crop());
                ItemStack seed = new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(definition.seed())));
                NBTTagCompound tag = new NBTTagCompound(); tag.setTag(CropNbt.ITEM_KEY, CropNbt.write(tile.crop().at(0))); seed.setTagCompound(tag);
                drops.add(seed);
            }
        }
        drops.add(sticks);
    }

    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state); world.removeTileEntity(pos);
    }
}
