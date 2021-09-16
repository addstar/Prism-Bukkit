package me.botsko.prism.commands;

import me.botsko.prism.Il8nHelper;
import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.api.actions.PrismProcessType;
import me.botsko.prism.appliers.Previewable;
import me.botsko.prism.appliers.PrismApplierCallback;
import me.botsko.prism.appliers.Restore;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.PreprocessArgs;
import me.botsko.prism.text.ReplaceableTextComponent;
import org.bukkit.entity.Player;

import java.util.List;

public class RestoreCommand extends AbstractCommand {

    private final Prism plugin;

    public RestoreCommand(Prism plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(final CallInfo call) {

        final QueryParameters parameters = PreprocessArgs.process(plugin.config, call.getSender(), call.getArgs(),
                PrismProcessType.RESTORE, 1, !plugin.config.parameterConfig.neverUseDefaults);
        if (parameters == null) {
            return;
        }
        parameters.setProcessType(PrismProcessType.RESTORE);
        parameters.setStringFromRawArgs(call.getArgs(), 1);
        StringBuilder defaultsReminder = checkIfDefaultUsed(parameters);
        Prism.messenger.sendMessage(call.getSender(),
                Prism.messenger.playerSubduedHeaderMsg(ReplaceableTextComponent.builder("restore-prepare")
                        .replace("<defaults>", defaultsReminder).build()));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            final ActionsQuery aq = new ActionsQuery(plugin);
            final QueryResult results = aq.lookup(parameters, call.getSender());
            if (!results.getActionResults().isEmpty()) {

                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("restore-start")));

                // Inform nearby players
                if (call.getSender() instanceof final Player player) {
                    plugin.notifyNearby(player, parameters.getRadius(), ReplaceableTextComponent
                            .builder("block-changes-near")
                            .replace("<player>", player.getDisplayName())
                            .build());
                }

                // Perform restore on the main thread
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    final Previewable rs = new Restore(plugin, call.getSender(), results.getActionResults(),
                            parameters, new PrismApplierCallback());
                    rs.apply();
                });

            } else {
                Prism.messenger.sendMessage(call.getSender(),
                        Prism.messenger.playerError(Il8nHelper.getMessage("restore-error")));
            }
        });
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return PreprocessArgs.complete(call.getSender(), call.getArgs());
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-restore")};
    }

    @Override
    public String getRef() {
        return "/restore.html";
    }
}