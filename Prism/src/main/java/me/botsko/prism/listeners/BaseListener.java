package me.botsko.prism.listeners;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.utils.MaterialTag;
import me.botsko.prism.utils.block.Utilities;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.Listener;

/**
 * Created for the Prism-Bukkit Project.
 * Created by Narimm on 4/09/2020.
 */
public abstract class BaseListener implements Listener {

    protected final Prism plugin;

    protected BaseListener(Prism plugin) {
        this.plugin = plugin;
    }

    protected void constructBlockEvent(final ActionType parentAction, final String cause,
                                       final Iterable<Block> blockList) {
        final PrismBlockEvents be = new PrismBlockEvents(plugin); //todo is this necessary?
        for (Block block : blockList) {
            // don't bother record upper doors.
            if (MaterialTag.DOORS.isTagged(block.getType())
                    && ((Door) block.getState().getBlockData()).getHalf() == Bisected.Half.TOP) {
                continue;
            }

            // Change handling a bit if it's a long block
            final Block sibling = Utilities.getSiblingForDoubleLengthBlock(block);
            if (sibling != null && !block.getType().equals(Material.CHEST)
                    && !block.getType().equals(Material.TRAPPED_CHEST)) {
                block = sibling;
            }

            // log items removed from container
            // note: done before the container so a "rewind" for rollback will
            // work properly
            final Block b2 = block;
            be.forEachItem(block, (i, s) ->
                    RecordingQueue.addToQueue(ActionFactory.createItemStack(ActionType.ITEM_REMOVE,
                            i, i.getAmount(), 0, null, b2.getLocation(), cause)));
            // be.logItemRemoveFromDestroyedContainer( name, block );
            RecordingQueue.addToQueue(ActionFactory.createBlock(parentAction, block, cause));
            // look for relationships
            be.logBlockRelationshipsForBlock(cause, block);

        }
    }

}
