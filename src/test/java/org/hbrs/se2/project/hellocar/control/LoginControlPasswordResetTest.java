package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-Testfälle für die Passwort-Zurücksetzung in der LoginControl.
 *
 * Diese Klasse testet die Methoden checkResetCredentials() und
 * updatePassword() der LoginControl.
 *
 * Die Datenbank wird durch @Transactional nach jedem Test automatisch
 * zurückgesetzt. Testdaten werden über die TestDatenFabrik erzeugt.
 *
 * Für Methoden, die auf die Vaadin-Session zugreifen, werden UI und
 * Session mit Mockito gemockt.
 */
@SpringBootTest
@Transactional
public class LoginControlPasswordResetTest {

    @Autowired
    private LoginControl loginControl;

    @Autowired
    private TestDatenFabrik testDatenFabrik;

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private PasswordResetControl passwordResetControl;

    // Mock für Vaadin UI und Session zur Prüfung von Session-Attributen
    private UI mockUI;
    private VaadinSession mockSession;


    @BeforeEach
    void setup() {
        // Vor jedem Test: gemockte UI und Session vorbereiten
        mockUI = mock(UI.class);
        mockSession = mock(VaadinSession.class);
        when(mockUI.getSession()).thenReturn(mockSession);
        UI.setCurrent(mockUI);
    }

    /**

     * Testfall 1: Richtige Kombination aus Benutzername und E-Mail.
     *
     * Erwartung:
     * - checkResetCredentials() gibt true zurück.
     * - Die Benutzer-ID wird in der Session gespeichert.
     */
    @Test
    void testCheckResetCredentials_Erfolgreich() {
        // Vorbereitung: Benutzer mit bekannter Email erstellen
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("ResetUser");

        // Eigentlicher Test: checkResetCredentials mit korrekten Daten aufrufen
        assertDoesNotThrow(() -> {
            boolean result = loginControl.checkResetCredentials(benutzer.getUsername(), benutzer.getEmail());
            // checkResetCredentials sollte true zurückgeben, wenn Benutzer existiert
            assertTrue(result, "checkResetCredentials sollte true zurückgeben für existierenden Benutzer.");
            // Prüfe, dass die User-ID in der Session gespeichert wurde
            verify(mockSession).setAttribute("reset_user_id", benutzer.getUserId());
        });
    }

    /**

     * Testfall 2: Falsche Kombination aus Benutzername und E-Mail.
     *
     * Erwartung:
     * - checkResetCredentials() gibt false zurück.
     * - Es wird kein Session-Attribut gesetzt.
     */
    @Test
    void testCheckResetCredentials_BenutzerNichtGefunden() {
        //Testbenutzer erzeugen
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("ResetFalsch");
        // Eigentlicher Test: checkResetCredentials mit falscher Email aufrufen
        assertDoesNotThrow(() -> {
            boolean result = loginControl.checkResetCredentials(benutzer.getUsername(), "falsche@email.com");
            // checkResetCredentials sollte false zurückgeben, wenn Benutzer nicht existiert
            assertFalse(result, "checkResetCredentials sollte false zurückgeben für nicht existierenden Benutzer.");
            // Prüfe, dass KEIN Session-Attribut gesetzt wurde (never bedeutet: gar nicht aufgerufen)
            verify(mockSession, never()).setAttribute(eq("reset_user_id"), any());
        });
    }

    /**
     * Testfall 3: Passwort wird nach erfolgreicher Verifizierung geändert.
     *
     * Erwartung:
     * - Das Passwort wird in der Datenbank aktualisiert.
     * - Das Session-Attribut wird anschließend entfernt.
     */
    @Test
    void testUpdatePassword_Erfolgreich() {
        // Vorbereitung 1: Benutzer erstellen
        Benutzer benutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("PasswordReset");
        String altesPasswort = benutzer.getPasswort();

        // Vorbereitung 2: checkResetCredentials aufrufen (setzt Session-ID)
        assertDoesNotThrow(() -> loginControl.checkResetCredentials(benutzer.getUsername(), benutzer.getEmail()));

        // Vorbereitung 3: Mock so konfigurieren, dass Session-ID zurückgegeben wird
        when(mockSession.getAttribute("reset_user_id")).thenReturn(benutzer.getUserId());

        // Eigentlicher Test: updatePassword aufrufen
        assertDoesNotThrow(() -> passwordResetControl.updatePassword("NuesesPasswort123"));

        // Nachprüfung: Benutzer aus der DB neu laden und prüfen, ob Passwort geändert wurde
        Benutzer aktualiserterBenutzer = benutzerRepository.findById(benutzer.getUserId()).orElse(null);
        assertNotNull(aktualiserterBenutzer, "Benutzer sollte in der Datenbank existieren.");
        assertEquals("NuesesPasswort123", aktualiserterBenutzer.getPasswort(), "Passwort sollte aktualisiert sein.");
        assertNotEquals(altesPasswort, aktualiserterBenutzer.getPasswort(), "Neues Passwort sollte sich vom alten unterscheiden.");

        // Prüfe, dass Session-Attribut gelöscht wurde (auf null gesetzt)
        verify(mockSession).setAttribute("reset_user_id", null);
    }

    /**
     * Testfall 4: updatePassword() ohne aktiven Reset-Prozess.
     *
     * Erwartung:
     * - Es wird eine DatabaseUserException ausgelöst,
     *   wenn keine Benutzer-ID in der Session vorhanden ist.
     */
    @Test
    void testUpdatePassword_OhneVerifizierung_ThrowsException() {
        // Vorbereitung: Mock so konfigurieren, dass keine Session-ID vorhanden ist
        when(mockSession.getAttribute("reset_user_id")).thenReturn(null);

        // Eigentlicher Test: updatePassword aufrufen sollte DatabaseUserException werfen
        DatabaseUserException exception = assertThrows(
                DatabaseUserException.class,
                () -> passwordResetControl.updatePassword("NeuesPasswort"),
                "updatePassword sollte DatabaseUserException werfen, wenn keine Session-ID vorhanden ist."
        );

        // Zusätzliche Prüfung: Exception-Meldung sollte aussagekräftig sein
        // DatabaseUserException speichert Nachricht in getReason(), daher dort prüfen
        assertTrue(exception.getMessage().contains("Passwort-Zurücksetzungsprozess"),
                "Exception-Meldung sollte auf fehlenden Prozess hinweisen.");
    }
}
