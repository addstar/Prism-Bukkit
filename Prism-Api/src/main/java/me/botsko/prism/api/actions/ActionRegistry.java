package me.botsko.prism.api.actions;

import me.botsko.prism.exceptions.InvalidActionException;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created for Prism.
 *
 * @author Narimm on 16/08/2021
 * @since 2.1.8-SNAPSHOT
 */
public interface ActionRegistry {

    void registerCustomAction(Plugin apiPlugin, Action action)  throws InvalidActionException;

    Map<ActionType, List<Action>> getRegisteredActions();

    /**
     * Returns an Action by its name. For regular actions use the
     * {@link ActionRegistry#getAction(ActionType)}
     * for custom Actions use {@link ActionRegistry#getCustomAction(String)}
     *
     * @param name String
     * @return Action
     * @deprecated use {@link ActionRegistry#getAction(ActionType)}
     */
    @Deprecated
    Action getActionByName(String name);

    Action getAction(ActionType name);

    Action getCustomAction(String name);

    ArrayList<Action> getActionsByShortName(String name);

    ArrayList<Action> getActionsByFamilyName(String name);

    String[] listAll();

    ArrayList<String> listActionsThatAllowRollback();

    ArrayList<String> listActionsThatAllowRestore();
}
