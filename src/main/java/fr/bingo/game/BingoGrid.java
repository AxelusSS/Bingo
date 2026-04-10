package fr.bingo.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Grille de Bingo. Génère les objectifs en fonction du mode et de la difficulté.
 */
public class BingoGrid {

    private final List<BingoObjective> objectives;
    private int size = 5;

    public BingoGrid() {
        this.objectives = new ArrayList<>();
    }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public List<BingoObjective> getObjectives() { return objectives; }

    /**
     * Génère une grille aléatoire selon le mode et la difficulté max.
     */
    public void generateRandomGrid(BingoMode mode, Difficulty maxDifficulty) {
        objectives.clear();

        List<BingoObjectivePool.PoolEntry> pool = new ArrayList<>();

        // Remplir le pool selon le mode
        if (mode == BingoMode.ITEMS || mode == BingoMode.MIXED) {
            pool.addAll(BingoObjectivePool.getItemPool());
        }
        if (mode == BingoMode.ACHIEVEMENTS || mode == BingoMode.MIXED) {
            pool.addAll(BingoObjectivePool.getAchievementPool());
        }

        // Filtrer par difficulté max
        pool = pool.stream()
                .filter(e -> e.difficulty.ordinal() <= maxDifficulty.ordinal())
                .collect(Collectors.toList());

        Collections.shuffle(pool);

        int total = size * size;
        if (total > pool.size()) total = pool.size();

        for (int i = 0; i < total; i++) {
            BingoObjectivePool.PoolEntry entry = pool.get(i);
            if (entry.isAchievement) {
                objectives.add(new BingoObjective(entry.id, entry.icon, entry.difficulty));
            } else {
                objectives.add(new BingoObjective(entry.icon, entry.difficulty));
            }
        }
    }

    /** Compat : génère avec les valeurs par défaut */
    public void generateRandomGrid() {
        generateRandomGrid(BingoMode.ITEMS, Difficulty.HARD);
    }
}
