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

        // Nettoyer les anciens advancements (sinon ils restent superposés)
        cleanDirectory(dataFolder);

        // Créer l'advancement Root
        createRootAdvancement(dataFolder);

        // Créer tous les objectifs (grille NxN)
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();
        
        for (int i = 0; i < objectives.size(); i++) {
            BingoObjective obj = objectives.get(i);
            // Calculer (x, y) dans la grille
            int col = i % size;
            int row = i / size;
            
            // Éspacement visuel optimal dans le menu
            float displayX = col * 1.5f;
            float displayY = row * 1.5f;
            
            createObjectiveAdvancement(dataFolder, obj, "bingoclassique:root", displayX, displayY);
        }

        BingoPlugin.getInstance().getLogger().info("Datapack généré avec succès ! Rechargement du jeu...");
        Bukkit.reloadData(); // Entraîne un léger freeze mais injecte le datapack dans le serveur
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
                "    \"icon\": { \"id\": \"minecraft:paper\" },\n" +
                "    \"title\": \"Bingo Classique\",\n" +
                "    \"description\": \"La partie a commencé !\",\n" +
                "    \"background\": \"minecraft:textures/gui/advancements/backgrounds/end.png\",\n" +
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

    private void createObjectiveAdvancement(File dataFolder, BingoObjective obj, String parent, float x, float y) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        
        String json = "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"" + itemId + "\" },\n" +
                "    \"title\": \"Obtenir un(e) " + obj.getId().replace("_", " ") + "\",\n" +
                "    \"description\": \"Trouve cet objet !\",\n" +
                "    \"frame\": \"task\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false,\n" +
                "    \"x\": " + x + ",\n" +
                "    \"y\": " + y + "\n" +
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
