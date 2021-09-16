package me.botsko.prism.listeners;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.api.actions.ActionType;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

public class PrismInventoryMoveItemEvent implements Listener {

    /**
     * InventoryMoveEvent.
     *
     * @param event InventoryMoveEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryMoveItem(final InventoryMoveItemEvent event) {

        // Hopper inserted
        if (Prism.getIgnore().event(ActionType.ITEM_INSERT) && event.getDestination() != null) {

            // Get container
            final InventoryHolder ih = event.getDestination().getHolder();
            Location containerLoc = null;
            if (ih instanceof final BlockState eventChest) containerLoc = eventChest.getLocation();

            if (containerLoc == null) {
                return;
            }

            String invName = event.getSource().getType().name().toLowerCase();

            RecordingQueue.addToQueue(ActionFactory.createItemStack(ActionType.ITEM_INSERT, event.getItem(),
                    event.getItem().getAmount(), 0, null, containerLoc, invName));
        }

        // Hopper removed
        if (Prism.getIgnore().event(ActionType.ITEM_REMOVE) && event.getSource() != null) {

            // Get container
            final InventoryHolder ih = event.getSource().getHolder();
            Location containerLoc = null;
            if (ih instanceof final BlockState eventChest) {
                containerLoc = eventChest.getLocation();
            }

            if (containerLoc == null) {
                return;
            }

            String invName = event.getDestination().getType().name().toLowerCase();

            RecordingQueue.addToQueue(ActionFactory.createItemStack(ActionType.ITEM_REMOVE, event.getItem(),
                    event.getItem().getAmount(), 0, null, containerLoc, invName));
        }
    }
}