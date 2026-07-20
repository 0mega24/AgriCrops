/*
 */
package dev.omega24.agricrops.farming.growthrequirement;

import com.agricraft.agricore.util.MathHelper;
import dev.omega24.agricrops.api.v1.requirement.ICondition;
import dev.omega24.agricrops.api.v1.requirement.IGrowthReqBuilder;
import dev.omega24.agricrops.api.v1.requirement.IGrowthRequirement;
import dev.omega24.agricrops.api.v1.soil.ISoilComposition;
import java.util.ArrayList;
import java.util.List;

public class GrowthReqBuilder implements IGrowthReqBuilder {

    private final List<ISoilComposition> soilCompositions;
    private final List<ICondition> conditions;
    private int minLight = 0;
    private int maxLight = 16;

    public GrowthReqBuilder() {
        this.soilCompositions = new ArrayList<>();
        this.conditions = new ArrayList<>();
    }

    @Override
    public GrowthReqBuilder addSoilComposition(ISoilComposition composition) {
        this.soilCompositions.add(composition);
        return this;
    }

    @Override
    public IGrowthReqBuilder addCondition(ICondition condition) {
        this.conditions.add(condition);
        return this;
    }

    @Override
    public IGrowthReqBuilder setMinLight(int minLight) {
        this.minLight = MathHelper.inRange(minLight, 0, 16);
        return this;
    }

    @Override
    public IGrowthReqBuilder setMaxLight(int maxLight) {
        this.maxLight = MathHelper.inRange(maxLight, 0, 16);
        return this;
    }

    @Override
    public IGrowthRequirement build() {
        return new GrowthRequirement(soilCompositions, conditions, minLight, maxLight);
    }

}
