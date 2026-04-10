package fr.bingo.game;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Gère la génération du datapack d'advancements pour la grille Bingo.
 *
 * Chaque item a 2 critères :
 * - "auto" (tick) → force la visibilité (le client connaît l'item)
 * - "found" (impossible) → contrôle gris/or (awarding per-player par équipe)
 *
 * requirements: [["found"]] → seul "found" détermine si l'item est complété.
 * Résultat : pas de barre de progression, gris → or, per-player !
 */
public class DatapackManager {

    private final String namespace = "bingoclassique";

    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        return getAdvancementId(index / gridSize, index % gridSize);
    }

    // ── Génération ──

    public void generateAdvancementsDatapack(BingoGrid grid) {
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

                createItemAdvancement(dataFolder, obj, advId, parent);
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
     * Quand une équipe trouve un item :
     * → Award "found" pour TOUS les joueurs de cette équipe uniquement.
     * → Les autres équipes voient toujours l'item en GRIS.
     */
    public static void markObjectiveFound(BingoGrid grid, BingoTeam team, String objectiveId) {
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        for (int i = 0; i < objectives.size(); i++) {
            if (objectives.get(i).getId().equalsIgnoreCase(objectiveId)) {
                String advId = getAdvancementIdFromIndex(i, size);
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("bingoclassique", advId);

                Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                    org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
                    if (adv != null) {
                        // Award uniquement pour les joueurs de CETTE équipe
                        for (java.util.UUID uuid : team.getPlayers()) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) {
                                p.getAdvancementProgress(adv).awardCriteria("found");
                            }
                        }
                    }
                }, 1L);
                break;
            }
        }
    }

    // ── Fichiers JSON ──

    /**
     * Item de la grille avec double critère :
     * - "auto" (tick) → visibilité immédiate
     * - "found" (impossible) → gris/or per-player
     * - requirements: [["found"]] → PAS de barre de progression
     */
    private void createItemAdvancement(File dir, BingoObjective obj, String advId, String parent) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        String name = obj.getId().replace("_", " ");
        if (!name.isEmpty()) name = name.substring(0, 1).toUpperCase() + name.substring(1);

        String json = "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"" + itemId + "\" },\n" +
                "    \"title\": \"" + name + "\",\n" +
                "    \"description\": \"Obtenir un(e) " + name + "\",\n" +
                "    \"frame\": \"task\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:tick\" },\n" +
                "    \"found\": { \"trigger\": \"minecraft:impossible\" }\n" +
                "  },\n" +
                "  \"requirements\": [[\"found\"]]\n" +
                "}";
        saveFile(dir, advId + ".json", json);
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
