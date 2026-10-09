import sys

path = 'src/main/java/fr/hel/listeners/HelListener.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_block = '''        // D\u00E9tection d'items pour le Hel (si dans l'inventaire du joueur)
        if (HelPlugin.getInstance().getHelGame().getState() == GameState.PLAYING) {
            ItemStack clicked = event.getCurrentItem();'''

new_block = '''        // D\u00E9tection d'items pour le Hel (si dans l'inventaire du joueur)
        if (HelPlugin.getInstance().getHelGame().getState() == GameState.PLAYING && !event.isCancelled()) {
            ItemStack clicked = event.getCurrentItem();'''

if old_block in c:
    c = c.replace(old_block, new_block)
else:
    print("Block not found!")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

