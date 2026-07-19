/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.omega24.agricrops.init;

import com.agricrops.agricore.config.AgriConfigCategory;
import com.agricrops.agricore.config.AgriConfigurable;
import dev.omega24.agricrops.commands.CommandNbt;
import dev.omega24.agricrops.commands.CommandStat;
import javax.annotation.Nonnull;

/**
 *
 * @author Ryan
 */
public class AgriCommands {

    @AgriConfigurable(
            category = AgriConfigCategory.CORE,
            key = "Enable Commands",
            comment = "Set to false to disable the AgriCraft commands."
    )
    public static boolean enableCommands = true;
    
    private static final AgriCommands INSTANCE = new AgriCommands();

    public static AgriCommands getInstance() {
        return INSTANCE;
    }    
    
    private AgriCommands() {
        this.STAT = new CommandStat();
        this.NBT = new CommandNbt();
    }
    
    @Nonnull
    public final CommandStat STAT;
    
    @Nonnull
    public final CommandNbt NBT;
    
}
