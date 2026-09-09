package fr.hel.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Grille de Hel. G\u00E9n\u00E8re les objectifs en fonction du mode et de la difficult\u00E9.
 */
public class HelGrid {

    private final List<HelObjective> objectives;
    private int size = 5;

    public HelGrid() {
        this.objectives = new ArrayList<>();
    }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public List<HelObjective> getObjectives() { return objectives; }

    /**
     * G\u00E9n\u00E8re une grille al\u00E9atoire selon le mode et la difficult\u00E9 max.
     */
    public void generateRandomGrid(HelMode mode, Difficulty maxDifficulty) {
        objectives.clear();

        List<HelObjectivePool.PoolEntry> pool = new ArrayList<>();

        // Remplir le pool selon le mode
        if (mode == HelMode.ITEMS || mode == HelMode.MIXED) {
            pool.addAll(HelObjectivePool.getItemPool());
        }
        if (mode == HelMode.ACHIEVEMENTS || mode == HelMode.MIXED) {
            pool.addAll(HelObjectivePool.getAchievementPool());
        }

        // Filtrer par difficult\u00E9 max et items d\u00E9sactiv\u00E9s
        java.util.Set<String> disabled = fr.hel.HelPlugin.getInstance().getHelGame().getDisabledPoolItems();
        pool = pool.stream()
                .filter(e -> e.difficulty.ordinal() <= maxDifficulty.ordinal())
                .filter(e -> !disabled.contains(e.id))
                .collect(Collectors.toList());

        Collections.shuffle(pool);

        int total = size * size;
        if (total > pool.size()) total = pool.size();

        for (int i = 0; i < total; i++) {
            HelObjectivePool.PoolEntry entry = pool.get(i);
            if (entry.isAchievement) {
                objectives.add(new HelObjective(entry.id, entry.icon, entry.difficulty));
            } else {
                objectives.add(new HelObjective(entry.icon, entry.difficulty));
            }
        }
    }

    /** Compat : g\u00E9n\u00E8re avec les valeurs par d\u00E9faut */
    public void generateRandomGrid() {
        generateRandomGrid(HelMode.ITEMS, Difficulty.HARD);
    }
}
