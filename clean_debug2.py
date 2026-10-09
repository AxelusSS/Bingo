import sys
import re

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Suppression stricte des messages de debug
c = re.sub(r'Bukkit\.broadcastMessage\(".*?\[DEBUG\].*?"\);\n', '', c)
c = re.sub(r'Bukkit\.broadcastMessage\(".*? - " \+ adv.*?Key\(\).*?toString\(\)\);\n', '', c)
c = re.sub(r'Bukkit\.broadcastMessage\(".*?Total.*?trouv.s : " \+ count\);\n', '', c)

# Et les listes des datapacks scann.s
c = re.sub(r'Bukkit\.broadcastMessage\(".*?Liste des datapacks scann.s.*?"\);\n', '', c)
c = re.sub(r'Bukkit\.dispatchCommand\(Bukkit\.getConsoleSender\(\), "datapack list"\);\n', '', c)

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

