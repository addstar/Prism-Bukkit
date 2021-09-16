package me.botsko.prism.bridge;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.api.actions.ActionType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class PrismWorldEditLogger extends AbstractDelegateExtent {
    private final Actor player;
    private final World world;

    /**
     * Constructor.
     *
     * @param player Actor
     * @param extent Extent
     * @param world  World
     */
    public PrismWorldEditLogger(Actor player, Extent extent, World world) {
        super(extent);
        this.player = player;
        this.world = world;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 pt, B newBlock) throws WorldEditException {
        if (Prism.getInstance().config.trackingConfig.trackers.get(ActionType.WORLD_EDIT)) {
            Location loc = BukkitAdapter.adapt(world, pt);
            Block oldBlock = loc.getBlock();
            Material newMaterial = BukkitAdapter.adapt(newBlock.getBlockType());
            BlockData newData = BukkitAdapter.adapt(newBlock);
            RecordingQueue.addToQueue(ActionFactory.createBlockChange(ActionType.WORLD_EDIT, loc, oldBlock.getType(),
                    oldBlock.getBlockData(), newMaterial, newData, Bukkit.getPlayer(player.getUniqueId())));
        }
        return super.setBlock(pt, newBlock);
    }
}