package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Scenario implements Listener {

    private final String name;
    private final Material icon;
    private final String description;
    private boolean enabled;

    public Scenario(String name, Material icon, String description, boolean defaultEnabled) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.enabled = defaultEnabled;
    }

    public String getName() { return name; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }
    
    public boolean isEnabled() { return enabled; }
    
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
            HelPlugin.getInstance().getServer().getPluginManager().registerEvents(this, HelPlugin.getInstance());
        } else {
            onDisable();
            HandlerList.unregisterAll(this);
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Appel\u00E9 lorsque le sc\u00E9nario est activ\u00E9 dans les menus.
     */
    public void onEnable() {}

    /**
     * Appel\u00E9 lorsque le sc\u00E9nario est d\u00E9sactiv\u00E9 dans les menus.
     */
    public void onDisable() {}

    /**
     * Appel\u00E9 au lancement de la partie (GameState.PLAYING).
     * Utile pour donner des items, effets initiaux, etc.
     */
    public void onGameStart() {}
    
    /**
     * Appel\u00E9 quand on fait clic-droit sur l'item dans le menu des sc\u00E9narios.
     * @param player le joueur qui a cliqu\u00E9
     */
    public void onRightClick(Player player) {}
    
    /**
     * @return true si le sc\u00E9nario poss\u00E8de un sous-menu de configuration (ouvre au clic droit).
     */
    public boolean hasConfigMenu() { return false; }
}
