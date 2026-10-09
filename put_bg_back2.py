import sys
import re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('"    \\"title\\": { \\"text\\": \\"Bingo\\" },\\n" +', '"    \\"title\\": { \\"text\\": \\"Bingo\\" },\\n" +\n                "    \\"background\\": \\"minecraft:textures/gui/advancements/backgrounds/stone.png\\",\\n" +')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

