package fr.hel.gui;

import fr.hel.HelPlugin;
import fr.hel.scenario.AnonymousScenario;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.destroystokyo.paper.profile.PlayerProfile;

import java.util.ArrayList;
import java.util.List;

public class AnonymousConfigGUI implements InventoryHolder {
    private final Inventory inv;
    private final AnonymousScenario scenario;

    public AnonymousConfigGUI(AnonymousScenario scenario) {
        this.scenario = scenario;
        this.inv = Bukkit.createInventory(this, 27, "Configuration: Anonymous");
        populate();
    }

    private void populate() {
        inv.clear();

        // Glitched Names
        ItemStack glitch = new ItemStack(Material.NAME_TAG);
        ItemMeta gm = glitch.getItemMeta();
        gm.setDisplayName("\u00A7eOption 1: Noms Glitch\u00E9s");
        List<String> gl = new ArrayList<>();
        gl.add("\u00A77Rend les pseudos illisibles (\u00A7k12345678)");
        gl.add("\u00A77Masque les couleurs d'\u00E9quipes.");
        gl.add(" ");
        gl.add(scenario.isGlitchedNames() ? "\u00A7a\u25B6 Activ\u00E9" : "\u00A7c\u25B6 D\u00E9sactiv\u00E9");
        gm.setLore(gl);
        glitch.setItemMeta(gm);
        inv.setItem(11, glitch);

        // Same Skin
        ItemStack skin = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta sm = skin.getItemMeta();
        sm.setDisplayName("\u00A7eOption 2: M\u00EAme Skin");
        List<String> sl = new ArrayList<>();
        sl.add("\u00A77Tous les joueurs auront le m\u00EAme skin.");
        sl.add(" ");
        sl.add(scenario.isSameSkin() ? "\u00A7a\u25B6 Activ\u00E9" : "\u00A7c\u25B6 D\u00E9sactiv\u00E9");
        if (scenario.getSharedSkin() != null) {
            sl.add("\u00A77Skin actuel d\u00E9fini.");
        } else {
            sl.add("\u00A7cAucun skin d\u00E9fini.");
        }
        sl.add("\u00A7eClic-droit: D\u00E9finir ton skin comme mod\u00E8le");
        sm.setLore(sl);
        skin.setItemMeta(sm);
        inv.setItem(15, skin);

        // Retour
        ItemStack retour = new ItemStack(Material.ARROW);
        ItemMeta rm = retour.getItemMeta();
        rm.setDisplayName("\u00A7cRetour");
        retour.setItemMeta(rm);
        inv.setItem(26, retour);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public void handleClick(Player p, int slot, boolean rightClick) {
        if (slot == 11) {
            scenario.setGlitchedNames(!scenario.isGlitchedNames());
            populate();
        } else if (slot == 15) {
            if (rightClick) {
                scenario.setSharedSkin(p.getPlayerProfile());
                p.sendMessage("\u00A7a[Anonymous] \u00A7fLe skin de tout le monde sera le tien au lancement !");
                scenario.setSameSkin(true);
            } else {
                scenario.setSameSkin(!scenario.isSameSkin());
            }
            populate();
        } else if (slot == 26) {
            p.closeInventory();
            p.openInventory(new ScenarioConfigGUI().getInventory());
        }
    }
}

