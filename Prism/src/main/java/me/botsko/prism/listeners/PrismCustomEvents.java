package me.botsko.prism.listeners;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.events.PrismCustomPlayerActionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class PrismCustomEvents implements Listener {

    private final Prism plugin;

    /**
     * Create a custom event listener.
     *
     * @param plugin Prism
     */
    public PrismCustomEvents(Prism plugin) {
        this.plugin = plugin;
    }

    /**
     * On Custom action.
     * @param event PrismCustomPlayerActionEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCustomPlayerAction(final PrismCustomPlayerActionEvent event) {
        final List<String> allowedPlugins = plugin.config.trackingConfig.allowedPlugins;
        if (allowedPlugins.contains(event.getPluginName())) {
            RecordingQueue.addToQueue(
                    ActionFactory.createPlayer(event.getActionTypeName(), event.getPlayer(), event.getMessage()));
        }
    }
}