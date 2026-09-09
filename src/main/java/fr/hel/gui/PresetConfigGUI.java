package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.preset.PresetData;
import fr.hel.preset.PresetManager;
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
        this.inventory = Bukkit.createInventory(this, 54, "\u00A76\u00A7lSauvegardes & Presets");
        populate(player);
    }

    public void populate(Player player) {
        inventory.clear();
        
        PresetManager manager = new PresetManager(); // it reloads community file
        Map<String, PresetData> community = manager.getCommunityPresets();
        Map<Integer, String> personal = HelPlugin.getInstance().getDatabaseManager().getPlayerPresetNames(player.getUniqueId().toString());
        
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
                meta.setDisplayName("\u00A7b\u00A7l" + name);
                List<String> lore = new ArrayList<>();
                lore.add("\u00A77Sauvegarde Communautaire");
                lore.add("");
                lore.add("\u00A7e\u25BA Clic pour charger");
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
            borderMeta.setDisplayName("\u00A7r");
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
                meta.setDisplayName("\u00A7a\u00A7l" + entry.getValue());
                List<String> lore = new ArrayList<>();
                lore.add("\u00A77Sauvegarde Personnelle");
                lore.add("\u00A78ID: " + entry.getKey());
                lore.add("");
                lore.add("\u00A7e\u25BA Clic Gauche pour charger");
                lore.add("\u00A7c\u25BA Clic Droit pour supprimer");
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
            createMeta.setDisplayName("\u00A7e\u00A7l+ Cr\u00E9er une sauvegarde");
            List<String> lore = new ArrayList<>();
            lore.add("\u00A77Sauvegarde la configuration actuelle");
            lore.add("\u00A77(\u00C9quipes, Sc\u00E9narios, Items...)");
            lore.add("");
            if (personal.size() >= 10) {
                lore.add("\u00A7c\u26A0 Limite de 10 sauvegardes atteinte !");
            } else {
                lore.add("\u00A7e\u25BA Clic pour cr\u00E9er");
            }
            createMeta.setLore(lore);
            create.setItemMeta(createMeta);
        }
        inventory.setItem(49, create);
        
        // --- BACK ---
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("\u00A7cRetour");
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
        
        // Cr\u00E9er une sauvegarde
        if (slot == 49) {
            Map<Integer, String> personal = HelPlugin.getInstance().getDatabaseManager().getPlayerPresetNames(player.getUniqueId().toString());
            if (personal.size() >= 10) {
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage("\u00A7cVous avez atteint la limite de 10 sauvegardes personnelles.");
                return;
            }
            
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            
            player.closeInventory();
            manager.addPendingSave(player.getUniqueId());
            
            player.sendMessage("\u00A7e\u00A7m----------------------------------");
            player.sendMessage("\u00A7a[HEL] \u00A7fVeuillez \u00E9crire le \u00A7anom de votre sauvegarde \u00A7fdans le chat.");
            player.sendMessage("\u00A77(Pour annuler, tapez 'annuler' ou 'cancel')");
            player.sendMessage("\u00A7e\u00A7m----------------------------------");
                
            return;
        }
        
        // Charger / Supprimer
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getType() == Material.AIR || item.getType() == Material.BLACK_STAINED_GLASS_PANE) return;
        
        if (slot <= 8) {
            // Communaut\u00E9
            String name = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
            Map<String, PresetData> community = manager.getCommunityPresets();
            if (community.containsKey(name)) {
                manager.applyData(community.get(name));
                HelPlugin.getInstance().getHelGame().setActivePresetName(name);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                player.sendMessage("\u00A7a[HEL] \u00A7fPreset '\u00A7e" + name + "\u00A7f' charg\u00E9 !");
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
                            HelPlugin.getInstance().getDatabaseManager().deletePreset(id, player.getUniqueId().toString());
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                            player.sendMessage("\u00A7c[HEL] \u00A7fSauvegarde supprim\u00E9e !");
                            populate(player); // Refresh
                        } else {
                            Map<Integer, PresetData> personalPresets = HelPlugin.getInstance().getDatabaseManager().getPlayerPresets(player.getUniqueId().toString());
                            if (personalPresets.containsKey(id)) {
                                manager.applyData(personalPresets.get(id));
                                String saveName = org.bukkit.ChatColor.stripColor(item.getItemMeta().getDisplayName());
                                HelPlugin.getInstance().getHelGame().setActivePresetName(saveName);
                                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                player.sendMessage("\u00A7a[HEL] \u00A7fSauvegarde charg\u00E9e !");
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
