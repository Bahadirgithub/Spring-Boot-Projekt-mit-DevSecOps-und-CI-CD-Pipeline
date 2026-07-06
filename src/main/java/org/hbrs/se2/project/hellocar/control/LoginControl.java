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
            UI currentUi = UI.getCurrent();
            if (currentUi != null && currentUi.getSession() != null) {
                currentUi.getSession().setAttribute("reset_user_id", benutzer.getUserId());
            }
            return true;

        } catch (DataAccessResourceFailureException e) {
            throw new DatabaseUserException("Fehler bei der Verbindung zur Datenbank während der Verifizierung.");
        }
    }

}