import re

def fix(path, pattern, repl):
    try:
        with open(path, 'r', encoding='utf-8') as f: content = f.read()
    except:
        with open(path, 'r', encoding='latin-1') as f: content = f.read()
    content = re.sub(pattern, repl, content)
    def escape_unicode(match):
        char = match.group(0)
        return f'\\\\u{ord(char):04X}'
    content = re.sub(r'[^\x00-\x7F]', escape_unicode, content)
    with open(path, 'w', encoding='ascii') as f:
        f.write(content)

fix('src/main/java/fr/hel/scenario/EternalDayScenario.java', r'Le soleil reste bloqu.*midi\.', 'Le soleil reste bloqu\\\\u00E9 \\\\u00E0 midi.')
fix('src/main/java/fr/hel/scenario/SwitchInventoryScenario.java', 'Echange d', '\\\\u00C9change d')
fix('src/main/java/fr/hel/scenario/SwitchScenario.java', 'Echange de', '\\\\u00C9change de')

