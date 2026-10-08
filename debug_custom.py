import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_it = '''                int count = 0;
                java.util.Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
                while (it.hasNext()) {
                    org.bukkit.advancement.Advancement adv = it.next();
                    if (adv.getKey().getNamespace().equals("hel")) {
                        Bukkit.broadcastMessage("\u00A7e - " + adv.getKey().toString());
                        count++;
                    }
                }
                Bukkit.broadcastMessage("\u00A7eTotal 'hel:' trouv\u00E9s : " + count);'''

new_it = '''                int count = 0;
                java.util.Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
                Bukkit.broadcastMessage("\u00A7e[DEBUG] Tous les custom advancements trouv\u00E9s :");
                while (it.hasNext()) {
                    org.bukkit.advancement.Advancement adv = it.next();
                    if (!adv.getKey().getNamespace().equals("minecraft") && !adv.getKey().getNamespace().equals("bukkit") && !adv.getKey().getNamespace().equals("paper") && !adv.getKey().getNamespace().equals("purpur")) {
                        Bukkit.broadcastMessage("\u00A7e - " + adv.getKey().toString());
                        count++;
                    }
                }
                Bukkit.broadcastMessage("\u00A7eTotal custom trouv\u00E9s : " + count);'''

if old_it in c:
    c = c.replace(old_it, new_it)
else:
    print("Failed to replace old_it")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

