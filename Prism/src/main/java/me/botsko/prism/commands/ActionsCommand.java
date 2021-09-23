package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class ActionsCommand implements SubHandler {

    /**
     * Handle the command.
     */
    @Override
    public void handle(CallInfo call) {
        help(call.getSender());
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-action-list")};
    }

    @Override
    public String getRef() {
        return "/parameters.html#actions-list";
    }

    /**
     * Display param help.
     *
     * @param sender CommandSender
     */
    private void help(CommandSender sender) {

        Prism.messenger.sendMessage(sender,
                Prism.messenger.playerHeaderMsg(
                        Component.text("--- " + Il8nHelper.getRawMessage("action-list-header") + "---")
                                .color(NamedTextColor.GOLD)));
        // Build short list
        final StringBuilder actionList = new StringBuilder();
        ActionType.getShortNames()
                .stream()
                .filter(s -> s.contains("prism"))
                .distinct()
                .sorted()
                .forEach(s -> actionList.append(s).append(", "));
        actionList.delete(actionList.lastIndexOf(","),actionList.length());
        Prism.messenger.sendMessage(sender, Prism.messenger
                .playerMsg(Il8nHelper.getMessage("action-alias", ": ").color(NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(actionList.toString()))));
        // Build display of full actions
        final StringBuilder fullActionList = new StringBuilder();
        Arrays.stream(ActionType.values()).sorted()
                .filter(s -> s.name.contains("prism"))
                .distinct()
                .forEach(s -> fullActionList.append(s).append(", "));
        fullActionList.delete(fullActionList.lastIndexOf(","),fullActionList.length());
        Prism.messenger.sendMessage(sender, Prism.messenger
                .playerMsg(Il8nHelper.getMessage("full-action-alias", ": ")
                        .append(Component.text(fullActionList.toString()))));

    }
}