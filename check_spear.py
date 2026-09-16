import sys
import zipfile
import re

# Find paper-api jar in .m2
import os
m2_path = os.path.expanduser('~/.m2/repository/io/papermc/paper/paper-api')
jar_path = None
for root, dirs, files in os.walk(m2_path):
    for file in files:
        if file.endswith('.jar') and '26.2' in file:
            jar_path = os.path.join(root, file)
            break
if not jar_path:
    print("Could not find Paper API jar.")
    sys.exit(1)

# Unfortunately we can't easily extract classes to parse enum without JVM.
# But we can extract the Material.class and run strings on it!
import subprocess
import shutil

os.makedirs('temp_jar', exist_ok=True)
with zipfile.ZipFile(jar_path, 'r') as z:
    z.extract('org/bukkit/Material.class', 'temp_jar')

# run strings or powershell equivalent
print("Extracted Material.class")
