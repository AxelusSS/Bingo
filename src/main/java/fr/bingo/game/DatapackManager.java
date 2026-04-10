package fr.bingo.game;

import fr.bingo.BingoPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatapackManager {

    private final String namespace = "bingoclassique";

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

                String parent;
                if (col == 0) {
                    parent = namespace + ":root";
                } else {
                    int prevIndex = row * size + (col - 1);
                    parent = namespace + ":" + objectives.get(prevIndex).getId().toLowerCase();
                }

                createObjectiveAdvancement(dataFolder, obj, parent);
                filesCreated++;

                // Log debug pour les premières lignes
                if (row < 2) {
                    BingoPlugin.getInstance().getLogger().info("[Bingo] [" + row + "," + col + "] " + obj.getId().toLowerCase() + " parent=" + parent);
                }
            }
        }

        BingoPlugin.getInstance().getLogger().info("[Bingo] " + filesCreated + " fichiers créés dans " + dataFolder.getAbsolutePath());

        // Vérification : compter les fichiers réellement créés
        File[] createdFiles = dataFolder.listFiles();
        if (createdFiles != null) {
            BingoPlugin.getInstance().getLogger().info("[Bingo] Fichiers sur disque : " + createdFiles.length + " (attendu: " + (filesCreated + 1) + " avec root)");
        }

        // Forcer l'activation du datapack + reload
        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \"file/bingo_datapack\"");
            } catch (Exception ignored) {}
            
            Bukkit.reloadData();
            
            BingoPlugin.getInstance().getLogger().info("[Bingo] Datapack rechargé !");
            
            // Forcer les joueurs à re-recevoir les advancements en les kickant gentiment
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

    private void createObjectiveAdvancement(File dataFolder, BingoObjective obj, String parent) {
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
                "    \"description\": \"Trouve cet objet !\",\n" +
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

        saveFile(dataFolder, obj.getId().toLowerCase() + ".json", json);
    }

    private void saveFile(File dir, String fileName, String content) {
        try (FileWriter writer = new FileWriter(new File(dir, fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
