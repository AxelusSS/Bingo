package fr.bingo.game;

import fr.bingo.BingoPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BingoGame {

    private GameState state;
    private Location waitingPlatformLocation;
    private final BingoGrid grid;
    private long startTime;
    private long pausedElapsed; // temps écoulé au moment de la pause
    private Difficulty difficulty = Difficulty.HARD;
    private BingoMode mode = BingoMode.ITEMS;
    private int gameDurationMinutes = 120; // durée par défaut 2h

    public BingoGame() {
        this.state = GameState.WAITING;
        this.grid = new BingoGrid();
        setupWaitingPlatform();
    }

    // ── Getters/Setters ──

    public BingoGrid getGrid() { return grid; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty d) { this.difficulty = d; }
    public BingoMode getMode() { return mode; }
    public void setMode(BingoMode m) { this.mode = m; }
    public long getStartTime() { return startTime; }
    public int getGameDurationMinutes() { return gameDurationMinutes; }
    public void setGameDurationMinutes(int minutes) { this.gameDurationMinutes = minutes; }

    public long getElapsedSeconds() {
        if (state == GameState.WAITING) return 0;
        if (state == GameState.PAUSED) return pausedElapsed;
        return (System.currentTimeMillis() - startTime) / 1000;
    }

    // ── Plateforme d'attente ──

    private void setupWaitingPlatform() {
        World world = Bukkit.getWorlds().get(0);
        int y = 250;
        this.waitingPlatformLocation = new Location(world, 0.5, y + 1, 0.5);
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                world.getBlockAt(x, y, z).setType(Material.GLASS);
                if (x == -10 || x == 10 || z == -10 || z == 10) {
                    for (int wallY = 1; wallY <= 3; wallY++) {
                        world.getBlockAt(x, y + wallY, z).setType(Material.BARRIER);
                    }
                }
            }
        }
    }

    public void teleportToWaitingArea(Player player) {
        if (state == GameState.WAITING) {
            player.teleport(waitingPlatformLocation);
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    // ── Démarrage ──

    public void startParty() {
        if (this.state == GameState.PLAYING) return;

        Bukkit.broadcastMessage("§6§l►► La partie commence dans 5 secondes ! ◄◄");

        for (int i = 5; i >= 1; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                String color = count <= 2 ? "§c§l" : count <= 3 ? "§e§l" : "§a§l";
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(color + count, "§7Préparez-vous...", 0, 25, 5);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                }
            }, (5 - count) * 20L);
        }

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            this.state = GameState.PLAYING;
            this.startTime = System.currentTimeMillis();

            BingoPlugin.getInstance().getTeamManager().setTeamsLocked(true);

            World world = Bukkit.getWorlds().get(0);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setTime(6000);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("§a§lGO !", "§eBonne chance !", 0, 30, 10);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.7f, 1.5f);
                p.setGameMode(GameMode.SURVIVAL);
                p.getInventory().clear();
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.COOKED_BEEF, 64));
                p.setInvulnerable(true);
            }

            destroyWaitingPlatform();

            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.setInvulnerable(false);
                }
            }, 200L);

            Bukkit.broadcastMessage("§6§l►► BINGO DÉMARRE ! ◄◄ §r§eQue le meilleur gagne !");
        }, 5 * 20L);
    }

    private void destroyWaitingPlatform() {
        World world = waitingPlatformLocation.getWorld();
        int y = 250;
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                world.getBlockAt(x, y, z).setType(Material.AIR);
                for (int wallY = 1; wallY <= 3; wallY++)
                    world.getBlockAt(x, y + wallY, z).setType(Material.AIR);
            }
        }
    }

    // ── Pause / Resume ──

    public void pauseParty() {
        if (this.state != GameState.PLAYING) return;
        this.pausedElapsed = getElapsedSeconds();
        this.state = GameState.PAUSED;

        // Freeze tous les joueurs
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 250, false, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, Integer.MAX_VALUE, 255, false, false, false));
            p.setInvulnerable(true);
            p.sendTitle("§c§lPAUSE", "§7La partie est en pause", 0, 60, 10);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 0.5f);
        }
        Bukkit.broadcastMessage("§c§l►► PARTIE EN PAUSE ◄◄");
    }

    public void resumeParty() {
        if (this.state != GameState.PAUSED) return;
        // Recalculer le startTime pour conserver le temps écoulé
        this.startTime = System.currentTimeMillis() - (pausedElapsed * 1000);
        this.state = GameState.PLAYING;

        // Unfreeze tous les joueurs
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.MINING_FATIGUE);
            p.setInvulnerable(false);
            p.sendTitle("§a§lREPRISE !", "§eC'est reparti !", 0, 30, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
        Bukkit.broadcastMessage("§a§l►► REPRISE DE LA PARTIE ! ◄◄");
    }

    // ── Reset ──

    public void resetGame() {
        this.state = GameState.WAITING;

        BingoPlugin.getInstance().getTeamManager().setTeamsLocked(false);

        // Reset toutes les équipes
        for (fr.bingo.team.BingoTeam team : BingoPlugin.getInstance().getTeamManager().getTeams()) {
            team.getUnlockedObjectives().clear();
            team.setFinished(false);
            team.setScore(0);
        }

        setupWaitingPlatform();

        for (Player p : Bukkit.getOnlinePlayers()) {
            // Retirer les effets de pause si présents
            p.removePotionEffect(PotionEffectType.SLOWNESS);
            p.removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.removePotionEffect(PotionEffectType.MINING_FATIGUE);

            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().clear();
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(20f);
            p.setInvulnerable(false);
            teleportToWaitingArea(p);
            BingoPlugin.getInstance().getTeamManager().giveTeamBanners(p);

            // Donner le compas admin
            if (p.hasPermission("bingo.admin")) {
                giveAdminCompass(p);
            }
        }

        World world = Bukkit.getWorlds().get(0);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);

        Bukkit.broadcastMessage("§6§l►► La partie a été réinitialisée ! ◄◄");
    }

    // ── Admin Compass ──

    public static void giveAdminCompass(Player player) {
        org.bukkit.inventory.ItemStack compass = new org.bukkit.inventory.ItemStack(Material.COMPASS);
        org.bukkit.inventory.meta.ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§6§l⚙ Configuration Bingo");
        meta.setLore(java.util.List.of("§7Clic droit pour configurer la partie"));
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(BingoPlugin.getInstance(), "admin_compass");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        compass.setItemMeta(meta);
        player.getInventory().setItem(8, compass); // Slot 9 (dernier)
    }
}
