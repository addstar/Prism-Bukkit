package me.botsko.prism.api;

import me.botsko.prism.api.actions.ActionRegistry;
import me.botsko.prism.api.actions.Handler;
import org.bukkit.command.CommandSender;

import java.util.concurrent.Future;

/**
 * The Prism Api currently can provide an interface for external plugins to execute a lookup and return
 * a set of results.
 *
 * @author Narimm
 */
public interface PrismApi {

    /**
     * Creates a default set of parameters that can be used to perform a query.
     * The parameters can be modified after creation.
     * @return {@link PrismParameters}.
     */
    PrismParameters createParameters();

    /**
     * Performs an asynchronous lookup that will return a result.
     * @param parameters PrismParameters best created with {@link PrismApi#createParameters()}
     * @param sender CommandSender a Bukkit Command Sender.
     * @return Future - that will complete with a {@link Future} The future will contain a {@link Result}
     */
    Future<Result> performLookup(PrismParameters parameters, CommandSender sender);

    ActionRegistry getActionRegistry();

    void handleCustomAction(Handler handler);

}
