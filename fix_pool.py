import sys
import re

pool_path = 'src/main/java/fr/hel/game/HelObjectivePool.java'
with open(pool_path, 'r', encoding='utf-8') as f:
    c = f.read()

# Fix difficultes
c = c.replace('pool.add(new PoolEntry(Material.NAME_TAG, Difficulty.EXTREME));', 'pool.add(new PoolEntry(Material.NAME_TAG, Difficulty.EASY));')
c = c.replace('pool.add(new PoolEntry(Material.LEATHER_HORSE_ARMOR, Difficulty.MEDIUM));', 'pool.add(new PoolEntry(Material.LEATHER_HORSE_ARMOR, Difficulty.EASY));')
c = c.replace('pool.add(new PoolEntry(Material.LEATHER_HORSE_ARMOR, Difficulty.HARD));', 'pool.add(new PoolEntry(Material.LEATHER_HORSE_ARMOR, Difficulty.EASY));')
c = c.replace('pool.add(new PoolEntry(Material.LEATHER_HORSE_ARMOR, Difficulty.EXTREME));', 'pool.add(new PoolEntry(Material.LEATHER_HORSE_ARMOR, Difficulty.EASY));')
c = c.replace('pool.add(new PoolEntry(Material.SADDLE, Difficulty.EXTREME));', 'pool.add(new PoolEntry(Material.SADDLE, Difficulty.EASY));')

# Remove duplicates
lines = c.split('\n')
new_lines = []
seen_pool_adds = set()
for line in lines:
    if 'pool.add(new PoolEntry(' in line:
        # Extract the Material or achievement string
        match = re.search(r'PoolEntry\(([^,]+),', line)
        if match:
            item_id = match.group(1).strip()
            if item_id in seen_pool_adds:
                continue # Duplicate found, skip this line
            seen_pool_adds.add(item_id)
    new_lines.append(line)

with open(pool_path, 'w', encoding='utf-8') as f:
    f.write('\n'.join(new_lines))

