package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.*;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrationstests für das {@link BewerbungRepository}.
 *
 * Geprüft werden die Repository-Methoden zum Finden von Bewerbungen
 * anhand der Studenten-ID sowie der Gründungsidee.
 */
@SpringBootTest
@Transactional
class BewerbungRepositoryTest {

    @Autowired
    private BewerbungRepository bewerbungRepository;

    @Autowired
    private TestDatenFabrik testDatenFabrik;

    /**
     * Prüft, ob eine Bewerbung über die Studenten-ID
     * erfolgreich gefunden werden kann.
     */
    @Test
    void findByStudentUserId_findetBewerbungDesStudenten() {

        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer(UUID.randomUUID().toString());
        Student student = testDatenFabrik.erstelleUndSpeichereStudent(benutzer);

        Benutzer gruenderBenutzer = testDatenFabrik.erstelleUndSpeichereBenutzer(UUID.randomUUID().toString());
        Gruender gruender = testDatenFabrik.erstelleUndSpeichereGruender(gruenderBenutzer);
        Gruendungsidee idee = testDatenFabrik.erstelleUndSpeichereGruendungsidee(gruender);

        Bewerbung bewerbung = testDatenFabrik.erstelleUndSpeichereBewerbung(student, idee);

        List<Bewerbung> result = bewerbungRepository.findByStudentUserId(student.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBewerbungId()).isEqualTo(bewerbung.getBewerbungId());
    }

    /**
     * Prüft, ob eine Bewerbung anhand der Ideen-ID
     * korrekt aus der Datenbank geladen wird.
     */
    @Test
    void findByIdeeId_findetBewerbungZurIdee() {

        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer(UUID.randomUUID().toString());
        Student student = testDatenFabrik.erstelleUndSpeichereStudent(benutzer);

        Benutzer gruenderBenutzer = testDatenFabrik.erstelleUndSpeichereBenutzer(UUID.randomUUID().toString());
        Gruender gruender = testDatenFabrik.erstelleUndSpeichereGruender(gruenderBenutzer);
        Gruendungsidee idee = testDatenFabrik.erstelleUndSpeichereGruendungsidee(gruender);

        Bewerbung bewerbung = testDatenFabrik.erstelleUndSpeichereBewerbung(student, idee);

        List<Bewerbung> result = bewerbungRepository.findByIdeeId(idee.getIdeeId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBewerbungId()).isEqualTo(bewerbung.getBewerbungId());
    }

    /**
     * Prüft, ob eine Bewerbung anhand der Kombination
     * aus Studenten-ID und Ideen-ID gefunden wird.
     */
    @Test
    void findByStudentUserIdAndIdeeId_findetPassendeBewerbung() {

        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer(UUID.randomUUID().toString());
        Student student = testDatenFabrik.erstelleUndSpeichereStudent(benutzer);

        Benutzer gruenderBenutzer = testDatenFabrik.erstelleUndSpeichereBenutzer(UUID.randomUUID().toString());
        Gruender gruender = testDatenFabrik.erstelleUndSpeichereGruender(gruenderBenutzer);
        Gruendungsidee idee = testDatenFabrik.erstelleUndSpeichereGruendungsidee(gruender);

        Bewerbung bewerbung = testDatenFabrik.erstelleUndSpeichereBewerbung(student, idee);

        Optional<Bewerbung> result =
                bewerbungRepository.findByStudentUserIdAndIdeeId(student.getUserId(), idee.getIdeeId());

        assertThat(result).isPresent();
        assertThat(result.get().getBewerbungId()).isEqualTo(bewerbung.getBewerbungId());
    }
}