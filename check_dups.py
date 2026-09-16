with open('src/main/java/fr/hel/game/HelObjectivePool.java', 'r', encoding='utf-8') as f:
    content = f.read()
import re
items = re.findall(r'PoolEntry\(Material\.([A-Z_]+)', content)
from collections import Counter
counts = Counter(items)
for item, count in counts.items():
    if count > 1:
        print(f"Duplicate found: {item}")
