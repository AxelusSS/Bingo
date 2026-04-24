package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SwitchScenario extends Scenario {

    private BukkitTask task;
    private final Random random = new Random();

    public SwitchScenario() {
        super("Switch", Material.ENDER_PEARL, "Echange de place et d'équipe aléatoire toutes les 10 min", false);
    }

    @Override
    public void onGameStart() {
        startTask();
    }

    @Override
    public void onEnable() {
        if (BingoPlugin.getInstance().getBingoGame().getState() == fr.bingo.game.GameState.PLAYING) {
            startTask();
        }
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void startTask() {
        if (task != null) task.cancel();
        
        task = Bukkit.getScheduler().runTaskTimer(BingoPlugin.getInstance(), () -> {
            TeamManager tm = BingoPlugin.getInstance().getTeamManager();
            List<BingoTeam> validTeams = new ArrayList<>();
            for (BingoTeam t : tm.getTeams()) {
                if (t.getPlayers().size() > 0 && !t.getName().equals("Spectateur")) {
                    validTeams.add(t);
                }
            }
            
            if (validTeams.size() >= 2) {
                BingoTeam team1 = validTeams.remove(random.nextInt(validTeams.size()));
                BingoTeam team2 = validTeams.remove(random.nextInt(validTeams.size()));
                
                java.util.UUID uuid1 = team1.getPlayers().get(random.nextInt(team1.getPlayers().size()));
                java.util.UUID uuid2 = team2.getPlayers().get(random.nextInt(team2.getPlayers().size()));
                
                Player p1 = Bukkit.getPlayer(uuid1);
                Player p2 = Bukkit.getPlayer(uuid2);
                
                if (p1 != null && p2 != null) {
                    Location loc1 = p1.getLocation();
                    Location loc2 = p2.getLocation();
                    p1.teleport(loc2);
                    p2.teleport(loc1);
                    
                    team1.removePlayer(p1);
                    team2.removePlayer(p2);
                    team1.addPlayer(p2);
                    team2.addPlayer(p1);
                    
                    Bukkit.broadcastMessage("§d§lSWITCH ! §f" + p1.getName() + " §7et §f" + p2.getName() + " §7ont échangé de place et d'équipe !");
                    p1.playSound(p1.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    p2.playSound(p2.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                }
            }
        }, 12000L, 12000L); // 10 minutes
    }
}
