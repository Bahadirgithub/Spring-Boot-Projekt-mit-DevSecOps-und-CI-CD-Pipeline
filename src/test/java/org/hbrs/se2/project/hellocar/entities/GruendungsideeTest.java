package org.hbrs.se2.project.hellocar.entities;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GruendungsideeTest {

    @Test
    void testAlleGetterUndSetter() {
        // Arrange
        Gruendungsidee idee = new Gruendungsidee();
        List<String> skills = Arrays.asList("Java", "Spring", "React");

        // Act - Alle Setter aufrufen
        idee.setIdeeId(1);
        idee.setGruenderUserId(100);
        idee.setBeschreibung("Eine innovative Startup-Idee");
        idee.setPosition("Software Entwickler");
        idee.setAnforderungen("Mind. 3 Jahre Erfahrung");
        idee.setArbeitsbedingungen("Homeoffice möglich");
        idee.setGesuchteSkills(skills);

        // Assert - Alle Getter prüfen
        assertThat(idee.getIdeeId()).isEqualTo(1);
        assertThat(idee.getGruenderUserId()).isEqualTo(100);
        assertThat(idee.getBeschreibung()).isEqualTo("Eine innovative Startup-Idee");
        assertThat(idee.getPosition()).isEqualTo("Software Entwickler");
        assertThat(idee.getAnforderungen()).isEqualTo("Mind. 3 Jahre Erfahrung");
        assertThat(idee.getArbeitsbedingungen()).isEqualTo("Homeoffice möglich");
        assertThat(idee.getGesuchteSkills()).containsExactly("Java", "Spring", "React");
    }

    @Test
    void testEqualsUndHashCode() {
        // Arrange
        Gruendungsidee idee1 = new Gruendungsidee();
        idee1.setIdeeId(1);

        Gruendungsidee idee2 = new Gruendungsidee();
        idee2.setIdeeId(1);

        Gruendungsidee idee3 = new Gruendungsidee();
        idee3.setIdeeId(2);

        // Assert
        // Gleiche ID = gleich
        assertThat(idee1).isEqualTo(idee2);
        assertThat(idee1).hasSameHashCodeAs(idee2);

        // Unterschiedliche ID = ungleich
        assertThat(idee1).isNotEqualTo(idee3);
        assertThat(idee1).isNotEqualTo(null);
        assertThat(idee1).isNotEqualTo("String"); // Anderer Typ
    }

    @Test
    void testGesuchteSkillsLeer() {
        // Arrange
        Gruendungsidee idee = new Gruendungsidee();

        // Act
        idee.setGesuchteSkills(null);

        // Assert
        assertThat(idee.getGesuchteSkills()).isNull();
    }

    @Test
    void testGesuchteSkillsMitMehrerenEintraegen() {
        // Arrange
        Gruendungsidee idee = new Gruendungsidee();
        List<String> skills = Arrays.asList("Python", "Docker", "Kubernetes", "AWS");

        // Act
        idee.setGesuchteSkills(skills);

        // Assert
        assertThat(idee.getGesuchteSkills()).hasSize(4);
        assertThat(idee.getGesuchteSkills()).contains("Python", "AWS");
        assertThat(idee.getGesuchteSkills()).doesNotContain("Java");
    }

    @Test
    void testBeschreibungMitSonderzeichen() {
        // Arrange
        Gruendungsidee idee = new Gruendungsidee();

        // Act
        idee.setBeschreibung("Startup mit KI & ML - 100% Remote! 💻");

        // Assert
        assertThat(idee.getBeschreibung()).contains("KI & ML");
        assertThat(idee.getBeschreibung()).hasSizeGreaterThan(10);
    }

    @Test
    void testGruenderUserIdPositiv() {
        // Arrange
        Gruendungsidee idee = new Gruendungsidee();

        // Act
        idee.setGruenderUserId(999);

        // Assert
        assertThat(idee.getGruenderUserId()).isEqualTo(999);
        assertThat(idee.getGruenderUserId()).isPositive();
    }
}