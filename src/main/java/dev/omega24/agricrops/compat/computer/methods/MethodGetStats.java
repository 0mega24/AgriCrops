package dev.omega24.agricrops.compat.computer.methods;

import dev.omega24.agricrops.api.v1.AgriApi;
import dev.omega24.agricrops.api.v1.stat.CropStat;
import dev.omega24.agricrops.api.v1.stat.IAgriStat;
import dev.omega24.agricrops.tiles.TileEntityPeripheral;
import dev.omega24.agricrops.tiles.TileEntityCrop;
import dev.omega24.agricrops.utility.StackHelper;
import java.util.ArrayList;
import java.util.Optional;

public class MethodGetStats extends MethodBase {

    public MethodGetStats() {
        super("getSpecimenStats", false, true, true);
    }

    @Override
    protected Object[] onMethodCalled(TileEntityCrop crop) throws InvocationException {
        return Optional.ofNullable(crop.getSeed())
                .map(s -> s.getStat())
                .filter(IAgriStat::isAnalyzed)
                .map(MethodGetStats::toArray)
                .orElse(null);
    }

    @Override
    protected Object[] onMethodCalled(TileEntityPeripheral peripheral) throws InvocationException {
        IAgriStat stats = AgriApi.getStatRegistry().valueOf(StackHelper.getTag(peripheral.getSpecimen())).orElse(null);
        return stats == null ? null : toArray(stats);
    }

    private static Object[] toArray(IAgriStat stat) {
        final CropStat[] all = CropStat.values();
        final Object[] values = new Object[all.length];
        for (int i = 0; i < all.length; i++) {
            values[i] = stat.get(all[i]);
        }
        return values;
    }

    @Override
    protected ArrayList<MethodParameter> getParameters() {
        ArrayList<MethodParameter> pars = new ArrayList<>();
        pars.add(MethodParameter.DIRECTION_OPTIONAL);
        return pars;
    }

}
