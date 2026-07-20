package dev.omega24.agricrops.blocks;

import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import com.infinityraider.infinitylib.item.IInfinityItem;
import dev.omega24.agricrops.items.blocks.ItemBlockAgricraft;
import dev.omega24.agricrops.items.tabs.AgriTabs;
import dev.omega24.agricrops.tiles.TileEntityCropScanner;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The Crop Scanner hub block (crop-mod-design.md §10). Plain vanilla-blockstate cube - no
 * custom renderer/GUI needed, since all of its behavior is exposed through its OC component
 * ({@link TileEntityCropScanner}).
 */
public class BlockCropScanner extends BlockBaseTile<TileEntityCropScanner> {

    private final ItemBlockAgricraft item;

    public BlockCropScanner() {
        super("crop_scanner", Material.IRON);
        this.setCreativeTab(AgriTabs.TAB_AGRICRAFT);
        this.setHardness(2.0F);
        this.item = new ItemBlockAgricraft(this);
    }

    @Override
    public TileEntityCropScanner createNewTileEntity(World world, int meta) {
        return new TileEntityCropScanner();
    }

    @Override
    protected InfinityProperty[] getPropertyArray() {
        return new InfinityProperty[0];
    }

    @Override
    @Nonnull
    public <T extends ItemBlock & IInfinityItem> Optional<T> getItemBlock() {
        return Optional.of((T) this.item);
    }

    @Override
    public List<String> getOreTags() {
        return Collections.emptyList();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            world.removeTileEntity(pos);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return !player.capabilities.isCreativeMode || super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        if (!world.isRemote) {
            if (!player.capabilities.isCreativeMode) {
                this.dropBlockAsItem(world, pos, state, 0);
            }
            this.breakBlock(world, pos, state);
        }
    }

}
