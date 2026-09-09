package fr.hel.scenario;

import fr.hel.HelPlugin;
import org.bukkit.Material;

/**
 * Sc\u00E9nario Ma\u00EEtre pour l'UHC Run.
 * Active automatiquement les sc\u00E9narios requis : Hastey Bot, Timber, Triple Ore, Boost Loot.
 */
public class UHCRunScenario extends Scenario {

    public UHCRunScenario() {
        super("UHC RUN", Material.DIAMOND_PICKAXE, "Active automatiquement Hastey, Timber, Triple Ore et Boost Loot", false);
    }

    @Override
    public void onEnable() {
        ScenarioManager sm = HelPlugin.getInstance().getScenarioManager();
        enableIfNotNull(sm, HasteyBoysScenario.class);
        enableIfNotNull(sm, TimberScenario.class);
        enableIfNotNull(sm, TripleOresScenario.class);
        enableIfNotNull(sm, BoostLootScenario.class);
        
        // D\u00E9sactiver le Mini Nether classique au profit du Mini Nether UHC (schematic)
        Scenario miniNether = sm.getScenario(MiniNetherScenario.class);
        if (miniNether != null) miniNether.setEnabled(false);
        
        Scenario miniNetherUhc = sm.getScenario(MiniNetherUHCRunScenario.class);
        if (miniNetherUhc != null) miniNetherUhc.setEnabled(true);
    }

    @Override
    public void onDisable() {
        ScenarioManager sm = HelPlugin.getInstance().getScenarioManager();
        disableIfNotNull(sm, HasteyBoysScenario.class);
        disableIfNotNull(sm, TimberScenario.class);
        disableIfNotNull(sm, TripleOresScenario.class);
        disableIfNotNull(sm, BoostLootScenario.class);
        
        Scenario miniNetherUhc = sm.getScenario(MiniNetherUHCRunScenario.class);
        if (miniNetherUhc != null) miniNetherUhc.setEnabled(false);
    }

    private void enableIfNotNull(ScenarioManager sm, Class<? extends Scenario> clazz) {
        Scenario s = sm.getScenario(clazz);
        if (s != null) s.setEnabled(true);
    }

    private void disableIfNotNull(ScenarioManager sm, Class<? extends Scenario> clazz) {
        Scenario s = sm.getScenario(clazz);
        if (s != null) s.setEnabled(false);
    }
}
