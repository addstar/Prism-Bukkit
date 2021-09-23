package me.botsko.prism.commandlibs;

import java.util.List;

public interface SubHandler {

    void handle(CallInfo call);

    List<String> handleComplete(CallInfo call);

    /**
     * Returns a short help message.
     * @return String
     */
    String[] getHelp();

    /**
     * This should return the web reference to documentation.
     * @return String
     */
    String getRef();
}
