package org.hbrs.se2.project.hellocar.control.exception;

/**
 * Exception, die geworfen wird, wenn der Benutzer das erforderliche Mindestalter nicht erreicht hat.
 */
public class UserTooYoungException extends Exception {
    public UserTooYoungException(String message) {
        super(message);
    }
}