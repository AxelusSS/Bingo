package fr.hel.scenario;
import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
public class AnonymousScenario extends Scenario implements Listener {
    private boolean glitchedNames = false;
    private boolean sameSkin = false;
    private PlayerProfile sharedSkinProfile = null;
    private final List<UUID> originalSkinsSaved = new ArrayList<>();
    public AnonymousScenario() {
        super("Anonymous", Material.SKELETON_SKULL, "Rend les joueurs anonymes.", false);
    }
    public boolean isGlitchedNames() { return glitchedNames; }
    public void setGlitchedNames(boolean v) { this.glitchedNames = v; }
    public boolean isSameSkin() { return sameSkin; }
    public void setSameSkin(boolean v) { this.sameSkin = v; }
    public void setSharedSkin(PlayerProfile profile) {
        this.sharedSkinProfile = profile;
    }
    public PlayerProfile getSharedSkin() {
        return sharedSkinProfile;
    }
    @Override
    public void onEnable() {
        // Apply same skin to everyone if option 2 is enabled
        if (sameSkin && sharedSkinProfile != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                try {
                    PlayerProfile pProfile = p.getPlayerProfile();
                    pProfile.setProperties(sharedSkinProfile.getProperties());
                    p.setPlayerProfile(pProfile);
                } catch (Exception ignored) {}
            }
        }
        // Glitched names are handled globally by TablistManager and Chat
        if (glitchedNames) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setDisplayName("\u00A7k12345678");
                p.setCustomName("\u00A7k12345678");
                p.setCustomNameVisible(true);
            }
        }
    }
    @Override
    public void onDisable() {
        // Option 1 & 2 reset will happen on server restart or game reset typically
    }
    @Override
    public boolean hasConfigMenu() { return true; }

    @Override
    public void onRightClick(Player player) {
        player.openInventory(new fr.hel.gui.AnonymousConfigGUI(this).getInventory());
    }


}

