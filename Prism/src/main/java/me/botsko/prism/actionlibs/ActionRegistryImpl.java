package me.botsko.prism.actionlibs;

import com.google.common.collect.Lists;
import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
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
import me.botsko.prism.actions.PrismProcessAction;
import me.botsko.prism.actions.PrismRollbackAction;
import me.botsko.prism.actions.SignAction;
import me.botsko.prism.actions.UseAction;
import me.botsko.prism.actions.VehicleAction;
import me.botsko.prism.api.actions.Action;
import me.botsko.prism.api.actions.ActionRegistry;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.exceptions.InvalidActionException;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static me.botsko.prism.api.actions.ActionType.BED_EXPLODE;
import static me.botsko.prism.api.actions.ActionType.BLOCK_BREAK;
import static me.botsko.prism.api.actions.ActionType.BLOCK_BURN;
import static me.botsko.prism.api.actions.ActionType.BLOCK_DISPENSE;
import static me.botsko.prism.api.actions.ActionType.BLOCK_FADE;
import static me.botsko.prism.api.actions.ActionType.BLOCK_FALL;
import static me.botsko.prism.api.actions.ActionType.BLOCK_FORM;
import static me.botsko.prism.api.actions.ActionType.BLOCK_PLACE;
import static me.botsko.prism.api.actions.ActionType.BLOCK_SHIFT;
import static me.botsko.prism.api.actions.ActionType.BLOCK_SPREAD;
import static me.botsko.prism.api.actions.ActionType.BLOCK_USE;
import static me.botsko.prism.api.actions.ActionType.BUCKET_FILL;
import static me.botsko.prism.api.actions.ActionType.CAKE_EAT;
import static me.botsko.prism.api.actions.ActionType.CONTAINER_ACCESS;
import static me.botsko.prism.api.actions.ActionType.CRAFT_ITEM;
import static me.botsko.prism.api.actions.ActionType.CREEPER_EXPLODE;
import static me.botsko.prism.api.actions.ActionType.CROP_TRAMPLE;
import static me.botsko.prism.api.actions.ActionType.CUSTOM_ACTION;
import static me.botsko.prism.api.actions.ActionType.DRAGON_EAT;
import static me.botsko.prism.api.actions.ActionType.ENCHANT_ITEM;
import static me.botsko.prism.api.actions.ActionType.ENDERMAN_PICKUP;
import static me.botsko.prism.api.actions.ActionType.ENDERMAN_PLACE;
import static me.botsko.prism.api.actions.ActionType.ENTITY_BREAK;
import static me.botsko.prism.api.actions.ActionType.ENTITY_DYE;
import static me.botsko.prism.api.actions.ActionType.ENTITY_EXPLODE;
import static me.botsko.prism.api.actions.ActionType.ENTITY_FOLLOW;
import static me.botsko.prism.api.actions.ActionType.ENTITY_FORM;
import static me.botsko.prism.api.actions.ActionType.ENTITY_KILL;
import static me.botsko.prism.api.actions.ActionType.ENTITY_LEASH;
import static me.botsko.prism.api.actions.ActionType.ENTITY_SHEAR;
import static me.botsko.prism.api.actions.ActionType.ENTITY_SPAWN;
import static me.botsko.prism.api.actions.ActionType.ENTITY_UNLEASH;
import static me.botsko.prism.api.actions.ActionType.FIREBALL;
import static me.botsko.prism.api.actions.ActionType.FIREWORK_LAUNCH;
import static me.botsko.prism.api.actions.ActionType.FIRE_SPREAD;
import static me.botsko.prism.api.actions.ActionType.HANGINGITEM_BREAK;
import static me.botsko.prism.api.actions.ActionType.HANGINGITEM_PLACE;
import static me.botsko.prism.api.actions.ActionType.ITEM_BREAK;
import static me.botsko.prism.api.actions.ActionType.ITEM_DROP;
import static me.botsko.prism.api.actions.ActionType.ITEM_INSERT;
import static me.botsko.prism.api.actions.ActionType.ITEM_PICKUP;
import static me.botsko.prism.api.actions.ActionType.ITEM_RECEIVE;
import static me.botsko.prism.api.actions.ActionType.ITEM_REMOVE;
import static me.botsko.prism.api.actions.ActionType.ITEM_ROTATE;
import static me.botsko.prism.api.actions.ActionType.LAVA_BREAK;
import static me.botsko.prism.api.actions.ActionType.LAVA_BUCKET;
import static me.botsko.prism.api.actions.ActionType.LAVA_FLOW;
import static me.botsko.prism.api.actions.ActionType.LAVA_IGNITE;
import static me.botsko.prism.api.actions.ActionType.LEAF_DECAY;
import static me.botsko.prism.api.actions.ActionType.LIGHTER;
import static me.botsko.prism.api.actions.ActionType.LIGHTNING;
import static me.botsko.prism.api.actions.ActionType.MUSHROOM_GROW;
import static me.botsko.prism.api.actions.ActionType.PLAYER_CHAT;
import static me.botsko.prism.api.actions.ActionType.PLAYER_COMMAND;
import static me.botsko.prism.api.actions.ActionType.PLAYER_DEATH;
import static me.botsko.prism.api.actions.ActionType.PLAYER_GAMEMODECHANGE;
import static me.botsko.prism.api.actions.ActionType.PLAYER_JOIN;
import static me.botsko.prism.api.actions.ActionType.PLAYER_KILL;
import static me.botsko.prism.api.actions.ActionType.PLAYER_QUIT;
import static me.botsko.prism.api.actions.ActionType.PLAYER_TELEPORT;
import static me.botsko.prism.api.actions.ActionType.PLAYER_TRADE;
import static me.botsko.prism.api.actions.ActionType.POTION_SPLASH;
import static me.botsko.prism.api.actions.ActionType.PRISM_DRAIN;
import static me.botsko.prism.api.actions.ActionType.PRISM_EXTINGUISH;
import static me.botsko.prism.api.actions.ActionType.PRISM_PROCESS;
import static me.botsko.prism.api.actions.ActionType.PRISM_ROLLBACK;
import static me.botsko.prism.api.actions.ActionType.SHEEP_EAT;
import static me.botsko.prism.api.actions.ActionType.SIGN_CHANGE;
import static me.botsko.prism.api.actions.ActionType.SPAWNEGG_USE;
import static me.botsko.prism.api.actions.ActionType.TARGET_HIT;
import static me.botsko.prism.api.actions.ActionType.TNT_EXPLODE;
import static me.botsko.prism.api.actions.ActionType.TNT_PRIME;
import static me.botsko.prism.api.actions.ActionType.TREE_GROW;
import static me.botsko.prism.api.actions.ActionType.VEHICLE_BREAK;
import static me.botsko.prism.api.actions.ActionType.VEHICLE_ENTER;
import static me.botsko.prism.api.actions.ActionType.VEHICLE_PLACE;
import static me.botsko.prism.api.actions.ActionType.WATER_BREAK;
import static me.botsko.prism.api.actions.ActionType.WATER_BUCKET;
import static me.botsko.prism.api.actions.ActionType.WATER_FLOW;
import static me.botsko.prism.api.actions.ActionType.WORLD_EDIT;
import static me.botsko.prism.api.actions.ActionType.XP_PICKUP;

public class ActionRegistryImpl implements ActionRegistry {

    public static final HashMap<ActionType, Integer> prismActions = new HashMap<>();
    private final TreeMap<ActionType, List<Action>> registeredActions = new TreeMap<>();

    private final Map<String,Action> customRegisteredActions = new HashMap<>();


    public ActionRegistryImpl() {
        registerPrismDefaultActions();
    }

    /**
     * Register a new action type for event recording, lookups, etc.
     *
     * @param action ActionImpl.
     */
    private void registerAction(Action action) {
        List<Action> actions = registeredActions.get(action.getActionType());
        if (actions == null) {
            actions = Lists.newArrayList(action);
        } else {
            actions.add(action);
        }
        registeredActions.put(action.getActionType(), actions);
    }

    /**
     * Register a new action for event recording, lookups, etc.  Actions must have a name with
     * 2 hyphens.  And the plugin must be on the allowed list of plugins.
     *
     * @param apiPlugin Plugin
     * @param action    Action
     * @throws InvalidActionException if action not allowed.
     */
    @SuppressWarnings("unused")
    @Override
    public void registerCustomAction(Plugin apiPlugin, Action action) throws InvalidActionException {
        final List<String> allowedPlugins = Prism.getInstance().config.trackingConfig.allowedPlugins;
        if (!allowedPlugins.contains(apiPlugin.getName())) {
            throw new InvalidActionException("Registering action type not allowed. Plugin '" + apiPlugin.getName()
                    + "' is not in list of allowed plugins.");
        }
        if (action.getActionType() != CUSTOM_ACTION) {
            throw new InvalidActionException("Invalid action type. To register a new action you must assign it"
                    + " the TYPE - CUSTOM_ACTION see help docs");


        }
        if (TypeUtils.subStrOccurences(action.getName(), "-") != 2) {
            throw new InvalidActionException("Invalid action type. Custom actions must contain two hyphens.=>"
                    + action.getName());
        }
        String[] splitter = action.getName().split("-");
        if (splitter[1].equalsIgnoreCase("custom") || splitter[2].equalsIgnoreCase("action")) {
            throw new InvalidActionException("Invalid action type. Custom actions must start with custom-action-"
                    + " followed by a unique name");
        }

        Prism.getInstance().getPrismDataSource().addActionName(action.getActionType());
        List<Action> actions = registeredActions.get(action.getActionType());
        if (actions == null) {
            actions = Collections.singletonList(action);
        } else {
            for (Action a : actions) {
                if (a.getName().equals(action.getName())) {
                    throw new InvalidActionException("Invalid action type. Custom actions must have a unique name: "
                            + action.getName());
                }
            }
            actions.add(action);
        }
        registeredActions.put(action.getActionType(), actions);
        customRegisteredActions.put(action.getName(),action);
    }

    @Deprecated
    public Map<ActionType, List<Action>> getRegisteredAction() {
        return getRegisteredActions();
    }

    /**
     * Get the Action for the type.
     *
     * @param name String
     * @return Action
     * @deprecated use {@link this#getAction(ActionType)}
     */
    @Deprecated
    public Action getAction(String name) {
        return getAction(ActionType.getByName(name));
    }

    /**
     * Get the Action for the type.
     *
     * @param name ActionType
     * @return Action
     */
    public Action getAction(ActionType name) {
        List<Action> actions = registeredActions.get(name);
        if (actions == null) {
            return null;
        } else {
            if (name == CUSTOM_ACTION) {
                PrismLogHandler.warn("Call to ActionType using CUSTOM ACTION is unlikely"
                        + "to return the correct action");
            }
            return actions.get(0);
        }
    }

    @Override
    @Nullable
    public Action getCustomAction(@Nonnull String name) {
        Action actions = customRegisteredActions.get(name);
        if (actions == null) {
            PrismLogHandler.warn("Custom action " + name + " not found");
        }
        return actions;
    }

    @Override
    public Map<ActionType, List<Action>> getRegisteredActions() {
        return registeredActions;
    }

    @Deprecated
    @Override
    public Action getActionByName(String name) {
        return getAction(ActionType.getByName(name));
    }

    /**
     * Retrieve a list of actions based on a search criteria.
     *
     * @param name to search by
     * @return {@code List<ActionType>}
     */
    public ArrayList<Action> getActionsByShortName(String name) {
        final ArrayList<Action> actions = new ArrayList<>();
        List<ActionType> types = ActionType.getByShortName(name);
        for (ActionType type : types) {
            actions.addAll(registeredActions.get(type));
        }
        return actions;
    }

    /**
     * Get the Actions by Action family name.
     *
     * @param name String.
     * @return List
     */
    public ArrayList<Action> getActionsByFamilyName(String name) {
        final ArrayList<Action> actions = new ArrayList<>();
        List<ActionType> types = ActionType.getByFamilyName(name);
        for (ActionType type : types) {
            actions.addAll(registeredActions.get(type));
        }
        return actions;
    }

    /**
     * List all Action Types.
     *
     * @return list
     */
    public String[] listAll() {
        final String[] names = new String[registeredActions.size()];
        int i = 0;
        for (final Entry<ActionType, List<Action>> entry : registeredActions.entrySet()) {
            names[i] = entry.getKey().name();
            i++;
        }
        return names;
    }

    /**
     * List that allow rollback.
     *
     * @return list
     */
    public ArrayList<String> listActionsThatAllowRollback() {
        final ArrayList<String> names = new ArrayList<>();
        for (final Entry<ActionType, List<Action>> entry : registeredActions.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().forEach(action -> {
                    action.canRollback();
                    names.add(action.getName());
                });
            }
        }
        return names;
    }

    /**
     * List of actions that allow restore.
     *
     * @return list
     */
    @SuppressWarnings("unused")
    public ArrayList<String> listActionsThatAllowRestore() {
        final ArrayList<String> names = new ArrayList<>();
        for (final Entry<ActionType, List<Action>> entry : registeredActions.entrySet()) {
            if (entry.getValue() != null) {
                entry.getValue().forEach(action -> {
                    action.canRestore();
                    names.add(action.getName());
                });
            }
        }
        return names;
    }

    private void registerPrismDefaultActions() {

        registerAction(new ActionImpl(BLOCK_BREAK, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionImpl(BLOCK_BURN, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("burned")));
        registerAction(new ActionImpl(BLOCK_DISPENSE, false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("dispensed")));
        registerAction(new ActionImpl(BLOCK_FADE, false, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("faded")));
        registerAction(new ActionImpl(BLOCK_FALL, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("fell")));
        registerAction(new ActionImpl(BLOCK_FORM, false, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("formed")));
        registerAction(new ActionImpl(BLOCK_PLACE, true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("placed")));
        registerAction(new ActionImpl(BLOCK_SHIFT, true, false, false,
                BlockShiftAction.class, Il8nHelper.getRawMessage("moved")));
        registerAction(new ActionImpl(BLOCK_SPREAD, true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("grew")));
        registerAction(new ActionImpl(BLOCK_USE, false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("used")));
        registerAction(new ActionImpl(BLOCK_USE, false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("used")));
        registerAction(new ActionImpl(BUCKET_FILL, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("filled")));
        registerAction(new ActionImpl(CAKE_EAT, false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("ate")));
        registerAction(new ActionImpl(CONTAINER_ACCESS, false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("accessed")));
        registerAction(new ActionImpl(CRAFT_ITEM, false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("crafted")));
        registerAction(new ActionImpl(CREEPER_EXPLODE, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionImpl(CROP_TRAMPLE, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("trampled")));
        registerAction(new ActionImpl(DRAGON_EAT, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("ate")));
        registerAction(new ActionImpl(ENCHANT_ITEM, false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("enchanted")));
        registerAction(new ActionImpl(ENDERMAN_PICKUP, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("picked-up")));
        registerAction(new ActionImpl(ENDERMAN_PLACE, true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("placed")));
        registerAction(new ActionImpl(ENTITY_BREAK, true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionImpl(ENTITY_DYE, false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("dyed")));
        registerAction(new ActionImpl(ENTITY_EXPLODE, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionImpl(ENTITY_FOLLOW, false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("lured")));
        registerAction(new ActionImpl(ENTITY_FORM, true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("formed")));
        registerAction(new ActionImpl(ENTITY_KILL, false, true, false,
                EntityAction.class, Il8nHelper.getRawMessage("killed")));
        registerAction(new ActionImpl(ENTITY_LEASH, true, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("leashed")));
        registerAction(new ActionImpl(ENTITY_SHEAR, false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("sheared")));
        registerAction(new ActionImpl(ENTITY_SPAWN, false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("spawned")));
        registerAction(new ActionImpl(ENTITY_UNLEASH, false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("unleashed")));
        registerAction(new ActionImpl(FIREBALL, false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ignited")));
        registerAction(new ActionImpl(FIRE_SPREAD, true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("spread")));
        registerAction(new ActionImpl(FIREWORK_LAUNCH, false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("launched")));
        registerAction(new ActionImpl(HANGINGITEM_BREAK, false, true, true,
                HangingItemAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionImpl(HANGINGITEM_PLACE, true, true, true,
                HangingItemAction.class, Il8nHelper.getRawMessage("hung")));
        registerAction(new ActionImpl(ITEM_DROP, false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("dropped")));
        registerAction(new ActionImpl(ITEM_INSERT, false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("inserted")));
        registerAction(new ActionImpl(ITEM_PICKUP, false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("picked-up")));
        registerAction(new ActionImpl(ITEM_REMOVE, false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("removed")));
        registerAction(new ActionImpl(ITEM_BREAK, false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionImpl(ITEM_ROTATE, false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("turned-item")));
        //noinspection DuplicatedCode
        registerAction(new ActionImpl(LAVA_BREAK, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionImpl(LAVA_BUCKET, true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("poured")));
        registerAction(new ActionImpl(LAVA_FLOW, true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("flowed-into")));
        registerAction(new ActionImpl(LAVA_IGNITE, false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ignited")));
        registerAction(new ActionImpl(LEAF_DECAY, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("decayed")));
        registerAction(new ActionImpl(LIGHTER, false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("set-fire")));
        registerAction(new ActionImpl(LIGHTNING, false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ignited")));
        registerAction(new ActionImpl(MUSHROOM_GROW, true, true, true,
                GrowAction.class, Il8nHelper.getRawMessage("grew")));
        registerAction(new ActionImpl(PLAYER_CHAT, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("said")));
        registerAction(new ActionImpl(PLAYER_COMMAND, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("command-run")));
        registerAction(new ActionImpl(PLAYER_DEATH, false, false, false,
                PlayerDeathAction.class, Il8nHelper.getRawMessage("died")));
        registerAction(new ActionImpl(PLAYER_JOIN, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("joined")));
        registerAction(new ActionImpl(PLAYER_KILL, false, true, false,
                EntityAction.class, Il8nHelper.getRawMessage("killed")));
        registerAction(new ActionImpl(PLAYER_QUIT, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("quit")));
        registerAction(new ActionImpl(PLAYER_GAMEMODECHANGE, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("changed_game_mode")));
        registerAction(new ActionImpl(PLAYER_TELEPORT, false, false, false,
                EntityTravelAction.class, Il8nHelper.getRawMessage("teleported")));
        registerAction(new ActionImpl(POTION_SPLASH, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("potion-throw")));
        registerAction(new ActionImpl(PRISM_DRAIN, false, true, true,
                PrismRollbackAction.class, Il8nHelper.getRawMessage("drained")));
        registerAction(new ActionImpl(PRISM_EXTINGUISH, false, true, true,
                PrismRollbackAction.class, Il8nHelper.getRawMessage("extinguished")));
        registerAction(new ActionImpl(PRISM_PROCESS, false, false, false,
                PrismProcessAction.class, Il8nHelper.getRawMessage("ran-process")));
        registerAction(new ActionImpl(PRISM_ROLLBACK, true, false, false,
                PrismRollbackAction.class, Il8nHelper.getRawMessage("rolled-back")));
        registerAction(new ActionImpl(SHEEP_EAT, false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ate")));
        registerAction(new ActionImpl(SIGN_CHANGE, false, false, true,
                SignAction.class, Il8nHelper.getRawMessage("wrote")));
        registerAction(new ActionImpl(SPAWNEGG_USE, false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("used")));
        registerAction(new ActionImpl(TNT_EXPLODE, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionImpl(BED_EXPLODE, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionImpl(TNT_PRIME, false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("primed")));
        registerAction(new ActionImpl(TREE_GROW, true, true, true,
                GrowAction.class, Il8nHelper.getRawMessage("grew")));
        registerAction(new ActionImpl(VEHICLE_BREAK, false, true, false,
                VehicleAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionImpl(VEHICLE_ENTER, false, false, false,
                VehicleAction.class, Il8nHelper.getRawMessage("entered")));
        registerAction(new ActionImpl(VEHICLE_ENTER, false, false, false,
                VehicleAction.class, Il8nHelper.getRawMessage("exited")));
        registerAction(new ActionImpl(VEHICLE_PLACE, true, false, false,
                VehicleAction.class, Il8nHelper.getRawMessage("placed")));
        registerAction(new ActionImpl(WATER_BREAK, false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionImpl(WATER_BUCKET, true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("poured")));
        registerAction(new ActionImpl(WATER_FLOW, true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("flowed-into")));
        registerAction(new ActionImpl(WORLD_EDIT, true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("edited")));
        registerAction(new ActionImpl(XP_PICKUP, false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("picked-up")));
        if (Prism.isPaper) {
            registerAction(new ActionImpl(TARGET_HIT, false, false, false,
                    BlockAction.class, Il8nHelper.getRawMessage("hit_and_triggered")));
            registerAction(new ActionImpl(PLAYER_TRADE, false, false, false,
                    EntityAction.class, Il8nHelper.getRawMessage("traded_with")));
            registerAction(new ActionImpl(ITEM_RECEIVE, false, true, true,
                    ItemStackAction.class, Il8nHelper.getRawMessage("received")));
        }
    }
}