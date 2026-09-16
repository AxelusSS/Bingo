import os
for root, dirs, files in os.walk('src/main/java/fr/hel/'):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            with open(path, 'rb') as f:
                content = f.read()
            if content.startswith(b'\xef\xbb\xbf'):
                content = content[3:]
                with open(path, 'wb') as f:
                    f.write(content)
