import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

debug_path = '''    private void writePackMcmeta(File dir) {
        saveFile(dir, "pack.mcmeta",
                "{\\n" +
                "  \\"pack\\": {\\n" +
                "    \\"pack_format\\": 48,\\n" +
                "    \\"description\\": \\"Hel\\"\\n" +
                "  }\\n" +
                "}");
        Bukkit.broadcastMessage("\u00A7e[DEBUG] pack.mcmeta a \u00E9t\u00E9 \u00E9crit dans : " + new File(dir, "pack.mcmeta").getAbsolutePath());
        Bukkit.broadcastMessage("\u00A7e[DEBUG] Est-ce que le fichier existe ? " + new File(dir, "pack.mcmeta").exists());
        
        File datapacksFolder = dir.getParentFile();
        Bukkit.broadcastMessage("\u00A7e[DEBUG] Dossier parent 'datapacks' existe ? " + datapacksFolder.exists());
        if (datapacksFolder.exists()) {
            Bukkit.broadcastMessage("\u00A7e[DEBUG] Contenu de " + datapacksFolder.getAbsolutePath() + " :");
            for (String s : datapacksFolder.list()) {
                Bukkit.broadcastMessage("\u00A7e - " + s);
            }
        }
    }'''

old_mcmeta = '''    private void writePackMcmeta(File dir) {
        saveFile(dir, "pack.mcmeta",
                "{\\n" +
                "  \\"pack\\": {\\n" +
                "    \\"pack_format\\": 48,\\n" +
                "    \\"description\\": \\"Hel\\"\\n" +
                "  }\\n" +
                "}");
    }'''

if old_mcmeta in c:
    c = c.replace(old_mcmeta, debug_path)
else:
    print("Failed to replace old_mcmeta")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

