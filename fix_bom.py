import os, glob
for root, dirs, files in os.walk('src/main/java'):
    for file in files:
        if file.endswith('.java'):
            path = os.path.join(root, file)
            with open(path, 'rb') as f:
                content = f.read()
            if content.startswith(b'\xef\xbb\xbf'):
                with open(path, 'wb') as f:
                    f.write(content[3:])

