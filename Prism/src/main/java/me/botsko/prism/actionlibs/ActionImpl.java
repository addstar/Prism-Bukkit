package me.botsko.prism.actionlibs;

import me.botsko.prism.api.actions.Action;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;

public class ActionImpl implements Action {

    private final boolean doesCreateBlock;
    private final boolean canRollback;
    private final boolean canRestore;
    private final Class<? extends Handler> handler;
    private final String niceDescription;
    private final ActionType type;

    /**
     * Create An Action. -  it cant be restored rolled back and doesnt create a block.
     *
     * @param type            Name
     * @param handler         Handler
     * @param niceDescription nice description.
     */
    @SuppressWarnings("unused")
    public ActionImpl(ActionType type, Class<? extends Handler> handler, String niceDescription) {
        this(type, false, false, false, handler, niceDescription);
    }

    /**
     * Create An Action.
     *
     * @param type            Name
     * @param doesCreateBlock true if creates a block
     * @param canRollback     can we roll this back
     * @param canRestore      can it be restored
     * @param handler         Handler
     * @param niceDescription nice description.
     */
    public ActionImpl(ActionType type, boolean doesCreateBlock, boolean canRollback, boolean canRestore,
                          Class<? extends Handler> handler, String niceDescription) {
        this.doesCreateBlock = doesCreateBlock;
        this.canRollback = canRollback;
        this.canRestore = canRestore;
        this.handler = handler;
        this.niceDescription = niceDescription;
        this.type = type;
    }

    public boolean canRollback() {
        return canRollback;
    }

    public boolean canRestore() {
        return canRestore;
    }

    public Class<? extends Handler> getHandler() {
        return handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionType getActionType() {
        return type;
    }

    /**
     * Returns the nice name of the action.
     *
     * @return String
     */
    String getNiceDescription() {
        return niceDescription;
    }

    /**
     *  Checks if an Action requires a specific handler.
     * @param handler Handler
     * @return boolean
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean requiresHandler(Class<? extends Handler> handler) {
        return (getHandler() != null && getHandler().equals(handler));
    }

    public boolean doesCreateBlock() {
        return doesCreateBlock;
    }

    public String getName() {
        return type.name();
    }

    /**
     * Get the Class family name.
     * @return String
     */
    public String getFamilyName() {
        return this.getActionType().getFamilyName();
    }

    /**
     * Get the Class Short name.
     * @return String
     */
    public String getShortName() {
        return this.getActionType().getShortName();
    }

}