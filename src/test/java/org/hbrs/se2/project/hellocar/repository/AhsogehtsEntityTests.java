package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.*;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional  // Nach jedem Test wird die DB automatisch zurückgesetzt
@Rollback       // Stellt sicher, dass keine Daten dauerhaft gespeichert werden
public class AhsogehtsEntityTests {

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GruenderRepository gruenderRepository;

    @Autowired
    private GruendungsideeRepository gruendungsideeRepository;

    @Autowired
    private BewerbungRepository bewerbungRepository;

    @Autowired
    private BewertungRepository bewertungRepository;

    // Injizieren der neuen TestDatenFabrik
    @Autowired
    private TestDatenFabrik testDatenFabrik;

    // -------------------------------------------------------
    // Test 1: Benutzer speichern und Login prüfen
    // -------------------------------------------------------
    @Test
    void testBenutzerSpeichernUndLogin() {
        // Nutzung der Fabrik, um einen Standard-Benutzer anzulegen
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("LoginTest");

        // Wir testen nur noch, ob das Speichern und Abfragen (für den Login) funktioniert
        assertTrue(benutzerRepository.existsByUsername(benutzer.getUsername()));
        assertTrue(benutzerRepository.findByUsernameAndPasswort(benutzer.getUsername(), benutzer.getPasswort()).isPresent());
    }

    // -------------------------------------------------------
    // Test 2: Student speichern
    // -------------------------------------------------------
    @Test
    void testStudentSpeichern() {
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("StudentTest");
        
        // Die Fabrik übernimmt auch die Erstellung des Studenten
        Student student = testDatenFabrik.erstelleUndSpeichereStudent(benutzer);

        assertTrue(studentRepository.existsById(student.getUserId()));
    }

    // -------------------------------------------------------
    // Test 3: Gründer speichern
    // -------------------------------------------------------
    @Test
    void testGruenderSpeichern() {
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("GruenderTest");
        
        // Die Fabrik übernimmt auch die Erstellung des Gründers
        Gruender gruender = testDatenFabrik.erstelleUndSpeichereGruender(benutzer);

        assertTrue(gruenderRepository.existsById(gruender.getUserId()));
    }

    // -------------------------------------------------------
    // Test 4: Gründungsidee speichern
    // -------------------------------------------------------
    @Test
    void testGruendungsideeSpeichern() {
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("IdeeGruenderTest");
        Gruender gruender = testDatenFabrik.erstelleUndSpeichereGruender(benutzer);
        
        // Die Fabrik erstellt die Idee für den Gründer
        testDatenFabrik.erstelleUndSpeichereGruendungsidee(gruender);

        assertTrue(gruendungsideeRepository.existsByGruenderUserId(gruender.getUserId()));
    }

    // -------------------------------------------------------
    // Test 5: Bewerbung und Bewertung speichern
    // -------------------------------------------------------
    @Test
    void testBewerbungUndBewertungSpeichern() {
        // 1. Aufbau der Abhängigkeiten über die Fabrik
        Benutzer studentBenutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("BewerberTest");
        Student student = testDatenFabrik.erstelleUndSpeichereStudent(studentBenutzer);

        Benutzer gruenderBenutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("IdeengeberTest");
        Gruender gruender = testDatenFabrik.erstelleUndSpeichereGruender(gruenderBenutzer);

        Gruendungsidee idee = testDatenFabrik.erstelleUndSpeichereGruendungsidee(gruender);

        // 2. Erstellen von Bewerbung und Bewertung
        Bewerbung bewerbung = testDatenFabrik.erstelleUndSpeichereBewerbung(student, idee);
        Bewertung bewertung = testDatenFabrik.erstelleUndSpeichereBewertung(student, idee);

        // 3. Assertions
        assertTrue(bewerbungRepository.existsById(bewerbung.getBewerbungId()));
        assertTrue(bewertungRepository.existsById(bewertung.getBewertungId()));
    }
}