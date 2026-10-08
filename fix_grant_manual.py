import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

start_str = "// Accorder l'advancement root \u00E0 tout le monde"
end_str = "} else {"
end_str2 = "L'advancement hel:root n'a pas pu \u00E9tre r\u00E9cup\u00E9r\u00E9 apr\u00E8s le rechargement !\");\n            }"

start_idx = c.find(start_str)
end_idx = c.find(end_str2) + len(end_str2)

if start_idx != -1 and end_idx != -1:
    new_grant = '''// Accorder l'advancement root nativement via la commande pour contourner les bugs de l'API Bukkit
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant @a only hel:root");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("\u00A7b\u00A7l[Hel] \u00A7aGrille mise \u00E0 jour ! Appuyez sur \u00A7e[L] \u00A7apour la voir.");
            }'''
    c = c[:start_idx] + new_grant + c[end_idx:]
    with open(path, 'w', encoding='utf-8') as f:
        f.write(c)
    print("Replaced successfully")
else:
    print("Could not find boundaries")

