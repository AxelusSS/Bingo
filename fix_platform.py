import sys

path = 'src/main/java/fr/hel/game/HelGame.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_platform = '''        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                world.getBlockAt(x, y, z).setType(Material.GLASS);
                if (x == -16 || x == 16 || z == -16 || z == 16) {
                    for (int wallY = 1; wallY <= 3; wallY++) {
                        world.getBlockAt(x, y + wallY, z).setType(Material.BARRIER);
                    }
                }
            }
        }'''

new_platform = '''        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);
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

c = c.replace(old_platform, new_platform)

old_destroy = '''    private void destroyWaitingPlatform() {
        World world = waitingPlatformLocation.getWorld();
        int y = 250;
        for (int x = -16; x <= 16; x++) {
            for (int z = -16; z <= 16; z++) {
                world.getBlockAt(x, y, z).setType(Material.AIR);
                for (int wallY = 1; wallY <= 3; wallY++)
                    world.getBlockAt(x, y + wallY, z).setType(Material.AIR);
            }
        }
    }'''

new_destroy = '''    private void destroyWaitingPlatform() {
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

c = c.replace(old_destroy, new_destroy)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
