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

    /**
     * Intervalle de relais : toutes les RELAY_INTERVAL colonnes, l'item est auto-complété (doré).
     * Les colonnes intermédiaires sont grises (critère impossible).
     * Cela contourne la limite de profondeur du client (~8 niveaux).
     *
     * Pour 5x5 : relais en col 0 et col 3 → 2 colonnes dorées, 3 grises
     * Pour 7x7 : relais en col 0, col 3, col 6 → 3 colonnes dorées, 4 grises
     */
    private static final int RELAY_INTERVAL = 3;

    private boolean isRelayColumn(int col) {
        return col % RELAY_INTERVAL == 0;
    }

    public static String getAdvancementId(int row, int col) {
        return String.format("r%dc%d", row, col);
    }

    public static String getAdvancementIdFromIndex(int index, int gridSize) {
        return getAdvancementId(index / gridSize, index % gridSize);
    }

    private static String getBridgeId(int row, int col) {
        return String.format("b%dc%d", row, col);
    }

    /**
     * Génère le datapack avec le système hybride :
     * - Colonnes relais (0, 3, 6...) : tick → auto-complète → DORÉ (toujours visible)
     * - Autres colonnes : impossible → GRIS (passe en or quand trouvé)
     * - Ponts invisibles entre chaque item (tick, no display)
     */
    public void generateAdvancementsDatapack(BingoGrid grid) {
        File dataFolder = getDataFolder();
        if (dataFolder == null) return;

        File datapackRoot = dataFolder.getParentFile().getParentFile();

        writePackMcmeta(datapackRoot.getParentFile());
        cleanDirectory(dataFolder);
        disableVanillaAdvancements(datapackRoot);
        createRootAdvancement(dataFolder);

        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        BingoPlugin.getInstance().getLogger().info("[Bingo] Génération grille " + size + "x" + size + " (relais toutes les " + RELAY_INTERVAL + " colonnes)");

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int index = row * size + col;
                if (index >= objectives.size()) break;

                BingoObjective obj = objectives.get(index);
                String advId = getAdvancementId(row, col);

                // Parent : root pour col 0, sinon le pont précédent
                String parent;
                if (col == 0) {
                    parent = namespace + ":root";
                } else {
                    parent = namespace + ":" + getBridgeId(row, col - 1);
                }

                // Relais ou item normal
                boolean relay = isRelayColumn(col);
                createObjectiveAdvancement(dataFolder, obj, advId, parent, relay);

                // Pont invisible après l'item (sauf dernier de la rangée)
                if (col < size - 1) {
                    String bridgeId = getBridgeId(row, col);
                    String bridgeParent = namespace + ":" + advId;
                    createBridgeAdvancement(dataFolder, bridgeId, bridgeParent);
                }
            }
        }

        BingoPlugin.getInstance().getLogger().info("[Bingo] Advancements créés.");

        Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§b§l[Bingo] §aGrille mise à jour ! Appuyez sur §e[L] §apour la voir.");
            }
        }, 10L);
    }

    /**
     * Marque un objectif comme trouvé → award "found" → passe de GRIS à OR.
     * (Ne fonctionne QUE pour les items non-relais)
     */
    public static void markObjectiveFound(BingoGrid grid, BingoTeam team, String objectiveId) {
        List<BingoObjective> objectives = grid.getObjectives();
        int size = grid.getSize();

        for (int i = 0; i < objectives.size(); i++) {
            if (objectives.get(i).getId().equalsIgnoreCase(objectiveId)) {
                int col = i % size;

                // Les relais sont déjà dorés, pas besoin d'award
                if (col % RELAY_INTERVAL == 0) {
                    BingoPlugin.getInstance().getLogger().info("[Bingo] " + objectiveId + " est un relais (col " + col + "), déjà doré.");
                    return;
                }

                String advId = getAdvancementIdFromIndex(i, size);
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("bingoclassique", advId);

                BingoPlugin.getInstance().getLogger().info("[Bingo] markObjectiveFound: " + objectiveId + " → " + key);

                Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                    org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
                    if (adv != null) {
                        for (java.util.UUID uuid : team.getPlayers()) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) {
                                boolean ok = p.getAdvancementProgress(adv).awardCriteria("found");
                                BingoPlugin.getInstance().getLogger().info("[Bingo] Award 'found' " + p.getName() + " sur " + advId + " → " + (ok ? "OK" : "ECHEC"));
                            }
                        }
                    } else {
                        BingoPlugin.getInstance().getLogger().warning("[Bingo] Advancement introuvable: " + key);
                    }
                }, 1L);
                break;
            }
        }
    }

    // ── Création des fichiers JSON ──

    /**
     * Item de la grille.
     * - relay=true  → tick auto-complète → DORÉ (relais de visibilité)
     * - relay=false → impossible → GRIS, passe en or via markObjectiveFound
     */
    private void createObjectiveAdvancement(File dataFolder, BingoObjective obj, String advId, String parent, boolean relay) {
        String itemId = "minecraft:" + obj.getId().toLowerCase();
        String displayName = obj.getId().replace("_", " ");
        if (!displayName.isEmpty()) {
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        }

        String trigger = relay ? "minecraft:tick" : "minecraft:impossible";
        String criteriaName = relay ? "auto" : "found";

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
                "    \"" + criteriaName + "\": {\n" +
                "      \"trigger\": \"" + trigger + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        saveFile(dataFolder, advId + ".json", json);
    }

    /**
     * Pont INVISIBLE entre deux items (pas de display, tick auto-complète).
     */
    private void createBridgeAdvancement(File dataFolder, String bridgeId, String parent) {
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
