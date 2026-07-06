package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.control.exception.InvalidBirthdayException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Skill;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.SkillRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Mock
    private SkillRepository skillRepository;

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

    @Test
    void testLadeAlleSkillNamen_returnsNames() {
        Skill s1 = new Skill(); s1.setName("A"); s1.setKategorie("K1");
        Skill s2 = new Skill(); s2.setName("B"); s2.setKategorie("K1");
        when(skillRepository.findAllByOrderByKategorieAscNameAsc()).thenReturn(List.of(s1, s2));

        List<String> names = verwaltungControl.ladeAlleSkillNamen();

        assertEquals(2, names.size());
        assertEquals(List.of("A","B"), names);
        verify(skillRepository, times(1)).findAllByOrderByKategorieAscNameAsc();
    }

    @Test
    void testLadeStudent_found_returnsStudentWithSkills() {
        Student st = new Student();
        st.setUserId(1);
        Skill sk = new Skill(); sk.setName("Java");
        st.setSkillEntities(Set.of(sk));
        when(studentRepository.findById(1)).thenReturn(Optional.of(st));

        Student result = verwaltungControl.ladeStudent(1);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertFalse(result.getSkillEntities().isEmpty());
        verify(studentRepository).findById(1);
    }

    @Test
    void testLadeStudent_notFound_returnsNull() {
        when(studentRepository.findById(99)).thenReturn(Optional.empty());
        assertNull(verwaltungControl.ladeStudent(99));
    }

    @Test
    void testLadeGruender_found_returnsGruender() {
        Gruender g = new Gruender(); g.setUserId(5);
        when(gruenderRepository.findById(5)).thenReturn(Optional.of(g));
        assertNotNull(verwaltungControl.ladeGruender(5));
    }

    @Test
    void testLadeGruender_notFound_returnsNull() {
        when(gruenderRepository.findById(42)).thenReturn(Optional.empty());
        assertNull(verwaltungControl.ladeGruender(42));
    }

    @Test
    void testVornameAendern_nonEmpty_setsVorname() {
        Benutzer b = new Benutzer();
        assertTrue(verwaltungControl.vornameAendern(b, "Anna"));
        assertEquals("Anna", b.getVorname());
    }

    @Test
    void testVornameAendern_empty_noChange() {
        Benutzer b = new Benutzer();
        b.setVorname("Old");
        assertTrue(verwaltungControl.vornameAendern(b, ""));
        assertEquals("Old", b.getVorname());
    }

    @Test
    void testEmailAendern_invalid_returnsFalse() {
        Benutzer b = new Benutzer();
        assertFalse(verwaltungControl.emailAendern(b, "not-an-email"));
        assertNull(b.getEmail());
    }

    @Test
    void testEmailAendern_valid_setsEmail() {
        Benutzer b = new Benutzer();
        assertTrue(verwaltungControl.emailAendern(b, "foo@example.com"));
        assertEquals("foo@example.com", b.getEmail());
    }

    @Test
    void testPasswortAendern_invalid_returnsFalse() {
        Benutzer b = new Benutzer();
        assertFalse(verwaltungControl.passwortAendern(b, "short"));
    }

    @Test
    void testPasswortAendern_valid_setsPassword() {
        Benutzer b = new Benutzer();
        String good = "Abcdef1!"; // meets ValidateUserControl pattern
        assertTrue(verwaltungControl.passwortAendern(b, good));
        assertEquals(good, b.getPasswort());
    }

    @Test
    void testGeburtstagAendern_under18_throws() {
        Benutzer b = new Benutzer();
        LocalDate under18 = LocalDate.now().minusYears(17);
        assertThrows(InvalidBirthdayException.class, () -> verwaltungControl.geburtstagAendern(b, under18));
    }

    @Test
    void testGeburtstagAendern_valid_setsDate() throws InvalidBirthdayException {
        Benutzer b = new Benutzer();
        LocalDate adult = LocalDate.now().minusYears(25);
        assertTrue(verwaltungControl.geburtstagAendern(b, adult));
        assertEquals(adult, b.getGeburtstag());
    }

    @Test
    void testSkillsAendern_null_returnsTrue() {
        Student s = new Student();
        assertTrue(verwaltungControl.skillsAendern(s, null));
    }

    @Test
    void testSkillsAendern_resolvesAndSetsSkills() {
        Student s = new Student();
        Skill sk = new Skill(); sk.setName("Java");
        when(skillRepository.findByNameIn(List.of("Java"))).thenReturn(List.of(sk));

        assertTrue(verwaltungControl.skillsAendern(s, List.of("Java")));
        assertEquals(1, s.getSkillEntities().size());
        assertTrue(s.getSkillEntities().stream().anyMatch(x -> "Java".equals(x.getName())));
    }
}