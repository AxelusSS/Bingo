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
            writer.write("{ \"pack\": { \"pack_format\": 48, \"description\": \"Bingo Advancements\" } }");
        } catch (IOException e) {
            e.printStackTrace();
        }

        cleanDirectory(dataFolder);
        createRootAdvancement(dataFolder);

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        // Stratégie de chaînage pour obtenir une vraie grille NxN :
        // - L'item (row, 0) est enfant de root → s'empile verticalement sous root
        // - L'item (row, col) est enfant de (row, col-1) → s'étend horizontalement
        // Résultat visuel MC :
        //   [root] [r0c0] [r0c1] [r0c2] ...
        //          [r1c0] [r1c1] [r1c2] ...
        //          [r2c0] [r2c1] [r2c2] ...

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
            }
        }

        BingoPlugin.getInstance().getLogger().info("Datapack généré ! Taille: " + size + "x" + size);
        Bukkit.reloadData();
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
        // On utilise un fond vanilla valide (stone = texture qui existe à coup sûr)
        String json = "{\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:nether_star\" },\n" +
                "    \"title\": \"Bingo Classique\",\n" +
                "    \"description\": \"Appuyez sur [L] pour voir la grille !\",\n" +
                "    \"background\": \"minecraft:textures/gui/advancements/backgrounds/adventure.png\",\n" +
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
