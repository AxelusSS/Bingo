import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_cmd = '''            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant @a only hel:root");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("\u00A7b\u00A7l[Hel] \u00A7aGrille mise \u00E0 jour ! Appuyez sur \u00A7e[L] \u00A7apour la voir.");
            }'''

new_cmd = '''            Bukkit.getScheduler().runTaskLater(HelPlugin.getInstance(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant @a only hel:root");
                
                Bukkit.broadcastMessage("\u00A7e[DEBUG] Liste des advancements 'hel:' en m\u00E9moire :");
                int count = 0;
                java.util.Iterator<org.bukkit.advancement.Advancement> it = Bukkit.advancementIterator();
                while (it.hasNext()) {
                    org.bukkit.advancement.Advancement adv = it.next();
                    if (adv.getKey().getNamespace().equals("hel")) {
                        Bukkit.broadcastMessage("\u00A7e - " + adv.getKey().toString());
                        count++;
                    }
                }
                Bukkit.broadcastMessage("\u00A7eTotal 'hel:' trouv\u00E9s : " + count);
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("\u00A7b\u00A7l[Hel] \u00A7aGrille mise \u00E0 jour ! Appuyez sur \u00A7e[L] \u00A7apour la voir.");
                }
            }, 20L); // Wait an extra second just in case!'''

if old_cmd in c:
    c = c.replace(old_cmd, new_cmd)
else:
    print("Failed to replace old_cmd")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

