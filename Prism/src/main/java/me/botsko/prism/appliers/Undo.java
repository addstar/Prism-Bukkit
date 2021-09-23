package me.botsko.prism.appliers;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.actions.Handler;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class Undo extends Preview {

    /**
     * Constructor.
     *
     * @param plugin Prism
     */
    public Undo(Prism plugin, Player player, Collection<Handler> results, QueryParameters parameters,
                ApplierCallback callback) {
        super(plugin, player, results, parameters, callback);
    }

    @Override
    public void preview() {
        Prism.messenger.sendMessage(player, Prism.messenger.playerError("You can't preview an undo."));
    }
}