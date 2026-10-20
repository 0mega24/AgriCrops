package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.GameContent;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public final class ReservoirBlock extends Block implements ITileEntityProvider {
    public ReservoirBlock() {
        super(Material.WOOD); setRegistryName("agricrops", "reservoir"); setUnlocalizedName("agricrops.reservoir");
        setHardness(1); setSoundType(SoundType.WOOD); setCreativeTab(CreativeTabs.DECORATIONS);
    }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new ReservoirTile(); }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float x, float y, float z) {
        if (world.isRemote) return true;
        if (!player.canPlayerEdit(pos, facing, player.getHeldItem(hand))) return false;
        TileEntity entity = world.getTileEntity(pos); if (!(entity instanceof ReservoirTile)) return false;
        ReservoirTile tile = (ReservoirTile) entity; ItemStack held = player.getHeldItem(hand);
        if (tile.quarantined()) { player.sendMessage(new TextComponentTranslation("agricrops.inspect.quarantined")); return true; }
        if (held.getItem() == Items.WATER_BUCKET && tile.water() <= 7000) {
            tile.addWater(1000); if (!player.capabilities.isCreativeMode) player.setHeldItem(hand, new ItemStack(Items.BUCKET));
        } else if ((held.getItem() == Items.DYE && held.getMetadata() == 15) || held.getItem() == GameContent.COMPOST) {
            if (tile.addNutrients(2000, held.getItem() == GameContent.COMPOST) > 0 && !player.capabilities.isCreativeMode) held.shrink(1);
        }
        player.sendMessage(new TextComponentTranslation("agricrops.reservoir.status", tile.water(), tile.nutrients(), tile.organic())); return true;
    }
    @Override public void getDrops(NonNullList<ItemStack> drops, net.minecraft.world.IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ItemStack item = new ItemStack(this); TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ReservoirTile && ((ReservoirTile) tile).quarantined()) {
            net.minecraft.nbt.NBTTagCompound tag = new net.minecraft.nbt.NBTTagCompound(); tag.setTag("BlockEntityTag", tile.writeToNBT(new net.minecraft.nbt.NBTTagCompound())); item.setTagCompound(tag);
        }
        drops.add(item);
    }
    @Override public void breakBlock(World world, BlockPos pos, IBlockState state) { super.breakBlock(world, pos, state); world.removeTileEntity(pos); }
    // Contents are intentionally lost when broken; only persisted placement retains stocks.
}
