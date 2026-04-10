package fr.bingo.gui;

import fr.bingo.BingoPlugin;
import fr.bingo.team.BingoTeam;
import fr.bingo.team.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamSelectorGUI implements InventoryHolder {

    private final Inventory inventory;

    public TeamSelectorGUI() {
        this.inventory = Bukkit.createInventory(this, 27, "§8Choisis ton équipe");
        setupInventory();
    }

    private void setupInventory() {
        TeamManager teamManager = BingoPlugin.getInstance().getTeamManager();
        List<BingoTeam> teams = teamManager.getTeams();
        
        int[] slots = {10, 12, 14, 16}; // Milieu de l'inventaire 27 slots
        
        for (int i = 0; i < teams.size(); i++) {
            if (i >= slots.length) break;
            
            BingoTeam team = teams.get(i);
            ItemStack item = new ItemStack(team.getBannerMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName(team.getChatColor() + "§l" + team.getName());
                List<String> lore = new ArrayList<>();
                lore.add("§7Membres (" + team.getPlayers().size() + "/" + teamManager.getMaxPlayersPerTeam() + "):");
                
                if (team.getPlayers().isEmpty()) {
                    lore.add("§e§oAucun joueur");
                } else {
                    for (UUID uuid : team.getPlayers()) {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                        lore.add(team.getChatColor() + "- " + (p.getName() != null ? p.getName() : "Inconnu"));
                    }
                }
                
                lore.add("");
                lore.add("§e► Clique pour rejoindre !");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            inventory.setItem(slots[i], item);
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
