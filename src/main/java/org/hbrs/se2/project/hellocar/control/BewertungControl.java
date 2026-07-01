package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Bewertung;
import org.hbrs.se2.project.hellocar.repository.BewertungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class BewertungControl {
    @Autowired
    private BewertungRepository bewertungRepository;

    @Transactional
    public Bewertung bewertungErstellen(BenutzerDTO user, int ideeId, int sterne, String kommentar) {
        Bewertung b = new Bewertung();
        b.setIdeeId(ideeId);
        // Wir setzen die studentUserId aus dem DTO des eingeloggten Benutzers
        b.setStudentUserId(user.getUserId());
        b.setBewertung(sterne);
        b.setKommentar(kommentar);
        b.setZeitstempel(LocalDateTime.now());

        return bewertungRepository.save(b);
    }
}
