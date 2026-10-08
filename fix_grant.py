import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_task = '''        Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
            enableAndReload();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("\\u00A7b\\u00A7l[Hel] \\u00A7aGrille mise \\u00E0 jour ! Appuyez sur \\u00A7e[L] \\u00A7apour la voir.");
            }
        }, 10L);'''

new_task = '''        Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
            enableAndReload();
            
            // Accorder l'advancement root \u00E0 tout le monde pour forcer l'affichage de l'onglet
            org.bukkit.advancement.Advancement rootAdv = Bukkit.getAdvancement(new org.bukkit.NamespacedKey("hel", "root"));
            if (rootAdv != null) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    org.bukkit.advancement.AdvancementProgress progress = p.getAdvancementProgress(rootAdv);
                    for (String crit : progress.getRemainingCriteria()) {
                        progress.awardCriteria(crit);
                    }
                    p.sendMessage("\\u00A7b\\u00A7l[Hel] \\u00A7aGrille mise \\u00E0 jour ! Appuyez sur \\u00A7e[L] \\u00A7apour la voir.");
                }
            } else {
                Bukkit.broadcastMessage("\\u00A7c[DEBUG] ERREUR : L'advancement hel:root n'a pas pu \u00E9tre r\u00E9cup\u00E9r\u00E9 apr\u00E8s le rechargement !");
            }
        }, 10L);'''

if old_task in c:
    c = c.replace(old_task, new_task)
else:
    print("Failed to replace old_task")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

