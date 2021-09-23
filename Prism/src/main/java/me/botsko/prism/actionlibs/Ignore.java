package me.botsko.prism.actionlibs;

import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.config.PrismConfig;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class Ignore {

    private final PrismConfig config;
    private final List<UUID> ignorePlayers;
    private final boolean ignorePlayersWhiteList;
    private final List<UUID> ignoreWorlds;
    private final boolean ignoreWorldsWhiteList;
    private final boolean ignoreCreative;

    /**
     * Create Ignore.
     *
     * @param config the PrismConfig.
     */
    public Ignore(PrismConfig config) {
        this.config = config;
        ignorePlayers = config.ignoreConfig.players;
        ignorePlayersWhiteList = config.ignoreConfig.isPlayerWhiteList;
        ignoreWorlds = config.ignoreConfig.worlds;
        ignoreWorldsWhiteList = config.ignoreConfig.isWorldWhiteList;
        ignoreCreative = config.ignoreConfig.creativePlayers;
    }

    /**
     * Check event type.  This checks to see if an event was configured for tracking - in effect it is an inverted
     * check.  You could just call the config instead..  but this does report to debug if the action is not tracked.
     *
     * @param actionType type.
     * @return boolean
     */
    public boolean event(ActionType actionType) {

        // Always track Prism actions - it's mainly internal
        if (actionType.name.contains("prism")) {
            return true;
        }

        if (config.trackingConfig.trackers.getOrDefault(actionType,true)) {
            return true;
        } else {
            PrismLogHandler.debug("Ignoring Action Type: " + actionType.name);
            return false;
        }
    }

    /**
     * Check whether an action should be ignored.
     *
     * @param actionTypeName type to ignore.
     * @param world          world to check.
     * @param playerUuid     player to check
     * @return boolean.
     */
    public boolean event(ActionType actionTypeName, World world, UUID playerUuid) {
        return event(actionTypeName, world) && playerIisIgnored(playerUuid);
    }

    /**
     * Asssess tracking.
     *
     * @param actionType  String
     * @param player Player
     * @return boolean
     * @deprecated use {@link Ignore#event(ActionType, Player)}
     */
    @Deprecated
    public boolean event(String actionType,Player player) {
        ActionType type = ActionType.valueOf(actionType);
        return event(type,player);
    }

    /**
     * Check whether an action should be ignored.
     *
     * @param actionTypeName type to ignore.
     * @param player         player to check.
     * @return boolean.
     */
    public boolean event(ActionType actionTypeName, Player player) {

        if (!event(actionTypeName, player.getWorld())) {
            return false;
        }

        // Does the player have perms to ignore this action type?
        if (config.ignoreConfig.enablePermNodes
                && player.hasPermission("prism.ignore.tracking." + actionTypeName)) {
            PrismLogHandler.debug("Player has permission node to ignore " + actionTypeName);
            return false;
        }

        return event(player);

    }


    /**
     * Check if we are ignoring a player.
     *
     * @param player the player
     * @return boolean
     */
    public boolean event(Player player) {

        if (player == null) {
            PrismLogHandler.debug("Player is null will be ignored");
            return false;
        }

        // Should we ignore this player?
        if (playerIisIgnored(player.getUniqueId())) {
            PrismLogHandler.debug("Player is being ignored, per config: " + player.getName());
            return false;
        }

        // Should we ignore this player for being in creative?
        if (ignoreCreative) {
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                PrismLogHandler.debug("Player is in creative mode, creative mode ignored: " + player.getName());
                return false;
            }
        }

        return true;
    }

    /**
     * Check whether an action should be ignored.
     *
     * @param actionTypeName type to ignore.
     * @param block          to check.
     * @return boolean.
     */
    public boolean event(ActionType actionTypeName, Block block) {
        return event(actionTypeName, block.getWorld());
    }

    /**
     * Check whether an action should be ignored.
     *
     * @param actionTypeName type to ignore.
     * @param world          world to check.
     * @return boolean.
     */
    public boolean event(ActionType actionTypeName, World world) {

        // Should we ignore this world?
        if (ignoreWorlds != null && ignoreWorlds.contains(world.getUID()) != ignoreWorldsWhiteList) {
            PrismLogHandler.debug("World is being ignored, per config: " + world.getName());
            return false;
        }

        return event(actionTypeName);

    }

    private boolean playerIisIgnored(UUID playerUuid) {
        return ignorePlayers.contains(playerUuid) && !ignorePlayersWhiteList;
    }
}