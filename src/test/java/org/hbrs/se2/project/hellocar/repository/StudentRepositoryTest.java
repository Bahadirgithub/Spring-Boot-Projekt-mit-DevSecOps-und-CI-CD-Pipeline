package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Skill;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.builder.StudentBuilder;
import org.hbrs.se2.project.hellocar.builder.BenutzerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BenutzerRepository benutzerRepository;

    @Autowired
    private SkillRepository skillRepository;

    private Student testStudent;
    private Benutzer testBenutzer;

    @BeforeEach
    void setUp() {
        // Zuerst Benutzer erstellen (Fremdschlüssel-Beziehung)
        testBenutzer = BenutzerBuilder.aBenutzer()
                .withUsername("studentuser")
                .withEmail("student@test.de")
                .build();
        benutzerRepository.save(testBenutzer);

        // Student mit Builder erstellen
        testStudent = StudentBuilder.aStudent()
                .withBenutzer(BenutzerBuilder.aBenutzer()
                        .withUsername("studentuser")
                        .withEmail("student@test.de"))
                .withUniversitaet("H-BRS")
                .withSemester(3)
                .build()
                .getStudent();

        // UserId setzen für DB-Beziehung
        testStudent.setUserId(testBenutzer.getUserId());

        testStudent.setStudentId("12345");
        testStudent.setSkillEntities(new HashSet<>());
    }

    //Hilfsmethode um Skillset zu erstellen
    private Set<Skill> createSkillSet(String[] skillNames) {
        Set<Skill> skillSet = new HashSet<>();
        for (String name : skillNames) {
            Optional<Skill> skill = skillRepository.findByName(name);

            // Wenn er existiert, füge ihn hinzu, sonst erstelle ihn (oder wirf einen Fehler)
            skill.ifPresentOrElse(
                    skillSet::add,
                    () -> { throw new RuntimeException("Skill " + name + " nicht in DB gefunden!"); }
            );
        }
        return skillSet;
    }

    @Test
    void testRoundTrippingStudentSpeichernUndLaden() {
        // Act - Speichern
        Student gespeicherterStudent = studentRepository.save(testStudent);

        // Act - Laden
        Optional<Student> geladenerStudent = studentRepository.findById(gespeicherterStudent.getUserId());

        // Assert - Round Trip erfolgreich?
        assertThat(geladenerStudent).isPresent();
        assertThat(geladenerStudent.get().getUserId()).isEqualTo(testStudent.getUserId());
        assertThat(geladenerStudent.get().getUniversitaet()).isEqualTo("H-BRS");
        assertThat(geladenerStudent.get().getSemester()).isEqualTo(3);
        assertThat(geladenerStudent.get().getSkills()).isEmpty();
    }

    /**
     * Student wird gespeichert
     */
    @Test
    void testStudentSpeichern() {
        // Act
        Student gespeicherterStudent = studentRepository.save(testStudent);

        // Assert
        assertThat(gespeicherterStudent).as("Das Repository hat null zurückgegeben!").isNotNull();
        assertThat(gespeicherterStudent.getUserId()).isEqualTo(testBenutzer.getUserId());
        assertThat(gespeicherterStudent.getStudentId()).isEqualTo("12345");
    }

    /**
     * Student nach ID finden
     */
    @Test
    void testStudentNachIdFinden() {
        // Arrange
        studentRepository.save(testStudent);

        // Act
        Optional<Student> gefunden = studentRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getUniversitaet()).isEqualTo("H-BRS");
    }

    /**
     * Prüfen ob Student existiert
     */
    @Test
    void testExistsById() {
        // Arrange
        studentRepository.save(testStudent);

        // Act & Assert - Existiert
        assertThat(studentRepository.existsById(testBenutzer.getUserId())).isTrue();

        // Act & Assert - Existiert nicht
        assertThat(studentRepository.existsById(99999)).isFalse();
    }

    /**
     * Student aktualisieren
     */
    @Test
    void testStudentAktualisieren() {
        // Arrange
        Student gespeicherterStudent = studentRepository.save(testStudent);

        // Act - Daten ändern
        gespeicherterStudent.setUniversitaet("RWTH Aachen");
        gespeicherterStudent.setSemester(5);

        Set<Skill> neueSkills = createSkillSet(new String[]{"Python", "Java"});

        gespeicherterStudent.setSkillEntities(neueSkills);
        Student aktualisierterStudent = studentRepository.save(gespeicherterStudent);

        // Assert - Round Trip: Änderungen persistiert?
        Optional<Student> geladen = studentRepository.findById(aktualisierterStudent.getUserId());
        assertThat(geladen).isPresent();
        assertThat(geladen.get().getUniversitaet()).isEqualTo("RWTH Aachen");
        assertThat(geladen.get().getSemester()).isEqualTo(5);
        assertThat(geladen.get().getSkills()).containsExactlyInAnyOrder("Python", "Java");
    }

    /**
     * Student löschen
     */
    @Test
    void testStudentLoeschen() {
        // Arrange
        studentRepository.save(testStudent);

        // Act - Löschen
        studentRepository.deleteById(testBenutzer.getUserId());

        // Assert - Nicht mehr vorhanden
        Optional<Student> gefunden = studentRepository.findById(testBenutzer.getUserId());
        assertThat(gefunden).isEmpty();
    }

    /**
     * Student mit leeren Skills
     */
    @Test
    void testStudentMitLeerenSkills() {
        // Arrange
        testStudent.setSkillEntities(new HashSet<>());
        studentRepository.save(testStudent);

        // Act
        Optional<Student> gefunden = studentRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getSkills()).isEmpty();
    }

    /**
     * Student mit vielen Skills
     */
    @Test
    void testStudentMitVielenSkills() {
        // Arrange
        String[] existierendeSkills = new String[] {
                "Java", "Python", "JavaScript", "HTML", "CSS", "Spring Boot", "MySQL"
        };
        testStudent.setSkillEntities(createSkillSet(existierendeSkills));
        studentRepository.save(testStudent);

        // Act
        Optional<Student> gefunden = studentRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getSkills()).hasSize(7);
    }

    /**
     * Student mit speziellem Abschluss
     */
    @Test
    void testStudentMitSpeziellemAbschluss() {
        // Arrange
        testStudent.setAbschluss("Master of Science");
        studentRepository.save(testStudent);

        // Act
        Optional<Student> gefunden = studentRepository.findById(testBenutzer.getUserId());

        // Assert
        assertThat(gefunden).isPresent();
        assertThat(gefunden.get().getAbschluss()).isEqualTo("Master of Science");
    }
}