package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionMessage;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.actions.PrismProcessAction;
import me.botsko.prism.api.PrismParameters;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.appliers.Previewable;
import me.botsko.prism.appliers.PrismApplierCallback;
import me.botsko.prism.appliers.Undo;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import me.botsko.prism.utils.TypeUtils;
import org.bukkit.ChatColor;

import java.util.List;

public class UndoCommand implements SubHandler {

    private final Prism plugin;

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public UndoCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(CallInfo call) {
        if (call.getArgs().length > 1) {

            final ActionsQuery aq = new ActionsQuery(plugin);

            long recordId = 0;
            if (TypeUtils.isNumeric(call.getArg(1))) {
                recordId = Long.parseLong(call.getArg(1));
                if (recordId <= 0) {
                    Prism.messenger.sendMessage(call.getPlayer(),
                            Prism.messenger.playerError("Record ID must be greater than zero."));
                    return;
                }
            } else {
                if (call.getArg(1).equals("last")) {
                    recordId = aq.getUsersLastPrismProcessId(call.getPlayer().getName());
                }
            }

            // Invalid id
            if (recordId == 0) {
                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerError("Either you have no last process or an invalid ID."));
                return;
            }

            final PrismProcessAction process = aq.getPrismProcessRecord(recordId);
            if (process == null) {
                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerError("A process does not exists with that value."));
                return;
            }

            // We only support this for drains
            if (!process.getProcessChildActionType().equals(ActionType.PRISM_DRAIN)) {
                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerError("You can't currently undo anything other than a drain process."));
                return;
            }

            // Pull the actual block change data for this undo event
            final QueryParameters parameters = new QueryParameters();
            parameters.setWorld(call.getPlayer().getWorld().getName());
            parameters.addActionType(process.getProcessChildActionType());
            parameters.addPlayerName(call.getPlayer().getName());
            parameters.setParentId(recordId);
            parameters.setProcessType(PrismProcessType.UNDO);

            // make sure the distance isn't too far away

            final QueryResult results = aq.lookup(parameters, call.getPlayer());
            if (!results.getActionResults().isEmpty()) {

                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("command-undo-complete")));

                final Previewable rb = new Undo(plugin, call.getPlayer(), results.getActionResults(), parameters,
                        new PrismApplierCallback());
                rb.apply();

            } else {
                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerError("Nothing found to undo. Must be a problem with Prism."));
            }

        } else {

            // Show the list
            // Process and validate all of the arguments
            final PrismParameters parameters = new QueryParameters();
            parameters.setAllowNoRadius(true);
            parameters.addActionType(ActionType.PRISM_PROCESS);
            parameters.addPlayerName(call.getPlayer().getName());
            parameters.setLimit(5); // @todo config this, and move the logic
            final ActionsQuery aq = new ActionsQuery(plugin);
            final QueryResult results = aq.lookup(parameters, call.getPlayer());
            if (!results.getActionResults().isEmpty()) {
                Prism.messenger.sendMessage(call.getPlayer(),Prism.messenger.playerHeaderMsg(
                        Il8nHelper.formatMessage("lookup-header-message",
                                results.getTotalResults(), 1, results.getTotalPages())));
                Prism.messenger.sendMessage(call.getPlayer(),
                        Prism.messenger.playerSubduedHeaderMsg(Il8nHelper.getMessage("command-undo-help")));

                final List<Handler> paginated = results.getPaginatedActionResults();
                if (paginated != null) {
                    for (final Handler a : paginated) {
                        final ActionMessage am = new ActionMessage(a);
                        if (parameters.hasFlag(Flags.EXTENDED)
                                || plugin.config.parameterConfig.alwaysShowExtended) {
                            am.showExtended();
                        }
                        Prism.messenger.sendMessage(call.getPlayer(),Prism.messenger.playerMsg(am.getMessage()));
                    }
                } else {
                    Prism.messenger.sendMessage(call.getPlayer(),Prism.messenger
                            .playerError("Pagination can't find anything. Do you have the right page number?"));
                }
            } else {
                Prism.messenger.sendMessage(call.getPlayer(),Prism.messenger.playerError(
                        "Nothing found." + ChatColor.GRAY + " Either you're missing something, or we are."));
            }
        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-undo")};
    }

    @Override
    public String getRef() {
        return "/undo.html";
    }
}