package me.botsko.prism.exceptions;

public class InvalidActionException extends Exception {

    private static final long serialVersionUID = -6924719015186263369L;

    /**
     *  An exception thrown when an action is invalid.
     * @param message String
     */
    public InvalidActionException(String message) {
        super(message);
    }
}
