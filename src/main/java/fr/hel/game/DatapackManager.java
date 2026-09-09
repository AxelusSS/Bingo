package fr.hel.game;

import fr.hel.HelPlugin;
import fr.hel.team.HelTeam;
import fr.hel.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * G\u00E8re la g\u00E9n\u00E9ration du datapack d'advancements pour la grille Hel.
 *
 * Structure :
 *   root (Nether Star, tick trigger, auto-complete)
 *    \u251C\u2500\u2500 r0c0 (impossible trigger \u2192 grantable per-player)
 *    \u251C\u2500\u2500 r0c1 (impossible trigger)
 *    ... 
 *    \u2514\u2500\u2500 r4c4 (impossible trigger)
 *
 * Tous les items sont des enfants directs du root pour un affichage en grille (x,y).
 * Les items utilisent le trigger "impossible" \u2192 ne s'auto-compl\u00E8te pas.
 * On accorde les advancements par joueur via le code Java.
 */
public class DatapackManager {

    private final String namespace = "bingoclassique";
    private static int pendingReloadTask = -1;

    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        return getAdvancementId(index / gridSize, index % gridSize);
    }


    // \u2500\u2500 G\u00E9n\u00E9ration \u2500\u2500

    public void generateAdvancementsDatapack(HelGrid grid) {
        generateAdvancementsDatapack(grid, HelMode.ITEMS);
    }

    public void generateAdvancementsDatapack(HelGrid grid, HelMode mode) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        writePackMcmeta(dataFolder.getParentFile().getParentFile().getParentFile());
        cleanDirectory(dataFolder);

        // Ne d\u00E9sactiver les advancements vanilla que en mode ITEMS pur
        if (mode == HelMode.ITEMS) {
            disableVanillaAdvancements(dataFolder.getParentFile().getParentFile());
        }

        createRootAdvancement(dataFolder);
        List<HelObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        // Cr\u00E9er les cha\u00EEnes horizontales : \u00C9toile -> C0 -> C1 -> C2 -> C3 -> C4 -> FIN
        for (int row = 0; row < size; row++) {
            String lastParent = namespace + ":root";

            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                HelObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);

                createItemAdvancement(dataFolder, obj, advId, lastParent, row, col);
                lastParent = namespace + ":" + advId; // Le suivant d\u00E9pend du pr\u00E9c\u00E9dent
            }

            // Ajouter le point technique de fin de ligne pour forcer la visibilit\u00E9
            createTechnicalRowEnd(dataFolder, row, size, lastParent);
        }

        Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("\u00A7b\u00A7l[Hel] \u00A7aGrille mise \u00E0 jour ! Appuyez sur \u00A7e[L] \u00A7apour la voir.");
            }
        }, 10L);
    }

    /**
     * Rafra\u00EEchit les advancements pour l'affichage global (compat).
     * Avec le nouveau syst\u00E8me per-player, cette m\u00E9thode n'est plus n\u00E9cessaire
     * pour changer les frames \u2014 on l'appelle uniquement pour le debounced reload.
     */
    public void refreshFoundItems(HelGrid grid) {
        // Pas de regeneration de JSON n\u00E9cessaire \u2014 le tracking est per-player
        // On garde un debounced reload au cas o\u00F9
        if (pendingReloadTask != -1) {
            Bukkit.getScheduler().cancelTask(pendingReloadTask);
        }
        pendingReloadTask = Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
            pendingReloadTask = -1;
        }, 10L).getTaskId();
    }

    // \u2500\u2500 JSON \u2500\u2500

    /**
     * Cr\u00E9e un item advancement avec trigger impossible.
     * Enfant direct d'un relay \u2192 visible d\u00E8s le chargement.
     * Marqu\u00E9 comme "found" seulement par code Java (per-player).
     */
    private void createItemAdvancement(File dir, HelObjective obj, String advId, String parent, int row, int col) {
        String iconId = "minecraft:" + obj.getDisplayMaterial().name().toLowerCase();
        String name = obj.getId().replace("_", " ").replace("/", " > ");
        if (!name.isEmpty()) name = name.substring(0, 1).toUpperCase() + name.substring(1);

        String desc = obj.isAchievement()
                ? "\\u00a7d[Achievement] " + name
                : "Obtenir " + name;

        saveFile(dir, advId + ".json",
                "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"" + iconId + "\" },\n" +
                "    \"title\": \"" + name + "\",\n" +
                "    \"description\": \"" + desc + "\",\n" +
                "    \"frame\": \"challenge\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false,\n" +
                "    \"x\": " + (double) (col * 1.5) + ",\n" +
                "    \"y\": " + (double) (row * 1.5) + "\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"found\": { \"trigger\": \"minecraft:impossible\" }\n" +
                "  }\n" +
                "}");

    }

    private void createRootAdvancement(File dir) {
        saveFile(dir, "root.json",
                "{\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:nether_star\" },\n" +
                "    \"title\": \"Hel Classique\",\n" +
                "    \"description\": \"Utilisez /bg pour voir votre progression\",\n" +
                "    \"background\": \"minecraft:textures/block/light_blue_concrete_powder.png\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false,\n" +
                "    \"x\": -1.5,\n" +
                "    \"y\": 3.0\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:tick\" }\n" +
                "  }\n" +
                "}");
    }

    /**
     * Cr\u00E9e un point technique "r\u00E9ussi" \u00E0 la fin de chaque ligne.
     * Force la visibilit\u00E9 de toute la ligne parente.
     */
    private void createTechnicalRowEnd(File dir, int row, int size, String parent) {
        saveFile(dir, "row_end_" + row + ".json",
                "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:tick\" }\n" +
                "  }\n" +
                "}");
    }

    private void disableVanillaAdvancements(File dataRoot) {
        String json = "{ \"criteria\": { \"x\": { \"trigger\": \"minecraft:impossible\" } } }";
        String[][] tabs = {
            {"minecraft", "story"}, {"minecraft", "adventure"},
            {"minecraft", "husbandry"}, {"minecraft", "nether"}, {"minecraft", "end"}
        };
        for (String[] tab : tabs) {
            File dir = new File(dataRoot, tab[0] + "/advancement/" + tab[1]);
            if (!dir.exists()) dir.mkdirs();
            saveFile(dir, "root.json", json);
        }
    }

    // \u2500\u2500 Utilitaires \u2500\u2500

    private File getDataFolder() {
        File f = new File(Bukkit.getWorlds().get(0).getWorldFolder(),
                "datapacks/bingo_datapack/data/" + namespace + "/advancement");
        if (!f.exists() && !f.mkdirs()) {
            HelPlugin.getInstance().getLogger().severe("Impossible de cr\u00E9er le dossier datapack !");
            return null;
        }
        return f;
    }

    private void writePackMcmeta(File dir) {
        saveFile(dir, "pack.mcmeta",
                "{ \"pack\": { \"pack_format\": 71, \"supported_formats\": [48, 71], \"description\": \"Hel\" } }");
    }

    private void enableAndReload() {
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \"file/bingo_datapack\""); }
        catch (Exception ignored) {}
        Bukkit.reloadData();
    }

    private void cleanDirectory(File folder) {
        if (!folder.exists() || !folder.isDirectory()) return;
        File[] files = folder.listFiles();
        if (files != null) for (File f : files) { if (f.isDirectory()) cleanDirectory(f); f.delete(); }
    }

    private void saveFile(File dir, String name, String content) {
        try (FileWriter w = new FileWriter(new File(dir, name))) { w.write(content); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
