import sys
import re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Suppression de l'ancienne ligne de background pour le root
c = re.sub(r'\"    \\\"background\\\": \\\".*?\\\",\\n\" \+\s*', '', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
