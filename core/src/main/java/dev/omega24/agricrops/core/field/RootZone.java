package dev.omega24.agricrops.core.field;

import java.util.*;

/** Values are returned to their original source positions by the adapter. No aggregate pool. */
public final class RootZone {
    public final List<SoilCell> cells;
    public final List<SoilProfile> profiles;
    public RootZone(List<SoilCell> cells, List<SoilProfile> profiles) {
        if (cells.isEmpty() || cells.size() > 3 || cells.size() != profiles.size()) throw new IllegalArgumentException("Root zone must have 1..3 cells");
        for (int i = 0; i < cells.size(); i++) if (!cells.get(i).block().equals(profiles.get(i).block)) throw new IllegalArgumentException("Soil/profile mismatch");
        this.cells = Collections.unmodifiableList(new ArrayList<>(cells)); this.profiles = Collections.unmodifiableList(new ArrayList<>(profiles));
    }
    public static RootZone single(SoilCell cell) { return new RootZone(Collections.singletonList(cell), Collections.singletonList(new SoilProfile(cell.block(), 10000, 10000, 0, 0, 0, 10000, 0.9))); }
    public int availableWater(int reach) { int result = 0; for (int i = 0; i < Math.min(reach, cells.size()); i++) result += Math.min(cells.get(i).water(), profiles.get(i).uptake); return result; }
    public int availableNutrients(int reach) { int result = 0; for (int i = 0; i < Math.min(reach, cells.size()); i++) result += Math.min(cells.get(i).nutrients(), profiles.get(i).uptake); return result; }
    public double moisture(int reach) { double stock = 0, capacity = 0; for (int i = 0; i < Math.min(reach, cells.size()); i++) { stock += cells.get(i).water(); capacity += profiles.get(i).waterCapacity; } return stock / Math.max(1, capacity); }
    public double nutrition(int reach) { double stock = 0, capacity = 0; for (int i = 0; i < Math.min(reach, cells.size()); i++) { stock += cells.get(i).nutrients(); capacity += profiles.get(i).nutrientCapacity; } return stock / Math.max(1, capacity); }
    public double aeration(int reach) { double result = 0; int count = Math.min(reach, cells.size()); for (int i = 0; i < count; i++) result += profiles.get(i).aeration(cells.get(i)); return result / count; }
    public RootZone consume(int water, int nutrients, int reach) {
        if (water < 0 || nutrients < 0 || water > availableWater(reach) || nutrients > availableNutrients(reach)) throw new IllegalArgumentException("Root overdraw");
        List<SoilCell> next = new ArrayList<>(cells);
        for (int i = 0; i < Math.min(reach, cells.size()); i++) {
            int w = Math.min(water, Math.min(cells.get(i).water(), profiles.get(i).uptake));
            int n = Math.min(nutrients, Math.min(cells.get(i).nutrients(), profiles.get(i).uptake));
            next.set(i, cells.get(i).consume(w, n)); water -= w; nutrients -= n;
        }
        return new RootZone(next, profiles);
    }
    /** Downward transfer conserves stocks; excess below the last cell is an explicit runoff loss. */
    public RootZone drain() {
        List<SoilCell> next = new ArrayList<>(cells);
        for (int i = 0; i < next.size(); i++) {
            SoilCell source = next.get(i); SoilProfile profile = profiles.get(i);
            int amount = Math.min(profile.drainage, Math.max(0, source.water() - (int) (profile.waterCapacity * profile.saturationStart)));
            if (i + 1 < next.size()) {
                amount = Math.min(amount, Math.max(0, profiles.get(i + 1).waterCapacity - next.get(i + 1).water()));
                next.set(i + 1, next.get(i + 1).add(amount, 0));
            }
            next.set(i, source.consume(amount, 0));
        }
        return new RootZone(next, profiles);
    }
}
