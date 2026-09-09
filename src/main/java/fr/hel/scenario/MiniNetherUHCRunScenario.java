package fr.hel.scenario;

import fr.hel.HelPlugin;
import fr.hel.game.HelGame;
import org.bukkit.*;
import org.bukkit.event.Listener;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Sc\u00E9nario Mini Nether pour l'UHC Run.
 * Pop des structures Nether via schematic en sous-sol.
 */
public class MiniNetherUHCRunScenario extends Scenario {

    private final Random random = new Random();
    private final java.util.List<Location> structureLocations = new java.util.ArrayList<>();

    public MiniNetherUHCRunScenario() {
        super("Mini Nether UHC", Material.NETHER_STAR, "Pop des mini nether (schematics) en sous-sol", false);
    }

    @Override
    public void onGameStart() {
        if (!isEnabled()) return;
        
        HelPlugin.getInstance().getLogger().info("[MiniNether] Lancement de la g\u00E9n\u00E9ration structur\u00E9e...");
        
        World world = Bukkit.getWorlds().get(0);
        structureLocations.clear();

        // 1. Un au centre (0,0)
        Location center = new Location(world, 0, -30, 0);
        pasteSchematic(center);
        structureLocations.add(center);

        // 2. 6 structures avant 1000 blocs
        generateInRadius(world, 100, 1000, 6);

        // 3. 6 structures tous les 1000 blocs jusqu'\u00E0 5000
        for (int r = 1000; r < 5000; r += 1000) {
            generateInRadius(world, r, r + 1000, 6);
        }
    }

    private void generateInRadius(World world, int inner, int outer, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = inner + random.nextDouble() * (outer - inner);
            int x = (int) (radius * Math.cos(angle));
            int z = (int) (radius * Math.sin(angle));
            int y = -40 + random.nextInt(20); // Dans la zone Nether (Y < 0)
            
            Location loc = new Location(world, x, y, z);
            pasteSchematic(loc);
            structureLocations.add(loc);
        }
    }

    public java.util.List<Location> getStructureLocations() {
        return structureLocations;
    }

    private void pasteSchematic(Location location) {
        // Le fichier est dans les resources, on le sauvegarde si n\u00E9cessaire
        File schematicFile = new File(HelPlugin.getInstance().getDataFolder(), "nether_uhc.schem");
        
        if (!schematicFile.exists()) {
            HelPlugin.getInstance().saveResource("nether_uhc.schem", false);
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            HelPlugin.getInstance().getLogger().warning("[MiniNether] Format de schematic non reconnu !");
            return;
        }

        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = format.getReader(fis)) {
            Clipboard clipboard = reader.read();
            
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
                HelPlugin.getInstance().getLogger().info("[MiniNether] Structure coll\u00E9e en " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
