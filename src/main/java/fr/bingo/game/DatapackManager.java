package fr.bingo.game;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatapackManager {

    private final String namespace = "bingoclassique";

    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    /** ID du pont entre item [row,col] et item [row,col+1] */
    private static String getBridgeId(int row, int col) {
        return String.format("b%dc%d", row, col);
    }

    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        return getAdvancementId(index / gridSize, index % gridSize);
    }

    /**
     * Génère le datapack avec des ponts entre chaque item.
     * 
     * Structure par rangée :
     *   root(DONE) → item_c0(gris) → bridge_c0(DONE,invisible) → item_c1(gris) → bridge_c1(DONE) → item_c2(gris) → ...
     * 
     * Les ponts ont un seul critère tick → auto-complètent → DONE → enfant visible.
     * Résultat : TOUTE la grille est visible en gris, et passe en or quand trouvé.
     */
    public void generateAdvancementsDatapack(BingoGrid grid) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        writePackMcmeta(dataFolder.getParentFile());
        cleanDirectory(dataFolder);
        createRootAdvancement(dataFolder);

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        BingoPlugin.getInstance().getLogger().info("[Bingo] Génération de " + objectives.size() + " objectifs + ponts pour grille " + size + "x" + size);

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                BingoObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);

                // Déterminer le parent de l'item
                String parent;
                if (col == 0) {
                    // Premier item de la rangée → enfant de root
                    parent = namespace + ":root";
                } else {
                    // Enfant du PONT précédent (pas de l'item précédent !)
                    parent = namespace + ":" + getBridgeId(row, col - 1);
                }

                // Créer l'item (2 critères : visible + found)
                createObjectiveAdvancement(dataFolder, obj, advId, parent);

                // Créer le pont APRÈS cet item (sauf pour le dernier de la rangée)
                if (col < size - 1) {
                    String bridgeId = getBridgeId(row, col);
                    String bridgeParent = namespace + ":" + advId;
                    createBridgeAdvancement(dataFolder, bridgeId, bridgeParent);
                }
            }
        }

        int totalFiles = size * size + (size * (size - 1)); // items + bridges
        BingoPlugin.getInstance().getLogger().info("[Bingo] " + totalFiles + " fichiers créés (items + ponts).");

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§b§l[Bingo] §aLa grille a été mise à jour ! Appuyez sur §e[L] §aou tapez §e/bg §apour la voir.");
            }
        }, 10L);
    }

    /**
     * Marque un objectif comme trouvé → award "found" → passe de GRIS à OR.
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

    // ── Création des fichiers JSON ──

    /**
     * Item de la grille : 1 seul critère "found" (impossible).
     * La visibilité est assurée par le pont invisible (parent DONE).
     * Gris par défaut, passe en or quand "found" est award.
     * PAS de barre de progression (un seul critère = 0/1 → pas affiché).
     */
    private void createObjectiveAdvancement(File dataFolder, BingoObjective obj, String advId, String parent) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        String displayName = obj.getId().replace("_", " ");
        if (!displayName.isEmpty()) {
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        }

        String json = "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"" + itemId + "\" },\n" +
                "    \"title\": \"" + displayName + "\",\n" +
                "    \"description\": \"Obtenir un(e) " + displayName + "\",\n" +
                "    \"frame\": \"task\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"found\": {\n" +
                "      \"trigger\": \"minecraft:impossible\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        saveFile(dataFolder, advId + ".json", json);
    }

    /**
     * Pont INVISIBLE entre deux items.
     * PAS de champ "display" → n'apparaît PAS dans l'arbre d'advancements.
     * 1 seul critère tick → auto-complète → DONE → le prochain item est visible.
     * Les enfants se connectent visuellement au plus proche parent affiché.
     */
    private void createBridgeAdvancement(File dataFolder, String bridgeId, String parent) {
        // PAS de "display" → le pont est 100% invisible dans l'onglet advancements
        String json = "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": {\n" +
                "      \"trigger\": \"minecraft:tick\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        saveFile(dataFolder, bridgeId + ".json", json);
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
