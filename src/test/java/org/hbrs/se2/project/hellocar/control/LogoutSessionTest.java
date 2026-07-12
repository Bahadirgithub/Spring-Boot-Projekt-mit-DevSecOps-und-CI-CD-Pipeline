package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für Task #277 — Session-Löschung nach Logout.
 *
 * Testziel: Prüfen ob nach dem Logout die Benutzer-Sitzung ungültig ist
 * und getCurrentUser() null zurückgibt.
 *
 * Hintergrund: In Vaadin wird beim Logout die Session gecleared:
 *   VaadinSession.getCurrent().setAttribute(Globals.CURRENT_USER, null)
 * Danach ist currentUser = null. AuthorizationControl.isLoggedIn(null)
 * muss in diesem Fall false zurückgeben — der Benutzer gilt als ausgeloggt.
 *
 * Schicht: Unit-Test — kein @SpringBootTest, keine echte DB.
 * Repositories werden mit Mockito gemockt.
 */
@ExtendWith(MockitoExtension.class)
public class LogoutSessionTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GruenderRepository gruenderRepository;

    // AuthorizationControl prüft ob ein Benutzer eingeloggt ist
    @InjectMocks
    private AuthorizationControl authorizationControl;

    // Simuliert einen aktiv eingeloggten Benutzer VOR dem Logout
    private BenutzerDTO eingeloggterBenutzer;

    @BeforeEach
    void setUp() {
        // Mock-Benutzer mit userId=1 — repräsentiert eine aktive Session
        eingeloggterBenutzer = mock(BenutzerDTO.class);
        lenient().when(eingeloggterBenutzer.getUserId()).thenReturn(1);
    }


    /**
     * TEST 1 — Referenztest: Vor dem Logout ist der Benutzer eingeloggt.
     *
     * Zeigt den Ausgangszustand — danach vergleichen wir den Zustand NACH dem Logout.
     */
    @Test
    void testVorLogout_BenutzerGiltAlsEingeloggt() {
        assertTrue(authorizationControl.isLoggedIn(eingeloggterBenutzer),
                "Ein Benutzer mit aktiver Session muss als eingeloggt gelten.");
    }


    /**
     * TEST 2 — Nach Logout: Session ist ungültig, Benutzer gilt als ausgeloggt.
     *
     * Nach dem Logout setzt Vaadin currentUser = null in der Session.
     * isLoggedIn(null) muss false zurückgeben.
     */
    @Test
    void testNachLogout_SessionUngueltig_BenutzerNichtMehrEingeloggt() {
        // null repräsentiert die gecleared Vaadin-Session nach dem Logout
        BenutzerDTO ausgeloggterBenutzer = null;

        assertFalse(authorizationControl.isLoggedIn(ausgeloggterBenutzer),
                "Nach Logout muss isLoggedIn(null) false zurückgeben — Session ist ungültig.");
    }


    /**
     * TEST 3 — Nach Logout: Benutzer wird nicht mehr als Student erkannt.
     *
     * Wenn currentUser = null, darf isStudent() nicht in der DB nachschauen.
     * Das Repository darf bei null gar nicht erst aufgerufen werden.
     */
    @Test
    void testNachLogout_NichtMehrAlsStudentErkannt() {
        BenutzerDTO ausgeloggterBenutzer = null;

        assertFalse(authorizationControl.isStudent(ausgeloggterBenutzer),
                "Nach Logout darf isStudent(null) nicht true zurückgeben.");

        // Sicherheitscheck: Repository darf bei null nicht aufgerufen werden
        verify(studentRepository, never()).existsById(anyInt());
    }


    /**
     * TEST 4 — Nach Logout: Benutzer wird nicht mehr als Gründer erkannt.
     *
     * Gleiche Logik wie bei isStudent — Repository darf nicht aufgerufen werden.
     */
    @Test
    void testNachLogout_NichtMehrAlsGruenderErkannt() {
        BenutzerDTO ausgeloggterBenutzer = null;

        assertFalse(authorizationControl.isGruender(ausgeloggterBenutzer),
                "Nach Logout darf isGruender(null) nicht true zurückgeben.");

        verify(gruenderRepository, never()).existsById(anyInt());
    }
}