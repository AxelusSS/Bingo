package fr.bingo.scenario;

import fr.bingo.BingoPlugin;
import java.util.ArrayList;
import java.util.List;

public class ScenarioManager {
    
    private final List<Scenario> scenarios = new ArrayList<>();
    
    public ScenarioManager() {
        scenarios.add(new CatEyesScenario());
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
        scenarios.add(new BingoScenario());
        scenarios.add(new LiteGappleScenario());
        scenarios.add(new TripleOresScenario());
        scenarios.add(new BoostLootScenario());
        scenarios.add(new MiniNetherScenario());
        scenarios.add(new PvP18Scenario());
        scenarios.add(new ClassicHandScenario());
        scenarios.add(new ClassicPhysicsScenario());
        
        // Initialiser les events pour les scénarios activés par défaut
        for (Scenario s : scenarios) {
            if (s.isEnabled()) {
                s.onEnable();
                BingoPlugin.getInstance().getServer().getPluginManager().registerEvents(s, BingoPlugin.getInstance());
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
