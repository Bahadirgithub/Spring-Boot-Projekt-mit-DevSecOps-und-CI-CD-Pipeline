package org.hbrs.se2.project.hellocar.test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Datenbanktest für die Entity Gruendungsidee (US #43).
 *
 * Schicht: Integrationstest — testet das Zusammenspiel von
 * JPA-Repository, Hibernate und der echten PostgreSQL-Datenbank.
 *
 * @SpringBootTest  – startet den vollständigen Spring-Kontext mit DB-Verbindung
 * @Transactional   – jeder Test läuft in einer eigenen Transaktion
 * @Rollback        – alle Änderungen werden nach jedem Test automatisch
 *                   rückgängig gemacht, kein Testdatenmüll in der DB
 */
@SpringBootTest
@Transactional
@Rollback
public class DatenbankTest {

    // Spring injiziert alle drei Repositories automatisch.
    // Gruendungsidee benötigt immer einen Gründer (Fremdschlüssel),
    // und ein Gründer benötigt immer einen Benutzer — daher alle drei.

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private GruenderRepository gruenderRepository;

    @Autowired
    private GruendungsideeRepository gruendungsideeRepository;

    /**
     * Der EntityManager verwaltet den JPA First-Level-Cache (RAM-Zwischenspeicher).
     * entityManager.clear() leert diesen Cache und zwingt JPA beim nächsten
     * Lesezugriff, wirklich in die PostgreSQL-Datenbank zu schauen — und nicht
     * das bereits im Cache liegende Java-Objekt zurückzugeben.
     * Ohne clear() wäre der Round-Trip-Test kein echter Integrationstest.
     */
    @PersistenceContext
    private EntityManager entityManager;


    // -------------------------------------------------------
    // Hilfsmethode — Benutzer + Gründer anlegen
    //
    // Eine Gruendungsidee hat einen Fremdschlüssel auf gruender.user_id.
    // Benutzer muss zuerst existieren, da Gründer dessen user_id übernimmt.
    // Rückgabe: die userId, die als Fremdschlüssel in der Idee genutzt wird.
    // -------------------------------------------------------
    private int erstelleTestGruender() {

        // Benutzer anlegen — die Felder entsprechen exakt Benutzer.java
        Benutzer benutzer = new Benutzer();
        benutzer.setVorname("Test");
        benutzer.setNachname("Gründer");
        // currentTimeMillis() sorgt für einen einzigartigen Username —
        // verhindert Kollisionen wenn der Test mehrfach ausgeführt wird
        benutzer.setUsername("test_" + System.currentTimeMillis());
        benutzer.setPasswort("test123");
        benutzer.setEmail("test@startmeup.de");
        benutzer.setGeburtstag(LocalDate.of(1995, 3, 15));
        benutzer.setTelefonnummer("0228123456");

        // saveAndFlush schreibt sofort in die DB.
        // Benutzer.userId hat @GeneratedValue(IDENTITY) → DB vergibt die ID,
        // JPA liest sie zurück. Danach gibt getUserId() die echte DB-ID zurück.
        benutzerRepository.saveAndFlush(benutzer);

        // Gründer anlegen — Felder aus Gruender.java
        // userId = Fremdschlüssel → muss identisch zu benutzer.getUserId() sein
        Gruender gruender = new Gruender();
        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname("StartMeUp GmbH");
        gruender.setStandort("Bonn");
        gruenderRepository.saveAndFlush(gruender);

        return benutzer.getUserId();
    }


    // -------------------------------------------------------
    // TEST 1 — DB-Verbindungstest
    // -------------------------------------------------------

    /**
     * Prüft, ob die Anwendung beim Start erfolgreich eine Verbindung
     * zur PostgreSQL-Datenbank aufbaut.
     *
     * Testziel: VPN aktiv, Zugangsdaten in application.properties korrekt,
     * kein Netzwerkproblem zwischen App und dumbo.inf.h-brs.de.
     *
     * Methode: Das Repository wird injiziert und count() aufgerufen —
     * eine lesende, nicht-verändernde Abfrage.
     * Bei Verbindungsproblemen würde hier eine PSQLException geworfen.
     */
    @Test
    void testDatenbankVerbindung() {

        // assertNotNull: Spring muss das Repository erfolgreich injiziert haben
        assertNotNull(
                benutzerRepository,
                "BenutzerRepository ist null — Dependency Injection fehlgeschlagen."
        );

        // assertDoesNotThrow: count() darf keine Exception werfen.
        // Schlägt fehl wenn DB nicht erreichbar (kein VPN, falsche Credentials).
        assertDoesNotThrow(
                () -> benutzerRepository.count(),
                "DB-Verbindung fehlgeschlagen — VPN und application.properties prüfen."
        );
    }


    // -------------------------------------------------------
    // TEST 2 — Round-Trip-Test (C-R-Ass-D)
    // -------------------------------------------------------

    /**
     * Vollständiger Round-Trip-Test für Gruendungsidee (Create, Read, Assert, Delete).
     *
     * Testziel: Alle Pflichtfelder der Gruendungsidee werden korrekt in
     * PostgreSQL gespeichert und byte-identisch wieder ausgelesen.
     * Die Gruendungsidee ist die zentrale Entity der Plattform —
     * funktioniert sie nicht, können weder Gründer Ideen veröffentlichen
     * noch Studenten sich bewerben.
     *
     * Technische Besonderheit beim Delete:
     * Gruendungsidee.ideeId hat kein @GeneratedValue. Java setzt den
     * Standardwert 0. Spring Data's delete(entity) prüft intern isNew() —
     * bei ideeId=0 hält es das Objekt fälschlicherweise für "neu" und
     * bricht kommentarlos ab. Deshalb nutzen wir deleteAllInBatch(), das
     * direkt SQL "DELETE WHERE idee_id IN (...)" generiert und isNew()
     * komplett umgeht.
     */
    @Test
    void testGruendungsideeRoundTrip() {

        // Voraussetzung: Gründer anlegen (Fremdschlüssel-Abhängigkeit)
        int gruenderUserId = erstelleTestGruender();


        // -------------------------------------------
        // C — Create: Gruendungsidee speichern
        // Felder aus Gruendungsidee.java:
        //   gruenderUserId, beschreibung, position, anforderungen, arbeitsbedingungen
        // -------------------------------------------

        Gruendungsidee idee = new Gruendungsidee();
        idee.setGruenderUserId(gruenderUserId);
        idee.setBeschreibung("KI-gestützte Lernplattform für Studenten");
        idee.setPosition("Full-Stack Entwickler");
        idee.setAnforderungen("Java, Spring Boot, React");
        idee.setArbeitsbedingungen("Remote, flexible Arbeitszeiten");

        gruendungsideeRepository.saveAndFlush(idee);


        // -------------------------------------------
        // R — Read: Idee aus der echten DB laden
        //
        // entityManager.clear() leert den JPA-Cache.
        // Ohne clear() würde findByGruenderUserId() das Java-Objekt
        // aus dem RAM zurückgeben (= dieselbe Instanz wie oben).
        // assertNotSame() würde dann fälschlicherweise fehlschlagen.
        // -------------------------------------------

        entityManager.clear();

        // findByGruenderUserId() kommt aus GruendungsideeRepository —
        // sucht über den fachlichen Fremdschlüssel, nicht über die technische ID
        List<Gruendungsidee> gefundeneIdeen =
                gruendungsideeRepository.findByGruenderUserId(gruenderUserId);

        assertFalse(
                gefundeneIdeen.isEmpty(),
                "Keine Gruendungsidee für gruenderUserId=" + gruenderUserId + " gefunden."
        );

        Gruendungsidee geleseneIdee = gefundeneIdeen.get(0);


        // -------------------------------------------
        // Ass — Assert: Feldweise Gleichheitsprüfung
        //
        // assertEquals: inhaltlich gleich (Wert aus DB stimmt überein)
        // assertNotSame: verschiedene Java-Objekte (echter DB-Zugriff hat stattgefunden)
        // -------------------------------------------

        assertEquals(gruenderUserId,                             geleseneIdee.getGruenderUserId(),    "gruenderUserId stimmt nicht.");
        assertEquals("KI-gestützte Lernplattform für Studenten", geleseneIdee.getBeschreibung(),      "beschreibung stimmt nicht.");
        assertEquals("Full-Stack Entwickler",                    geleseneIdee.getPosition(),           "position stimmt nicht.");
        assertEquals("Java, Spring Boot, React",                 geleseneIdee.getAnforderungen(),      "anforderungen stimmen nicht.");
        assertEquals("Remote, flexible Arbeitszeiten",           geleseneIdee.getArbeitsbedingungen(), "arbeitsbedingungen stimmen nicht.");

        // Zwei verschiedene Objekte — beweist: Cache war leer, DB wurde wirklich gelesen
        assertNotSame(idee, geleseneIdee, "Gleiche Instanz — entityManager.clear() hat nicht gewirkt.");


        // -------------------------------------------
        // D — Delete: Idee löschen und verifizieren
        //
        // deleteAllInBatch() generiert: DELETE WHERE idee_id IN (0)
        // Das umgeht den isNew()-Bug bei ideeId=0.
        // -------------------------------------------

        gruendungsideeRepository.deleteAllInBatch(gefundeneIdeen);
        entityManager.clear();

        assertFalse(
                gruendungsideeRepository.existsByGruenderUserId(gruenderUserId),
                "Gruendungsidee noch vorhanden — deleteAllInBatch() hat nicht funktioniert."
        );
    }
}