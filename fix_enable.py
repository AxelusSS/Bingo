import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_enable = '''        // Forcer l'activation m\u00EAme si le pack_format ne correspond pas parfaitement
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \\"file/bingo_datapack\\""); }
        catch (Exception ignored) {}'''

new_enable = '''        // Forcer l'activation m\u00EAme si le pack_format ne correspond pas parfaitement
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \\"file/bingo_datapack\\""); }
        catch (Exception ignored) {}
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \\"bingo_datapack\\""); }
        catch (Exception ignored) {}'''

if old_enable in c:
    c = c.replace(old_enable, new_enable)
else:
    print("Failed to replace old_enable")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

