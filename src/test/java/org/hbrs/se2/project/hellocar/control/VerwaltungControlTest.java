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
        // Szenario: Ein Benutzer, der auch ein Student ist, wird gelöscht.

        // Annahme: Der Benutzer existiert als Student und als allgemeiner Benutzer
        when(studentRepository.existsById(1)).thenReturn(true);
        when(benutzerRepository.existsById(1)).thenReturn(true);

        /**
         * Bitte den Test neu erstellen. Es wird kein Boolean mehr zurück gegeben
         * und war von Anfang keine gute weise es zu testen.
         */

        // Aufruf der zu testenden Methode
        //boolean result = verwaltungControl.loeschen(mockBenutzerDTO);

        // Überprüfung: Die Methode sollte erfolgreich sein
        //assertTrue(result);

        // Überprüfung: Die deleteById-Methode muss für beide Repositories aufgerufen worden sein
        verify(studentRepository, times(1)).deleteById(1);
        verify(benutzerRepository, times(1)).deleteById(1);
        // Überprüfung: Da er kein Gründer ist, darf hier nichts gelöscht werden
        verify(gruenderRepository, never()).deleteById(1);
    }

    @Test
    void testLoeschen_GruenderUndBenutzer_Success() {
        // Szenario: Ein Benutzer, der auch ein Gründer ist, wird gelöscht.

        when(gruenderRepository.existsById(1)).thenReturn(true);
        when(benutzerRepository.existsById(1)).thenReturn(true);

        //boolean result = verwaltungControl.loeschen(mockBenutzerDTO);
        /**
         * erneut neu erstellen
         */
        //assertTrue(result);
        verify(gruenderRepository, times(1)).deleteById(1);
        verify(benutzerRepository, times(1)).deleteById(1);
        verify(studentRepository, never()).deleteById(1);
    }

    @Test
    void testLoeschen_BenutzerNichtGefunden_ReturnsFalse() {
        // Szenario: Versuch, einen Benutzer zu löschen, den es nicht gibt.

        // Annahme: Der Benutzer existiert in keinem der Repositories
        when(studentRepository.existsById(1)).thenReturn(false);
        when(gruenderRepository.existsById(1)).thenReturn(false);
        when(benutzerRepository.existsById(1)).thenReturn(false);

        //boolean result = verwaltungControl.loeschen(mockBenutzerDTO);

        /**
         * erneut neu erstellen
         */

        // Überprüfung: Die Methode sollte fehlschlagen (false zurückgeben)
        //assertFalse(result);

        // Überprüfung: Es darf keine Löschoperation aufgerufen worden sein
        verify(studentRepository, never()).deleteById(anyInt());
        verify(gruenderRepository, never()).deleteById(anyInt());
        verify(benutzerRepository, never()).deleteById(anyInt());
    }

    @Test
    void testLoeschen_CurrentUserIsNull_ReturnsFalse() {
        // Szenario: Die Methode wird mit 'null' aufgerufen.
        // Dies testet die Robustheit der Methode gegen unerwartete Eingaben.
        //boolean result = verwaltungControl.loeschen(null);

        /**
         * erneut neu erstellen
         */

        // Überprüfung: Die Methode muss sicher fehlschlagen
        //assertFalse(result);
    }


    // -------------------------------------------------------
    // Tests für die ladeBenutzer() Methode
    // -------------------------------------------------------

    @Test
    void testLadeBenutzer_BenutzerGefunden_ReturnsDTO() {
        // Szenario: Ein existierender Benutzer wird geladen.

        // Annahme: Das Repository findet den Benutzer und gibt ihn zurück
        when(benutzerRepository.findById(1)).thenReturn(Optional.of(mockBenutzer));

        // TODO Test abändern!
        // Aufruf der Methode
        //BenutzerDTO resultDTO = verwaltungControl.ladeBenutzer(1);

        // Überprüfung: Das zurückgegebene DTO darf nicht null sein
        //assertNotNull(resultDTO);
        // Überprüfung: Die Daten im DTO müssen mit der Entität übereinstimmen
        //assertEquals("Max", resultDTO.getVorname());
        //assertEquals("Mustermann", resultDTO.getNachname());
    }

    @Test
    void testLadeBenutzer_BenutzerNichtGefunden_ReturnsNull() {
        // Szenario: Versuch, einen nicht existierenden Benutzer zu laden.

        // Annahme: Das Repository findet keinen Benutzer
        when(benutzerRepository.findById(anyInt())).thenReturn(Optional.empty());

        // TODO Test abändern!
        // Aufruf der Methode
        //BenutzerDTO resultDTO = verwaltungControl.ladeBenutzer(99);

        // Überprüfung: Die Methode muss null zurückgeben
        //xassertNull(resultDTO);
    }
}