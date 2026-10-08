import sys
import re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Pour la root
c = re.sub(
    r'"    \\"hidden\\": false\\n" \+\s*"  },\\n" \+\s*"  \\"criteria\\": \{\\n" \+\s*"    \\"auto\\": \{ \\"trigger\\": \\"minecraft:impossible\\" \}\\n"',
    r'"    \\"hidden\\": false,\\n" +\n                "    \\"x\\": -1.5,\\n" +\n                "    \\"y\\": 3.0\\n" +\n                "  },\\n" +\n                "  \\"criteria\\": {\\n" +\n                "    \\"auto\\": { \\"trigger\\": \\"minecraft:impossible\\" }\\n"',
    c
)

# Pour les items
c = re.sub(
    r'"    \\"hidden\\": false\\n" \+\s*"  },\\n" \+\s*"  \\"criteria\\": \{\\n" \+\s*"    \\"found\\": \{ \\"trigger\\": \\"minecraft:impossible\\" \}\\n"',
    r'"    \\"hidden\\": false,\\n" +\n                "    \\"x\\": " + (double) (col * 1.5) + ",\\n" +\n                "    \\"y\\": " + (double) (row * 1.5) + "\\n" +\n                "  },\\n" +\n                "  \\"criteria\\": {\\n" +\n                "    \\"found\\": { \\"trigger\\": \\"minecraft:impossible\\" }\\n"',
    c
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

