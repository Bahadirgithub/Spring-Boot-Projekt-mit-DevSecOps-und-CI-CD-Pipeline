package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.builder.BenutzerBuilder;
import org.hbrs.se2.project.hellocar.builder.GruenderBuilder;
import org.hbrs.se2.project.hellocar.builder.StudentBuilder;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.control.exception.InvalidBirthdayException;
import org.hbrs.se2.project.hellocar.control.exception.InvalidEmailException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit-Testklasse für den {@link VerwaltungControl}.
 * Diese Klasse dient als Sicherheitsnetz für alle administrativen Aktionen,
 * wie das Löschen, Laden und Aktualisieren von Benutzerdaten.
 * Sie stellt sicher, dass diese kritischen Operationen zuverlässig funktionieren.
 * Durch die Verwendung von Mockito werden die Abhängigkeiten zu den Repositories isoliert,
 * sodass nur die Logik der Control-Klasse selbst getestet wird.
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
    private Benutzer testBenutzer;

    /**
     * Initialisiert die Mock-Objekte und Testdaten vor jedem einzelnen Test.
     * Die {@code @BeforeEach}-Annotation stellt sicher, dass jeder Test mit einem
     * sauberen, definierten Ausgangszustand startet, was die Tests voneinander isoliert.
     */
    @BeforeEach
    void setUp() {
        // Erstellen eines simulierten Benutzer-DTOs für die Tests
        mockBenutzerDTO = mock(BenutzerDTO.class);
        // lenient() wird verwendet, um eine "UnnecessaryStubbingException" zu vermeiden,
        // da dieser Mock nicht in jedem Testfall explizit benötigt wird.
        lenient().when(mockBenutzerDTO.getUserId()).thenReturn(1);

        // Erstellen einer wiederverwendbaren Benutzer-Entität mit dem Builder.
        // Dies fördert die Lesbarkeit und Konsistenz in den nachfolgenden Tests.
        testBenutzer = BenutzerBuilder.aBenutzer()
                .withUserId(1)
                .withVorname("Max")
                .withNachname("Mustermann")
                .build();
    }

    // -------------------------------------------------------
    // Tests für die loeschen() Methode
    // -------------------------------------------------------

    /**
     * Testet den Erfolgsfall des Löschens eines Benutzers, der die Rolle "Student" hat.
     * Szenario:
     * - Der Benutzer existiert als Student und als generischer Benutzer.
     * - Es wird verifiziert, dass die deleteById-Methode sowohl auf dem studentRepository
     *   als auch auf dem benutzerRepository aufgerufen wird.
     * - Es wird sichergestellt, dass das gruenderRepository unberührt bleibt.
     */
    @Test
    void testLoeschen_StudentUndBenutzer_Success() {
        // Arrange
        int expectedId = 1;
        when(mockBenutzerDTO.getUserId()).thenReturn(expectedId);
        when(studentRepository.existsById(expectedId)).thenReturn(true);
        when(benutzerRepository.existsById(expectedId)).thenReturn(true);

        // Act
        verwaltungControl.loeschen(mockBenutzerDTO);

        // Assert
        verify(studentRepository, times(1)).deleteById(expectedId);
        verify(benutzerRepository, times(1)).deleteById(expectedId);
        verify(gruenderRepository, never()).deleteById(anyInt());
    }

    /**
     * Testet den Erfolgsfall des Löschens eines Benutzers, der die Rolle "Gründer" hat.
     * Szenario:
     * - Der Benutzer existiert als Gründer und als generischer Benutzer.
     * - Es wird verifiziert, dass die deleteById-Methode sowohl auf dem gruenderRepository
     *   als auch auf dem benutzerRepository aufgerufen wird.
     * - Es wird sichergestellt, dass das studentRepository unberührt bleibt.
     */
    @Test
    void testLoeschen_GruenderUndBenutzer_Success() {
        // Arrange
        int id = 1;
        when(mockBenutzerDTO.getUserId()).thenReturn(id);
        when(gruenderRepository.existsById(id)).thenReturn(true);
        when(benutzerRepository.existsById(id)).thenReturn(true);

        // Act
        verwaltungControl.loeschen(mockBenutzerDTO);

        // Assert
        verify(gruenderRepository, times(1)).deleteById(id);
        verify(benutzerRepository, times(1)).deleteById(id);
        verify(studentRepository, never()).deleteById(anyInt());
    }

    /**
     * Testet den Fall, dass ein zu löschender Benutzer in der Datenbank nicht existiert.
     * Szenario:
     * - Das benutzerRepository meldet, dass kein Benutzer mit der gegebenen ID existiert.
     * - Die Methode soll {@code false} zurückgeben und keine Löschoperationen ausführen.
     */
    @Test
    void testLoeschen_BenutzerNichtGefunden_ReturnsFalse() {
        // Arrange
        int id = 1;
        when(mockBenutzerDTO.getUserId()).thenReturn(id);
        when(benutzerRepository.existsById(id)).thenReturn(false);

        // Act
        boolean result = verwaltungControl.loeschen(mockBenutzerDTO);

        // Assert
        assertFalse(result);
        verify(benutzerRepository, never()).deleteById(anyInt());
    }

    /**
     * Testet das Verhalten der Löschmethode, wenn {@code null} als Parameter übergeben wird.
     * Szenario:
     * - Die Methode wird mit {@code null} aufgerufen.
     * - Es wird erwartet, dass die Methode {@code false} zurückgibt und keine
     *   Interaktion mit den Repositories stattfindet.
     */
    @Test
    void testLoeschen_CurrentUserIsNull_ReturnsFalse() {
        // Act
        boolean result = verwaltungControl.loeschen(null);

        // Assert
        assertFalse(result);
        verifyNoInteractions(studentRepository, gruenderRepository, benutzerRepository);
    }


    // -------------------------------------------------------
    // Tests für die Lade-Methoden
    // -------------------------------------------------------

    /**
     * Testet das erfolgreiche Laden eines Benutzers und die korrekte Umwandlung in ein DTO.
     * Szenario:
     * - Das benutzerRepository findet einen Benutzer zur gegebenen ID.
     * - Es wird geprüft, ob ein valides DTO mit den korrekten Daten zurückgegeben wird.
     */
    @Test
    void testLadeBenutzer_BenutzerGefunden_ReturnsDTO() {
        // Arrange
        when(benutzerRepository.findById(1)).thenReturn(Optional.of(testBenutzer));

        // Act
        BenutzerDTO resultDTO = verwaltungControl.ladeBenutzer(1);

        // Assert
        assertNotNull(resultDTO, "Das zurückgegebene DTO sollte nicht null sein");
        assertEquals("Max", resultDTO.getVorname(), "Vorname sollte Max sein");
        assertEquals("Mustermann", resultDTO.getNachname(), "Nachname sollte Mustermann sein");
    }

    /**
     * Testet den Fall, dass kein Benutzer für eine gegebene ID gefunden wird.
     * Szenario:
     * - Das benutzerRepository gibt ein leeres Optional zurück.
     * - Es wird erwartet, dass die Methode {@code null} zurückgibt.
     */
    @Test
    void testLadeBenutzer_BenutzerNichtGefunden_ReturnsNull() {
        // Arrange
        when(benutzerRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        BenutzerDTO resultDTO = verwaltungControl.ladeBenutzer(99);

        // Assert
        assertNull(resultDTO, "Das DTO sollte null sein, wenn kein Benutzer gefunden wurde");
    }

    /**
     * Testet, ob die Namen aller Skills korrekt aus der Datenbank geladen werden.
     * Szenario:
     * - Das skillRepository gibt eine Liste von Skill-Entitäten zurück.
     * - Es wird geprüft, ob die Methode eine Liste von Strings mit den korrekten Namen zurückgibt.
     */
    @Test
    void testLadeAlleSkillNamen_returnsNames() {
        // Arrange
        Skill s1 = new Skill(); s1.setName("A");
        Skill s2 = new Skill(); s2.setName("B");
        when(skillRepository.findAllByOrderByKategorieAscNameAsc()).thenReturn(List.of(s1, s2));

        // Act
        List<String> names = verwaltungControl.ladeAlleSkillNamen();

        // Assert
        assertEquals(2, names.size());
        assertEquals(List.of("A","B"), names);
        verify(skillRepository).findAllByOrderByKategorieAscNameAsc();
    }

    /**
     * Testet das erfolgreiche Laden eines Studenten inklusive seiner Skills.
     * Szenario:
     * - Ein Student mit einem Skill wird über den Builder erstellt.
     * - Das studentRepository findet diesen Studenten.
     * - Es wird geprüft, ob der zurückgegebene Student korrekt ist und seine Skills geladen wurden.
     */
    @Test
    void testLadeStudent_found_returnsStudentWithSkills() {
        // Arrange
        Student testStudent = StudentBuilder.aStudent()
                .withSkills("Java")
                .build()
                .getStudent();
        testStudent.setUserId(1);
        when(studentRepository.findById(1)).thenReturn(Optional.of(testStudent));

        // Act
        Student result = verwaltungControl.ladeStudent(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertFalse(result.getSkills().isEmpty());
        assertTrue(result.getSkills().contains("Java"));
        verify(studentRepository).findById(1);
    }

    /**
     * Testet den Fall, dass kein Student für eine gegebene ID gefunden wird.
     */
    @Test
    void testLadeStudent_notFound_returnsNull() {
        // Arrange
        when(studentRepository.findById(99)).thenReturn(Optional.empty());
        // Act & Assert
        assertNull(verwaltungControl.ladeStudent(99));
    }

    /**
     * Testet das erfolgreiche Laden eines Gründers.
     */
    @Test
    void testLadeGruender_found_returnsGruender() {
        // Arrange
        Gruender testGruender = GruenderBuilder.aGruender().build().getGruender();
        testGruender.setUserId(5);
        when(gruenderRepository.findById(5)).thenReturn(Optional.of(testGruender));

        // Act & Assert
        assertNotNull(verwaltungControl.ladeGruender(5));
        verify(gruenderRepository).findById(5);
    }

    /**
     * Testet den Fall, dass kein Gründer für eine gegebene ID gefunden wird.
     */
    @Test
    void testLadeGruender_notFound_returnsNull() {
        // Arrange
        when(gruenderRepository.findById(42)).thenReturn(Optional.empty());
        // Act & Assert
        assertNull(verwaltungControl.ladeGruender(42));
    }

    // -------------------------------------------------------
    // Tests für die Änderungs-Methoden (einzelne Attribute)
    // -------------------------------------------------------

    /**
     * Testet, ob der Vorname eines Benutzers korrekt geändert wird, wenn ein gültiger String übergeben wird.
     */
    @Test
    void testVornameAendern_nonEmpty_setsVorname() {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer().build();
        // Act
        assertTrue(verwaltungControl.vornameAendern(benutzer, "Anna"));
        // Assert
        assertEquals("Anna", benutzer.getVorname());
    }

    /**
     * Testet, ob der Vorname eines Benutzers unverändert bleibt, wenn ein leerer String übergeben wird.
     */
    @Test
    void testVornameAendern_empty_noChange() {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer().withVorname("Old").build();
        // Act
        assertTrue(verwaltungControl.vornameAendern(benutzer, ""));
        // Assert
        assertEquals("Old", benutzer.getVorname());
    }

    /**
     * Testet, ob die E-Mail-Änderung bei einer ungültigen E-Mail fehlschlägt.
     */
    @Test
    void testEmailAendern_invalid_returnsFalse() {
        // Arrange: Erstelle einen Benutzer, der initial keine E-Mail hat.
        Benutzer benutzer = BenutzerBuilder.aBenutzer().withEmail(null).build();

        // Act
        boolean result = verwaltungControl.emailAendern(benutzer, "not-an-email");

        // Assert: Die Methode sollte false zurückgeben und die E-Mail sollte immer noch null sein.
        assertFalse(result);
        assertNull(benutzer.getEmail());
    }

    /**
     * Testet, ob die E-Mail eines Benutzers bei einer gültigen Eingabe korrekt gesetzt wird.
     */
    @Test
    void testEmailAendern_valid_setsEmail() {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer().build();
        // Act
        assertTrue(verwaltungControl.emailAendern(benutzer, "foo@example.com"));
        // Assert
        assertEquals("foo@example.com", benutzer.getEmail());
    }

    /**
     * Testet, ob die Passwort-Änderung bei einem zu kurzen Passwort fehlschlägt.
     */
    @Test
    void testPasswortAendern_invalid_returnsFalse() {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer().build();
        // Act & Assert
        assertFalse(verwaltungControl.passwortAendern(benutzer, "short"));
    }

    /**
     * Testet, ob das Passwort eines Benutzers bei einer gültigen Eingabe korrekt gesetzt wird.
     */
    @Test
    void testPasswortAendern_valid_setsPassword() {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer().build();
        String good = "Abcdef1!"; // Entspricht den Validierungsregeln
        // Act
        assertTrue(verwaltungControl.passwortAendern(benutzer, good));
        // Assert
        assertEquals(good, benutzer.getPasswort());
    }

    /**
     * Testet, ob bei der Angabe eines Geburtsdatums, das auf ein Alter unter 18 Jahren hindeutet,
     * eine {@link InvalidBirthdayException} geworfen wird.
     */
    @Test
    void testGeburtstagAendern_under18_throws() {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer().build();
        LocalDate under18 = LocalDate.now().minusYears(17);
        // Act & Assert
        assertThrows(InvalidBirthdayException.class, () -> verwaltungControl.geburtstagAendern(benutzer, under18));
    }

    /**
     * Testet, ob das Geburtsdatum bei einer gültigen Eingabe (Alter >= 18) korrekt gesetzt wird.
     */
    @Test
    void testGeburtstagAendern_valid_setsDate() throws InvalidBirthdayException {
        // Arrange
        Benutzer benutzer = BenutzerBuilder.aBenutzer().build();
        LocalDate adult = LocalDate.now().minusYears(25);
        // Act
        assertTrue(verwaltungControl.geburtstagAendern(benutzer, adult));
        // Assert
        assertEquals(adult, benutzer.getGeburtstag());
    }

    /**
     * Testet, ob die Skill-Änderungsmethode korrekt funktioniert, wenn {@code null} übergeben wird.
     * Es sollte keine Änderung und kein Fehler auftreten.
     */
    @Test
    void testSkillsAendern_null_returnsTrue() {
        // Arrange
        Student student = StudentBuilder.aStudent().build().getStudent();
        // Act & Assert
        assertTrue(verwaltungControl.skillsAendern(student, null));
    }

    /**
     * Testet, ob die Skills eines Studenten korrekt aktualisiert werden.
     * Szenario:
     * - Ein Student ohne Skills wird erstellt.
     * - Das skillRepository wird so gemockt, dass es für den Namen "Java" eine Skill-Entität findet.
     * - Die Methode wird aufgerufen, um den Skill "Java" hinzuzufügen.
     * - Es wird geprüft, ob der Student danach genau diesen einen Skill besitzt.
     */
    @Test
    void testSkillsAendern_resolvesAndSetsSkills() {
        // Arrange
        Student student = StudentBuilder.aStudent().withoutSkills().build().getStudent();
        Skill javaSkill = new Skill();
        javaSkill.setName("Java");
        when(skillRepository.findByNameIn(List.of("Java"))).thenReturn(List.of(javaSkill));

        // Act
        assertTrue(verwaltungControl.skillsAendern(student, List.of("Java")));

        // Assert
        assertEquals(1, student.getSkillEntities().size());
        assertTrue(student.getSkillEntities().contains(javaSkill));
    }

    // -------------------------------------------------------
    // Tests für die kompletten Update-Methoden
    // -------------------------------------------------------

    /**
     * Testet den kompletten Update-Vorgang für einen Studenten ("Happy Path").
     * Dieser Test schließt eine kritische Lücke, indem er die gesamte
     * {@link VerwaltungControl# updateStudent(BenutzerDTO, String, String, ...)} Methode überprüft.
     * Er stellt sicher, dass die Daten korrekt aus der DB geladen, die Änderungen
     * angewendet und die Ergebnisse wieder zum Speichern an die Repositories übergeben werden.
     */
    @Test
    void testUpdateStudent_savesChanges() throws InvalidEmailException, InvalidBirthdayException, DatabaseUserException {
        // Arrange: Testdaten und Mocking-Verhalten definieren
        final int userId = 1;
        BenutzerDTO currentUser = mock(BenutzerDTO.class);
        when(currentUser.getUserId()).thenReturn(userId);

        Benutzer existingBenutzer = BenutzerBuilder.aBenutzer().withUserId(userId).build();
        Student existingStudent = StudentBuilder.aStudent().build().getStudent();
        existingStudent.setUserId(userId);

        when(benutzerRepository.findById(userId)).thenReturn(Optional.of(existingBenutzer));
        when(studentRepository.findById(userId)).thenReturn(Optional.of(existingStudent));
        // Wichtig: Mocken des Skill-Repositorys, da die Methode es aufruft
        when(skillRepository.findByNameIn(anyList())).thenReturn(Collections.emptyList());

        // Act: Die zu testende Methode mit allen Parametern aufrufen
        verwaltungControl.updateStudent(
                currentUser,
                "Anna", // neuerVorname
                "Mustermann", // Nachname bleibt gleich
                LocalDate.of(1995, 5, 5), // Geburtstag
                "anna@example.com", // Email
                "012345", // Telefon
                "TH Köln", // neueUni
                3, // neuesSemester
                "Master", // Abschluss
                List.of("Python") // Skills
        );

        // Assert: Überprüfen, ob die save-Methoden aufgerufen wurden.
        verify(benutzerRepository, times(1)).save(existingBenutzer);
        verify(studentRepository, times(1)).save(existingStudent);
    }

    /**
     * Testet den kompletten Update-Vorgang für einen Gründer ("Happy Path").
     * Dieser Test sichert die {@link VerwaltungControl# updateGruender(BenutzerDTO, String, String, ...)} Methode ab.
     * Er stellt sicher, dass die Daten korrekt aus der DB geladen, die Änderungen
     * angewendet und die Ergebnisse wieder zum Speichern an die Repositories übergeben werden.
     */
    @Test
    void testUpdateGruender_savesChanges() throws InvalidEmailException, InvalidBirthdayException {
        // Arrange: Testdaten und Mocking-Verhalten definieren
        final int userId = 2;
        BenutzerDTO currentUser = mock(BenutzerDTO.class);
        when(currentUser.getUserId()).thenReturn(userId);

        Benutzer existingBenutzer = BenutzerBuilder.aBenutzer().withUserId(userId).build();
        Gruender existingGruender = GruenderBuilder.aGruender().build().getGruender();
        existingGruender.setUserId(userId);

        when(benutzerRepository.findById(userId)).thenReturn(Optional.of(existingBenutzer));
        when(gruenderRepository.findById(userId)).thenReturn(Optional.of(existingGruender));

        // Act: Die zu testende Methode mit allen Parametern aufrufen
        verwaltungControl.updateGruender(
                currentUser,
                "Jana", // neuerVorname
                "Schmidt", // Nachname
                LocalDate.of(1985, 8, 15), // Geburtstag
                "jana@example.com", // Email
                "543210", // Telefon
                "FutureTech GmbH", // neuesUnternehmen
                "Köln" // Standort
        );

        // Assert: Überprüfen, ob die save-Methoden aufgerufen wurden.
        verify(benutzerRepository, times(1)).save(existingBenutzer);
        verify(gruenderRepository, times(1)).save(existingGruender);
    }

    /**
     * Testet das Fehlerverhalten der {@link VerwaltungControl# updateStudent(BenutzerDTO, String, String, ...)} Methode,
     * wenn der zu aktualisierende Benutzer nicht in der Datenbank gefunden wird.
     * <p>
     * Dieser Test ist entscheidend für die Robustheit der Anwendung, da er sicherstellt,
     * dass das System bei inkonsistenten Daten (z.B. der Benutzer wurde in der Zwischenzeit gelöscht)
     * kontrolliert mit einer Exception reagiert und nicht abstürzt.
     * <p>
     * Szenario:
     * - Das {@code benutzerRepository} findet keinen Benutzer für die gegebene ID.
     * - Es wird erwartet, dass die Methode eine {@link DatabaseUserException} wirft.
     */
    @Test
    void testUpdateStudent_userNotFound_throwsDatabaseUserException() {
        // 1. Arrange: Mocking-Verhalten für den "Nicht gefunden"-Fall definieren
        final int userId = 99; // Eine nicht existierende ID
        BenutzerDTO currentUser = mock(BenutzerDTO.class);
        when(currentUser.getUserId()).thenReturn(userId);

        // Simulieren, dass das Repository den Benutzer nicht findet
        when(benutzerRepository.findById(userId)).thenReturn(Optional.empty());

        // 2. Act & Assert: Überprüfen, ob die erwartete Exception geworfen wird.
        assertThrows(DatabaseUserException.class, () -> verwaltungControl.updateStudent(
                currentUser,
                "Egal", "Egal", null, "egal@egal.com", "123",
                "Egal", 1, "Egal", Collections.emptyList()
        ));

        // 3. (Zusätzliche) Assert: Sicherstellen, dass keine save-Operation versucht wurde.
        verify(studentRepository, never()).save(any());
    }

    /**
     * Testet das Fehlerverhalten der {@link VerwaltungControl# updateGruender(BenutzerDTO, String, String, ...)} Methode,
     * wenn der zu aktualisierende Gründer nicht in der Datenbank gefunden wird.
     * <p>
     * Szenario:
     * - Das {@code gruenderRepository} findet keinen Gründer für die gegebene ID.
     * - Es wird erwartet, dass die Methode {@code false} zurückgibt und keine Daten speichert.
     */
    @Test
    void testUpdateGruender_gruenderNotFound_returnsFalse() throws Exception {
        // 1. Arrange: Mocking-Verhalten für den "Nicht gefunden"-Fall definieren
        final int userId = 99;
        BenutzerDTO currentUser = mock(BenutzerDTO.class);
        when(currentUser.getUserId()).thenReturn(userId);

        Benutzer existingBenutzer = BenutzerBuilder.aBenutzer().withUserId(userId).build();

        // Simulieren, dass der Benutzer gefunden wird, aber der Gründer nicht.
        when(benutzerRepository.findById(userId)).thenReturn(Optional.of(existingBenutzer));
        when(gruenderRepository.findById(userId)).thenReturn(Optional.empty());

        // 2. Act: Die zu testende Methode aufrufen
        boolean result = verwaltungControl.updateGruender(
                currentUser,
                "Egal", "Egal", LocalDate.now().minusYears(20), "egal@egal.com", "123",
                "Egal", "Egal"
        );

        // 3. Assert: Überprüfen, ob false zurückgegeben wird und nichts gespeichert wurde.
        assertFalse(result);
        verify(benutzerRepository, never()).save(any());
        verify(gruenderRepository, never()).save(any());
    }

    /**
     * Testet, ob die Telefonnummer eines Benutzers während des Update-Prozesses korrekt geändert wird.
     * Dieser Test schließt eine Lücke, bei der nur verifiziert wurde, DASS gespeichert wird,
     * aber nicht, WAS gespeichert wird.
     * <p>
     * Szenario:
     * - Ein bestehender Benutzer mit einer alten Telefonnummer wird simuliert.
     * - Die updateStudent-Methode wird mit einer neuen Telefonnummer aufgerufen.
     * - Es wird mittels ArgumentCaptor sichergestellt, dass das an das Repository übergebene
     *   Benutzer-Objekt die neue Telefonnummer enthält.
     */
    @Test
    void testUpdateStudent_updatesTelefonnummerCorrectly() throws Exception {
        // 1. Arrange
        final int userId = 1;
        final String alteNummer = "00000";
        final String neueNummer = "99999";

        BenutzerDTO currentUser = mock(BenutzerDTO.class);
        when(currentUser.getUserId()).thenReturn(userId);

        // Erstelle einen Benutzer mit der "alten" Telefonnummer
        Benutzer existingBenutzer = BenutzerBuilder.aBenutzer()
                .withUserId(userId)
                .withTelefonnummer(alteNummer)
                .build();
        Student existingStudent = StudentBuilder.aStudent().build().getStudent();
        existingStudent.setUserId(userId);

        when(benutzerRepository.findById(userId)).thenReturn(Optional.of(existingBenutzer));
        when(studentRepository.findById(userId)).thenReturn(Optional.of(existingStudent));
        when(skillRepository.findByNameIn(anyList())).thenReturn(Collections.emptyList());

        // 2. Act
        verwaltungControl.updateStudent(
                currentUser, "Max", "Mustermann", LocalDate.now().minusYears(20),
                "max@example.com", neueNummer, "H-BRS", 1, "Bachelor", Collections.emptyList()
        );

        // 3. Assert
        // Erstelle einen ArgumentCaptor, um das Objekt zu "fangen", das an die save-Methode übergeben wird
        ArgumentCaptor<Benutzer> benutzerCaptor = ArgumentCaptor.forClass(Benutzer.class);
        // Verifiziere, dass save aufgerufen wurde UND fange das Argument
        verify(benutzerRepository).save(benutzerCaptor.capture());
        // Hole das gefangene Benutzer-Objekt
        Benutzer capturedBenutzer = benutzerCaptor.getValue();
        // Überprüfe, ob das gefangene Objekt die neue Telefonnummer hat
        assertEquals(neueNummer, capturedBenutzer.getTelefonnummer());
    }
}
