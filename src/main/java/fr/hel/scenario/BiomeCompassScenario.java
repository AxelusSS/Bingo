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
import org.bukkit.generator.structure.Structure;

import java.util.*;

public class BiomeCompassScenario extends Scenario {

    private final String GUI_BIOME = "§8Traqueur de Biome";
    private final String GUI_STRUCT = "§8Traqueur de Structure";

    // ─── Biome entries ───
    private record BiomeEntry(Biome biome, Material icon, String displayName) {}

    private final List<BiomeEntry> overworldBiomes = Arrays.asList(
        // Forêts
        new BiomeEntry(Biome.FOREST, Material.OAK_LOG, "Forêt"),
        new BiomeEntry(Biome.BIRCH_FOREST, Material.BIRCH_LOG, "Forêt de Bouleaux"),
        new BiomeEntry(Biome.OLD_GROWTH_BIRCH_FOREST, Material.BIRCH_WOOD, "Vieille Forêt de Bouleaux"),
        new BiomeEntry(Biome.DARK_FOREST, Material.DARK_OAK_LOG, "Forêt Sombre"),
        new BiomeEntry(Biome.FLOWER_FOREST, Material.POPPY, "Forêt Fleurie"),
        new BiomeEntry(Biome.WINDSWEPT_FOREST, Material.OAK_SAPLING, "Forêt Balayée"),
        new BiomeEntry(Biome.PALE_GARDEN, Material.PALE_OAK_WOOD, "Pale Garden"),
        // Jungles
        new BiomeEntry(Biome.JUNGLE, Material.JUNGLE_LOG, "Jungle"),
        new BiomeEntry(Biome.BAMBOO_JUNGLE, Material.BAMBOO, "Jungle de Bambou"),
        new BiomeEntry(Biome.SPARSE_JUNGLE, Material.JUNGLE_SAPLING, "Jungle Clairsemée"),
        // Taïgas
        new BiomeEntry(Biome.TAIGA, Material.SPRUCE_LOG, "Taïga"),
        new BiomeEntry(Biome.SNOWY_TAIGA, Material.SPRUCE_WOOD, "Taïga Enneigée"),
        new BiomeEntry(Biome.OLD_GROWTH_PINE_TAIGA, Material.SPRUCE_LEAVES, "Taïga de Grands Pins"),
        new BiomeEntry(Biome.OLD_GROWTH_SPRUCE_TAIGA, Material.PODZOL, "Taïga de Grands Sapins"),
        // Plaines & prairies
        new BiomeEntry(Biome.PLAINS, Material.GRASS_BLOCK, "Plaines"),
        new BiomeEntry(Biome.SUNFLOWER_PLAINS, Material.SUNFLOWER, "Plaines de Tournesols"),
        new BiomeEntry(Biome.SNOWY_PLAINS, Material.SNOW_BLOCK, "Plaines Enneigées"),
        new BiomeEntry(Biome.MEADOW, Material.AZURE_BLUET, "Prairie"),
        // Savanes
        new BiomeEntry(Biome.SAVANNA, Material.ACACIA_LOG, "Savane"),
        new BiomeEntry(Biome.SAVANNA_PLATEAU, Material.ACACIA_WOOD, "Plateau de Savane"),
        new BiomeEntry(Biome.WINDSWEPT_SAVANNA, Material.ACACIA_SAPLING, "Savane Balayée"),
        // Déserts & Badlands
        new BiomeEntry(Biome.DESERT, Material.SAND, "Désert"),
        new BiomeEntry(Biome.BADLANDS, Material.RED_SAND, "Badlands"),
        new BiomeEntry(Biome.ERODED_BADLANDS, Material.TERRACOTTA, "Badlands Érodées"),
        new BiomeEntry(Biome.WOODED_BADLANDS, Material.ORANGE_TERRACOTTA, "Badlands Boisées"),
        // Marais
        new BiomeEntry(Biome.SWAMP, Material.LILY_PAD, "Marais"),
        new BiomeEntry(Biome.MANGROVE_SWAMP, Material.MANGROVE_LOG, "Marais de Mangrove"),
        // Montagnes & collines
        new BiomeEntry(Biome.WINDSWEPT_HILLS, Material.STONE, "Collines Balayées"),
        new BiomeEntry(Biome.WINDSWEPT_GRAVELLY_HILLS, Material.GRAVEL, "Collines de Gravier"),
        new BiomeEntry(Biome.STONY_PEAKS, Material.CALCITE, "Pics Rocheux"),
        new BiomeEntry(Biome.JAGGED_PEAKS, Material.PACKED_ICE, "Pics Acérés"),
        new BiomeEntry(Biome.FROZEN_PEAKS, Material.ICE, "Pics Gelés"),
        new BiomeEntry(Biome.SNOWY_SLOPES, Material.SNOWBALL, "Pentes Enneigées"),
        new BiomeEntry(Biome.GROVE, Material.POWDER_SNOW_BUCKET, "Bosquet"),
        // Champignons & spéciaux
        new BiomeEntry(Biome.MUSHROOM_FIELDS, Material.RED_MUSHROOM_BLOCK, "Champs de Champignons"),
        new BiomeEntry(Biome.CHERRY_GROVE, Material.CHERRY_LOG, "Forêt de Cerisiers"),
        new BiomeEntry(Biome.ICE_SPIKES, Material.BLUE_ICE, "Pics de Glace"),
        // Grottes
        new BiomeEntry(Biome.DRIPSTONE_CAVES, Material.DRIPSTONE_BLOCK, "Grottes de Stalactites"),
        new BiomeEntry(Biome.LUSH_CAVES, Material.MOSS_BLOCK, "Grottes Luxuriantes"),
        new BiomeEntry(Biome.DEEP_DARK, Material.SCULK, "Deep Dark"),
        new BiomeEntry(Biome.SULFUR_CAVES, Material.SULFUR_BRICKS, "Grottes de Sulfure"),
        // Côtes & rivières
        new BiomeEntry(Biome.BEACH, Material.SANDSTONE, "Plage"),
        new BiomeEntry(Biome.SNOWY_BEACH, Material.SNOW, "Plage Enneigée"),
        new BiomeEntry(Biome.STONY_SHORE, Material.COBBLESTONE, "Rivage Rocheux"),
        new BiomeEntry(Biome.RIVER, Material.WATER_BUCKET, "Rivière"),
        new BiomeEntry(Biome.FROZEN_RIVER, Material.FROSTED_ICE, "Rivière Gelée"),
        // Océans
        new BiomeEntry(Biome.OCEAN, Material.KELP, "Océan"),
        new BiomeEntry(Biome.DEEP_OCEAN, Material.PRISMARINE, "Océan Profond"),
        new BiomeEntry(Biome.WARM_OCEAN, Material.BRAIN_CORAL_BLOCK, "Océan Chaud"),
        new BiomeEntry(Biome.LUKEWARM_OCEAN, Material.TUBE_CORAL_BLOCK, "Océan Tiède"),
        new BiomeEntry(Biome.COLD_OCEAN, Material.PRISMARINE_BRICKS, "Océan Froid"),
        new BiomeEntry(Biome.FROZEN_OCEAN, Material.BLUE_ICE, "Océan Gelé")
    );

    private final List<BiomeEntry> netherBiomes = Arrays.asList(
        new BiomeEntry(Biome.NETHER_WASTES, Material.NETHERRACK, "Nether Wastes"),
        new BiomeEntry(Biome.CRIMSON_FOREST, Material.CRIMSON_STEM, "Forêt Cramoisie"),
        new BiomeEntry(Biome.WARPED_FOREST, Material.WARPED_STEM, "Forêt Biscornue"),
        new BiomeEntry(Biome.SOUL_SAND_VALLEY, Material.SOUL_SAND, "Vallée des Âmes"),
        new BiomeEntry(Biome.BASALT_DELTAS, Material.BASALT, "Deltas de Basalte")
    );

    // ─── Structure entries ───
    private record StructEntry(Structure structure, Material icon, String displayName) {}

    private final List<StructEntry> structures = Arrays.asList(
        new StructEntry(Structure.VILLAGE_PLAINS, Material.BELL, "Village (Plaines)"),
        new StructEntry(Structure.VILLAGE_DESERT, Material.DEAD_BUSH, "Village (Désert)"),
        new StructEntry(Structure.VILLAGE_SAVANNA, Material.HAY_BLOCK, "Village (Savane)"),
        new StructEntry(Structure.VILLAGE_TAIGA, Material.CAMPFIRE, "Village (Taïga)"),
        new StructEntry(Structure.VILLAGE_SNOWY, Material.BLUE_ICE, "Village (Neige)"),
        new StructEntry(Structure.DESERT_PYRAMID, Material.TNT, "Pyramide du Désert"),
        new StructEntry(Structure.JUNGLE_PYRAMID, Material.MOSSY_COBBLESTONE, "Temple de la Jungle"),
        new StructEntry(Structure.PILLAGER_OUTPOST, Material.CROSSBOW, "Avant-poste de Pillards"),
        new StructEntry(Structure.MANSION, Material.DARK_OAK_DOOR, "Manoir"),
        new StructEntry(Structure.MONUMENT, Material.SPONGE, "Monument Océanique"),
        new StructEntry(Structure.SWAMP_HUT, Material.CAULDRON, "Cabane de Sorcière"),
        new StructEntry(Structure.IGLOO, Material.SNOW_BLOCK, "Igloo"),
        new StructEntry(Structure.STRONGHOLD, Material.END_PORTAL_FRAME, "Forteresse de l'End"),
        new StructEntry(Structure.MINESHAFT, Material.RAIL, "Mine Abandonnée"),
        new StructEntry(Structure.BURIED_TREASURE, Material.HEART_OF_THE_SEA, "Trésor Enfoui"),
        new StructEntry(Structure.SHIPWRECK, Material.OAK_BOAT, "Épave"),
        new StructEntry(Structure.OCEAN_RUIN_WARM, Material.PRISMARINE_SHARD, "Ruines Océaniques"),
        new StructEntry(Structure.RUINED_PORTAL, Material.CRYING_OBSIDIAN, "Portail en Ruine"),
        new StructEntry(Structure.TRAIL_RUINS, Material.DECORATED_POT, "Ruines du Sentier"),
        new StructEntry(Structure.TRIAL_CHAMBERS, Material.TRIAL_KEY, "Chambres d'Épreuves"),
        new StructEntry(Structure.ANCIENT_CITY, Material.SCULK_SHRIEKER, "Cité Antique"),
        new StructEntry(Structure.FORTRESS, Material.NETHER_BRICKS, "Forteresse du Nether"),
        new StructEntry(Structure.BASTION_REMNANT, Material.POLISHED_BLACKSTONE_BRICKS, "Bastion"),
        new StructEntry(Structure.END_CITY, Material.PURPUR_BLOCK, "Cité de l'End")
    );

    private int currentBiomePage = 0;

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
                openBiomeGUI(p, 0);
            }
        }
    }

    private void openBiomeGUI(Player p, int page) {
        List<BiomeEntry> biomes = p.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER ? netherBiomes : overworldBiomes;
        int perPage = 45; // 5 rows
        int totalPages = (int) Math.ceil((double) biomes.size() / perPage);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, GUI_BIOME);

        int start = page * perPage;
        int end = Math.min(start + perPage, biomes.size());
        for (int i = start; i < end; i++) {
            BiomeEntry entry = biomes.get(i);
            ItemStack is = new ItemStack(entry.icon());
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName("§a" + entry.displayName());
            meta.setLore(List.of("§7" + entry.biome().name().replace("_", " ")));
            is.setItemMeta(meta);
            inv.setItem(i - start, is);
        }

        // Nav bar (row 6)
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta m = prev.getItemMeta();
            m.setDisplayName("§e← Page précédente");
            prev.setItemMeta(m);
            inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta m = next.getItemMeta();
            m.setDisplayName("§e→ Page suivante");
            next.setItemMeta(m);
            inv.setItem(53, next);
        }
        // Structure button
        ItemStack structBtn = new ItemStack(Material.SPYGLASS);
        ItemMeta sm = structBtn.getItemMeta();
        sm.setDisplayName("§d§lTraquer une Structure");
        sm.setLore(List.of("§7Cliquez pour chercher", "§7des structures à la place"));
        structBtn.setItemMeta(sm);
        inv.setItem(49, structBtn);

        p.openInventory(inv);
    }

    private void openStructureGUI(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_STRUCT);

        for (int i = 0; i < structures.size() && i < 45; i++) {
            StructEntry entry = structures.get(i);
            ItemStack is = new ItemStack(entry.icon());
            ItemMeta meta = is.getItemMeta();
            meta.setDisplayName("§d" + entry.displayName());
            is.setItemMeta(meta);
            inv.setItem(i, is);
        }

        // Back button
        ItemStack back = new ItemStack(Material.COMPASS);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName("§a§l← Retour aux Biomes");
        back.setItemMeta(bm);
        inv.setItem(49, back);

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player p)) return;
        String title = event.getView().getTitle();

        // ─── Biome GUI ───
        if (title.equals(GUI_BIOME)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            int slot = event.getRawSlot();

            // Nav: prev page
            if (slot == 45 && event.getCurrentItem().getType() == Material.ARROW) {
                currentBiomePage--;
                openBiomeGUI(p, currentBiomePage);
                return;
            }
            // Nav: next page
            if (slot == 53 && event.getCurrentItem().getType() == Material.ARROW) {
                currentBiomePage++;
                openBiomeGUI(p, currentBiomePage);
                return;
            }
            // Structure button
            if (slot == 49 && event.getCurrentItem().getType() == Material.SPYGLASS) {
                openStructureGUI(p);
                return;
            }

            // Biome click
            if (slot < 45 && event.getCurrentItem().getItemMeta() != null && event.getCurrentItem().getItemMeta().getLore() != null) {
                List<String> lore = event.getCurrentItem().getItemMeta().getLore();
                String biomeName = org.bukkit.ChatColor.stripColor(lore.get(0)).replace(" ", "_");
                try {
                    Biome targetBiome = Biome.valueOf(biomeName);
                    p.closeInventory();
                    p.sendMessage("§7Recherche du biome §e" + event.getCurrentItem().getItemMeta().getDisplayName() + " §7en cours...");

                    Bukkit.getScheduler().runTaskAsynchronously(HelPlugin.getInstance(), () -> {
                        org.bukkit.util.BiomeSearchResult result = p.getWorld().locateNearestBiome(p.getLocation(), 10000, targetBiome);
                        Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                            if (result != null) {
                                Location loc = result.getLocation();
                                p.setCompassTarget(loc);
                                int dist = (int) p.getLocation().distance(loc);
                                p.sendMessage("§aBiome trouvé à §e" + dist + " blocs §a! La boussole pointe vers votre destination.");
                            } else {
                                p.sendMessage("§cAucun biome trouvé dans un rayon de 10000 blocs.");
                            }
                        });
                    });
                } catch (Exception e) {
                    p.sendMessage("§cBiome invalide: " + biomeName);
                }
            }
        }

        // ─── Structure GUI ───
        if (title.equals(GUI_STRUCT)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            int slot = event.getRawSlot();

            // Back button
            if (slot == 49 && event.getCurrentItem().getType() == Material.COMPASS) {
                currentBiomePage = 0;
                openBiomeGUI(p, 0);
                return;
            }

            // Structure click
            if (slot < 45 && slot < structures.size()) {
                StructEntry entry = structures.get(slot);
                p.closeInventory();
                p.sendMessage("§7Recherche de §d" + entry.displayName() + " §7en cours...");

                Bukkit.getScheduler().runTaskAsynchronously(HelPlugin.getInstance(), () -> {
                    var result = p.getWorld().locateNearestStructure(p.getLocation(), entry.structure(), 10000, false);
                    Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                        if (result != null) {
                            Location loc = result.getLocation();
                            p.setCompassTarget(loc);
                            int dist = (int) p.getLocation().distance(loc);
                            p.sendMessage("§aStructure trouvée à §e" + dist + " blocs §a! La boussole pointe vers votre destination.");
                        } else {
                            p.sendMessage("§cAucune structure trouvée dans un rayon de 10000 blocs.");
                        }
                    });
                });
            }
        }
    }
}