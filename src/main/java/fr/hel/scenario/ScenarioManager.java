package fr.hel.scenario;

import fr.hel.HelPlugin;
import java.util.ArrayList;
import java.util.List;

public class ScenarioManager {
    
    private final List<Scenario> scenarios = new ArrayList<>();
    
    public ScenarioManager() {
        scenarios.add(new EternalDayScenario());
        scenarios.add(new CatEyesScenario());
        scenarios.add(new BiomeCompassScenario());
        scenarios.add(new KeepInventoryScenario());
        scenarios.add(new NoFoodScenario());
        scenarios.add(new FlowerPowerScenario());
        scenarios.add(new FriendlyCraftScenario());
        scenarios.add(new TeamInventoryScenario());
        scenarios.add(new CutCleanScenario());
        scenarios.add(new HasteyBoysScenario());
        scenarios.add(new TimberScenario());
        scenarios.add(new FastSmeltingScenario());
        scenarios.add(new NoFallDamageScenario());
        scenarios.add(new FirelessScenario());
        scenarios.add(new SwitchScenario());
        scenarios.add(new SwitchInventoryScenario());
        scenarios.add(new SharedHealthScenario());
        scenarios.add(new GraveScenario());
        scenarios.add(new SuperHeroScenario());
        scenarios.add(new HelScenario());
        scenarios.add(new LiteGappleScenario());
        scenarios.add(new TripleOresScenario());
        scenarios.add(new BoostLootScenario());
        
        // Sc\u00E9narios d\u00E9pendant de WorldEdit
        if (HelPlugin.getInstance().getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            scenarios.add(new MiniNetherScenario());
            scenarios.add(new MiniNetherUHCRunScenario());
        } else {
            HelPlugin.getInstance().getLogger().warning("[HEL] WorldEdit manquant : Les sc\u00E9narios Mini-Nether sont d\u00E9sactiv\u00E9s.");
        }
        
        scenarios.add(new UHCRunScenario());
        scenarios.add(new PvP18Scenario());
        scenarios.add(new ClassicHandScenario());
        scenarios.add(new ClassicPhysicsScenario());
        scenarios.add(new AnonymousScenario());
        
        // Initialiser les events pour les sc\u00E9narios activ\u00E9s par d\u00E9faut
        for (Scenario s : scenarios) {
            if (s.isEnabled()) {
                s.onEnable();
                HelPlugin.getInstance().getServer().getPluginManager().registerEvents(s, HelPlugin.getInstance());
            }
        }
    }
    
    public List<Scenario> getScenarios() {
        return scenarios;
    }
    
    public void onGameStart() {
        for (Scenario s : scenarios) {
            if (s.isEnabled()) {
                s.onGameStart();
            }
        }
    }
    
    public <T extends Scenario> T getScenario(Class<T> clazz) {
        for (Scenario s : scenarios) {
            if (s.getClass().equals(clazz)) {
                return clazz.cast(s);
            }
        }
        return null;
    }
    
    public boolean isScenarioEnabled(Class<? extends Scenario> clazz) {
        Scenario s = getScenario(clazz);
        return s != null && s.isEnabled();
    }
}
