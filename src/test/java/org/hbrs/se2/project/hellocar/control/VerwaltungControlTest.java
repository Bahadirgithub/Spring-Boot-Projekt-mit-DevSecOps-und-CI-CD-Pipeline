package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit-Testklasse für den VerwaltungControl.
 * Diese Klasse dient als Sicherheitsnetz für alle administrativen Aktionen,
 * wie das Löschen, Laden und Aktualisieren von Benutzerdaten.
 * Sie stellt sicher, dass diese kritischen Operationen zuverlässig funktionieren.
 */
@ExtendWith(MockitoExtension.class)
public class VerwaltungControlTest {

    // Mock für das BenutzerRepository zur Simulation von DB-Abfragen
    @Mock
    private BenutzerRepository benutzerRepository;

    // Mock für das StudentRepository
    @Mock
    private StudentRepository studentRepository;

    // Mock für das GruenderRepository
    @Mock
    private GruenderRepository gruenderRepository;

    // Injiziert die gemockten Repositories in die zu testende VerwaltungControl-Klasse
    @InjectMocks
    private VerwaltungControl verwaltungControl;

    // Ein Test-Benutzer-DTO, das in den Tests verwendet wird
    private BenutzerDTO mockBenutzerDTO;
    private Benutzer mockBenutzer;

    /**
     * Initialisiert die Mock-Objekte vor jedem Test.
     */
    @BeforeEach
    void setUp() {
        // Erstellen eines simulierten Benutzer-DTOs für die Tests
        mockBenutzerDTO = mock(BenutzerDTO.class);

        // Wir verwenden lenient(), um eine "UnnecessaryStubbingException" zu vermeiden.
        // Diese Regel wird zentral definiert, um Code-Duplikate zu verhindern (DRY-Prinzip).
        // Lenient() signalisiert Mockito, dass diese Vorbereitung nicht in jedem einzelnen Testfall
        // benötigt wird, was für die Lesbarkeit und Wartbarkeit des Codes in Ordnung ist.
        lenient().when(mockBenutzerDTO.getUserId()).thenReturn(1);

        // Erstellen einer simulierten Benutzer-Entität
        mockBenutzer = new Benutzer();
        mockBenutzer.setUserId(1);
        mockBenutzer.setVorname("Max");
        mockBenutzer.setNachname("Mustermann");
    }

    // -------------------------------------------------------
    // Tests für die loeschen() Methode
    // -------------------------------------------------------

    @Test
    void testLoeschen_StudentUndBenutzer_Success() {
        // Testdaten vorbereiten: Benutzer ID definieren und Datenbank-Antworten simulieren
        int expectedId = 1;
        when(mockBenutzerDTO.getUserId()).thenReturn(expectedId);
        when(studentRepository.existsById(expectedId)).thenReturn(true);
        when(benutzerRepository.existsById(expectedId)).thenReturn(true);

        // Methode ausführen
        verwaltungControl.loeschen(mockBenutzerDTO);

        // Prüfen, ob die richtigen Repositories aufgerufen wurden
        verify(studentRepository, times(1)).deleteById(expectedId);
        verify(benutzerRepository, times(1)).deleteById(expectedId);
        verify(gruenderRepository, never()).deleteById(anyInt());
    }

    @Test
    void testLoeschen_GruenderUndBenutzer_Success() {
        // Vorbereitung: ID und Datenbank-Antworten simulieren
        int id = 1;
        when(mockBenutzerDTO.getUserId()).thenReturn(id);
        when(gruenderRepository.existsById(id)).thenReturn(true);
        when(benutzerRepository.existsById(id)).thenReturn(true);

        // Aktion: Löschen ausführen
        verwaltungControl.loeschen(mockBenutzerDTO);

        // Überprüfung: Korrekte Repositories wurden aufgerufen
        verify(gruenderRepository, times(1)).deleteById(id);
        verify(benutzerRepository, times(1)).deleteById(id);
        verify(studentRepository, never()).deleteById(anyInt());
    }

    @Test
    void testLoeschen_BenutzerNichtGefunden_ReturnsFalse() {
        // Vorbereitung: ID festlegen und alle Repositories melden "nicht gefunden"
        int id = 1;
        when(mockBenutzerDTO.getUserId()).thenReturn(id);
        when(benutzerRepository.existsById(id)).thenReturn(false);

        // Aktion
        boolean result = verwaltungControl.loeschen(mockBenutzerDTO);

        // Überprüfung
        assertFalse(result); // Methode sollte false zurückgeben
        verify(benutzerRepository, never()).deleteById(anyInt());
    }

    @Test
    void testLoeschen_CurrentUserIsNull_ReturnsFalse() {
        // Aktion: Aufruf mit null
        boolean result = verwaltungControl.loeschen(null);

        // Überprüfung: Methode muss false zurückgeben und nichts löschen
        assertFalse(result);

        // Sicherstellen, dass keine Datenbank-Interaktionen stattgefunden haben
        verifyNoInteractions(studentRepository);
        verifyNoInteractions(gruenderRepository);
        verifyNoInteractions(benutzerRepository);
    }


    // -------------------------------------------------------
    // Tests für die ladeBenutzer() Methode
    // -------------------------------------------------------

    @Test
    void testLadeBenutzer_BenutzerGefunden_ReturnsDTO() {
        // Vorbereitung: ID und Antwort des Repositories definieren
        int id = 1;
        when(benutzerRepository.findById(id)).thenReturn(Optional.of(mockBenutzer));

        // Aktion: Methode aufrufen
        BenutzerDTO resultDTO = verwaltungControl.ladeBenutzer(id);

        // Überprüfung: Ergebnis validieren
        assertNotNull(resultDTO, "Das zurückgegebene DTO sollte nicht null sein");
        assertEquals("Max", resultDTO.getVorname(), "Vorname sollte Max sein");
        assertEquals("Mustermann", resultDTO.getNachname(), "Nachname sollte Mustermann sein");
    }

    @Test
    void testLadeBenutzer_BenutzerNichtGefunden_ReturnsNull() {
        // Vorbereitung: Repository gibt ein leeres Optional zurück
        when(benutzerRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Aktion: Methode mit einer beliebigen ID aufrufen
        BenutzerDTO resultDTO = verwaltungControl.ladeBenutzer(99);

        // Überprüfung: Ergebnis muss null sein
        assertNull(resultDTO, "Das DTO sollte null sein, wenn kein Benutzer gefunden wurde");
    }
}