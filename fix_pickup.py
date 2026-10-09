import sys

path = 'src/main/java/fr/hel/listeners/HelListener.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('@EventHandler\n    public void onPickup', '@EventHandler(ignoreCancelled = true)\n    public void onPickup')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

