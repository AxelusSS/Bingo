import sys

gui_path = 'src/main/java/fr/hel/gui/HelConfigGUI.java'
with open(gui_path, 'r', encoding='utf-8') as f:
    c = f.read()

# Replace rendering of slot 14
old_render = '''        // Slot 14 : Mode
        HelMode mode = game.getMode();
        Material modeMat = switch (mode) {
            case ITEMS -> Material.CHEST;
            case ACHIEVEMENTS -> Material.DRAGON_EGG;
            case MIXED -> Material.ENDER_CHEST;
        };
        inventory.setItem(14, createItem(modeMat, "\u00A7e\u00A7lMode : " + mode.getColor() + mode.getDisplayName(),
                List.of("\u00A77Clic pour changer",
                        "",
                        (mode == HelMode.ITEMS ? "\u00A7b\u25B8 " : "\u00A77  ") + "Items",
                        (mode == HelMode.ACHIEVEMENTS ? "\u00A7d\u25B8 " : "\u00A77  ") + "Achievements",
                        (mode == HelMode.MIXED ? "\u00A76\u25B8 " : "\u00A77  ") + "Mixte")));'''

new_render = '''        // Slot 13 & 15 : Mode (2 livres s\u00E9par\u00E9s)
        HelMode mode = game.getMode();
        inventory.setItem(13, createItem(Material.BOOK, (mode == HelMode.ITEMS ? "\u00A7b\u00A7l\u25B8 " : "\u00A77") + "Mode Items",
                List.of("\u00A77Jouer avec une grille d'items", "", "\u00A7e\u25BA Clic pour s\u00E9lectionner")));
        inventory.setItem(15, createItem(Material.KNOWLEDGE_BOOK, (mode == HelMode.ACHIEVEMENTS ? "\u00A7d\u00A7l\u25B8 " : "\u00A77") + "Mode Achievements",
                List.of("\u00A77Jouer avec une grille d'achievements", "", "\u00A7e\u25BA Clic pour s\u00E9lectionner")));
        inventory.setItem(14, null);'''

c = c.replace(old_render, new_render)

old_click = '''            case 14 -> { // Mode
                game.setMode(game.getMode().next());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }'''

new_click = '''            case 13 -> { // Mode Items
                game.setMode(fr.hel.game.HelMode.ITEMS);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }
            case 15 -> { // Mode Achievements
                game.setMode(fr.hel.game.HelMode.ACHIEVEMENTS);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
                refresh(player);
            }'''

c = c.replace(old_click, new_click)

with open(gui_path, 'w', encoding='utf-8') as f:
    f.write(c)

plugin_path = 'src/main/java/fr/hel/HelPlugin.java'
with open(plugin_path, 'r', encoding='utf-8') as f:
    p = f.read()

pregen_code = '''
        // Lancer la pr\u00E9-g\u00E9n\u00E9ration Chunky apr\u00E8s 5 secondes (le temps que tout charge)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getPluginManager().getPlugin("Chunky") != null) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius 3000");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start");
                getLogger().info("Pr\u00E9-g\u00E9n\u00E9ration Chunky lanc\u00E9e automatiquement (Rayon: 3000) !");
            }
        }, 100L);
'''

p = p.replace('getLogger().info("[HEL] Plugin actif !");', 'getLogger().info("[HEL] Plugin actif !");' + pregen_code)

with open(plugin_path, 'w', encoding='utf-8') as f:
    f.write(p)

