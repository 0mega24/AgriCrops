package dev.omega24.agricrops.command;

import com.mojang.authlib.GameProfile;
import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.content.GameContent;
import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.field.CropState;
import dev.omega24.agricrops.core.field.SoilCell;
import dev.omega24.agricrops.persistence.FieldSoils;
import dev.omega24.agricrops.world.CropTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import java.util.UUID;

/** Operator diagnostics; destructive acceptance setup additionally requires -PdevChecks. */
public final class AgricropsCommand extends CommandBase {
    @Override public String getName() { return "agricrops"; }
    @Override public String getUsage(ICommandSender sender) { return "agricrops.command.usage"; }
    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && args[0].equals("definitions")) {
            CropDefinitions.all().values().forEach(def -> sender.sendMessage(new TextComponentString(
                    def.id() + " seed=" + def.seed() + " duration=" + def.timeline().totalTicks()
                            + " costs=" + def.waterCost() + "/" + def.nutrientCost()
                            + " regrowthStage=" + def.regrowthStage() + "@" + def.regrowthTicks())));
            return;
        }
        if (args.length != 4) throw new WrongUsageException(getUsage(sender));
        BlockPos pos = parseBlockPos(sender, args, 1, false);
        World world = sender.getEntityWorld();
        if (!world.isBlockLoaded(pos)) throw new CommandException("Target chunk is not loaded");
        if (args[0].equals("inspect")) { sender.sendMessage(tile(world, pos).inspection()); return; }
        if (!Boolean.getBoolean("agricrops.devChecks")) throw new CommandException("Acceptance commands require the deliberate -PdevChecks development launch");
        try {
            if (args[0].equals("verify")) verify((WorldServer) world, pos);
            else if (args[0].equals("saved")) {
                CropTile tile = tile(world, pos);
                check(tile.crop() != null && tile.crop().progress() > 0 && !tile.quarantined(), "Saved specimen/progress missing");
                check(tile.soil().nutrients() < 5000 && tile.soil().water() > 0, "Saved resource state missing");
            } else throw new WrongUsageException(getUsage(sender));
        } catch (RuntimeException exception) {
            throw new CommandException("Agricrops acceptance failed: " + exception.getMessage());
        }
        CropTile tile = tile(world, pos);
        sender.sendMessage(new TextComponentString((args[0].equals("verify") ? "FIRST_SLICE_OK" : "SAVE_RELOAD_OK")
                + " pos=" + pos + " progress=" + tile.crop().progress() + " nutrients=" + tile.soil().nutrients()
                + " water=" + tile.soil().water() + " harvestRegrowth="
                + CropDefinitions.get(tile.crop().crop()).regrowthTicks()));
    }

    private static CropTile tile(World world, BlockPos pos) throws CommandException {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof CropTile)) throw new CommandException("No crop sticks at target");
        return (CropTile) tile;
    }

    private static void verify(WorldServer world, BlockPos pos) throws CommandException {
        check(pos.getY() > 2 && pos.getY() < 254, "Choose Y in 3..253");
        for (BlockPos target : new BlockPos[]{pos, pos.down(), pos.down().east()}) {
            check(world.isBlockLoaded(target) && world.isAirBlock(target), "Acceptance setup needs three empty loaded positions; nothing was replaced");
        }
        CropDefinition definition = CropDefinitions.get("agricrops:wheat");
        check(definition != null && definition.timeline().totalTicks() <= 12000, "Fixture requires wheat with at most 12000 active ticks");
        world.setBlockState(pos.down().east(), Blocks.WATER.getDefaultState());
        world.setBlockState(pos.down(), Blocks.FARMLAND.getDefaultState());
        world.setBlockState(pos, GameContent.CROP_STICKS.getDefaultState());
        FakePlayer player = new FakePlayer(world, new GameProfile(UUID.fromString("bf24f964-39bf-43c4-b04b-08486ee5b3ba"), "[AgriCheck]"));
        player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.WHEAT_SEEDS));
        activate(world, pos, player);
        CropTile tile = tile(world, pos);
        check(tile.crop() != null && player.getHeldItem(EnumHand.MAIN_HAND).isEmpty(), "Actual planting did not consume one seed");
        CropState planted = tile.crop();
        int steps = (int) definition.timeline().totalTicks() / 20 + 2;
        for (int i = 0; i < steps && tile.crop().progress() < definition.timeline().totalTicks(); i++) tile.advanceInterval();
        check(tile.crop().progress() >= definition.timeline().totalTicks(), "Growth did not mature (check light/resources)");
        NBTTagCompound encoded = tile.writeToNBT(new NBTTagCompound());
        CropTile restored = new CropTile(); restored.readFromNBT(encoded);
        check(!restored.quarantined() && restored.crop().genome().equals(planted.genome()), "Tile round trip changed genes");
        world.removeTileEntity(pos); world.setTileEntity(pos, restored); tile = restored;
        activate(world, pos, player);
        int products = countWheat(player);
        check(products > 0 && tile.crop().progress() == definition.regrowthTicks(), "Actual harvest did not produce/return to the configured stage");
        check(world.getBlockState(pos).getValue(GameContent.CROP_STICKS.AGE)
                == definition.regrowthStage(), "Harvest stored age did not rewind to the configured numeric stage");
        check(tile.crop().genome().equals(planted.genome()) && tile.crop().origin() == planted.origin(), "Harvest replaced the specimen");
        CropTile harvested = new CropTile(); harvested.readFromNBT(tile.writeToNBT(new NBTTagCompound()));
        check(!harvested.quarantined() && harvested.crop().progress() == definition.regrowthTicks(), "Post-harvest NBT lost the regrowth stage");
        player.inventory.mainInventory.set(1, player.getHeldItem(EnumHand.MAIN_HAND));
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        activate(world, pos, player);
        check(countWheat(player) == products, "Repeated harvest duplicated products");
        SoilCell paid = tile.soil();
        FieldSoils snapshot = new FieldSoils(); snapshot.readFromNBT(FieldSoils.get(world).writeToNBT(new NBTTagCompound()));
        check(snapshot.observe(pos.down(), paid.block()).nutrients() == paid.nutrients(), "Soil round trip lost depletion");
        // Exercise the drop calculation and replant route without leaving loose test items.
        net.minecraft.util.NonNullList<ItemStack> drops = net.minecraft.util.NonNullList.create();
        GameContent.CROP_STICKS.getDrops(drops, world, pos, world.getBlockState(pos), 0);
        check(drops.size() == 2, "Removal must return one seed and one set of sticks");
        ItemStack retained = drops.stream().filter(stack -> stack.getItem() == Items.WHEAT_SEEDS).findFirst().get();
        check(dev.omega24.agricrops.persistence.CropNbt.read(retained.getTagCompound().getCompoundTag(
                dev.omega24.agricrops.persistence.CropNbt.ITEM_KEY)).genome().equals(planted.genome()), "Seed drop lost genes");
        world.setBlockToAir(pos);
        check(GameContent.CROP_STICKS.canPlaceBlockAt(world, pos), "Sticks cannot be replaced on farmland");
        world.setBlockState(pos, GameContent.CROP_STICKS.getDefaultState());
        player.setHeldItem(EnumHand.MAIN_HAND, retained);
        activate(world, pos, player); tile = tile(world, pos);
        check(tile.crop() != null && tile.crop().genome().equals(planted.genome()), "Native seed replant lost specimen");
        check(tile.soil().nutrients() == paid.nutrients(), "Replant reset soil fertility");
        player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        int waterBefore = tile.soil().water(); activate(world, pos, player);
        check(player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.BUCKET
                && tile.soil().water() == Math.min(SoilCell.CAPACITY, waterBefore + 2000), "Watering failed to debit bucket/fill pool");
        player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DYE, 1, 15));
        int nutrientsBefore = tile.soil().nutrients(); activate(world, pos, player);
        check(player.getHeldItem(EnumHand.MAIN_HAND).isEmpty()
                && tile.soil().nutrients() == Math.min(SoilCell.CAPACITY, nutrientsBefore + 2000), "Fertilizer failed to debit item/fill pool");
        check(tile.crop().progress() == 0, "Care inputs skipped growth stages");
        tile.advanceInterval();
        check(tile.crop().progress() > 0, "Regrowth failed");
    }

    private static void activate(World world, BlockPos pos, FakePlayer player) {
        GameContent.CROP_STICKS.onBlockActivated(world, pos, world.getBlockState(pos), player,
                EnumHand.MAIN_HAND, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
    }
    private static int countWheat(FakePlayer player) {
        int total = 0;
        for (ItemStack stack : player.inventory.mainInventory) if (stack.getItem() == Items.WHEAT) total += stack.getCount();
        return total;
    }
    private static void check(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
}
