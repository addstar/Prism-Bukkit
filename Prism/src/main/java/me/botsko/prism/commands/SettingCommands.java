package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.RecordingTask;
import me.botsko.prism.commandlibs.CallInfo;

import java.util.Collections;
import java.util.List;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 7/08/2020.
 */
public class SettingCommands extends AbstractCommand {
    @Override
    public void handle(CallInfo call) {
        if (call.getArgs().length > 1) {
            switch (call.getArg(0).toLowerCase()) {
                case "batchsize" -> {
                    int actions = Integer.parseInt(call.getArg(1));
                    RecordingTask.setActionsPerInsert(actions);
                    Prism.messenger.sendMessage(call.getSender(),
                            Il8nHelper.formatMessage("settings-batch-insert", actions));
                    return;
                }
                default -> Prism.messenger.sendMessage(call.getSender(),
                        Il8nHelper.getMessage("invalid-command"));
            }
        }
        //todo add feedback
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        if (call.getArgs().length == 1) {
            return Collections.singletonList("batchsize");
        }
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-settings")};
    }

    @Override
    public String getRef() {
        return "/settings.html";
    }
}
