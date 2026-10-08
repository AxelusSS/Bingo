import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Suppression du debug de chemin
pattern_debug_path = '''        Bukkit.broadcastMessage("\u00A7e[DEBUG] pack.mcmeta a \u00E9t\u00E9 \u00E9crit dans : " + new File(dir, "pack.mcmeta").getAbsolutePath());
        Bukkit.broadcastMessage("\u00A7e[DEBUG] Est-ce que le fichier existe ? " + new File(dir, "pack.mcmeta").exists());
        
        File datapacksFolder = dir.getParentFile();
        Bukkit.broadcastMessage("\u00A7e[DEBUG] Dossier parent 'datapacks' existe ? " + datapacksFolder.exists());
        if (datapacksFolder.exists()) {
            Bukkit.broadcastMessage("\u00A7e[DEBUG] Contenu de " + datapacksFolder.getAbsolutePath() + " :");
            for (String s : datapacksFolder.list()) {
                Bukkit.broadcastMessage("\u00A7e - " + s);
            }
        }'''
c = c.replace(pattern_debug_path, "")

# Suppression du debug de liste
pattern_debug_list = '''        Bukkit.broadcastMessage("\u00A7e[DEBUG] Liste des datapacks scann\u00E9s par le serveur :");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack list");'''
c = c.replace(pattern_debug_list, "")

# Suppression du debug iterateur
pattern_debug_iter = '''                int count = 0;
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
c = c.replace(pattern_debug_iter, "")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

