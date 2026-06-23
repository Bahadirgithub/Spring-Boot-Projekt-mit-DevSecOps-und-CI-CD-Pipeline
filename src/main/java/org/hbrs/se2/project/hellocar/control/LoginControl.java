package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LoginControl {

    private final BenutzerRepository benutzerRepository;

    private BenutzerDTO benutzerDTO = null;

    public LoginControl(BenutzerRepository benutzerRepository) {
        this.benutzerRepository = benutzerRepository;
    }

    public boolean authenticate(String username, String password) throws DatabaseUserException {
        BenutzerDTO tmpBenutzer = this.getBenutzerWithJPA(username, password);

        if (tmpBenutzer == null) {
            return false;
        }

        this.benutzerDTO = tmpBenutzer;
        return true;
    }

    public BenutzerDTO getCurrentUser() {
        return this.benutzerDTO;
    }

    private BenutzerDTO getBenutzerWithJPA(String username, String password) throws DatabaseUserException {
        try {
            Optional<Benutzer> benutzer =
                    benutzerRepository.findByUsernameAndPasswort(username, password);

            if (benutzer.isEmpty()) {
                return null;
            }

            return convertToDTO(benutzer.get());

        } catch (DataAccessResourceFailureException e) {
            throw new DatabaseUserException("Fehler bei der Verbindung zur Datenbank mit JPA.");
        }
    }

    private BenutzerDTO convertToDTO(Benutzer benutzer) {
        BenutzerDTOImpl dto = new BenutzerDTOImpl();

        dto.setUserId(benutzer.getUserId());
        dto.setVorname(benutzer.getVorname());
        dto.setNachname(benutzer.getNachname());
        dto.setGeburtstag(benutzer.getGeburtstag());
        dto.setUsername(benutzer.getUsername());
        dto.setEmail(benutzer.getEmail());
        dto.setTelefonnummer(benutzer.getTelefonnummer());

        return dto;
    }

    public boolean checkResetCredentials(String username, String email) throws DatabaseUserException {
        try {
            // Suche den Benutzer anhand der Kombination im Repository
            Benutzer benutzer = benutzerRepository.findBenutzerByUsernameAndEmail(username, email);

            // Wenn kein Benutzer mit dieser Kombination existiert
            if (benutzer == null) {
                return false;
            }

            // Benutzer verifiziert! Wir merken uns die ID in der Vaadin-Session für den nächsten Schritt
            com.vaadin.flow.component.UI.getCurrent().getSession().setAttribute("reset_user_id", benutzer.getUserId());
            return true;

        } catch (DataAccessResourceFailureException e) {
            throw new DatabaseUserException("Fehler bei der Verbindung zur Datenbank während der Verifizierung.");
        }
    }

    public void updatePassword(String newPassword) throws DatabaseUserException {
        try {
            // Hole die vorgemerkte User-ID aus der Session
            Integer userId = (Integer) com.vaadin.flow.component.UI.getCurrent().getSession().getAttribute("reset_user_id");

            // Sicherheitscheck: Falls keine ID vorhanden ist, wurde der Prozess nicht korrekt gestartet
            if (userId == null) {
                throw new DatabaseUserException("Kein aktiver Passwort-Zurücksetzungsprozess gefunden.");
            }

            // Benutzer anhand der ID aus der Datenbank laden
            Optional<Benutzer> optionalBenutzer = benutzerRepository.findById(userId);

            if (optionalBenutzer.isEmpty()) {
                throw new DatabaseUserException("Der zu aktualisierende Benutzer wurde nicht gefunden.");
            }

            Benutzer benutzer = optionalBenutzer.get();

            // Neues Passwort setzen (Falls ihr Passwörter hasht, hier das gehashte PW übergeben!)
            benutzer.setPasswort(newPassword);

            // Änderungen in der Datenbank speichern
            benutzerRepository.save(benutzer);

            // WICHTIG: Die ID aus der Session löschen, damit der Vorgang abgeschlossen ist
            com.vaadin.flow.component.UI.getCurrent().getSession().setAttribute("reset_user_id", null);

        } catch (DataAccessResourceFailureException e) {
            throw new DatabaseUserException("Fehler beim Speichern des neuen Passworts in der Datenbank.");
        }
    }
}