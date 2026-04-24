package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
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
            BingoPlugin.getInstance().getServer().getPluginManager().registerEvents(this, BingoPlugin.getInstance());
        } else {
            onDisable();
            HandlerList.unregisterAll(this);
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    /**
     * Appelé lorsque le scénario est activé dans les menus.
     */
    public void onEnable() {}

    /**
     * Appelé lorsque le scénario est désactivé dans les menus.
     */
    public void onDisable() {}

    /**
     * Appelé au lancement de la partie (GameState.PLAYING).
     * Utile pour donner des items, effets initiaux, etc.
     */
    public void onGameStart() {}
    
    /**
     * Appelé quand on fait clic-droit sur l'item dans le menu des scénarios.
     * @param player le joueur qui a cliqué
     */
    public void onRightClick(Player player) {}
    
    /**
     * @return true si le scénario possède un sous-menu de configuration (ouvre au clic droit).
     */
    public boolean hasConfigMenu() { return false; }
}
