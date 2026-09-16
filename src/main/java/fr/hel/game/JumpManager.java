package fr.hel.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class JumpManager {

    private static class PlayerJumpData {
        public List<Block> activeBlocks = new ArrayList<>();
        public int currentIndex = 0;
        public Material glassColor;
    }

    private final Map<UUID, PlayerJumpData> jumpMap = new HashMap<>();
    private final Material[] glassColors = {
            Material.RED_STAINED_GLASS, Material.BLUE_STAINED_GLASS, Material.GREEN_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS, Material.PINK_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS
    };

    public void startJump(Player p) {
        cleanupPlayer(p);
        PlayerJumpData data = new PlayerJumpData();
        data.glassColor = glassColors[new Random().nextInt(glassColors.length)];
        jumpMap.put(p.getUniqueId(), data);

        Location start = getInitialLocation(p.getWorld());
        generateNextBlock(data, start); // Block 0
        generateNextBlock(data, start); // Block 1
        generateNextBlock(data, start); // Block 2

        Location tp = data.activeBlocks.get(0).getLocation().clone().add(0.5, 1, 0.5);
        tp.setYaw(p.getLocation().getYaw());
        tp.setPitch(p.getLocation().getPitch());
        p.teleport(tp);
    }

    private Location getInitialLocation(org.bukkit.World w) {
        Random r = new Random();
        int x = r.nextInt(16) - 8;
        int z = r.nextInt(16) - 8;
        return new Location(w, x, 255, z); 
    }

    private void generateNextBlock(PlayerJumpData data, Location baseLoc) {
        Location lastLoc = data.activeBlocks.isEmpty() ? baseLoc : data.activeBlocks.get(data.activeBlocks.size() - 1).getLocation();
        Random r = new Random();
        
        int dx = 0;
        int dz = 0;
        int dy = 0;

        if (!data.activeBlocks.isEmpty()) {
            boolean valid = false;
            int attempts = 0;
            while (!valid && attempts < 100) {
                attempts++;
                dx = (r.nextInt(7) - 3); 
                dz = (r.nextInt(7) - 3);
                dy = r.nextInt(3) - 1;   

                double dist = Math.sqrt(dx*dx + dz*dz);
                if (dist < 2.0 || dist > 3.8) continue;

                int nx = lastLoc.getBlockX() + dx;
                int nz = lastLoc.getBlockZ() + dz;
                int ny = lastLoc.getBlockY() + dy;

                if (nx >= -14 && nx <= 14 && nz >= -14 && nz <= 14 && ny >= 253 && ny <= 310) {
                    // Check if it overlaps with any existing active block
                    boolean overlaps = false;
                    for (Block active : data.activeBlocks) {
                        if (active.getX() == nx && active.getZ() == nz) {
                            overlaps = true;
                            break;
                        }
                    }
                    if (!overlaps) {
                        valid = true;
                        lastLoc = new Location(lastLoc.getWorld(), nx, ny, nz);
                    }
                }
            }
        }

        Block b = lastLoc.getBlock();
        b.setType(data.glassColor);
        data.activeBlocks.add(b);
    }

    public void handleMove(Player p, Location to) {
        PlayerJumpData data = jumpMap.get(p.getUniqueId());
        if (data == null) return;

        Block currentStanding = to.clone().subtract(0, 0.1, 0).getBlock();
        
        if (to.getY() < data.activeBlocks.get(data.currentIndex).getY() - 1.5) {
            cleanupPlayer(p);
            p.sendMessage("\u00A7cOops! Vous \u00EAtes tomb\u00E9 du jump.");
            return;
        }

        if (data.currentIndex + 1 < data.activeBlocks.size()) {
            Block targetBlock = data.activeBlocks.get(data.currentIndex + 1);
            if (currentStanding.equals(targetBlock)) {
                data.currentIndex++;
                Block old = data.activeBlocks.get(data.currentIndex - 1);
                old.setType(Material.AIR);
                generateNextBlock(data, null);
            }
        }
    }

    public void cleanupPlayer(Player p) {
        PlayerJumpData data = jumpMap.remove(p.getUniqueId());
        if (data != null) {
            for (Block b : data.activeBlocks) {
                b.setType(Material.AIR);
            }
        }
    }

    public void cleanupAll() {
        for (UUID uuid : new HashSet<>(jumpMap.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) cleanupPlayer(p);
            else {
                PlayerJumpData data = jumpMap.remove(uuid);
                for (Block b : data.activeBlocks) {
                    b.setType(Material.AIR);
                }
            }
        }
    }
}
