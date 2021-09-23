package me.botsko.prism.events;

import me.botsko.prism.api.actions.ActionType;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class PrismCustomBlockEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String pluginName;
    private final ActionType actionType;
    private String customTypeName = null;
    private final Player player;
    private final Block block;
    private final String message;

    /**
     * Constructor.
     * todo Implement proper CUSTOM Action handling
     * @param plugin         Plugin
     * @param actionTypeName String - can either be custom or inbuilt.
     * @param player         Player
     * @param message        message
     */
    public PrismCustomBlockEvent(Plugin plugin, String actionTypeName, Player player, Block block, String message) {
        this.pluginName = plugin.getName();
        ActionType type = ActionType.getByName(actionTypeName);
        if (type == null) {
            actionType = ActionType.CUSTOM_ACTION;
            customTypeName = actionTypeName;
        } else {
            actionType = type;
        }
        this.player = player;
        this.block = block;
        this.message = message + ChatColor.GOLD + " [" + this.pluginName + "]" + ChatColor.DARK_AQUA;
    }


    public String getPluginName() {
        return pluginName;
    }

    public ActionType getActionTypeName() {
        return actionType;
    }

    public String getCustomTypeName() {
        return customTypeName;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public Block getBlock() {
        return block;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Required by bukkit for proper event handling.
     */
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;

    }
}