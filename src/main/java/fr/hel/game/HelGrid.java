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
        
        // --- LOGIQUE DE LIMITATION PAR CATEGORIE ---
        int maxPerCategory = (size <= 3) ? 1 : ((size <= 5) ? 2 : 3);
        java.util.Map<HelObjectivePool.ItemCategory, Integer> categoryCounts = new java.util.HashMap<>();
        
        for (HelObjectivePool.PoolEntry p : pool) {
            if (objectives.size() >= total) break;
            
            if (p.category != HelObjectivePool.ItemCategory.NONE) {
                int count = categoryCounts.getOrDefault(p.category, 0);
                if (count >= maxPerCategory) {
                    continue; // On passe, on a deja trop d'items de cette categorie
                }
                categoryCounts.put(p.category, count + 1);
            }
            
            if (p.isAchievement) {
                objectives.add(new HelObjective(p.id, p.icon, p.difficulty));
            } else {
                objectives.add(new HelObjective(p.icon, p.difficulty));
            }
        }
        
        // S'il nous manque des objectifs (si le pool filtré était trop restrictif à cause des catégories)
        // on désactive la limite de catégorie pour compléter (fallback de secours rare).
        if (objectives.size() < total) {
            for (HelObjectivePool.PoolEntry p : pool) {
                if (objectives.size() >= total) break;
                // on check si c'est pas deja dedans
                boolean exists = false;
                for(HelObjective o : objectives) {
                    if (o.getId().equals(p.id)) { exists = true; break; }
                }
                if (!exists) {
                    if (p.isAchievement) {
                        objectives.add(new HelObjective(p.id, p.icon, p.difficulty));
                    } else {
                        objectives.add(new HelObjective(p.icon, p.difficulty));
                    }
                }
            }
        }
    }

    /** Compat : g\u00E9n\u00E8re avec les valeurs par d\u00E9faut */
    public void generateRandomGrid() {
        generateRandomGrid(HelMode.ITEMS, Difficulty.HARD);
    }
}
