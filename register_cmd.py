import sys

path = 'src/main/java/fr/hel/HelPlugin.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('getCommand("bingo").setExecutor(bingoCmd);', 'getCommand("bingo").setExecutor(bingoCmd);\n        getCommand("parkour").setExecutor(new fr.hel.commands.JumpCommand());')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

