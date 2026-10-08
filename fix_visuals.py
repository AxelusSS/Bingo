import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

old_json = '''                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:nether_star\" },\n" +
                "    \"title\": { \"text\": \"Hel Classique\" },\n" +
                "    \"description\": { \"text\": \"Utilisez /bg pour voir votre progression\" },\n" +
                "    \"background\": \"minecraft:textures/block/light_blue_concrete_powder.png\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n"'''

new_json = '''                "  \"display\": {\n" +
                "    \"icon\": { \"id\": \"minecraft:nether_star\" },\n" +
                "    \"title\": { \"text\": \"Bingo\" },\n" +
                "    \"description\": { \"text\": \"Utilisez /bg pour voir votre progression\" },\n" +
                "    \"background\": \"minecraft:textures/gui/advancements/backgrounds/stone.png\",\n" +
                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n"'''

if old_json in c:
    c = c.replace(old_json, new_json)
    print("Replaced successfully")
else:
    print("Could not find the target string")

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)
