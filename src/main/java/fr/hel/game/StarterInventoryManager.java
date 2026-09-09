package fr.hel.game;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StarterInventoryManager {

    private final File file;
    private FileConfiguration config;
    private ItemStack[] contents;
    private ItemStack[] armor;
    
    private final List<UUID> playersInEditMode = new ArrayList<>();

    public StarterInventoryManager() {
        this.file = new File(HelPlugin.getInstance().getDataFolder(), "starter_inventory.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        List<ItemStack> list = (List<ItemStack>) config.getList("inventory");
        if (list != null) {
            contents = list.toArray(new ItemStack[0]);
        } else {
            contents = new ItemStack[36];
        }
        
        List<ItemStack> aList = (List<ItemStack>) config.getList("armor");
        if (aList != null) {
            armor = aList.toArray(new ItemStack[0]);
        } else {
            armor = new ItemStack[4];
        }
    }

    public void save(Player player) {
        this.contents = player.getInventory().getStorageContents().clone();
        this.armor = player.getInventory().getArmorContents().clone();
        
        config.set("inventory", contents);
        config.set("armor", armor);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void giveToPlayer(Player player) {
        player.getInventory().clear();
        if (contents != null) {
            for (int i = 0; i < contents.length && i < player.getInventory().getStorageContents().length; i++) {
                if (contents[i] != null) player.getInventory().setItem(i, contents[i].clone());
            }
        }
        if (armor != null) {
            player.getInventory().setArmorContents(armor.clone());
        }
    }

    public void enterEditMode(Player player) {
        playersInEditMode.add(player.getUniqueId());
        player.setGameMode(GameMode.CREATIVE);
        player.getInventory().clear();
        giveToPlayer(player);
        
        player.sendMessage("\u00A7e\u00A7m----------------------------------");
        player.sendMessage("\u00A7a\u00A7lMODE \u00C9DITION D'INVENTAIRE");
        player.sendMessage("\u00A77Faites votre inventaire puis tapez \u00A7c/finish");
        player.sendMessage("\u00A7e\u00A7m----------------------------------");
    }

    public void exitEditMode(Player player) {
        if (!playersInEditMode.contains(player.getUniqueId())) return;
        
        save(player);
        playersInEditMode.remove(player.getUniqueId());
        
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.sendMessage("\u00A7a[Hel] \u00A7fInventaire de d\u00E9part sauvegard\u00E9 !");
        
        // Redonner la banni\u00E8re et le compas
        HelPlugin.getInstance().getTeamManager().giveTeamBanner(player);
        if (player.hasPermission("bingo.admin")) {
            HelGame.giveAdminCompass(player);
        }
    }

    public boolean isEditing(Player player) {
        return playersInEditMode.contains(player.getUniqueId());
    }
}
