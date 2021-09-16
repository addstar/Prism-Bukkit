package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.commandlibs.SubHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 29/04/2020.
 */
public abstract class AbstractCommand implements SubHandler {

    final StringBuilder checkIfDefaultUsed(PrismParameters parameters) {
        final List<String> defaultsUsed = parameters.getDefaultsUsed();
        StringBuilder defaultsReminder = new StringBuilder();
        if (!defaultsUsed.isEmpty()) {
            defaultsReminder.append(" using defaults:");
            for (final String d : defaultsUsed) {
                defaultsReminder.append(" ").append(d);
            }
        }
        return defaultsReminder;
    }

    @SuppressWarnings("WeakerAccess")
    protected static boolean checkRecorderActive(Prism plugin) {
        boolean recorderActive = false;
        if (plugin.getTaskManager().getRecordingTask() != null) {
            final int taskId = plugin.getTaskManager().getRecordingTask().getTaskId();
            final BukkitScheduler scheduler = Bukkit.getScheduler();
            if (scheduler.isCurrentlyRunning(taskId) || scheduler.isQueued(taskId)) {
                recorderActive = true;
            }
        }
        return recorderActive;
    }

    protected static boolean checkNoPermissions(CommandSender sender, String... permissions) {
        for (String perm : permissions) {
            if (!sender.hasPermission(perm)) {
                Prism.messenger.sendMessage(sender,
                        Prism.messenger.playerError(Il8nHelper.getMessage("no-permission")));
                return true;
            }
        }
        return false;
    }
}
