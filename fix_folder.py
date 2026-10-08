import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_code = '''    private File getDataFolder() {
        File f = new File(Bukkit.getWorlds().get(0).getWorldFolder(),
                "datapacks/bingo_datapack/data/" + namespace + "/advancement");'''

new_code = '''    private File getDataFolder() {
        File worldDir = new File(Bukkit.getServer().getWorldContainer(), Bukkit.getWorlds().get(0).getName());
        File f = new File(worldDir, "datapacks/bingo_datapack/data/" + namespace + "/advancement");'''

if old_code in c:
    c = c.replace(old_code, new_code)
else:
    print("Failed to replace old_code")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

