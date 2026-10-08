import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Remove x and y from createItemAdvancement
old_item = '''                "    \\"hidden\\": false,\\n" +
                "    \\"x\\": " + (double) (col * 1.5) + ",\\n" +
                "    \\"y\\": " + (double) (row * 1.5) + "\\n" +
                "  },\\n" +'''
new_item = '''                "    \\"hidden\\": false\\n" +
                "  },\\n" +'''
if old_item in c:
    c = c.replace(old_item, new_item)
else:
    print("Failed to replace old_item")

# Remove x and y from createRootAdvancement
old_root = '''                "    \\"hidden\\": false,\\n" +
                "    \\"x\\": -1.5,\\n" +
                "    \\"y\\": 3.0\\n" +
                "  },\\n" +'''
new_root = '''                "    \\"hidden\\": false\\n" +
                "  },\\n" +'''
if old_root in c:
    c = c.replace(old_root, new_root)
else:
    print("Failed to replace old_root")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
