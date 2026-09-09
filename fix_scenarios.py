import re

def replace_in_file(path, old, new):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace(old, new)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

replace_in_file('src/main/java/fr/hel/scenario/EternalDayScenario.java', '7Le soleil reste bloqu  midi.', 'Le soleil reste bloqu\u00e9 \u00e0 midi.')
replace_in_file('src/main/java/fr/hel/scenario/SwitchInventoryScenario.java', 'Echange d', '\u00c9change d')
replace_in_file('src/main/java/fr/hel/scenario/SwitchScenario.java', 'Echange de', '\u00c9change de')
replace_in_file('src/main/java/fr/hel/scenario/HelScenario.java', '"Hel"', '"Bingo"')
replace_in_file('src/main/java/fr/hel/scenario/HelScenario.java', 'Active le mode Hel', 'Active le mode Bingo')

