import sys

yml_path = 'src/main/resources/plugin.yml'
with open(yml_path, 'r', encoding='utf-8') as f:
    yml = f.read()

yml = yml.replace('  parkour:\n    description: Commencer le jump\n  team:', '  parkour:\n    description: Commencer le jump\n    aliases: [jump]\n  team:')

with open(yml_path, 'w', encoding='utf-8') as f:
    f.write(yml)
