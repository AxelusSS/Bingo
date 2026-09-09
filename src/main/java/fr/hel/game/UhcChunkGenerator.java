package fr.hel.game;

import fr.hel.HelPlugin;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

/**
 * G\u00E9n\u00E9rateur de terrain UHC consolid\u00E9.
 * - Monde de type Vanilla mais avec biomes restreints.
 * - Pas de Nether souterrain (uniquement pierre classique).
 */
public class UhcChunkGenerator extends ChunkGenerator {

    private final List<Biome> allowedBiomes = List.of(
            Biome.FOREST,
            Biome.BIRCH_FOREST,
            Biome.DESERT,
            Biome.RIVER,
            Biome.PLAINS
    );

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        PerlinOctaveGenerator noise = new PerlinOctaveGenerator(new Random(worldInfo.getSeed()), 8);
        noise.setScale(0.01);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;

                double noiseVal = noise.noise(realX, realZ, 0.5, 0.5) * 12;
                int height = (int) (70 + noiseVal);

                // On remplit tout en pierre/terre/herbe jusqu'en bas (Bedrock \u00E0 -64)
                for (int y = chunkData.getMinHeight(); y < height; y++) {
                    if (y <= chunkData.getMinHeight() + 1) {
                        chunkData.setBlock(x, y, z, Material.BEDROCK);
                    } else if (y < height - 1) {
                        chunkData.setBlock(x, y, z, Material.STONE);
                    } else {
                        chunkData.setBlock(x, y, z, Material.GRASS_BLOCK);
                    }
                }
            }
        }
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return new BiomeProvider() {
            @Override
            public @NotNull Biome getBiome(@NotNull WorldInfo info, int x, int y, int z) {
                int blockX = x << 2;
                int blockZ = z << 2;
                double dist = Math.sqrt(blockX * blockX + blockZ * blockZ);

                // 1. Centre (Rayon 300) : Pas de rivi\u00E8re
                if (dist < 300) {
                    HelGame game = HelPlugin.getInstance().getHelGame();
                    if (game != null && game.getGenerationType() == HelGame.GenerationType.V_1_8_STANDARD) {
                        return Biome.FOREST;
                    }
                    return Biome.PLAINS;
                }

                // 2. Rivi\u00E8res
                double riverNoise = noise(blockX * 0.015, blockZ * 0.015, info.getSeed() + 1);
                if (Math.abs(riverNoise) < 0.015) {
                    return Biome.RIVER;
                }

                // 3. Biomes (Rayon 5000)
                if (dist < 5000) {
                    List<Biome> landBiomes = List.of(Biome.FOREST, Biome.BIRCH_FOREST, Biome.DESERT, Biome.PLAINS);
                    int index = (int) (Math.abs(noise(blockX * 0.001, blockZ * 0.001, info.getSeed()) * 100) % landBiomes.size());
                    return landBiomes.get(index);
                }

                return Biome.PLAINS;
            }

            @Override
            public @NotNull List<Biome> getBiomes(@NotNull WorldInfo info) {
                return allowedBiomes;
            }
        };
    }

    private double noise(double x, double y, long seed) {
        int n = (int)x + (int)y * 57 + (int)seed;
        n = (n << 13) ^ n;
        return (1.0 - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
    }

    @Override
    public boolean shouldGenerateCaves() { return true; } // Retour des grottes car monde "Vanilla"
    @Override
    public boolean shouldGenerateDecorations() { return true; }
    @Override
    public boolean shouldGenerateMobs() { return true; }
    @Override
    public boolean shouldGenerateStructures() { return true; }
}
