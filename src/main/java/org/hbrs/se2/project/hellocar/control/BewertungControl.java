package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.BewertungInvalidException;
import org.hbrs.se2.project.hellocar.control.exception.BewertungNotLoggedInException;
import org.hbrs.se2.project.hellocar.control.exception.BewertungNurStudentException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Bewertung;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.BewertungRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class BewertungControl {
    @Autowired
    private BewertungRepository bewertungRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Transactional
    public Bewertung bewertungErstellen(BenutzerDTO user, int ideeId, int sterne, String kommentar) {

        // 1. Authentifizierung prüfen
        if (user == null) {
            throw new BewertungNotLoggedInException("User muss eingeloggt sein.");
        }

        // 2. Rollenprüfung: Nur Studenten dürfen bewerten (Studenten-DB Check)
        if (!studentRepository.existsById(user.getUserId())) {
            throw new BewertungNurStudentException("Nur Studenten dürfen bewerten!");
        }

        // 3. Validierung: Sterne-Bereich (1-5)
        if (sterne < 1 || sterne > 5) {
            throw new BewertungInvalidException("Bewertung muss zwischen 1 und 5 liegen.");
        }

        // 4. Validierung: Kommentar darf nicht leer sein (Studenten-Standard)
        if (kommentar == null || kommentar.trim().isEmpty()) {
            throw new BewertungInvalidException("Kommentar darf nicht leer sein.");
        }



        Bewertung b = new Bewertung();
        b.setIdeeId(ideeId);
        // Wir setzen die studentUserId aus dem DTO des eingeloggten Benutzers
        b.setStudentUserId(user.getUserId());
        b.setBewertung(sterne);
        b.setKommentar(kommentar);
        b.setZeitstempel(LocalDateTime.now());

        return bewertungRepository.save(b);
    }

    /**
     * Lädt alle Bewertungen zu einer Idee, neueste zuerst.
     */
    @Transactional
    public List<Bewertung> ladeBewertungenFuerIdeeSortiert(int ideeId) {
        return bewertungRepository.findByIdeeId(ideeId).stream()
                .sorted(Comparator.comparing(Bewertung::getZeitstempel,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .toList();
    }

    /**
     * Löst den Benutzernamen des Studenten auf, der eine Bewertung abgegeben hat.
     */
    @Transactional
    public String ermittleBenutzernameFuerBewertung(Bewertung bewertung) {
        return studentRepository.findById(bewertung.getStudentUserId())
                .map(Student::getUserId)
                .flatMap(benutzerRepository::findById)
                .map(Benutzer::getUsername)
                .orElse("Unbekannt");
    }

    /**
     * Erzeugt die Sternchen-Darstellung (★/☆) für eine Bewertung.
     */
    public static String createStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString();
    }
}
