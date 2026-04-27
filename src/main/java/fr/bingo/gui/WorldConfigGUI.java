package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.game.BingoGame;
import fr.bingo.game.BingoGame.BiomeSize;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class WorldConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public WorldConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "§6§l⚙ Configuration Monde");
        populate();
    }

    private void populate() {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();
        inventory.clear();

        // Fond
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName("§r");
        bg.setItemMeta(bgMeta);
        for (int i = 0; i < 27; i++) inventory.setItem(i, bg);

        // Slot 10 : Taille des biomes
        BiomeSize size = game.getBiomeSize();
        inventory.setItem(10, createItem(Material.GRASS_BLOCK, "§e§lTaille des Biomes : §b" + size.name(),
                List.of("§7Clic pour changer",
                        "",
                        (size == BiomeSize.SMALL ? "§b▸ " : "§7  ") + "Petit (2)",
                        (size == BiomeSize.MEDIUM ? "§b▸ " : "§7  ") + "Moyen (4)",
                        (size == BiomeSize.LARGE ? "§b▸ " : "§7  ") + "Large (6)")));

        // Slot 12 : Biomes désactivés (Montagnes/Océans)
        boolean noMountains = game.getDisabledBiomes().contains("mountains");
        boolean noOceans = game.getDisabledBiomes().contains("oceans");
        inventory.setItem(12, createItem(Material.WATER_BUCKET, "§e§lBiomes supprimés",
                List.of("§7Clic gauche : Montagnes " + (noMountains ? "§c[OFF]" : "§a[ON]"),
                        "§7Clic droit : Océans " + (noOceans ? "§c[OFF]" : "§a[ON]"),
                        "",
                        "§7(Note: Nécessite un reset de map)")));

        // Slot 14 : Seed
        long seed = game.getWorldSeed();
        String seedStr = seed == -1 ? "Aléatoire" : String.valueOf(seed);
        inventory.setItem(14, createItem(Material.WHEAT_SEEDS, "§e§lSeed : §b" + seedStr,
                List.of("§7Définit la seed du prochain monde",
                        "",
                        "§e► Clic pour réinitialiser (Aléatoire)")));

        // Slot 16 : Presets de génération
        inventory.setItem(16, createItem(Material.ENCHANTED_BOOK, "§a§l✦ PRESETS GÉNÉRATION",
                List.of("§7Charger une config de monde type",
                        "",
                        "§b▸ UHC §7(Biomes 1.8)",
                        "§b▸ UHC Run §7(Nether struct)",
                        "§b▸ Bingo Vanilla §7(1.21.1)")));

        // Slot 22 : Retour
        inventory.setItem(22, createItem(Material.ARROW, "§c§l← Retour", List.of("§7Retour au menu admin")));
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        BingoGame game = BingoPlugin.getInstance().getBingoGame();

        switch (slot) {
            case 10 -> { // Biome Size
                BiomeSize[] vals = BiomeSize.values();
                int next = (game.getBiomeSize().ordinal() + 1) % vals.length;
                game.setBiomeSize(vals[next]);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 12 -> { // Disabled Biomes
                if (isRightClick) {
                    if (game.getDisabledBiomes().contains("oceans")) game.getDisabledBiomes().remove("oceans");
                    else game.getDisabledBiomes().add("oceans");
                } else {
                    if (game.getDisabledBiomes().contains("mountains")) game.getDisabledBiomes().remove("mountains");
                    else game.getDisabledBiomes().add("mountains");
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 14 -> { // Seed
                game.setWorldSeed(-1);
                player.sendMessage("§a[Bingo] §fSeed remise en aléatoire.");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 16 -> { // Presets
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
                if (isRightClick) {
                    // Bingo Vanilla 1.21
                    game.getDisabledBiomes().clear();
                    game.setBiomeSize(BingoGame.BiomeSize.MEDIUM);
                    BingoPlugin.getInstance().getScenarioManager().getScenario(fr.bingo.scenario.BingoScenario.class).setEnabled(true);
                    player.sendMessage("§a[Preset] §fBingo Vanilla 1.21 chargé !");
                } else if (player.isSneaking()) {
                    // UHC Run
                    game.getDisabledBiomes().add("mountains");
                    game.getDisabledBiomes().add("oceans");
                    game.setBiomeSize(BingoGame.BiomeSize.SMALL);
                    BingoPlugin.getInstance().getScenarioManager().getScenario(fr.bingo.scenario.BingoScenario.class).setEnabled(false);
                    BingoPlugin.getInstance().getScenarioManager().getScenario(fr.bingo.scenario.MiniNetherScenario.class).setEnabled(true);
                    BingoPlugin.getInstance().getScenarioManager().getScenario(fr.bingo.scenario.CutCleanScenario.class).setEnabled(true);
                    player.sendMessage("§a[Preset] §fUHC Run chargé !");
                } else {
                    // UHC 1.8 biomes (Simulated)
                    game.getDisabledBiomes().add("oceans");
                    game.setBiomeSize(BingoGame.BiomeSize.LARGE);
                    BingoPlugin.getInstance().getScenarioManager().getScenario(fr.bingo.scenario.BingoScenario.class).setEnabled(false);
                    player.sendMessage("§a[Preset] §fUHC (Simu 1.8) chargé !");
                }
                refresh(player);
            }
            case 22 -> { // Retour
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                player.openInventory(new AdminConfigGUI().getInventory());
            }
        }
    }

    private void refresh(Player player) {
        populate();
        player.openInventory(inventory);
    }

    private ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
