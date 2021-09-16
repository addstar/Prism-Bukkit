package me.botsko.prism.actionlibs;


import me.botsko.prism.Prism;
import me.botsko.prism.actions.PrismProcessAction;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.commands.Flags;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.database.DeleteQuery;
import me.botsko.prism.database.SelectIdQuery;
import me.botsko.prism.database.SelectProcessActionQuery;
import me.botsko.prism.database.SelectQuery;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActionsQuery {

    private final Prism plugin;
    private final PrismConfig config;
    private final SelectQuery qb;
    private boolean shouldPauseDB = false;

    /**
     * Constructor.
     * @param plugin plugin.
     */
    public ActionsQuery(Prism plugin) {
        this.plugin = plugin;
        this.qb = plugin.getPrismDataSource().createSelectQuery();
        this.config = plugin.config;
    }

    /**
     * When set it pauses the db.
     *
     * @param shouldPauseDB boolean.
     */
    @SuppressWarnings("unused")
    public void setShouldPauseDB(boolean shouldPauseDB) {
        this.shouldPauseDB = shouldPauseDB;
    }

    public QueryResult lookup(PrismParameters parameters) {
        return lookup(parameters, null);
    }

    /**
     * Lookup.
     *
     * @param parameters params
     * @param sender     sender
     * @return result.
     */
    public QueryResult lookup(PrismParameters parameters, CommandSender sender) {

        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        // If lookup, determine if we need to group
        boolean shouldGroup = false;
        if (parameters.getProcessType().equals(PrismProcessType.LOOKUP)) {
            // What to default to
            shouldGroup = config.parameterConfig.lookupAutoGroup;
            // Any overriding flags passed?
            if (parameters.hasFlag(Flags.NO_GROUP) || parameters.hasFlag(Flags.EXTENDED)) {
                shouldGroup = false;
            }
        }
        qb.setParameters(parameters);
        qb.setShouldGroup(shouldGroup);
        QueryResult res = qb.executeSelect(plugin.eventTimer);
        // Pull results
        res.setPerPage(parameters.getPerPage());
        // Cache it if we're doing a lookup. Otherwise we don't
        // need a cache.
        if (parameters.getProcessType().equals(PrismProcessType.LOOKUP)) {
            String keyName = "console";
            if (player != null) {
                keyName = player.getName();
            }
            plugin.cachedQueries.remove(keyName);
            plugin.cachedQueries.put(keyName, res);
            // We also need to share these results with the -share-with players.
            for (final CommandSender sharedPlayer : parameters.getSharedPlayers()) {
                plugin.cachedQueries.put(sharedPlayer.getName(), res);
            }
        }

        plugin.eventTimer.recordTimedEvent("results object completed");

        // Return it
        return res;

    }

    /**
     * Get the Users last process id.
     *
     * @param playerName player.
     * @return long
     */
    public long getUsersLastPrismProcessId(String playerName) {
        SelectProcessActionQuery q = Prism.getInstance().getPrismDataSource().createProcessQuery();
        PrismParameters parameters = new QueryParameters();
        parameters.setKeyword(playerName);
        q.setParameters(parameters);
        q.setShouldGroup(false);
        q.isLastProcessID();
        return q.getLastProcessIdQuery();
    }

    /**
     * Get a Process associated with an ID.
     *
     * @param id long.
     * @return Process Action
     */
    public PrismProcessAction getPrismProcessRecord(long id) {
        SelectProcessActionQuery q = Prism.getInstance().getPrismDataSource().createProcessQuery();
        PrismParameters parameters = new QueryParameters();
        parameters.setId(id);
        q.setParameters(parameters);
        q.setShouldGroup(false);
        return q.executeProcessQuery();
    }

    /**
     * Get the minimum id for a param set.
     *
     * @param parameters params
     * @return id
     * @deprecated use {@link this#getQueryExtents(PrismParameters)}
     */
    @Deprecated
    public long getMinIdForQuery(PrismParameters parameters) {
        final SelectIdQuery idQ = Prism.getInstance().getPrismDataSource().createSelectIdQuery();
        idQ.setMin();
        parameters.setMinPrimaryKey(0);
        parameters.setMaxPrimaryKey(0);
        idQ.setParameters(parameters);
        return idQ.execute()[0];
    }

    /**
     * Get the max id for a param set.
     *
     * @param parameters params
     * @return id
     * @deprecated use {@link this#getQueryExtents(PrismParameters)}
     */
    @Deprecated
    public long getMaxIdForQuery(PrismParameters parameters) {
        final SelectIdQuery idQ = Prism.getInstance().getPrismDataSource().createSelectIdQuery();
        idQ.setMax();
        parameters.setMinPrimaryKey(0);
        parameters.setMaxPrimaryKey(0);
        idQ.setParameters(parameters);
        return idQ.execute()[0];
    }

    /**
     * Get min and max in 1 go.
     * @param parameters QueryParams
     * @return array with minID at 0 and maxID at 1
     */
    public long[] getQueryExtents(PrismParameters parameters) {
        final SelectIdQuery idQ = Prism.getInstance().getPrismDataSource().createSelectIdQuery();
        idQ.setMinMax();
        parameters.setMaxPrimaryKey(0);
        parameters.setMinPrimaryKey(0);
        idQ.setParameters(parameters);
        return idQ.execute();
    }

    /**
     * Delete based on params.
     *
     * @param parameters params.
     * @return the number of rows deleted.
     */
    public int delete(PrismParameters parameters) {
        final DeleteQuery dqb = Prism.getInstance().getPrismDataSource().createDeleteQuery();
        dqb.setParameters(parameters);
        dqb.setShouldGroup(false);//make it clear that we dont want to group for deletes
        dqb.setShouldPause(shouldPauseDB); //will stop recording queue
        return dqb.execute();
    }
}