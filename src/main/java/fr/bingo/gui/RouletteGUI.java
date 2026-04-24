package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
import fr.bingo.game.BingoObjective;
import fr.bingo.game.BingoObjectivePool;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RouletteGUI {

    public static void startRoulette(BingoObjective target, Runnable onComplete) {
        Inventory inv = Bukkit.createInventory(null, 27, "§6§lRoulette Bingo");

        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName("§r");
        bg.setItemMeta(bgMeta);

        ItemStack pointerDown = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta pDownMeta = pointerDown.getItemMeta();
        pDownMeta.setDisplayName("§a⬇ L'OBJECTIF ⬇");
        pointerDown.setItemMeta(pDownMeta);

        ItemStack pointerUp = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta pUpMeta = pointerUp.getItemMeta();
        pUpMeta.setDisplayName("§a⬆ L'OBJECTIF ⬆");
        pointerUp.setItemMeta(pUpMeta);

        for (int i = 0; i < 9; i++) inv.setItem(i, bg);
        for (int i = 18; i < 27; i++) inv.setItem(i, bg);
        
        // Indicateurs milieu (colonne 4 -> index 4 et 22)
        inv.setItem(4, pointerDown);
        inv.setItem(22, pointerUp);

        // Ouvrir l'inventaire à tout le monde
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.openInventory(inv);
        }

        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        
        // Récupérer un pool d'items au hasard pour faire défiler
        List<BingoObjectivePool.PoolEntry> rawPool = new ArrayList<>();
        if (game.getMode() == fr.bingo.game.BingoMode.ITEMS || game.getMode() == fr.bingo.game.BingoMode.MIXED) {
            rawPool.addAll(BingoObjectivePool.getItemPool());
        }
        if (game.getMode() == fr.bingo.game.BingoMode.ACHIEVEMENTS || game.getMode() == fr.bingo.game.BingoMode.MIXED) {
            rawPool.addAll(BingoObjectivePool.getAchievementPool());
        }
        
        // Générer une séquence
        int totalShifts = 40;
        List<ItemStack> sequence = new ArrayList<>();
        Random rand = new Random();
        
        for (int i = 0; i < totalShifts + 9; i++) {
            if (!rawPool.isEmpty()) {
                BingoObjectivePool.PoolEntry entry = rawPool.get(rand.nextInt(rawPool.size()));
                ItemStack item = new ItemStack(entry.icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§e§l" + entry.id.replace("_", " "));
                    item.setItemMeta(meta);
                }
                sequence.add(item);
            } else {
                sequence.add(new ItemStack(Material.STONE));
            }
        }
        
        // Insérer l'item cible pour qu'il s'arrête exactement au centre
        ItemStack targetItem = new ItemStack(target.getDisplayMaterial());
        ItemMeta targetMeta = targetItem.getItemMeta();
        if (targetMeta != null) {
            String prefix = target.isAchievement() ? "§d[Achievement] " : "§e§l";
            targetMeta.setDisplayName(prefix + target.getId().replace("_", " "));
            targetItem.setItemMeta(targetMeta);
        }
        sequence.set(totalShifts + 4, targetItem); // Slot 4 est le milieu de la ligne 9-17

        new BukkitRunnable() {
            int ticks = 0;
            int shifts = 0;
            int delay = 2; // Rapide au début

            @Override
            public void run() {
                if (shifts >= totalShifts) {
                    // C'est fini, on joue le son de victoire
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        p.sendTitle("§6§lOBJECTIF FIXÉ !", targetItem.getItemMeta().getDisplayName(), 10, 50, 10);
                    }
                    
                    Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getOpenInventory().getTopInventory().equals(inv)) {
                                p.closeInventory();
                            }
                        }
                        onComplete.run();
                    }, 60L); // 3 secondes de pause avant de fermer
                    
                    this.cancel();
                    return;
                }

                if (ticks % delay == 0) {
                    // Décaler les items
                    for (int i = 0; i < 9; i++) {
                        inv.setItem(9 + i, sequence.get(shifts + i));
                    }
                    shifts++;
                    
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    }

                    // Ralentissement progressif
                    if (shifts > totalShifts * 0.5) delay = 3;
                    if (shifts > totalShifts * 0.7) delay = 5;
                    if (shifts > totalShifts * 0.85) delay = 8;
                    if (shifts > totalShifts * 0.95) delay = 12;
                }
                ticks++;
            }
        }.runTaskTimer(BingoPlugin.getInstance(), 0L, 1L);
    }
}
