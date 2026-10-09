import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('"minecraft:gui/advancements/backgrounds/stone"', '"minecraft:block/light_blue_concrete_powder"')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
