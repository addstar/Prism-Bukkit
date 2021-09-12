package me.botsko.prism.actionlibs;

import me.botsko.prism.Prism;
import me.botsko.prism.actions.BlockAction;
import me.botsko.prism.actions.BlockChangeAction;
import me.botsko.prism.actions.BlockShiftAction;
import me.botsko.prism.actions.EntityAction;
import me.botsko.prism.actions.EntityTravelAction;
import me.botsko.prism.actions.GrowAction;
import me.botsko.prism.actions.HangingItemAction;
import me.botsko.prism.actions.ItemStackAction;
import me.botsko.prism.actions.PlayerAction;
import me.botsko.prism.actions.PlayerDeathAction;
import me.botsko.prism.actions.PortalCreateAction;
import me.botsko.prism.actions.PrismProcessAction;
import me.botsko.prism.actions.PrismRollbackAction;
import me.botsko.prism.actions.SignAction;
import me.botsko.prism.actions.UseAction;
import me.botsko.prism.actions.VehicleAction;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.exceptions.InvalidActionException;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class HandlerRegistry {

    private final Collection<Class<? extends Handler>> registeredHandlers = new HashSet<>();

    public HandlerRegistry() {
        registerPrismDefaultHandlers();
    }

    /**
     * Register a custom Handler.
     *
     * @param apiPlugin    an allowed plugin
     * @param handlerClass the class to register
     * @throws InvalidActionException InvalidException if plugin not allowed.
     */
    public void registerCustomHandler(Plugin apiPlugin, Class<? extends Handler> handlerClass)
            throws InvalidActionException {

        final List<String> allowedPlugins = Prism.config.getStringList("prism.tracking.api.allowed-plugins");
        if (!allowedPlugins.contains(apiPlugin.getName())) {
            throw new InvalidActionException("Registering action type not allowed. Plugin '" + apiPlugin.getName()
                    + "' is not in list of allowed plugins.");
        }

        final String[] names = handlerClass.getName().split("\\.");
        if (names.length > 0) {
            registeredHandlers.add(handlerClass);
        }
    }

    /**
     * Create an instance of the class.
     *
     * @param handlerClazz the Class
     * @param <T>          extension of Handler
     * @return Handler
     */
    public <T extends Handler> T create(Class<T> handlerClazz) {
        try {
            return handlerClazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to construct handler for " + handlerClazz.getSimpleName(), e);
        }
    }

    private void registerPrismDefaultHandlers() {

        registeredHandlers.add(BlockAction.class);
        registeredHandlers.add(BlockChangeAction.class);
        registeredHandlers.add(BlockShiftAction.class);
        registeredHandlers.add(EntityAction.class);
        registeredHandlers.add(EntityTravelAction.class);
        registeredHandlers.add(GrowAction.class);
        registeredHandlers.add(HangingItemAction.class);
        registeredHandlers.add(ItemStackAction.class);
        registeredHandlers.add(PlayerAction.class);
        registeredHandlers.add(PlayerDeathAction.class);
        registeredHandlers.add(PortalCreateAction.class);
        registeredHandlers.add(PrismProcessAction.class);
        registeredHandlers.add(PrismRollbackAction.class);
        registeredHandlers.add(SignAction.class);
        registeredHandlers.add(UseAction.class);
        registeredHandlers.add(VehicleAction.class);
    }
}