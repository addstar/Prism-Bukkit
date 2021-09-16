package me.botsko.prism.commandlibs;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.utils.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Executor implements CommandExecutor, TabCompleter {

    public final Plugin plugin;

    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    /**
     * Setting the executor to command mode allows it to handle all commands the
     * plugin watches for. SubCommand mode allows it to watch for commands that are
     * secondary to the primary command it's assigned to.
     */
    public final String mode;

    private final String defaultSubCommand = "default";

    /**
     * Constructor.
     * @param plugin Prism
     * @param mode String
     * @param permBase permission base
     */
    public Executor(Plugin plugin, @Nullable String mode, String permBase) {
        this.mode = (mode == null ? "command" : mode);
        this.plugin = plugin;
    }

    public final Map<String,SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel,
                             @NotNull String[] args) {

        // Set player
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        // Find command
        String subcommandName;
        if (mode.equals("subcommand") && args.length > 0) {
            subcommandName = args[0].toLowerCase();
        } else {
            subcommandName = cmd.getName();
        }

        String currentMode = mode;

        SubCommand sub = subCommands.get(subcommandName);
        if (sub == null) {
            sub = subCommands.get(defaultSubCommand);
            if (sub == null) {
                Prism.messenger.sendMessage(sender, Il8nHelper.getMessage("invalid-command"));
                return true;
            } else {
                // The default is used, we must switch back to command mode
                currentMode = "command";
            }
        }

        // Ensure they have permission
        if (player != null && !(sub.playerHasPermission(player))) {
            Prism.messenger.sendMessage(sender, Il8nHelper.getMessage("no-permission"));
            return true;
        } else if ((currentMode.equals("subcommand") && (args.length - 1) < sub.getMinArgs())
                || (currentMode.equals("command") && (args.length) < sub.getMinArgs())) {
            Prism.messenger.sendMessage(sender, Il8nHelper.formatMessage("invalid-arguments",args));
            return true;
        }
        // Ensure command allows console
        if (!(sender instanceof Player)) {
            if (!sub.isConsoleAllowed()) {
                Prism.messenger.sendMessage(sender, Il8nHelper.getMessage("invalid-not-in-game"));
                return true;
            }
        }

        // Pass along call to handler
        final CallInfo call = new CallInfo(sender, player, args);
        sub.getHandler().handle(call);

        return true;

    }

    /**
     * Add a sub command.
     * @param commandAliases String[] alias list
     * @param permissionNodes String[] permissions
     * @param handler SubHandler
     * @return SubCommand
     */
    private SubCommand addSub(String[] commandAliases, String[] permissionNodes,
                              @SuppressWarnings("SameParameterValue") SubHandler handler) {
        final SubCommand cmd = new SubCommand(commandAliases, permissionNodes, handler);
        for (final String alias : commandAliases) {
            subCommands.put(alias, cmd);
        }
        return cmd;
    }

    /**
     * Add a sub command with a null handler.
     * @param commandAliases String[] alias list
     * @param permissionNodes String[] permissions
     * @return SubCommand
     */
    protected SubCommand addSub(String[] commandAliases, String[] permissionNodes) {
        return addSub(commandAliases, permissionNodes, null);
    }

    /**
     * Add a sub command with a single permission.
     * @param commandAliases String[] alias list
     * @param permissionNode String permission
     * @return SubCommand
     */
    protected SubCommand addSub(String[] commandAliases, String permissionNode) {
        return addSub(commandAliases, new String[]{permissionNode}, null);
    }

    /**
     * Add a sub command with a single alias and null handler.
     * @param commandAlias String alias list
     * @param permissionNodes String[] permissions
     * @return SubCommand
     */
    @SuppressWarnings("unused")
    protected SubCommand addSub(String commandAlias, String[] permissionNodes) {
        return addSub(new String[]{commandAlias}, permissionNodes, null);
    }

    /**
     * Add a sub command with a single alias and permission and null handler.
     * @param commandAlias String alias
     * @param permissionNode String permissions
     * @return SubCommand
     */
    protected SubCommand addSub(String commandAlias, String permissionNode) {
        return addSub(new String[]{commandAlias}, new String[]{permissionNode}, null);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s,
                                      @NotNull String[] args) {
        // Set player
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        // Find command
        String subcommandName;
        if (mode.equals("subcommand") && args.length > 0) {
            subcommandName = args[0].toLowerCase();
            // Complete subcommand
            if (args.length == 1) {
                return MiscUtils.getStartingWith(subcommandName, subCommands.keySet());
            }
        } else {
            subcommandName = cmd.getName();
        }

        String currentMode = mode;

        SubCommand sub = subCommands.get(subcommandName);
        if (sub == null) {
            sub = subCommands.get(defaultSubCommand);
            if (sub == null) {
                sender.sendMessage("Invalid command");
                return null;
            } else {
                // The default is used, we must switch back to command mode
                currentMode = "command";
            }
        }

        // Ensure they have permission
        if (player != null && !(sub.playerHasPermission(player))) {
            sender.sendMessage("You do not have permission to use this command");
            return null;
        } else if ((currentMode.equals("subcommand") && (args.length - 1) < sub.getMinArgs())
                || (currentMode.equals("command") && (args.length) < sub.getMinArgs())) {
            sender.sendMessage("You're missing arguments for this command");
            return null;
        }
        // Ensure command allows console
        if (!(sender instanceof Player)) {
            if (!sub.isConsoleAllowed()) {
                sender.sendMessage("You must be in-game to use this command");
                return null;
            }
        }

        // Pass along call to handler
        final CallInfo call = new CallInfo(sender, player, args);
        return sub.getHandler().handleComplete(call);
    }
}