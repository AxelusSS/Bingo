import sys
import re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# find createItemAdvancement
match = re.search(r'private void createItemAdvancement.*?\}', c, re.DOTALL)
if match:
    print(match.group(0))
else:
    print("Not found")

