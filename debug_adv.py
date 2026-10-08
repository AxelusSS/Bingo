import sys

path = 'src/main/java/fr/hel/game/HelGame.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

debug_code = '''        // Scanner d'inventaire (seulement si Hel actif)
        if (isHelMode) {
            org.bukkit.advancement.Advancement root = Bukkit.getAdvancement(new org.bukkit.NamespacedKey("hel", "root"));
            if (root != null) {
                Bukkit.broadcastMessage("\u00A7a[DEBUG] L'advancement hel:root EST CHARG\u00C9 dans le serveur !");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(root);
                    for (String crit : progress.getRemainingCriteria()) {
                        progress.awardCriteria(crit);
                    }
                }
            } else {
                Bukkit.broadcastMessage("\u00A7c[DEBUG] ERREUR : L'advancement hel:root n'existe pas ! Le JSON est invalide ou le datapack a \u00E9chou\u00E9.");
            }
            startInventoryScanner();
        }'''

c = c.replace('''        // Scanner d'inventaire (seulement si Hel actif)
        if (isHelMode) {
            startInventoryScanner();
        }''', debug_code)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

