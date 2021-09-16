package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.appliers.PrismApplierCallback;
import me.botsko.prism.appliers.Rollback;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.PreprocessArgs;
import me.botsko.prism.text.ReplaceableTextComponent;

import java.util.List;

public class RollbackCommand extends AbstractCommand {

    private final Prism plugin;

    public RollbackCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(final CallInfo call) {

        final QueryParameters parameters = PreprocessArgs.process(plugin.config, call.getSender(), call.getArgs(),
                PrismProcessType.ROLLBACK, 1, !plugin.config.parameterConfig.neverUseDefaults);
        if (parameters == null) {
            return;
        }
        parameters.setProcessType(PrismProcessType.ROLLBACK);
        parameters.setStringFromRawArgs(call.getArgs(), 1);
        StringBuilder defaultsReminder = checkIfDefaultUsed(parameters);
        Prism.messenger.sendMessage(call.getSender(),
                Prism.messenger.playerSubduedHeaderMsg(ReplaceableTextComponent.builder("rollback-prepare")
                        .replace("<defaults>", defaultsReminder)
                        .build()));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            final ActionsQuery aq = new ActionsQuery(plugin);
            final QueryResult results = aq.lookup(parameters, call.getSender());
            if (!results.getActionResults().isEmpty()) {

                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("rollback-start")));

                // Perform rollback on the main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    final Rollback rb = new Rollback(plugin, call.getSender(), results.getActionResults(),
                            parameters, new PrismApplierCallback());
                    rb.apply();
                });

            } else {
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerError(Il8nHelper.getMessage("rollback-error")));
            }
        });
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return PreprocessArgs.complete(call.getSender(), call.getArgs());
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-rollback")};
    }

    @Override
    public String getRef() {
        return "/rollbacks.html";
    }
}