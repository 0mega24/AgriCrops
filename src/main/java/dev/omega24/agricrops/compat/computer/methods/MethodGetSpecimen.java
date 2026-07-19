package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.tiles.TileEntityPeripheral;
import dev.omega24.agricrops.container.ContainerSeedAnalyzer;
import net.minecraft.item.ItemStack;

public class MethodGetSpecimen extends MethodBasePeripheral {

    public MethodGetSpecimen() {
        super("getSpecimen");
    }

    @Override
    protected Object[] onMethodCalled(TileEntityPeripheral peripheral) throws InvocationException {
        ItemStack stack = peripheral.getStackInSlot(ContainerSeedAnalyzer.SEED_SLOT_ID);
        return new Object[]{stack == null ? null : stack.getDisplayName()};
    }
}
