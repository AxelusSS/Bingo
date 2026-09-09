import re
def fix_file(path):
    with open(path, 'r', encoding='ascii') as f:
        content = f.read()
    content = content.replace('\\u00C2\\u00A7', '\\u00A7')
    content = content.replace('\\u00C3\\u00A9', '\\u00E9')
    content = content.replace('\\u00C3\\u00A8', '\\u00E8')
    content = content.replace('\\u00C3\\u00AA', '\\u00EA')
    content = content.replace('\\u00E2\\u2013\\u00B6', '\\u25B6')
    with open(path, 'w', encoding='ascii') as f:
        f.write(content)
fix_file('src/main/java/fr/hel/gui/AnonymousConfigGUI.java')
fix_file('src/main/java/fr/hel/scenario/AnonymousScenario.java')

