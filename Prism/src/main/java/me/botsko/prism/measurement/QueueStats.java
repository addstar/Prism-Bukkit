package me.botsko.prism.measurement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

public class QueueStats {

    private static final ConcurrentSkipListMap<Long, TaskRunInfo> perRunRecordingCounts = new ConcurrentSkipListMap<>();

    /**
     * Add a TaskRunInfo to the run count.
     * @param count TaskRunInfo
     */
    public void addRunInfo(TaskRunInfo count) {

        final long currentTime = System.currentTimeMillis();
        // Delete any that are older than a few minutes, so we don't spam the
        // screen
        // unless we only have a few already
        if (perRunRecordingCounts.size() > 5) {
            long t5m = currentTime - (5 * 60 * 1000);
            Set<Map.Entry<Long, TaskRunInfo>> set = perRunRecordingCounts.descendingMap().entrySet();
            for (final Entry<Long, TaskRunInfo> entry : set) {
                if (entry.getKey() < t5m) {
                    perRunRecordingCounts.remove(entry.getKey());
                }

            }
        }
        perRunRecordingCounts.put(currentTime, count);
    }

    /**
     * Gets the average batch process time over the last minute.
     * @return average Batch processing time in millisecs;
     */
    public static double getPerMinuteBatchProcessAverage() {
        long lastMinute = System.currentTimeMillis() - (1000 * 60);
        List<Long> vals = new ArrayList<>();
        perRunRecordingCounts.tailMap(lastMinute)
                .forEach((time, taskRunInfo) -> vals.add(taskRunInfo.getBatchProcessTime()));
        return getAverageValue(vals);
    }

    /**
     * Gets the average batch build time over the last minute.
     * @return average Batch build time in millisecs;
     */
    public static double getPerMinuteBatchBuildAverage() {
        long lastMinute = System.currentTimeMillis() - (1000 * 60);
        List<Long> vals = new ArrayList<>();
        perRunRecordingCounts.tailMap(lastMinute)
                .forEach((time, taskRunInfo) -> vals.add(taskRunInfo.getBatchingTime()));
        return getAverageValue(vals);

    }

    /**
     * Gets the average batch insert count over the last minute.
     * @return average batch insert count;
     */
    public static double getPerMinuteInsertAverage() {
        long lastMinute = System.currentTimeMillis() - (1000 * 60);
        List<Long> values = new ArrayList<>();
        perRunRecordingCounts.tailMap(lastMinute)
                .forEach((time, taskRunInfo) -> values.add(taskRunInfo.getRecords()));
        return getAverageValue(values);
    }

    private static double getAverageValue(Collection<Long> vals) {
        return vals.stream().mapToDouble(a -> a).average().orElse(0);
    }

    /**
     * Returns a list of recent recording actions.
     * @return ConcurrentSkipListMap
     */
    public ConcurrentSkipListMap<Long, TaskRunInfo> getRecentRunCounts() {
        return perRunRecordingCounts;
    }

    /**
     * Holds the Task run data for metric records.
     */
    public static class TaskRunInfo {

        final long records;
        final long batchingTime;
        final long batchProcessTime;

        /**
         * Construct a Task info.
         * @param records number of records
         * @param batchingTime time to build the batch
         * @param batchProcessTime time to insert the batch into the database.
         */
        public TaskRunInfo(long records, long batchingTime, long batchProcessTime) {
            this.records = records;
            this.batchingTime = batchingTime;
            this.batchProcessTime = batchProcessTime;
        }

        public long getRecords() {
            return records;
        }

        public long getBatchingTime() {
            return batchingTime;
        }

        public long getBatchProcessTime() {
            return batchProcessTime;
        }
    }
}