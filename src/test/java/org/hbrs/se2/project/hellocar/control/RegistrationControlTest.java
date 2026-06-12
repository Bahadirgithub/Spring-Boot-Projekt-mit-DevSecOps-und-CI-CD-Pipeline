package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-Testfälle zur Benutzerregistrierung.
 * Diese Klasse testet die Validierungslogik der RegistrationControl.
 * Die Datenbank wird durch @Transactional nach jedem Test automatisch zurückgesetzt.
 */
@SpringBootTest
@Transactional
public class RegistrationControlTest {

    @Autowired
    private RegistrationControl registrationControl;

    @Autowired
    private TestDatenFabrik testDatenFabrik;

    private Benutzer standardBenutzer;

    /**
     * Diese Methode wird vor jedem einzelnen Test ausgeführt.
     * Sie legt über die TestDatenFabrik einen Standard-Benutzer an, um die Tests
     * für bereits existierende Benutzernamen oder E-Mails sauberer zu gestalten,
     * ohne die zu testende RegistrationControl für das Setup zu verwenden.
     */
    @BeforeEach
    void setUp() {
        // Vorbereitung: Einen Standard-Benutzer direkt in der DB anlegen.
        // Username: "testuser_RegistrationSetup", Email: "test_RegistrationSetup@mail.de"
        standardBenutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("RegistrationSetup");
    }

    /**
     * Testfall 1: Positivtest - Alle Registrierungsdaten sind valide und der Username ist frei.
     */
    @Test
    void testRegistrierungErfolgreich() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Mustermann", LocalDate.of(1999, 1, 1),
                "startup_founder_1", "test@mail.de", "Passwort123", "Uni Bonn", 1
        );
        assertTrue(result, "Registrierung sollte erfolgreich sein.");
    }

    /**
     * Testfall 2: Negativtest - Username existiert bereits in der Datenbank (Kollisionsprüfung).
     */
    @Test
    void testRegistrierungSchlaegtFehlWennUsernameVergeben() {
        // Eigentlicher Test: Versuch, den gleichen Namen nochmal zu nutzen.
        boolean result = registrationControl.registriereStudent(
                "Anna", "Schmidt", LocalDate.now(), standardBenutzer.getUsername(), "andere_mail@mail.de", "Passwort123", "Uni Köln", 2
        );
        assertFalse(result, "Registrierung sollte fehlschlagen, da '" + standardBenutzer.getUsername() + "' bereits existiert.");
    }

    /**
     * Testfall 3: Positivtest - E-Mail existiert bereits, aber der Username ist frei.
     */
    @Test
    void testRegistrierungErfolgreichTrotzDoppelterEmail() {
        // Eigentlicher Test: Anderer Username, aber GLEICHE E-Mail.
        boolean result = registrationControl.registriereStudent(
                "Anna", "Schmidt", LocalDate.now(), "neuer_user_2", standardBenutzer.getEmail(), "SicheresPasswort99", "Uni Köln", 2
        );
        assertTrue(result, "Registrierung sollte erfolgreich sein, da doppelte E-Mails laut Anforderung erlaubt sind.");
    }

    /**
     * Testfall 4: Negativtest - Das Passwort unterschreitet die Mindestlänge von 8 Zeichen.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennPasswortZuKurz() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "neuer_user_3", "user3@mail.de", "123", "Uni Bonn", 1
        );
        assertFalse(result, "Registrierung sollte blockiert werden, wenn das Passwort kürzer als 8 Zeichen ist.");
    }

    /**
     * Testfall 5: Negativtest - Die E-Mail-Adresse besitzt ein ungültiges Format.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennEmailUngueltig() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "neuer_user_4", "testmail", "Passwort123", "Uni Bonn", 1
        );
        assertFalse(result, "Registrierung sollte bei ungültigem E-Mail-Format fehlschlagen.");
    }

    /**
     * Testfall 6: Negativtest - Das Passwortfeld wurde leer übergeben.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennPasswortLeer() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "neuer_user_5", "user5@mail.de", "", "Uni Bonn", 1
        );
        assertFalse(result, "Registrierung sollte bei leerem Passwort fehlschlagen.");
    }

    /**
     * Testfall 7: Negativtest - Der Benutzername wurde leer übergeben.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennUsernameLeer() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "", "user6@mail.de", "Passwort123", "Uni Bonn", 1
        );
        assertFalse(result, "Registrierung sollte bei leerem Benutzernamen fehlschlagen.");
    }
}