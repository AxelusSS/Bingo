import sys

path = 'src/main/java/fr/hel/game/DatapackManager.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

# Restore x and y for root
old_root = '''                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:impossible\" }\n"'''
new_root = '''                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false,\n" +
                "    \"x\": -1.5,\n" +
                "    \"y\": 3.0\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:impossible\" }\n"'''
c = c.replace(old_root, new_root)

# Restore x and y for item (there are 2 places where it might exist, let's just do a specific search)
old_item = '''                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"found\": { \"trigger\": \"minecraft:impossible\" }\n"'''
new_item = '''                "    \"show_toast\": false,\n" +
                "    \"announce_to_chat\": false,\n" +
                "    \"hidden\": false,\n" +
                "    \"x\": " + (double) (col * 1.5) + ",\n" +
                "    \"y\": " + (double) (row * 1.5) + "\n" +
                "  },\n" +
                "  \"criteria\": {\n" +
                "    \"found\": { \"trigger\": \"minecraft:impossible\" }\n"'''
c = c.replace(old_item, new_item)

# Restore x and y for row end
old_row = '''                "  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:impossible\" }\n" +
                "  }\n" +
                "}");'''
new_row = '''  \"criteria\": {\n" +
                "    \"auto\": { \"trigger\": \"minecraft:impossible\" }\n" +
                "  }\n" +
                "}");'''
# Wait, for row end, it didn't have display? Let's check original.
# The original row end did not have display.

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

