package dev.omega24.agricrops.world;

import dev.omega24.agricrops.content.PocContent;
import dev.omega24.agricrops.core.environment.Environment;
import dev.omega24.agricrops.core.environment.Factor;
import java.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

/** Constant-sized observation, no cuboid scans and no invented pollution/season API. */
public final class WorldEnvironment {
    public interface Provider { Observation sample(World world, BlockPos position); }
    public static final class Observation {
        public final double value; public final String sourceKey;
        public Observation(double value, String sourceKey) {
            if (!Double.isFinite(value) || sourceKey == null || sourceKey.isEmpty() || sourceKey.length() > 256) throw new IllegalArgumentException("Invalid observation");
            this.value = value; this.sourceKey = sourceKey;
        }
    }
    private static final Map<String, Provider> PROVIDERS = new LinkedHashMap<>();
    private static final Set<String> WARNED = new HashSet<>();
    private static final Set<String> BUILTINS = new HashSet<>(Arrays.asList("agricrops:light", "agricrops:temperature", "agricrops:humidity", "agricrops:air_quality", "agricrops:height", "agricrops:dimension", "agricrops:rain", "agricrops:sky", "agricrops:moisture", "agricrops:nutrition", "agricrops:aeration"));
    private WorldEnvironment() {}
    /** Java addon registration during content collection; null observations mean genuinely unavailable. */
    public static void registerProvider(Factor definition, Provider provider, boolean replace) {
        if (definition.constant != null || provider == null) throw new IllegalArgumentException("A live provider needs a nonconstant factor");
        PocContent.factor(definition, replace); PROVIDERS.put(definition.id, provider);
    }
    public static boolean available(Factor factor) { return factor.constant != null || BUILTINS.contains(factor.id) || PROVIDERS.containsKey(factor.id); }
    public static Environment sample(World world, BlockPos pos) {
        if (!world.isBlockLoaded(pos)) throw new IllegalArgumentException("Unloaded crop environment");
        Biome biome = world.getBiome(pos); Map<String, Double> values = new LinkedHashMap<>(); Map<String, String> sources = new LinkedHashMap<>();
        put(values, sources, "light", world.getLightFromNeighbors(pos.up()), "agricrops.source.combined_light");
        put(values, sources, "temperature", biome.getTemperature(pos), "agricrops.source.position_temperature");
        put(values, sources, "humidity", Math.max(0, Math.min(1, biome.getRainfall() + (world.isRainingAt(pos) ? 0.1 : 0))), "agricrops.source.humidity_proxy");
        put(values, sources, "air_quality", 1, "agricrops.source.default_air_profile");
        put(values, sources, "height", pos.getY(), "agricrops.source.block_height");
        put(values, sources, "dimension", world.provider.getDimension(), "agricrops.source.dimension");
        put(values, sources, "rain", world.isRainingAt(pos) ? 1 : 0, "agricrops.source.actual_rain");
        put(values, sources, "sky", world.canSeeSky(pos) ? 1 : 0, "agricrops.source.sky_exposure");
        // Root observations are replaced with actual accessible cells by the pure phenotype solver.
        put(values, sources, "moisture", 0, "agricrops.source.root_cells"); put(values, sources, "nutrition", 0, "agricrops.source.root_cells"); put(values, sources, "aeration", 1, "agricrops.source.root_cells");
        for (Factor factor : PocContent.factors().values()) if (factor.constant != null) { values.put(factor.id, factor.constant); sources.put(factor.id, "agricrops.source.constant_profile"); }
        PROVIDERS.forEach((id, provider) -> {
            values.remove(id); sources.remove(id);
            try {
                Observation observation = provider.sample(world, pos);
                if (observation != null) { PocContent.factors().get(id).validate(observation.value); values.put(id, observation.value); sources.put(id, observation.sourceKey); }
            } catch (RuntimeException exception) {
                if (WARNED.add(id)) org.apache.logging.log4j.LogManager.getLogger("agricrops").warn("Environment provider {} unavailable; required crops wait rather than inventing a value", id, exception);
            }
        });
        PocContent.dimension(world.provider.getDimension()).forEach((id, value) -> { values.put(id, value); sources.put(id, "agricrops.source.dimension_profile"); });
        PocContent.biome(biome.getRegistryName().toString()).forEach((id, value) -> { values.put(id, value); sources.put(id, "agricrops.source.biome_profile"); });
        values.forEach((id, value) -> PocContent.factors().get(id).validate(value));
        return new Environment(values, sources);
    }
    private static void put(Map<String, Double> values, Map<String, String> sources, String id, double value, String source) { values.put("agricrops:" + id, value); sources.put("agricrops:" + id, source); }
}
