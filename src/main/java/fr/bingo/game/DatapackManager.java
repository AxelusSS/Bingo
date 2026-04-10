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
                String relayId = "relay_" + advId;

                // Le relay parent : col 0 → root, sinon → item précédent
                String relayParent = (col == 0)
                        ? namespace + ":root"
                        : namespace + ":" + getAdvancementId(row, col - 1);

                // 1) Relay invisible (tick, DONE, pas de display)
                createRelayAdvancement(dataFolder, relayId, relayParent);

                // 2) Item réel (impossible, display, parent = relay)
                createItemAdvancement(dataFolder, obj, advId, namespace + ":" + relayId);
            }
        }

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();

            // Les relais invisibles (tick) rendent tout visible automatiquement.
            // Items commencent GRIS → award "found" per-team quand trouvé.
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("§b§l[Bingo] §aGrille mise à jour ! Appuyez sur §e[L] §apour la voir.");
                }
            }, 10L);
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

    /**
     * Relay invisible : pas de "display" → n'apparaît pas dans l'UI.
     * Critère tick → toujours DONE → ses enfants sont visibles.
     * Display minimal (vitre bleu clair) pour compter comme "noeud affiché complété".
     */
    private void createRelayAdvancement(File dir, String relayId, String parent) {
        saveFile(dir, relayId + ".json",
                "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:light_blue_stained_glass_pane\" },\n" +
                "    \"title\": \" \",\n" +
                "    \"description\": \" \",\n" +
                "    \"frame\": \"task\",\n" +
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
