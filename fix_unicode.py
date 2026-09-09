import os, re

def escape_unicode(match):
    char = match.group(0)
    return f'\\u{ord(char):04X}'

for root, dirs, files in os.walk('src/main/java'):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            try:
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                with open(path, 'r', encoding='latin-1') as f:
                    content = f.read()
            # Find all non-ascii characters and replace them with \uXXXX
            new_content = re.sub(r'[^\x00-\x7F]', escape_unicode, content)
            if new_content != content:
                with open(path, 'w', encoding='ascii') as f:
                    f.write(new_content)

