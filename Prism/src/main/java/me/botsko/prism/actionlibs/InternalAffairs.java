package me.botsko.prism.actionlibs;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import org.bukkit.scheduler.BukkitScheduler;

public class InternalAffairs implements Runnable {

    private final Prism plugin;

    public InternalAffairs(Prism plugin) {
        PrismLogHandler.debug("[InternalAffairs] Keeping watch over the watchers.");
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (reportStatusGood()) {
            return;
        }
        PrismLogHandler.log("[InternalAffairs] Recorder is NOT active... checking database");

        StringBuilder result = new StringBuilder();
        if (Prism.getInstance().getPrismDataSource().reportDataSource(result)) {
            PrismLogHandler.log("[Internal Affairs]" + result);
            plugin.getTaskManager().actionRecorderTask();
            reportStatusGood();
        } else {
            PrismLogHandler.warn(result.toString());
        }
    }

    private boolean reportStatusGood() {
        final BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (plugin.getTaskManager().getRecordingTask() != null) {
            final int taskId = plugin.getTaskManager().getRecordingTask().getTaskId();
            if (scheduler.isCurrentlyRunning(taskId) || scheduler.isQueued(taskId)) {
                PrismLogHandler.debug("[InternalAffairs] Recorder is currently active. All is good.");
                return true;
            }
        }
        return false;
    }
}