package me.botsko.prism.listeners;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.utils.block.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class PrismWorldEvents implements Listener {

    /**
     * StructureGrowEvent.
     * @param event StructureGrowEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent event) {
        String type = "tree-grow";
        final TreeType species = event.getSpecies();
        if (species.name().toLowerCase().contains("mushroom")) {
            type = "mushroom-grow";
        }
        if (!Prism.getIgnore().event(type, event.getWorld())) {
            return;
        }
        for (final BlockState block : event.getBlocks()) {
            if (Utilities.isGrowableStructure(block.getType())) {
                if (event.getPlayer() != null) {
                    RecordingQueue.addToQueue(ActionFactory.createGrow(type, block, event.getPlayer()));
                } else {
                    RecordingQueue.addToQueue(ActionFactory.createGrow(type, block, "Environment"));
                }
            }
        }
    }

    /**
     * WorldLoadEvent.
     * @param event WorldLoadEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(final WorldLoadEvent event) {

        final String worldName = event.getWorld().getName();

        if (!Prism.prismWorlds.containsKey(worldName)) {
            Prism.getPrismDataSource().addWorldName(worldName);
        }
    }

    /**
     * Track portal creation events.
     * @param event PortalCreateEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR,ignoreCancelled = true)
    public void onPortalCreate(final PortalCreateEvent event) {
        final String type = "portal-create";
        if (!Prism.getIgnore().event(type, event.getWorld())) {
            return;
        }
        if (event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            for (final BlockState newBlock : event.getBlocks()) {
                // Include only the nether portal blocks that were created.
                if (newBlock.getType() == Material.NETHER_PORTAL) {
                    final Entity e = event.getEntity();
                    final BlockState oldBlock = event.getWorld().getBlockAt(newBlock.getLocation()).getState();
                    if (e != null) {
                        // Run the second after the fire was placed (20 ticks), so that it is recorded after the fire.
                        // We have to do this because the database only records changes per second, not tick or instant,
                        // which can result in the fire being recorded after the nether portal blocks.
                        Bukkit.getScheduler().runTaskLater(Prism.getInstance(), () -> {
                            recordCreatePortal(event, type, newBlock, e, oldBlock);
                        }, 20);
                    }
                }
            }
        } else if (event.getReason() == PortalCreateEvent.CreateReason.NETHER_PAIR) {
            // Record both the obsidian and portal blocks that were created.
            for (final BlockState newBlock : event.getBlocks()) {
                final Entity e = event.getEntity();
                final BlockState oldBlock = event.getWorld().getBlockAt(newBlock.getLocation()).getState();
                if (e != null) {
                    if (newBlock.getType() == Material.NETHER_PORTAL) {
                        // Run the second after the fire was placed (20 ticks), so that it is recorded after the fire.
                        // We have to do this because the database only records changes per second, not tick or instant,
                        // which can result in the fire being recorded after the nether portal blocks.
                        Bukkit.getScheduler().runTaskLater(Prism.getInstance(), () -> {
                            recordCreatePortal(event, type, newBlock, e, oldBlock);
                        }, 20);
                    } else {
                        recordCreatePortal(event, type, newBlock, e, oldBlock);
                    }
                }
            }
        }
    }

    private void recordCreatePortal(PortalCreateEvent event, String type, BlockState newBlock, Entity e,
                                    BlockState oldBlock) {
        if (e instanceof Player) {
            RecordingQueue.addToQueue(ActionFactory.createPortal(type, newBlock, oldBlock, (Player) event.getEntity()));
        } else {
            RecordingQueue.addToQueue(ActionFactory.createPortal(type, newBlock, oldBlock, e.getName().toLowerCase()));
        }
    }
}
