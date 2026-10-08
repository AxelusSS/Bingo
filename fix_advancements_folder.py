import sys
import re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Modifier getDataFolder pour inclure 'advancement' ET 'advancements' sera compliqu\u00E9.
# Le plus simple est de tout g\u00E9n\u00E9rer dans 'advancement', puis de copier tout le dossier en 'advancements'.
# Ajoutons cela \u00E0 la fin de generateAdvancementsDatapack.

copy_logic = '''        disableVanillaAdvancements(dataFolder.getParentFile().getParentFile());
        
        // --- FIX POUR LA COMPATIBILIT\u00C9 1.20 vs 1.21 ---
        // Copier 'advancement' vers 'advancements'
        try {
            File advancementsFolder = new File(dataFolder.getParentFile(), "advancements");
            if (!advancementsFolder.exists()) advancementsFolder.mkdirs();
            copyDirectory(dataFolder, advancementsFolder);
            
            // Et pour minecraft/advancement vers minecraft/advancements
            File mcAdv = new File(dataFolder.getParentFile().getParentFile(), "minecraft/advancement");
            File mcAdvs = new File(dataFolder.getParentFile().getParentFile(), "minecraft/advancements");
            if (mcAdv.exists()) {
                if (!mcAdvs.exists()) mcAdvs.mkdirs();
                copyDirectory(mcAdv, mcAdvs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }'''

# Create copyDirectory helper
copy_helper = '''    private void copyDirectory(File source, File target) throws java.io.IOException {
        if (source.isDirectory()) {
            if (!target.exists()) target.mkdirs();
            String[] children = source.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(source, children[i]), new File(target, children[i]));
            }
        } else {
            java.io.InputStream in = new java.io.FileInputStream(source);
            java.io.OutputStream out = new java.io.FileOutputStream(target);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }'''

if "private void copyDirectory" not in c:
    c = c.replace("private void cleanDirectory", copy_helper + "\n\n    private void cleanDirectory")

if "disableVanillaAdvancements(dataFolder.getParentFile().getParentFile());" in c:
    c = c.replace("disableVanillaAdvancements(dataFolder.getParentFile().getParentFile());", copy_logic)
else:
    print("Failed to hook into generateAdvancementsDatapack")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

