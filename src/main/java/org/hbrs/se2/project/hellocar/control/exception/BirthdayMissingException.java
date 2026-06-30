package org.hbrs.se2.project.hellocar.control.exception;

/**
 * Exception, die geworfen wird, wenn bei der Registrierung kein Geburtsdatum angegeben wurde.
 */
public class BirthdayMissingException extends Exception {
    public BirthdayMissingException(String message) {
        super(message);
    }
}