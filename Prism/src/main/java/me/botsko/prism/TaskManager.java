package me.botsko.prism;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.botsko.prism.actionlibs.InternalAffairs;
import me.botsko.prism.actionlibs.RecordingTask;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.purge.PurgeManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Created for Prism.  This class handles futures checking them for completion asynchronously and then running the
 * supplied task if completed.
 *
 * @author Narimm on 15/02/2021
 * @since 2.1.8
 */
public class TaskManager {
    public boolean active;
    private final PrismConfig config;
    private final BukkitScheduler scheduler;
    private BukkitTask functionalTaskHandler;
    private BukkitTask recordingTask;
    private ScheduledFuture<?> purgeManagerFuture;
    private ScheduledFuture<?> internalAffairsFuture;
    private final ScheduledThreadPoolExecutor schedulePool;
    private final Prism plugin;
    private final BlockingQueue<FunctionalBukkitTask<?>> tasks = new LinkedBlockingQueue<>();
    private PurgeManager purgeManager;

    /**
     * Get the purge manager.
     * @return PurgeManager
     */
    public PurgeManager getPurgeManager() {
        return purgeManager;
    }

    /**
     * This class handles futures checking them for completion asynchronously and then running the supplied task
     * if completed.
     *
     * @param schedule BukkitScheduler for synchronous execution
     * @param plugin   the plugin.
     */
    public TaskManager(BukkitScheduler schedule, Prism plugin) {
        this.scheduler = schedule;
        this.plugin = plugin;
        this.config = plugin.config;
        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("Prism-TaskManager-Thread-%d").build();
        this.schedulePool = new ScheduledThreadPoolExecutor(1,factory);
        active = true;
    }


    public ScheduledThreadPoolExecutor getSchedulePool() {
        return schedulePool;
    }

    /**
     * Schedule the Purge manager.
     */
    protected void launchScheduledPurgeManager() {
        final List<String> purgeRules = config.purgeConfig.rules;
        purgeManager = new PurgeManager(plugin, purgeRules);
        purgeManagerFuture = schedulePool.scheduleAtFixedRate(purgeManager, 0, 12, TimeUnit.HOURS);
    }

    /**
     * Launch InternalAffairs - to monitor recording.
     */
    protected void launchInternalAffairs() {
        internalAffairsFuture = schedulePool.scheduleAtFixedRate(new InternalAffairs(plugin), 0, 5, TimeUnit.MINUTES);
    }

    /**
     * Adds a task in a non blocking fashion.
     *
     * @param future The future to check
     * @param task   the Task to run once the future is complete
     * @param async  run the task async or on main thread
     * @param <T>    The object returned by the future and accepted by the task.
     */
    public <T> void addTask(CompletableFuture<T> future, Consumer<T> task, boolean async) throws InterruptedException {
        if (active) {
            tasks.put(new FunctionalBukkitTask<>(future, task, async));
        } else {
            throw new IllegalStateException("TaskManager inactive.");
        }

    }

    /**
     * Shutdown the task Manager.
     * @return True of all tasks are finished.
     */
    public boolean shutdown() {
        active = false;
        if(purgeManagerFuture != null) {
            purgeManagerFuture.cancel(false);
        }
        if(internalAffairsFuture != null) {
            internalAffairsFuture.cancel(false);
        }
        if(functionalTaskHandler != null) {
            functionalTaskHandler.cancel();
        }
        try {
            if (schedulePool.awaitTermination(350,TimeUnit.MILLISECONDS)) {
                tasks.clear();
                return true;
            } else {
                PrismLogHandler.warn("Task manager could not stop all executing tasks...and timed out while waiting");
                return fastShutdown();
            }
        } catch (InterruptedException e) {
            PrismLogHandler.warn(Level.WARNING,"Task manager threw an error while terminating",e);
            return fastShutdown();
        }
    }

    private boolean fastShutdown() {
        if (purgeManagerFuture != null && purgeManagerFuture.cancel(true)) {
            PrismLogHandler.warn("Task manager: forced PurgeManager to stop");
        }
        int failed = schedulePool.shutdownNow().size();
        if(functionalTaskHandler != null) {
            functionalTaskHandler.cancel();
        }
        tasks.clear();
        if (failed > 0) {
            PrismLogHandler.warn("Task manager: " + failed + " tasks were force stopped.");
            return false;
        }
        PrismLogHandler.warn("Task manager: fast shutdown complete");
        return  true;
    }

    public void setRecordingTask(BukkitTask recordingTask) {
        this.recordingTask = recordingTask;
    }

    public BukkitTask getRecordingTask() {
        return recordingTask;
    }


    /**
     * Schedule the RecorderTask async.
     */
    public void actionRecorderTask() {
        int recorderTickDelay = plugin.config.queueConfig.emptyTickDelay;
        if (recorderTickDelay < 1) {
            recorderTickDelay = 3;
        }
        // we schedule it once, it will reschedule itself
        setRecordingTask(scheduler.runTaskLaterAsynchronously(plugin, new RecordingTask(plugin), recorderTickDelay));
    }

    /**
     * Start the task manager.
     */
    public void run() {
        functionalTaskHandler = scheduler.runTaskAsynchronously(plugin, () -> {
            while (active) {
                try {
                    FunctionalBukkitTask<?> next = tasks.poll(100, TimeUnit.MILLISECONDS);
                    if (next != null) {
                        if (next.isDone()) {
                            next.complete();
                        } else {
                            tasks.put(next);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    active = false;
                }
            }
            PrismLogHandler.log("Task manager is stopping...");
        });
    }

    public class FunctionalBukkitTask<T> {
        private final CompletableFuture<T> future;
        private final Consumer<T> onCompletion;
        private final boolean async;
        private T result;

        /**
         * A task that will call the consumer on completion running it sync if needed.
         */
        public FunctionalBukkitTask(CompletableFuture<T> future, Consumer<T> function, boolean async) {
            this.future = future;
            this.onCompletion = function;
            this.async = async;
        }

        public boolean isDone() {
            return future.isDone();
        }

        /**
         * Completes the task once the future is done.
         */
        public void complete() {
            try {
                result = future.get();
                if (async) {
                    schedulePool.execute(() -> onCompletion.accept(result));
                } else {
                    scheduler.runTask(plugin, () -> onCompletion.accept(result));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}


