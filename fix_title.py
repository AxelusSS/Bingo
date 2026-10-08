import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('\"title\": { \"text\": \"Hel Classique\" }', '\"title\": { \"text\": \"Bingo\" }')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

