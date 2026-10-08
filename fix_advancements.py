import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix pack.mcmeta
old_mcmeta = '"{ \\"pack\\": { \\"pack_format\\": 71, \\"supported_formats\\": [48, 71], \\"description\\": \\"Hel\\" } }"'
new_mcmeta = '"{ \\"pack\\": { \\"pack_format\\": 48, \\"supported_formats\\": {\\"min_inclusive\\": 48, \\"max_inclusive\\": 99}, \\"description\\": \\"Hel\\" } }"'
c = c.replace(old_mcmeta, new_mcmeta)

# Fix title/description in createItemAdvancement
old_item = '''                "    \\"title\\": \\"" + name + "\\",\\n" +
                "    \\"description\\": \\"" + desc + "\\",\\n" +'''
new_item = '''                "    \\"title\\": { \\"text\\": \\"" + name + "\\" },\\n" +
                "    \\"description\\": { \\"text\\": \\"" + desc + "\\" },\\n" +'''
c = c.replace(old_item, new_item)

# Fix title/description in createRootAdvancement
old_root = '''                "    \\"title\\": \\"Hel Classique\\",\\n" +
                "    \\"description\\": \\"Utilisez /bg pour voir votre progression\\",\\n" +'''
new_root = '''                "    \\"title\\": { \\"text\\": \\"Hel Classique\\" },\\n" +
                "    \\"description\\": { \\"text\\": \\"Utilisez /bg pour voir votre progression\\" },\\n" +'''
c = c.replace(old_root, new_root)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

