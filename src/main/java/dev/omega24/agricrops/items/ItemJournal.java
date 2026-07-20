package dev.omega24.agricrops.items;

import com.agricraft.agricore.core.AgriCore;
import com.google.common.base.Preconditions;
import dev.omega24.agricrops.AgriCraft;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.items.IAgriJournalItem;
import dev.omega24.agricrops.api.v1.plant.IAgriPlant;
import dev.omega24.agricrops.farming.AgriDiscoveryLedger;
import dev.omega24.agricrops.handler.GuiHandler;
import dev.omega24.agricrops.items.tabs.AgriTabs;
import dev.omega24.agricrops.reference.AgriNBT;
import dev.omega24.agricrops.utility.StackHelper;
import com.infinityraider.infinitylib.item.IItemWithModel;
import com.infinityraider.infinitylib.item.ItemBase;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemJournal extends ItemBase implements IAgriJournalItem, IItemWithModel {

    public ItemJournal() {
        super("journal");
        this.setMaxStackSize(1);
        this.setCreativeTab(AgriTabs.TAB_AGRICRAFT);
    }

    //this has to return true to make it so the getContainerItem method gets called when this item is used in a recipe
    @Override
    public boolean hasContainerItem(@Nonnull ItemStack stack) {
        Preconditions.checkNotNull(stack);
        return true;
    }

    //when this item is used in a crafting recipe it is replaced by the item return by this method
    @Override
    @Nonnull
    public ItemStack getContainerItem(@Nonnull ItemStack stack) {
        Preconditions.checkNotNull(stack);
        return stack.copy();
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            player.openGui(AgriCraft.instance, GuiHandler.JOURNAL_GUI_ID, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flag) {
        Preconditions.checkNotNull(stack);
        tooltip.add(AgriCore.getTranslator().translate("agricrops_tooltip.discoveredSeeds") + ": " + getDiscoveredSeedIds(stack).count());
    }

    @Nonnull
    private Stream<String> getDiscoveredSeedIds(@Nonnull ItemStack journal) {
        // Validate
        Preconditions.checkNotNull(journal);
        // Return
        return Optional.of(journal)
                .map(ItemStack::getTagCompound)
                .map(tag -> tag.getString(AgriNBT.DISCOVERED_SEEDS))
                .map(ids -> ids.split(";"))
                .map(Arrays::stream)
                .orElseGet(Stream::empty);
    }

    @Override
    public void addEntry(@Nonnull ItemStack journal, @Nullable IAgriPlant plant) {
        // Validate.
        Preconditions.checkNotNull(journal);
        // Do stuff.
        if (plant != null) {
            if (!isSeedDiscovered(journal, plant)) {
                NBTTagCompound tag = StackHelper.getTag(journal);
                String old = tag.getString(AgriNBT.DISCOVERED_SEEDS);
                tag.setString(AgriNBT.DISCOVERED_SEEDS, old + plant.getId() + ";");
                journal.setTagCompound(tag);
            }
            // Also feed the global, queryable discovery ledger (crop-mod-design.md §10) -
            // independent of this specific journal item, so OC scripts can query "known to
            // anyone" rather than only "known to this player's journal."
            final AgriDiscoveryLedger ledger = AgriDiscoveryLedger.get();
            if (ledger != null) {
                ledger.recordSpecies(plant.getId());
            }
        }
    }

    @Override
    public boolean isSeedDiscovered(@Nonnull ItemStack journal, @Nullable IAgriPlant plant) {
        // Validate
        Preconditions.checkNotNull(journal);
        // Return
        return (plant != null) && getDiscoveredSeedIds(journal).anyMatch(plant.getId()::equals);
    }

    @Override
    public List<IAgriPlant> getDiscoveredSeeds(@Nullable ItemStack journal) {
        return Optional.ofNullable(journal)
                .map(this::getDiscoveredSeedIds)
                .orElseGet(Stream::empty)
                .map(AgriApi.getPlantRegistry()::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}
