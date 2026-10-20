package dev.omega24.agricrops.command;

import dev.omega24.agricrops.content.CropDefinitions;
import dev.omega24.agricrops.content.GameContent;
import dev.omega24.agricrops.world.CropTile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/** Operator diagnostics; destructive acceptance setup additionally requires -PdevChecks. */
public final class AgricropsCommand extends CommandBase {
    @Override public String getName() { return "agricrops"; }
    @Override public String getUsage(ICommandSender sender) { return "agricrops.command.usage"; }
    @Override public int getRequiredPermissionLevel() { return 2; }

    @Override public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && (args[0].equals("pocSpawn") || args[0].equals("pocSavedSpawn"))) {
            BlockPos spawn = sender.getEntityWorld().getSpawnPoint();
            args = new String[]{args[0].equals("pocSpawn") ? "poc" : "pocSaved", Integer.toString(spawn.getX()), "90", Integer.toString(spawn.getZ())};
        }
        if (args.length == 1 && args[0].equals("assets")) {
            sender.sendMessage(new TextComponentString("POC texture manifest: no mandatory new PNGs. Crop bodies use verified native stage models; retained seed/clipping icons follow the body fallback. Custom visuals beyond wheat/beetroot/carrot are not supported yet."));
            CropDefinitions.all().values().forEach(def -> sender.sendMessage(new TextComponentString(def.id() + " seed=" + def.seed() + "@" + def.rules().seedMeta + " product=" + def.product() + "@" + def.rules().productMeta + " borrowedProfile=" + def.rules().visual)));
            return;
        }
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
            if (args[0].equals("poc") || args[0].equals("pocSaved")) {
                if (args[0].equals("poc")) PocAcceptance.run((WorldServer) world, pos); else PocAcceptance.saved((WorldServer) world, pos);
                sender.sendMessage(new TextComponentString((args[0].equals("poc") ? "POC_WORLD_OK: paid parents, dynamic outputs, clipping, 3-generation fixation, fresh backcross, soil and finite controller" : "POC_SAVE_RELOAD_OK: identity, composition, traits, ancestry, paid production and controller retained") + " base=" + pos)); return;
            }
            if (args[0].equals("verify") || args[0].equals("saved")) throw new CommandException("The old first-slice fixture is superseded by /agricrops poc and pocSaved in a disposable world");
            throw new WrongUsageException(getUsage(sender));
        } catch (RuntimeException exception) {
            throw new CommandException("Agricrops acceptance failed: " + exception.getMessage());
        }
    }

    private static CropTile tile(World world, BlockPos pos) throws CommandException {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof CropTile)) throw new CommandException("No crop sticks at target");
        return (CropTile) tile;
    }

}
