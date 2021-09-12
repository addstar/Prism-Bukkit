package me.botsko.prism.actionlibs;

import me.botsko.prism.Il8nHelper;
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
import me.botsko.prism.exceptions.InvalidActionException;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ActionRegistry {

    private final TreeMap<String, ActionTypeImpl> registeredActions = new TreeMap<>();

    public ActionRegistry() {
        registerPrismDefaultActions();
    }

    /**
     * Register a new action type for event recording, lookups, etc.
     *
     * @param actionType type
     */
    private void registerAction(ActionTypeImpl actionType) {
        registeredActions.put(actionType.getName(), actionType);
    }

    /**
     * Register a new action type for event recording, lookups, etc.  Actions must have a name with
     * 2 hyphens.  And the plugin must be on the allowed list of plugins.
     *
     * @param actionType type
     * @throws InvalidActionException if action not allowed.
     */
    @SuppressWarnings("unused")
    public void registerCustomAction(Plugin apiPlugin, ActionTypeImpl actionType) throws InvalidActionException {
        final List<String> allowedPlugins = Prism.config.getStringList("prism.tracking.api.allowed-plugins");
        if (!allowedPlugins.contains(apiPlugin.getName())) {
            throw new InvalidActionException("Registering action type not allowed. Plugin '" + apiPlugin.getName()
                    + "' is not in list of allowed plugins.");
        }
        if (TypeUtils.subStrOccurences(actionType.getName(), "-") != 2) {
            throw new InvalidActionException("Invalid action type. Custom actions must contain two hyphens.");
        }
        Prism.getPrismDataSource().addActionName(actionType.getName());
        registeredActions.put(actionType.getName(), actionType);
    }

    public TreeMap<String, ActionTypeImpl> getRegisteredAction() {
        return registeredActions;
    }

    public ActionTypeImpl getAction(String name) {
        return registeredActions.get(name);
    }

    /**
     * Retrieve a list of actions based on a search criteria.
     *
     * @param name to search by
     * @return {@code List<ActionType>}
     */
    public ArrayList<ActionTypeImpl> getActionsByShortName(String name) {
        final ArrayList<ActionTypeImpl> actions = new ArrayList<>();
        for (final Entry<String, ActionTypeImpl> entry : registeredActions.entrySet()) {
            // Match either the name or the short name
            if (entry.getValue().getFamilyName().equals(name) || entry.getValue().getShortName().equals(name)
                    || entry.getValue().getName().equals(name)) {
                actions.add(entry.getValue());
            }
        }
        return actions;
    }

    /**
     * List all.
     *
     * @return list
     */
    public String[] listAll() {
        final String[] names = new String[registeredActions.size()];
        int i = 0;
        for (final Entry<String, ActionTypeImpl> entry : registeredActions.entrySet()) {
            names[i] = entry.getKey();
            i++;
        }
        return names;
    }

    /**
     * List that allow rollback.
     *
     * @return list
     */
    @SuppressWarnings("unused")
    public ArrayList<String> listActionsThatAllowRollback() {
        final ArrayList<String> names = new ArrayList<>();
        for (final Entry<String, ActionTypeImpl> entry : registeredActions.entrySet()) {
            if (entry.getValue().canRollback()) {
                names.add(entry.getKey());
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
        for (final Entry<String, ActionTypeImpl> entry : registeredActions.entrySet()) {
            if (entry.getValue().canRestore()) {
                names.add(entry.getKey());
            }
        }
        return names;
    }

    private void registerPrismDefaultActions() {

        registerAction(new ActionTypeImpl("block-break", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionTypeImpl("block-burn", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("burned")));
        registerAction(new ActionTypeImpl("block-dispense", false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("dispensed")));
        registerAction(new ActionTypeImpl("block-fade", false, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("faded")));
        registerAction(new ActionTypeImpl("block-fall", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("fell")));
        registerAction(new ActionTypeImpl("block-form", false, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("formed")));
        registerAction(new ActionTypeImpl("block-place", true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("placed")));
        registerAction(new ActionTypeImpl("block-shift", true, false, false,
                BlockShiftAction.class, Il8nHelper.getRawMessage("moved")));
        registerAction(new ActionTypeImpl("block-spread", true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("grew")));
        registerAction(new ActionTypeImpl("block-use", false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("used")));
        registerAction(new ActionTypeImpl("bonemeal-use", false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("used")));
        registerAction(new ActionTypeImpl("bucket-fill", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("filled")));
        registerAction(new ActionTypeImpl("cake-eat", false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("ate")));
        registerAction(new ActionTypeImpl("container-access", false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("accessed")));
        registerAction(new ActionTypeImpl("craft-item", false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("crafted")));
        registerAction(new ActionTypeImpl("creeper-explode", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionTypeImpl("crop-trample", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("trampled")));
        registerAction(new ActionTypeImpl("dragon-eat", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("ate")));
        registerAction(new ActionTypeImpl("enchant-item", false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("enchanted")));
        registerAction(new ActionTypeImpl("enderman-pickup", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("picked-up")));
        registerAction(new ActionTypeImpl("enderman-place", true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("placed")));
        registerAction(new ActionTypeImpl("entity-break", true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionTypeImpl("entity-dye", false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("dyed")));
        registerAction(new ActionTypeImpl("entity-explode", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionTypeImpl("entity-follow", false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("lured")));
        registerAction(new ActionTypeImpl("entity-form", true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("formed")));
        registerAction(new ActionTypeImpl("entity-kill", false, true, false,
                EntityAction.class, Il8nHelper.getRawMessage("killed")));
        registerAction(new ActionTypeImpl("entity-leash", true, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("leashed")));
        registerAction(new ActionTypeImpl("entity-shear", false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("sheared")));
        registerAction(new ActionTypeImpl("entity-spawn", false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("spawned")));
        registerAction(new ActionTypeImpl("entity-unleash", false, false, false,
                EntityAction.class, Il8nHelper.getRawMessage("unleashed")));
        registerAction(new ActionTypeImpl("fireball", false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ignited")));
        registerAction(new ActionTypeImpl("fire-spread", true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("spread")));
        registerAction(new ActionTypeImpl("firework-launch", false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("launched")));
        registerAction(new ActionTypeImpl("hangingitem-break", false, true, true,
                HangingItemAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionTypeImpl("hangingitem-place", true, true, true,
                HangingItemAction.class, Il8nHelper.getRawMessage("hung")));
        registerAction(new ActionTypeImpl("item-drop", false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("dropped")));
        registerAction(new ActionTypeImpl("item-insert", false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("inserted")));
        registerAction(new ActionTypeImpl("item-pickup", false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("picked-up")));
        registerAction(new ActionTypeImpl("item-remove", false, true, true,
                ItemStackAction.class, Il8nHelper.getRawMessage("removed")));
        registerAction(new ActionTypeImpl("item-break", false, false, false,
                ItemStackAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionTypeImpl("item-rotate", false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("turned-item")));
        registerAction(new ActionTypeImpl("lava-break", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionTypeImpl("lava-bucket", true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("poured")));
        registerAction(new ActionTypeImpl("lava-flow", true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("flowed-into")));
        registerAction(new ActionTypeImpl("lava-ignite", false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ignited")));
        registerAction(new ActionTypeImpl("leaf-decay", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("decayed")));
        registerAction(new ActionTypeImpl("lighter", false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("set-fire")));
        registerAction(new ActionTypeImpl("lightning", false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ignited")));
        registerAction(new ActionTypeImpl("mushroom-grow", true, true, true,
                GrowAction.class, Il8nHelper.getRawMessage("grew")));
        registerAction(new ActionTypeImpl("player-chat", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("said")));
        registerAction(new ActionTypeImpl("player-command", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("command-run")));
        registerAction(new ActionTypeImpl("player-death", false, false, false,
                PlayerDeathAction.class, Il8nHelper.getRawMessage("died")));
        registerAction(new ActionTypeImpl("player-join", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("joined")));
        registerAction(new ActionTypeImpl("player-kill", false, true, false,
                EntityAction.class, Il8nHelper.getRawMessage("killed")));
        registerAction(new ActionTypeImpl("player-quit", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("quit")));
        registerAction(new ActionTypeImpl("player-gamemodechange", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("changed_game_mode")));
        registerAction(new ActionTypeImpl("player-teleport", false, false, false,
                EntityTravelAction.class, Il8nHelper.getRawMessage("teleported")));
        registerAction(new ActionTypeImpl("portal-create", true, true, true,
                PortalCreateAction.class, "created portal"));
        registerAction(new ActionTypeImpl("potion-splash", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("potion-throw")));
        registerAction(new ActionTypeImpl("prism-drain", false, true, true,
                PrismRollbackAction.class, Il8nHelper.getRawMessage("drained")));
        registerAction(new ActionTypeImpl("prism-extinguish", false, true, true,
                PrismRollbackAction.class, Il8nHelper.getRawMessage("extinguished")));
        registerAction(new ActionTypeImpl("prism-process", false, false, false,
                PrismProcessAction.class, Il8nHelper.getRawMessage("ran-process")));
        registerAction(new ActionTypeImpl("prism-rollback", true, false, false,
                PrismRollbackAction.class, Il8nHelper.getRawMessage("rolled-back")));
        registerAction(new ActionTypeImpl("sheep-eat", false, false, false,
                BlockAction.class, Il8nHelper.getRawMessage("ate")));
        registerAction(new ActionTypeImpl("sign-change", false, false, true,
                SignAction.class, Il8nHelper.getRawMessage("wrote")));
        registerAction(new ActionTypeImpl("spawnegg-use", false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("used")));
        registerAction(new ActionTypeImpl("tnt-explode", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionTypeImpl("bed-explode", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("blew-up")));
        registerAction(new ActionTypeImpl("tnt-prime", false, false, false,
                UseAction.class, Il8nHelper.getRawMessage("primed")));
        registerAction(new ActionTypeImpl("tree-grow", true, true, true,
                GrowAction.class, Il8nHelper.getRawMessage("grew")));
        registerAction(new ActionTypeImpl("vehicle-break", false, true, false,
                VehicleAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionTypeImpl("vehicle-enter", false, false, false,
                VehicleAction.class, Il8nHelper.getRawMessage("entered")));
        registerAction(new ActionTypeImpl("vehicle-exit", false, false, false,
                VehicleAction.class, Il8nHelper.getRawMessage("exited")));
        registerAction(new ActionTypeImpl("vehicle-place", true, false, false,
                VehicleAction.class, Il8nHelper.getRawMessage("placed")));
        registerAction(new ActionTypeImpl("water-break", false, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("broke")));
        registerAction(new ActionTypeImpl("water-bucket", true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("poured")));
        registerAction(new ActionTypeImpl("water-flow", true, true, true,
                BlockAction.class, Il8nHelper.getRawMessage("flowed-into")));
        registerAction(new ActionTypeImpl("world-edit", true, true, true,
                BlockChangeAction.class, Il8nHelper.getRawMessage("edited")));
        registerAction(new ActionTypeImpl("xp-pickup", false, false, false,
                PlayerAction.class, Il8nHelper.getRawMessage("picked-up")));
        if (Prism.isPaper) {
            registerAction(new ActionTypeImpl("target-hit", false, false, false,
                    BlockAction.class, Il8nHelper.getRawMessage("hit_and_triggered")));
            registerAction(new ActionTypeImpl("player-trade", false, false, false,
                    EntityAction.class, Il8nHelper.getRawMessage("traded_with")));
            registerAction(new ActionTypeImpl("item-receive", false, true, true,
                    ItemStackAction.class, Il8nHelper.getRawMessage("received")));
        }
    }
}