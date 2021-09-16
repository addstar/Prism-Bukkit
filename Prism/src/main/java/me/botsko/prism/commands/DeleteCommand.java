package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.PreprocessArgs;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.purge.SenderPurgeCallback;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

public class DeleteCommand extends AbstractCommand {

    private final Prism plugin;
    private final PrismConfig config;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public DeleteCommand(Prism plugin) {
        this.plugin = plugin;
        this.config = plugin.config;
    }

    @Override
    public void handle(final CallInfo call) {

        // Allow for canceling tasks
        if (call.getArgs().length > 1 && call.getArg(1).equals("cancel")) {
            ScheduledFuture<?> task = plugin.getTaskManager().getPurgeManager().purgeTask;
            if (task != null) {
                if (plugin.getTaskManager().getPurgeManager().purgeTask.cancel(false)) {
                    Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerMsg(Il8nHelper.getMessage("cancel-purge")));
                } else {
                    Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerMsg(Il8nHelper.getMessage("cancel-purge-error")));
                }
            } else {
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerError(Il8nHelper.getMessage("no-purge-running")));
            }
            return;
        }

        // Allow for wiping live queue
        if (call.getArgs().length > 1 && call.getArg(1).equals("queue")) {
            if (RecordingQueue.getQueue().size() > 0) {
                me.botsko.prism.PrismLogHandler.log("User " + call.getSender().getName()
                        + " wiped the live queue before it could be written to the database. "
                        + RecordingQueue.getQueue().size() + " events lost.");
                RecordingQueue.getQueue().clear();
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerSuccess(Il8nHelper.getMessage("clear-queue")));
            } else {
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerError(
                                Il8nHelper.getMessage("event-queue-clear")));
            }
            return;
        }

        // Process and validate all of the arguments
        final QueryParameters parameters = PreprocessArgs.process(config, call.getSender(), call.getArgs(),
                PrismProcessType.DELETE, 1, !config.parameterConfig.neverUseDefaults);
        if (parameters == null) {
            return;
        }
        parameters.setStringFromRawArgs(call.getArgs(), 1);

        StringBuilder defaultsReminder = checkIfDefaultUsed(parameters);
        if (parameters.getFoundArgs().size() > 0) {

            Prism.messenger.sendMessage(call.getSender(),
                    Prism.messenger.playerSubduedHeaderMsg(Il8nHelper.getMessage("purge-data")
                            .replaceText(builder -> builder.match("<defaults>")
                                    .replacement(Component.text(defaultsReminder.toString())).once())
                    )
            );
            Prism.messenger.sendMessage(call.getSender(), Prism.messenger
                    .playerHeaderMsg(Il8nHelper.getMessage("start-purge")));
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                int purgeTickDelay = config.purgeConfig.batchTickDelay;
                if (purgeTickDelay < 1) {
                    purgeTickDelay = 20;
                }

                // build callback
                final SenderPurgeCallback callback = new SenderPurgeCallback();
                callback.setSender(call.getSender());

                // add to an arraylist so we're consistent
                final CopyOnWriteArrayList<QueryParameters> paramList = new CopyOnWriteArrayList<>();
                paramList.add(parameters);

                final ActionsQuery aq = new ActionsQuery(plugin);
                final long[] extents = aq.getQueryExtents(parameters);
                final long minId = extents[0];
                final long maxId = extents[1];
                PrismLogHandler.log("Beginning prism database purge cycle. Will be performed in batches so "
                                + "we don't tie up the db...");
                plugin.getTaskManager().getPurgeManager()
                        .scheduleNewPurgeTaskOutOfCycle(paramList,purgeTickDelay,minId,maxId,callback);
            });
        } else {
            Prism.messenger.sendMessage(call.getSender(),
                    Prism.messenger.playerError(Il8nHelper.getMessage("no-parameter")));
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return PreprocessArgs.complete(call.getSender(), call.getArgs());
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-delete")};
    }

    @Override
    public String getRef() {
        return null;
    }
}