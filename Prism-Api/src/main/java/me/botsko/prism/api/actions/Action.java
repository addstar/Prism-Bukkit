package me.botsko.prism.api.actions;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 10/01/2021.
 */
public interface Action {

    /**
     * Get Action Type of an action.
     * @return {@link ActionType}
     */
    ActionType getActionType();

    /**
     * True if this Action can rollback.
     * @return {@code boolean}
     */
    boolean canRollback();

    /**
     * True if this Action can restore.
     * @return {@code boolean}
     */
    boolean canRestore();

    Class<? extends Handler> getHandler();

    /**
     *  Checks if an Action requires a specific handler.
     * @param handler Handler
     * @return boolean
     */
    boolean requiresHandler(Class<? extends Handler> handler);

    /**
     * True if this Action can create a block.
     * @return {@code boolean}
     */
    boolean doesCreateBlock();

    /**
     * Gets the name of the Action Type.
     * @return the Action Name.
     */
    String getName();

    /**
     * Get the Class Short name.
     * see {@link ActionType#getShortName()} ()} for examples on this.
     * @return String
     */
    String getShortName();

    /**
     * Get the Action Class Family name.
     * see {@link ActionType#getFamilyName()} for examples on this.
     * @return String
     */
    String getFamilyName();


}
