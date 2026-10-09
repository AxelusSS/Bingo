import sys

path = 'src/main/java/fr/hel/listeners/HelListener.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('if (HelPlugin.getInstance().getHelGame().getState() == GameState.PLAYING) {\n            ItemStack clicked = event.getCurrentItem();', 'if (HelPlugin.getInstance().getHelGame().getState() == GameState.PLAYING && !event.isCancelled()) {\n            ItemStack clicked = event.getCurrentItem();')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

