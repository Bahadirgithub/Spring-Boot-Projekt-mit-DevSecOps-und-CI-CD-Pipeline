package org.hbrs.se2.project.hellocar.test;

import org.hbrs.se2.project.hellocar.control.LoginControl;
import org.hbrs.se2.project.hellocar.control.RegistrationControl;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException; // Korrigierter Import
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-Testfälle zur Benutzeranmeldung.
 * Diese Klasse testet die Logik der LoginControl.
 * Die Datenbank wird durch @Transactional nach jedem Test automatisch zurückgesetzt.
 */
@SpringBootTest
@Transactional
public class LoginControlTest {

    @Autowired
    private LoginControl loginControl;

    @Autowired
    private RegistrationControl registrationControl; // Zum Anlegen von Testbenutzern

    // @Autowired
    // private BenutzerRepository benutzerRepository; // Entfernt, da ungenutzt

    /**
     * Testfall 1: Positivtest - Erfolgreicher Login mit gültigen Anmeldedaten.
     */
    @Test
    void testLoginErfolgreich() {
        // Vorbereitung: Einen Benutzer registrieren
        registrationControl.registriereStudent(
                "Test", "User", LocalDate.of(2000, 1, 1),
                "testuser", "test@example.com", "SecurePassword123", "Test Uni", 1
        );

        // Eigentlicher Test: Login versuchen
        try {
            boolean result = loginControl.authentificate("testuser", "SecurePassword123");
            assertTrue(result, "Login sollte erfolgreich sein.");
        } catch (DatabaseUserException e) {
            fail("Es sollte keine DatabaseUserException geworfen werden: " + e.getMessage());
        }
    }

    /**
     * Testfall 2: Negativtest - Login schlägt fehl bei falschem Passwort.
     */
    @Test
    void testLoginSchlaegtFehlBeiFalschemPasswort() {
        // Vorbereitung: Einen Benutzer registrieren
        registrationControl.registriereStudent(
                "Test", "User", LocalDate.of(2000, 1, 1),
                "testuser2", "test2@example.com", "SecurePassword123", "Test Uni", 1
        );

        // Eigentlicher Test: Login mit falschem Passwort versuchen
        try {
            boolean result = loginControl.authentificate("testuser2", "WrongPassword");
            assertFalse(result, "Login sollte fehlschlagen bei falschem Passwort.");
        } catch (DatabaseUserException e) {
            fail("Es sollte keine DatabaseUserException geworfen werden: " + e.getMessage());
        }
    }

    /**
     * Testfall 3: Negativtest - Login schlägt fehl bei unbekanntem Benutzernamen.
     */
    @Test
    void testLoginSchlaegtFehlBeiUnbekanntemBenutzernamen() {
        // Es wird kein Benutzer registriert, um einen unbekannten Benutzernamen zu simulieren.
        try {
            boolean result = loginControl.authentificate("unknownuser", "AnyPassword");
            assertFalse(result, "Login sollte fehlschlagen bei unbekanntem Benutzernamen.");
        } catch (DatabaseUserException e) {
            fail("Es sollte keine DatabaseUserException geworfen werden: " + e.getMessage());
        }
    }

    /**
     * Testfall 4: Negativtest - Login schlägt fehl bei leerem Benutzernamen.
     */
    @Test
    void testLoginSchlaegtFehlBeiLeeremBenutzernamen() {
        try {
            boolean result = loginControl.authentificate("", "SecurePassword123");
            assertFalse(result, "Login sollte fehlschlagen bei leerem Benutzernamen.");
        } catch (DatabaseUserException e) {
            fail("Es sollte keine DatabaseUserException geworfen werden: " + e.getMessage());
        }
    }

    /**
     * Testfall 5: Negativtest - Login schlägt fehl bei leerem Passwort.
     */
    @Test
    void testLoginSchlaegtFehlBeiLeeremPasswort() {
        // Vorbereitung: Einen Benutzer registrieren
        registrationControl.registriereStudent(
                "Test", "User", LocalDate.of(2000, 1, 1),
                "testuser3", "test3@example.com", "SecurePassword123", "Test Uni", 1
        );

        try {
            boolean result = loginControl.authentificate("testuser3", "");
            assertFalse(result, "Login sollte fehlschlagen bei leerem Passwort.");
        } catch (DatabaseUserException e) {
            fail("Es sollte keine DatabaseUserException geworfen werden: " + e.getMessage());
        }
    }
}
