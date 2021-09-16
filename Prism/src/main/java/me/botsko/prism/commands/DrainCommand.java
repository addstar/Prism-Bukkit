package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.api.BlockStateChange;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import me.botsko.prism.events.EventHelper;
import me.botsko.prism.events.PrismDrainEvent;
import me.botsko.prism.utils.TypeUtils;
import me.botsko.prism.utils.block.Utilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;

public class DrainCommand implements SubHandler {

    private final Prism plugin;

    /**
     * Constructor.
     * @param plugin Prism
     */
    public DrainCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(CallInfo call) {

        String drainType = "";
        int radius = plugin.config.drainCommandConfig.defaultRadius;
        if (call.getArgs().length == 3) {
            if (call.getArg(1).equalsIgnoreCase("water") || call.getArg(1).equalsIgnoreCase("lava")) {
                drainType = call.getArg(1);
            } else {

                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerError("Invalid drain type. Must be lava, water, or left out."));
                return;
            }
            // Validate radius
            radius = validateRadius(call, call.getArg(2));
        } else if (call.getArgs().length == 2) {
            if (TypeUtils.isNumeric(call.getArg(1))) {
                radius = validateRadius(call, call.getArg(1));
            } else {
                if (call.getArg(1).equalsIgnoreCase("water") || call.getArg(1).equalsIgnoreCase("lava")) {
                    drainType = call.getArg(1);
                } else {
                    Prism.messenger.sendMessage(call.getPlayer(),
                            Prism.messenger.playerError("Invalid drain type. Must be lava, water, or left out."));
                    return;
                }
            }
        }

        if (radius == 0) {
            return;
        }

        TextComponent.Builder builder = Component.text()
                .append(Il8nHelper.formatMessage("command-drain-lookup", drainType, radius));
        String key = "command-drain-lookup-water";
        if (drainType.equals("lava")) {
            key = "command-drain-lookup-lava";
        }
        builder.append(Component.text(" ")).append(Il8nHelper.getMessage(key).color(NamedTextColor.GRAY));

        Prism.messenger.sendMessage(call.getPlayer(), Prism.messenger.playerHeaderMsg(builder.build()));

        ArrayList<BlockStateChange> blockStateChanges = null;
        if (drainType.isEmpty()) {
            blockStateChanges = Utilities.drain(call.getPlayer().getLocation(), radius);
        } else if (drainType.equals("water")) {
            blockStateChanges = Utilities.drainWater(call.getPlayer().getLocation(), radius);
        } else if (drainType.equals("lava")) {
            blockStateChanges = Utilities.drainLava(call.getPlayer().getLocation(), radius);
        }

        if (blockStateChanges != null && !blockStateChanges.isEmpty()) {

            // @todo remove the extra space in msg
            Component out = Prism.messenger
                    .playerHeaderMsg(Il8nHelper.formatMessage("command-drain-lookup-result",
                            blockStateChanges.size(), drainType))
                    .append(Component.newline())
                    .append(Prism.messenger.playerSubduedHeaderMsg(Il8nHelper.getMessage("command-drain-result-undo")));
            Prism.messenger.sendMessage(call.getSender(), out);

            // Trigger the event
            final PrismDrainEvent event = EventHelper.createDrainEvent(blockStateChanges, call.getPlayer(), radius);
            plugin.getServer().getPluginManager().callEvent(event);

        } else {
            Prism.messenger.sendMessage(call.getPlayer(),
                    Prism.messenger.playerError(Il8nHelper.getMessage("command-drain-result-empty")));
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-drain-radius")};
    }

    @Override
    public String getRef() {
        return "/drain.html";
    }

    protected int validateRadius(CallInfo call, String radiusArg) {
        if (TypeUtils.isNumeric(radiusArg)) {
            final int _tmp_radius = Integer.parseInt(radiusArg);
            if (_tmp_radius > 0) {
                if (_tmp_radius > plugin.config.drainCommandConfig.maxRadius) {
                    Prism.messenger.sendMessage(call.getPlayer(),
                            Prism.messenger.playerError(Il8nHelper.getMessage("exceed-max-radius")));
                    return 0;
                } else {
                    return _tmp_radius;
                }
            } else {
                Prism.messenger.sendMessage(call.getPlayer(), Prism.messenger.playerError(
                        Il8nHelper.getMessage("radius-small")));
                return 0;
            }
        } else {
            Prism.messenger.sendMessage(call.getPlayer(), Prism.messenger.playerError(
                    Il8nHelper.getMessage("radius-not-numeric")));
            return 0;
        }
    }
}