import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_reload = '''    private void enableAndReload() {
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \\"file/bingo_datapack\\""); }
        catch (Exception ignored) {}
        Bukkit.reloadData();
    }'''

new_reload = '''    private void enableAndReload() {
        // Premier reload pour d\u00E9couvrir le datapack
        Bukkit.reloadData();
        // Forcer l'activation m\u00EAme si le pack_format ne correspond pas parfaitement
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \\"file/bingo_datapack\\""); }
        catch (Exception ignored) {}
        // Deuxi\u00E8me reload pour appliquer les fichiers
        Bukkit.reloadData();
    }'''

if old_reload in c:
    c = c.replace(old_reload, new_reload)
else:
    print("Failed to replace old_reload")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
