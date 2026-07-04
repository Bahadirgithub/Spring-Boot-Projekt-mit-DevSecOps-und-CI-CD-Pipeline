package org.hbrs.se2.project.hellocar.control;

import com.vaadin.flow.component.UI;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.util.CryptoProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetControl {

    private final LoginControl loginControl;
    private final BenutzerRepository benutzerRepository;

    public PasswordResetControl(LoginControl loginControl, BenutzerRepository benutzerRepository) {
        this.loginControl = loginControl;
        this.benutzerRepository = benutzerRepository;
    }

    protected Object getSessionAttribute(String name) {
        return UI.getCurrent().getSession().getAttribute(name);
    }

    protected void setSessionAttribute(String name, Object value) {
        UI.getCurrent().getSession().setAttribute(name, value);
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

    public void updatePassword(String newPassword) throws DatabaseUserException {
        try {
            // Hole die vorgemerkte User-ID aus der Session
            Integer userId = (Integer) getSessionAttribute("reset_user_id");

            // Sicherheitscheck: Falls keine ID vorhanden ist, wurde der Prozess nicht korrekt gestartet
            if (userId == null) {
                throw new DatabaseUserException("Kein aktiver Passwort-Zurücksetzungsprozess gefunden.");
            }

            // Benutzer anhand der ID aus der Datenbank laden
            Benutzer benutzer = benutzerRepository.findById(userId)
                    .orElseThrow(DatabaseUserException::new);

            // Neues Passwort setzen (Falls ihr Passwörter hasht, hier das gehashte PW übergeben!)
            String hashedPw = CryptoProvider.CreatePassword(newPassword);
            benutzer.setPasswort(hashedPw);

            // Änderungen in der Datenbank speichern
            benutzerRepository.save(benutzer);

            // WICHTIG: Die ID aus der Session löschen, damit der Vorgang abgeschlossen ist
            setSessionAttribute("reset_user_id", null);

        } catch (DataAccessResourceFailureException e) {
            throw new DatabaseUserException("Fehler beim Speichern des neuen Passworts in der Datenbank.");
        }
    }
}