package me.botsko.prism.database;

import me.botsko.prism.api.actions.Handler;

public interface UpdateQuery {

    void updateRollbacked(Handler... handlers);

}
