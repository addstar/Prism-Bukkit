package me.botsko.prism.parameters;

import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.commands.Flag;
import me.botsko.prism.commands.Flags;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FlagParameter implements PrismParameterHandler {


    @Override
    public String getName() {
        return "Flag";
    }


    @Override
    public String[] getHelp() {
        return new String[0];
    }


    @Override
    public boolean applicable(String parameter, CommandSender sender) {
        if (Pattern.compile("(-)([^\\s]+)?").matcher(parameter).matches()) {
            final String[] flagComponents = parameter.substring(1).split("=");
            try {
                Flag flag = Flags.valueOf(flagComponents[0].replace("-", "_").toUpperCase());
                return true;
            } catch (final IllegalArgumentException ex) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void process(QueryParameters query, String parameter, CommandSender sender) {
        final String[] flagComponents = parameter.substring(1).split("=");
        Flag flag;
        try {
            flag = Flags.valueOf(flagComponents[0].replace("-", "_").toUpperCase());
        } catch (final IllegalArgumentException ex) {
            throw new IllegalArgumentException("Flag -" + flagComponents[0] + " not found", ex);
        }
        if (!(query.hasFlag(flag))) {

            query.addFlag(flag);

            // Flag has a value
            if (flagComponents.length > 1) {
                if (flag.equals(Flags.PER_PAGE)) {
                    if (TypeUtils.isNumeric(flagComponents[1])) {
                        query.setPerPage(Integer.parseInt(flagComponents[1]));
                    } else {
                        throw new IllegalArgumentException(
                                "Per-page flag value must be a number. Use /prism ? for help.");
                    }
                } else if (flag.equals(Flags.SHARE)) {
                    for (final String sharePlayer : flagComponents[1].split(",")) {
                        if (sharePlayer.equals(sender.getName())) {
                            throw new IllegalArgumentException("You can't share lookup results with yourself!");
                        }
                        final Player shareWith = Bukkit.getServer().getPlayer(sharePlayer);
                        if (shareWith != null) {
                            query.addSharedPlayer(shareWith);
                        } else {
                            throw new IllegalArgumentException(
                                    "Can't share with " + sharePlayer + ". Are they online?");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void defaultTo(QueryParameters query, CommandSender sender) {

    }

    @Override
    public List<String> tabComplete(String partialParameter, CommandSender sender) {
        final String[] flagComponents = partialParameter.substring(1).split("=", 2);
        Flag flag;
        final String name = flagComponents[0].replace("-", "_").toUpperCase();
        try {
            flag = Flags.valueOf(name);
        } catch (final IllegalArgumentException ex) {
            final List<String> completions = new ArrayList<>();
            for (final Flag possibleFlag : Flags.values()) {
                final String flagName = possibleFlag.toString();
                if (flagName.startsWith(name)) {
                    completions.add("-" + flagName.replace('_', '-').toLowerCase());
                }
            }
            return completions;
        }

        // Flag has a value
        if (flagComponents.length <= 1) {
            return null;
        }

        String prefix = "-" + flag.toString().replace('_', '-').toLowerCase() + "=";
        if (flag.equals(Flags.SHARE)) {
            final String value = flagComponents[1];
            final int end = value.lastIndexOf(',');
            String partialName = value;
            if (end != -1) {
                partialName = value.substring(end + 1);
                prefix = prefix + value.substring(0, end) + ",";
            }
            return getPlayerNameCompletions(prefix, partialName);
        }
        return null;
    }



    @Override
    public boolean hasPermission(String parameter, Permissible permissible) {
        final String[] flagComponents = parameter.substring(1).split("=");
        Flag flag;
        try {
            flag = Flags.valueOf(flagComponents[0].replace("-", "_").toUpperCase());
        } catch (final IllegalArgumentException ex) {
            return false;
        }
        return flag.hasPermission(permissible);
    }
}