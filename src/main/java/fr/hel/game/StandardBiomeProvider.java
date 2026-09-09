package fr.hel.game;

import fr.hel.HelPlugin;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fournisseur de biomes restreint pour les modes UHC.
 */
public class StandardBiomeProvider extends BiomeProvider {

    private final List<Biome> biomesV18 = List.of(
            Biome.FOREST,
            Biome.BIRCH_FOREST,
            Biome.DESERT,
            Biome.RIVER,
            Biome.PLAINS
    );

    private final List<Biome> biomesV121 = List.of(
            Biome.PLAINS, Biome.FOREST, Biome.BIRCH_FOREST, Biome.DESERT, 
            Biome.SAVANNA, Biome.JUNGLE, Biome.TAIGA, Biome.SNOWY_PLAINS,
            Biome.CHERRY_GROVE, Biome.FLOWER_FOREST
    );

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        // Forcer PLAINS au centre (rayon de 100 blocs autour du 0,0)
        if (Math.abs(x) < 100 && Math.abs(z) < 100) {
            return Biome.PLAINS;
        }

        List<Biome> pool = getAllowedBiomes();
        if (pool == null || pool.isEmpty()) return Biome.PLAINS;

        long seed = worldInfo.getSeed();
        double scale = 0.002; 
        
        double noise = noise(x * scale, z * scale, seed);
        int index = (int) (Math.abs(noise * 100) % pool.size());
        
        return pool.get(index);
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return getAllowedBiomes();
    }

    private List<Biome> getAllowedBiomes() {
        if (HelPlugin.getInstance() == null) return biomesV18;
        HelGame game = HelPlugin.getInstance().getHelGame();
        if (game == null) return biomesV18;

        if (game.getGenerationType() == HelGame.GenerationType.V_1_21_11) {
            List<Biome> base = new ArrayList<>(biomesV121);
            if (!game.getDisabledBiomes().contains("mountains")) {
                base.addAll(List.of(Biome.JAGGED_PEAKS, Biome.STONY_PEAKS, Biome.FROZEN_PEAKS));
            }
            if (!game.getDisabledBiomes().contains("oceans")) {
                base.addAll(List.of(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN));
            }
            return base;
        } else {
            return biomesV18;
        }
    }

    private double noise(double x, double y, long seed) {
        int n = (int)x + (int)y * 57 + (int)seed;
        n = (n << 13) ^ n;
        return (1.0 - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
    }
}
