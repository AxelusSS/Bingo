import sys

listener_path = 'src/main/java/fr/hel/listeners/HelListener.java'
with open(listener_path, 'r', encoding='utf-8') as f:
    c = f.read()

old_tp = '''            // Failsafe pour ne pas tomber de la map d'attente
            if (p.getLocation().getY() < 248) {
                game.teleportToWaitingArea(p);
                game.getJumpManager().cleanupPlayer(p);
            }'''

new_tp = '''            // Failsafe pour ne pas tomber de la map d'attente
            if (p.getLocation().getY() < 248) {
                if (p.getGameMode() != org.bukkit.GameMode.CREATIVE && p.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                    game.teleportToWaitingArea(p);
                    game.getJumpManager().cleanupPlayer(p);
                }
            }'''

c = c.replace(old_tp, new_tp)

with open(listener_path, 'w', encoding='utf-8') as f:
    f.write(c)

jump_path = 'src/main/java/fr/hel/game/JumpManager.java'
with open(jump_path, 'r', encoding='utf-8') as f:
    j = f.read()

old_gen = '''                int nx = lastLoc.getBlockX() + dx;
                int nz = lastLoc.getBlockZ() + dz;
                int ny = lastLoc.getBlockY() + dy;

                if (nx >= -14 && nx <= 14 && nz >= -14 && nz <= 14 && ny >= 253 && ny <= 310) {
                    valid = true;
                    lastLoc = new Location(lastLoc.getWorld(), nx, ny, nz);
                }'''

new_gen = '''                int nx = lastLoc.getBlockX() + dx;
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
                }'''

j = j.replace(old_gen, new_gen)

with open(jump_path, 'w', encoding='utf-8') as f:
    f.write(j)

