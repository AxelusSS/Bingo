import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('minecraft:textures/gui/advancements/backgrounds/adventure.png', 'minecraft:textures/gui/advancements/backgrounds/stone.png')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
