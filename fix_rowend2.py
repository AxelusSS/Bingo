import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_func = '''    private void createTechnicalRowEnd(File dir, int row, int size, String parent) {
        saveFile(dir, "row_end_" + row + ".json",
                "{\\n" +
                "  \\"parent\\": \\"" + parent + "\\",\\n" +
                "  \\"criteria\\": {\\n" +
                "    \\"auto\\": { \\"trigger\\": \\"minecraft:impossible\\" }\\n" +
                "  }\\n" +
                "}");
    }'''

new_func = '''    private void createTechnicalRowEnd(File dir, int row, int size, String parent) {
        saveFile(dir, "row_end_" + row + ".json",
                "{\\n" +
                "  \\"parent\\": \\"" + parent + "\\",\\n" +
                "  \\"criteria\\": {\\n" +
                "    \\"auto\\": { \\"trigger\\": \\"minecraft:tick\\" }\\n" +
                "  }\\n" +
                "}");
    }'''

c = c.replace(old_func, new_func)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
