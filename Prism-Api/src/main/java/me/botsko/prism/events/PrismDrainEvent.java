package me.botsko.prism.events;

import me.botsko.prism.api.BlockStateChange;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PrismDrainEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    protected final int radius;
    private final ArrayList<BlockStateChange> blockStateChanges;
    private final Player onBehalfOf;

    /**
     * Constructor.
     * @param blockStateChanges ArrayList
     * @param onBehalfOf Player
     * @param radius int
     */
    protected PrismDrainEvent(ArrayList<BlockStateChange> blockStateChanges, Player onBehalfOf, int radius) {
        this.blockStateChanges = blockStateChanges;
        this.onBehalfOf = onBehalfOf;
        this.radius = radius;
    }

    public ArrayList<BlockStateChange> getBlockStateChanges() {
        return blockStateChanges;
    }

    public Player onBehalfOf() {
        return onBehalfOf;
    }

    public int getRadius() {
        return radius;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Required by bukkit for proper event handling.
     * @return {@link HandlerList}
     */
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;

    }
}
