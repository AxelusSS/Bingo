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

    // Anti-spam : évite de reload 10x de suite si plusieurs items sont trouvés en même temps
    private static int pendingReloadTask = -1;

    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        return getAdvancementId(index / gridSize, index % gridSize);
    }

    /**
     * Génère le datapack initial (tous les items en frame "task").
     */
    public void generateAdvancementsDatapack(BingoGrid grid) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        writePackMcmeta(dataFolder.getParentFile());
        cleanDirectory(dataFolder);
        createRootAdvancement(dataFolder);

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        BingoPlugin.getInstance().getLogger().info("[Bingo] Génération de " + objectives.size() + " objectifs pour grille " + size + "x" + size);

        // Aucun item trouvé au départ
        Set<String> foundIds = new HashSet<>();

        writeAllAdvancements(dataFolder, grid, foundIds);

        BingoPlugin.getInstance().getLogger().info("[Bingo] " + objectives.size() + " advancements créés.");

        // Forcer le rechargement
        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§b§l[Bingo] §aLa grille a été mise à jour ! Appuyez sur §e[L] §aou tapez §e/bg §apour la voir.");
            }
        }, 10L);
    }

    /**
     * Met à jour la grille pour montrer les items trouvés avec le frame "challenge" (étoile).
     * Appelé quand une équipe trouve un item.
     * Debounce : si plusieurs appels rapides, un seul reload est fait.
     */
    public void refreshFoundItems(BingoGrid grid) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        // Collecter TOUS les objectifs trouvés par n'importe quelle équipe
        TeamManager tm = BingoPlugin.getInstance().getTeamManager();
        Set<String> foundIds = new HashSet<>();
        for (BingoTeam team : tm.getTeams()) {
            foundIds.addAll(team.getUnlockedObjectives());
        }

        // Regénérer tous les fichiers d'avancement
        createRootAdvancement(dataFolder);
        writeAllAdvancements(dataFolder, grid, foundIds);

        // Debounce le reload (attendre 10 ticks pour grouper les changements)
        if (pendingReloadTask != -1) {
            Bukkit.getScheduler().cancelTask(pendingReloadTask);
        }
        pendingReloadTask = Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            pendingReloadTask = -1;
        }, 10L).getTaskId();
    }

    // ── Méthodes internes ──

    private void writeAllAdvancements(File dataFolder, BingoGrid grid, Set<String> foundIds) {
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
    }

    private void createObjectiveAdvancement(File dataFolder, BingoObjective obj, String advId, String parent, boolean found) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        String displayName = obj.getId().replace("_", " ");
        if (!displayName.isEmpty()) {
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        }

        // Frame différent selon si l'item est trouvé ou non :
        // - "task" = cadre carré (non trouvé)
        // - "challenge" = cadre étoile/spiky (trouvé !)
        String frame = found ? "challenge" : "task";
        String description = found ? "§a✔ Trouvé !" : "Obtenir un(e) " + displayName;

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

    private void saveFile(File dir, String fileName, String content) {
        try (FileWriter writer = new FileWriter(new File(dir, fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
