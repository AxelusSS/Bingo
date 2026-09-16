package fr.hel.scenario;

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

    private final String GUI_NAME = "§8Traqueur de Biome";
    
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
            
            Material iconMat = Material.PAPER;
            String bName = b.name();
            if (bName.contains("FOREST")) iconMat = Material.OAK_WOOD;
            if (bName.contains("BIRCH")) iconMat = Material.BIRCH_WOOD;
            if (bName.contains("DARK_OAK")) iconMat = Material.DARK_OAK_WOOD;
            if (bName.contains("JUNGLE")) iconMat = Material.JUNGLE_WOOD;
            if (bName.contains("SPRUCE") || bName.contains("TAIGA")) iconMat = Material.SPRUCE_WOOD;
            if (bName.contains("ACACIA") || bName.contains("SAVANNA")) iconMat = Material.ACACIA_WOOD;
            if (bName.contains("MANGROVE")) iconMat = Material.MANGROVE_WOOD;
            if (bName.contains("CHERRY")) iconMat = Material.CHERRY_WOOD;
            if (bName.contains("PALE")) iconMat = Material.PALE_OAK_WOOD;
            if (bName.contains("DESERT")) iconMat = Material.SAND;
            if (bName.contains("BADLANDS")) iconMat = Material.RED_SAND;
            if (bName.contains("SNOW") || bName.contains("ICE")) iconMat = Material.SNOW_BLOCK;
            if (bName.contains("MUSHROOM")) iconMat = Material.RED_MUSHROOM_BLOCK;
            if (bName.contains("SWAMP")) iconMat = Material.SLIME_BLOCK;
            if (bName.contains("OCEAN") || bName.contains("RIVER")) iconMat = Material.WATER_BUCKET;
            if (bName.contains("PLAINS")) iconMat = Material.GRASS_BLOCK;
            
            // Nether biomes
            if (bName.contains("CRIMSON")) iconMat = Material.CRIMSON_STEM;
            if (bName.contains("WARPED")) iconMat = Material.WARPED_STEM;
            if (bName.contains("SOUL")) iconMat = Material.SOUL_SAND;
            if (bName.contains("BASALT")) iconMat = Material.BASALT;
            if (bName.contains("WASTES")) iconMat = Material.NETHERRACK;

            ItemStack is = new ItemStack(iconMat);

            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName("§a" + b.name().replace("_", " "));
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
                    p.sendMessage("§7Recherche du biome §e" + biomeName + "§7 en cours...");
                    
                    Bukkit.getScheduler().runTaskAsynchronously(HelPlugin.getInstance(), () -> {
                        org.bukkit.util.BiomeSearchResult result = p.getWorld().locateNearestBiome(p.getLocation(), 10000, targetBiome);
                        Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                            if (result != null) {
                                Location loc = result.getLocation();
                                p.setCompassTarget(loc);
                                p.sendMessage("§aBiome trouvé ! La boussole pointe vers le §e" + biomeName + "§a le plus proche !");
                            } else {
                                p.sendMessage("§cAucun biome §e" + biomeName + "§c trouvé dans un rayon de 10000 blocs.");
                            }
                        });
                    });
                } catch (Exception e) {}
            }
        }
    }
}