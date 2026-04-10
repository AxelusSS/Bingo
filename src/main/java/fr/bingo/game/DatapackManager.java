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

        File datapackRoot = dataFolder.getParentFile().getParentFile(); // data/

        writePackMcmeta(datapackRoot.getParentFile());
        cleanDirectory(dataFolder);

        // Masquer les onglets vanilla (override les roots → invisible)
        disableVanillaAdvancements(datapackRoot);

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

                String parent;
                if (col == 0) {
                    parent = namespace + ":root";
                } else {
                    parent = namespace + ":" + getBridgeId(row, col - 1);
                }

                createObjectiveAdvancement(dataFolder, obj, advId, parent);

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

            // Revoke TOUTES les progressions (reset complet pour éviter les items déjà dorés)
            Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                revokeAllAdvancements();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("§b§l[Bingo] §aLa grille a été mise à jour ! Appuyez sur §e[L] §aou tapez §e/bg §apour la voir.");
                }
            }, 5L);
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

                BingoPlugin.getInstance().getLogger().info("[Bingo] markObjectiveFound: " + objectiveId + " → " + key);

                Bukkit.getScheduler().runTaskLater(BingoPlugin.getInstance(), () -> {
                    org.bukkit.advancement.Advancement adv = Bukkit.getAdvancement(key);
                    if (adv != null) {
                        for (java.util.UUID uuid : team.getPlayers()) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) {
                                boolean success = p.getAdvancementProgress(adv).awardCriteria("found");
                                BingoPlugin.getInstance().getLogger().info("[Bingo] Award 'found' pour " + p.getName() + " sur " + advId + " → " + (success ? "OK" : "ECHEC"));
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

    /**
     * Override les roots des onglets vanilla pour les masquer.
     * Crée des fichiers qui remplacent les advancements vanilla par des versions invisibles.
     */
    private void disableVanillaAdvancements(File dataRoot) {
        // Les 5 onglets vanilla à masquer
        String[][] vanillaTabs = {
            {"minecraft", "story/root"},
            {"minecraft", "adventure/root"},
            {"minecraft", "husbandry/root"},
            {"minecraft", "nether/root"},
            {"minecraft", "end/root"}
        };

        // Advancement sans display = invisible (pas d'onglet créé)
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

        BingoPlugin.getInstance().getLogger().info("[Bingo] Onglets vanilla masqués.");
    }

    /**
     * Revoke uniquement les progressions des ITEMS bingo (rXcY).
     * Ne touche PAS au root ni aux bridges (ils doivent rester DONE pour la visibilité).
     */
    private void revokeAllAdvancements() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            java.util.Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
            while (it.hasNext()) {
                org.bukkit.advancement.Advancement adv = it.next();
                String key = adv.getKey().toString();

                // Ne revoquer que les items bingo (bingoclassique:rXcY)
                // Ignorer root, ponts (bXcY) et advancements vanilla
                if (!key.startsWith(namespace + ":r")) continue;

                org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(adv);
                // Copier pour éviter ConcurrentModificationException
                java.util.Set<String> awarded = new java.util.HashSet<>(progress.getAwardedCriteria());
                for (String criteria : awarded) {
                    progress.revokeCriteria(criteria);
                }
            }
        }
        BingoPlugin.getInstance().getLogger().info("[Bingo] Progressions des items bingo revoquées.");
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
