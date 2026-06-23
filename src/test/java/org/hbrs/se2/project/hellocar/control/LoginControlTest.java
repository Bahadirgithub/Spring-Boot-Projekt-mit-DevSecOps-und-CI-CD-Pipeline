package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-Testfälle zur Benutzeranmeldung.
 * Diese Klasse testet die Logik der LoginControl.
 * Die Datenbank wird durch @Transactional nach jedem Test automatisch zurückgesetzt.
 * Die Testdaten werden über die TestDatenFabrik erzeugt, um eine saubere
 * Trennung von der RegistrationControl zu gewährleisten.
 */
@SpringBootTest
@Transactional
public class LoginControlTest {

    @Autowired
    private LoginControl loginControl;

    // Die TestDatenFabrik wird injiziert, um Testbenutzer zu erstellen.
    // Die Kopplung zur RegistrationControl wird damit entfernt.
    @Autowired
    private TestDatenFabrik testDatenFabrik;

    /**
     * Testfall 1: Positivtest - Erfolgreicher Login mit gültigen Anmeldedaten.
     */
    @Test
    void testLoginErfolgreich() {
        // Vorbereitung: Einen Benutzer über die Fabrik erstellen.
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("LoginErfolgreich");

        // Eigentlicher Test: Login mit den Daten des erstellten Benutzers versuchen.
        assertDoesNotThrow(() -> {
            boolean result = loginControl.authenticate(benutzer.getUsername(), benutzer.getPasswort());
            assertTrue(result, "Login sollte mit korrekten Daten erfolgreich sein.");
        }, "Ein erfolgreicher Login sollte keine DatabaseUserException werfen.");
    }

    /**
     * Testfall 2: Negativtest - Login schlägt fehl bei falschem Passwort.
     */
    @Test
    void testLoginSchlaegtFehlBeiFalschemPasswort() {
        // Vorbereitung: Einen Benutzer über die Fabrik erstellen.
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("FalschesPasswort");

        // Eigentlicher Test: Login mit falschem Passwort versuchen.
        assertDoesNotThrow(() -> {
            boolean result = loginControl.authenticate(benutzer.getUsername(), "absolutFalschesPasswort");
            assertFalse(result, "Login sollte bei falschem Passwort fehlschlagen.");
        }, "Ein fehlschlagender Login sollte keine DatabaseUserException werfen.");
    }

    /**
     * Testfall 3: Negativtest - Login schlägt fehl bei unbekanntem Benutzernamen.
     */
    @Test
    void testLoginSchlaegtFehlBeiUnbekanntemBenutzernamen() {
        // Es wird kein Benutzer registriert, um einen unbekannten Benutzernamen zu simulieren.
        assertDoesNotThrow(() -> {
            boolean result = loginControl.authenticate("unbekannter_user_12345", "AnyPassword");
            assertFalse(result, "Login sollte fehlschlagen bei unbekanntem Benutzernamen.");
        }, "Ein unbekannter Benutzername sollte keine DatabaseUserException werfen.");
    }

    /**
     * Testfall 4: Negativtest - Login schlägt fehl bei leerem Benutzernamen.
     */
    @Test
    void testLoginSchlaegtFehlBeiLeeremBenutzernamen() {
        assertDoesNotThrow(() -> {
            boolean result = loginControl.authenticate("", "SecurePassword123");
            assertFalse(result, "Login sollte fehlschlagen bei leerem Benutzernamen.");
        }, "Ein leerer Benutzername sollte keine DatabaseUserException werfen.");
    }

    /**
     * Testfall 5: Negativtest - Login schlägt fehl bei leerem Passwort.
     */
    @Test
    void testLoginSchlaegtFehlBeiLeeremPasswort() {
        // Vorbereitung: Einen Benutzer über die Fabrik erstellen.
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("LeeresPasswort");

        assertDoesNotThrow(() -> {
            boolean result = loginControl.authenticate(benutzer.getUsername(), "");
            assertFalse(result, "Login sollte fehlschlagen bei leerem Passwort.");
        }, "Ein leeres Passwort sollte keine DatabaseUserException werfen.");
    }
}