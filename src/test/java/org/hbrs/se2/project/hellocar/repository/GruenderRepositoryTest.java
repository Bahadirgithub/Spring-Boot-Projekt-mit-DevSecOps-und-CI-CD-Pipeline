package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.builder.GruenderBuilder;
import org.hbrs.se2.project.hellocar.builder.BenutzerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GruenderRepositoryTest {

    @Autowired
    private GruenderRepository gruenderRepository;

    @Autowired
    private BenutzerRepository benutzerRepository;

    private Gruender testGruender;
    private Benutzer testBenutzer;

    @BeforeEach
    void setUp() {
        // Zuerst Benutzer erstellen (Fremdschlüssel-Beziehung)
        testBenutzer = BenutzerBuilder.aBenutzer()
                .withUsername("gruenderuser")
                .withEmail("gruender@test.de")
                .build();
        benutzerRepository.save(testBenutzer);

        // Gründer mit Builder erstellen
        testGruender = GruenderBuilder.aGruender()
                .withBenutzer(BenutzerBuilder.aBenutzer()
                        .withUsername("gruenderuser")
                        .withEmail("gruender@test.de"))
                .withUnternehmensname("TestStartup GmbH")
                .withStandort("Sankt Augustin")
                .build()
                .getGruender();

        // UserId setzen für DB-Beziehung
        testGruender.setUserId(testBenutzer.getUserId());
    }

    @Test
    void testRoundTrippingGruenderSpeichernUndLaden() {
        // Act - Speichern
        Gruender gespeicherterGruender = gruenderRepository.save(testGruender);

        // Act - Laden
        Optional<Gruender> geladenerGruender = gruenderRepository.findById(gespeicherterGruender.getUserId());

        // Assert - Round Trip erfolgreich?
        assertThat(geladenerGruender).isPresent();
        assertThat(geladenerGruender.get().getUserId()).isEqualTo(testGruender.getUserId());
        assertThat(geladenerGruender.get().getUnternehmensname()).isEqualTo("TestStartup GmbH");
        assertThat(geladenerGruender.get().getStandort()).isEqualTo("Sankt Augustin");
    }

    /**
     * Gründer wird gespeichert
     */
    @Test
    void testGruenderSpeichern() {
        // Erzwinge die Zuweisung VOR dem Speichern nochmal explizit
        testGruender.setUserId(testBenutzer.getUserId());

        // Act
        Gruender gespeicherterGruender = gruenderRepository.save(testGruender);

        // Assert
        assertThat(gespeicherterGruender).as("Repository hat null zurückgegeben - das darf nicht passieren!").isNotNull();
        assertThat(gespeicherterGruender.getUserId()).isEqualTo(testBenutzer.getUserId());
    }

    /**
     * Gründer nach ID finden
     */
    @Test
    void testGruenderNachIdFinden() {
        // Arrange
        gruenderRepository.save(testGruender);

        // Act
        Optional<Gruender> gefunden = gruenderRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getUnternehmensname()).isEqualTo("TestStartup GmbH");
    }

    /**
     * Prüfen ob Gründer existiert
     */
    @Test
    void testExistsById() {
        // Arrange
        gruenderRepository.save(testGruender);

        // Act & Assert - Existiert
        assertThat(gruenderRepository.existsById(testBenutzer.getUserId())).isTrue();

        // Act & Assert - Existiert nicht
        assertThat(gruenderRepository.existsById(99999)).isFalse();
    }

    /**
     * Gründer aktualisieren
     */
    @Test
    void testGruenderAktualisieren() {
        // Arrange
        Gruender gespeicherterGruender = gruenderRepository.save(testGruender);

        // Act - Daten ändern
        gespeicherterGruender.setUnternehmensname("NeuesStartup AG");
        gespeicherterGruender.setStandort("Köln");
        Gruender aktualisierterGruender = gruenderRepository.save(gespeicherterGruender);

        // Assert - Round Trip: Änderungen persistiert?
        Optional<Gruender> geladen = gruenderRepository.findById(aktualisierterGruender.getUserId());
        assertThat(geladen).isPresent();
        assertThat(geladen.get().getUnternehmensname()).isEqualTo("NeuesStartup AG");
        assertThat(geladen.get().getStandort()).isEqualTo("Köln");
    }

    /**
     * Gründer löschen
     */
    @Test
    void testGruenderLoeschen() {
        // Arrange
        gruenderRepository.save(testGruender);

        // Act - Löschen
        gruenderRepository.deleteById(testBenutzer.getUserId());

        // Assert - Nicht mehr vorhanden
        Optional<Gruender> gefunden = gruenderRepository.findById(testBenutzer.getUserId());
        assertThat(gefunden).isEmpty();
    }

    /**
     * Gründer mit langem Unternehmensnamen
     */
    @Test
    void testGruenderMitLangemUnternehmensnamen() {
        // Arrange
        testGruender.setUnternehmensname("Sehr Langes Unternehmen Name International GmbH & Co. KG");
        gruenderRepository.save(testGruender);

        // Act
        Optional<Gruender> gefunden = gruenderRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getUnternehmensname()).hasSizeGreaterThan(50);
    }

    /**
     * Gründer mit Sonderzeichen im Standort
     */
    @Test
    void testGruenderMitSonderzeichenImStandort() {
        // Arrange
        testGruender.setStandort("München-Riem, Deutschland");
        gruenderRepository.save(testGruender);

        // Act
        Optional<Gruender> gefunden = gruenderRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getStandort()).contains("München");
    }

    /**
     * Gründer mit internationalem Standort
     */
    @Test
    void testGruenderMitInternationalemStandort() {
        // Arrange
        testGruender.setStandort("New York, USA");
        gruenderRepository.save(testGruender);

        // Act
        Optional<Gruender> gefunden = gruenderRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getStandort()).contains("New York");
    }
}