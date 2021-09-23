package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.commandlibs.CallInfo;

import java.util.List;

public class RecorderCommand extends AbstractCommand {

    private final Prism plugin;

    /**
     * Constructor.
     * @param plugin Prism
     */
    public RecorderCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(final CallInfo call) {

        if (call.getArgs().length <= 1) {
            Prism.messenger.sendMessage(call.getSender(),
                    Prism.messenger.playerError(Il8nHelper.getMessage("invalid-command")));
            return;
        }

        boolean recorderActive = checkRecorderActive(plugin);

        // Allow for canceling recorders
        if (call.getArg(1).equals("cancel")) {
            if (recorderActive) {
                plugin.getTaskManager().getRecordingTask().cancel();
                plugin.getTaskManager().setRecordingTask(null);
                Prism.messenger.sendMessage(call.getSender(), Prism.messenger
                        .playerMsg(Il8nHelper.getMessage("recorder-stopped")));
                Prism.messenger.sendMessage(call.getSender(), Prism.messenger
                        .playerError(Il8nHelper.getMessage("recorder-stopped-warn")));
            } else {
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerError(Il8nHelper.getMessage("report-recorder-stopped")));
            }
            return;
        }

        // Allow for force-restarting recorders
        if (call.getArg(1).equals("start")) {
            if (recorderActive) {
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerError(Il8nHelper.getMessage("report-already-running")));
            } else {
                Prism.messenger.sendMessage(call.getSender(),
                      Prism.messenger.playerMsg(Il8nHelper.getMessage("database-validating")));
                StringBuilder result = new StringBuilder();
                if (Prism.getInstance().getPrismDataSource().reportDataSource(result)) {
                    Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerSuccess(Il8nHelper.getMessage("pool-valid-connection")));
                    Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerMsg(Il8nHelper.getMessage("recorder-restarting")));
                    plugin.getTaskManager().actionRecorderTask();
                } else {
                    Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerError(Il8nHelper.getMessage("no-valid-database")));
                    PrismLogHandler.warn(result.toString());
                }
            }
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-recorder-start"),
                Il8nHelper.getRawMessage("help-recorder-stop")};
    }

    @Override
    public String getRef() {
        return "/settings.html";
    }
}