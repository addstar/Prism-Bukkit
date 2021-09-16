package me.botsko.prism.actionlibs;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.database.InsertQuery;
import me.botsko.prism.measurement.QueueStats;

import java.sql.SQLException;

public class RecordingTask implements Runnable {

    private final Prism plugin;
    private static int actionsPerInsert;
    private final int maxFailures;
    private int recordingTickDelay;

    public static void setActionsPerInsert(int adjust) {
        actionsPerInsert = adjust;
    }


    /**
     * Create the task.
     *
     * @param plugin Plugin
     */
    public RecordingTask(Prism plugin) {
        this.plugin = plugin;
        actionsPerInsert = plugin.config.queueConfig.actionsPerBatch;
        maxFailures = plugin.config.queueConfig.maxFailures;
        recordingTickDelay = plugin.config.queueConfig.emptyTickDelay;


    }

    /**
     * Insert Action.
     *
     * @param a Handler
     * @return rows affected.
     */
    public static long insertActionIntoDatabase(Handler a) {
        return Prism.getInstance().getPrismDataSource().getDataInsertionQuery().insertActionIntoDatabase(a);
    }

    /**
     * If the queue isn't empty run an insert.
     */
    public void save() {
        if (!RecordingQueue.getQueue().isEmpty()) {
            insertActionsIntoDatabase();
        }
    }

    /**
     * Create a Insertion.
     */
    void insertActionsIntoDatabase() {
        int actionsRecorded = 0;
        int perBatch = actionsPerInsert;
        if (perBatch < 1) {
            perBatch = 1000;
        }
        if (!RecordingQueue.getQueue().isEmpty()) {
            if (Prism.getInstance().getPrismDataSource().isPaused()) {
                PrismLogHandler.log("Prism database paused. An external actor has paused database processing..."
                                + "scheduling next recording");
                scheduleNextRecording();
                return;
            }
            long start = System.currentTimeMillis();
            PrismLogHandler.debug("Beginning batch insert from queue. " + start);
            StringBuilder builder = new StringBuilder();
            if (Prism.getInstance().getPrismDataSource().reportDataSource(builder)) {
                RecordingManager.failedDbConnectionCount = 0;
            } else {
                if (RecordingManager.failedDbConnectionCount == 0) {
                    PrismLogHandler.log("Prism database error. Connection should be there but it's not. "
                            + "Leaving actions to log in queue.");
                    PrismLogHandler.log(builder.toString());
                }
                RecordingManager.failedDbConnectionCount++;
                if (RecordingManager.failedDbConnectionCount > maxFailures) {
                    PrismLogHandler.log("Too many problems connecting. Giving up for a bit.");
                    scheduleNextRecording();
                }
                PrismLogHandler.debug("Database connection still missing, incrementing count.");
                return;
            }
            InsertQuery batchedQuery;
            try {
                batchedQuery = Prism.getInstance().getPrismDataSource().getDataInsertionQuery();
                batchedQuery.createBatch();
            } catch (Exception e) {
                e.printStackTrace();
                if (e instanceof SQLException) {
                    Prism.getInstance().getPrismDataSource().handleDataSourceException((SQLException) e);
                }
                PrismLogHandler.debug("Database connection issue;");
                RecordingManager.failedDbConnectionCount++;
                return;
            }
            int i = 0;
            while (!RecordingQueue.getQueue().isEmpty()) {
                final Handler a = RecordingQueue.getQueue().poll();

                // poll() returns null if queue is empty
                if (a == null) {
                    break;
                }
                if (a.isCanceled()) {
                    continue;
                }
                batchedQuery.insertActionIntoDatabase(a);

                actionsRecorded++;

                // Break out of the loop and just commit what we have
                if (i >= perBatch) {
                    PrismLogHandler.debug("Recorder: Batch max exceeded, running insert. Queue remaining: "
                            + RecordingQueue.getQueue().size());
                    break;
                }
                i++;
            }
            long batchDoneTime = System.currentTimeMillis();
            long batchingTime = batchDoneTime - start;
            // The main delay is here
            try {
                batchedQuery.processBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Save the current count to the queue for short historical data
            long batchProcessedEnd = System.currentTimeMillis();
            long batchRunTime = batchProcessedEnd - batchDoneTime;
            plugin.queueStats.addRunInfo(new QueueStats.TaskRunInfo(actionsRecorded,batchingTime,batchRunTime));
        }
    }

    /**
     * Rebuild the datasource and schedule the next run.
     */
    @Override
    public void run() {
        if (RecordingManager.failedDbConnectionCount > 5) {
            Prism.getInstance().getPrismDataSource().rebuildDataSource(); // force rebuild pool after several failures
        }
        save();
        scheduleNextRecording();
    }

    /**
     * Get the delay based on connection failure.
     *
     * @return delay
     */
    private int getTickDelayForNextBatch() {

        // If we have too many rejected connections, increase the schedule
        if (RecordingManager.failedDbConnectionCount > maxFailures) {
            return RecordingManager.failedDbConnectionCount * 20;
        }

        if (recordingTickDelay < 1) {
            recordingTickDelay = 3;
        }
        return recordingTickDelay;
    }

    /**
     * Schedule a async recording with delay.
     */
    private void scheduleNextRecording() {
        if (!plugin.isEnabled()) {
            PrismLogHandler.log("Can't schedule new recording tasks as plugin is now disabled. If you're shutting"
                            + " down the server, ignore me.");
            return;
        }
        plugin.getTaskManager().setRecordingTask(plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin,
                this, getTickDelayForNextBatch()));
    }
}
