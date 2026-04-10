package fr.bingo.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.bingo.BingoPlugin;

import java.util.LinkedList;
import java.util.Queue;

public class PregenCommand implements CommandExecutor {

    private boolean isPregenning = false;
    private int totalChunks = 0;
    private int processedChunks = 0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bingo.admin")) {
            sender.sendMessage("§cPermission refusée.");
            return true;
        }

        if (isPregenning) {
            sender.sendMessage("§cUne pré-génération est déjà en cours (" + processedChunks + "/" + totalChunks + ") !");
            return true;
        }

        int radiusBlocks = 500;
        if (args.length >= 1) {
            try {
                radiusBlocks = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cFormat de rayon invalide. Utilisation de 500 par défaut.");
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

        sender.sendMessage("§a[PREGEN] Démarrage de la génération de " + totalChunks + " chunks (Rayon: " + radiusBlocks + " blocs).");

        processQueue(world, chunkQueue, sender);
        return true;
    }

    private void processQueue(World world, Queue<long[]> queue, CommandSender sender) {
        if (queue.isEmpty()) {
            isPregenning = false;
            sender.sendMessage("§a[PREGEN] Pré-génération terminée avec succès !");
            return;
        }

        // On traite 5 chunks à la fois pour ne pas bloquer le système de queue
        for (int i = 0; i < 5; i++) {
            if (queue.isEmpty()) break;
            
            long[] coords = queue.poll();
            int cx = (int) coords[0];
            int cz = (int) coords[1];

            // Chargement asynchrone Paper API
            world.getChunkAtAsync(cx, cz, true).thenAccept(chunk -> {
                processedChunks++;
                
                // Petit retour d'information tous les 200 chunks (modifiable)
                if (processedChunks % 500 == 0 || processedChunks == totalChunks) {
                    float percent = (processedChunks * 100.0f) / totalChunks;
                    String msg = String.format("§e[PREGEN] Avancement : %d/%d (%.1f%%)", processedChunks, totalChunks, percent);
                    sender.sendMessage(msg);
                }
                
                // Si on a terminé le paquet de 5, on rappelle la méthode pour les prochains
                // via le thread principal de Bukkit (Scheduler) pour relancer l'Asynchrone
                if (i == 4 || queue.isEmpty()) {
                    Bukkit.getScheduler().runTask(BingoPlugin.getInstance(), () -> {
                        processQueue(world, queue, sender);
                    });
                }
            });
            
            // Note: En mode asynchrone pur, Paper gère lui-même sa file.
            // Ce système de tickets empêche juste d'envoyer trop de completable futures d'un coup.
        }
    }
}
