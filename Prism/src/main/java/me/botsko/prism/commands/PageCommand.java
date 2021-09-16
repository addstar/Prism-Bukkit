package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionMessage;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.api.commands.Flag;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import me.botsko.prism.utils.MiscUtils;
import me.botsko.prism.utils.TypeUtils;
import net.kyori.adventure.identity.Identity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class PageCommand implements SubHandler {

    private final Prism plugin;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public PageCommand(Prism plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(CallInfo call) {

        // Is there anything even stored to paginate?
        String keyName = "console";
        if (call.getSender() instanceof Player) {
            keyName = call.getSender().getName();
        }
        if (!plugin.cachedQueries.containsKey(keyName)) {
            Prism.messenger.sendMessage(call.getSender(), Prism.messenger
                    .playerError("There's no saved query to paginate. Maybe they expired? Try your lookup again."));
            return;
        }

        // Get stored results
        final QueryResult results = plugin.cachedQueries.get(keyName);

        if (call.getArgs().length != 2) {
            Prism.getAudiences().sender(call.getSender())
                    .sendMessage(Identity.nil(),
                          Prism.messenger.playerError("Please specify a page number. Like /prism page 2"));
            return;
        }

        // Determine page number
        int page;
        if (TypeUtils.isNumeric(call.getArg(1))) {
            page = Integer.parseInt(call.getArg(1));
        } else {

            if (call.getArg(1).equals("next") || call.getArg(1).equals("n")) {
                page = results.getPage() + 1;
            } else if (call.getArg(1).equals("prev") || call.getArg(1).equals("p")) {
                if (results.getPage() <= 1) {
                    Prism.messenger.sendMessage(call.getSender(),
                            Prism.messenger.playerError("There is no previous page."));
                    return;
                }
                page = results.getPage() - 1;
            } else {
                Prism.messenger.sendMessage(call.getSender(), Prism.messenger
                        .playerError("Page numbers need to actually be numbers, or next/prev."
                                + " Like /prism page 2"));
                return;
            }
        }

        // No negatives
        if (page <= 0) {
            Prism.messenger.sendMessage(call.getSender(),
                    Prism.messenger.playerError("Page must be greater than zero."));
            return;
        }

        results.setPage(page);

        // Refresh the query time and replace
        results.setQueryTime();
        plugin.cachedQueries.replace(keyName, results);

        // Results?
        if (results.getActionResults().isEmpty()) {
            Prism.messenger.sendMessage(call.getSender(), Prism.messenger
                    .playerError("Nothing found." + ChatColor.GRAY
                            + " Either you're missing something, or we are."));
            return;
        }

        Prism.messenger.sendMessage(call.getSender(),
                Prism.messenger.playerHeaderMsg(Il8nHelper.formatMessage("lookup-header-message",
                        results.getTotalResults(), results.getPage(), results.getTotalPages())));
        final List<Handler> paginated = results.getPaginatedActionResults();
        if (paginated == null || paginated.size() == 0) {
            Prism.messenger.sendMessage(call.getSender(),
                    Prism.messenger.playerError("Pagination can't find anything. "
                            + "Do you have the right page number?"));
            return;
        }

        // Show it!
        int resultCount = results.getIndexOfFirstResult();
        for (final Handler a : paginated) {
            final ActionMessage am = new ActionMessage(a);
            if (results.getParameters().hasFlag(Flags.EXTENDED)
                    || plugin.config.parameterConfig.alwaysShowExtended) {
                am.showExtended();
            }
            am.setResultIndex(resultCount);
            MiscUtils.sendClickableTpRecord(am, call.getSender());
            resultCount++;
        }
        MiscUtils.sendPageButtons(results, call.getSender());

    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-pg-nav")};
    }

    @Override
    public String getRef() {
        return "/lookups.html#pagination";
    }
}