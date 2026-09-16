import sys

path = 'src/main/java/fr/hel/commands/JumpCommand.java'
with open(path, 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace('HelPlugin.getInstance().getHelGame().getJumpManager().startJump(p);', '''p.sendMessage("\u00A7aLancement du parkour...");
        try {
            HelPlugin.getInstance().getHelGame().getJumpManager().startJump(p);
        } catch (Exception e) {
            p.sendMessage("\u00A7cErreur: " + e.getMessage());
            e.printStackTrace();
        }''')

with open(path, 'w', encoding='utf-8') as f:
    f.write(c)

