import sys

with open('pom.xml', 'r', encoding='utf-8') as f:
    pom = f.read()
pom = pom.replace('<version>1.5.1</version>', '<version>1.6.0</version>')
with open('pom.xml', 'w', encoding='utf-8') as f:
    f.write(pom)

doc_path = r"C:\Users\louis\.gemini\antigravity\brain\1cbc4e6e-f70e-480b-b0fd-84a9acaf3d42\hel_documentation.md"
with open(doc_path, 'r', encoding='utf-8') as f:
    doc = f.read()
doc = doc.replace('V1.5.1/bingo-1.5.1.jar', 'V1.6.0/bingo-1.6.0.jar')
doc = doc.replace('V1.5.1/BingoPack.zip', 'V1.6.0/BingoPack.zip')
doc = doc.replace('V1.5.1/server-icon.png', 'V1.6.0/server-icon.png')
with open(doc_path, 'w', encoding='utf-8') as f:
    f.write(doc)
