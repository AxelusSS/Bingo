import sys
import re

pool_path = 'src/main/java/fr/hel/game/HelObjectivePool.java'
with open(pool_path, 'r', encoding='utf-8') as f:
    c = f.read()

# Add a category for 26.x items if needed, or just NONE
items_to_add = '''
        // --- 26.x ITEMS ---
        pool.add(new PoolEntry(Material.SULFUR, Difficulty.MEDIUM));
        pool.add(new PoolEntry(Material.SPEAR, Difficulty.HARD));
'''

c = c.replace('return pool;', items_to_add + '\n        return pool;')

with open(pool_path, 'w', encoding='utf-8') as f:
    f.write(c)

