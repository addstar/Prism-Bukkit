package me.botsko.prism.purge;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;

import java.util.concurrent.CopyOnWriteArrayList;

class PurgeTask implements Runnable {

    private final Prism plugin;
    private final CopyOnWriteArrayList<QueryParameters> paramList;
    private final int purgeTickDelay;
    private final PurgeCallback callback;
    private long minId = 0;
    private long maxId = 0;

    /**
     * Used when we dont know the min max.
     *
     * @param plugin Prism
     * @param paramList List
     * @param purgeTickDelay int
     * @param callback Callback
     */
    public PurgeTask(Prism plugin, CopyOnWriteArrayList<QueryParameters> paramList, int purgeTickDelay,
                     PurgeCallback callback) {
        this.plugin = plugin;
        this.paramList = paramList;
        this.purgeTickDelay = purgeTickDelay;
        this.callback = callback;
    }

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public PurgeTask(Prism plugin, CopyOnWriteArrayList<QueryParameters> paramList, int purgeTickDelay, long minId,
                     long maxId, PurgeCallback callback) {
        this.plugin = plugin;
        this.paramList = paramList;
        this.purgeTickDelay = purgeTickDelay;
        this.minId = minId;
        this.maxId = maxId;
        this.callback = callback;
    }

    @Override
    public void run() {

        if (paramList.isEmpty()) {
            return;
        }

        final ActionsQuery aq = new ActionsQuery(plugin);

        // Pull the next-in-line purge param
        final QueryParameters param = paramList.get(0);
        if (minId == 0 && maxId == 0) {
            // First run - Set the min and max IDs
            long[] result = aq.getQueryExtents(param);
            minId = result[0];
            if (minId > 0) {
                maxId = result[1];
            }
        }
        boolean cycleComplete = false;
        int cycleRowsAffected = 0;
        // We're chunking by IDs instead of using LIMIT because
        // that should be a lot better as far as required record lock counts
        // http://mysql.rjweb.org/doc.php/deletebig
        int spread = plugin.config.purgeConfig.recordsPerBatch;
        if (spread <= 1) {
            spread = 10000;
        }
        // Delete includes id < newMinId. This ensures the maxId isn't exceeded on the final chunk
        // and also handles the case where minId == maxId (they need to be different by at least 1).
        long newMinId = Math.min(minId + spread, maxId + 1);
        long startTime = System.nanoTime();
        // Make sure there are rows to potentially delete
        PurgeManager manager = plugin.getTaskManager().getPurgeManager();
        if (maxId > 0) {
            param.setMinPrimaryKey(minId);
            param.setMaxPrimaryKey(newMinId);
            cycleRowsAffected = aq.delete(param);
            manager.addToTotalRecords(cycleRowsAffected);
        }
        // If done, remove rule and mark complete
        if (newMinId >= maxId) {
            paramList.remove(param);
            cycleComplete = true;

        }

        long cycleTime = (System.nanoTime() - startTime) / 1000000L; //ms
        plugin.getTaskManager().getPurgeManager().setMaxCycleTime(cycleTime);
        PrismLogHandler.debug("------------------- ");
        PrismLogHandler.debug("params: " + param.getOriginalCommand());
        PrismLogHandler.debug("minId: " + minId);
        PrismLogHandler.debug("maxId: " + maxId);
        PrismLogHandler.debug("newMinId: " + newMinId);
        PrismLogHandler.debug("cycleRowsAffected: " + cycleRowsAffected);
        PrismLogHandler.debug("cycleComplete: " + cycleComplete);
        PrismLogHandler.debug("plugin.total_records_affected: " + manager.getTotalRecordsAffected());
        PrismLogHandler.debug("-------------------");

        // Send cycle to callback
        callback.cycle(param, cycleRowsAffected, manager.getTotalRecordsAffected(),
                cycleComplete, manager.getMaxCycleTime());

        if (!plugin.isEnabled()) {
            PrismLogHandler.log("Can't schedule new purge tasks as plugin is now disabled. "
                            + "If you're shutting down the server, ignore me.");
            return;
        }

        // If cycle is incomplete, reschedule it
        if (!cycleComplete) {
            manager.scheduleNewPurgeTask(paramList,purgeTickDelay,newMinId,maxId,callback);
        } else {
            // reset counts
            manager.setTotalRecordsAffected(0);
            manager.resetMaxCycleTime();
            if (paramList.isEmpty()) {
                return;
            }
            PrismLogHandler.log("Moving on to next purge rule...");
            // schedule a new task with next param
            manager.scheduleNewPurgeTask(paramList,purgeTickDelay,callback);
        }
    }
}