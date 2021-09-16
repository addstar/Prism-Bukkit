package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.api.BlockStateChange;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import me.botsko.prism.events.EventHelper;
import me.botsko.prism.events.PrismExtinguishEvent;
import me.botsko.prism.utils.TypeUtils;
import me.botsko.prism.utils.block.Utilities;
import net.kyori.adventure.identity.Identity;

import java.util.ArrayList;
import java.util.List;

public class ExtinguishCommand implements SubHandler {

    private final Prism plugin;

    /**
     * Constructor.
     * @param plugin Prism
     */
    public ExtinguishCommand(Prism plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle the command.
     */
    @Override
    public void handle(CallInfo call) {

        int radius = plugin.config.extinguishCommandConfig.defaultRadius;
        if (call.getArgs().length == 2) {
            if (TypeUtils.isNumeric(call.getArg(1))) {
                final int _tmp_radius = Integer.parseInt(call.getArg(1));
                if (_tmp_radius > 0) {
                    if (_tmp_radius > plugin.config.extinguishCommandConfig.maxRadius) {
                        Prism.messenger.sendMessage(call.getPlayer(),
                                Prism.messenger.playerError(Il8nHelper.getMessage("radius-max")));
                        return;
                    } else {
                        radius = _tmp_radius;
                    }
                } else {
                    Prism.messenger.sendMessage(call.getPlayer(),
                            Prism.messenger.playerError(Il8nHelper.getMessage("radius-small")));
                    return;
                }
            } else {
                Prism.messenger.sendMessage(call.getPlayer(), Prism.messenger.playerError(
                        Il8nHelper.getMessage("radius-not-numeric")));
                return;
            }
        }

        final ArrayList<BlockStateChange> blockStateChanges = Utilities.extinguish(call.getPlayer().getLocation(),
                radius);
        if (!blockStateChanges.isEmpty()) {

            Prism.messenger.sendMessage(call.getPlayer(),
                    Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("fire-extinguished-sucess")));

            // Trigger the event
            final PrismExtinguishEvent event = EventHelper.createExtinguishEvent(blockStateChanges, call.getPlayer(),
                    radius);
            plugin.getServer().getPluginManager().callEvent(event);

        } else {
            Prism.getAudiences().player(call.getPlayer())
                    .sendMessage(Identity.nil(),
                            Prism.messenger.playerError(Il8nHelper.getMessage("no-fires-found")));
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-extinguish-radius")};
    }

    @Override
    public String getRef() {
        return "/extinguish.html";
    }
}