package fr.bingo.game;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatapackManager {

    private final String namespace = "bingoclassique";

    /**
     * Retourne l'ID d'advancement pour un objectif à la position donnée dans la grille.
     * Format : "r{row}c{col}" pour un contrôle total de l'ordre alphabétique
     * (r0c1 < r1c0 → l'horizontal est toujours "premier enfant" → va à DROITE)
     */
    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    /**
     * Retrouve l'ID d'advancement à partir de l'index dans la liste d'objectifs.
     */
    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        int row = index / gridSize;
        int col = index % gridSize;
        return getAdvancementId(row, col);
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

        int filesCreated = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                BingoObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);

                // ── Chaînage en cascade ──
                // col0, row0 → parent = root
                // col0, rowN → parent = col0 de row(N-1)  (descend verticalement)
                // colN       → parent = col(N-1) même row  (va à droite)
                String parent;
                if (col == 0 && row == 0) {
                    parent = namespace + ":root";
                } else if (col == 0) {
                    // Cascade : enfant du col0 de la rangée précédente
                    parent = namespace + ":" + getAdvancementId(row - 1, 0);
                } else {
                    // Chaîne horizontale : enfant de l'item précédent dans la même rangée
                    parent = namespace + ":" + getAdvancementId(row, col - 1);
                }

                createObjectiveAdvancement(dataFolder, obj, advId, parent);
                filesCreated++;

                if (row < 2) {
                    BingoPlugin.getInstance().getLogger().info("[Bingo] [" + row + "," + col + "] " + advId + " (" + obj.getId().toLowerCase() + ") parent=" + parent);
                }
            }
        }

        BingoPlugin.getInstance().getLogger().info("[Bingo] " + filesCreated + " fichiers créés");

        // Forcer l'activation du datapack + reload
        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \"file/bingo_datapack\"");
            } catch (Exception ignored) {}

            Bukkit.reloadData();
            BingoPlugin.getInstance().getLogger().info("[Bingo] Datapack rechargé !");

            // Kick les joueurs pour forcer le reload des advancements côté client
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
                    p.kickPlayer("§b§lBingo Classique\n\n§eLa grille a été mise à jour !\n§fReconnectez-vous pour voir les changements.");
                }
            }, 20L);
        }, 10L);
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
                "    \"show_toast\": true,\n" +
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
