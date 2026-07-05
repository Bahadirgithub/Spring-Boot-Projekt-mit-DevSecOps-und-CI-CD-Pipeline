package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetControlTest {

    @Mock
    private BenutzerRepository benutzerRepository;

    private LoginControl loginControl;
    private PasswordResetControl testControl;
    private Integer simulatedUserId = null;

    @BeforeEach
    void setup() {
        this.loginControl = new LoginControl(benutzerRepository);
        // Wir nutzen eine anonyme Unterklasse, um die Session-Methoden zu simulieren
        testControl = new PasswordResetControl(loginControl, benutzerRepository) {
            @Override
            protected Object getSessionAttribute(String name) { return simulatedUserId; }
            @Override
            protected void setSessionAttribute(String name, Object value) { simulatedUserId = (Integer) value; }
        };
        simulatedUserId = null;
    }

    @Test
    void testUpdatePassword_Erfolgreich() throws DatabaseUserException {
        // GIVEN: Ein Benutzer existiert
        int userId = 42;
        simulatedUserId = userId;
        Benutzer mockBenutzer = new Benutzer();
        when(benutzerRepository.findById(userId)).thenReturn(Optional.of(mockBenutzer));

        // WHEN: Passwort aktualisiert wird
        testControl.updatePassword("NeuesSicheresPasswort!");

        // THEN: Repository wurde aufgerufen, Passwort gesetzt und Session geleert
        verify(benutzerRepository).save(mockBenutzer);
        assertNull(simulatedUserId, "Session sollte nach erfolgreichem Reset leer sein.");
    }

    @Test
    void testUpdatePassword_KeinAktiverProzess_WirftException() {
        // GIVEN: Keine UserId in der Session
        simulatedUserId = null;

        // WHEN/THEN:
        assertThrows(DatabaseUserException.class, () ->
                testControl.updatePassword("Passwort123"));

        verifyNoInteractions(benutzerRepository);
    }

    @Test
    void testValidatePasswords_Valid() {
        assertDoesNotThrow(() -> testControl.validatePasswords("LangGenug123", "LangGenug123"));
    }

    @Test
    void testValidatePasswords_ZuKurz_WirftException() {
        assertThrows(IllegalArgumentException.class, () -> testControl.validatePasswords("kurz", "kurz"));
    }

    @Test
    void testValidatePasswords_NichtIdentisch_WirftException() {
        assertThrows(IllegalArgumentException.class, () -> testControl.validatePasswords("Passwort123", "Passwort456"));
    }
}
