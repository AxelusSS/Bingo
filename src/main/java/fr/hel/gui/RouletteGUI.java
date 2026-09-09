package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.HelGame;
import fr.hel.game.HelObjective;
import fr.hel.game.HelObjectivePool;
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

import org.bukkit.inventory.InventoryHolder;

public class RouletteGUI implements InventoryHolder {

    private final Inventory inventory;

    public RouletteGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "\u00A76\u00A7lRoulette Hel");
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void startRoulette(HelObjective target, Runnable onComplete) {
        RouletteGUI gui = new RouletteGUI();
        Inventory inv = gui.getInventory();

        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName("\u00A7r");
        bg.setItemMeta(bgMeta);

        ItemStack pointerDown = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta pDownMeta = pointerDown.getItemMeta();
        pDownMeta.setDisplayName("\u00A7a\u2B07 L'OBJECTIF \u2B07");
        pointerDown.setItemMeta(pDownMeta);

        ItemStack pointerUp = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta pUpMeta = pointerUp.getItemMeta();
        pUpMeta.setDisplayName("\u00A7a\u2B06 L'OBJECTIF \u2B06");
        pointerUp.setItemMeta(pUpMeta);

        for (int i = 0; i < 9; i++) inv.setItem(i, bg);
        for (int i = 18; i < 27; i++) inv.setItem(i, bg);
        
        // Indicateurs milieu (colonne 4 -> index 4 et 22)
        inv.setItem(4, pointerDown);
        inv.setItem(22, pointerUp);

        // Ouvrir l'inventaire \u00E0 tout le monde
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.openInventory(inv);
        }

        HelGame game = HelPlugin.getInstance().getHelGame();
        
        // R\u00E9cup\u00E9rer un pool d'items au hasard pour faire d\u00E9filer
        List<HelObjectivePool.PoolEntry> rawPool = new ArrayList<>();
        if (game.getMode() == fr.hel.game.HelMode.ITEMS || game.getMode() == fr.hel.game.HelMode.MIXED) {
            rawPool.addAll(HelObjectivePool.getItemPool());
        }
        if (game.getMode() == fr.hel.game.HelMode.ACHIEVEMENTS || game.getMode() == fr.hel.game.HelMode.MIXED) {
            rawPool.addAll(HelObjectivePool.getAchievementPool());
        }
        
        // G\u00E9n\u00E9rer une s\u00E9quence
        int totalShifts = 40;
        List<ItemStack> sequence = new ArrayList<>();
        Random rand = new Random();
        
        for (int i = 0; i < totalShifts + 9; i++) {
            if (!rawPool.isEmpty()) {
                HelObjectivePool.PoolEntry entry = rawPool.get(rand.nextInt(rawPool.size()));
                ItemStack item = new ItemStack(entry.icon);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("\u00A7e\u00A7l" + entry.id.replace("_", " "));
                    item.setItemMeta(meta);
                }
                sequence.add(item);
            } else {
                sequence.add(new ItemStack(Material.STONE));
            }
        }
        
        // Ins\u00E9rer l'item cible pour qu'il s'arr\u00EAte exactement au centre
        ItemStack targetItem = new ItemStack(target.getDisplayMaterial());
        ItemMeta targetMeta = targetItem.getItemMeta();
        if (targetMeta != null) {
            String prefix = target.isAchievement() ? "\u00A7d[Achievement] " : "\u00A7e\u00A7l";
            targetMeta.setDisplayName(prefix + target.getId().replace("_", " "));
            targetItem.setItemMeta(targetMeta);
        }
        sequence.set(totalShifts + 3, targetItem); // Slot 4 (milieu) s'arr\u00EAte sur index totalShifts + 3

        new BukkitRunnable() {
            int ticks = 0;
            int shifts = 0;
            int delay = 2; // Rapide au d\u00E9but

            @Override
            public void run() {
                if (shifts >= totalShifts) {
                    // C'est fini, on joue le son de victoire
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        p.sendTitle("\u00A76\u00A7lOBJECTIF FIX\u00C9 !", targetItem.getItemMeta().getDisplayName(), 10, 50, 10);
                    }
                    
                    Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
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
                    // D\u00E9caler les items
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
        }.runTaskTimer(HelPlugin.getInstance(), 0L, 1L);
    }
}
