import sys

listener_path = 'src/main/java/fr/hel/listeners/HelListener.java'
with open(listener_path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('if (game.getState() == fr.hel.game.GameState.WAITING || game.getState() == fr.hel.game.GameState.STARTING) {', 'if (game.getState() == fr.hel.game.GameState.WAITING) {')

with open(listener_path, 'w', encoding='utf-8') as f:
    f.write(c)
