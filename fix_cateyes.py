import sys

path = 'src/main/java/fr/hel/scenario/CatEyesScenario.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('200, 0, false, false, false', '1200, 0, false, false, false')
c = c.replace('10 secondes (200 ticks)', '60 secondes (1200 ticks)')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
