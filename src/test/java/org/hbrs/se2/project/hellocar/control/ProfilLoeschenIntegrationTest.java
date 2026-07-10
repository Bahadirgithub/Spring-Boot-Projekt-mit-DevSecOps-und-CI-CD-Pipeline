package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.builder.GruenderBuilder;
import org.hbrs.se2.project.hellocar.builder.StudentBuilder;
import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstest für Task #27 — "Profil löschen" (DSGVO-konform).
 * Testziel: Prüfen ob loeschen() alle personenbezogenen Daten eines Benutzers
 * vollständig aus der PostgreSQL-Datenbank entfernt (Benutzer + Rolle).
 * Schicht: Integrationstest — testet VerwaltungControl mit echter DB.
 *  @ Transactional stellt sicher, dass nach jedem Test keine Testdaten übrig bleiben.
 */
@SpringBootTest
@Transactional
@DisplayName("Integrationstests für die Profil-Lösch-Funktion (VerwaltungControl)")
public class ProfilLoeschenIntegrationTest {

    // VerwaltungControl enthält die loeschen()-Methode — das ist, was wir testen
    @Autowired
    private VerwaltungControl verwaltungControl;

    // Repositories um Testdaten zu speichern und den DB-Zustand zu prüfen
    @Autowired
    private BenutzerRepository benutzerRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private GruenderRepository gruenderRepository;


    /**
     * TEST 1 — vollständige Löschung eines Studenten-Profils.
     * Szenario: Ein Benutzer mit Student-Rolle löscht sein Profil.
     * Erwartet: Benutzer UND Student-Eintrag sind danach nicht mehr in der DB.
     */
    @Test
    @DisplayName("Ein Studenten-Profil wird vollständig gelöscht.")
    void testLoeschen_StudentProfil_BenutzerUndStudentAusDBEntfernt() {

        // ── Arrange: Student über Builder erstellen und explizit speichern ──
        StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent().build();
        Benutzer benutzer = benutzerRepository.save(testStudent.getBenutzer());
        Student student = testStudent.getStudent();
        student.setUserId(benutzer.getUserId()); // Verknüpfung setzen
        studentRepository.save(student);

        int userId = benutzer.getUserId();

        // Vorher-Prüfung: Beide Einträge müssen in der DB existieren
        assertTrue(benutzerRepository.existsById(userId),
                "Benutzer muss vor der Löschung in der DB existieren.");
        assertTrue(studentRepository.existsById(userId),
                "Student muss vor der Löschung in der DB existieren.");

        // BenutzerDTO bauen — so übergibt die View den eingeloggten User
        BenutzerDTOImpl dto = new BenutzerDTOImpl();
        dto.setUserId(userId);

        // ── Act: Profil löschen ──
        boolean result = verwaltungControl.loeschen(dto);

        // ── Assert: Rückgabewert und DB-Zustand prüfen ──
        assertTrue(result,
                "loeschen() muss true zurückgeben wenn Löschung erfolgreich war.");

        assertFalse(benutzerRepository.existsById(userId),
                "Benutzer-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");
        assertFalse(studentRepository.existsById(userId),
                "Student-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");

        // Sicherheitscheck: Gründer-Eintrag darf nicht existiert haben
        assertFalse(gruenderRepository.existsById(userId),
                "Es darf kein Gründer-Eintrag für diesen User existieren.");
    }


    /**
     * TEST 2 — vollständige Löschung eines Gründer-Profils.
     * Szenario: Ein Benutzer mit Gründer-Rolle löscht sein Profil.
     * Erwartet: Benutzer UND Gründer-Eintrag sind danach nicht mehr in der DB.
     */
    @Test
    @DisplayName("Ein Gründer-Profil wird vollständig gelöscht.")
    void testLoeschen_GruenderProfil_BenutzerUndGruenderAusDBEntfernt() {

        // ── Arrange: Gründer über Builder erstellen und explizit speichern ──
        GruenderBuilder.TestGruender testGruender = GruenderBuilder.aGruender().build();
        Benutzer benutzer = benutzerRepository.save(testGruender.getBenutzer());
        Gruender gruender = testGruender.getGruender();
        gruender.setUserId(benutzer.getUserId()); // Verknüpfung setzen
        gruenderRepository.save(gruender);

        int userId = benutzer.getUserId();

        // Vorher-Prüfung
        assertTrue(benutzerRepository.existsById(userId),
                "Benutzer muss vor der Löschung existieren.");
        assertTrue(gruenderRepository.existsById(userId),
                "Gründer muss vor der Löschung existieren.");

        BenutzerDTOImpl dto = new BenutzerDTOImpl();
        dto.setUserId(userId);

        // ── Act ──
        boolean result = verwaltungControl.loeschen(dto);

        // ── Assert ──
        assertTrue(result, "loeschen() muss true zurückgeben.");

        assertFalse(benutzerRepository.existsById(userId),
                "Benutzer-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");
        assertFalse(gruenderRepository.existsById(userId),
                "Gründer-Eintrag muss nach Löschung weg sein — DSGVO-Verletzung!");

        // Sicherheitscheck: Student-Eintrag darf nicht existiert haben
        assertFalse(studentRepository.existsById(userId),
                "Es darf kein Student-Eintrag für diesen User existieren.");
    }


    /**
     * TEST 3 — Löschung eines nicht existierenden Benutzers.
     * Szenario: loeschen() wird mit einem userId aufgerufen der nicht in der DB ist.
     * Erwartet: Methode gibt false zurück — kein Absturz, kein Fehler.
     */
    @Test
    @DisplayName("Löschversuch eines nicht-existierenden Benutzers gibt false zurück.")
    void testLoeschen_NichtExistierenderBenutzer_GibtFalseZurueck() {

        // Eine userId die garantiert nicht in der DB existiert
        BenutzerDTOImpl dto = new BenutzerDTOImpl();
        dto.setUserId(Integer.MAX_VALUE); // sehr unwahrscheinliche ID

        boolean result = verwaltungControl.loeschen(dto);

        assertFalse(result, "loeschen() muss false zurückgeben wenn Benutzer nicht existiert.");
    }


    /**
     * TEST 4 — Robustheitstest: loeschen() mit null aufgerufen.
     * Szenario: Die View übergibt versehentlich null als currentUser.
     * Erwartet: Kein NullPointerException — Methode gibt sicher false zurück.
     */
    @Test
    @DisplayName("Löschversuch mit 'null' als Benutzer gibt false zurück.")
    void testLoeschen_NullAlsBenutzer_GibtFalseZurueckOhneAbsturz() {
        assertDoesNotThrow(
                () -> {
                    boolean result = verwaltungControl.loeschen(null);
                    assertFalse(result, "loeschen(null) muss false zurückgeben.");
                },
                "loeschen(null) darf keine Exception werfen."
        );
    }
}