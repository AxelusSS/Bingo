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
 * Tous les items utilisent le trigger tick (auto-complète → visible et doré).
 * Les items trouvés passent en frame "challenge" (étoile ★).
 * Pas de bridges, pas de limite de profondeur.
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
        generateAdvancementsDatapack(grid, new HashSet<>());
    }

    private void generateAdvancementsDatapack(BingoGrid grid, Set<String> foundIds) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        writePackMcmeta(dataFolder.getParentFile().getParentFile().getParentFile());
        cleanDirectory(dataFolder);
        disableVanillaAdvancements(dataFolder.getParentFile().getParentFile());
        createRootAdvancement(dataFolder);

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                BingoObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);
                String parent = col == 0
                        ? namespace + ":root"
                        : namespace + ":" + getAdvancementId(row, col - 1);
                boolean found = foundIds.contains(obj.getId());

                createItemAdvancement(dataFolder, obj, advId, parent, found);
            }
        }

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§b§l[Bingo] §aGrille mise à jour ! Appuyez sur §e[L] §apour la voir.");
            }
        }, 10L);
    }

    /**
     * Rafraîchit la grille : items trouvés → frame challenge (★).
     * Debounce intégré pour éviter les reloads multiples.
     */
    public void refreshFoundItems(BingoGrid grid) {
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        Set<String> foundIds = new HashSet<>();
        for (BingoTeam team : tm.getTeams()) {
            foundIds.addAll(team.getUnlockedObjectives());
        }

        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                BingoObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);
                String parent = col == 0
                        ? namespace + ":root"
                        : namespace + ":" + getAdvancementId(row, col - 1);
                boolean found = foundIds.contains(obj.getId());

                createItemAdvancement(dataFolder, obj, advId, parent, found);
            }
        }

        if (pendingReloadTask != -1) {
            Bukkit.getScheduler().cancelTask(pendingReloadTask);
        }
        pendingReloadTask = Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            pendingReloadTask = -1;
        }, 10L).getTaskId();
    }

    // ── Fichiers JSON ──

    private void createItemAdvancement(File dir, BingoObjective obj, String advId, String parent, boolean found) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        String name = obj.getId().replace("_", " ");
        if (!name.isEmpty()) name = name.substring(0, 1).toUpperCase() + name.substring(1);

        String frame = found ? "challenge" : "task";
        String desc = found ? "\\u00a7a\\u2714 Trouv\\u00e9 !" : "Obtenir un(e) " + name;

        saveFile(dir, advId + ".json",
                "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"" + itemId + "\" },\n" +
                "    \"title\": \"" + name + "\",\n" +
                "    \"description\": \"" + desc + "\",\n" +
                "    \"frame\": \"" + frame + "\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:tick\" }\n" +
                "  }\n" +
                "}");
    }

    private void createRootAdvancement(File dir) {
        saveFile(dir, "root.json",
                "{\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:nether_star\" },\n" +
                "    \"title\": \"Bingo Classique\",\n" +
                "    \"description\": \"Appuyez sur [L] ou tapez /bg\",\n" +
                "    \"background\": \"minecraft:block/light_blue_concrete_powder\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
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
