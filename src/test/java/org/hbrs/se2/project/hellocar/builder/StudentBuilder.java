package org.hbrs.se2.project.hellocar.builder;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Student;

import java.util.Collections;

/**
 * Test-Builder für die Erstellung eines vollständigen Studenten (Benutzer + Student-Rolle).
 * Dieses Pattern ermöglicht die einfache und flexible Erstellung von Test-Objekten für Unit-Tests.
 * Der Builder erzeugt reine Java-Objekte (POJOs), die NICHT automatisch in der Datenbank gespeichert werden.
 * -----------------------------------------------------------------------------------------------------------------
 * ANLEITUNG FÜR TEAMKOLLEGEN:
 * -----------------------------------------------------------------------------------------------------------------
 * WANN IST DIESER BUILDER ZU VERWENDEN?
 * Immer dann, wenn ihr ein Studenten-Szenario für einen Unit-Test benötigt, bei dem keine
 * echte Datenbank-Interaktion erforderlich ist.
 * BEISPIEL 1: Einen Standard-Studenten (inkl. Standard-Benutzer) erstellen
 * {@code
 * TestStudent standard = StudentBuilder.aStudent().build();
 * // Zugriff auf die Objekte:
 * // Benutzer benutzer = standard.getBenutzer();
 * // Student student = standard.getStudent();
 * }
 *
 * BEISPIEL 2: Einen Studenten mit spezifischen Eigenschaften erstellen
 * {@code
 * TestStudent testStudent = StudentBuilder.aStudent()
 *     .withUniversitaet("RWTH Aachen")
 *     .withSemester(3)
 *     .build();
 * }
 *
 * BEISPIEL 3: Einen Studenten mit einem spezifischen Benutzer-Setup erstellen
 * {@code
 * // Zuerst den Benutzer-Teil konfigurieren
 * BenutzerBuilder bb = BenutzerBuilder.aBenutzer().withUsername("student_jane");
 * // Dann den Studenten-Builder damit füttern
 * TestStudent custom = StudentBuilder.aStudent().withBenutzer(bb).build();
 * }
 * Ki-erstellte Doku
 * -----------------------------------------------------------------------------------------------------------------
 */
public class StudentBuilder {

    // Komposition: Wir verwenden den existierenden BenutzerBuilder wieder.
    private BenutzerBuilder benutzerBuilder = BenutzerBuilder.aBenutzer();

    // Nur die Felder, die spezifisch für die Student-Rolle sind.
    private String universitaet = "H-BRS";
    private int semester = 5;
    private String skills = "Java, Spring, JPA";
    private String abschluss = "Bachelor";

    /**
     * Statische Factory-Methode für bessere Lesbarkeit im Testcode.
     * @return Eine neue Instanz des StudentBuilders.
     */
    public static StudentBuilder aStudent() {
        return new StudentBuilder();
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
     * Überschreibt die Standard-Skills.
     * @param skills Die gewünschten Skills.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withSkills(String skills) {
        this.skills = skills;
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
     * "Brücken"-Methode, um einen vorkonfigurierten BenutzerBuilder zu injizieren.
     * @param benutzerBuilder Der konfigurierte BenutzerBuilder.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public StudentBuilder withBenutzer(BenutzerBuilder benutzerBuilder) {
        this.benutzerBuilder = benutzerBuilder;
        return this;
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
        student.setUniversitaet(this.universitaet);
        student.setSemester(this.semester);
        student.setSkills(Collections.singletonList(this.skills));
        student.setAbschluss(this.abschluss);

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