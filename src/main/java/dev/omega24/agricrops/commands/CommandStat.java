/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.omega24.agricrops.commands;

import com.agricraft.agricore.util.MathHelper;
import com.agricraft.agricore.util.TypeHelper;
import com.google.common.collect.ComparisonChain;
import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.utility.StackHelper;
import com.infinityraider.infinitylib.utility.MessageUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

/**
 *
 * @author Ryan
 */
public class CommandStat implements ICommand {

    @Override
    public String getName() {
        return "ac.stat";
    }

    @Override
    public String getUsage(ICommandSender ics) {
        return "ac.stat <set|get> <" + statIdList() + "|all> <value>";
    }

    private static String statIdList() {
        final StringBuilder sb = new StringBuilder();
        for (CropStat stat : CropStat.values()) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(stat.getId());
        }
        return sb.toString();
    }

    private static Optional<CropStat> findStat(String id) {
        return Arrays.stream(CropStat.values()).filter(s -> s.getId().equals(id)).findFirst();
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer ms, ICommandSender ics, String[] arguments) throws CommandException {
        // Validate the action argument.
        if (arguments.length < 1) {
            throw new CommandException("Too few arguments to command! Expected \"set\" or \"get\" as first argument!");
        }

        // Get the action argument.
        final String action = arguments[0].trim().toLowerCase();

        // Validate the action argument matches the number of arguments.
        if (action.equals("get") && arguments.length != 2) {
            throw new CommandException("Incorrect number of arguments!");
        } else if (action.equals("set") && arguments.length != 3) {
            throw new CommandException("Incorrect number of arguments!");
        }

        // Validate the command sender.
        @Nonnull
        final EntityPlayer player = TypeHelper.cast(ics.getCommandSenderEntity(), EntityPlayer.class)
                .orElseThrow(() -> new CommandException("Command must be invoked by a player!"));

        // Validate the command target stack.
        @Nonnull
        final ItemStack stack = Optional.ofNullable(player.getHeldItemMainhand())
                .filter(StackHelper::isValid)
                .orElseThrow(() -> new CommandException("Player must be holding valid itemstack to be used as a target!"));

        // Validate the command stat.
        @Nonnull
        final IAgriStat stat = AgriApi.getStatRegistry().valueOf(stack)
                .orElseThrow(() -> new CommandException("Player must be holding an AgriCraft seed item."));

        // Switch the action.
        switch (action) {
            case "get":
                getStat(player, stack, stat, arguments[1].trim().toLowerCase());
                return;
            case "set":
                final int value = MathHelper.parseIntOr(arguments[2], -1);
                setStat(player, stack, stat, arguments[1].trim().toLowerCase(), value);
                return;
            default:
                throw new CommandException("Invalid action: \"%s\"!", action);
        }
    }

    private void setStat(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, @Nonnull IAgriStat stat, @Nonnull String target, @Nonnull int value) throws CommandException {
        if (target.equals("all")) {
            for (CropStat cropStat : CropStat.values()) {
                MessageUtil.messagePlayer(player, "{0}: {1} -> {2}", cropStat.getId(), stat.get(cropStat), value);
                stat = stat.with(cropStat, value);
            }
            writeStat(stack, stat);
            return;
        }
        final CropStat cropStat = findStat(target)
                .orElseThrow(() -> new CommandException("Invalid target: \"{0}\"!", target));
        MessageUtil.messagePlayer(player, "{0}: {1} -> {2}", cropStat.getId(), stat.get(cropStat), value);
        writeStat(stack, stat.with(cropStat, value));
    }

    private void writeStat(@Nonnull ItemStack stack, @Nonnull IAgriStat stat) {
        final NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        stat.writeToNBT(tag);
        stack.setTagCompound(tag);
    }

    private void getStat(@Nonnull EntityPlayer player, @Nonnull ItemStack stack, @Nonnull IAgriStat stat, @Nonnull String target) throws CommandException {
        if (target.equals("id")) {
            MessageUtil.messagePlayer(player, "ID: {0}", stat.getId());
            return;
        }
        if (target.equals("all")) {
            MessageUtil.messagePlayer(player, "ID: {0}", stat.getId());
            for (CropStat cropStat : CropStat.values()) {
                MessageUtil.messagePlayer(player, "{0}: {1}", cropStat.getId(), stat.get(cropStat));
            }
            return;
        }
        final CropStat cropStat = findStat(target)
                .orElseThrow(() -> new CommandException("Invalid target: \"%s\"!", target));
        MessageUtil.messagePlayer(player, "{0}: {1}", cropStat.getId(), stat.get(cropStat));
    }

    @Override
    public boolean checkPermission(MinecraftServer ms, ICommandSender ics) {
        // Temporary for testing purposes (only enabled in creative mode atm, since quick hack).
        return TypeHelper.cast(ics.getCommandSenderEntity(), EntityPlayer.class)
                .filter(EntityPlayer::isCreative)
                .isPresent();
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer ms, ICommandSender ics, String[] arguments, BlockPos bp) {
        // The list of possible completions.
        final List<String> completions = new ArrayList<>();

        // Determine the possible completions, based off of the number of arguments.
        switch (arguments.length) {
            case 1:
                completions.add("set");
                completions.add("get");
                break;
            case 2:
                for (CropStat stat : CropStat.values()) {
                    completions.add(stat.getId());
                }
                completions.add("all");
                break;
            case 3:
                if (arguments[0].trim().toLowerCase().equals("set")) {
                    completions.add(String.valueOf(CropStat.MIN));
                    completions.add(String.valueOf(CropStat.MAX));
                }
                break;
        }

        // Return the list of possible completions.
        return completions;
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return ComparisonChain.start()
                .compare(this.getName(), o.getName())
                .result();
    }

}
