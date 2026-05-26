package org.hbrs.se2.project.hellocar.test;

import org.hbrs.se2.project.hellocar.entities.*;
import org.hbrs.se2.project.hellocar.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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

    @Test
    void testBenutzerSpeichernUndLogin() {
        String username = "testBenutzerJUnit";

        if (!benutzerRepository.existsByUsername(username)) {
            Benutzer benutzer = new Benutzer();

            benutzer.setVorname("Test");
            benutzer.setNachname("Benutzer");
            benutzer.setGeburtstag(LocalDate.of(2000, 1, 1));
            benutzer.setUsername(username);
            benutzer.setPasswort("test123");
            benutzer.setEmail("test@benutzer.de");
            benutzer.setTelefonnummer("123456");

            benutzerRepository.saveAndFlush(benutzer);
        }

        assertTrue(benutzerRepository.existsByUsername(username));
        assertTrue(benutzerRepository.findByUsernameAndPasswort(username, "test123").isPresent());
    }

    @Test
    void testStudentSpeichern() {
        Benutzer benutzer = new Benutzer();

        benutzer.setVorname("Student");
        benutzer.setNachname("Test");
        benutzer.setGeburtstag(LocalDate.of(2001, 2, 2));
        benutzer.setUsername("studentJUnit" + System.currentTimeMillis());
        benutzer.setPasswort("test123");
        benutzer.setEmail("student@test.de");
        benutzer.setTelefonnummer("111111");

        benutzerRepository.saveAndFlush(benutzer);

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

    @Test
    void testGruenderSpeichern() {
        Benutzer benutzer = new Benutzer();

        benutzer.setVorname("Gruender");
        benutzer.setNachname("Test");
        benutzer.setGeburtstag(LocalDate.of(1999, 3, 3));
        benutzer.setUsername("gruenderJUnit" + System.currentTimeMillis());
        benutzer.setPasswort("test123");
        benutzer.setEmail("gruender@test.de");
        benutzer.setTelefonnummer("222222");

        benutzerRepository.saveAndFlush(benutzer);

        Gruender gruender = new Gruender();

        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname("JUnit Startup GmbH");
        gruender.setStandort("Bonn");

        gruenderRepository.saveAndFlush(gruender);

        assertTrue(gruenderRepository.existsById(benutzer.getUserId()));
    }

    @Test
    void testGruendungsideeSpeichern() {
        int ideeId = (int) (System.currentTimeMillis() % 1000000);

        Benutzer benutzer = new Benutzer();

        benutzer.setVorname("Idee");
        benutzer.setNachname("Gruender");
        benutzer.setGeburtstag(LocalDate.of(1998, 4, 4));
        benutzer.setUsername("ideeGruenderJUnit" + System.currentTimeMillis());
        benutzer.setPasswort("test123");
        benutzer.setEmail("idee@test.de");
        benutzer.setTelefonnummer("333333");

        benutzerRepository.saveAndFlush(benutzer);

        Gruender gruender = new Gruender();

        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname("Ideen GmbH");
        gruender.setStandort("Köln");

        gruenderRepository.saveAndFlush(gruender);

        Gruendungsidee idee = new Gruendungsidee();

        idee.setIdeeId(ideeId);
        idee.setGruenderUserId(benutzer.getUserId());
        idee.setBeschreibung("Testidee aus JUnit");
        idee.setPosition("Java Entwickler");
        idee.setAnforderungen("Java und SQL");
        idee.setArbeitsbedingungen("Remote möglich");

        gruendungsideeRepository.saveAndFlush(idee);

        assertTrue(gruendungsideeRepository.existsById(ideeId));
    }

    @Test
    void testBewerbungUndBewertungSpeichern() {
        int ideeId = (int) (System.currentTimeMillis() % 1000000);
        int bewerbungId = ideeId + 1;
        int bewertungId = ideeId + 2;

        Benutzer studentBenutzer = new Benutzer();

        studentBenutzer.setVorname("Bewerber");
        studentBenutzer.setNachname("Student");
        studentBenutzer.setGeburtstag(LocalDate.of(2002, 5, 5));
        studentBenutzer.setUsername("bewerberJUnit" + System.currentTimeMillis());
        studentBenutzer.setPasswort("test123");
        studentBenutzer.setEmail("bewerber@test.de");
        studentBenutzer.setTelefonnummer("444444");

        benutzerRepository.saveAndFlush(studentBenutzer);

        Student student = new Student();

        student.setUserId(studentBenutzer.getUserId());
        student.setStudentId("s" + studentBenutzer.getUserId());
        student.setUniversitaet("H-BRS");
        student.setSemester(3);
        student.setSkills("Java");
        student.setAbschluss("Bachelor");

        studentRepository.saveAndFlush(student);

        Benutzer gruenderBenutzer = new Benutzer();

        gruenderBenutzer.setVorname("Ideengeber");
        gruenderBenutzer.setNachname("Gruender");
        gruenderBenutzer.setGeburtstag(LocalDate.of(1995, 6, 6));
        gruenderBenutzer.setUsername("ideengeberJUnit" + System.currentTimeMillis());
        gruenderBenutzer.setPasswort("test123");
        gruenderBenutzer.setEmail("ideengeber@test.de");
        gruenderBenutzer.setTelefonnummer("555555");

        benutzerRepository.saveAndFlush(gruenderBenutzer);

        Gruender gruender = new Gruender();

        gruender.setUserId(gruenderBenutzer.getUserId());
        gruender.setGruenderId("g" + gruenderBenutzer.getUserId());
        gruender.setUnternehmensname("Bewertung Startup GmbH");
        gruender.setStandort("Bonn");

        gruenderRepository.saveAndFlush(gruender);

        Gruendungsidee idee = new Gruendungsidee();

        idee.setIdeeId(ideeId);
        idee.setGruenderUserId(gruenderBenutzer.getUserId());
        idee.setBeschreibung("Testidee für Bewerbung und Bewertung");
        idee.setPosition("Backend Entwickler");
        idee.setAnforderungen("Spring Boot");
        idee.setArbeitsbedingungen("Hybrid");

        gruendungsideeRepository.saveAndFlush(idee);

        Bewerbung bewerbung = new Bewerbung();

        bewerbung.setBewerbungId(bewerbungId);
        bewerbung.setStudentUserId(studentBenutzer.getUserId());
        bewerbung.setIdeeId(ideeId);
        bewerbung.setBewerbungsdatum(LocalDate.now());
        bewerbung.setStatus("Eingegangen");
        bewerbung.setAnschreibenDateiname("anschreiben.pdf");
        bewerbung.setAnschreibenDateipfad("/uploads/anschreiben.pdf");
        bewerbung.setLebenslaufDateiname("lebenslauf.pdf");
        bewerbung.setLebenslaufDateipfad("/uploads/lebenslauf.pdf");

        bewerbungRepository.saveAndFlush(bewerbung);

        Bewertung bewertung = new Bewertung();

        bewertung.setBewertungId(bewertungId);
        bewertung.setStudentUserId(studentBenutzer.getUserId());
        bewertung.setIdeeId(ideeId);
        bewertung.setBewertung(5);
        bewertung.setKommentar("Sehr gute Idee");
        bewertung.setZeitstempel(LocalDateTime.now());

        bewertungRepository.saveAndFlush(bewertung);

        assertTrue(bewerbungRepository.existsById(bewerbungId));
        assertTrue(bewertungRepository.existsById(bewertungId));
    }
}