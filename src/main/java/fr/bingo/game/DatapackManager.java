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

        // Ecrire le pack.mcmeta
        try (FileWriter writer = new FileWriter(new File(datapackFolder, "pack.mcmeta"))) {
            writer.write("{ \"pack\": { \"pack_format\": 48, \"description\": \"Bingo Advancements\" } }");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Nettoyer les anciens advancements
        cleanDirectory(dataFolder);

        // Créer l'advancement Root (point d'ancrage invisible)
        createRootAdvancement(dataFolder);

        // Créer tous les objectifs en grille NxN
        // Stratégie : chaque item de la colonne 0 est enfant de root (= s'empilent verticalement)
        // chaque item suivant dans la ligne est enfant du précédent (= va vers la droite)
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;
                
                BingoObjective obj = objectives.get(index);
                
                String parent;
                if (col == 0) {
                    // Premier de la ligne => enfant direct de root => positionné verticalement
                    parent = namespace + ":root";
                } else {
                    // Enfant du précédent dans la ligne => positionné à droite
                    int prevIndex = row * size + (col - 1);
                    parent = namespace + ":" + objectives.get(prevIndex).getId().toLowerCase();
                }
                
                createObjectiveAdvancement(dataFolder, obj, parent);
            }
        }

        BingoPlugin.getInstance().getLogger().info("Datapack généré avec succès ! Rechargement du jeu...");
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
        String json = "{\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:nether_star\" },\n" +
                "    \"title\": \"Bingo Classique\",\n" +
                "    \"description\": \"Appuyez sur [L] pour voir la grille !\",\n" +
                "    \"background\": \"minecraft:textures/block/light_blue_concrete_powder.png\",\n" +
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
        // Première lettre majuscule
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
