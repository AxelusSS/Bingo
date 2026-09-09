import os, glob

src_dir = 'src/main/java/fr'

# Rename directory
os.rename(os.path.join(src_dir, 'bingo'), os.path.join(src_dir, 'hel'))

renames = {
    'fr.bingo': 'fr.hel',
    'BingoGame': 'HelGame',
    'BingoPlugin': 'HelPlugin',
    'BingoListener': 'HelListener',
    'BingoTeam': 'HelTeam',
    'BingoObjective': 'HelObjective',
    'BingoGrid': 'HelGrid',
    'BingoConfigGUI': 'HelConfigGUI',
    'BingoScenario': 'HelScenario',
    'BingoScoreboard': 'HelScoreboard',
    'BingoMode': 'HelMode',
    'Bingo': 'Hel'
}

files_to_process = []
for root, _, files in os.walk('.'):
    if 'target' in root or '.git' in root:
        continue
    for file in files:
        if file.endswith(('.java', '.yml', '.xml')):
            files_to_process.append(os.path.join(root, file))

for filepath in files_to_process:
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        for k, v in renames.items():
            content = content.replace(k, v)
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
    except Exception as e:
        print(f'Error processing {filepath}: {e}')

# Rename files
for root, _, files in os.walk('.'):
    if 'target' in root or '.git' in root:
        continue
    for file in files:
        if file.startswith('Bingo') and file.endswith('.java'):
            new_name = file.replace('Bingo', 'Hel')
            os.rename(os.path.join(root, file), os.path.join(root, new_name))

