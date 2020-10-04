package me.botsko.prism.commands;

import me.botsko.prism.Il8n;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.appliers.PreviewSession;
import me.botsko.prism.appliers.Previewable;
import me.botsko.prism.appliers.PrismApplierCallback;
import me.botsko.prism.appliers.PrismProcessType;
import me.botsko.prism.appliers.Restore;
import me.botsko.prism.appliers.Rollback;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.PreprocessArgs;
import me.botsko.prism.utils.MiscUtils;
import net.kyori.adventure.audience.Audience;

import java.util.ArrayList;
import java.util.List;

public class PreviewCommand extends AbstractCommand {

    private final Prism plugin;

    private final List<String> secondaries;

    /**
     * Contructor.
     *
     * @param plugin Prism
     */
    public PreviewCommand(Prism plugin) {
        this.plugin = plugin;
        secondaries = new ArrayList<>();
        secondaries.add("apply");
        secondaries.add("cancel");
        secondaries.add("rollback");
        secondaries.add("restore");
        secondaries.add("rb");
        secondaries.add("rs");
    }

    @Override
    public void handle(final CallInfo call) {
        final Audience audience = Prism.getAudiences().sender(call.getPlayer());
        if (call.getArgs().length >= 2) {

            if (call.getArg(1).equalsIgnoreCase("apply")) {
                if (plugin.playerActivePreviews.containsKey(call.getPlayer().getName())) {
                    final PreviewSession previewSession = plugin.playerActivePreviews.get(call.getPlayer().getName());
                    previewSession.getPreviewer().apply_preview();
                    plugin.playerActivePreviews.remove(call.getPlayer().getName());
                } else {
                    audience.sendMessage(Prism.messenger.playerError("You have no preview pending."));
                }
                return;
            }

            if (call.getArg(1).equalsIgnoreCase("cancel")) {
                if (plugin.playerActivePreviews.containsKey(call.getPlayer().getName())) {
                    final PreviewSession previewSession = plugin.playerActivePreviews.get(call.getPlayer().getName());
                    previewSession.getPreviewer().cancel_preview();
                    plugin.playerActivePreviews.remove(call.getPlayer().getName());
                } else {
                    audience.sendMessage(Prism.messenger.playerError("You have no preview pending."));
                }
                return;
            }

            // Ensure no current preview is waiting
            if (plugin.playerActivePreviews.containsKey(call.getPlayer().getName())) {
                audience.sendMessage(Prism.messenger
                        .playerError("You have an existing preview pending. Please apply or cancel before moving on."));
                return;
            }

            if (call.getArg(1).equalsIgnoreCase("rollback") || call.getArg(1).equalsIgnoreCase("restore")
                    || call.getArg(1).equalsIgnoreCase("rb") || call.getArg(1).equalsIgnoreCase("rs")) {

                final QueryParameters parameters = PreprocessArgs.process(plugin, call.getPlayer(), call.getArgs(),
                        PrismProcessType.ROLLBACK, 2,
                        !plugin.getConfig().getBoolean("prism.queries.never-use-defaults"));
                if (parameters == null) {
                    return;
                }
                parameters.setStringFromRawArgs(call.getArgs(), 1);

                if (parameters.getActionTypes().containsKey("world-edit")) {
                    audience.sendMessage(Prism.messenger
                            .playerError("Prism does not support previews for WorldEdit rollbacks/restores yet."));
                    return;
                }
                StringBuilder defaultsReminder = checkIfDefaultUsed(parameters);
                audience.sendMessage(Prism.messenger
                        .playerSubduedHeaderMsg(
                                Il8n.getMessage("queryparameter.defaults.prefix",
                                        defaultsReminder.toString())));
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

                    // Perform preview
                    final ActionsQuery aq = new ActionsQuery(plugin);
                    final QueryResult results = aq.lookup(parameters, call.getPlayer());

                    // Rollback
                    if (call.getArg(1).equalsIgnoreCase("rollback")
                            || call.getArg(1).equalsIgnoreCase("rb")) {
                        parameters.setProcessType(PrismProcessType.ROLLBACK);
                        if (!results.getActionResults().isEmpty()) {

                            audience.sendMessage(Prism.messenger.playerHeaderMsg(
                                    Il8n.getMessage("preview-apply-start")));

                            // Perform preview on the main thread
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                final Previewable rs = new Rollback(plugin, call.getPlayer(),
                                        results.getActionResults(), parameters, new PrismApplierCallback());
                                rs.preview();
                            });
                        } else {
                            audience.sendMessage(Prism.messenger.playerError("Nothing found to preview."));
                        }
                    }
                    // Restore
                    if (call.getArg(1).equalsIgnoreCase("restore")
                            || call.getArg(1).equalsIgnoreCase("rs")) {
                        parameters.setProcessType(PrismProcessType.RESTORE);
                        if (!results.getActionResults().isEmpty()) {

                            audience.sendMessage(Prism.messenger.playerHeaderMsg(
                                    Il8n.getMessage("preview-apply-start")));

                            // Perform preview on the main thread
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                final Previewable rs = new Restore(plugin, call.getPlayer(),
                                        results.getActionResults(), parameters, new PrismApplierCallback());
                                rs.preview();
                            });
                        } else {
                            audience.sendMessage(Prism.messenger.playerError(Il8n.getMessage("preview-no-actions")));
                        }
                    }
                });
                return;
            }

            audience.sendMessage(Prism.messenger.playerError("Invalid command. Check /prism ? for help."));

        }
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        if (call.getArgs().length == 2) {
            return MiscUtils.getStartingWith(call.getArg(1), secondaries);
        }
        return PreprocessArgs.complete(call.getSender(), call.getArgs());
    }
}