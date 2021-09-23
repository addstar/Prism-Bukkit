package me.botsko.prism.parameters;

import me.botsko.prism.actionlibs.QueryParameters;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface PrismParameterHandler {

    String getName();

    String[] getHelp();

    boolean applicable(String parameter, CommandSender sender);

    void process(QueryParameters query, String parameter, CommandSender sender);

    void defaultTo(QueryParameters query, CommandSender sender);

    /**
     * Complete a param after the `:`.
     *
     * @param partialParameter The partial parameter
     * @param sender           The sender
     * @return List of strings with suggestions or null if not applicable
     */
    List<String> tabComplete(String partialParameter, CommandSender sender);

    boolean hasPermission(String parameter, Permissible permissible);

    @NotNull
    default List<String> getPlayerNameCompletions(String prefix, String partialName) {
        partialName = partialName.toLowerCase();
        final List<String> completions = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(partialName)) {
                completions.add(prefix + player.getName());
            }
        }
        return completions;
    }
}