/*
 */
package dev.omega24.agricrops.compat.computer;

import dev.omega24.agricrops.api.v1.plugin.AgriPlugin;
import dev.omega24.agricrops.api.v1.plugin.IAgriPlugin;
import dev.omega24.agricrops.compat.computer.methods.MethodAnalyze;
import dev.omega24.agricrops.compat.computer.methods.MethodGetBaseBlock;
import dev.omega24.agricrops.compat.computer.methods.MethodGetBiomeElevationModifier;
import dev.omega24.agricrops.compat.computer.methods.MethodGetBrightness;
import dev.omega24.agricrops.compat.computer.methods.MethodGetBrightnessRange;
import dev.omega24.agricrops.compat.computer.methods.MethodGetCurrentSoil;
import dev.omega24.agricrops.compat.computer.methods.MethodGetGrowthProgress;
import dev.omega24.agricrops.compat.computer.methods.MethodGetGrowthStage;
import dev.omega24.agricrops.compat.computer.methods.MethodGetIdealConditions;
import dev.omega24.agricrops.compat.computer.methods.MethodGetIdealSoilStack;
import dev.omega24.agricrops.compat.computer.methods.MethodGetNeededSoil;
import dev.omega24.agricrops.compat.computer.methods.MethodGetPlant;
import dev.omega24.agricrops.compat.computer.methods.MethodGetPreferredBiomes;
import dev.omega24.agricrops.compat.computer.methods.MethodGetPreferredSoil;
import dev.omega24.agricrops.compat.computer.methods.MethodGetSoilData;
import dev.omega24.agricrops.compat.computer.methods.MethodGetSpecimen;
import dev.omega24.agricrops.compat.computer.methods.MethodGetStats;
import dev.omega24.agricrops.compat.computer.methods.MethodHasJournal;
import dev.omega24.agricrops.compat.computer.methods.MethodHasPlant;
import dev.omega24.agricrops.compat.computer.methods.MethodIsAnalyzed;
import dev.omega24.agricrops.compat.computer.methods.MethodIsCrossCrop;
import dev.omega24.agricrops.compat.computer.methods.MethodIsFertile;
import dev.omega24.agricrops.compat.computer.methods.MethodIsMature;
import dev.omega24.agricrops.compat.computer.methods.MethodNeedsBaseBlock;
import dev.omega24.agricrops.api.v1.misc.IAgriPeripheralMethod;
import dev.omega24.agricrops.api.v1.misc.IAgriRegistry;
import javax.annotation.Nonnull;

/**
 *
 *
 */
@AgriPlugin
public class ComputerPlugin implements IAgriPlugin {

    @Nonnull
    private static final IAgriPeripheralMethod[] METHODS = new IAgriPeripheralMethod[]{
        new MethodAnalyze(),
        new MethodGetBaseBlock(),
        new MethodGetBrightness(),
        new MethodGetBrightnessRange(),
        new MethodGetCurrentSoil(),
        new MethodGetGrowthStage(),
        new MethodGetNeededSoil(),
        new MethodGetPlant(),
        new MethodGetSpecimen(),
        new MethodGetStats(),
        new MethodHasJournal(),
        new MethodHasPlant(),
        new MethodIsAnalyzed(),
        new MethodIsCrossCrop(),
        new MethodIsFertile(),
        new MethodIsMature(),
        new MethodNeedsBaseBlock(),
        new MethodGetSoilData(),
        new MethodGetBiomeElevationModifier(),
        new MethodGetGrowthProgress(),
        new MethodGetPreferredBiomes(),
        new MethodGetPreferredSoil(),
        new MethodGetIdealSoilStack(),
        new MethodGetIdealConditions()
    };

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getId() {
        return "computer";
    }

    @Override
    public String getName() {
        return "Computer Integration";
    }

    @Override
    public void registerPeripheralMethods(IAgriRegistry<IAgriPeripheralMethod> methodRegistry) {
        // Register all the default methods.
        for (IAgriPeripheralMethod m : METHODS) {
            methodRegistry.add(m);
        }
    }

}
