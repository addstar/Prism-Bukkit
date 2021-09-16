package me.botsko.prism.purge;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.text.ReplaceableTextComponent;
import org.bukkit.command.CommandSender;

public class SenderPurgeCallback implements PurgeCallback {

    private CommandSender sender;

    @Override
    public void cycle(QueryParameters param, int cycleRowsAffected, int totalRecordsAffected,
                      boolean cycleComplete, long maxCycleTime) {
        if (sender == null) {
            return;
        }
        Prism.messenger.sendMessage(sender,
                Prism.messenger.playerSubduedHeaderMsg(ReplaceableTextComponent.builder("purge-cycle-cleared")
                        .replace("<cycleRowsAffected>", cycleRowsAffected)
                        .build()));
        if (cycleComplete) {
            Prism.messenger.sendMessage(sender,
                    Prism.messenger.playerHeaderMsg(ReplaceableTextComponent.builder("purge-cycle-complete")
                            .replace("<totalRecordsAffected>", totalRecordsAffected)
                            .replace("<maxCycleTime>", maxCycleTime)
                            .build()));
        }
    }

    public void setSender(CommandSender sender) {
        this.sender = sender;
    }
}