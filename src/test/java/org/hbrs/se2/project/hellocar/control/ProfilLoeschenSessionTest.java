package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.util.Globals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für Task #27 — Zugriffssperre nach Profil-Löschung.
 *
 * Testziel: Prüfen ob nach der Löschung eines Profils der Benutzer keine
 * geschützten Seiten mehr aufrufen kann und als "nicht eingeloggt" gilt.
 *
 * Hintergrund: Nach loeschen() wird in der View die Session gecleared
 * (currentUser = null). AuthorizationControl prüft dann ob null-User
 * noch Zugriff auf geschützte Seiten hat — das muss überall false sein.
 *
 * Schicht: Unit-Test — kein @SpringBootTest, keine DB.
 * Repositories werden mit Mockito gemockt → schnell und isoliert.
 */
@ExtendWith(MockitoExtension.class)
public class ProfilLoeschenSessionTest {

    // Repositories werden gemockt — keine echte DB nötig
    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GruenderRepository gruenderRepository;

    // AuthorizationControl ist das was wir testen
    @InjectMocks
    private AuthorizationControl authorizationControl;

    // Simuliert einen eingeloggten Benutzer VOR der Löschung
    private BenutzerDTO eingeloggterBenutzer;

    @BeforeEach
    void setUp() {
        // Mock-Benutzer mit userId=42 — repräsentiert einen aktiven eingeloggten User
        eingeloggterBenutzer = mock(BenutzerDTO.class);
        lenient().when(eingeloggterBenutzer.getUserId()).thenReturn(42);
    }


    /**
     * TEST 1 — Referenztest: Vor der Löschung ist der Benutzer eingeloggt.
     *
     * Dieser Test zeigt den Ausgangszustand — danach vergleichen wir
     * mit dem Zustand NACH der Löschung.
     */
    @Test
    void testVorLoeschung_BenutzerGiltAlsEingeloggt() {
        assertTrue(authorizationControl.isLoggedIn(eingeloggterBenutzer),
                "Ein angemeldeter Benutzer muss als eingeloggt gelten.");
    }


    /**
     * TEST 2 — Nach Löschung: Benutzer gilt nicht mehr als eingeloggt.
     *
     * Nach erfolgreicher Löschung wird die Session gecleared:
     * currentUser = null. isLoggedIn(null) muss false zurückgeben.
     */
    @Test
    void testNachLoeschung_BenutzerGiltNichtMehrAlsEingeloggt() {
        // null simuliert die gecleared Session nach der Profilöschung
        BenutzerDTO geloeschterBenutzer = null;

        assertFalse(authorizationControl.isLoggedIn(geloeschterBenutzer),
                "Nach Löschung muss isLoggedIn() false zurückgeben.");
    }


    /**
     * TEST 3 — Nach Löschung: Kein Zugriff auf die Ideen-Übersicht.
     *
     * Globals.Pages.SHOW_IDEAS ist eine geschützte Seite.
     * Nach Profilöschung darf diese Seite nicht mehr erreichbar sein.
     */
    @Test
    void testNachLoeschung_KeinZugriffAufIdeenUebersicht() {
        BenutzerDTO geloeschterBenutzer = null;

        assertFalse(
                authorizationControl.canAccessFeature(geloeschterBenutzer, Globals.Pages.SHOW_IDEAS),
                "Gelöschter Benutzer darf Ideen-Übersicht nicht sehen."
        );
    }


    /**
     * TEST 4 — Nach Löschung: Kein Zugriff auf Ideen erstellen.
     *
     * Globals.Pages.ENTER_IDEAS ist nur für eingeloggte Gründer.
     * Nach Profilöschung darf diese Seite erst recht nicht erreichbar sein.
     */
    @Test
    void testNachLoeschung_KeinZugriffAufIdeenErstellen() {
        BenutzerDTO geloeschterBenutzer = null;

        assertFalse(
                authorizationControl.canAccessFeature(geloeschterBenutzer, Globals.Pages.ENTER_IDEAS),
                "Gelöschter Benutzer darf keine Ideen erstellen."
        );
    }


    /**
     * TEST 5 — Nach Löschung: Benutzer wird nicht mehr als Student erkannt.
     *
     * isStudent(null) muss false zurückgeben — das Repository
     * darf dabei nicht einmal aufgerufen werden.
     */
    @Test
    void testNachLoeschung_NichtMehrAlsStudentErkannt() {
        BenutzerDTO geloeschterBenutzer = null;

        assertFalse(authorizationControl.isStudent(geloeschterBenutzer),
                "Gelöschter Benutzer darf nicht als Student erkannt werden.");

        // Repository darf bei null-User nicht aufgerufen werden
        verify(studentRepository, never()).existsById(anyInt());
    }


    /**
     * TEST 6 — Nach Löschung: Benutzer wird nicht mehr als Gründer erkannt.
     *
     * isGruender(null) muss false zurückgeben — das Repository
     * darf dabei nicht einmal aufgerufen werden.
     */
    @Test
    void testNachLoeschung_NichtMehrAlsGruenderErkannt() {
        BenutzerDTO geloeschterBenutzer = null;

        assertFalse(authorizationControl.isGruender(geloeschterBenutzer),
                "Gelöschter Benutzer darf nicht als Gründer erkannt werden.");

        verify(gruenderRepository, never()).existsById(anyInt());
    }
}
