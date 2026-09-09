package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.HelObjectivePool;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PoolConfigGUI implements InventoryHolder {

    private final Inventory inventory;
    private final int page;
    private final List<HelObjectivePool.PoolEntry> allItems;

    public PoolConfigGUI(int page) {
        this.page = page;
        this.inventory = Bukkit.createInventory(this, 54, "\u00A76\u00A7lPool Hel - Page " + (page + 1));
        
        this.allItems = new ArrayList<>();
        this.allItems.addAll(HelObjectivePool.getItemPool());
        this.allItems.addAll(HelObjectivePool.getAchievementPool());
        
        populate();
    }

    public void populate() {
        inventory.clear();
        
        Set<String> disabled = HelPlugin.getInstance().getHelGame().getDisabledPoolItems();
        
        int startIndex = page * 45;
        int slot = 0;
        
        for (int i = startIndex; i < allItems.size() && slot < 45; i++) {
            HelObjectivePool.PoolEntry entry = allItems.get(i);
            
            ItemStack item = new ItemStack(entry.icon);
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                String displayName = entry.id.replace("_", " ");
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
                
                if (entry.id.startsWith("POTION_") && item.getType() == Material.POTION) {
                    applyPotionMeta(item, entry.id);
                    meta = item.getItemMeta(); 
                    displayName = getPotionDisplayName(entry.id);
                }
                
                boolean isDisabled = disabled.contains(entry.id);
                
                meta.setDisplayName((isDisabled ? "\u00A7c" : "\u00A7a") + displayName);
                
                List<String> lore = new ArrayList<>();
                lore.add("\u00A77Difficult\u00E9: \u00A7e" + entry.difficulty.name());
                if (entry.isAchievement) {
                    lore.add("\u00A7d[Achievement]");
                }
                lore.add("");
                lore.add(isDisabled ? "\u00A7c\u25B8 D\u00E9sactiv\u00E9" : "\u00A7a\u25B8 Activ\u00E9");
                lore.add("");
                lore.add("\u00A7e\u25BA Clic pour basculer");
                
                meta.setLore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(meta);
            }
            
            inventory.setItem(slot, item);
            slot++;
        }
        
        // Navigation
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("\u00A7aPage Pr\u00E9c\u00E9dente");
            prev.setItemMeta(prevMeta);
            inventory.setItem(45, prev);
        }
        
        if (startIndex + 45 < allItems.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("\u00A7aPage Suivante");
            next.setItemMeta(nextMeta);
            inventory.setItem(53, next);
        }
        
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("\u00A7cRetour");
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);
    }
    
    public void handleClick(Player player, int slot) {
        if (slot == 45 && page > 0) {
            player.openInventory(new PoolConfigGUI(page - 1).getInventory());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            return;
        }
        
        if (slot == 53 && (page * 45) + 45 < allItems.size()) {
            player.openInventory(new PoolConfigGUI(page + 1).getInventory());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            return;
        }
        
        if (slot == 49) {
            player.openInventory(new AdminConfigGUI().getInventory());
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            return;
        }
        
        if (slot < 45) {
            int index = (page * 45) + slot;
            if (index < allItems.size()) {
                HelObjectivePool.PoolEntry entry = allItems.get(index);
                Set<String> disabled = HelPlugin.getInstance().getHelGame().getDisabledPoolItems();
                
                if (disabled.contains(entry.id)) {
                    disabled.remove(entry.id);
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1f);
                } else {
                    disabled.add(entry.id);
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1f);
                }
                
                populate();
            }
        }
    }
    
    private void applyPotionMeta(ItemStack item, String id) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return;
        
        PotionType type;
        
        if (id.contains("SPEED")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_SWIFTNESS;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_SWIFTNESS;
            else type = PotionType.SWIFTNESS;
        } else if (id.contains("STRENGTH")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_STRENGTH;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_STRENGTH;
            else type = PotionType.STRENGTH;
        } else if (id.contains("JUMP")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_LEAPING;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_LEAPING;
            else type = PotionType.LEAPING;
        } else if (id.contains("FIRE_RES")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_FIRE_RESISTANCE;
            else type = PotionType.FIRE_RESISTANCE;
        } else if (id.contains("WATER_BREATH")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_WATER_BREATHING;
            else type = PotionType.WATER_BREATHING;
        } else if (id.contains("REGEN")) {
            if (id.endsWith("_2")) type = PotionType.STRONG_REGENERATION;
            else if (id.endsWith("_EXT")) type = PotionType.LONG_REGENERATION;
            else type = PotionType.REGENERATION;
        } else if (id.contains("INVIS")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_INVISIBILITY;
            else type = PotionType.INVISIBILITY;
        } else if (id.contains("NIGHT_VIS")) {
            if (id.endsWith("_EXT")) type = PotionType.LONG_NIGHT_VISION;
            else type = PotionType.NIGHT_VISION;
        } else {
            type = PotionType.AWKWARD;
        }
        
        meta.setBasePotionType(type);
        item.setItemMeta(meta);
    }
    
    private String getPotionDisplayName(String id) {
        String base = "Potion de ";
        if (id.contains("SPEED")) base += "Vitesse";
        else if (id.contains("STRENGTH")) base += "Force";
        else if (id.contains("JUMP")) base += "Saut";
        else if (id.contains("FIRE_RES")) base += "Resistance au Feu";
        else if (id.contains("WATER_BREATH")) base += "Respiration Aquatique";
        else if (id.contains("REGEN")) base += "Regeneration";
        else if (id.contains("INVIS")) base += "Invisibilite";
        else if (id.contains("NIGHT_VIS")) base += "Vision Nocturne";
        
        if (id.endsWith("_2")) base += " II";
        if (id.endsWith("_EXT")) base += " (Allongee)";
        
        return "\u00A7d" + base;
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
