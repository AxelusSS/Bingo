package fr.bingo.game;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatapackManager {

    private final String namespace = "bingoclassique";

    /**
     * ID positionnel pour un objectif à la position donnée.
     */
    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        return getAdvancementId(index / gridSize, index % gridSize);
    }

    public void generateAdvancementsDatapack(BingoGrid grid) {
        File worldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
        File datapackFolder = new File(worldFolder, "datapacks/bingo_datapack");
        File dataFolder = new File(datapackFolder, "data/" + namespace + "/advancement");

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            BingoPlugin.getInstance().getLogger().severe("Impossible de creer le dossier datapack !");
            return;
        }

        try (FileWriter writer = new FileWriter(new File(datapackFolder, "pack.mcmeta"))) {
            writer.write("{ \"pack\": { \"pack_format\": 71, \"supported_formats\": [48, 71], \"description\": \"Bingo Advancements\" } }");
        } catch (IOException e) {
            e.printStackTrace();
        }

        cleanDirectory(dataFolder);
        createRootAdvancement(dataFolder);

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        BingoPlugin.getInstance().getLogger().info("[Bingo] Génération de " + objectives.size() + " objectifs pour grille " + size + "x" + size);

        // Structure : tous les col0 sont enfants de root (→ grille verticale)
        //             chaque colN est enfant de col(N-1) (→ chaîne horizontale)
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                BingoObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);

                String parent;
                if (col == 0) {
                    parent = namespace + ":root";
                } else {
                    parent = namespace + ":" + getAdvancementId(row, col - 1);
                }

                createObjectiveAdvancement(dataFolder, obj, advId, parent);
            }
        }

        BingoPlugin.getInstance().getLogger().info("[Bingo] " + objectives.size() + " advancements créés.");

        // Forcer le rechargement
        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \"file/bingo_datapack\"");
            } catch (Exception ignored) {}

            Bukkit.reloadData();
            BingoPlugin.getInstance().getLogger().info("[Bingo] Datapack rechargé.");

            // Rendre TOUS les advancements visibles pour les joueurs en ligne
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    discoverAllAdvancements(p, grid);
                }
                BingoPlugin.getInstance().getLogger().info("[Bingo] Advancements rendus visibles pour tous les joueurs.");

                // Kick pour forcer le rendu client
                Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.kickPlayer("§b§lBingo Classique\n\n§eLa grille a été mise à jour !\n§fReconnectez-vous pour voir la grille.");
                    }
                }, 10L);
            }, 20L);
        }, 10L);
    }

    /**
     * Donne puis retire tous les advancements à un joueur pour les rendre "découverts"
     * (visibles dans l'onglet même si non obtenus).
     * C'est LE trick pour que toute la grille soit visible d'un coup.
     */
    public void discoverAllAdvancements(Player player, BingoGrid grid) {
        int size = grid.getSize();
        List<BingoObjective> objectives = grid.getObjectives();

        // Phase 1 : GRANT tous les advancements (les rend "discovered")
        for (int i = 0; i < objectives.size(); i++) {
            String advId = getAdvancementIdFromIndex(i, size);
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(namespace, advId);
            org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null) {
                org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(adv);
                for (String criteria : adv.getCriteria()) {
                    progress.awardCriteria(criteria);
                }
            }
        }

        // Phase 2 : REVOKE tous les advancements (remet à "non obtenu" mais reste visible)
        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            for (int i = 0; i < objectives.size(); i++) {
                String advId = getAdvancementIdFromIndex(i, size);
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(namespace, advId);
                org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
                if (adv != null) {
                    org.bukkit.advancement.AdvancementProgress progress = player.getAdvancementProgress(adv);
                    for (String criteria : progress.getAwardedCriteria()) {
                        progress.revokeCriteria(criteria);
                    }
                }
            }
        }, 2L);
    }

    private void cleanDirectory(File folder) {
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) cleanDirectory(f);
                f.delete();
            }
        }
    }

    private void createRootAdvancement(File dataFolder) {
        String json = "{\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:nether_star\" },\n" +
                "    \"title\": \"Bingo Classique\",\n" +
                "    \"description\": \"Appuyez sur [L] pour voir la grille !\",\n" +
                "    \"background\": \"minecraft:block/light_blue_concrete_powder\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": {\n" +
                "      \"trigger\": \"minecraft:tick\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        saveFile(dataFolder, "root.json", json);
    }

    private void createObjectiveAdvancement(File dataFolder, BingoObjective obj, String advId, String parent) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        String displayName = obj.getId().replace("_", " ");
        if (!displayName.isEmpty()) {
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        }

        String json = "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"" + itemId + "\" },\n" +
                "    \"title\": \"" + displayName + "\",\n" +
                "    \"description\": \"Obtenir un(e) " + displayName + "\",\n" +
                "    \"frame\": \"task\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"impossible\": {\n" +
                "      \"trigger\": \"minecraft:impossible\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        saveFile(dataFolder, advId + ".json", json);
    }

    private void saveFile(File dir, String fileName, String content) {
        try (FileWriter writer = new FileWriter(new File(dir, fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
