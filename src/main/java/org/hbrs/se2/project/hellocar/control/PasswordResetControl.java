package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetControl {

    private final LoginControl loginControl;

    public PasswordResetControl(LoginControl loginControl) {
        this.loginControl = loginControl;
    }

    // Wir werfen hier eigene Exceptions für die Validierung
    public void validatePasswords(String newPw, String confirmPw) throws IllegalArgumentException {
        if (newPw == null || newPw.isEmpty()) {
            throw new IllegalArgumentException("Das Passwort darf nicht leer sein.");
        }
        if (newPw.length() < 8) {
            throw new IllegalArgumentException("Das Passwort muss mindestens 8 Zeichen lang sein.");
        }
        if (!newPw.equals(confirmPw)) {
            throw new IllegalArgumentException("Die Passwörter stimmen nicht überein!");
        }
    }

    public void updatePassword(String newPw) throws DatabaseUserException {
        loginControl.updatePassword(newPw);
    }
}