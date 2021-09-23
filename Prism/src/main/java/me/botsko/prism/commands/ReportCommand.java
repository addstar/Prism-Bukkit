package me.botsko.prism.commands;

import com.zaxxer.hikari.HikariDataSource;
import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.RecordingManager;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.MatchRule;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.PreprocessArgs;
import me.botsko.prism.database.ActionReportQuery;
import me.botsko.prism.database.BlockReportQuery;
import me.botsko.prism.measurement.QueueStats;
import me.botsko.prism.text.ReplaceableTextComponent;
import me.botsko.prism.utils.MiscUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

public class ReportCommand extends AbstractCommand {

    private final Prism plugin;
    private final List<String> secondaries;
    private final List<String> sumTertiaries;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public ReportCommand(Prism plugin) {
        this.plugin = plugin;
        secondaries = new ArrayList<>();
        secondaries.add("queue");
        secondaries.add("db");
        secondaries.add("sum");
        sumTertiaries = new ArrayList<>();
        sumTertiaries.add("blocks");
        sumTertiaries.add("actions");
    }

    /**
     * Handle the command.
     */
    @Override
    public void handle(CallInfo call) {

        if (call.getArgs().length < 2) {
            Prism.messenger.sendMessage(call.getSender(),
                  Prism.messenger.playerError(Il8nHelper.getMessage("report-error")));
            return;
        }
        switch (call.getArg(1)) {
            case "queue" -> queueReport(call.getSender());
            case "db" -> queueReport(call.getSender());
            case "sum" -> {
                if (call.getArgs().length < 3) {
                    Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerError(Il8nHelper.getMessage("report-sum-error")));
                    return;
                }
                switch (call.getArg(2)) {
                    case "blocks" -> blockSumReports(call);
                    case "actions" -> actionTypeCountReport(call);
                    default -> {
                        Prism.messenger.sendMessage(call.getSender(),
                                Prism.messenger.playerError(Il8nHelper.getMessage("report-player-error")));
                        return;
                    }
                }
            }
            default -> Prism.messenger.sendMessage(call.getSender(), Prism.messenger.playerError(Il8nHelper.formatMessage("invalid-arguments",call.getArg(1))));
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        if (call.getArgs().length == 2) {
            return MiscUtils.getStartingWith(call.getArg(1), secondaries);
        }
        if (call.getArg(1).equals("sum")) {
            if (call.getArgs().length == 3) {
                return MiscUtils.getStartingWith(call.getArg(2), sumTertiaries);
            }
            return PreprocessArgs.complete(call.getSender(), call.getArgs());
        }
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{
                Il8nHelper.getRawMessage("help-report-args"),
                Il8nHelper.getRawMessage("help-report-queue"),
                Il8nHelper.getRawMessage("help-report-db"),
                Il8nHelper.getRawMessage("help-report-player")
        };
    }

    @Override
    public String getRef() {
        return ".html";
    }

    private void queueReport(CommandSender sender) {

        Prism.messenger.sendMessage(sender,
                Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("report-queue-header")));

        Prism.messenger.sendMessage(sender,
              Prism.messenger.playerMsg(ReplaceableTextComponent.builder("report-actions-queue")
                    .replace("<size>", RecordingQueue.getQueueSize())
                    .build()));

        final ConcurrentSkipListMap<Long, QueueStats.TaskRunInfo> runs = plugin.queueStats.getRecentRunCounts();
        if (runs.size() > 0) {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("report-queue-recent")));
            for (final Entry<Long, QueueStats.TaskRunInfo> entry : runs.entrySet()) {
                final String time = new SimpleDateFormat("HH:mm:ss").format(entry.getKey());
                Prism.messenger.sendMessage(sender,
                        Prism.messenger.playerMsg(Component.text()
                                .content(time + " ").color(NamedTextColor.GRAY)
                                .append(Component.text(entry.getValue().getRecords()))
                                .build()));
            }
        }
    }

    private void databaseReport(CommandSender sender) {

        Prism.messenger.sendMessage(sender, Prism.messenger.playerHeaderMsg(
                Il8nHelper.getMessage("report-database-header")));

        Prism.messenger.sendMessage(sender, Prism.messenger
              .playerMsg(ReplaceableTextComponent.builder("report-database-failureCount")
                    .replace("<count>", RecordingManager.failedDbConnectionCount)
                    .build()));
        Prism.messenger.sendMessage(sender,
              Prism.messenger.playerMsg(ReplaceableTextComponent.builder("report-actions-queue")
                    .replace("<size>", RecordingQueue.getQueueSize())
                    .build()));
        if (Prism.getInstance().getPrismDataSource().getDataSource() instanceof HikariDataSource ds) {
            Prism.messenger.sendMessage(sender, Prism.messenger.playerMsg(ReplaceableTextComponent
                  .builder("report-hikari-props")
                  .replace("<total>", ds.getHikariPoolMXBean().getTotalConnections())
                  .replace("<activeConnections>", ds.getHikariPoolMXBean().getActiveConnections())
                  .replace("<idleConnections>", ds.getHikariPoolMXBean().getIdleConnections())
                  .replace("<minIdleConnections>", ds.getMinimumIdle())
                  .replace("<maxIdleConnections>", ds.getMaximumPoolSize())
                  .build()));
        }

        boolean recorderActive = checkRecorderActive(plugin);

        if (recorderActive) {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerSuccess(Il8nHelper.getMessage("report-recorder-running")));
        } else {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerError(Il8nHelper.getMessage("report-recorder-stopped")));
        }

        Prism.messenger.sendMessage(sender,
                Prism.messenger.playerSubduedHeaderMsg(Il8nHelper.getMessage("report-recorder-readiness")));
        StringBuilder builder = new StringBuilder();
        if (Prism.getInstance().getPrismDataSource().reportDataSource(builder)) {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerSuccess(builder.toString()));
        } else {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerError(builder.toString()));
        }
    }


    private void blockSumReports(final CallInfo call) {

        // Process and validate all of the arguments
        final QueryParameters parameters = PreprocessArgs.process(plugin.config, call.getSender(), call.getArgs(),
              PrismProcessType.LOOKUP, 3, !plugin.config.parameterConfig.neverUseDefaults);
        if (parameters == null) {
            Prism.messenger.sendMessage(call.getSender(),Il8nHelper.getMessage("report-blocks-error"));
            return;
        }
        // No actions
        if (checkParams(parameters, call)) {
            return;
        }

        final BlockReportQuery reportQuery = Prism.getInstance().getPrismDataSource().createBlockReportQuery();
        reportQuery.setParameters(parameters);
        /*
          Run the lookup itself in an async task so the lookup query isn't done on the
          main thread
         */
        Prism.messenger.sendMessage(call.getSender(),Il8nHelper.getMessage("report-generating"));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> reportQuery.report(call.getSender()));
    }

    private boolean checkParams(PrismParameters parameters, CallInfo call) {
        if (!parameters.getActionTypes().isEmpty()) {
            Prism.messenger.sendMessage(call.getSender(),Prism.messenger.playerError(
                                    Il8nHelper.getMessage("report-actions-invalid")));
            return true;
        }
        // Verify single player name for now
        final Map<String, MatchRule> players = parameters.getPlayerNames();
        if (players.size() != 1) {
            Prism.messenger.sendMessage(call.getSender(),
                    Prism.messenger.playerError(Il8nHelper.getMessage("single-player-only")));
            return true;
        }
        return false;
    }

    private void actionTypeCountReport(final CallInfo call) {

        // Process and validate all of the arguments
        final QueryParameters parameters = PreprocessArgs.process(plugin.config, call.getSender(), call.getArgs(),
              PrismProcessType.LOOKUP, 3,
              !plugin.config.parameterConfig.neverUseDefaults);
        if (parameters == null) {
            Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerError(Il8nHelper.getMessage("report-player-error")));
            return;
        }

        // No actions
        if (checkParams(parameters, call)) {
            return;
        }
        final ActionReportQuery reportQuery = Prism.getInstance().getPrismDataSource().createActionReportQuery();
        reportQuery.setParameters(parameters);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> reportQuery.report(call.getSender()));
    }
}