package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.preset.PresetData;
import fr.bingo.preset.PresetManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class PresetConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public PresetConfigGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 54, "§6§lSauvegardes & Presets");
        populate(player);
    }

    public void populate(Player player) {
        inventory.clear();
        
        PresetManager manager = new PresetManager(); // it reloads community file
        Map<String, PresetData> community = manager.getCommunityPresets();
        Map<Integer, String> personal = BingoPlugin.getInstance().getDatabaseManager().getPlayerPresetNames(player.getUniqueId().toString());
        
        // --- COMMUNITY PRESETS ---
        int slot = 0;
        for (Map.Entry<String, PresetData> entry : community.entrySet()) {
            if (slot > 8) break;
            
            String name = entry.getKey();
            PresetData data = entry.getValue();
            
            Material mat = Material.ENCHANTED_BOOK;
            try {
                if (data.icon != null && !data.icon.isEmpty()) {
                    mat = Material.valueOf(data.icon.toUpperCase());
                }
            } catch (Exception ignored) {}
            
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§b§l" + name);
                List<String> lore = new ArrayList<>();
                lore.add("§7Sauvegarde Communautaire");
                lore.add("");
                lore.add("§e► Clic pour charger");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot, item);
            slot++;
        }
        
        // --- BORDURE ---
        ItemStack border = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName("§r");
            border.setItemMeta(borderMeta);
        }
        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, border);
        }
        
        // --- PERSONAL PRESETS ---
        int pSlot = 18;
        for (Map.Entry<Integer, String> entry : personal.entrySet()) {
            if (pSlot > 35) break; // Max 18 saves but logic limits to 10 usually
            
            ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§a§l" + entry.getValue());
                List<String> lore = new ArrayList<>();
                lore.add("§7Sauvegarde Personnelle");
                lore.add("§8ID: " + entry.getKey());
                lore.add("");
                lore.add("§e► Clic Gauche pour charger");
                lore.add("§c► Clic Droit pour supprimer");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(pSlot, item);
            pSlot++;
        }
        
        // --- CREATE BUTTON ---
        ItemStack create = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta createMeta = create.getItemMeta();
        if (createMeta != null) {
            createMeta.setDisplayName("§e§l+ Créer une sauvegarde");
            List<String> lore = new ArrayList<>();
            lore.add("§7Sauvegarde la configuration actuelle");
            lore.add("§7(Équipes, Scénarios, Items...)");
            lore.add("");
            if (personal.size() >= 10) {
                lore.add("§c⚠ Limite de 10 sauvegardes atteinte !");
            } else {
                lore.add("§e► Clic pour créer");
            }
            createMeta.setLore(lore);
            create.setItemMeta(createMeta);
        }
        inventory.setItem(49, create);
        
        // --- BACK ---
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§cRetour");
            back.setItemMeta(backMeta);
        }
        inventory.setItem(45, back);
    }
    
    public void handleClick(Player player, int slot, boolean isRightClick) {
        PresetManager manager = new PresetManager();
        
        if (slot == 45) {
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            player.openInventory(new AdminConfigGUI().getInventory());
            return;
        }
        
        // Créer une sauvegarde
        if (slot == 49) {
            Map<Integer, String> personal = BingoPlugin.getInstance().getDatabaseManager().getPlayerPresetNames(player.getUniqueId().toString());
            if (personal.size() >= 10) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage("§cVous avez atteint la limite de 10 sauvegardes personnelles.");
                return;
            }
            
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            
            player.closeInventory();
            manager.addPendingSave(player.getUniqueId());
            
            player.sendMessage("§e§m----------------------------------");
            player.sendMessage("§a[Bingo] §fVeuillez écrire le §anom de votre sauvegarde §fdans le chat.");
            player.sendMessage("§7(Pour annuler, tapez 'annuler' ou 'cancel')");
            player.sendMessage("§e§m----------------------------------");
                
            return;
        }
        
        // Charger / Supprimer
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
        
        if (slot <= 8) {
            // Communauté
            String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
            Map<String, PresetData> community = manager.getCommunityPresets();
            if (community.containsKey(name)) {
                manager.applyData(community.get(name));
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                player.sendMessage("§a[Bingo] §fPreset '§e" + name + "§f' chargé !");
                player.openInventory(new AdminConfigGUI().getInventory()); // Retour au menu principal pour voir les modifs
            }
        } else if (slot >= 18 && slot <= 35) {
            // Personnel
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null && lore.size() > 1) {
                String idLine = org.bukkit.ChatColor.stripColor(lore.get(1)); // "ID: X"
                if (idLine.startsWith("ID: ")) {
                    try {
                        int id = Integer.parseInt(idLine.substring(4));
                        
                        if (isRightClick) {
                            BingoPlugin.getInstance().getDatabaseManager().deletePreset(id, player.getUniqueId().toString());
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                            player.sendMessage("§c[Bingo] §fSauvegarde supprimée !");
                            populate(player); // Refresh
                        } else {
                            Map<Integer, PresetData> personalPresets = BingoPlugin.getInstance().getDatabaseManager().getPlayerPresets(player.getUniqueId().toString());
                            if (personalPresets.containsKey(id)) {
                                manager.applyData(personalPresets.get(id));
                                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                player.sendMessage("§a[Bingo] §fSauvegarde chargée !");
                                player.openInventory(new AdminConfigGUI().getInventory());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
