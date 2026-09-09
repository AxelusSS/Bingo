package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Random;

/**
 * Mini Nether : Quand un joueur allume un portail du Nether, au lieu de le t\u00E9l\u00E9porter,
 * on g\u00E9n\u00E8re une box 16x16 de Nether AUTOUR du portail avec un spawner \u00E0 Blaze.
 */
public class MiniNetherScenario extends Scenario {

    private final Random random = new Random();

    public MiniNetherScenario() {
        super("Mini Nether", Material.NETHERRACK, "Cr\u00E9e des mini-nether dans l'overworld", false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.FLINT_AND_STEEL) return;
        if (event.getClickedBlock() == null) return;

        Block clicked = event.getClickedBlock();
        if (clicked.getType() != Material.OBSIDIAN) return;
        if (!isPartOfPortalFrame(clicked)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        player.sendMessage("\u00A75\u00A7l[Mini Nether] \u00A7dUne structure du Nether appara\u00EEt autour du portail !");
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 0.5f);

        generateMiniNether(clicked.getLocation());
    }

    private boolean isPartOfPortalFrame(Block block) {
        int obsidianCount = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (block.getRelative(dx, dy, dz).getType() == Material.OBSIDIAN) {
                        obsidianCount++;
                    }
                }
            }
        }
        return obsidianCount >= 3;
    }

    private void generateMiniNether(Location portalLoc) {
        World world = portalLoc.getWorld();
        int centerX = portalLoc.getBlockX();
        int baseY = portalLoc.getBlockY() - 1; // Sol au niveau du bas du portail
        int centerZ = portalLoc.getBlockZ();

        int halfWidth = 8; // 16x16
        int height = 10;

        // Phase 1 : Construire la box (murs, sol, plafond en netherrack)
        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -halfWidth; z <= halfWidth; z++) {
                    Block block = world.getBlockAt(centerX + x, baseY + y, centerZ + z);

                    boolean isWall = (x == -halfWidth || x == halfWidth || z == -halfWidth || z == halfWidth);
                    boolean isFloor = (y == 0);
                    boolean isCeiling = (y == height - 1);

                    if (isWall || isFloor || isCeiling) {
                        // Vari\u00E9t\u00E9 dans les murs
                        if (random.nextInt(6) == 0) {
                            block.setType(Material.NETHER_BRICKS);
                        } else if (random.nextInt(8) == 0) {
                            block.setType(Material.MAGMA_BLOCK);
                        } else {
                            block.setType(Material.NETHERRACK);
                        }
                    } else {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        // Phase 2 : Sol vari\u00E9 (soul sand, netherrack, nether bricks)
        for (int x = -halfWidth + 1; x < halfWidth; x++) {
            for (int z = -halfWidth + 1; z < halfWidth; z++) {
                int r = random.nextInt(10);
                if (r < 2) {
                    world.getBlockAt(centerX + x, baseY, centerZ + z).setType(Material.SOUL_SAND);
                } else if (r < 3) {
                    world.getBlockAt(centerX + x, baseY, centerZ + z).setType(Material.SOUL_SOIL);
                } else if (r < 4) {
                    world.getBlockAt(centerX + x, baseY, centerZ + z).setType(Material.NETHER_BRICKS);
                }
            }
        }

        // Phase 3 : Verrues du Nether (coin nord-ouest)
        for (int x = -halfWidth + 2; x <= -halfWidth + 4; x++) {
            for (int z = -halfWidth + 2; z <= -halfWidth + 4; z++) {
                world.getBlockAt(centerX + x, baseY, centerZ + z).setType(Material.SOUL_SAND);
                world.getBlockAt(centerX + x, baseY + 1, centerZ + z).setType(Material.NETHER_WART);
            }
        }

        // Phase 4 : Spawner \u00E0 Blaze (coin sud-est, sur un pi\u00E9destal de nether bricks)
        int spawnerX = centerX + halfWidth - 3;
        int spawnerZ = centerZ + halfWidth - 3;

        // Pi\u00E9destal
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.getBlockAt(spawnerX + dx, baseY, spawnerZ + dz).setType(Material.NETHER_BRICKS);
                world.getBlockAt(spawnerX + dx, baseY + 1, spawnerZ + dz).setType(Material.NETHER_BRICK_FENCE);
            }
        }
        world.getBlockAt(spawnerX, baseY + 1, spawnerZ).setType(Material.NETHER_BRICKS);

        // Spawner
        Block spawnerBlock = world.getBlockAt(spawnerX, baseY + 2, spawnerZ);
        spawnerBlock.setType(Material.SPAWNER);
        if (spawnerBlock.getState() instanceof org.bukkit.block.CreatureSpawner cs) {
            cs.setSpawnedType(EntityType.BLAZE);
            cs.setDelay(20);
            cs.setMinSpawnDelay(200);
            cs.setMaxSpawnDelay(600);
            cs.setSpawnCount(1);
            cs.setMaxNearbyEntities(6);
            cs.setRequiredPlayerRange(16);
            cs.update();
        }

        // Phase 5 : Lave d\u00E9corative (quelques bassins)
        int[][] lavaPools = {{-5, -5}, {5, 5}, {-5, 5}};
        for (int[] pool : lavaPools) {
            world.getBlockAt(centerX + pool[0], baseY, centerZ + pool[1]).setType(Material.NETHERRACK);
            world.getBlockAt(centerX + pool[0], baseY + 1, centerZ + pool[1]).setType(Material.LAVA);
        }

        // Phase 6 : Glowstone au plafond
        for (int i = 0; i < 8; i++) {
            int gx = centerX + random.nextInt(2 * halfWidth - 2) - halfWidth + 1;
            int gz = centerZ + random.nextInt(2 * halfWidth - 2) - halfWidth + 1;
            world.getBlockAt(gx, baseY + height - 1, gz).setType(Material.GLOWSTONE);
        }

        // Phase 7 : Quelques champignons et feu
        for (int i = 0; i < 5; i++) {
            int fx = centerX + random.nextInt(2 * halfWidth - 4) - halfWidth + 2;
            int fz = centerZ + random.nextInt(2 * halfWidth - 4) - halfWidth + 2;
            Block floor = world.getBlockAt(fx, baseY, fz);
            if (floor.getType() == Material.NETHERRACK) {
                world.getBlockAt(fx, baseY + 1, fz).setType(Material.FIRE);
            }
        }

        // Phase 8 : Quelques tiges de champignon (warped/crimson stems) pour l'ambiance
        for (int i = 0; i < 3; i++) {
            int sx = centerX + random.nextInt(2 * halfWidth - 4) - halfWidth + 2;
            int sz = centerZ + random.nextInt(2 * halfWidth - 4) - halfWidth + 2;
            Block floor = world.getBlockAt(sx, baseY + 1, sz);
            if (floor.getType() == Material.AIR) {
                floor.setType(random.nextBoolean() ? Material.CRIMSON_FUNGUS : Material.WARPED_FUNGUS);
            }
        }
    }
}
