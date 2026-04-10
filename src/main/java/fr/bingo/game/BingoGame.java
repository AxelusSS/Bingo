package fr.bingo.game;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BingoGame {
    
    private GameState state;
    private Location waitingPlatformLocation;
    private final BingoGrid grid;
    private long startTime;

    public BingoGame() {
        this.state = GameState.WAITING;
        this.grid = new BingoGrid();
        setupWaitingPlatform();
    }

    public BingoGrid getGrid() {
        return grid;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public long getElapsedSeconds() {
        if (state == GameState.WAITING) return 0;
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    private void setupWaitingPlatform() {
        World world = Bukkit.getWorlds().get(0); // Overworld par défaut
        
        // On génère la plateforme très haut (Y=250) en verre invisible (BARRIER ou GLASS)
        int y = 250;
        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);

        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                world.getBlockAt(x, y, z).setType(Material.GLASS);
                // Murs invisibles pour ne pas tomber
                if (x == -10 || x == 10 || z == -10 || z == 10) {
                    for(int wallY = 1; wallY <= 3; wallY++) {
                        world.getBlockAt(x, y + wallY, z).setType(Material.BARRIER);
                    }
                }
            }
        }
        
        BingoPlugin.getInstance().getLogger().info("Plateforme d'attente générée en " + waitingPlatformLocation.toString());
    }

    public void teleportToWaitingArea(Player player) {
        if (state == GameState.WAITING) {
            player.teleport(waitingPlatformLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    public void startParty() {
        this.state = GameState.PLAYING;
        this.startTime = System.currentTimeMillis();
        // Téléportation de tous les joueurs au sol, distribution du matériel de départ, etc.
        World world = waitingPlatformLocation.getWorld();
        Location spawn = world.getHighestBlockAt(0, 0).getLocation().add(0.5, 1, 0.5);
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(spawn);
            p.setGameMode(GameMode.SURVIVAL);
            p.sendMessage("§a§lLA PARTIE COMMENCE ! Que le meilleur gagne !");
        }
    }

    public void pauseParty() {
        this.state = GameState.PAUSED;
        Bukkit.broadcastMessage("§c§lPARTIE EN PAUSE !");
        // Logique de freeze à implémenter dans les events (annuler déplacements)
    }
}
