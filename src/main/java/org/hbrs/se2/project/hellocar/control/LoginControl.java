package org.hbrs.se2.project.hellocar.control;

import com.vaadin.flow.component.UI;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.control.exception.PasswordInvalidException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.util.CryptoProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LoginControl {

    private final BenutzerRepository benutzerRepository;

    public LoginControl(BenutzerRepository benutzerRepository) {
        this.benutzerRepository = benutzerRepository;
    }

    public BenutzerDTO authenticate(String username, String password) throws PasswordInvalidException, DatabaseUserException, DataAccessResourceFailureException {
        Benutzer benutzer = benutzerRepository.findByUsername(username)
                .orElseThrow(DatabaseUserException::new);

        if(!CryptoProvider.ValidatePassword(password, benutzer.getPasswort())) {
            throw new PasswordInvalidException();
        }

        return new BenutzerDTOImpl(
                benutzer.getUserId(),
                benutzer.getUsername(),
                benutzer.getVorname(),
                benutzer.getNachname(),
                benutzer.getGeburtstag(),
                benutzer.getEmail(),
                benutzer.getTelefonnummer()
        );
    }

    public boolean checkResetCredentials(String username, String email) throws DatabaseUserException {
        try {
            // Suche den Benutzer anhand der Kombination im Repository
            Benutzer benutzer = benutzerRepository.findBenutzerByUsernameAndEmail(username, email)
                    .orElseThrow(DatabaseUserException::new);

            // Benutzer verifiziert! Wir merken uns die ID in der Vaadin-Session für den nächsten Schritt
            UI.getCurrent().getSession().setAttribute("reset_user_id", benutzer.getUserId());
            return true;

        } catch (DataAccessResourceFailureException e) {
            throw new DatabaseUserException("Fehler bei der Verbindung zur Datenbank während der Verifizierung.");
        }
    }

    public void updatePassword(String newPassword) throws DatabaseUserException {
        try {
            // Hole die vorgemerkte User-ID aus der Session
            // TODO fix method
            Integer userId = (Integer) UI.getCurrent().getSession().getAttribute("reset_user_id");

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
            UI.getCurrent().getSession().setAttribute("reset_user_id", null);

        } catch (DataAccessResourceFailureException e) {
            throw new DatabaseUserException("Fehler beim Speichern des neuen Passworts in der Datenbank.");
        }
    }
}