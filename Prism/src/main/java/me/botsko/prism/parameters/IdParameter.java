package me.botsko.prism.parameters;

import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class IdParameter extends SimplePrismParameterHandler {

    /**
     * Constructor.
     */
    public IdParameter() {
        super("ID", Pattern.compile("[\\d,]+"), "id");
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public void process(QueryParameters query, String alias, String input, CommandSender sender) {

        if (!TypeUtils.isNumeric(input)) {
            throw new IllegalArgumentException("ID must be a number. Use /prism ? for help.");
        }
        query.setId(Long.parseLong(input));
    }
}