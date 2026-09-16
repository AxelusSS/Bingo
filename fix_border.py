import sys
with open('src/main/java/fr/hel/game/BorderManager.java', 'r', encoding='utf-8') as f:
    c = f.read()
c = c.replace('HelPlugin.getInstance().getHelGame().isHelMode()', 'HelPlugin.getInstance().getScenarioManager().isScenarioEnabled(fr.hel.scenario.HelScenario.class)')
with open('src/main/java/fr/hel/game/BorderManager.java', 'w', encoding='utf-8') as f:
    f.write(c)

with open('src/main/java/fr/hel/game/HelGame.java', 'r', encoding='utf-8') as f:
    c = f.read()
c = c.replace('if (isHelMode) {', 'if (HelPlugin.getInstance().getScenarioManager().isScenarioEnabled(fr.hel.scenario.HelScenario.class)) {', 1) # Only for border, wait, I already used isHelMode in start() but my replacement used isHelMode where it was not defined? No, my replacement was inside startGame() so isHelMode IS defined! Let's check.
