package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.builder.StudentBuilder;
import org.hbrs.se2.project.hellocar.builder.BenutzerBuilder;
import org.hbrs.se2.project.hellocar.builder.GruenderBuilder;
import org.hbrs.se2.project.hellocar.control.exception.StudentensucheZugriffVerweigertException;
import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StudentensucheControlTest {

    @Autowired
    private StudentensucheControl studentensucheControl;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private GruenderRepository gruenderRepository;

    @Autowired
    private SkillRepository skillRepository;

    private BenutzerDTOImpl gruenderDTO;
    private BenutzerDTOImpl studentDTO;
    private Benutzer gruenderBenutzer;
    private Benutzer studentBenutzer;

    @BeforeEach
    void setUp() {
        // Gründer erstellen und speichern (Roundtrip)
        gruenderBenutzer = BenutzerBuilder.aBenutzer()
                .withUsername("gruender_search_" + System.currentTimeMillis())
                .withEmail("gruender@search-test.de")
                .build();
        gruenderBenutzer = benutzerRepository.save(gruenderBenutzer);

        Gruender gruender = GruenderBuilder.aGruender()
                .withBenutzer(BenutzerBuilder.aBenutzer()
                        .withUsername("gruender_search_" + System.currentTimeMillis()))
                .build()
                .getGruender();
        gruender.setUserId(gruenderBenutzer.getUserId());
        gruenderRepository.save(gruender);

        gruenderDTO = new BenutzerDTOImpl();
        gruenderDTO.setUserId(gruenderBenutzer.getUserId());
        gruenderDTO.setVorname("Max");
        gruenderDTO.setNachname("Gründer");

        // Student erstellen (für Zugriffstests)
        studentBenutzer = BenutzerBuilder.aBenutzer()
                .withUsername("student_search_" + System.currentTimeMillis())
                .withEmail("student@search-test.de")
                .build();
        studentBenutzer = benutzerRepository.save(studentBenutzer);

        studentDTO = new BenutzerDTOImpl();
        studentDTO.setUserId(studentBenutzer.getUserId());
        studentDTO.setVorname("Lisa");
        studentDTO.setNachname("Student");
    }

    @Nested
    class FilterFunktionTests {

        @Test
        void roundtripStudentenSpeichernUndNachSkillFiltern() {
            // Arrange – Studenten mit Skills speichern
            Student s1 = erstelleUndSpeichereStudent("java_dev", Arrays.asList("Java", "Spring"));
            Student s2 = erstelleUndSpeichereStudent("python_dev", Arrays.asList("Python", "ML"));
            Student s3 = erstelleUndSpeichereStudent("fullstack", Arrays.asList("Java", "JavaScript"));

            // Act – Nach Java filtern
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("Java")
            );

            // Assert
            assertEquals(2, ergebnis.size());
            assertTrue(ergebnis.stream().anyMatch(s -> s.getUserId() == s1.getUserId()));
            assertTrue(ergebnis.stream().anyMatch(s -> s.getUserId() == s3.getUserId()));
        }

        @Test
        void roundtripFilterNachUniversitaet() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudentMitUni("hbRS_1", "H-BRS");
            Student s2 = erstelleUndSpeichereStudentMitUni("rwth_1", "RWTH Aachen");
            Student s3 = erstelleUndSpeichereStudentMitUni("hbRS_2", "H-BRS");

            // Act
            List<Student> ergebnis = studentensucheControl.filterNachUniversitaet(
                    gruenderDTO,
                    "H-BRS"
            );

            // Assert
            assertEquals(2, ergebnis.size());
            assertTrue(ergebnis.stream().allMatch(s -> s.getUniversitaet().equals("H-BRS")));
        }

        @Test
        void roundtripFilterNachSemester() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudentMitSemester("sem_3_1", 3);
            Student s2 = erstelleUndSpeichereStudentMitSemester("sem_5_1", 5);
            Student s3 = erstelleUndSpeichereStudentMitSemester("sem_3_2", 3);

            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSemester(
                    gruenderDTO,
                    3
            );

            // Assert
            assertEquals(2, ergebnis.size());
            assertTrue(ergebnis.stream().allMatch(s -> s.getSemester() == 3));
        }

        @Test
        void roundtripKombinierterFilter() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudentKomplett("target", "Java", "H-BRS", 5, "Bachelor");
            Student s2 = erstelleUndSpeichereStudentKomplett("wrong_skill", "Python", "H-BRS", 5, "Bachelor");
            Student s3 = erstelleUndSpeichereStudentKomplett("wrong_uni", "Java", "RWTH", 5, "Bachelor");

            // Act
            List<Student> ergebnis = studentensucheControl.filterKomplett(
                    gruenderDTO,
                    Arrays.asList("Java"),
                    "H-BRS",
                    4,
                    6,
                    "Bachelor"
            );

            // Assert
            assertEquals(1, ergebnis.size());
            assertEquals(s1.getUserId(), ergebnis.get(0).getUserId());
        }

        @Test
        void roundtripSkillsWerdenKorrektGeladen() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudent("skill_test", Arrays.asList("Java", "Spring", "MySQL"));

            // Act – Student neu laden (simuliert neuen DB-Zugriff)
            Student geladen = studentRepository.findById(s1.getUserId()).orElse(null);

            // Assert
            assertNotNull(geladen);
            assertEquals(3, geladen.getSkillEntities().size());
            assertTrue(geladen.getSkills().contains("Java"));
        }

        @Test
        void roundtripGrossKleinschreibungIgnoriert() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudent("case_test", Arrays.asList("Java", "Spring"));

            // Act – Mit anderer Schreibweise suchen
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("JAVA", "spring")
            );

            // Assert
            assertEquals(1, ergebnis.size());
        }

        @Test
        void roundtripFilterMitNichtExistierendemSkill() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudent("no_match", Arrays.asList("Java"));

            // Act – Nach nicht vorhandenem Skill suchen
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("COBOL")
            );

            // Assert
            assertTrue(ergebnis.isEmpty());
        }

        @Test
        void roundtripMehrereFilterHintereinander() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudent("multi_1", Arrays.asList("Java"));
            Student s2 = erstelleUndSpeichereStudent("multi_2", Arrays.asList("Python"));
            Student s3 = erstelleUndSpeichereStudent("multi_3", Arrays.asList("Java", "Python"));

            // Act – Erst nach Java, dann nach Python
            List<Student> javaResult = studentensucheControl.filterNachSkill(
                    gruenderDTO, Arrays.asList("Java"));
            List<Student> pythonResult = studentensucheControl.filterNachSkill(
                    gruenderDTO, Arrays.asList("Python"));

            // Assert
            assertEquals(2, javaResult.size());
            assertEquals(2, pythonResult.size());
            assertEquals(1, javaResult.stream()
                    .filter(s -> pythonResult.stream().anyMatch(p -> p.getUserId() == s.getUserId()))
                    .count());
        }

        @Test
        void roundtripStudentOhneSkills() {
            // Arrange
            Student s1 = StudentBuilder.aStudent()
                    .withoutSkills()
                    .withBenutzer(BenutzerBuilder.aBenutzer()
                            .withUsername("no_skills_user_" + System.currentTimeMillis()))
                    .build();

            Benutzer benutzer = benutzerRepository.save(s1.getBenutzer());
            s1.getStudent().setUserId(benutzer.getUserId());
            studentRepository.save(s1.getStudent());

            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("Java")
            );

            // Assert
            assertFalse(ergebnis.stream().anyMatch(s -> s.getUserId() == benutzer.getUserId()));
        }

        @Test
        void roundtripDatenkonsistenzNachFilter() {
            // Arrange
            Student s1 = erstelleUndSpeichereStudent("consistency", Arrays.asList("Java"));
            int originalUserId = s1.getUserId();
            String originalUni = s1.getUniversitaet();

            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("Java")
            );

            // Assert
            Student gefunden = ergebnis.stream()
                    .filter(s -> s.getUserId() == originalUserId)
                    .findFirst()
                    .orElse(null);

            assertNotNull(gefunden);
            assertEquals(originalUni, gefunden.getUniversitaet());
        }
    }

    @Nested
    class LeereSuchergebnisseTests {

        @Test
        void leereDatenbankLeereErgebnisliste() {
            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("Java")
            );

            // Assert
            assertTrue(ergebnis.isEmpty());
        }

        @Test
        void studentenExistierenAberKeinerPasst() {
            // Arrange
            erstelleUndSpeichereStudent("python_only", Arrays.asList("Python", "ML"));
            erstelleUndSpeichereStudent("js_only", Arrays.asList("JavaScript", "React"));

            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("Java")
            );

            // Assert
            assertTrue(ergebnis.isEmpty());
        }

        @Test
        void filterNachNichtExistierenderUniversitaet() {
            // Arrange
            erstelleUndSpeichereStudentMitUni("hbRS", "H-BRS");
            erstelleUndSpeichereStudentMitUni("rwth", "RWTH Aachen");

            // Act
            List<Student> ergebnis = studentensucheControl.filterNachUniversitaet(
                    gruenderDTO,
                    "TU München"
            );

            // Assert
            assertTrue(ergebnis.isEmpty());
        }

        @Test
        void semesterFilterAusserhalbDesBereichs() {
            // Arrange
            erstelleUndSpeichereStudentMitSemester("sem_3", 3);
            erstelleUndSpeichereStudentMitSemester("sem_5", 5);

            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSemester(
                    gruenderDTO,
                    10
            );

            // Assert
            assertTrue(ergebnis.isEmpty());
        }

        @Test
        void kombinierterFilterZuRestriktiv() {
            // Arrange
            erstelleUndSpeichereStudentKomplett("s1", "Java", "H-BRS", 5, "Bachelor");
            erstelleUndSpeichereStudentKomplett("s2", "Python", "H-BRS", 3, "Master");

            // Act – Sehr spezifischer Filter
            List<Student> ergebnis = studentensucheControl.filterKomplett(
                    gruenderDTO,
                    Arrays.asList("COBOL"),
                    "TU Berlin",
                    10,
                    12,
                    "Promotion"
            );

            // Assert
            assertTrue(ergebnis.isEmpty());
        }

        @Test
        void leereErgebnislisteWirftKeineException() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                List<Student> ergebnis = studentensucheControl.filterNachSkill(
                        gruenderDTO,
                        Arrays.asList("NonExistentSkill")
                );
                assertTrue(ergebnis.isEmpty());
            });
        }

        @Test
        void leereErgebnislisteGroesseIst0() {
            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("XYZ")
            );

            // Assert
            assertEquals(0, ergebnis.size());
            assertTrue(ergebnis.isEmpty());
        }

        @Test
        void leereErgebnislisteIstVeraenderbar() {
            // Act
            List<Student> ergebnis = studentensucheControl.filterNachSkill(
                    gruenderDTO,
                    Arrays.asList("XYZ")
            );

            // Assert – Sollte keine UnsupportedOperationException werfen
            assertDoesNotThrow(() -> ergebnis.add(null));
        }

        @Test
        void mehrfacheSucheMitLeeremErgebnisKonsistent() {
            // Act
            List<Student> ergebnis1 = studentensucheControl.filterNachSkill(
                    gruenderDTO, Arrays.asList("XYZ"));
            List<Student> ergebnis2 = studentensucheControl.filterNachSkill(
                    gruenderDTO, Arrays.asList("XYZ"));

            // Assert
            assertTrue(ergebnis1.isEmpty());
            assertTrue(ergebnis2.isEmpty());
            assertEquals(ergebnis1.size(), ergebnis2.size());
        }
    }

    @Nested
    class ZugriffssperreTests {

        @Test
        void gruenderInDbZugriffErlaubt() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                studentensucheControl.pruefeZugriff(gruenderDTO);
            });
        }

        @Test
        void studentInDbZugriffVerweigert() {
            // Act & Assert
            assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                studentensucheControl.pruefeZugriff(studentDTO);
            });
        }

        @Test
        void geloeschterGruenderZugriffVerweigert() {
            // Arrange – Gründer aus DB löschen
            gruenderRepository.deleteById(gruenderBenutzer.getUserId());

            // Act & Assert
            assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                studentensucheControl.pruefeZugriff(gruenderDTO);
            });
        }

        @Test
        void nichtExistierendeUserIdZugriffVerweigert() {
            // Arrange
            BenutzerDTOImpl fakeDTO = new BenutzerDTOImpl();
            fakeDTO.setUserId(Integer.MAX_VALUE);

            // Act & Assert
            assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                studentensucheControl.pruefeZugriff(fakeDTO);
            });
        }

        @Test
        void zugriffVorFilterIntegration() {
            // Arrange – Student versucht zu filtern
            erstelleUndSpeichereStudent("test", Arrays.asList("Java"));

            // Act & Assert
            assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                studentensucheControl.filterNachSkillMitZugriff(
                        studentDTO,
                        Arrays.asList("Java")
                );
            });
        }

        @Test
        void zugriffNachRollenwechselDynamisch() throws Exception {
            // Arrange – Student wird zum Gründer befördert
            Gruender gruender = GruenderBuilder.aGruender()
                    .withBenutzer(BenutzerBuilder.aBenutzer()
                            .withUsername(studentBenutzer.getUsername()))
                    .build()
                    .getGruender();
            gruender.setUserId(studentBenutzer.getUserId());
            gruenderRepository.save(gruender);

            // Act & Assert – Jetzt sollte Zugriff erlaubt sein
            assertDoesNotThrow(() -> {
                studentensucheControl.pruefeZugriff(studentDTO);
            });
        }

        @Test
        void roundtripGruenderLoeschenZugriffVerweigert() {
            // Arrange
            assertDoesNotThrow(() -> studentensucheControl.pruefeZugriff(gruenderDTO));

            // Act – Gründer löschen
            gruenderRepository.deleteById(gruenderBenutzer.getUserId());

            // Assert – Jetzt sollte Zugriff verweigert werden
            assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                studentensucheControl.pruefeZugriff(gruenderDTO);
            });
        }

        @Test
        void exceptionNachrichtIstHilfreich() {
            // Act & Assert
            StudentensucheZugriffVerweigertException exception =
                    assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                        studentensucheControl.pruefeZugriff(studentDTO);
                    });

            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().length() > 10);
        }

        @Test
        void zugriffWirdBeiJederSucheNeuGeprueft() throws Exception {
            // Arrange
            studentensucheControl.pruefeZugriff(gruenderDTO);

            // Act – Gründer löschen
            gruenderRepository.deleteById(gruenderBenutzer.getUserId());

            // Assert – Nächste Suche sollte fehlschlagen
            assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                studentensucheControl.pruefeZugriff(gruenderDTO);
            });
        }

        @Test
        void transaktionRollbacktNachFehlgeschlagenerSuche() {
            // Arrange
            int anzahlStudentenVorher = (int) studentRepository.count();

            // Act & Assert
            assertThrows(StudentensucheZugriffVerweigertException.class, () -> {
                studentensucheControl.filterNachSkillMitZugriff(
                        studentDTO,
                        Arrays.asList("Java")
                );
            });

            // Assert – DB sollte unverändert sein
            assertEquals(anzahlStudentenVorher, studentRepository.count());
        }
    }

    // Hilfsmethoden für Testdaten-Erstellung (Roundtrip)

    private Student erstelleUndSpeichereStudent(String usernameSuffix, List<String> skills) {
        StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                .withBenutzer(BenutzerBuilder.aBenutzer()
                        .withUsername("search_" + usernameSuffix + "_" + System.currentTimeMillis()))
                .withSkills(skills.toArray(new String[0]))
                .build();

        Benutzer benutzer = benutzerRepository.save(testStudent.getBenutzer());
        Student student = testStudent.getStudent();
        student.setUserId(benutzer.getUserId());
        return studentRepository.save(student);
    }

    private Student erstelleUndSpeichereStudentMitUni(String usernameSuffix, String uni) {
        StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                .withBenutzer(BenutzerBuilder.aBenutzer()
                        .withUsername("search_uni_" + usernameSuffix + "_" + System.currentTimeMillis()))
                .withUniversitaet(uni)
                .build();

        Benutzer benutzer = benutzerRepository.save(testStudent.getBenutzer());
        Student student = testStudent.getStudent();
        student.setUserId(benutzer.getUserId());
        return studentRepository.save(student);
    }

    private Student erstelleUndSpeichereStudentMitSemester(String usernameSuffix, int semester) {
        StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                .withBenutzer(BenutzerBuilder.aBenutzer()
                        .withUsername("search_sem_" + usernameSuffix + "_" + System.currentTimeMillis()))
                .withSemester(semester)
                .build();

        Benutzer benutzer = benutzerRepository.save(testStudent.getBenutzer());
        Student student = testStudent.getStudent();
        student.setUserId(benutzer.getUserId());
        return studentRepository.save(student);
    }

    private Student erstelleUndSpeichereStudentKomplett(String usernameSuffix, String skill,
                                                        String uni, int semester, String abschluss) {
        StudentBuilder.TestStudent testStudent = StudentBuilder.aStudent()
                .withBenutzer(BenutzerBuilder.aBenutzer()
                        .withUsername("search_full_" + usernameSuffix + "_" + System.currentTimeMillis()))
                .withSkills(skill)
                .withUniversitaet(uni)
                .withSemester(semester)
                .withAbschluss(abschluss)
                .build();

        Benutzer benutzer = benutzerRepository.save(testStudent.getBenutzer());
        Student student = testStudent.getStudent();
        student.setUserId(benutzer.getUserId());
        return studentRepository.save(student);
    }
}
