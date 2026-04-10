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

public class DatapackManager {

    private final String namespace = "bingoclassique";

    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        return getAdvancementId(index / gridSize, index % gridSize);
    }

    /**
     * Génère le datapack. Tous les items utilisent tick (auto-complète → DONE → visible).
     * Les items trouvés utilisent frame "challenge" (étoile), les non-trouvés "task" (carré).
     * Structure simple : col0 = enfant de root, colN = enfant de col(N-1). Pas de bridges.
     */
    public void generateAdvancementsDatapack(BingoGrid grid) {
        generateAdvancementsDatapack(grid, new HashSet<>());
    }

    private void generateAdvancementsDatapack(BingoGrid grid, Set<String> foundIds) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        File datapackRoot = dataFolder.getParentFile().getParentFile(); // data/

        writePackMcmeta(datapackRoot.getParentFile());
        cleanDirectory(dataFolder);
        disableVanillaAdvancements(datapackRoot);
        createRootAdvancement(dataFolder);

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        BingoPlugin.getInstance().getLogger().info("[Bingo] Génération de " + objectives.size() + " objectifs pour grille " + size + "x" + size);

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

                boolean isFound = foundIds.contains(obj.getId());
                createObjectiveAdvancement(dataFolder, obj, advId, parent, isFound);
            }
        }

        BingoPlugin.getInstance().getLogger().info("[Bingo] " + objectives.size() + " advancements créés.");

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§b§l[Bingo] §aLa grille a été mise à jour ! Appuyez sur §e[L] §aou tapez §e/bg §apour la voir.");
            }
        }, 10L);
    }

    /**
     * Actualise la grille : items trouvés → frame "challenge" (étoile dorée).
     * Appelé quand un item est trouvé par une équipe.
     * Debounce intégré pour éviter les reloads multiples.
     */
    private static int pendingReloadTask = -1;

    public void refreshFoundItems(BingoGrid grid) {
        // Collecter tous les objectifs trouvés par n'importe quelle équipe
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        Set<String> foundIds = new HashSet<>();
        for (BingoTeam team : tm.getTeams()) {
            foundIds.addAll(team.getUnlockedObjectives());
        }

        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        // Regénérer les fichiers d'advancements
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

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

                boolean isFound = foundIds.contains(obj.getId());
                createObjectiveAdvancement(dataFolder, obj, advId, parent, isFound);
            }
        }

        // Debounce le reload
        if (pendingReloadTask != -1) {
            Bukkit.getScheduler().cancelTask(pendingReloadTask);
        }
        pendingReloadTask = Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            pendingReloadTask = -1;
            BingoPlugin.getInstance().getLogger().info("[Bingo] Grille rafraîchie avec les items trouvés.");
        }, 10L).getTaskId();
    }

    // ── Création des fichiers JSON ──

    /**
     * Crée un advancement pour un objectif.
     * - Tous utilisent tick (auto-complète → visible d'office)
     * - Non trouvé: frame "task" (carré doré)
     * - Trouvé: frame "challenge" (étoile dorée ★) + description verte
     */
    private void createObjectiveAdvancement(File dataFolder, BingoObjective obj, String advId, String parent, boolean found) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        String displayName = obj.getId().replace("_", " ");
        if (!displayName.isEmpty()) {
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        }

        String frame = found ? "challenge" : "task";
        String description = found ? "\\u00a7a\\u2714 Trouvé !" : "Obtenir un(e) " + displayName;

        String json = "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"" + itemId + "\" },\n" +
                "    \"title\": \"" + displayName + "\",\n" +
                "    \"description\": \"" + description + "\",\n" +
                "    \"frame\": \"" + frame + "\",\n" +
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
        saveFile(dataFolder, advId + ".json", json);
    }

    private void createRootAdvancement(File dataFolder) {
        String json = "{\n" +
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
                "    \"auto\": {\n" +
                "      \"trigger\": \"minecraft:tick\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        saveFile(dataFolder, "root.json", json);
    }

    // ── Vanilla advancements ──

    private void disableVanillaAdvancements(File dataRoot) {
        String[][] vanillaTabs = {
            {"minecraft", "story/root"},
            {"minecraft", "adventure/root"},
            {"minecraft", "husbandry/root"},
            {"minecraft", "nether/root"},
            {"minecraft", "end/root"}
        };

        String hiddenJson = "{\n" +
                "  \"criteria\": {\n" +
                "    \"impossible\": {\n" +
                "      \"trigger\": \"minecraft:impossible\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        for (String[] tab : vanillaTabs) {
            File dir = new File(dataRoot, tab[0] + "/advancement/" + tab[1].substring(0, tab[1].lastIndexOf('/')));
            if (!dir.exists()) dir.mkdirs();
            saveFile(dir, "root.json", hiddenJson);
        }
    }

    // ── Utilitaires ──

    private File getDataFolder() {
        File worldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
        File dataFolder = new File(worldFolder, "datapacks/bingo_datapack/data/" + namespace + "/advancement");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            BingoPlugin.getInstance().getLogger().severe("Impossible de creer le dossier datapack !");
            return null;
        }
        return dataFolder;
    }

    private void writePackMcmeta(File datapackFolder) {
        try (FileWriter writer = new FileWriter(new File(datapackFolder, "pack.mcmeta"))) {
            writer.write("{ \"pack\": { \"pack_format\": 71, \"supported_formats\": [48, 71], \"description\": \"Bingo Advancements\" } }");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableAndReload() {
        try {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \"file/bingo_datapack\"");
        } catch (Exception ignored) {}
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

    private void saveFile(File dir, String fileName, String content) {
        try (FileWriter writer = new FileWriter(new File(dir, fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
