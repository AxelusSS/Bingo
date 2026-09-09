package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.game.HelGame;
import fr.hel.game.HelGame.BiomeSize;
import fr.hel.game.HelGame.GenerationType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WorldConfigGUI implements InventoryHolder {

    private final Inventory inventory;

    public WorldConfigGUI() {
        this.inventory = Bukkit.createInventory(this, 54, "\u00A76\u00A7l\u2699 Gestion de la G\u00E9n\u00E9ration");
        populate();
    }

    private void populate() {
        HelGame game = HelPlugin.getInstance().getHelGame();
        inventory.clear();

        // Fond stylis\u00E9
        ItemStack gray = createItem(Material.GRAY_STAINED_GLASS_PANE, "\u00A7r", null);
        ItemStack orange = createItem(Material.ORANGE_STAINED_GLASS_PANE, "\u00A7r", null);
        
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                inventory.setItem(i, orange);
            } else {
                inventory.setItem(i, gray);
            }
        }

        // --- G\u00C9N\u00C9RATION (Ligne 1) ---
        
        // Type de g\u00E9n\u00E9ration (Slot 11)
        GenerationType currentGen = game.getGenerationType();
        List<String> genLore = new ArrayList<>();
        genLore.add("\u00A77D\u00E9finit le moteur de g\u00E9n\u00E9ration.");
        genLore.add("");
        for (GenerationType type : GenerationType.values()) {
            boolean active = (type == currentGen);
            genLore.add((active ? "\u00A7a\u00A7l\u25B8 " : "\u00A78  ") + (active ? "\u00A7a\u00A7l" : "\u00A77") + type.getDisplayName());
        }
        genLore.add("");
        genLore.add("\u00A7e\u25BA Clic pour d\u00E9filer");
        inventory.setItem(11, createItem(Material.COMPASS, "\u00A7e\u00A7lVersion G\u00E9n\u00E9ration", genLore));

        // Taille des biomes (Slot 13)
        BiomeSize size = game.getBiomeSize();
        inventory.setItem(13, createItem(Material.GRASS_BLOCK, "\u00A7e\u00A7lTaille des Biomes",
                List.of("\u00A77Valeur actuelle : \u00A7b" + size.name(),
                        "",
                        (size == BiomeSize.SMALL ? "\u00A7b\u25B8 \u00A7lPetit (2)" : "\u00A77  Petit (2)"),
                        (size == BiomeSize.MEDIUM ? "\u00A7b\u25B8 \u00A7lMoyen (4)" : "\u00A77  Moyen (4)"),
                        (size == BiomeSize.LARGE ? "\u00A7b\u25B8 \u00A7lLarge (6)" : "\u00A77  Large (6)"),
                        "",
                        "\u00A7e\u25BA Clic pour changer")));

        // Seed (Slot 15)
        long seed = game.getWorldSeed();
        String seedStr = seed == -1 ? "Al\u00E9atoire" : String.valueOf(seed);
        inventory.setItem(15, createItem(Material.WHEAT_SEEDS, "\u00A7e\u00A7lSeed du Monde",
                List.of("\u00A77Valeur : \u00A7b" + seedStr,
                        "",
                        "\u00A7e\u25BA Clic gauche : Entrer une seed",
                        "\u00A7e\u25BA Clic droit : Al\u00E9atoire")));

        // --- FILTRES (Ligne 2) ---

        // --- STRUCTURES (Ligne 3) ---

        // --- STRUCTURES (Ligne 3) ---

        // Densit\u00E9 Mini Nether (Slot 29)
        int density = game.getMiniNetherDensity();
        inventory.setItem(29, createItem(Material.NETHERRACK, "\u00A7c\u00A7lDensit\u00E9 Mini-Nether",
                List.of("\u00A77Nombre de structures Nether.",
                        "\u00A77Quantit\u00E9 : \u00A7b" + density,
                        "",
                        "\u00A7e\u25BA Clic Gauche : \u00A7a+1",
                        "\u00A7e\u25BA Clic Droit : \u00A7c-1")));

        // --- ACTIONS (Bas) ---

        // RESET MAP (Slot 40)
        inventory.setItem(40, createItem(Material.TNT, "\u00A7c\u00A7l\u1F4A5 R\u00C9INITIALISER LA MAP \u1F4A5",
                List.of("\u00A77Relance le serveur et g\u00E9n\u00E8re une",
                        "\u00A77nouvelle map avec ces param\u00E8tres.",
                        "",
                        "\u00A7c\u26A0 ACTION IRR\u00C9VERSIBLE \u26A0",
                        "",
                        "\u00A7e\u25BA Clic pour lancer")));

        // Retour (Slot 49)
        inventory.setItem(49, createItem(Material.ARROW, "\u00A7cRetour", List.of("\u00A77Retour au menu principal")));
    }

    public void handleClick(Player player, int slot, boolean isRightClick) {
        HelGame game = HelPlugin.getInstance().getHelGame();

        switch (slot) {
            case 11 -> { // Generation Type
                GenerationType[] types = GenerationType.values();
                int next = (game.getGenerationType().ordinal() + 1) % types.length;
                game.setGenerationType(types[next]);
                
                // Ajustements automatiques selon le mode
                if (game.getGenerationType() == GenerationType.V_1_8_UHC_RUN) {
                    game.getDisabledBiomes().add("oceans");
                    game.getDisabledBiomes().add("mountains");
                    game.setBiomeSize(BiomeSize.SMALL);
                    game.setMiniNetherDensity(15);
                    HelPlugin.getInstance().getScenarioManager().getScenario(fr.hel.scenario.UHCRunScenario.class).setEnabled(true);
                }
                
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
                refresh(player);
            }
            case 13 -> { // Biome Size
                BiomeSize[] vals = BiomeSize.values();
                int next = (game.getBiomeSize().ordinal() + 1) % vals.length;
                game.setBiomeSize(vals[next]);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 15 -> { // Seed
                if (isRightClick) {
                    game.setWorldSeed(-1);
                    player.sendMessage("\u00A7a[HEL] \u00A7fSeed remise en al\u00E9atoire.");
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                    refresh(player);
                } else {
                    HelPlugin.getInstance().getHelListener().addSeedInputPlayer(player);
                    player.closeInventory();
                    player.sendMessage("\u00A7a[HEL] \u00A7fVeuillez \u00E9crire la \u00A7eSeed \u00A7fdans le chat.");
                }
            }
            case 29 -> { // Density
                int d = game.getMiniNetherDensity();
                if (isRightClick) d = Math.max(0, d - 1);
                else d = Math.min(50, d + 1);
                game.setMiniNetherDensity(d);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
                refresh(player);
            }
            case 40 -> { // Reset
                game.prepareWorldReset(player);
                player.closeInventory();
            }
            case 49 -> { // Retour
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
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public Inventory getInventory() { return inventory; }
}
