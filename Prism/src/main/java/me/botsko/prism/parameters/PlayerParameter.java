package me.botsko.prism.parameters;

import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.actions.MatchRule;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.regex.Pattern;

public class PlayerParameter extends SimplePrismParameterHandler {

    /**
     * A parameter that handles a player.
     */
    public PlayerParameter() {
        super("Player", Pattern.compile("[~|!]?[\\w,]+"), "p");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(QueryParameters query, String alias, String input, CommandSender sender) {
        MatchRule match = MatchRule.INCLUDE;
        if (input.startsWith("!")) {
            match = MatchRule.EXCLUDE;
            input = input.replace("!", "");
        } else if (input.startsWith("~")) {
            match = MatchRule.PARTIAL;
            input = input.replace("~", "");
        }
        final String[] playerNames = input.split(",");
        if (playerNames.length > 0) {
            for (String playerName : playerNames) {
                query.addPlayerName(playerName, match);
            }
        }
    }

    @Override
    protected List<String> tabComplete(String alias, String partialParameter, CommandSender sender) {
        String prefix = "";
        String partialName = partialParameter;
        if (partialParameter.startsWith("!") || partialParameter.startsWith("~")) {
            prefix = partialParameter.substring(0, 1);
            partialName = partialParameter.substring(1);
        }
        final int end = partialName.lastIndexOf(',');
        if (end != -1) {
            prefix = prefix + partialName.substring(0, end) + ",";
            partialName = partialName.substring(end + 1);
        }
        return getPlayerNameCompletions(prefix, partialName);
    }
}