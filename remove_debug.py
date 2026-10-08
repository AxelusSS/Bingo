import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_debug_path = '''        Bukkit.broadcastMessage("\\u00A7e[DEBUG] pack.mcmeta a \\u00E9t\\u00E9 \\u00E9crit dans : " + new File(dir, "pack.mcmeta").getAbsolutePath());
        Bukkit.broadcastMessage("\\u00A7e[DEBUG] Est-ce que le fichier existe ? " + new File(dir, "pack.mcmeta").exists());
        
        File datapacksFolder = dir.getParentFile();
        Bukkit.broadcastMessage("\\u00A7e[DEBUG] Dossier parent 'datapacks' existe ? " + datapacksFolder.exists());
        if (datapacksFolder.exists()) {
            Bukkit.broadcastMessage("\\u00A7e[DEBUG] Contenu de " + datapacksFolder.getAbsolutePath() + " :");
            for (String s : datapacksFolder.list()) {
                Bukkit.broadcastMessage("\\u00A7e - " + s);
            }
        }'''

if old_debug_path in c:
    c = c.replace(old_debug_path, "")
else:
    print("Failed to remove old_debug_path")

old_debug_list = '''        // Debug: Lister les datapacks disponibles
        Bukkit.broadcastMessage("\\u00A7e[DEBUG] Liste des datapacks scann\\u00E9s par le serveur :");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack list");'''

if old_debug_list in c:
    c = c.replace(old_debug_list, "")
else:
    print("Failed to remove old_debug_list")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

