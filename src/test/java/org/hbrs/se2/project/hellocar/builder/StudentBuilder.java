package org.hbrs.se2.project.hellocar.builder;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Skill;
import org.hbrs.se2.project.hellocar.entities.Student;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test-Builder für die Erstellung eines vollständigen Studenten (Benutzer + Student-Rolle).
 * Dieses Pattern ermöglicht die einfache und flexible Erstellung von Test-Objekten für Unit-Tests.
 * Der Builder erzeugt reine Java-Objekte (POJOs), die NICHT automatisch in der Datenbank gespeichert werden.
 * Dieser Builder ist darauf ausgelegt, die 'TestDataFabrik' schrittweise zu ersetzen, indem er eine
 * flexible, lesbare und wartbare Alternative zur Testdatenerzeugung bietet.
 * -----------------------------------------------------------------------------------------------------------------
 * ANLEITUNG FÜR TEAMKOLLEGEN:
 * -----------------------------------------------------------------------------------------------------------------
 * BEISPIEL 1: Einen Standard-Studenten erstellen
 * {@code
 * TestStudent standard = StudentBuilder.aStudent().build();
 * }
 *
 * BEISPIEL 2: Einen Studenten mit spezifischen Eigenschaften erstellen
 * {@code
 * TestStudent testStudent = StudentBuilder.aStudent()
 *     .withUniversitaet("RWTH Aachen")
 *     .withSemester(3)
 *     .withSkills("Python", "Machine Learning")
 *     .addSkill("TensorFlow")
 *     .build();
 * }
 *
 * BEISPIEL 3: Einen Studenten ohne Skills erstellen
 * {@code
 * TestStudent beginner = StudentBuilder.aStudent().withoutSkills().build();
 * }
 *
 * BEISPIEL 4: Einen Studenten mit einem spezifischen Benutzer-Setup erstellen
 * {@code
 * BenutzerBuilder bb = BenutzerBuilder.aBenutzer().withUsername("student_jane");
 * TestStudent custom = StudentBuilder.aStudent().withBenutzer(bb).build();
 * }
 * -----------------------------------------------------------------------------------------------------------------
 */
public class StudentBuilder {

    // Komposition: Wir verwenden den existierenden BenutzerBuilder wieder.
    private BenutzerBuilder benutzerBuilder = BenutzerBuilder.aBenutzer();

    // Felder, die spezifisch für die Student-Rolle sind, mit sinnvollen Standardwerten.
    private String studentId = "s_test_123";
    private String universitaet = "H-BRS";
    private int semester = 5;
    private String abschluss = "Bachelor";
    private Set<String> skills = new HashSet<>(Arrays.asList("Java", "Spring", "JPA"));

    /**
     * Statische Factory-Methode für bessere Lesbarkeit im Testcode ("fluent interface").
     * @return Eine neue Instanz des StudentBuilders.
     */
    public static StudentBuilder aStudent() {
        return new StudentBuilder();
    }

    /**
     * Überschreibt die system-interne, fachliche Studenten-ID.
     * @param studentId Die gewünschte ID für den Testfall.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withStudentId(String studentId) {
        this.studentId = studentId;
        return this;
    }

    /**
     * Überschreibt die Standard-Universität.
     * @param universitaet Die gewünschte Universität.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withUniversitaet(String universitaet) {
        this.universitaet = universitaet;
        return this;
    }

    /**
     * Überschreibt das Standard-Semester.
     * @param semester Das gewünschte Semester.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withSemester(int semester) {
        this.semester = semester;
        return this;
    }

    /**
     * Überschreibt den Standard-Abschluss.
     * @param abschluss Der gewünschte Abschluss.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withAbschluss(String abschluss) {
        this.abschluss = abschluss;
        return this;
    }

    /**
     * Ersetzt die Standard-Skills vollständig durch die übergebenen Werte.
     * @param skills Eine variable Anzahl an Skill-Namen.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withSkills(String... skills) {
        this.skills = new HashSet<>(Arrays.asList(skills));
        return this;
    }

    /**
     * Fügt einen einzelnen Skill zur bestehenden Liste hinzu.
     * @param skill Der hinzuzufügende Skill.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder addSkill(String skill) {
        this.skills.add(skill);
        return this;
    }

    /**
     * Entfernt alle Skills, um einen Studenten ohne Vorkenntnisse zu erstellen.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withoutSkills() {
        this.skills.clear();
        return this;
    }

    /**
     * "Brücken"-Methode, um einen vorkonfigurierten BenutzerBuilder zu injizieren.
     * @param benutzerBuilder Der konfigurierte BenutzerBuilder.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withBenutzer(BenutzerBuilder benutzerBuilder) {
        this.benutzerBuilder = benutzerBuilder;
        return this;
    }

    /**
     * Interne Hilfsmethode, die aus den Skill-Namen (Strings) die Skill-Entitäten baut.
     * @return Ein Set von {@link Skill}-Entitäten.
     */
    private Set<Skill> baueSkillEntities() {
        // Stream über die Namen, erstelle für jeden eine neue Skill-Entität und sammle sie in einem Set.
        return this.skills.stream()
                .map(skillName -> {
                    Skill skill = new Skill();
                    skill.setName(skillName.trim());
                    return skill;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Baut die {@link Student}- und {@link Benutzer}-Objekte aus den konfigurierten Werten.
     * @return Ein {@link TestStudent}-Objekt, das den ungespeicherten Benutzer und Studenten enthält.
     */
    public TestStudent build() {
        // 1. Delegiere die Erstellung des Benutzers an den inneren BenutzerBuilder.
        Benutzer benutzer = this.benutzerBuilder.build();

        // 2. Erstelle das Student-Objekt.
        Student student = new Student();

        // 3. Befülle das Student-Objekt mit den spezifischen Werten dieses Builders.
        student.setStudentId(this.studentId);
        student.setUniversitaet(this.universitaet);
        student.setSemester(this.semester);
        student.setAbschluss(this.abschluss);
        student.setSkillEntities(baueSkillEntities());

        // WICHTIG: Die user_id wird hier bewusst NICHT gesetzt.
        // In einem echten Szenario würde der Benutzer zuerst gespeichert, wodurch die DB eine ID generiert.
        // Diese ID würde dann dem Studentenobjekt zugewiesen. Für reine Unit-Tests ist dies oft nicht nötig.
        // student.setUserId(benutzer.getId());

        // 4. Gib die beiden erstellten Objekte in einem sauberen Container zurück.
        return new TestStudent(benutzer, student);
    }

    /**
     * Container-Klasse, um das Ergebnis des Builders (Benutzer und Student) sauber zurückzugeben.
     */
    public static class TestStudent {
        private final Benutzer benutzer;
        private final Student student;

        public TestStudent(Benutzer benutzer, Student student) {
            this.benutzer = benutzer;
            this.student = student;
        }

        public Benutzer getBenutzer() {
            return benutzer;
        }

        public Student getStudent() {
            return student;
        }
    }
}
