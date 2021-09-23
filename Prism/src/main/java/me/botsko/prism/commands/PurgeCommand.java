package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import me.botsko.prism.text.ReplaceableTextComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by Narimm on 5/06/2017.
 */
public class PurgeCommand implements SubHandler {

    private final Prism plugin;

    public PurgeCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(CallInfo call) {
        if (call.getArgs().length <= 1) {
            Prism.messenger.sendMessage(call.getSender(),
                    Prism.messenger.playerHeaderMsg(Component.text("Prism")
                            .append(Component.text(" v" + plugin.getPrismVersion()).color(NamedTextColor.GRAY))));
            ScheduledThreadPoolExecutor pool = plugin.getTaskManager().getSchedulePool();
            Prism.messenger.sendMessage(call.getSender(),
                  Prism.messenger.playerSubduedHeaderMsg(ReplaceableTextComponent.builder("purge-report")
                    .replace("<taskCount>", pool.getTaskCount())
                    .replace("<purgesComplete>", pool.getCompletedTaskCount())
                    .replace("<poolString>", pool.toString())
                    .build()));
        } else if (call.getArgs().length > 1) {
            if (Objects.equals(call.getArgs()[1], "execute")) {
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("purge-execute")));
                Bukkit.getScheduler().runTaskAsynchronously(plugin, plugin.getTaskManager().getPurgeManager());
            }
        } else {
            Prism.messenger.sendMessage(call.getSender(),Il8nHelper.getMessage("invalid-command"));
        }

    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-purge")};
    }

    @Override
    public String getRef() {
        return "/purge.html";
    }
}
