package me.botsko.prism.commandlibs;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.MatchRule;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.parameters.PrismParameterHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PreprocessArgs {

    public static QueryParameters process(PrismConfig config, CommandSender sender, String[] args,
                                          PrismProcessType processType, int startAt, boolean useDefaults) {
        return process(config, sender, args, processType, startAt, useDefaults, false);
    }

    /**
     * Create a set of parameters.
     * @param config PrismConfig.
     * @param sender CommandSender
     * @param args arg list
     * @param processType {@link PrismProcessType}
     * @param startAt int
     * @param useDefaults  bool
     * @param optional bool
     * @return {@link QueryParameters}
     */
    public static QueryParameters process(PrismConfig config, CommandSender sender, String[] args,
                                          PrismProcessType processType, int startAt, boolean useDefaults,
                                          boolean optional) {

        // Check for player or sender
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        // Start query
        final QueryParameters parameters = new QueryParameters();
        parameters.setProcessType(processType);

        // Define pagination/process type
        if (parameters.getProcessType().equals(PrismProcessType.LOOKUP)) {
            parameters.setLimit(config.parameterConfig.lookupMaxResults);
            parameters.setPerPage(config.parameterConfig.defaultResultsPerPage);
        }

        // Load registered parameters
        final HashMap<String, PrismParameterHandler> registeredParams = Prism.getParameters();

        // Store names of matched params/handlers
        final Set<String> foundArgsNames = new HashSet<>();
        final Collection<MatchedParam> foundArgsList = new ArrayList<>();

        // Iterate all command arguments
        if (args == null) {
            return parameters;
        }

        for (int i = startAt; i < args.length; i++) {

            final String arg = args[i];
            if (arg.isEmpty()) {
                continue;
            }
            if (ParseResult.NotFound == parseParam(sender, parameters, registeredParams,
                    foundArgsNames, foundArgsList, arg)) {
                return null;
            }
        }
        parameters.setFoundArgs(foundArgsNames);

        // Reject no matches
        if (foundArgsList.isEmpty() && !optional) {
            if (sender != null) {
                Prism.messenger.sendMessage(sender, Prism.messenger
                        .playerError("You're missing valid parameters. Use /prism ? for assistance."));
            } else {
                me.botsko.prism.PrismLogHandler.log("Missing valid parameters");
            }
            return null;
        }

        /*
          Call default method for handlers *not* used
         */
        if (useDefaults) {
            for (final Entry<String, PrismParameterHandler> entry : registeredParams.entrySet()) {
                if (!foundArgsNames.contains(entry.getKey().toLowerCase())) {
                    entry.getValue().defaultTo(parameters, sender);
                }
            }
        }

        /*
          Send arguments to parameter handlers
         */
        for (final MatchedParam matchedParam : foundArgsList) {
            try {
                final PrismParameterHandler handler = matchedParam.getHandler();
                handler.process(parameters, matchedParam.getArg(), sender);
            } catch (final IllegalArgumentException e) {
                if (sender != null) {
                    Prism.messenger.sendMessage(sender, Prism.messenger.playerError(e.getMessage()));
                } else {
                    me.botsko.prism.PrismLogHandler.log(e.getMessage());
                }
                return null;
            }
        }

        // Enforce specifying an action
        if (sender != null && !sender.hasPermission("prism.parameters.action-filter-bypass")
                && parameters.getActionTypes().isEmpty()) {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerError("You're missing valid actions. Use /prism ? for assistance."));
            return null;
        }

        // Player location
        if (player != null && !config.parameterConfig.neverUseDefaults
                && parameters.getPlayerLocation() == null
                && (parameters.getMaxLocation() == null || parameters.getMinLocation() == null)) {
            parameters.setMinMaxVectorsFromPlayerLocation(player.getLocation());
        }
        return parameters;
    }

    /**
     * Parse a set of params.
     * @param sender CommandSender
     * @param parameters QueryParameters
     * @param registeredParams Map
     * @param foundArgsNames Collection
     * @param foundArgsList Collection
     * @param arg String
     * @return ParseResult.
     */
    private static ParseResult parseParam(CommandSender sender, PrismParameters parameters,
                                          Map<String, PrismParameterHandler> registeredParams,
                                          Collection<String> foundArgsNames, Collection<MatchedParam> foundArgsList,
                                          String arg) {
        ParseResult result = ParseResult.NotFound;

        // Match command argument to parameter handler
        for (final Entry<String, PrismParameterHandler> entry : registeredParams.entrySet()) {
            PrismParameterHandler parameterHandler = entry.getValue();
            if (!parameterHandler.applicable(arg, sender)) {
                continue;
            }
            if (!parameterHandler.hasPermission(arg, sender)) {
                result = ParseResult.NoPermission;
                continue;
            }
            result = ParseResult.Found;
            foundArgsList.add(new MatchedParam(parameterHandler, arg));
            foundArgsNames.add(parameterHandler.getName().toLowerCase());
            break;
        }

        // Reject argument that doesn't match anything
        if (result == ParseResult.NotFound) {
            // We support an alternate player syntax so that people
            // can use the tab-complete
            // feature of minecraft. Using p: prevents it.

            final Player autoFillPlayer = Bukkit.getServer().getPlayer(arg);
            if (autoFillPlayer != null) {
                MatchRule match = MatchRule.INCLUDE;
                if (arg.startsWith("!")) {
                    match = MatchRule.EXCLUDE;
                }
                result = ParseResult.Found;
                parameters.addPlayerName(arg.replace("!", ""), match);
            }
        }

        switch (result) {
            case NotFound:
                if (sender != null) {
                    Prism.messenger.sendMessage(sender,
                            Prism.messenger.playerError("Unrecognized parameter '"
                                    + arg + "'. Use /prism ? for help."));
                } else {
                    me.botsko.prism.PrismLogHandler.log("Unrecognized parameter '" + arg + "'");
                }
                break;
            case NoPermission:
                if (sender != null) {
                    Prism.messenger.sendMessage(sender,
                            Prism.messenger.playerError("No permission for parameter '"
                                    + arg + "', skipped."));
                } else {
                    me.botsko.prism.PrismLogHandler.log("No permission for parameter '" + arg + "'");
                }
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * TabComplete the an argument.
     * @param sender  CommandSender
     * @param args String[]
     * @param arg int
     * @return List
     */
    public static List<String> complete(CommandSender sender, String[] args, int arg) {
        // Iterate all command arguments
        if (args == null || args.length <= arg) {
            return null;
        }

        return complete(sender, args[arg]);
    }

    /**
     * TabComplete the last argument.
     * @param sender  CommandSender
     * @param args String[]
     * @return List
     */
    public static List<String> complete(CommandSender sender, String[] args) {
        return complete(sender, args, args.length - 1);
    }

    /**
     * TabComplete the an argument.
     * @param sender  CommandSender
     * @param arg String
     * @return List
     */
    public static List<String> complete(CommandSender sender, String arg) {
        if (arg.isEmpty()) {
            return null;
        }

        // Load registered parameters
        final HashMap<String, PrismParameterHandler> registeredParams = Prism.getParameters();

        // Match command argument to parameter handler
        for (final Entry<String, PrismParameterHandler> entry : registeredParams.entrySet()) {
            if (entry.getValue().applicable(arg, sender) && entry.getValue().hasPermission(arg, sender)) {
                return entry.getValue().tabComplete(arg, sender);
            }
        }

        return null;
    }

    /**
     * Enum to show results of a parse.
     * @author botskonet
     */
    private enum ParseResult {
        NotFound,
        NoPermission,
        Found
    }

    /**
     * Matched Params.
     */
    private static class MatchedParam {
        private final PrismParameterHandler handler;
        private final String arg;

        MatchedParam(PrismParameterHandler handler, String arg) {
            this.handler = handler;
            this.arg = arg;
        }

        public PrismParameterHandler getHandler() {
            return handler;
        }

        public String getArg() {
            return arg;
        }
    }
}