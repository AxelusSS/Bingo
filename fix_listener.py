import sys

listener_path = 'src/main/java/fr/hel/listeners/HelListener.java'
with open(listener_path, 'r', encoding='utf-8') as f:
    c = f.read()

move_handler = '''
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        HelGame game = HelPlugin.getInstance().getHelGame();
        if (game.getState() == fr.hel.game.GameState.WAITING) {
            org.bukkit.entity.Player p = event.getPlayer();
            
            // Failsafe pour ne pas tomber de la map d'attente
            if (p.getLocation().getY() < 248) {
                game.teleportToWaitingArea(p);
                game.getJumpManager().cleanupPlayer(p);
            }
            
            // Logique de g\u00E9n\u00E9ration de saut infinie
            if (event.getTo() != null && (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ())) {
                game.getJumpManager().handleMove(p, event.getTo());
            }
        }
    }
'''

c = c.replace('public class HelListener implements Listener {', 'public class HelListener implements Listener {' + move_handler)

with open(listener_path, 'w', encoding='utf-8') as f:
    f.write(c)

