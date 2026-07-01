package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.builder.BenutzerBuilder;
import org.hbrs.se2.project.hellocar.builder.GruenderBuilder;
import org.hbrs.se2.project.hellocar.builder.StudentBuilder;
import org.hbrs.se2.project.hellocar.control.exception.*;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstests für die {@link RegistrationControl}.
 * <p>
 * Diese Klasse testet die Validierungslogik und den gesamten Registrierungsprozess
 * unter Einbeziehung der Datenbank.
 * <p>
 * Die Annotation {@code @Transactional} sorgt dafür, dass alle Datenbankänderungen
 * nach jedem Test automatisch zurückgerollt werden, sodass die Tests unabhängig
 * voneinander bleiben.
 * <p>
 * Die Tests sind mit {@code @Nested} und {@code @DisplayName} strukturiert, um die
 * Lesbarkeit im Test-Runner der IDE zu maximieren.
 */
@SpringBootTest
@Transactional
@DisplayName("Integrationstests für die RegistrationControl")
public class RegistrationControlTest {

    @Autowired
    private RegistrationControl registrationControl;
    @Autowired
    private TestDatenFabrik testDatenFabrik;
    @Autowired
    private BenutzerRepository benutzerRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private GruenderRepository gruenderRepository;

    private Benutzer kollisionsBenutzer;

    @BeforeEach
    void setUp() {
        kollisionsBenutzer = testDatenFabrik.erstelleUndSpeichereBenutzer("Kollision");
    }

    @Nested
    @DisplayName("Erfolgsfälle (Happy Paths)")
    class SuccessTests {
        /**
         * <ul>
         *     <li><b>Szenario:</b> Ein neuer Student wird mit vollständig validen Daten registriert.</li>
         *     <li><b>Erwartung:</b> Der Prozess läuft ohne Fehler durch, und ein neuer Benutzer sowie ein neuer Student sind danach in der Datenbank vorhanden.</li>
         * </ul>
         */
        @Test
        @DisplayName("Ein Student wird erfolgreich registriert.")
        void testRegistriereStudent_Success() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withUsername("neuer_student_1"))
                    .build();

            assertDoesNotThrow(() -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));

            Benutzer dbBenutzer = benutzerRepository.findByUsername("neuer_student_1").orElse(null);
            assertNotNull(dbBenutzer);
            assertTrue(studentRepository.existsById(dbBenutzer.getUserId()));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Ein neuer Gründer wird mit vollständig validen Daten registriert.</li>
         *     <li><b>Erwartung:</b> Der Prozess läuft ohne Fehler durch, und ein neuer Benutzer sowie ein neuer Gründer sind danach in der Datenbank vorhanden.</li>
         * </ul>
         */
        @Test
        @DisplayName("Ein Gründer wird erfolgreich registriert.")
        void testRegistriereGruender_Success() {
            GruenderBuilder.TestGruender testGruender = GruenderBuilder.aGruender()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withUsername("neuer_gruender_1"))
                    .build();

            assertDoesNotThrow(() -> registrationControl.registriereGruender(
                    testGruender.getBenutzer().getVorname(),
                    testGruender.getBenutzer().getNachname(),
                    testGruender.getBenutzer().getGeburtstag(),
                    testGruender.getBenutzer().getUsername(),
                    testGruender.getBenutzer().getEmail(),
                    testGruender.getBenutzer().getPasswort(),
                    testGruender.getGruender().getUnternehmensname()
            ));

            Benutzer dbBenutzer = benutzerRepository.findByUsername("neuer_gruender_1").orElse(null);
            assertNotNull(dbBenutzer);
            assertTrue(gruenderRepository.existsById(dbBenutzer.getUserId()));
        }
    }

    @Nested
    @DisplayName("Fehlerfälle: Allgemeine Benutzer-Validierung")
    class UserValidationFailureTests {
        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand versucht sich mit einem Benutzernamen zu registrieren, der bereits vergeben ist.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link UsernameAlreadyExistsException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn Username bereits existiert.")
        void testRegistriereStudent_Failure_UsernameAlreadyExists() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withUsername(kollisionsBenutzer.getUsername()))
                    .build();

            assertThrows(UsernameAlreadyExistsException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    "andere-email@test.de",
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand versucht sich mit einem leeren Benutzernamen zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link UsernameEmptyException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn Username leer ist.")
        void testRegistriereStudent_Failure_UsernameEmpty() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withUsername(""))
                    .build();

            assertThrows(UsernameEmptyException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand versucht sich mit einem Passwort zu registrieren, das nicht den Sicherheitsrichtlinien entspricht.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link PasswordInvalidException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn Passwort unsicher ist.")
        void testRegistriereStudent_Failure_InvalidPassword() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withPasswort("schwach"))
                    .build();

            assertThrows(PasswordInvalidException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand versucht sich mit einer E-Mail-Adresse zu registrieren, die ein ungültiges Format hat.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link InvalidEmailException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn E-Mail ungültig ist.")
        void testRegistriereStudent_Failure_InvalidEmail() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withEmail("keine-gueltige-email"))
                    .build();

            assertThrows(InvalidEmailException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand versucht sich ohne Angabe eines Vornamens zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link FirstNameEmptyException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn Vorname leer ist.")
        void testRegistriereStudent_Failure_FirstNameEmpty() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withVorname(""))
                    .build();

            assertThrows(FirstNameEmptyException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand versucht sich ohne Angabe eines Nachnamens zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link LastNameEmptyException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn Nachname leer ist.")
        void testRegistriereStudent_Failure_LastNameEmpty() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withNachname(""))
                    .build();

            assertThrows(LastNameEmptyException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand versucht sich ohne Angabe eines Geburtsdatums zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link BirthdayMissingException}. (Dieser Test wird fehlschlagen, bis die Logik implementiert ist!)</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn Geburtsdatum null ist.")
        void testRegistriereStudent_Failure_BirthdayMissing() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withGeburtstag(null))
                    .build();

            assertThrows(BirthdayMissingException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Jemand, der unter 16 ist, versucht sich zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link UserTooYoungException}. (Dieser Test wird fehlschlagen, bis die Logik implementiert ist!)</li>
         * </ul>
         */
        @Test
        @DisplayName("Registrierung schlägt fehl, wenn Benutzer zu jung ist.")
        void testRegistriereStudent_Failure_UserTooYoung() {
            LocalDate seventeenYearsAgo = LocalDate.now().minusYears(15);
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withBenutzer(BenutzerBuilder.aBenutzer().withGeburtstag(seventeenYearsAgo))
                    .build();

            assertThrows(UserTooYoungException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }
    }

    @Nested
    @DisplayName("Fehlerfälle: Rollen-spezifische Validierung")
    class RoleValidationFailureTests {
        /**
         * <ul>
         *     <li><b>Szenario:</b> Ein Student versucht sich ohne Angabe einer Universität zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link UniEmptyException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Studenten-Registrierung schlägt fehl, wenn Universität leer ist.")
        void testRegistriereStudent_Failure_UniversityEmpty() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withUniversitaet("")
                    .build();

            assertThrows(UniEmptyException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Ein Student versucht sich mit einem ungültigen Semester (0 oder negativ) zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link IllegalArgumentException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Studenten-Registrierung schlägt fehl, wenn Semester ungültig ist.")
        void testRegistriereStudent_Failure_InvalidSemester() {
            StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                    .withSemester(0)
                    .build();

            assertThrows(IllegalArgumentException.class, () -> registrationControl.registriereStudent(
                    testStudent.getBenutzer().getVorname(),
                    testStudent.getBenutzer().getNachname(),
                    testStudent.getBenutzer().getGeburtstag(),
                    testStudent.getBenutzer().getUsername(),
                    testStudent.getBenutzer().getEmail(),
                    testStudent.getBenutzer().getPasswort(),
                    testStudent.getStudent().getUniversitaet(),
                    testStudent.getStudent().getSemester()
            ));
        }

        /**
         * <ul>
         *     <li><b>Szenario:</b> Ein Gründer versucht sich ohne Angabe eines Unternehmensnamens zu registrieren.</li>
         *     <li><b>Erwartung:</b> Der Prozess schlägt fehl und wirft eine {@link StartUpEmptyException}.</li>
         * </ul>
         */
        @Test
        @DisplayName("Gründer-Registrierung schlägt fehl, wenn Unternehmensname leer ist.")
        void testRegistriereGruender_Failure_StartupNameEmpty() {
            GruenderBuilder.TestGruender testGruender = GruenderBuilder.aGruender()
                    .withUnternehmensname("")
                    .build();

            assertThrows(StartUpEmptyException.class, () -> registrationControl.registriereGruender(
                    testGruender.getBenutzer().getVorname(),
                    testGruender.getBenutzer().getNachname(),
                    testGruender.getBenutzer().getGeburtstag(),
                    testGruender.getBenutzer().getUsername(),
                    testGruender.getBenutzer().getEmail(),
                    testGruender.getBenutzer().getPasswort(),
                    testGruender.getGruender().getUnternehmensname()
            ));
        }
    }
}