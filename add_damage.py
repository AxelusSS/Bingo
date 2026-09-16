import sys

listener_path = 'src/main/java/fr/hel/listeners/HelListener.java'
with open(listener_path, 'r', encoding='utf-8') as f:
    c = f.read()

damage_event = '''
    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        HelGame game = HelPlugin.getInstance().getHelGame();
        // Désactiver TOUS les dégâts si la partie n'a pas commencé
        if (game.getState() == fr.hel.game.GameState.WAITING || game.getState() == fr.hel.game.GameState.STARTING) {
            event.setCancelled(true);
        }
    }
'''

c = c.replace('public class HelListener implements Listener {', 'public class HelListener implements Listener {' + damage_event)

with open(listener_path, 'w', encoding='utf-8') as f:
    f.write(c)

