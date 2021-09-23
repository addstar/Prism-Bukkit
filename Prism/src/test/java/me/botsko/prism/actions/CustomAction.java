package me.botsko.prism.actions;

import me.botsko.prism.api.actions.Action;
import me.botsko.prism.api.actions.ActionType;
import me.botsko.prism.api.actions.Handler;

/**
 * An example of a custom action.
 * Created for Prism.
 *
 * @author Narimm on 18/08/2021
 * @since 2.1.8
 */
public class CustomAction implements Action {

    private final Handler customHandler;

    public CustomAction() {
        this.customHandler = new CustomHandler();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.CUSTOM_ACTION;
    }

    @Override
    public boolean canRollback() {
        return false;
    }

    @Override
    public boolean canRestore() {
        return false;
    }

    @Override
    public Class<? extends Handler> getHandler() {
        return null;
    }

    @Override
    public boolean requiresHandler(Class<? extends Handler> handler) {
        return false;
    }

    @Override
    public boolean doesCreateBlock() {
        return true;
    }

    @Override
    public String getName() {
        return "custom-action-block-create";
    }

    @Override
    public String getShortName() {
        return "block-create";
    }

    @Override
    public String getFamilyName() {
        return "custom-action";
    }
}
