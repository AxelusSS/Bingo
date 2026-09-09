package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

/**
 * Lite Gapple : Craft de la pomme en or avec 4 lingots d'or en croix.
 */
public class LiteGappleScenario extends Scenario {

    private NamespacedKey recipeKey;

    public LiteGappleScenario() {
        super("Lite Gapple", Material.GOLDEN_APPLE, "Pomme en or avec 4 lingots au lieu de 8", false);
    }

    @Override
    public void onGameStart() {
        // Retirer le craft vanille et ajouter le craft all\u00E9g\u00E9
        recipeKey = new NamespacedKey(HelPlugin.getInstance(), "lite_gapple");
        
        // Supprimer le craft vanille
        Bukkit.removeRecipe(NamespacedKey.minecraft("golden_apple"));
        
        // Ajouter le craft all\u00E9g\u00E9 : 4 lingots en croix autour d'une pomme
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, new ItemStack(Material.GOLDEN_APPLE));
        recipe.shape(" G ", "GAG", " G ");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('A', Material.APPLE);
        
        Bukkit.addRecipe(recipe);
        
        Bukkit.broadcastMessage("\u00A76\u00A7l[Lite Gapple] \u00A7ePomme en or = 4 lingots en croix !");
    }

    @Override
    public void onDisable() {
        // Remettre le craft vanille
        if (recipeKey != null) {
            Bukkit.removeRecipe(recipeKey);
        }
    }
}
