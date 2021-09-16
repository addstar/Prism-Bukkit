package me.botsko.prism.parameters;

import me.botsko.prism.ApiHandler;
import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.bridge.WorldEditBridge;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.utils.ChunkUtils;
import me.botsko.prism.utils.MiscUtils;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.util.regex.Pattern;

public class RadiusParameter extends SimplePrismParameterHandler {

    public RadiusParameter() {
        super("Radius", Pattern.compile("[\\w,:-]+"), "r");
    }

    @Override
    public void process(QueryParameters query, String alias, String input, CommandSender sender) {

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        String inputValue = input;

        final PrismConfig config = Prism.getInstance().config;

        if (TypeUtils.isNumeric(inputValue) || (inputValue.contains(":") && inputValue.split(":").length >= 1
                && TypeUtils.isNumeric(inputValue.split(":")[1]))) {
            Location coordsLoc = null;
            int desiredRadius;
            if (inputValue.contains(":")) {
                desiredRadius = Integer.parseInt(inputValue.split(":")[1]);
                final String radiusLocOrPlayer = inputValue.split(":")[0];
                if (radiusLocOrPlayer.contains(",") && player != null) { // Coordinates;
                    // x,y,z
                    final String[] coordinates = radiusLocOrPlayer.split(",");
                    if (coordinates.length != 3) {
                        throw new IllegalArgumentException("Couldn't parse the coordinates '" + radiusLocOrPlayer
                                + "'. Perhaps you have more than two commas?");
                    }
                    for (final String s : coordinates) {
                        if (!TypeUtils.isNumeric(s)) {
                            throw new IllegalArgumentException("The coordinate '" + s + "' is not a number.");
                        }
                    }
                    coordsLoc = (new Location(player.getWorld(), Integer.parseInt(coordinates[0]),
                            Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2])));
                } else {
                    // Try to find an online player
                    Player p2 = Bukkit.getServer().getPlayer(radiusLocOrPlayer);
                    if (p2 == null) {
                        throw new IllegalArgumentException("Couldn't find the player named '" + radiusLocOrPlayer
                                + "'. Perhaps they are not online or you misspelled their name?");
                    }
                    player = p2;
                }
            } else {
                desiredRadius = Integer.parseInt(inputValue);
            }
            if (desiredRadius <= 0) {
                throw new IllegalArgumentException(
                        "Radius must be greater than zero. Or leave it off to use the default. Use /prism ? for help.");
            }

            // If neither sender or a named player found, die here
            if (player == null) {
                throw new IllegalArgumentException(
                        "The radius parameter must be used by a player. Use w:worldname if attempting "
                                + "to limit to a world.");
            }

            // Clamp radius based on perms, configs
            int radius = MiscUtils.clampRadius(player, desiredRadius, query.getProcessType(), config);
            if (desiredRadius != radius) {
                if (sender != null) {
                    Prism.messenger.sendMessage(sender,
                            Prism.messenger.playerError("Forcing radius to " + radius + " as allowed by config."));
                }
            }

            if (radius > 0) {
                query.setRadius(radius);
                if (coordsLoc != null) {
                    query.setMinMaxVectorsFromPlayerLocation(coordsLoc); // We
                    // need
                    // to
                    // set
                    // this
                    // *after*
                    // the
                    // radius
                    // has
                    // been
                    // set
                    // or
                    // it
                    // won't
                    // work.
                } else {
                    query.setMinMaxVectorsFromPlayerLocation(player.getLocation());
                }
            }
        } else {

            // If neither sender or a named player found, die here
            if (player == null) {
                throw new IllegalArgumentException(
                        "The radius parameter must be used by a player. "
                                + "Use w:worldname if attempting to limit to a world.");
            }

            // User wants an area inside of a worldedit selection
            switch (inputValue) {
                case "we":

                    if (ApiHandler.worldEditPlugin == null) {
                        throw new IllegalArgumentException(
                                "This feature is disabled because Prism couldn't find WorldEdit.");
                    } else {

                        // Load a selection from world edit as our area.
                        if (!WorldEditBridge.getSelectedArea(Prism.getInstance().config, player, query)) {
                            throw new IllegalArgumentException(
                                    "Invalid region selected. Make sure you have a region selected,"
                                            + " and that it doesn't exceed the max radius.");
                        }
                    }
                    break;

                // Confine to the chunk
                case "c":
                case "chunk":

                    final Chunk ch = player.getLocation().getChunk();
                    query.setWorld(ch.getWorld().getName());
                    query.setMinLocation(ChunkUtils.getChunkMinVector(ch));
                    query.setMaxLocation(ChunkUtils.getChunkMaxVector(ch));

                    break;

                // User wants no radius, but contained within the current world
                case "world":
                    // Do they have permission to override the global lookup radius
                    if (query.getProcessType().equals(PrismProcessType.LOOKUP)
                            && !player.hasPermission("prism.override-max-lookup-radius")) {
                        throw new IllegalArgumentException(
                                Il8nHelper.getRawMessage("parameter-radius-override-no-permission"));
                    }
                    // Do they have permission to override the global applier radius
                    if (!query.getProcessType().equals(PrismProcessType.LOOKUP)
                            && !player.hasPermission("prism.override-max-applier-radius")) {
                        throw new IllegalArgumentException(
                                Il8nHelper.getRawMessage("parameter-radius-override-no-permission"));
                    }
                    // Use the world defined in the w: param
                    if (query.getWorld() != null) {
                        inputValue = query.getWorld();
                    } else {
                        inputValue = player.getWorld().getName();
                    }
                    query.setWorld(inputValue);
                    query.setAllowNoRadius(true);
                    break;

                // User has asked for a global radius
                case "global":
                    // Do they have permission to override the global lookup radius
                    if (query.getProcessType().equals(PrismProcessType.LOOKUP)
                            && !player.hasPermission("prism.override-max-lookup-radius")) {
                        throw new IllegalArgumentException(
                                Il8nHelper.getRawMessage("parameter-radius-override-no-permission"));
                    }
                    // Do they have permission to override the global applier radius
                    if (!query.getProcessType().equals(PrismProcessType.LOOKUP)
                            && !player.hasPermission("prism.override-max-applier-radius")) {
                        throw new IllegalArgumentException(
                                Il8nHelper.getRawMessage("parameter-radius-override-no-permission"));
                    }
                    // Either they have permission or player is null
                    query.setWorld(null);
                    query.setAllowNoRadius(true);

                    break;
                default:
                    throw new IllegalArgumentException(
                            "Radius is invalid. There's a bunch of choice, so use /prism actions for assistance.");
            }
        }
    }

    @Override
    public void defaultTo(QueryParameters query, CommandSender sender) {
        if (query.getProcessType().equals(PrismProcessType.DELETE)) {
            return;
        }
        if (sender instanceof Player) {
            if (!query.allowsNoRadius()) {
                query.setRadius(Prism.getInstance().config.parameterConfig.defaultRadius);
                query.addDefaultUsed("r:" + query.getRadius());
            }
        }
    }
}