package me.botsko.prism.commandlibs;

import me.botsko.prism.Prism;
import org.bukkit.entity.Player;

public final class SubCommand {

    private final String[] commandAliases;
    private String[] permissionNodes;
    private int minArgs = 0;
    private boolean allowConsole = false;
    private SubHandler handler = null;

    /**
     * Create subcommand.
     *
     * @param commandAliases  String[]
     * @param permissionNodes String[]
     */
    public SubCommand(String[] commandAliases, String[] permissionNodes) {
        this.commandAliases = commandAliases;
        this.permissionNodes = permissionNodes;
    }

    /**
     * Create subcommand.
     * @param commandAliases String[]
     * @param permissionNodes String[]
     * @param handler SubHandler.
     */
    public SubCommand(String[] commandAliases, String[] permissionNodes, SubHandler handler) {
        this(commandAliases, permissionNodes);
        this.handler = handler;
    }

    /**
     * Set allow console true.
     * @return SubCOmmand
     */
    public SubCommand allowConsole() {
        this.allowConsole = true;
        return this;
    }

    /**
     * If console allowed.
     * @return boolean.
     */
    public boolean isConsoleAllowed() {
        return this.allowConsole;
    }

    /**
     * Min Args.
     * @return int
     */
    public int getMinArgs() {
        return minArgs;
    }

    /**
     * Set min args.
     * @param minArgs int
     * @return Subcommand.
     */
    public SubCommand setMinArgs(int minArgs) {
        this.minArgs = minArgs;
        return this;
    }

    /**
     * Get handler.
     * @return {@link SubHandler}
     */
    public SubHandler getHandler() {
        return handler;
    }

    /**
     * Set handler.
     * @param handler SubHandler
     * @return this
     */
    public SubCommand setHandler(SubHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * If has perm.
     * @return boolean
     */
    public boolean playerHasPermission(Player player) {
        for (String node : permissionNodes) {
            if (player.hasPermission(node)) {
                return true;
            }
            // Also check for global nodes
            if (node.contains("*")) {
                continue;
            }

            int index = node.lastIndexOf('.');
            while (index != -1) {
                node = node.substring(0, index);
                if (player.hasPermission(node + ".*")) {
                    return true;
                }
                index = node.lastIndexOf('.');
            }
        }
        return false;
    }

    /**
     * Set permission nodes to check.
     * @param permissionNodes String[]
     */
    public void setPermNodes(String[] permissionNodes) {
        this.permissionNodes = permissionNodes;
    }

    /**
     * Get aliases.
     * @return aliases String[]
     */
    public String[] getAliases() {
        return this.commandAliases;
    }

    public String[] getHelp() {
        return handler.getHelp();
    }

    /**
     * Returns a URL for help for the command.
     *
     * @return String
     */
    public String getWebLink() {
        if (handler != null) {
            return Prism.getBaseUrl() + "commands" + handler.getRef();
        } else {
            return Prism.getBaseUrl();
        }
    }
}