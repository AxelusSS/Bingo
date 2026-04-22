package fr.bingo.game;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gère la génération du datapack d'advancements pour la grille Bingo.
 *
 * Structure :
 *   root (Nether Star, tick trigger, auto-complete)
 *    ├── r0c0 (impossible trigger → grantable per-player)
 *    ├── r0c1 (impossible trigger)
 *    ... 
 *    └── r4c4 (impossible trigger)
 *
 * Tous les items sont des enfants directs du root pour un affichage en grille (x,y).
 * Les items utilisent le trigger "impossible" → ne s'auto-complète pas.
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


    // ── Génération ──

    public void generateAdvancementsDatapack(BingoGrid grid) {
        generateAdvancementsDatapack(grid, BingoMode.ITEMS);
    }

    public void generateAdvancementsDatapack(BingoGrid grid, BingoMode mode) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        writePackMcmeta(dataFolder.getParentFile().getParentFile().getParentFile());
        cleanDirectory(dataFolder);

        // Ne désactiver les advancements vanilla que en mode ITEMS pur
        if (mode == BingoMode.ITEMS) {
            disableVanillaAdvancements(dataFolder.getParentFile().getParentFile());
        }

        createRootAdvancement(dataFolder);
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        // Créer les chaînes horizontales : Étoile -> C0 -> C1 -> C2 -> C3 -> C4 -> FIN
        for (int row = 0; row < size; row++) {
            String lastParent = namespace + ":root";

            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                BingoObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);

                createItemAdvancement(dataFolder, obj, advId, lastParent, row, col);
                lastParent = namespace + ":" + advId; // Le suivant dépend du précédent
            }

            // Ajouter le point technique de fin de ligne pour forcer la visibilité
            createTechnicalRowEnd(dataFolder, row, size, lastParent);
        }

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§b§l[Bingo] §aGrille mise à jour ! Appuyez sur §e[L] §apour la voir.");
            }
        }, 10L);
    }

    /**
     * Rafraîchit les advancements pour l'affichage global (compat).
     * Avec le nouveau système per-player, cette méthode n'est plus nécessaire
     * pour changer les frames — on l'appelle uniquement pour le debounced reload.
     */
    public void refreshFoundItems(BingoGrid grid) {
        // Pas de regeneration de JSON nécessaire — le tracking est per-player
        // On garde un debounced reload au cas où
        if (pendingReloadTask != -1) {
            Bukkit.getScheduler().cancelTask(pendingReloadTask);
        }
        pendingReloadTask = Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            pendingReloadTask = -1;
        }, 10L).getTaskId();
    }

    // ── JSON ──

    /**
     * Crée un item advancement avec trigger impossible.
     * Enfant direct d'un relay → visible dès le chargement.
     * Marqué comme "found" seulement par code Java (per-player).
     */
    private void createItemAdvancement(File dir, BingoObjective obj, String advId, String parent, int row, int col) {
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
                "    \"title\": \"Bingo Classique\",\n" +
                "    \"description\": \"Utilisez /bg pour voir votre progression\",\n" +
                "    \"background\": \"minecraft:block/light_blue_concrete_powder\",\n" +
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
     * Crée un point technique "réussi" à la fin de chaque ligne.
     * Force la visibilité de toute la ligne parente.
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

    // ── Utilitaires ──

    private File getDataFolder() {
        File f = new File(Bukkit.getWorlds().get(0).getWorldFolder(),
                "datapacks/bingo_datapack/data/" + namespace + "/advancement");
        if (!f.exists() && !f.mkdirs()) {
            BingoPlugin.getInstance().getLogger().severe("Impossible de créer le dossier datapack !");
            return null;
        }
        return f;
    }

    private void writePackMcmeta(File dir) {
        saveFile(dir, "pack.mcmeta",
                "{ \"pack\": { \"pack_format\": 71, \"supported_formats\": [48, 71], \"description\": \"Bingo\" } }");
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
