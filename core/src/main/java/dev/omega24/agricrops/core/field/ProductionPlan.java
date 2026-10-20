package dev.omega24.agricrops.core.field;

import dev.omega24.agricrops.core.content.CropDefinition;
import dev.omega24.agricrops.core.genetics.ProductMix;
import dev.omega24.agricrops.core.genetics.StandardStat;
import java.util.*;

/** A once-resolved harvest and price. Persisted so reload/config changes cannot reroll it. */
public final class ProductionPlan {
    public final Map<String, Integer> products, nextCredit;
    public final int water, nutrients;
    public ProductionPlan(Map<String, Integer> products, Map<String, Integer> nextCredit, int water, int nutrients) {
        if (products.isEmpty() || products.size() > 4 || nextCredit.isEmpty() || nextCredit.size() > 4 || water < 1 || nutrients < 1) throw new IllegalArgumentException("Invalid production plan");
        int units = 0;
        for (Map.Entry<String, Integer> entry : products.entrySet()) { CropDefinition.identifier(entry.getKey()); if (entry.getValue() < 1 || entry.getValue() > 8) throw new IllegalArgumentException("Invalid output count"); units += entry.getValue(); }
        if (units > 8 || !nextCredit.keySet().containsAll(products.keySet()) || nextCredit.values().stream().mapToInt(Integer::intValue).sum() != 0) throw new IllegalArgumentException("Invalid allocation plan");
        nextCredit.forEach((id, credit) -> { CropDefinition.identifier(id); if (Math.abs((long) credit) > 40000) throw new IllegalArgumentException("Invalid production credit"); });
        this.products = Collections.unmodifiableMap(new TreeMap<>(products)); this.nextCredit = Collections.unmodifiableMap(new TreeMap<>(nextCredit)); this.water = water; this.nutrients = nutrients;
    }
    public static ProductionPlan resolve(CropDefinition body, CropState state, Map<String, CropDefinition> crops) {
        return resolve(body, state, crops, 1, 1);
    }
    public static ProductionPlan resolve(CropDefinition body, CropState state, Map<String, CropDefinition> crops, double waterMultiplier, double nutrientMultiplier) {
        int units = body.rules().harvestUnits + state.genome().get(StandardStat.YIELD) / 16;
        ProductMix.Allocation allocation = state.specimen().products.allocate(units, state.credit());
        long w = 0, n = 0;
        for (Map.Entry<String, Integer> output : allocation.output.entrySet()) {
            CropDefinition source = crops.get(output.getKey());
            if (source == null) throw new IllegalArgumentException("Missing product source " + output.getKey());
            w += source.timeline().totalTicks() * source.waterCost() * output.getValue();
            n += source.timeline().totalTicks() * source.nutrientCost() * output.getValue();
        }
        long water = (long) Math.ceil(w * waterMultiplier), nutrients = (long) Math.ceil(n * nutrientMultiplier);
        return new ProductionPlan(allocation.output, allocation.nextCredit, Math.toIntExact(water), Math.toIntExact(nutrients));
    }
}
