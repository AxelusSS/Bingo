import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_mcmeta = '''    private void writePackMcmeta(File dir) {
        saveFile(dir, "pack.mcmeta",
                "{ \\"pack\\": { \\"pack_format\\": 48, \\"supported_formats\\": {\\"min_inclusive\\": 48, \\"max_inclusive\\": 99}, \\"description\\": \\"Hel\\" } }");
    }'''

new_mcmeta = '''    private void writePackMcmeta(File dir) {
        saveFile(dir, "pack.mcmeta",
                "{\\n" +
                "  \\"pack\\": {\\n" +
                "    \\"pack_format\\": 48,\\n" +
                "    \\"description\\": \\"Hel\\"\\n" +
                "  }\\n" +
                "}");
    }'''

if old_mcmeta in c:
    c = c.replace(old_mcmeta, new_mcmeta)
else:
    print("Failed to replace old_mcmeta")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

