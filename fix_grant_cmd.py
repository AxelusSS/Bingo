import sys, re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

pattern = re.compile(r'// Accorder l\'advancement root.*?\} else \{.*?\}', re.DOTALL)

new_grant = '''// Accorder l'advancement root nativement via la commande pour contourner les bugs de l'API Bukkit
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant @a only hel:root");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("\\u00A7b\\u00A7l[Hel] \\u00A7aGrille mise \\u00E0 jour ! Appuyez sur \\u00A7e[L] \\u00A7apour la voir.");
            }'''

c, count = pattern.subn(new_grant, c)
print(f"Replaced {count} occurrences")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

