package org.hbrs.se2.project.hellocar.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstests für die Entity Gruendungsidee (US #43).
 * Testet das Zusammenspiel von JPA-Repository, Hibernate und PostgreSQL.
 * @Rollback macht alle DB-Änderungen nach jedem Test automatisch rückgängig.
 */
@SpringBootTest
@Transactional
@Rollback
public class DatenbankTest {

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private GruendungsideeRepository gruendungsideeRepository;

    /**
     * Neu: TestDatenFabrik ersetzt die alte private Hilfsmethode erstelleTestGruender().
     * Vorteil: Testdaten-Erstellung ist zentralisiert — wenn sich eine Entity ändert,
     * muss nur die Fabrik angepasst werden, nicht jeder Test einzeln.
     */
    @Autowired
    private TestDatenFabrik testDatenFabrik;

    /**
     * Leert den JPA First-Level-Cache.
     * Notwendig damit findById() wirklich aus der DB liest
     * und nicht das bereits im RAM liegende Objekt zurückgibt.
     */
    @PersistenceContext
    private EntityManager entityManager;


    /**
     * TEST 1 — DB-Verbindungstest.
     * Prüft ob Spring das Repository injiziert hat und
     * ob eine einfache DB-Abfrage ohne Exception durchläuft.
     * Schlägt fehl wenn kein VPN aktiv oder application.properties falsch.
     */
    @Test
    void testDatenbankVerbindung() {
        assertNotNull(benutzerRepository,
                "BenutzerRepository ist null — Dependency Injection fehlgeschlagen.");

        assertDoesNotThrow(() -> benutzerRepository.count(),
                "DB-Verbindung fehlgeschlagen — VPN und application.properties prüfen.");
    }


    /**
     * TEST 2 — Round-Trip-Test (C-R-Ass-D) für Gruendungsidee.
     *
     * Änderungen gegenüber der alten Version:
     *
     * 1. TestDatenFabrik statt eigener Hilfsmethode für die Testvorbereitung.
     *
     * 2. Gruendungsidee hat jetzt @GeneratedValue(IDENTITY) — die DB vergibt
     *    die ideeId automatisch. Nach saveAndFlush() ist ideeId > 0.
     *    Dadurch können wir jetzt findById() und deleteById() benutzen —
     *    der alte deleteAllInBatch()-Workaround ist nicht mehr nötig.
     *
     * 3. Der Update-Schritt (U) wurde entfernt, da aktualisiereBeschreibung()
     *    nicht mehr im GruendungsideeRepository vorhanden ist.
     */
    @Test
    void testGruendungsideeRoundTrip() {

        // Vorbereitung: Fabrik erstellt Benutzer + Gründer in der DB
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("RoundTripTest");
        Gruender gruender = testDatenFabrik.erstelleUndSpeichereGruender(benutzer);
        int gruenderUserId = gruender.getUserId();


        // -------------------------------------------
        // C — Create: Gruendungsidee speichern
        // -------------------------------------------
        Gruendungsidee idee = new Gruendungsidee();
        idee.setGruenderUserId(gruenderUserId);
        idee.setBeschreibung("KI-gestützte Lernplattform für Studenten");
        idee.setPosition("Full-Stack Entwickler");
        idee.setAnforderungen("Java, Spring Boot, React");
        idee.setArbeitsbedingungen("Remote, flexible Arbeitszeiten");
        gruendungsideeRepository.saveAndFlush(idee);

        // NEU: Dank @GeneratedValue(IDENTITY) setzt die DB jetzt die ID.
        // ideeId ist nach saveAndFlush() korrekt befüllt — nicht mehr 0!
        int gespeicherteId = idee.getIdeeId();
        assertTrue(gespeicherteId > 0,
                "ideeId ist 0 — @GeneratedValue(IDENTITY) hat nicht funktioniert.");


        // -------------------------------------------
        // R — Read: Idee aus der echten DB laden
        // Cache leeren → erzwingt echten DB-Zugriff
        // -------------------------------------------
        entityManager.clear();

        // NEU: findById() statt findByGruenderUserId() —
        // das ist jetzt möglich weil die ID korrekt gesetzt ist.
        Optional<Gruendungsidee> wrapper =
                gruendungsideeRepository.findById(gespeicherteId);

        assertTrue(wrapper.isPresent(),
                "Gruendungsidee mit ID " + gespeicherteId + " nicht in DB gefunden.");

        Gruendungsidee geleseneIdee = wrapper.get();


        // -------------------------------------------
        // Ass — Assert: Feldweise Gleichheitsprüfung
        // -------------------------------------------
        assertEquals(gruenderUserId,
                geleseneIdee.getGruenderUserId(),    "gruenderUserId stimmt nicht.");
        assertEquals("KI-gestützte Lernplattform für Studenten",
                geleseneIdee.getBeschreibung(),      "beschreibung stimmt nicht.");
        assertEquals("Full-Stack Entwickler",
                geleseneIdee.getPosition(),          "position stimmt nicht.");
        assertEquals("Java, Spring Boot, React",
                geleseneIdee.getAnforderungen(),     "anforderungen stimmen nicht.");
        assertEquals("Remote, flexible Arbeitszeiten",
                geleseneIdee.getArbeitsbedingungen(),"arbeitsbedingungen stimmen nicht.");

        // Zwei verschiedene Objekte → Cache war wirklich leer
        assertNotSame(idee, geleseneIdee,
                "Gleiche Instanz — entityManager.clear() hat nicht gewirkt.");


        // -------------------------------------------
        // D — Delete: Löschen und verifizieren
        //
        // NEU: deleteById() statt deleteAllInBatch().
        // Funktioniert jetzt korrekt weil gespeicherteId > 0 ist.
        // -------------------------------------------
        gruendungsideeRepository.deleteById(gespeicherteId);
        entityManager.flush();

        assertFalse(gruendungsideeRepository.existsById(gespeicherteId),
                "Idee noch vorhanden — deleteById() hat nicht funktioniert.");
    }
}