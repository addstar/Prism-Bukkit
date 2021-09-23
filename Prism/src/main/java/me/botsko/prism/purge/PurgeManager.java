package me.botsko.prism.purge;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.commandlibs.PreprocessArgs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class PurgeManager implements Runnable {

    private final List<String> purgeRules;
    private final Prism plugin;
    public ScheduledFuture<?> purgeTask;
    private int totalRecordsAffected;
    private long maxCycleTime = 0;

    /**
     * Create a purge manager.
     *
     * @param plugin     Prism.
     * @param purgeRules list of rules.
     */
    public PurgeManager(Prism plugin, List<String> purgeRules) {
        this.plugin = plugin;
        this.purgeRules = purgeRules;
    }

    public long getMaxCycleTime() {
        return maxCycleTime;
    }

    public void setMaxCycleTime(long time) {
        maxCycleTime = Math.max(maxCycleTime,time);
    }

    public void resetMaxCycleTime() {
        maxCycleTime = 0;
    }

    public int getTotalRecordsAffected() {
        return totalRecordsAffected;
    }

    public void setTotalRecordsAffected(int totalRecordsAffected) {
        this.totalRecordsAffected = totalRecordsAffected;
    }

    public void addToTotalRecords(int additional) {
        totalRecordsAffected = totalRecordsAffected + additional;
    }

    /**
     * This schedules the purge Task for a new schedule of runs.  It builds a parameter list from the purge rules
     * defined in the configuration.
     */
    @Override
    public void run() {

        PrismLogHandler.log("Scheduled purge executor beginning new run...");

        if (!purgeRules.isEmpty()) {

            final CopyOnWriteArrayList<QueryParameters> paramList = new CopyOnWriteArrayList<>();

            for (final String purgeArgs : purgeRules) {

                // Process and validate all of the arguments
                final QueryParameters parameters = PreprocessArgs.process(plugin.config, null, purgeArgs.split(" "),
                      PrismProcessType.DELETE, 0, false);

                if (parameters == null) {
                    PrismLogHandler.log("Invalid parameters for database purge: " + purgeArgs);
                    continue;
                }

                if (parameters.getFoundArgs().size() > 0) {
                    parameters.setStringFromRawArgs(purgeArgs.split(" "), 0);
                    paramList.add(parameters);
                    PrismLogHandler.log("Processed parameters for database purge: " + purgeArgs);
                }
            }

            if (paramList.size() > 0) {


                int purgeTickDelay = plugin.config.purgeConfig.batchTickDelay;
                if (purgeTickDelay < 1) {
                    purgeTickDelay = 20;
                }

                /*
                  We're going to cycle through the param rules, one rule at a time in a single
                  async task. This task will reschedule itself when each purge cycle has
                  completed and records remain
                 */
                PrismLogHandler.log("Beginning prism database purge cycle. "
                                + "Will be performed in batches so we don't tie up the db...");
                scheduleNewPurgeTask(paramList,purgeTickDelay);
            }
        } else {
            PrismLogHandler.log("Purge rules are empty, not purging anything.");
        }
    }

    private void scheduleNewPurgeTask(CopyOnWriteArrayList<QueryParameters> paramList, int purgeTickDelay) {
        scheduleNewPurgeTask(paramList,purgeTickDelay,new LogPurgeCallback());
    }

    /**
     * Schedules a new purge task after checking and ensuring the current task is complete or cancelled.
     * @param paramList List of QueryParameters
     * @param purgeTickDelay int
     * @param callback PurgeCallBack
     */

    public void scheduleNewPurgeTask(CopyOnWriteArrayList<QueryParameters> paramList, int purgeTickDelay,
                                     PurgeCallback callback) {
        if (purgeTask == null || purgeTask.isDone() || purgeTask.isCancelled()) {
            purgeTask = plugin.getTaskManager().getSchedulePool().schedule(
                    new PurgeTask(plugin, paramList, purgeTickDelay, callback),
                    purgeTickDelay * 20L, TimeUnit.MILLISECONDS);
        } else {
            PrismLogHandler.warn("PurgeTask scheduled would overwrite pending task. Skipping");
        }
    }

    /**
     * Schedules a new purge task after checking and ensuring the current task is complete or cancelled.
     * @param paramList List of QueryParameters
     * @param purgeTickDelay int
     * @param maxId long
     * @param minId long
     * @param callback PurgeCallBack
     */
    public void scheduleNewPurgeTask(CopyOnWriteArrayList<QueryParameters> paramList, int purgeTickDelay, long minId,
                                     long maxId, PurgeCallback callback) {
        if (purgeTask == null || purgeTask.isDone() || purgeTask.isCancelled()) {
            purgeTask = scheduleNewPurgeTaskOutOfCycle(paramList, purgeTickDelay, minId, maxId, callback);
        }  else {
            PrismLogHandler.warn("PurgeTask scheduled would overwrite pending task. Skipping");
        }
    }

    /**
     * Schedules a new purge task returns the future result. Does not check the current cycle.
     * @param paramList List of QueryParameters
     * @param purgeTickDelay int
     * @param maxId long
     * @param minId long
     * @param callback PurgeCallBack
     * @return ScheduledFuture
     */
    public ScheduledFuture<?> scheduleNewPurgeTaskOutOfCycle(CopyOnWriteArrayList<QueryParameters> paramList,
                                                             int purgeTickDelay, long minId, long maxId,
                                                             PurgeCallback callback) {
        return plugin.getTaskManager().getSchedulePool().schedule(
                new PurgeTask(plugin, paramList, purgeTickDelay, minId, maxId, callback),
                purgeTickDelay * 20L, TimeUnit.MILLISECONDS);
    }
}