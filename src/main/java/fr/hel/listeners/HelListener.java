package fr.hel.listeners;

import fr.hel.HelPlugin;
import fr.hel.game.*;
import fr.hel.gui.AdminConfigGUI;
import fr.hel.gui.PvpConfigGUI;
import fr.hel.gui.TeamConfigGUI;
import fr.hel.gui.TeamSelectorGUI;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scheduler.BukkitRunnable;

public class HelListener implements Listener {

    // \u2500\u2500 Sauvegarde d'\u00E9tat joueur pour la reconnexion (T\u00E2che 1) \u2500\u2500
    private static class PlayerState {
        final ItemStack[] inventory;
        final ItemStack[] armor;
        final ItemStack offhand;
        final Location location;
        final GameMode gameMode;
        final double health;
        final int foodLevel;
        final float saturation;
        final float exp;
        final int level;
        final int totalExp;

        PlayerState(Player p) {
            this.inventory = p.getInventory().getStorageContents().clone();
            this.armor = p.getInventory().getArmorContents().clone();
            this.offhand = p.getInventory().getItemInOffHand().clone();
            this.location = p.getLocation().clone();
            this.gameMode = p.getGameMode();
            this.health = p.getHealth();
            this.foodLevel = p.getFoodLevel();
            this.saturation = p.getSaturation();
            this.exp = p.getExp();
            this.level = p.getLevel();
            this.totalExp = p.getTotalExperience();
        }

        void restore(Player p) {
            p.teleport(location);
            p.setGameMode(gameMode);
            p.getInventory().setStorageContents(inventory);
            p.getInventory().setArmorContents(armor);
            p.getInventory().setItemInOffHand(offhand);
            p.setHealth(Math.min(health, p.getMaxHealth()));
            p.setFoodLevel(foodLevel);
            p.setSaturation(saturation);
            p.setExp(exp);
            p.setLevel(level);
            p.setTotalExperience(totalExp);
        }
    }

    private final Map<UUID, PlayerState> savedStates = new ConcurrentHashMap<>();
    private final Set<UUID> seedInputPlayers = new HashSet<>();

    public HelListener() {
        startActionBarFFTask();
    }

    @EventHandler
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (seedInputPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            String message = event.getMessage();
            try {
                long seed;
                try {
                    seed = Long.parseLong(message);
                } catch (NumberFormatException e) {
                    seed = (long) message.hashCode();
                }
                HelPlugin.getInstance().getHelGame().setWorldSeed(seed);
                player.sendMessage("\u00A76\u00A7lHEL \u00A77\u00BB \u00A7aSeed d\u00E9finie sur : \u00A7e" + seed);
            } catch (Exception e) {
                player.sendMessage("\u00A7cErreur lors de la d\u00E9finition de la seed.");
            }
            seedInputPlayers.remove(player.getUniqueId());
            
            // Re-ouvrir le menu (n\u00E9cessite d'\u00EAtre sur le thread principal)
            Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                player.openInventory(new fr.hel.gui.WorldConfigGUI().getInventory());
            });
        }
    }

    public void addSeedInputPlayer(Player player) {
        seedInputPlayers.add(player.getUniqueId());
        player.closeInventory();
        player.sendMessage("");
        player.sendMessage("\u00A76\u00A7l\u2699 CONFIGURATION SEED");
        player.sendMessage("\u00A77Veuillez entrer la seed souhait\u00E9e dans le chat.");
        player.sendMessage("\u00A77(Nombres ou texte autoris\u00E9s)");
        player.sendMessage("");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Custom Join Message
        event.setJoinMessage("\u00A78[\u00A7a+\u00A78] \u00A7f" + player.getName());
        
        HelGame game = HelPlugin.getInstance().getHelGame();
        TeamManager teamManager = HelPlugin.getInstance().getTeamManager();

        HelPlugin.getInstance().getLogger().info("[Hel] Connexion de " + player.getName() + " - Etat: " + game.getState());

        // Si la partie est lanc\u00E9e, on ne touche pas au joueur s'il est d\u00E9j\u00E0 en jeu
        if (game.getState() == GameState.PLAYING) {
            HelTeam team = teamManager.getPlayerTeam(player);
            if (team == null) {
                team = teamManager.getPlayerTeamByName(player.getName());
                if (team != null) HelPlugin.getInstance().getLogger().info("[Hel] Joueur reconnu par pseudo : " + player.getName());
            }

            if (team == null || team.getName().equals("Spectateur")) {
                // Nouveau joueur ou spectateur rejoignant en cours de route
                if (team == null) teamManager.joinTeam(player, teamManager.getSpectatorTeam());
                player.setGameMode(GameMode.SPECTATOR);
                HelPlugin.getInstance().getLogger().info("[Hel] Nouveau joueur/Spec forc\u00E9 en spectateur : " + player.getName());
            } else {
                // Restaurer l'\u00E9tat sauvegard\u00E9 (T\u00E2che 1 \u2014 fix reconnexion)
                PlayerState state = savedStates.remove(player.getUniqueId());
                if (state != null) {
                    // D\u00E9lai de 1 tick pour laisser le serveur finir le login
                    final HelTeam finalTeam = team;
                    Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
                        state.restore(player);
                        game.syncTeamAdvancements(player, finalTeam);
                        HelPlugin.getInstance().getLogger().info("[Hel] \u00C9tat restaur\u00E9 pour " + player.getName() + " (Equipe: " + finalTeam.getName() + ")");
                    }, 1L);
                } else {
                    // Pas d'\u00E9tat sauvegard\u00E9, mais le joueur est dans une \u00E9quipe
                    // On force le gamemode survival et on sync les advancements
                    final HelTeam finalTeam = team;
                    Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
                        player.setGameMode(GameMode.SURVIVAL);
                        game.syncTeamAdvancements(player, finalTeam);
                    }, 1L);
                }
                HelPlugin.getInstance().getLogger().info("[Hel] Reconnexion autoris\u00E9e pour " + player.getName() + " (Equipe: " + team.getName() + ")");
            }

            // D\u00E9bloquer tous les crafts pour le joueur qui rejoint (T\u00E2che 4)
            Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
                discoverAllRecipes(player);
            }, 2L);

            return; // On arr\u00EAte l\u00E0 pour les parties en cours - SECURITE ABSOLUE
        }

        // --- Logique du HUB (WAITING) ---
        if (teamManager.getPlayerTeam(player) == null) {
            teamManager.joinTeam(player, teamManager.getSpectatorTeam());
        }

        if (game.getState() == GameState.WAITING) {
            game.teleportToWaitingArea(player);

            // Banni\u00E8re de s\u00E9lection d'\u00E9quipe (slot 4, centre hotbar)
            teamManager.giveTeamBanner(player);

            // Compas admin
            if (player.hasPermission("bingo.admin")) {
                HelGame.giveAdminCompass(player);
            }

            startActionBarReminder(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Custom Quit Message
        event.setQuitMessage("\u00A78[\u00A7c-\u00A78] \u00A7f" + player.getName());
        
        HelGame game = HelPlugin.getInstance().getHelGame();
        TeamManager teamManager = HelPlugin.getInstance().getTeamManager();

        // Sauvegarder l'\u00E9tat du joueur s'il est en partie (T\u00E2che 1)
        if (game.getState() == GameState.PLAYING) {
            HelTeam team = teamManager.getPlayerTeam(player);
            if (team != null && !team.getName().equals("Spectateur")) {
                savedStates.put(player.getUniqueId(), new PlayerState(player));
                HelPlugin.getInstance().getLogger().info("[Hel] \u00C9tat sauvegard\u00E9 pour " + player.getName() + " (Equipe: " + team.getName() + ")");
            }
        }
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        HelGame game = HelPlugin.getInstance().getHelGame();
        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        
        String mode = tm.isSoloMode() ? "FFA" : "To" + tm.getMaxPlayersPerTeam();
        String diff = game.getDifficulty().getDisplayName();
        String size = game.getGrid().getSize() + "x" + game.getGrid().getSize();
        String type = game.getMode().getDisplayName();

        if (game.getState() == GameState.WAITING) {
            event.setMotd("\u00A76\u00A7lBINGO \u00A77\u00BB \u00A7f" + mode + " \u00A78| \u00A77" + type + " \u00A78| \u00A77" + diff + " \u00A78| \u00A77" + size + "\n\u00A7e\u00A7l\u27A1 \u00A7aEn attente de joueurs...");
        } else if (game.getState() == GameState.PLAYING) {
            long elapsed = game.getElapsedSeconds();
            String timer = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
            event.setMotd("\u00A76\u00A7lBINGO \u00A77\u00BB \u00A7cEn cours \u00A78| \u00A7e" + timer + " \u00A78| \u00A7b" + mode + " \u00A78| \u00A7b" + type + "\n\u00A7e\u00A7l\u27A1 \u00A7f" + diff + " \u00A78| \u00A7f" + size);
        } else {
            event.setMotd("\u00A76\u00A7lBINGO \u00A77\u00BB \u00A78Partie termin\u00E9e");
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        HelGame game = HelPlugin.getInstance().getHelGame();
        if (game.getState() == GameState.PLAYING) {
            UUID uuid = event.getUniqueId();
            String name = event.getName();
            TeamManager tm = HelPlugin.getInstance().getTeamManager();
            
            // On autorise si: pr\u00E9sent au lancement OU dans une \u00E9quipe (y compris spectateur)
            boolean authorized = game.getStartingPlayers().contains(uuid);
            if (!authorized) {
                fr.hel.team.HelTeam team = tm.getPlayerTeam(uuid);
                if (team == null) team = tm.getPlayerTeamByName(name);
                
                if (team != null) {
                    authorized = true;
                }
            }
            
            // OPs toujours autoris\u00E9s
            if (!authorized && !org.bukkit.Bukkit.getOfflinePlayer(uuid).isOp()) {
                long elapsed = game.getElapsedSeconds();
                String timer = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                    "\u00A7c\u00A7lBINGO\n\n\u00A7cUne partie est d\u00E9j\u00E0 lanc\u00E9e !\n\u00A77Temps \u00E9coul\u00E9 : \u00A7e" + timer + "\n\n\u00A77Seuls les participants peuvent rejoindre.");
            }
        }
    }

    private void startActionBarFFTask() {
        Bukkit.getScheduler().runTaskTimer(HelPlugin.getInstance(), () -> {
            HelGame game = HelPlugin.getInstance().getHelGame();
            if (game.getState() != GameState.PLAYING) return;

            TeamManager tm = HelPlugin.getInstance().getTeamManager();
            if (tm.isSoloMode()) return;

            for (HelTeam team : tm.getActiveTeams()) {
                int votes = team.getForfeitVoteCount();
                if (votes > 0) {
                    int total = team.getPlayers().size();
                    String message = "\u00A7c\u00A7lVote FF : \u00A7e" + votes + "\u00A77/\u00A7e" + total;
                    for (UUID uuid : team.getPlayers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    private void startActionBarReminder(Player player) {
        Bukkit.getScheduler().runTaskTimer(HelPlugin.getInstance(), () -> {
            if (!player.isOnline()) return;
            if (HelPlugin.getInstance().getHelGame().getState() != GameState.WAITING) return;

            TeamManager tm = HelPlugin.getInstance().getTeamManager();
            HelTeam team = tm.getPlayerTeam(player);
            boolean isSpectator = (team != null && team.getName().equals("Spectateur"));

            String message;
            if (tm.isSoloMode()) {
                if (isSpectator) {
                    message = "\u00A7e\u00A7l\u2691 Clique sur l'item pour PARTICIPER \u2691";
                } else {
                    message = "\u00A7a\u00A7l\u2691 Vous participez au Hel Solo \u2691";
                }
            } else {
                if (team == null || isSpectator) {
                    message = "\u00A7b\u00A7l\u2691 Clique sur la banni\u00E8re pour choisir ton \u00E9quipe \u2691";
                } else {
                    message = "\u00A7a\u00A7l\u2691 Vous \u00EAtes pr\u00EAt pour la partie ! \u2691";
                }
            }

            player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                net.md_5.bungee.api.chat.TextComponent.fromLegacy(message)
            );
        }, 0L, 30L);
    }

    // \u2500\u2500 Kill Tracking (UHC) \u2500\u2500

    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        HelGame game = HelPlugin.getInstance().getHelGame();
        if (game.getState() != GameState.PLAYING) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && killer != victim) {
            fr.hel.team.TeamManager tm = HelPlugin.getInstance().getTeamManager();
            fr.hel.team.HelTeam killerTeam = tm.getPlayerTeam(killer);
            if (killerTeam != null && !killerTeam.getName().equals("Spectateur")) {
                killerTeam.addKill();
            }
        }
        
        // Mode UHC : check si une seule \u00E9quipe reste en vie
        boolean isHel = HelPlugin.getInstance().getScenarioManager().isScenarioEnabled(fr.hel.scenario.HelScenario.class);
        if (!isHel) {
            victim.setGameMode(GameMode.SPECTATOR);
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
        }

        Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), this::checkUhcEndCondition, 1L);
    }

    // \u2500\u2500 Clic inventaire \u2500\u2500

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // GUI Grille Hel
        if (event.getInventory().getHolder() instanceof fr.hel.gui.HelGridGUI) {
            event.setCancelled(true);
            return;
        }

        // GUI Roulette
        if (event.getInventory().getHolder() instanceof fr.hel.gui.RouletteGUI) {
            event.setCancelled(true);
            return;
        }

        // GUI Admin Config
        if (event.getInventory().getHolder() instanceof AdminConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Hel Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.HelConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Border Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.BorderConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Scenario Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.ScenarioConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Super Hero Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.SuperHeroConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot());
            return;
        }

        // GUI Anonymous Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.AnonymousConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.getClick().isRightClick());
            return;
        }

        // GUI Pool Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.PoolConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot());
            return;
        }

        // GUI Preset Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.PresetConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI PVP Config
        if (event.getInventory().getHolder() instanceof PvpConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI Team Config
        if (event.getInventory().getHolder() instanceof TeamConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // GUI World Config
        if (event.getInventory().getHolder() instanceof fr.hel.gui.WorldConfigGUI gui) {
            event.setCancelled(true);
            gui.handleClick(player, event.getRawSlot(), event.isRightClick());
            return;
        }

        // D\u00E9tection d'items pour le Hel (si dans l'inventaire du joueur)
        if (HelPlugin.getInstance().getHelGame().getState() == GameState.PLAYING) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(clicked));
            }
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(cursor));
            }
        }

        // GUI Team Selector \u2014 clic sur une banni\u00E8re d'\u00E9quipe
        if (event.getInventory().getHolder() instanceof TeamSelectorGUI) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            // Ignorer les glass panes
            if (!event.getCurrentItem().getType().name().endsWith("_BANNER")) return;

            TeamManager teamManager = HelPlugin.getInstance().getTeamManager();
            Material clickedMat = event.getCurrentItem().getType();

            // Banni\u00E8re blanche = spectateur (toujours accessible)
            if (clickedMat == Material.WHITE_BANNER) {
                teamManager.joinTeam(player, teamManager.getSpectatorTeam());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new TeamSelectorGUI().getInventory());
                return;
            }

            // Les autres banni\u00E8res = \u00E9quipes
            if (teamManager.isTeamsLocked() && !player.hasPermission("bingo.admin")) {
                player.sendMessage("\u00A7cLes \u00E9quipes sont verrouill\u00E9es !");
                player.closeInventory();
                return;
            }

            for (HelTeam team : teamManager.getActiveTeams()) {
                if (team.getBannerMaterial() == clickedMat) {
                    if (teamManager.joinTeam(player, team)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                        player.openInventory(new TeamSelectorGUI().getInventory());
                    }
                    return;
                }
            }
        }

        // Emp\u00EAcher de d\u00E9placer la banni\u00E8re de s\u00E9lection en WAITING
        if (HelPlugin.getInstance().getHelGame().getState() == GameState.WAITING) {
            if (event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta()) {
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(HelPlugin.getInstance(), "team_selector");
                if (event.getCurrentItem().getItemMeta().getPersistentDataContainer()
                        .has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // \u2500\u2500 Clic droit : banni\u00E8re d'\u00E9quipe OU compas admin \u2500\u2500

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;
        if (!event.getAction().name().contains("RIGHT")) return;

        org.bukkit.persistence.PersistentDataContainer pdc = event.getItem().getItemMeta().getPersistentDataContainer();

        // Compas admin
        org.bukkit.NamespacedKey compassKey = new org.bukkit.NamespacedKey(HelPlugin.getInstance(), "admin_compass");
        if (pdc.has(compassKey, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (!player.hasPermission("bingo.admin")) {
                player.sendMessage("\u00A7cPermission refus\u00E9e.");
                return;
            }
            player.openInventory(new AdminConfigGUI().getInventory());
            return;
        }

        // Item de s\u00E9lection d'\u00E9quipe (Banni\u00E8re ou T\u00EAte)
        org.bukkit.NamespacedKey teamKey = new org.bukkit.NamespacedKey(HelPlugin.getInstance(), "team_selector");
        if (pdc.has(teamKey, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
            if (HelPlugin.getInstance().getHelGame().getState() != GameState.WAITING) return;
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);

            TeamManager tm = HelPlugin.getInstance().getTeamManager();
            if (tm.isSoloMode()) {
                // Toggle FFA : Joueur <-> Spectateur
                HelTeam current = tm.getPlayerTeam(player);
                if (current != null && current.getName().equals("Spectateur")) {
                    tm.removePlayerFromTeam(player);
                    player.sendMessage("\u00A7a\u00A7lHel \u00A77\u00BB \u00A7fVous participez d\u00E9sormais \u00E0 la partie !");
                } else {
                    tm.joinTeam(player, tm.getSpectatorTeam());
                    player.sendMessage("\u00A7a\u00A7lHel \u00A77\u00BB \u00A77Vous \u00EAtes d\u00E9sormais spectateur.");
                }
                // Mettre \u00E0 jour l'item visuellement
                tm.giveTeamBanner(player);
            } else {
                // Mode \u00C9quipe : Ouvrir le menu classique
                player.openInventory(new TeamSelectorGUI().getInventory());
            }
        }
    }

    // \u2500\u2500 Protection gameplay \u2500\u2500

    @EventHandler
    public void onItemDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        if (HelPlugin.getInstance().getHelGame().getState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        // Emp\u00EAcher de drop la banni\u00E8re de s\u00E9lection
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (dropped.hasItemMeta()) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(HelPlugin.getInstance(), "team_selector");
            if (dropped.getItemMeta().getPersistentDataContainer()
                    .has(key, org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        HelGame game = HelPlugin.getInstance().getHelGame();

        if (game.getState() != GameState.PLAYING) {
            event.setCancelled(true);
            return;
        }

        if (game.isPvpDisabled() || !game.isPvpEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (HelPlugin.getInstance().getHelGame().getState() == GameState.WAITING) {
            if (event.getEntity() instanceof Player p) {
                event.setCancelled(true);
                p.setFoodLevel(20);
                p.setSaturation(20f);
            }
        }
    }

    // \u2500\u2500 D\u00E9tection d'objectifs \u2500\u2500

    private void checkObjective(Player player, String objectiveId) {
        if (objectiveId == null) return;
        if (HelPlugin.getInstance().getHelGame().getState() != GameState.PLAYING) return;

        TeamManager teamManager = HelPlugin.getInstance().getTeamManager();
        HelTeam team = teamManager.getPlayerTeam(player);
        if (team == null || team.getName().equals("Spectateur")) return;

        HelGrid grid = HelPlugin.getInstance().getHelGame().getGrid();
        List<HelObjective> objectives = grid.getObjectives();
        for (int i = 0; i < objectives.size(); i++) {
            HelObjective obj = objectives.get(i);
            if (obj.getId().equalsIgnoreCase(objectiveId)) {
                if (!team.hasUnlocked(objectiveId)) {
                    team.unlockObjective(objectiveId, grid.getSize());

                    // Accorder l'advancement UNIQUEMENT aux joueurs de cette \u00E9quipe
                    for (java.util.UUID uuid : team.getPlayers()) {
                        Player tp = Bukkit.getPlayer(uuid);
                        if (tp != null) {
                            HelPlugin.getInstance().getHelGame().grantHelAdvancement(tp, i);
                        }
                    }

                    String displayName = obj.getId().replace("_", " ");
                    String prefix = obj.isAchievement() ? "\u00A7d[Achievement] " : "";

                    // En FFA : pseudo blanc, pas de couleur d'\u00E9quipe
                    if (teamManager.isSoloMode()) {
                        Bukkit.broadcastMessage("\u00A78[\u00A76Hel\u00A78] \u00A7f" + player.getName() + " \u00A7aa trouv\u00E9 " + prefix + "\u00A7e" + displayName + " \u00A7a!");
                    } else {
                        Bukkit.broadcastMessage("\u00A78[\u00A76Hel\u00A78] " + team.getChatColor() + "L'\u00E9quipe " + team.getName() + " \u00A7aa trouv\u00E9 " + prefix + "\u00A7e" + displayName + " \u00A7a!");
                    }

                    playFoundEffects(player, team);

                    int seconds = (int) HelPlugin.getInstance().getHelGame().getElapsedSeconds();
                    HelPlugin.getInstance().getDatabaseManager().recordStat(player.getName(), seconds, objectiveId);

                    checkTeamCompletion(team, grid);
                }
                break;
            }
        }
    }

    private void playFoundEffects(Player player, HelTeam team) {
        for (java.util.UUID uuid : team.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
                p.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5);
            }
        }
    }

    // \u2500\u2500 Fin de partie \u2500\u2500

    private void checkTeamCompletion(HelTeam team, HelGrid grid) {
        int totalObjectives = grid.getObjectives().size();
        int teamFound = team.getUnlockedObjectives().size();

        if (teamFound >= totalObjectives && !team.isFinished()) {
            team.setFinished(true);
            TeamManager tm = HelPlugin.getInstance().getTeamManager();

            long elapsed = HelPlugin.getInstance().getHelGame().getElapsedSeconds();
            int min = (int) (elapsed / 60);
            int sec = (int) (elapsed % 60);
            String timeStr = String.format("%02d:%02d", min, sec);

            String teamLabel = tm.isSoloMode()
                    ? team.getChatColor() + "\u00A7l\u2605 " + getTeamPlayerName(team) + " a termin\u00E9 le Hel ! \u2605"
                    : team.getChatColor() + "\u00A7l\u2605 L'\u00E9quipe " + team.getName() + " a termin\u00E9 le Hel ! \u2605";

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
            Bukkit.broadcastMessage("  " + teamLabel);
            Bukkit.broadcastMessage("  \u00A77Temps : \u00A7e\u00A7l" + timeStr);
            Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
            Bukkit.broadcastMessage("");

            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    cleanupAndSpectate(p);
                    p.sendTitle(team.getChatColor() + "\u00A7lBINGO !", "\u00A77Temps : \u00A7e" + timeStr, 10, 60, 20);
                    launchFirework(p.getLocation());
                }
            }

            checkEndCondition();
        }
    }

    private String getTeamPlayerName(HelTeam team) {
        if (team.getPlayers().isEmpty()) return "???";
        org.bukkit.OfflinePlayer p = Bukkit.getOfflinePlayer(team.getPlayers().get(0));
        return p.getName() != null ? p.getName() : "???";
    }

    private void cleanupAndSpectate(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
        player.setExp(0f);
        player.setLevel(0);
        player.setTotalExperience(0);

        Iterator<Advancement> advIterator = Bukkit.advancementIterator();
        while (advIterator.hasNext()) {
            Advancement adv = advIterator.next();
            AdvancementProgress progress = player.getAdvancementProgress(adv);
            for (String criteria : progress.getAwardedCriteria()) {
                progress.revokeCriteria(criteria);
            }
        }
    }

    private void launchFirework(Location loc) {
        org.bukkit.entity.Firework fw = loc.getWorld().spawn(loc, org.bukkit.entity.Firework.class);
        org.bukkit.inventory.meta.FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(org.bukkit.FireworkEffect.builder()
                .withColor(Color.YELLOW, Color.ORANGE)
                .withFade(Color.RED)
                .with(org.bukkit.FireworkEffect.Type.STAR)
                .flicker(true)
                .trail(true)
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    public void checkEndCondition() {
        HelGame game = HelPlugin.getInstance().getHelGame();
        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        EndMode endMode = game.getEndMode();

        List<HelTeam> activeTeams = tm.getActiveTeams();
        int totalPlaying = 0;
        int notFinished = 0;

        for (HelTeam t : activeTeams) {
            if (t.getPlayers().isEmpty()) continue;
            totalPlaying++;
            if (!t.isFinished()) notFinished++;
        }

        if (endMode == EndMode.ALL_TEAMS) {
            if (notFinished == 0 && totalPlaying > 0) {
                triggerGameEnd();
            }
        } else if (endMode == EndMode.LAST_STANDING) {
            if (totalPlaying >= 2 && notFinished <= 1) {
                if (notFinished == 1) {
                    for (HelTeam t : activeTeams) {
                        if (!t.getPlayers().isEmpty() && !t.isFinished()) {
                            String label = tm.isSoloMode() ? getTeamPlayerName(t) : "L'\u00E9quipe " + t.getName();
                            Bukkit.broadcastMessage("");
                            Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
                            Bukkit.broadcastMessage("  \u00A76\u00A7l\u23F0 La partie est termin\u00E9e !");
                            Bukkit.broadcastMessage("  " + t.getChatColor() + label + " \u00A77est la derni\u00E8re en jeu.");
                            Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
                            Bukkit.broadcastMessage("");
                            for (java.util.UUID uuid : t.getPlayers()) {
                                Player p = Bukkit.getPlayer(uuid);
                                if (p != null) cleanupAndSpectate(p);
                            }
                            break;
                        }
                    }
                }
                triggerGameEnd();
            } else if (notFinished == 0 && totalPlaying > 0) {
                triggerGameEnd();
            }
        } else if (endMode == EndMode.FIRST_TO_FINISH) {
            if (notFinished < totalPlaying && totalPlaying > 0) {
                triggerGameEnd();
            }
        }
    }

    public void triggerGameEnd() {
        Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("\u00A76\u00A7l\u2726\u2726\u2726 PARTIE TERMIN\u00C9E ! \u2726\u2726\u2726");
            Bukkit.broadcastMessage("\u00A77Merci d'avoir jou\u00E9 !");
            Bukkit.broadcastMessage("");

            displayRanking();

            HelPlugin.getInstance().getHelGame().setState(GameState.FINISHED);
            
            // Mettre tout le monde en spectateur
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setGameMode(GameMode.SPECTATOR);
            }

            // T\u00E2che 2 : D\u00E9compte de 300 secondes dans l'ActionBar avant changement de map
            startEndCountdown();
        }, 60L);
    }

    /**
     * D\u00E9compte de 300 secondes (5 minutes) affich\u00E9 discr\u00E8tement dans l'ActionBar.
     * \u00C0 la fin, d\u00E9clenche le changement de map automatique.
     */
    private void startEndCountdown() {
        new BukkitRunnable() {
            int remaining = 300;

            @Override
            public void run() {
                if (remaining <= 0) {
                    this.cancel();
                    Bukkit.broadcastMessage("\u00A76\u00A7l\u25BA Changement de carte en cours...");
                    HelPlugin.getInstance().getHelGame().prepareWorldReset(null);
                    return;
                }

                int min = remaining / 60;
                int sec = remaining % 60;
                String timeStr = min > 0
                        ? "\u00A7b" + min + "m" + String.format("%02d", sec) + "s"
                        : "\u00A7b" + sec + "s";
                String actionBarMsg = "\u00A77Changement de carte dans " + timeStr;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacy(actionBarMsg));
                }

                remaining--;
            }
        }.runTaskTimer(HelPlugin.getInstance(), 0L, 20L);
    }

    private void displayRanking() {
        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        List<HelTeam> ranking = new java.util.ArrayList<>(tm.getActiveTeams());
        ranking.removeIf(t -> t.getPlayers().isEmpty());
        ranking.sort((a, b) -> {
            if (a.isFinished() && !b.isFinished()) return -1;
            if (!a.isFinished() && b.isFinished()) return 1;
            if (a.isFinished() && b.isFinished()) return Long.compare(a.getFinishedTime(), b.getFinishedTime());
            return Integer.compare(b.getScore(), a.getScore());
        });

        Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
        Bukkit.broadcastMessage("  \u00A76\u00A7l\u2726 CLASSEMENT FINAL \u2726");
        String[] medals = {"\u00A7e\u00A7l\u1F947 ", "\u00A7f\u00A7l\u1F948 ", "\u00A76\u00A7l\u1F949 ", "\u00A77   ", "\u00A77   "};
        for (int i = 0; i < ranking.size(); i++) {
            HelTeam t = ranking.get(i);
            String medal = i < medals.length ? medals[i] : "\u00A77   ";
            String name = tm.isSoloMode() ? getTeamPlayerName(t) : t.getName();
            String status = t.isFinished() ? "\u00A7a\u2714 Termin\u00E9" : "\u00A77" + t.getScore() + " pts";
            Bukkit.broadcastMessage("  " + medal + t.getChatColor() + name + " \u00A78- " + status);
        }
        Bukkit.broadcastMessage("\u00A78\u00A7m                                                \u00A7r");
    }

    // \u2500\u2500 Events de d\u00E9tection \u2500\u2500

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            checkObjective(player, getIdentifier(event.getItem().getItemStack()));
        }
    }
 
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(result));
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            ItemStack dragged = event.getOldCursor();
            if (dragged != null && dragged.getType() != Material.AIR) {
                checkObjective(player, getIdentifier(dragged));
            }
        }
    }

    private String getIdentifier(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (item.getType() == Material.POTION) {
            if (item.hasItemMeta() && item.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta meta) {
                org.bukkit.potion.PotionType type = meta.getBasePotionType();
                String name = type.name();
                
                if (name.contains("SWIFTNESS")) return getPotionId("SPEED", name);
                if (name.contains("STRENGTH")) return getPotionId("STRENGTH", name);
                if (name.contains("LEAPING")) return getPotionId("JUMP", name);
                if (name.contains("FIRE_RES")) return getPotionId("FIRE_RES", name);
                if (name.contains("WATER_BREATH")) return getPotionId("WATER_BREATH", name);
                if (name.contains("REGENERATION")) return getPotionId("REGEN", name);
                if (name.contains("INVISIBILITY")) return getPotionId("INVIS", name);
                if (name.contains("NIGHT_VISION")) return getPotionId("NIGHT_VIS", name);
            }
            return "POTION";
        }
        return item.getType().name();
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().getKey();
        if (key.contains("recipes/")) return;
        if (event.getAdvancement().getKey().getNamespace().equals("bingoclassique")) return;
        checkObjective(event.getPlayer(), key);
    }

    // \u2500\u2500 Chat team \u2500\u2500

    @EventHandler
    public void onPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // --- V\u00E9rification Preset Save ---
        fr.hel.preset.PresetManager manager = new fr.hel.preset.PresetManager();
        fr.hel.preset.PresetData pending = manager.getPendingSave(player.getUniqueId());
        if (pending != null) {
            event.setCancelled(true);
            
            if (message.equalsIgnoreCase("annuler") || message.equalsIgnoreCase("cancel")) {
                manager.removePendingSave(player.getUniqueId());
                player.sendMessage("\u00A7c[Hel] \u00A7fCr\u00E9ation de la sauvegarde annul\u00E9e.");
                return;
            }
            
            manager.removePendingSave(player.getUniqueId());
            String json = manager.toJson(pending);
            HelPlugin.getInstance().getDatabaseManager().savePreset(message, player.getUniqueId().toString(), json);
            
            player.sendMessage("\u00A7a[Hel] \u00A7fSauvegarde '\u00A7e" + message + "\u00A7f' cr\u00E9\u00E9e avec succ\u00E8s !");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
            
            Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                player.openInventory(new fr.hel.gui.PresetConfigGUI(player).getInventory());
            });
            return;
        }
        // --------------------------------

        TeamManager teamManager = HelPlugin.getInstance().getTeamManager();
        HelTeam team = teamManager.getPlayerTeam(player);

        String dName = player.getDisplayName();
        fr.hel.scenario.AnonymousScenario anon = HelPlugin.getInstance().getScenarioManager().getScenario(fr.hel.scenario.AnonymousScenario.class);
        if (anon != null && anon.isEnabled() && anon.isGlitchedNames()) {
            dName = "\u00A7k12345678";
            team = null; // Remove team coloring in chat
        }

        if (message.startsWith("!")) {
            event.setFormat("\u00A78[\u00A77Global\u00A78] " + (team != null ? team.getChatColor() : "\u00A77") + dName + " \u00A78\u00BB \u00A7f%2$s");
            event.setMessage(message.substring(1));
        } else {
            if (team == null || team.getName().equals("Spectateur")) {
                event.setFormat("\u00A78[\u00A77Spectateur\u00A78] \u00A77" + dName + " \u00A78\u00BB \u00A7f%2$s");
                return;
            }

            event.setCancelled(true);
            String teamPrefix;
            if (teamManager.isSoloMode()) {
                teamPrefix = "\u00A78[" + team.getChatColor() + "Chat\u00A78] ";
            } else {
                teamPrefix = "\u00A78[" + team.getChatColor() + "Team " + team.getName() + "\u00A78] ";
            }
            String formattedMessage = teamPrefix + team.getChatColor() + dName + " \u00A78\u00BB \u00A7f" + message;

            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(formattedMessage);
            }

            HelTeam specTeam = teamManager.getSpectatorTeam();
            for (java.util.UUID uuid : specTeam.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage("\u00A78[\u00A7cSpy\u00A78] " + formattedMessage);
            }
        }
    }

    @EventHandler
    public void onCommandPreprocess(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        if (HelPlugin.getInstance().getHelGame().getState() != GameState.PLAYING) return;
        String msg = event.getMessage().toLowerCase();
        if (msg.startsWith("/msg ") || msg.startsWith("/tell ") || msg.startsWith("/w ") || msg.startsWith("/r ") || msg.startsWith("/whisper ")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("\u00A7cLes messages priv\u00E9s sont d\u00E9sactiv\u00E9s pendant le Hel !");
        }
    }

    private String getPotionId(String base, String typeName) {
        String suffix = "_1";
        if (typeName.startsWith("STRONG_")) suffix = "_2";
        else if (typeName.startsWith("LONG_")) suffix = "_EXT";
        return "POTION_" + base + suffix;
    }

    /**
     * T\u00E2che 4 : D\u00E9couvre toutes les recettes du serveur pour un joueur.
     * Permet d'avoir le livre de recettes enti\u00E8rement d\u00E9bloqu\u00E9.
     */
    public static void discoverAllRecipes(Player player) {
        java.util.List<org.bukkit.NamespacedKey> keys = new java.util.ArrayList<>();
        Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            org.bukkit.inventory.Recipe recipe = it.next();
            if (recipe instanceof org.bukkit.Keyed keyed) {
                keys.add(keyed.getKey());
            }
        }
        if (!keys.isEmpty()) {
            player.discoverRecipes(keys);
        }
    }

    /**
     * V\u00E9rifie si une seule \u00E9quipe (ou joueur) reste en vie en mode UHC.
     */
    private void checkUhcEndCondition() {
        HelGame game = HelPlugin.getInstance().getHelGame();
        if (game.getState() != GameState.PLAYING) return;
        
        // Si le Hel est actif, la condition de fin est diff\u00E9rente (items)
        if (HelPlugin.getInstance().getScenarioManager().isScenarioEnabled(fr.hel.scenario.HelScenario.class)) {
            return;
        }

        TeamManager tm = HelPlugin.getInstance().getTeamManager();
        List<HelTeam> aliveTeams = new java.util.ArrayList<>();

        for (HelTeam team : tm.getActiveTeams()) {
            boolean hasAlivePlayer = false;
            for (java.util.UUID uuid : team.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.getGameMode() == GameMode.SURVIVAL) {
                    hasAlivePlayer = true;
                    break;
                }
            }
            if (hasAlivePlayer) {
                aliveTeams.add(team);
            }
        }

        if (aliveTeams.size() <= 1 && tm.getActiveTeams().size() > 1) {
            if (aliveTeams.size() == 1) {
                HelTeam winner = aliveTeams.get(0);
                String label = tm.isSoloMode() ? getTeamPlayerName(winner) : "L'\u00E9quipe " + winner.getName();
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("\u00A76\u00A7l\u1F3C6 VICTOIRE \u1F3C6");
                Bukkit.broadcastMessage("  " + winner.getChatColor() + label + " \u00A77est la derni\u00E8re en vie !");
                Bukkit.broadcastMessage("");
                winner.setFinished(true);
            }
            game.forceGameEnd("Derni\u00E8re \u00E9quipe en vie");
        }
    }
}
