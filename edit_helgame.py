import sys
with open('src/main/java/fr/hel/game/HelGame.java', 'r', encoding='utf-8') as f:
    c = f.read()

# 1. Platform bounds
c = c.replace('for (int x = -10; x <= 10; x++)', 'for (int x = -16; x <= 16; x++)')
c = c.replace('for (int z = -10; z <= 10; z++)', 'for (int z = -16; z <= 16; z++)')

# 2. Border
border_old = '''        // \u2500\u2500 Bordure \u2500\u2500
        if (borderEnabled) {
            HelPlugin.getInstance().getBorderManager().startBorder();
        } else {
            // S'assurer que la bordure est grande si d\u00E9sactiv\u00E9e
            World bw = Bukkit.getWorlds().get(0);
            bw.getWorldBorder().setSize(10000);
        }'''
border_new = '''        // \u2500\u2500 Bordure \u2500\u2500
        if (isHelMode) {
            World bw = Bukkit.getWorlds().get(0);
            bw.getWorldBorder().setCenter(0, 0);
            bw.getWorldBorder().setSize(HelPlugin.getInstance().getBorderManager().getInitialSize());
            bw.getWorldBorder().setDamageAmount(1.0);
            bw.getWorldBorder().setDamageBuffer(5.0);
            bw.getWorldBorder().setWarningDistance(20);
        } else if (borderEnabled) {
            HelPlugin.getInstance().getBorderManager().startBorder();
        } else {
            World bw = Bukkit.getWorlds().get(0);
            bw.getWorldBorder().setSize(10000);
        }'''
c = c.replace(border_old, border_new)

# 3. Firework
firework_old = '''                                        // Feu d'artifice
                                        org.bukkit.entity.Firework fw = tp.getWorld().spawn(tp.getLocation(), org.bukkit.entity.Firework.class);
                                        org.bukkit.inventory.meta.FireworkMeta fwm = fw.getFireworkMeta();
                                        org.bukkit.Color color = org.bukkit.Color.GREEN;
                                        if (!tm.isSoloMode() && team.getChatColor() != null) {
                                            switch (team.getChatColor().name()) {
                                                case "RED": color = org.bukkit.Color.RED; break;
                                                case "BLUE": color = org.bukkit.Color.BLUE; break;
                                                case "GREEN": color = org.bukkit.Color.LIME; break;
                                                case "YELLOW": color = org.bukkit.Color.YELLOW; break;
                                                case "AQUA": color = org.bukkit.Color.AQUA; break;
                                                case "LIGHT_PURPLE": color = org.bukkit.Color.FUCHSIA; break;
                                                case "GOLD": color = org.bukkit.Color.ORANGE; break;
                                            }
                                        }
                                        fwm.addEffect(org.bukkit.FireworkEffect.builder().withColor(color).with(org.bukkit.FireworkEffect.Type.BALL_LARGE).build());
                                        fwm.setPower(0);
                                        fw.setFireworkMeta(fwm);
                                        fw.detonate();'''
c = c.replace(firework_old, '')

with open('src/main/java/fr/hel/game/HelGame.java', 'w', encoding='utf-8') as f:
    f.write(c)
