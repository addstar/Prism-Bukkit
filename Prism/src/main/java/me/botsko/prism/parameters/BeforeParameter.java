package me.botsko.prism.parameters;

import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.utils.DateUtil;
import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class BeforeParameter extends SimplePrismParameterHandler {


    public BeforeParameter() {
        super("Before", Pattern.compile("[\\w]+"), "before");
    }

    @Override
    public void process(QueryParameters query, String alias, String input, CommandSender sender) {
        final Long date = DateUtil.translateTimeStringToDate(input);
        if (date != null) {
            query.setBeforeTime(date);
        } else {
            throw new IllegalArgumentException(
                    "Date/time for 'before' parameter value not recognized. Try /pr ? for help");
        }
    }
}