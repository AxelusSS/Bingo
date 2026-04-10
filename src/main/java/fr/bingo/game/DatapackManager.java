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
 * Système per-player :
 * 1. Items avec critère "found" (impossible) → gris par défaut
 * 2. Après reload : award "found" sur TOUT pour TOUS → visible + doré
 * 3. Puis revoke "found" pour TOUS → visible mais GRIS (client les connaît)
 * 4. En jeu : award "found" par équipe → per-player gris/or
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

            // Étape 1 : Award "found" pour TOUS → tout visible + doré
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                awardAllForAllPlayers(grid);

                // Étape 2 : Revoke "found" pour TOUS → tout visible mais GRIS
                Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                    revokeAllForAllPlayers(grid);

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage("§b§l[Bingo] §aGrille mise à jour ! Appuyez sur §e[L] §apour la voir.");
                    }
                }, 5L);
            }, 5L);
        }, 10L);
    }

    /**
     * Award "found" sur TOUS les items pour TOUS les joueurs.
     * Le client découvre tous les items (ils deviennent visibles).
     */
    private void awardAllForAllPlayers(BingoGrid grid) {
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        for (int i = 0; i < objectives.size(); i++) {
            String advId = getAdvancementIdFromIndex(i, size);
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(namespace, advId);
            org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getAdvancementProgress(adv).awardCriteria("found");
                }
            }
        }
        BingoPlugin.getInstance().getLogger().info("[Bingo] Award ALL pour " + Bukkit.getOnlinePlayers().size() + " joueurs sur " + objectives.size() + " items.");
    }

    /**
     * Revoke "found" sur TOUS les items pour TOUS les joueurs.
     * Les items restent visibles (le client les connaît) mais repassent en GRIS.
     */
    private void revokeAllForAllPlayers(BingoGrid grid) {
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        for (int i = 0; i < objectives.size(); i++) {
            String advId = getAdvancementIdFromIndex(i, size);
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(namespace, advId);
            org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
            if (adv != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getAdvancementProgress(adv).revokeCriteria("found");
                }
            }
        }
        BingoPlugin.getInstance().getLogger().info("[Bingo] Revoke ALL pour " + Bukkit.getOnlinePlayers().size() + " joueurs. Items maintenant GRIS.");
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

    // ── JSON ──

    /**
     * Item avec un seul critère "found" (impossible).
     * Gris par défaut, or quand "found" est award per-player.
     */
    private void createItemAdvancement(File dir, BingoObjective obj, String advId, String parent) {
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
                "    \"frame\": \"task\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
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
