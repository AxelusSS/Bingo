import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

debug_reload = '''    private void enableAndReload() {
        // Premier reload pour d\u00E9couvrir le datapack
        Bukkit.reloadData();
        
        // Debug: Lister les datapacks disponibles
        Bukkit.broadcastMessage("\u00A7e[DEBUG] Liste des datapacks scann\u00E9s par le serveur :");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack list");

        // Forcer l'activation m\u00EAme si le pack_format ne correspond pas parfaitement
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \\"file/bingo_datapack\\""); }
        catch (Exception ignored) {}
        
        // Deuxi\u00E8me reload pour appliquer les fichiers
        Bukkit.reloadData();
    }'''

c = c.replace('''    private void enableAndReload() {
        // Premier reload pour d\u00E9couvrir le datapack
        Bukkit.reloadData();
        // Forcer l'activation m\u00EAme si le pack_format ne correspond pas parfaitement
        try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \\"file/bingo_datapack\\""); }
        catch (Exception ignored) {}
        // Deuxi\u00E8me reload pour appliquer les fichiers
        Bukkit.reloadData();
    }''', debug_reload)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

