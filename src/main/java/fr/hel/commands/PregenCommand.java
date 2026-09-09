package fr.hel.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.hel.HelPlugin;

import java.util.LinkedList;
import java.util.Queue;

public class PregenCommand implements CommandExecutor {

    private boolean isPregenning = false;
    private int totalChunks = 0;
    private int processedChunks = 0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bingo.admin")) {
            sender.sendMessage("\u00A7cPermission refus\u00E9e.");
            return true;
        }

        if (isPregenning) {
            sender.sendMessage("\u00A7cUne pr\u00E9-g\u00E9n\u00E9ration est d\u00E9j\u00E0 en cours (" + processedChunks + "/" + totalChunks + ") !");
            return true;
        }

        int radiusBlocks = 500;
        if (args.length >= 1) {
            try {
                radiusBlocks = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("\u00A7cFormat de rayon invalide. Utilisation de 500 par d\u00E9faut.");
            }
        }

        World world = Bukkit.getWorlds().get(0); // Overworld
        if (sender instanceof Player p) {
            world = p.getWorld();
        }

        int chunkRadius = radiusBlocks >> 4; // Division par 16
        
        Queue<long[]> chunkQueue = new LinkedList<>();
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                chunkQueue.add(new long[]{cx, cz});
            }
        }
        
        totalChunks = chunkQueue.size();
        processedChunks = 0;
        isPregenning = true;

        sender.sendMessage("\u00A7a[PREGEN] D\u00E9marrage de la g\u00E9n\u00E9ration de " + totalChunks + " chunks (Rayon: " + radiusBlocks + " blocs).");

        processQueue(world, chunkQueue, sender);
        return true;
    }

    private void processQueue(World world, Queue<long[]> queue, CommandSender sender) {
        if (queue.isEmpty()) {
            isPregenning = false;
            sender.sendMessage("\u00A7a[PREGEN] Pr\u00E9-g\u00E9n\u00E9ration termin\u00E9e avec succ\u00E8s !");
            return;
        }

        // On traite 5 chunks \u00E0 la fois pour ne pas bloquer le syst\u00E8me de queue
        for (int i = 0; i < 5; i++) {
            if (queue.isEmpty()) break;
            
            long[] coords = queue.poll();
            int cx = (int) coords[0];
            int cz = (int) coords[1];
            
            final int loopIndex = i;

            // Chargement asynchrone Paper API
            world.getChunkAtAsync(cx, cz, true).thenAccept(chunk -> {
                processedChunks++;
                
                // Petit retour d'information tous les 200 chunks (modifiable)
                if (processedChunks % 500 == 0 || processedChunks == totalChunks) {
                    float percent = (processedChunks * 100.0f) / totalChunks;
                    String msg = String.format("\u00A7e[PREGEN] Avancement : %d/%d (%.1f%%)", processedChunks, totalChunks, percent);
                    sender.sendMessage(msg);
                }
                
                // Si on a termin\u00E9 le paquet de 5, on rappelle la m\u00E9thode pour les prochains
                // via le thread principal de Bukkit (Scheduler) pour relancer l'Asynchrone
                if (loopIndex == 4 || queue.isEmpty()) {
                    Bukkit.getScheduler().runTask(HelPlugin.getInstance(), () -> {
                        processQueue(world, queue, sender);
                    });
                }
            });
            
            // Note: En mode asynchrone pur, Paper g\u00E8re lui-m\u00EAme sa file.
            // Ce syst\u00E8me de tickets emp\u00EAche juste d'envoyer trop de completable futures d'un coup.
        }
    }
}
