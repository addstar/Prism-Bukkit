package me.botsko.prism.parameters;

import me.botsko.prism.Prism;
import me.botsko.prism.PrismLogHandler;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.config.PrismConfig;
import me.botsko.prism.utils.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Pattern;

public class SinceParameter extends SimplePrismParameterHandler {

    /**
     * Time since parameter.
     */
    public SinceParameter() {
        super("Since", Pattern.compile("[\\w]+"), "t", "since");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(QueryParameters query, String alias, String input, CommandSender sender) {
        if (input.equalsIgnoreCase("none")) {
            query.setIgnoreTime(true);
        } else {
            final Long date = DateUtil.translateTimeStringToDate(input);
            if (date != null) {
                query.setSinceTime(date);
            } else {
                throw new IllegalArgumentException(
                        "Date/time for 'since' parameter value not recognized. Try /pr ? for help");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void defaultTo(QueryParameters query, CommandSender sender) {

        if (query.getProcessType().equals(PrismProcessType.DELETE)) {
            return;
        }

        if (!query.getFoundArgs().contains("before") && !query.getFoundArgs().contains("since")) {

            final String defaultTimeSince = Prism.getInstance().config.parameterConfig.defaultTimeSince;

            Long date = DateUtil.translateTimeStringToDate(defaultTimeSince);
            if (date <= 0L) {
                PrismLogHandler.log("Error - date range configuration for prism.time-since is not valid");
                date = DateUtil.translateTimeStringToDate("3d");
            }
            query.setSinceTime(date);
            query.addDefaultUsed("t:" + defaultTimeSince);
        }
    }
}