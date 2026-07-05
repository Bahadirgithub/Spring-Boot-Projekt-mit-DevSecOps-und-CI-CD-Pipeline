package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.builder.BenutzerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class BenutzerRepositoryTest {

    @Autowired
    private BenutzerRepository benutzerRepository;

    private Benutzer testBenutzer;

    @BeforeEach
    void setUp() {
        // Builder Pattern für saubere Test-Daten
        testBenutzer = BenutzerBuilder.aBenutzer()
                .withUsername("testuser")
                .withEmail("test@example.de")
                .withVorname("Test")
                .withNachname("User")
                .build();
    }

    @Test
    void testRoundTrippingSpeichernUndLaden() {
        // Act - Speichern
        Benutzer gespeicherterBenutzer = benutzerRepository.save(testBenutzer);

        // Act - Laden
        Optional<Benutzer> geladenerBenutzer = benutzerRepository.findById(gespeicherterBenutzer.getUserId());

        // Assert - Round Trip erfolgreich?
        assertThat(geladenerBenutzer).isPresent();
        assertThat(geladenerBenutzer.get().getUsername()).isEqualTo(testBenutzer.getUsername());
        assertThat(geladenerBenutzer.get().getEmail()).isEqualTo(testBenutzer.getEmail());
        assertThat(geladenerBenutzer.get().getVorname()).isEqualTo(testBenutzer.getVorname());
        assertThat(geladenerBenutzer.get().getNachname()).isEqualTo(testBenutzer.getNachname());
    }

    /**
     * Neuer Benutzer wird gespeichert
     */
    @Test
    void testBenutzerSpeichern() {
        // Act
        Benutzer gespeicherterBenutzer = benutzerRepository.save(testBenutzer);

        // Assert
        assertThat(gespeicherterBenutzer.getUserId()).isGreaterThan(0);
        assertThat(gespeicherterBenutzer.getUsername()).isEqualTo("testuser");
    }

    /**
     * Benutzer nach ID finden
     */
    @Test
    void testBenutzerNachIdFinden() {
        // Arrange
        Benutzer gespeicherterBenutzer = benutzerRepository.save(testBenutzer);

        // Act
        Optional<Benutzer> gefunden = benutzerRepository.findById(gespeicherterBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getUserId()).isEqualTo(gespeicherterBenutzer.getUserId());
    }

    /**
     * Benutzer nach Username finden
     */
    @Test
    void testBenutzerNachUsernameFinden() {
        // Arrange
        benutzerRepository.save(testBenutzer);

        // Act
        Optional<Benutzer> gefunden = benutzerRepository.findByUsername("testuser");

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getUsername()).isEqualTo("testuser");
    }

    /**
     * Benutzer nach Username und Passwort finden
     */
    @Test
    void testBenutzerNachUsernameUndPasswortFinden() {
        // Arrange
        benutzerRepository.save(testBenutzer);

        // Act
        Optional<Benutzer> gefunden = benutzerRepository.findByUsernameAndPasswort("testuser", "SicheresPasswort123!");

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getUsername()).isEqualTo("testuser");
    }

    /**
     * Benutzer nach Username und Email finden
     */
   // @Test
  /*  void testBenutzerNachUsernameUndEmailFinden() {
        // Arrange
        benutzerRepository.save(testBenutzer);

        // Act
        Benutzer gefunden = benutzerRepository.findBenutzerByUsernameAndEmail("testuser", "test@example.de");

        // Assert
        //assertDoesNotThrow(gefunden);
        assertThat(gefunden.getUsername()).isEqualTo("testuser");
        assertThat(gefunden.getEmail()).isEqualTo("test@example.de");
    }*/ // TODO Test anpassen

    /**
     * Prüfen ob Username existiert
     */
    @Test
    void testExistsByUsername() {
        // Arrange
        benutzerRepository.save(testBenutzer);

        // Act & Assert - Existiert
        assertThat(benutzerRepository.existsByUsername("testuser")).isTrue();

        // Act & Assert - Existiert nicht
        assertThat(benutzerRepository.existsByUsername("nichtvorhanden")).isFalse();
    }

    /**
     * Benutzer aktualisieren
     */
    @Test
    void testBenutzerAktualisieren() {
        // Arrange
        Benutzer gespeicherterBenutzer = benutzerRepository.save(testBenutzer);

        // Act - Daten ändern
        gespeicherterBenutzer.setVorname("GeänderterVorname");
        gespeicherterBenutzer.setEmail("neu@example.de");
        Benutzer aktualisierterBenutzer = benutzerRepository.save(gespeicherterBenutzer);

        // Assert - Round Trip: Änderungen persistiert?
        Optional<Benutzer> geladen = benutzerRepository.findById(aktualisierterBenutzer.getUserId());
        assertThat(geladen).isPresent();
        assertThat(geladen.get().getVorname()).isEqualTo("GeänderterVorname");
        assertThat(geladen.get().getEmail()).isEqualTo("neu@example.de");
    }

    /**
     * Benutzer löschen
     */
    @Test
    void testBenutzerLoeschen() {
        // Arrange
        Benutzer gespeicherterBenutzer = benutzerRepository.save(testBenutzer);

        // Act - Löschen
        benutzerRepository.deleteById(gespeicherterBenutzer.getUserId());

        // Assert - Nicht mehr vorhanden
        Optional<Benutzer> gefunden = benutzerRepository.findById(gespeicherterBenutzer.getUserId());
        assertThat(gefunden).isEmpty();
    }

    /**
     * Anzahl der Benutzer zählen
     */
    @Test
    void testAnzahlBenutzerZaehlen() {
        // Arrange
        benutzerRepository.save(testBenutzer);
        benutzerRepository.save(
                BenutzerBuilder.aBenutzer()
                        .withUsername("user2")
                        .withEmail("user2@test.de")
                        .build()
        );

        // Act
        long anzahl = benutzerRepository.count();

        // Assert
        assertThat(anzahl).isEqualTo(2);
    }

    /**
     * Nicht vorhandener Benutzer
     */
    @Test
    void testNichtVorhandenerBenutzer() {
        // Act
        Optional<Benutzer> gefunden = benutzerRepository.findById(99999);

        // Assert
        assertThat(gefunden).isEmpty();
    }

    /**
     * Benutzer mit Sonderzeichen
     */
    @Test
    void testBenutzerMitSonderzeichen() {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer()
                .withVorname("Müller")
                .withNachname("Österreich")
                .withUsername("müller_123")
                .build();

        // Act
        Benutzer gespeichert = benutzerRepository.save(benutzer);
        Optional<Benutzer> geladen = benutzerRepository.findById(gespeichert.getUserId());

        // Assert
        assertThat(geladen).isPresent();
        assertThat(geladen.get().getVorname()).isEqualTo("Müller");
        assertThat(geladen.get().getNachname()).isEqualTo("Österreich");
    }

    /**
     * Benutzer mit minimalem Alter (18 Jahre)
     */
    @Test
    void testBenutzerMitMinimalemAlter() {
        // Arrange
        LocalDate vor18Jahren = LocalDate.now().minusYears(18);
        Benutzer benutzer = BenutzerBuilder.aBenutzer()
                .withGeburtstag(vor18Jahren)
                .build();

        // Act
        Benutzer gespeichert = benutzerRepository.save(benutzer);
        Optional<Benutzer> geladen = benutzerRepository.findById(gespeichert.getUserId());

        // Assert
        assertThat(geladen).isPresent();
        assertThat(geladen.get().getGeburtstag()).isEqualTo(vor18Jahren);
    }
}
