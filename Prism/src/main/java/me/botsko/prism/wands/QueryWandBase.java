package me.botsko.prism.wands;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.commandlibs.PreprocessArgs;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * A base class for Wands that use
 * {@link me.botsko.prism.actionlibs.QueryParameters} and show results. This
 * will allow users to specify parameters when creating the wand and have them
 * saved in the Wand for every time they use it until it is disabled.
 */
public abstract class QueryWandBase extends WandBase {

    /**
     * Keep an instance of {@link me.botsko.prism.Prism Prism} to use.
     */
    protected final Prism plugin;
    /**
     * The parameters that are specified. Whenever we do a search we can clone this
     * and then add the extra stuff. (Location, etc)
     */
    protected QueryParameters parameters;

    /**
     * When we initialize the class, make the {@link #parameters} equal to a fresh
     * QueryParameters.
     */
    public QueryWandBase(Prism plugin) {
        parameters = new QueryParameters();
        this.plugin = plugin;
    }

    /**
     * Set the field {@link #parameters} with the parameters here. This will be
     * using the stuff in <code>/prism params</code>
     *
     * @param sender   The sender of the command.
     * @param args     The arguments from <code>/prism params</code>.
     * @param argStart What argument to start on.
     */
    public boolean setParameters(Player sender, String[] args, int argStart) {
        final PrismProcessType processType = this instanceof RollbackWand ? PrismProcessType.ROLLBACK
                : this instanceof RestoreWand ? PrismProcessType.RESTORE : PrismProcessType.LOOKUP;

        final QueryParameters params = PreprocessArgs.process(plugin.config, sender, args, processType, argStart,
                false, true);
        if (params == null) {
            return false;
        } else {
            params.resetMinMaxVectors();
            this.parameters = params;
            return true;
        }
    }

    QueryResult getResult(PrismParameters params, Player player) {
        boolean timeDefault = false;
        for (final String _default : params.getDefaultsUsed()) {
            if (_default.startsWith("t:")) {
                timeDefault = true;
                break;
            }
        }
        if (timeDefault) {
            params.setIgnoreTime(true);
        }

        // Query
        final ActionsQuery aq = new ActionsQuery(plugin);
        return aq.lookup(params, player);
    }

    QueryParameters checkQueryParams(Block block, QueryParameters parameters, Player player) {
        plugin.eventTimer.recordTimedEvent("rollback wand used");
        QueryParameters params;
        try {
            params = parameters.clone();
        } catch (final CloneNotSupportedException ex) {
            params = new QueryParameters();
            Prism.messenger.sendMessage(player,
                    Prism.messenger.playerError("Error retrieving parameters. Checking with default parameters."));
        }
        params.setWorld(player.getWorld().getName());
        params.setSpecificBlockLocation(block.getLocation());
        params.setLimit(1);
        return params;
    }

    /**
     * Get the {@link #parameters} set from {@link #setParameters}.
     *
     * @return The wand's {@link me.botsko.prism.actionlibs.QueryParameters}.
     */
    public QueryParameters getParameters() {
        return parameters;
    }
}
