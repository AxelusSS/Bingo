import re

def fix(path, pattern, repl):
    with open(path, 'r', encoding='ascii') as f:
        content = f.read()
    content = re.sub(pattern, repl, content)
    with open(path, 'w', encoding='ascii') as f:
        f.write(content)

fix('src/main/java/fr/hel/scenario/EternalDayScenario.java', r'Le soleil reste bloqu.*midi\.', 'Le soleil reste bloqu\\\\u00E9 \\\\u00E0 midi.')
fix('src/main/java/fr/hel/scenario/SwitchInventoryScenario.java', 'Echange d', '\\\\u00C9change d')
fix('src/main/java/fr/hel/scenario/SwitchScenario.java', 'Echange de', '\\\\u00C9change de')

