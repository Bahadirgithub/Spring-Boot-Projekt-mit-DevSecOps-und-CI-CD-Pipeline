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
 * Unit-Test für Task #278 — Zugriffssperre auf geschützte Seiten nach Logout.
 *
 * Testziel: Prüfen ob ein ausgeloggter Benutzer (currentUser = null) keinen
 * Zugriff auf geschützte Seiten erhält und zur Login-Ansicht zurückgeleitet wird.
 *
 * Hintergrund: canAccessFeature() prüft intern isLoggedIn(benutzer).
 * Nach dem Logout ist benutzer = null → isLoggedIn(null) = false →
 * canAccessFeature gibt false zurück → die View leitet zur Login-Seite um.
 *
 * Schicht: Unit-Test — kein @SpringBootTest, keine echte DB.
 */
@ExtendWith(MockitoExtension.class)
public class LogoutZugriffTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GruenderRepository gruenderRepository;

    @InjectMocks
    private AuthorizationControl authorizationControl;

    // Simuliert einen eingeloggten Benutzer VOR dem Logout
    private BenutzerDTO eingeloggterBenutzer;

    // Simuliert den Zustand NACH dem Logout (null = keine Session)
    private final BenutzerDTO ausgeloggterBenutzer = null;

    @BeforeEach
    void setUp() {
        eingeloggterBenutzer = mock(BenutzerDTO.class);
        lenient().when(eingeloggterBenutzer.getUserId()).thenReturn(1);
        lenient().when(gruenderRepository.existsById(1)).thenReturn(true);
    }


    /**
     * TEST 1 — Referenztest: Vor dem Logout darf der Gründer Ideen erstellen.
     *
     * Zeigt den Ausgangszustand eines eingeloggten Gründers.
     */
    @Test
    void testVorLogout_GruenderHatZugriffAufGeschuetzteSeite() {
        assertTrue(
                authorizationControl.canAccessFeature(eingeloggterBenutzer, Globals.Pages.ENTER_IDEAS),
                "Ein eingeloggter Gründer muss auf 'Ideen erstellen' zugreifen dürfen."
        );
    }


    /**
     * TEST 2 — Nach Logout: Kein Zugriff auf Ideen-Übersicht (SHOW_IDEAS).
     *
     * SHOW_IDEAS ist für alle eingeloggten Benutzer sichtbar.
     * Nach dem Logout darf niemand mehr darauf zugreifen.
     */
    @Test
    void testNachLogout_KeinZugriffAufIdeenUebersicht() {
        assertFalse(
                authorizationControl.canAccessFeature(ausgeloggterBenutzer, Globals.Pages.SHOW_IDEAS),
                "Nach Logout darf die Ideen-Übersicht nicht erreichbar sein."
        );
    }


    /**
     * TEST 3 — Nach Logout: Kein Zugriff auf Ideen erstellen (ENTER_IDEAS).
     *
     * ENTER_IDEAS ist nur für eingeloggte Gründer.
     * Nach dem Logout muss der Zugriff verweigert werden.
     */
    @Test
    void testNachLogout_KeinZugriffAufIdeenErstellen() {
        assertFalse(
                authorizationControl.canAccessFeature(ausgeloggterBenutzer, Globals.Pages.ENTER_IDEAS),
                "Nach Logout darf 'Ideen erstellen' nicht erreichbar sein."
        );
    }


    /**
     * TEST 4 — Nach Logout: Kein Zugriff auf das Profil (PROFIL).
     *
     * Das eigene Profil ist eine geschützte Seite.
     * Nach dem Logout muss der Zugriff verweigert werden.
     */
    @Test
    void testNachLogout_KeinZugriffAufProfil() {
        assertFalse(
                authorizationControl.canAccessFeature(ausgeloggterBenutzer, Globals.Pages.PROFIL),
                "Nach Logout darf das Profil nicht erreichbar sein."
        );
    }


    /**
     * TEST 5 — Nach Logout: Weiterleitung zur Login-Seite ist korrekt definiert.
     *
     * Prüft ob der Login-Pfad in Globals.Pages.LOGIN_VIEW korrekt gesetzt ist.
     * Die View navigiert nach dem Logout zu dieser Route.
     */
    @Test
    void testNachLogout_LoginSeiteIstKorrektDefiniert() {
        assertNotNull(Globals.Pages.LOGIN_VIEW,
                "LOGIN_VIEW darf nicht null sein — sonst schlägt die Weiterleitung nach Logout fehl.");
        assertFalse(Globals.Pages.LOGIN_VIEW.isBlank() && !Globals.Pages.LOGIN_VIEW.equals(Globals.Pages.MAIN_VIEW),
                "LOGIN_VIEW muss auf eine gültige Route zeigen.");
    }


    /**
     * TEST 6 — Nach Logout: Alle geschützten Seiten sind gesperrt.
     *
     * Kombinierter Test — prüft alle bekannten geschützten Seiten auf einmal.
     * Keine einzige darf nach dem Logout erreichbar sein.
     */
    @Test
    void testNachLogout_AlleGeschuetztenSeitenGesperrt() {
        String[] geschuetzteSeiten = {
                Globals.Pages.SHOW_IDEAS,
                Globals.Pages.ENTER_IDEAS,
                Globals.Pages.PROFIL
        };

        for (String seite : geschuetzteSeiten) {
            assertFalse(
                    authorizationControl.canAccessFeature(ausgeloggterBenutzer, seite),
                    "Nach Logout darf Seite '" + seite + "' nicht erreichbar sein."
            );
        }
    }
}
