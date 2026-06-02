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

    public boolean authentificate(String username, String password) throws DatabaseUserException {
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
}