package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstest für Task #27 — "Profil löschen" (DSGVO-konform).
 *
 * Testziel: Prüfen ob loeschen() alle personenbezogenen Daten eines Benutzers
 * vollständig aus PostgreSQL entfernt (Benutzer + Rolle).
 *
 * Wichtig: Wir prüfen ausschließlich den DB-Zustand nach dem Aufruf.
 * Der Rückgabewert von loeschen() wird bewusst ignoriert — was zählt
 * ist ob die Daten wirklich weg sind, nicht was die Methode zurückgibt.
 *
 * @Rollback stellt sicher dass nach jedem Test keine Testdaten übrig bleiben.
 */
@SpringBootTest
@Transactional
@Rollback
public class ProfilLoeschenIntegrationTest {

    @Autowired
    private VerwaltungControl verwaltungControl;

    @Autowired
    private TestDatenFabrik testDatenFabrik;

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GruenderRepository gruenderRepository;


    /**
     * TEST 1 — Vollständige Löschung eines Studenten-Profils.
     *
     * Szenario: Ein Benutzer mit Student-Rolle löscht sein Profil.
     * Erwartet: Benutzer UND Student-Eintrag sind danach nicht mehr in der DB.
     */
    @Test
    void testLoeschen_StudentProfil_BenutzerUndStudentAusDBEntfernt() {

        // Arrange: Student anlegen
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("LoeschTest_Student");
        testDatenFabrik.erstelleUndSpeichereStudent(benutzer);
        int userId = benutzer.getUserId();

        // Vorher-Prüfung: Einträge müssen existieren
        assertTrue(benutzerRepository.existsById(userId),
                "Benutzer muss vor der Löschung in der DB existieren.");
        assertTrue(studentRepository.existsById(userId),
                "Student muss vor der Löschung in der DB existieren.");

        BenutzerDTOImpl dto = new BenutzerDTOImpl();
        dto.setUserId(userId);

        // Act: Profil löschen — Rückgabewert wird bewusst ignoriert
        verwaltungControl.loeschen(dto);

        // Assert: DB-Zustand prüfen
        assertFalse(benutzerRepository.existsById(userId),
                "Benutzer-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");
        assertFalse(studentRepository.existsById(userId),
                "Student-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");
        assertFalse(gruenderRepository.existsById(userId),
                "Es darf kein Gründer-Eintrag für diesen User existieren.");
    }


    /**
     * TEST 2 — Vollständige Löschung eines Gründer-Profils.
     *
     * Szenario: Ein Benutzer mit Gründer-Rolle löscht sein Profil.
     * Erwartet: Benutzer UND Gründer-Eintrag sind danach nicht mehr in der DB.
     */
    @Test
    void testLoeschen_GruenderProfil_BenutzerUndGruenderAusDBEntfernt() {

        // Arrange: Gründer anlegen
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("LoeschTest_Gruender");
        testDatenFabrik.erstelleUndSpeichereGruender(benutzer);
        int userId = benutzer.getUserId();

        assertTrue(benutzerRepository.existsById(userId),
                "Benutzer muss vor der Löschung existieren.");
        assertTrue(gruenderRepository.existsById(userId),
                "Gründer muss vor der Löschung existieren.");

        BenutzerDTOImpl dto = new BenutzerDTOImpl();
        dto.setUserId(userId);

        // Act — Rückgabewert wird bewusst ignoriert
        verwaltungControl.loeschen(dto);

        // Assert: DB-Zustand prüfen
        assertFalse(benutzerRepository.existsById(userId),
                "Benutzer-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");
        assertFalse(gruenderRepository.existsById(userId),
                "Gründer-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");
        assertFalse(studentRepository.existsById(userId),
                "Es darf kein Student-Eintrag für diesen User existieren.");
    }


    /**
     * TEST 3 — Löschung eines nicht existierenden Benutzers.
     *
     * Szenario: loeschen() wird mit einer userId aufgerufen die nicht in der DB ist.
     * Erwartet: Die Methode läuft ohne Absturz durch — kein Fehler, keine Exception.
     */
    @Test
    void testLoeschen_NichtExistierenderBenutzer_KeinAbsturz() {
        BenutzerDTOImpl dto = new BenutzerDTOImpl();
        dto.setUserId(Integer.MAX_VALUE); // garantiert nicht in der DB

        // Nur prüfen dass kein Absturz passiert — DB bleibt unverändert
        assertDoesNotThrow(() -> verwaltungControl.loeschen(dto),
                "loeschen() darf bei nicht existierendem User keine Exception werfen.");
    }


    /**
     * TEST 4 — Robustheitstest: loeschen() mit null aufgerufen.
     *
     * Szenario: Die View übergibt versehentlich null als currentUser.
     * Erwartet: Kein NullPointerException — die Methode fängt null sicher ab.
     */
    @Test
    void testLoeschen_NullAlsBenutzer_KeinAbsturz() {
        assertDoesNotThrow(() -> verwaltungControl.loeschen(null),
                "loeschen(null) darf keine Exception werfen.");
    }
}