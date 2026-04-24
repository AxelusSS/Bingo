package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Mini Nether : Quand un joueur allume un portail du Nether, au lieu de le téléporter,
 * on génère une mini-salle de Nether dans l'Overworld avec les ressources nécessaires.
 */
public class MiniNetherScenario extends Scenario {

    private final Random random = new Random();

    public MiniNetherScenario() {
        super("Mini Nether", Material.NETHERRACK, "Crée des mini-nether dans l'overworld", false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getItem().getType() != Material.FLINT_AND_STEEL) return;
        if (event.getClickedBlock() == null) return;

        Block clicked = event.getClickedBlock();

        // Vérifier si le bloc cliqué fait partie d'un cadre de portail
        if (clicked.getType() != Material.OBSIDIAN) return;
        
        // Vérifier si c'est un portail standard (colonne d'obsidienne)
        if (!isPartOfPortalFrame(clicked)) return;

        event.setCancelled(true);
        
        Player player = event.getPlayer();
        player.sendMessage("§5§l[Mini Nether] §dUne structure du Nether apparaît !");
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 0.5f);

        // Générer la salle souterraine sous le portail
        generateMiniNether(clicked.getLocation());
    }

    private boolean isPartOfPortalFrame(Block block) {
        // Vérifier s'il y a de l'obsidienne adjacente (simple heuristique)
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
        return obsidianCount >= 3; // Au moins 3 blocs d'obsidienne = frame
    }

    private void generateMiniNether(Location portalLoc) {
        World world = portalLoc.getWorld();
        int baseX = portalLoc.getBlockX();
        int baseY = portalLoc.getBlockY() - 5; // 5 blocs sous le portail
        int baseZ = portalLoc.getBlockZ();

        // Dimensions de la salle : 11x7x11
        int halfWidth = 5;
        int height = 7;

        // Phase 1 : Creuser et remplir de netherrack
        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -halfWidth; z <= halfWidth; z++) {
                    Block block = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
                    
                    // Murs/Sol/Plafond
                    if (x == -halfWidth || x == halfWidth || z == -halfWidth || z == halfWidth || y == 0 || y == height - 1) {
                        block.setType(Material.NETHERRACK);
                    } else {
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        // Phase 2 : Sol de soul sand aléatoire
        for (int x = -halfWidth + 1; x < halfWidth; x++) {
            for (int z = -halfWidth + 1; z < halfWidth; z++) {
                if (random.nextInt(4) == 0) {
                    world.getBlockAt(baseX + x, baseY, baseZ + z).setType(Material.SOUL_SAND);
                }
            }
        }

        // Phase 3 : Placer les ressources clés

        // Nether Wart (sur du soul sand)
        world.getBlockAt(baseX - 3, baseY, baseZ - 3).setType(Material.SOUL_SAND);
        world.getBlockAt(baseX - 3, baseY + 1, baseZ - 3).setType(Material.NETHER_WART);
        world.getBlockAt(baseX - 2, baseY, baseZ - 3).setType(Material.SOUL_SAND);
        world.getBlockAt(baseX - 2, baseY + 1, baseZ - 3).setType(Material.NETHER_WART);

        // Spawner de Blaze au centre
        world.getBlockAt(baseX, baseY + 1, baseZ).setType(Material.SPAWNER);
        Block spawner = world.getBlockAt(baseX, baseY + 1, baseZ);
        if (spawner.getState() instanceof org.bukkit.block.CreatureSpawner cs) {
            cs.setSpawnedType(EntityType.BLAZE);
            cs.setDelay(20);
            cs.setMinSpawnDelay(200);
            cs.setMaxSpawnDelay(400);
            cs.setSpawnCount(1);
            cs.setMaxNearbyEntities(4);
            cs.setRequiredPlayerRange(16);
            cs.update();
        }

        // Nether Brick autour du spawner
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                world.getBlockAt(baseX + dx, baseY, baseZ + dz).setType(Material.NETHER_BRICKS);
            }
        }
        world.getBlockAt(baseX, baseY + 2, baseZ).setType(Material.NETHER_BRICKS);

        // Lave décorative
        world.getBlockAt(baseX + 3, baseY + 1, baseZ + 3).setType(Material.LAVA);
        world.getBlockAt(baseX - 3, baseY + 1, baseZ + 3).setType(Material.LAVA);

        // Glowstone au plafond
        for (int i = 0; i < 4; i++) {
            int gx = baseX + random.nextInt(7) - 3;
            int gz = baseZ + random.nextInt(7) - 3;
            world.getBlockAt(gx, baseY + height - 1, gz).setType(Material.GLOWSTONE);
        }

        // Phase 4 : Escalier d'accès (creuser un trou depuis le portail vers la salle)
        for (int y = baseY + height; y <= portalLoc.getBlockY() + 1; y++) {
            world.getBlockAt(baseX, y, baseZ).setType(Material.AIR);
            world.getBlockAt(baseX + 1, y, baseZ).setType(Material.AIR);
            world.getBlockAt(baseX, y, baseZ + 1).setType(Material.AIR);
            world.getBlockAt(baseX + 1, y, baseZ + 1).setType(Material.AIR);
        }
        // Échelle pour descendre
        for (int y = baseY + 1; y <= portalLoc.getBlockY() + 1; y++) {
            Block ladderBlock = world.getBlockAt(baseX - 1, y, baseZ);
            ladderBlock.setType(Material.NETHERRACK);
            Block ladder = world.getBlockAt(baseX, y, baseZ);
            ladder.setType(Material.LADDER);
        }
    }
}
