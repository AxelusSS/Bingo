import os
content = '''package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public class BiomeCompassScenario extends Scenario {

    private final String GUI_NAME = "\u00A78Traqueur de Biome";
    
    private final List<Biome> overworldBiomes = Arrays.asList(
        Biome.DESERT, Biome.JUNGLE, Biome.BADLANDS, Biome.DARK_FOREST, 
        Biome.SWAMP, Biome.MANGROVE_SWAMP, Biome.SAVANNA, Biome.SNOWY_TAIGA,
        Biome.MUSHROOM_FIELDS, Biome.CHERRY_GROVE, Biome.ICE_SPIKES, Biome.PALE_GARDEN
    );
    
    private final List<Biome> netherBiomes = Arrays.asList(
        Biome.CRIMSON_FOREST, Biome.WARPED_FOREST, Biome.SOUL_SAND_VALLEY, Biome.BASALT_DELTAS
    );

    public BiomeCompassScenario() {
        super("Biome compass", Material.COMPASS, "Traquez le biome de votre choix avec une boussole", false);
    }

    @Override
    public void onGameStart() {
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType() == Material.COMPASS) {
                openBiomeGUI(p);
            }
        }
    }

    private void openBiomeGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_NAME);
        
        List<Biome> biomes = p.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER ? netherBiomes : overworldBiomes;
        
        for (int i = 0; i < biomes.size(); i++) {
            Biome b = biomes.get(i);
            ItemStack is = new ItemStack(Material.MAP);
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName("\u00A7a" + b.name().replace("_", " "));
            is.setItemMeta(meta);
            inv.setItem(i, is);
        }
        
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (event.getView().getTitle().equals(GUI_NAME)) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.MAP) {
                Player p = (Player) event.getWhoClicked();
                String biomeName = org.bukkit.ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName()).replace(" ", "_");
                try {
                    Biome targetBiome = Biome.valueOf(biomeName);
                    p.closeInventory();
                    p.sendMessage("\u00A77Recherche du biome \u00A7e" + biomeName + "\u00A77 en cours...");
                    
                    Bukkit.getScheduler().runTaskAsynchronously(HelPlugin.getInstance(), () -> {
                        org.bukkit.generator.biome.BiomeSearchResult result = p.getWorld().locateNearestBiome(p.getLocation(), 10000, targetBiome);
                        Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                            if (result != null) {
                                Location loc = result.getLocation();
                                p.setCompassTarget(loc);
                                p.sendMessage("\u00A7aBiome trouv\u00E9 ! La boussole pointe vers le \u00A7e" + biomeName + "\u00A7a le plus proche !");
                            } else {
                                p.sendMessage("\u00A7cAucun biome \u00A7e" + biomeName + "\u00A7c trouv\u00E9 dans un rayon de 10000 blocs.");
                            }
                        });
                    });
                } catch (Exception e) {}
            }
        }
    }
}'''
with open('src/main/java/fr/hel/scenario/BiomeCompassScenario.java', 'w', encoding='utf-8') as f:
    f.write(content)
