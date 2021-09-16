package me.botsko.prism.listeners.self;

import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.actionlibs.RecordingTask;
import me.botsko.prism.api.BlockStateChange;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.events.PrismDrainEvent;
import me.botsko.prism.events.PrismExtinguishEvent;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;

public class PrismMiscEvents implements Listener {

    /**
     * PrismDrainEvent.
     *
     * @param event PrismDrainEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrismBlocksDrainEvent(final PrismDrainEvent event) {

        // Get all block changes for this event
        final ArrayList<BlockStateChange> blockStateChanges = event.getBlockStateChanges();
        if (!blockStateChanges.isEmpty()) {

            // Create an entry for the rollback as a whole
            final Handler primaryAction = ActionFactory.createPrismProcess(ActionType.PRISM_PROCESS,
                    PrismProcessType.DRAIN, event.onBehalfOf(), "" + event.getRadius());
            final long id = RecordingTask.insertActionIntoDatabase(primaryAction);
            if (id == 0) {
                return;
            }
            for (final BlockStateChange stateChange : blockStateChanges) {

                final BlockState orig = stateChange.getOriginalBlock();
                final BlockState newBlock = stateChange.getNewBlock();

                // Build the action
                RecordingQueue.addToQueue(
                        ActionFactory.createPrismRollback(ActionType.PRISM_DRAIN, orig, newBlock, event.onBehalfOf(),
                                id));

            }
            // ActionQueue.save();
        }
    }

    /**
     * PrismExtinguishEvent.
     * @param event PrismExtinguishEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPrismBlocksExtinguishEvent(final PrismExtinguishEvent event) {

        // Get all block changes for this event
        final ArrayList<BlockStateChange> blockStateChanges = event.getBlockStateChanges();
        if (!blockStateChanges.isEmpty()) {

            // Create an entry for the rollback as a whole
            final Handler primaryAction = ActionFactory.createPrismProcess(ActionType.PRISM_PROCESS,
                    PrismProcessType.EXTINGUISH, event.onBehalfOf(), "" + event.getRadius());
            final long id = RecordingTask.insertActionIntoDatabase(primaryAction);
            if (id == 0) {
                return;
            }
            for (final BlockStateChange stateChange : blockStateChanges) {

                final BlockState orig = stateChange.getOriginalBlock();
                final BlockState newBlock = stateChange.getNewBlock();

                // Build the action
                RecordingQueue.addToQueue(
                        ActionFactory.createPrismRollback(ActionType.PRISM_EXTINGUISH, orig, newBlock,
                                event.onBehalfOf(), id));

            }
            // ActionQueue.save();
        }
    }
}