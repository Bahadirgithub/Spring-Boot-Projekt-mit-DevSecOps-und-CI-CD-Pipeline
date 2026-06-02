package org.hbrs.se2.project.hellocar.test;

import org.hbrs.se2.project.hellocar.entities.*;
import org.hbrs.se2.project.hellocar.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // -------------------------------------------------------
    // Hilfsmethode: Benutzer erstellen (vermeidet Code-Dopplung)
    // -------------------------------------------------------
    private Benutzer erstelleBenutzer(String vorname, String nachname,
                                      String username, String email,
                                      String telefon, LocalDate geburtstag) {
        Benutzer benutzer = new Benutzer();
        benutzer.setVorname(vorname);
        benutzer.setNachname(nachname);
        benutzer.setGeburtstag(geburtstag);
        benutzer.setUsername(username);
        benutzer.setPasswort("test123");
        benutzer.setEmail(email);
        benutzer.setTelefonnummer(telefon);
        return benutzerRepository.saveAndFlush(benutzer);
    }

    // -------------------------------------------------------
    // Test 1: Benutzer speichern und Login prüfen
    // -------------------------------------------------------
    @Test
    void testBenutzerSpeichernUndLogin() {
        String username = "testBenutzerJUnit";

        Benutzer benutzer = erstelleBenutzer(
                "Mark", "Lark",
                username,
                "test@benutzer.de",
                "123456",
                LocalDate.of(2000, 1, 1)
        );

        assertTrue(benutzerRepository.existsByUsername(username));
        assertTrue(benutzerRepository.findByUsernameAndPasswort(username, "test123").isPresent());
    }

    // -------------------------------------------------------
    // Test 2: Student speichern
    // -------------------------------------------------------
    @Test
    void testStudentSpeichern() {
        Benutzer benutzer = erstelleBenutzer(
                "Student", "Test",
                "studentJUnit",
                "student@test.de",
                "111111",
                LocalDate.of(2001, 2, 2)
        );

        Student student = new Student();
        student.setUserId(benutzer.getUserId());
        student.setStudentId("s" + benutzer.getUserId());
        student.setUniversitaet("H-BRS");
        student.setSemester(4);
        student.setSkills("Java, SQL");
        student.setAbschluss("Bachelor");
        studentRepository.saveAndFlush(student);

        assertTrue(studentRepository.existsById(benutzer.getUserId()));
    }

    // -------------------------------------------------------
    // Test 3: Gründer speichern
    // -------------------------------------------------------
    @Test
    void testGruenderSpeichern() {
        Benutzer benutzer = erstelleBenutzer(
                "Gruender", "Test",
                "gruenderJUnit",
                "gruender@test.de",
                "222222",
                LocalDate.of(1999, 3, 3)
        );

        Gruender gruender = new Gruender();
        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname("JUnit Startup GmbH");
        gruender.setStandort("Bonn");
        gruenderRepository.saveAndFlush(gruender);

        assertTrue(gruenderRepository.existsById(benutzer.getUserId()));
    }

    // -------------------------------------------------------
    // Test 4: Gründungsidee speichern
    // -------------------------------------------------------
    @Test
    void testGruendungsideeSpeichern() {
        Benutzer benutzer = erstelleBenutzer(
                "Idee", "Gruender",
                "ideeGruenderJUnit",
                "idee@test.de",
                "333333",
                LocalDate.of(1998, 4, 4)
        );

        Gruender gruender = new Gruender();
        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname("Ideen GmbH");
        gruender.setStandort("Köln");
        gruenderRepository.saveAndFlush(gruender);

        Gruendungsidee idee = new Gruendungsidee();
        idee.setGruenderUserId(benutzer.getUserId());
        idee.setBeschreibung("Testidee aus JUnit");
        idee.setPosition("Java Entwickler");
        idee.setAnforderungen("Java und SQL");
        idee.setArbeitsbedingungen("Remote möglich");
        gruendungsideeRepository.saveAndFlush(idee);

        // ID wird von der DB vergeben – wir prüfen über den Gründer
        assertTrue(gruendungsideeRepository.existsByGruenderUserId(benutzer.getUserId()));
    }

    // -------------------------------------------------------
    // Test 5: Bewerbung und Bewertung speichern
    // -------------------------------------------------------
    @Test
    void testBewerbungUndBewertungSpeichern() {
        // Student anlegen
        Benutzer studentBenutzer = erstelleBenutzer(
                "Bewerber", "Student",
                "bewerberJUnit",
                "bewerber@test.de",
                "444444",
                LocalDate.of(2002, 5, 5)
        );

        Student student = new Student();
        student.setUserId(studentBenutzer.getUserId());
        student.setStudentId("s" + studentBenutzer.getUserId());
        student.setUniversitaet("H-BRS");
        student.setSemester(3);
        student.setSkills("Java");
        student.setAbschluss("Bachelor");
        studentRepository.saveAndFlush(student);

        // Gründer anlegen
        Benutzer gruenderBenutzer = erstelleBenutzer(
                "Ideengeber", "Gruender",
                "ideengeberJUnit",
                "ideengeber@test.de",
                "555555",
                LocalDate.of(1995, 6, 6)
        );

        Gruender gruender = new Gruender();
        gruender.setUserId(gruenderBenutzer.getUserId());
        gruender.setGruenderId("g" + gruenderBenutzer.getUserId());
        gruender.setUnternehmensname("Bewertung Startup GmbH");
        gruender.setStandort("Bonn");
        gruenderRepository.saveAndFlush(gruender);

        // Idee anlegen
        Gruendungsidee idee = new Gruendungsidee();
        idee.setGruenderUserId(gruenderBenutzer.getUserId());
        idee.setBeschreibung("Testidee für Bewerbung und Bewertung");
        idee.setPosition("Backend Entwickler");
        idee.setAnforderungen("Spring Boot");
        idee.setArbeitsbedingungen("Hybrid");
        gruendungsideeRepository.saveAndFlush(idee);
        int ideeId = idee.getIdeeId(); // ID von DB holen

        // Bewerbung anlegen
        Bewerbung bewerbung = new Bewerbung();
        bewerbung.setStudentUserId(studentBenutzer.getUserId());
        bewerbung.setIdeeId(ideeId);
        bewerbung.setBewerbungsdatum(LocalDate.now());
        bewerbung.setStatus("Eingegangen");
        bewerbung.setAnschreibenDateiname("anschreiben.pdf");
        bewerbung.setAnschreibenDateipfad("/uploads/anschreiben.pdf");
        bewerbung.setLebenslaufDateiname("lebenslauf.pdf");
        bewerbung.setLebenslaufDateipfad("/uploads/lebenslauf.pdf");
        bewerbungRepository.saveAndFlush(bewerbung);

        // Bewertung anlegen
        Bewertung bewertung = new Bewertung();
        bewertung.setStudentUserId(studentBenutzer.getUserId());
        bewertung.setIdeeId(ideeId);
        bewertung.setBewertung(5);
        bewertung.setKommentar("Sehr gute Idee");
        bewertung.setZeitstempel(LocalDateTime.now());
        bewertungRepository.saveAndFlush(bewertung);

        assertTrue(bewerbungRepository.existsById(bewerbung.getBewerbungId()));
        assertTrue(bewertungRepository.existsById(bewertung.getBewertungId()));
    }
}