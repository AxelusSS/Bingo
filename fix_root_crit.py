import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_crit = '''  \\"criteria\\": {\\n" +
                "    \\"auto\\": { \\"trigger\\": \\"minecraft:tick\\" }\\n" +'''

new_crit = '''  \\"criteria\\": {\\n" +
                "    \\"auto\\": { \\"trigger\\": \\"minecraft:impossible\\" }\\n" +'''

if old_crit in c:
    c = c.replace(old_crit, new_crit)
else:
    print("Failed to replace old_crit")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

