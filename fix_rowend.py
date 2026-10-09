import sys
import re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Modifier createTechnicalRowEnd
c = re.sub(
    r'private void createTechnicalRowEnd.*?\{.*?saveFile.*?\"row_end_\" \+ row \+ \"\.json\",.*?\"\{\\n\" \+.*?\"  \\\"parent\\\": \\\"\" \+ parent \+ \"\\\",\\n\" \+.*?\"  \\\"criteria\\\": \{\\n\" \+.*?\"    \\\"auto\\\": \{ \\\"trigger\\\": \\\"minecraft:impossible\\\" \}\\n\" \+.*?\"  \}\\n\" \+.*?\"\}\"\);.*?\}',
    '''private void createTechnicalRowEnd(File dir, int row, int size, String parent) {
        saveFile(dir, "row_end_" + row + ".json",
                "{\\n" +
                "  \\"parent\\": \\"" + parent + "\\",\\n" +
                "  \\"criteria\\": {\\n" +
                "    \\"auto\\": { \\"trigger\\": \\"minecraft:tick\\" }\\n" +
                "  }\\n" +
                "}");
    }''',
    c,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
