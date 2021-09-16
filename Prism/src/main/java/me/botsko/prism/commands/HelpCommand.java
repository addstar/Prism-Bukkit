package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubCommand;
import me.botsko.prism.commandlibs.SubHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelpCommand implements SubHandler {

    private final boolean failed;

    public HelpCommand(boolean failed) {
        this.failed = failed;
    }

    /**
     * {@inheritDoc}
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
        return new String[]{"Displays Help"};
    }

    @Override
    public String getRef() {
        return "";
    }

    /**
     * Displays help.
     *
     * @param s CommandSender
     */
    protected void help(CommandSender s) {
        Audience sender = Prism.getAudiences().sender(s);
        if (failed) {
            sender.sendMessage(Identity.nil(),
                    Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("prism-disabled-header")
                            .color(NamedTextColor.GOLD))
                            .append(Component.newline())
                            .append(
                                    Prism.messenger.playerMsg(Il8nHelper.getMessage("prism-disabled-content"))
                                            .color(NamedTextColor.GOLD))
                            .append(Component.newline())
                            .append(
                                    Prism.messenger.playerSubduedHeaderMsg(Il8nHelper.getMessage("discord", ":")
                                            .color(NamedTextColor.WHITE)
                                            .append(Component.text(Il8nHelper.getRawMessage("discord-url")))))
                            .append(Component.newline())
                            .append(
                                    Prism.messenger.playerSubduedHeaderMsg(Il8nHelper.getMessage("wiki", ":")
                                            .color(NamedTextColor.WHITE)
                                            .append(Component.text(Il8nHelper.getRawMessage("wiki-url"))))));
            return;
        }
        TextComponent component = Prism.messenger.playerHeaderMsg(
                Component.text("--- Basic Usage ---").color(NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Prism.messenger.playerSubduedHeaderMsg(Il8nHelper.getMessage("help-extended-message")))
                .append(Component.newline());
        Stream<SubCommand> stream = Prism.getInstance().getCommands().getSubCommands().values().stream().distinct();
        for (SubCommand command:stream.collect(Collectors.toList())) {
            if (command.getHelp().length > 1) {
                int i = 0;
                for (String message : command.getHelp()) {
                    if (i == 0) {
                        component = component.append(Prism.messenger.playerHelp(
                                Arrays.toString(command.getAliases()), message)
                                .clickEvent(ClickEvent.openUrl(command.getWebLink())))
                                .append(Component.newline());
                    } else {
                        component = component.append(Prism.messenger.playerHelp("      |- ", message)
                                .clickEvent(ClickEvent.openUrl(command.getWebLink())))
                                .append(Component.newline());
                    }
                    i++;
                }
            } else {
                component = component.append(Prism.messenger.playerHelp(
                        Arrays.toString(command.getAliases()), command.getHelp()[0])
                        .clickEvent(ClickEvent.openUrl(command.getWebLink())))
                        .append(Component.newline());
            }
        }
        sender.sendMessage(Identity.nil(),component);
    }
}