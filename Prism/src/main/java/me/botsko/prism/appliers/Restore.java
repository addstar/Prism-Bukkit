package me.botsko.prism.appliers;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.api.actions.Handler;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class Restore extends Preview {

    /**
     * Create a restore.
     *
     * @param plugin Prism
     */
    public Restore(Prism plugin, CommandSender sender, Collection<Handler> results, QueryParameters parameters,
                   ApplierCallback callback) {
        super(plugin, sender, results, parameters, callback);
    }

    /**
     * Set preview move and then do a rollback.
     */
    @Override
    public void preview() {
        setIsPreview(true);
        apply();
    }
}