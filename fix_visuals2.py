import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('"title": { "text": "Hel Classique" }', '"title": { "text": "Bingo" }')
c = c.replace('minecraft:textures/block/light_blue_concrete_powder.png', 'minecraft:textures/gui/advancements/backgrounds/adventure.png')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

