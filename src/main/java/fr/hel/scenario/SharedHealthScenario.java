package fr.hel.scenario;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.attribute.Attribute;

public class SharedHealthScenario extends Scenario {

    private boolean isSyncing = false;

    public SharedHealthScenario() {
        super("Shared Health", Material.REDSTONE, "Vie partag\u00E9e avec toute l'\u00E9quipe", false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (isSyncing) return;
        if (event.getEntity() instanceof Player player) {
            HelTeam team = HelPlugin.getInstance().getTeamManager().getPlayerTeam(player);
            if (team == null || team.getPlayers().size() <= 1) return;
            
            double damage = event.getFinalDamage();
            
            isSyncing = true;
            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && !p.equals(player)) {
                    double newHealth = p.getHealth() - damage;
                    if (newHealth <= 0) {
                        p.setHealth(0);
                    } else {
                        p.setHealth(newHealth);
                        p.damage(0.01); // Just to play the damage animation
                    }
                }
            }
            isSyncing = false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (isSyncing) return;
        if (event.getEntity() instanceof Player player) {
            HelTeam team = HelPlugin.getInstance().getTeamManager().getPlayerTeam(player);
            if (team == null || team.getPlayers().size() <= 1) return;
            
            double amount = event.getAmount();
            
            isSyncing = true;
            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && !p.equals(player)) {
                    double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
                    p.setHealth(Math.min(maxHealth, p.getHealth() + amount));
                }
            }
            isSyncing = false;
        }
    }
}
