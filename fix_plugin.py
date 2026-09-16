import sys

plugin_path = 'src/main/java/fr/hel/HelPlugin.java'
with open(plugin_path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('getCommand("hel").setExecutor(new HelCommand());', 'getCommand("hel").setExecutor(new HelCommand());\n        getCommand("jump").setExecutor(new fr.hel.commands.JumpCommand());')

with open(plugin_path, 'w', encoding='utf-8') as f:
    f.write(c)

yml_path = 'src/main/resources/plugin.yml'
with open(yml_path, 'r', encoding='utf-8') as f:
    yml = f.read()

yml = yml.replace('  team:', '  jump:\n    description: Commencer le jump\n  team:')

with open(yml_path, 'w', encoding='utf-8') as f:
    f.write(yml)
