import sys

# 1. Update plugin.yml
yml_path = 'src/main/resources/plugin.yml'
with open(yml_path, 'r', encoding='utf-8') as f:
    yml = f.read()
yml = yml.replace('  jump:', '  parkour:')
with open(yml_path, 'w', encoding='utf-8') as f:
    f.write(yml)

# 2. Update HelPlugin.java
plugin_path = 'src/main/java/fr/hel/HelPlugin.java'
with open(plugin_path, 'r', encoding='utf-8') as f:
    plug = f.read()
plug = plug.replace('getCommand("jump").setExecutor', 'getCommand("parkour").setExecutor')
with open(plugin_path, 'w', encoding='utf-8') as f:
    f.write(plug)

# 3. Update Hologram in HelGame.java
game_path = 'src/main/java/fr/hel/game/HelGame.java'
with open(game_path, 'r', encoding='utf-8') as f:
    game = f.read()
game = game.replace('faites /jump', 'faites /parkour')
with open(game_path, 'w', encoding='utf-8') as f:
    f.write(game)

