package fr.hel.game;

import fr.hel.HelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.scheduler.BukkitRunnable;

public class BorderManager {

    private int initialSize = 2000;
    private int finalSize = 100;
    private int timeBeforeShrinkMinutes = 60;
    private int shrinkTimeMinutes = 30; // Temps mis pour r\u00E9tr\u00E9cir
    
    private BukkitRunnable shrinkTask;

    public int getInitialSize() { return initialSize; }
    public void setInitialSize(int size) { this.initialSize = size; }

    public int getFinalSize() { return finalSize; }
    public void setFinalSize(int size) { this.finalSize = size; }

    public int getTimeBeforeShrinkMinutes() { return timeBeforeShrinkMinutes; }
    public void setTimeBeforeShrinkMinutes(int minutes) { this.timeBeforeShrinkMinutes = minutes; }

    public int getShrinkTimeMinutes() { return shrinkTimeMinutes; }
    public void setShrinkTimeMinutes(int minutes) { this.shrinkTimeMinutes = minutes; }

    public void startBorder() {
        World world = Bukkit.getWorlds().get(0);
        WorldBorder border = world.getWorldBorder();
        
        border.setCenter(0, 0);
        border.setSize(initialSize);
        border.setDamageAmount(1.0);
        border.setDamageBuffer(5.0);
        border.setWarningDistance(20);

        if (shrinkTask != null) shrinkTask.cancel();

        shrinkTask = new BukkitRunnable() {
            int elapsed = 0;
            boolean shrinking = false;

            @Override
            public void run() {
                if (HelPlugin.getInstance().getHelGame().getState() != GameState.PLAYING) {
                    this.cancel();
                    return;
                }

                if (!shrinking && elapsed >= timeBeforeShrinkMinutes * 60) {
                    shrinking = true;
                    Bukkit.broadcastMessage("\u00A7c\u00A7l\u26A0 La bordure commence \u00E0 r\u00E9tr\u00E9cir !");
                    border.setSize(finalSize, shrinkTimeMinutes * 60L);
                } else if (!shrinking) {
                    int remaining = (timeBeforeShrinkMinutes * 60) - elapsed;
                    if (remaining == 10 * 60) {
                        Bukkit.broadcastMessage("\u00A7e\u26A0 La bordure r\u00E9tr\u00E9cit dans \u00A7c10 minutes");
                    } else if (remaining == 5 * 60) {
                        Bukkit.broadcastMessage("\u00A7e\u26A0 La bordure r\u00E9tr\u00E9cit dans \u00A7c5 minutes");
                    } else if (remaining == 60) {
                        Bukkit.broadcastMessage("\u00A7e\u26A0 La bordure r\u00E9tr\u00E9cit dans \u00A7c1 minute");
                    }
                }

                elapsed++;
            }
        };
        shrinkTask.runTaskTimer(HelPlugin.getInstance(), 0L, 20L);
    }
    
    public void stopBorder() {
        if (shrinkTask != null) {
            shrinkTask.cancel();
            shrinkTask = null;
        }
    }
}
