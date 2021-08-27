package me.botsko.prism.actionlibs;

import me.botsko.prism.Prism;
import me.botsko.prism.actions.PrismProcessAction;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.api.commands.Flag;
import me.botsko.prism.database.DeleteQuery;
import me.botsko.prism.database.SelectIdQuery;
import me.botsko.prism.database.SelectProcessActionQuery;
import me.botsko.prism.database.SelectQuery;
import me.botsko.prism.database.UpdateQuery;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActionsQuery {

    private final Prism plugin;
    private final SelectQuery qb;
    private boolean shouldPauseDB = false;

    public ActionsQuery(Prism plugin) {
        this.plugin = plugin;
        this.qb = Prism.getPrismDataSource().createSelectQuery();
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

    public QueryResult lookup(QueryParameters parameters) {
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
            shouldGroup = plugin.getConfig().getBoolean("prism.queries.lookup-auto-group");
            // Any overriding flags passed?
            if (parameters.hasFlag(Flag.NO_GROUP) || parameters.hasFlag(Flag.EXTENDED)) {
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
     * @param playername player.
     * @return long
     */
    public long getUsersLastPrismProcessId(String playername) {
        SelectProcessActionQuery q = Prism.getPrismDataSource().createProcessQuery();
        QueryParameters parameters = new QueryParameters();
        parameters.setKeyword(playername);
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
        SelectProcessActionQuery q = Prism.getPrismDataSource().createProcessQuery();
        QueryParameters parameters = new QueryParameters();
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
     * @deprecated use {@link this#getQueryExtents(QueryParameters)}
     */
    @Deprecated
    public long getMinIdForQuery(QueryParameters parameters) {
        final SelectIdQuery idQ = Prism.getPrismDataSource().createSelectIdQuery();
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
     * @deprecated use {@link this#getQueryExtents(QueryParameters)}
     */
    @Deprecated
    public long getMaxIdForQuery(QueryParameters parameters) {
        final SelectIdQuery idQ = Prism.getPrismDataSource().createSelectIdQuery();
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
    public long[] getQueryExtents(QueryParameters parameters) {
        final SelectIdQuery idQ = Prism.getPrismDataSource().createSelectIdQuery();
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
    public int delete(QueryParameters parameters) {
        final DeleteQuery dqb = Prism.getPrismDataSource().createDeleteQuery();
        dqb.setParameters(parameters);
        dqb.setShouldGroup(false);//make it clear that we dont want to group for deletes
        dqb.setShouldPause(shouldPauseDB); //will stop recording queue
        return dqb.execute();
    }

    /**
     * Update the rollbacked state of actions.
     *
     * @param handlers the handlers need to update.
     */
    public void updateRollbacked(Handler... handlers) {
        final UpdateQuery updateQuery = Prism.getPrismDataSource().createUpdateQuery();
        updateQuery.updateRollbacked(handlers);
    }
}