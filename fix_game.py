import sys
import re

path = 'src/main/java/fr/hel/game/HelGame.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Add JumpManager and Hologram
imports = 'import org.bukkit.entity.ArmorStand;\nimport org.bukkit.entity.EntityType;\n'
c = c.replace('import org.bukkit.entity.Player;', 'import org.bukkit.entity.Player;\n' + imports)

fields = '''    private EndMode endMode = EndMode.ALL_TEAMS;
    
    private JumpManager jumpManager = new JumpManager();
    private ArmorStand lobbyHologram;
    
    public JumpManager getJumpManager() { return jumpManager; }'''

c = c.replace('    private EndMode endMode = EndMode.ALL_TEAMS;', fields)

# Revert wall and parkour
old_platform = '''        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                world.getBlockAt(x, y, z).setType(Material.GLASS);
                if (x == -16 || x == 16 || z == -16 || z == 16) {
                    // Monter les murs tr\u00E8s haut pour emp\u00EAcher de sortir du jump
                    for (int wallY = 1; wallY <= 30; wallY++) {
                        world.getBlockAt(x, y + wallY, z).setType(Material.BARRIER);
                    }
                }
            }
        }
        
        // G\u00E9n\u00E9rer un petit jump al\u00E9atoire pour patienter
        java.util.Random r = new java.util.Random(world.getSeed());
        int jumpY = y + 1;
        int prevX = 0;
        int prevZ = 0;
        for(int i = 0; i < 20; i++) {
            int jX = r.nextInt(24) - 12;
            int jZ = r.nextInt(24) - 12;
            // Assurer que ce n'est pas trop loin du bloc pr\u00E9c\u00E9dent
            jX = Math.max(-14, Math.min(14, prevX + (r.nextInt(7) - 3)));
            jZ = Math.max(-14, Math.min(14, prevZ + (r.nextInt(7) - 3)));
            
            jumpY += r.nextInt(2) + 1; // Monte de 1 ou 2 blocs max
            world.getBlockAt(jX, jumpY, jZ).setType(Material.CYAN_STAINED_GLASS);
            prevX = jX;
            prevZ = jZ;
        }'''

new_platform = '''        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                world.getBlockAt(x, y, z).setType(Material.GLASS);
                if (x == -16 || x == 16 || z == -16 || z == 16) {
                    for (int wallY = 1; wallY <= 4; wallY++) {
                        world.getBlockAt(x, y + wallY, z).setType(Material.BARRIER);
                    }
                }
            }
        }
        
        if (lobbyHologram != null) lobbyHologram.remove();
        lobbyHologram = (ArmorStand) world.spawnEntity(waitingPlatformLocation.clone().add(0, 1.5, 0), EntityType.ARMOR_STAND);
        lobbyHologram.setVisible(false);
        lobbyHologram.setMarker(true);
        lobbyHologram.setGravity(false);
        lobbyHologram.setCustomNameVisible(true);
        lobbyHologram.setCustomName("\u00A7e\u00A7lPour acc\u00E9der au jump, faites /jump");
'''

c = c.replace(old_platform, new_platform)

# Cleanup
old_destroy = '''    private void destroyWaitingPlatform() {
        World world = waitingPlatformLocation.getWorld();
        int y = 250;
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                for (int h = 0; h <= 30; h++) {
                    world.getBlockAt(x, y + h, z).setType(Material.AIR);
                }
            }
        }
    }'''

new_destroy = '''    private void destroyWaitingPlatform() {
        if (lobbyHologram != null) lobbyHologram.remove();
        jumpManager.cleanupAll();
        
        World world = waitingPlatformLocation.getWorld();
        int y = 250;
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                for (int h = 0; h <= 4; h++) {
                    world.getBlockAt(x, y + h, z).setType(Material.AIR);
                }
            }
        }
    }'''

c = c.replace(old_destroy, new_destroy)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
