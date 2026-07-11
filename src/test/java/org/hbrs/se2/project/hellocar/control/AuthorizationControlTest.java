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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 * JUnit-Testklasse für die AuthorizationControl.
 * Testet die Geschäftslogik zur Überprüfung der Zugriffsberechtigungen
 * basierend auf Benutzerrollen (Student vs. Gründer) im JPA-Kontext.
 */
@ExtendWith(MockitoExtension.class)
public class AuthorizationControlTest {

    // Mock für das StudentRepository (JPA-Abstraktion)
    @Mock
    private StudentRepository studentRepository;

    // Mock für das GruenderRepository (JPA-Abstraktion)
    @Mock
    private GruenderRepository gruenderRepository;

    // Injektiert die gemockten Repositories in die zu testende Klasse
    @InjectMocks
    private AuthorizationControl authorizationControl;

    // Ein gemocktes DTO zur Simulation eines Benutzers
    private BenutzerDTO mockBenutzer;

    /**
     * Setzt die Testdaten vor jedem einzelnen Testfall zurück.
     */
    @BeforeEach
    void setUp() {
        mockBenutzer = mock(BenutzerDTO.class);
    }

    // -------------------------------------------------------
    // Tests für isLoggedIn()
    // -------------------------------------------------------

    @Test
    void testIsLoggedInWithValidUserReturnsTrue() {
        // Wenn ein gültiger Benutzer übergeben wird, muss true zurückgegeben werden
        assertTrue(authorizationControl.isLoggedIn(mockBenutzer));
    }

    @Test
    void testIsLoggedInWithNullReturnsFalse() {
        // Wenn kein Benutzer übergeben wird (null), muss false zurückgegeben werden
        assertFalse(authorizationControl.isLoggedIn(null));
    }

    // -------------------------------------------------------
    // Tests für isStudent()
    // -------------------------------------------------------

    @Test
    void testIsStudentWithValidStudentReturnsTrue() {
        // Simuliere die JPA-Abfrage: Der Benutzer existiert in der Student-Tabelle
        when(mockBenutzer.getUserId()).thenReturn(1);
        when(studentRepository.existsById(1)).thenReturn(true);

        assertTrue(authorizationControl.isStudent(mockBenutzer));
    }

    @Test
    void testIsStudentWithNonStudentReturnsFalse() {
        // Simuliere die JPA-Abfrage: Der Benutzer existiert NICHT in der Student-Tabelle
        when(mockBenutzer.getUserId()).thenReturn(2);
        when(studentRepository.existsById(2)).thenReturn(false);

        assertFalse(authorizationControl.isStudent(mockBenutzer));
    }

    @Test
    void testIsStudentWithNullReturnsFalse() {
        // Null-Check: Darf nicht zu einer NullPointerException führen
        assertFalse(authorizationControl.isStudent(null));
    }

    // -------------------------------------------------------
    // Tests für isGruender()
    // -------------------------------------------------------

    @Test
    void testIsGruenderWithValidGruenderReturnsTrue() {
        // Simuliere die JPA-Abfrage: Der Benutzer existiert in der Gruender-Tabelle
        when(mockBenutzer.getUserId()).thenReturn(1);
        when(gruenderRepository.existsById(1)).thenReturn(true);

        assertTrue(authorizationControl.isGruender(mockBenutzer));
    }

    @Test
    void testIsGruenderWithNonGruenderReturnsFalse() {
        // Simuliere die JPA-Abfrage: Der Benutzer existiert NICHT in der Gruender-Tabelle
        when(mockBenutzer.getUserId()).thenReturn(2);
        when(gruenderRepository.existsById(2)).thenReturn(false);

        assertFalse(authorizationControl.isGruender(mockBenutzer));
    }

    @Test
    void testIsGruenderWithNullReturnsFalse() {
        // Null-Check für Gründer-Validierung
        assertFalse(authorizationControl.isGruender(null));
    }

    // -------------------------------------------------------
    // Tests für canAccessFeature()
    // -------------------------------------------------------

    @Test
    void testCanAccessFeatureShowCarsAsLoggedInUser() {
        // Jeder eingeloggte Benutzer darf die "Show Cars" (bzw. bald "Show Ideas") Ansicht sehen
        assertTrue(authorizationControl.canAccessFeature(mockBenutzer, Globals.Pages.SHOW_IDEAS));
    }

    @Test
    void testCanAccessFeatureEnterCarAsGruender() {
        // Nur Gründer dürfen die "Enter Car" (bzw. "Enter Idea") Ansicht sehen
        when(mockBenutzer.getUserId()).thenReturn(1);
        when(gruenderRepository.existsById(1)).thenReturn(true);

        assertTrue(authorizationControl.canAccessFeature(mockBenutzer, Globals.Pages.ENTER_IDEAS));
    }

    @Test
    void testCanAccessFeatureEnterCarAsStudent() {
        // Studenten (Nicht-Gründer) dürfen die "Enter Car" Ansicht nicht sehen
        when(mockBenutzer.getUserId()).thenReturn(2);
        when(gruenderRepository.existsById(2)).thenReturn(false);

        assertFalse(authorizationControl.canAccessFeature(mockBenutzer, Globals.Pages.ENTER_IDEAS));
    }

    @Test
    void testCanAccessFeatureUnknownFeatureReturnsFalse() {
        // Ein unbekanntes Feature sollte standardmäßig den Zugriff verweigern
        assertFalse(authorizationControl.canAccessFeature(mockBenutzer, "UNKNOWN_FEATURE"));
    }
}