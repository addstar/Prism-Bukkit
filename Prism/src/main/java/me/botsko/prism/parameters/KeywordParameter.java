package me.botsko.prism.parameters;

import me.botsko.prism.actionlibs.QueryParameters;
import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class KeywordParameter extends SimplePrismParameterHandler {

    /**
     * A keyword parameter.
     */
    public KeywordParameter() {
        super("Keyword", Pattern.compile("[^\\s]+"), "k");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(QueryParameters query, String alias, String input, CommandSender sender) {
        query.setKeyword(input);
    }
}