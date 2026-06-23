package org.hbrs.se2.project.hellocar.util;

import org.hbrs.se2.project.hellocar.entities.*;
import org.hbrs.se2.project.hellocar.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test Data Builder / Object Mother für die Erzeugung von Testdaten.
 * Diese Fabrik-Klasse zentralisiert die Erstellung von JPA-Entitäten für unsere Tests.
 * Das reduziert Code-Duplizierung und erleichtert die Wartbarkeit, da bei Änderungen
 * an den Entitäten (z.B. neue Pflichtfelder) nur diese eine Klasse angepasst werden muss.
 * Alle Objekte werden direkt per JPA in der Datenbank persistiert.
 */
@Component
public class TestDatenFabrik {

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

    /**
     * Erstellt einen Standard-Benutzer mit validen Daten und speichert ihn in der DB.
     * 
     * @param suffix Ein String (z.B. "1", "Student", oder UUID), der an den Benutzernamen
     *               angehängt wird, um Unique-Constraints in der Datenbank zu umgehen.
     *               (Hinweis: Die E-Mail darf in diesem System mehrfach vorkommen,
     *               wir hängen den Suffix trotzdem der Übersichtlichkeit halber an).
     * @return Den persistierten Benutzer inklusive generierter ID.
     */
    public Benutzer erstelleUndSpeichereBenutzer(String suffix) {
        Benutzer benutzer = new Benutzer();
        benutzer.setVorname("Test");
        benutzer.setNachname("User_" + suffix);
        benutzer.setUsername("testuser_" + suffix);
        benutzer.setEmail("test_" + suffix + "@mail.de");
        benutzer.setPasswort("SicheresPasswort123");
        benutzer.setTelefonnummer("0123456789" + suffix);
        benutzer.setGeburtstag(LocalDate.of(2000, 1, 1));
        
        // saveAndFlush schreibt sofort in die DB, sodass wir die echte ID zurückbekommen.
        return benutzerRepository.saveAndFlush(benutzer);
    }

    /**
     * Erstellt einen Student, der mit einem übergebenen Benutzer verknüpft ist.
     * 
     * @param benutzer Der Benutzer, dem die Rolle Student zugewiesen wird.
     * @return Den persistierten Studenten.
     */
    public Student erstelleUndSpeichereStudent(Benutzer benutzer) {
        Student student = new Student();
        // Fremdschlüssel auf den Benutzer setzen
        student.setUserId(benutzer.getUserId());
        // Geschäfts-ID generieren
        student.setStudentId("s" + benutzer.getUserId());
        student.setUniversitaet("H-BRS");
        student.setSemester(4);
        student.setSkills(java.util.List.of("Java", "SQL"));
        student.setAbschluss("Bachelor");
        
        return studentRepository.saveAndFlush(student);
    }

    /**
     * Erstellt einen Gründer, der mit einem übergebenen Benutzer verknüpft ist.
     * 
     * @param benutzer Der Benutzer, dem die Rolle Gründer zugewiesen wird.
     * @return Den persistierten Gründer.
     */
    public Gruender erstelleUndSpeichereGruender(Benutzer benutzer) {
        Gruender gruender = new Gruender();
        // Fremdschlüssel auf den Benutzer setzen
        gruender.setUserId(benutzer.getUserId());
        // Geschäfts-ID generieren
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname("Startup_" + benutzer.getUserId() + " GmbH");
        gruender.setStandort("Bonn");
        
        return gruenderRepository.saveAndFlush(gruender);
    }

    /**
     * Erstellt eine Gründungsidee, die einem spezifischen Gründer zugeordnet ist.
     * 
     * @param gruender Der Gründer (bzw. dessen User-ID), dem die Idee gehört.
     * @return Die persistierte Gründungsidee.
     */
    public Gruendungsidee erstelleUndSpeichereGruendungsidee(Gruender gruender) {
        Gruendungsidee idee = new Gruendungsidee();
        // Fremdschlüssel auf den Gründer (via userId)
        idee.setGruenderUserId(gruender.getUserId());
        idee.setBeschreibung("Eine innovative Test-Idee aus der Fabrik");
        idee.setPosition("Java Fullstack Entwickler");
        idee.setAnforderungen("Clean Code, JPA, Vaadin");
        idee.setArbeitsbedingungen("100% Remote, Flexible Arbeitszeiten");
        
        return gruendungsideeRepository.saveAndFlush(idee);
    }

    /**
     * Erstellt eine Bewerbung für einen bestimmten Studenten auf eine spezifische Idee.
     * 
     * @param student Der Student, der sich bewirbt.
     * @param idee Die Gründungsidee, auf die sich beworben wird.
     * @return Die persistierte Bewerbung.
     */
    public Bewerbung erstelleUndSpeichereBewerbung(Student student, Gruendungsidee idee) {
        Bewerbung bewerbung = new Bewerbung();
        bewerbung.setStudentUserId(student.getUserId());
        bewerbung.setIdeeId(idee.getIdeeId());
        bewerbung.setBewerbungsdatum(LocalDate.now());
        bewerbung.setStatus("Eingegangen");
        bewerbung.setAnschreibenDateiname("anschreiben.pdf");
        bewerbung.setAnschreibenDateipfad("/uploads/anschreiben.pdf");
        bewerbung.setLebenslaufDateiname("lebenslauf.pdf");
        bewerbung.setLebenslaufDateipfad("/uploads/lebenslauf.pdf");
        
        return bewerbungRepository.saveAndFlush(bewerbung);
    }

    /**
     * Erstellt eine Bewertung für eine Idee durch einen Studenten.
     * 
     * @param student Der Student, der die Bewertung abgibt.
     * @param idee Die Idee, die bewertet wird.
     * @return Die persistierte Bewertung.
     */
    public Bewertung erstelleUndSpeichereBewertung(Student student, Gruendungsidee idee) {
        Bewertung bewertung = new Bewertung();
        bewertung.setStudentUserId(student.getUserId());
        bewertung.setIdeeId(idee.getIdeeId());
        bewertung.setBewertung(5);
        bewertung.setKommentar("Klingt nach einem großartigen Projekt!");
        bewertung.setZeitstempel(LocalDateTime.now());
        
        // Korrektur: Speichert das 'bewertung'-Objekt, nicht 'bewerbung'
        return bewertungRepository.saveAndFlush(bewertung);
    }
}