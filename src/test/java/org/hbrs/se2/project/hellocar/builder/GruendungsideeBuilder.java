package org.hbrs.se2.project.hellocar.builder;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.entities.Skill;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test-Builder für die Erstellung einer vollständigen Gründungsidee inkl. des zugehörigen Gründers.
 * Dieses Pattern ermöglicht die einfache und flexible Erstellung von komplexen Test-Szenarien.
 * Der Builder erzeugt reine Java-Objekte (POJOs), die NICHT automatisch in der Datenbank gespeichert werden.
 * -----------------------------------------------------------------------------------------------------------------
 * ANLEITUNG FÜR TEAMKOLLEGEN:
 * -----------------------------------------------------------------------------------------------------------------
 * BEISPIEL 1: Eine Standard-Idee mit einem Standard-Gründer erstellen
 * {@code
 * TestGruendungsidee standard = GruendungsideeBuilder.anIdea().build();
 * // Zugriff auf die Objekte:
 * // Benutzer benutzer = standard.getBenutzer();
 * // Gruender gruender = standard.getGruender();
 * // Gruendungsidee idee = standard.getIdee();
 * }
 *
 * BEISPIEL 2: Eine Idee mit spezifischen Eigenschaften erstellen
 * {@code
 * TestGruendungsidee customIdea = GruendungsideeBuilder.anIdea()
 *     .withBeschreibung("Brüht den perfekten Kaffee basierend auf biometrischen Daten.")
 *     .withSkills("AI", "IoT", "Java")
 *     .build();
 * }
 *
 * BEISPIEL 3: Eine Idee für einen bereits existierenden, spezifischen Gründer erstellen
 * {@code
 * // Zuerst den Gründer-Teil konfigurieren
 * GruenderBuilder gb = GruenderBuilder.aGruender()
 *     .withUnternehmensname("FutureTech AG")
 *     .withBenutzer(BenutzerBuilder.aBenutzer().withUsername("ceo_jane"));
 *
 * // Dann den Ideen-Builder damit füttern
 * TestGruendungsidee specificIdea = GruendungsideeBuilder.anIdea()
 *     .ownedBy(gb)
 *     .build();
 * }
 * -----------------------------------------------------------------------------------------------------------------
 */
public class GruendungsideeBuilder {

    // Komposition: Wir verwenden den existierenden GruenderBuilder wieder.
    private GruenderBuilder gruenderBuilder = GruenderBuilder.aGruender();

    // Felder, die spezifisch für die Gründungsidee sind, mit sinnvollen Standardwerten.
    private String beschreibung = "Dies ist eine generierte Beschreibung für Testzwecke.";
    private String position = "Full-Stack Entwickler";
    private String anforderungen = "Teamfähigkeit, Neugier, 3+ Jahre Erfahrung in Java";
    private String arbeitsbedingungen = "100% Remote, flexible Arbeitszeiten, 30 Tage Urlaub";
    private Set<String> gesuchteSkills = new HashSet<>(Arrays.asList("Java", "Spring Boot", "Vaadin"));

    /**
     * Statische Factory-Methode für bessere Lesbarkeit im Testcode ("fluent interface").
     * @return Eine neue Instanz des GruendungsideeBuilder.
     */
    public static GruendungsideeBuilder anIdea() {
        return new GruendungsideeBuilder();
    }

    // --- Fluent Setter für die Idee ---

    /**
     * Überschreibt die Standard-Beschreibung der Idee.
     * @param beschreibung Die gewünschte Beschreibung.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder withBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
        return this;
    }

    /**
     * Überschreibt die Standard-Bezeichnung der gesuchten Position.
     * @param position Die gewünschte Positionsbezeichnung.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder withPosition(String position) {
        this.position = position;
        return this;
    }

    /**
     * Überschreibt die Standard-Anforderungen an Bewerber.
     * @param anforderungen Der gewünschte Anforderungstext.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder withAnforderungen(String anforderungen) {
        this.anforderungen = anforderungen;
        return this;
    }

    /**
     * Überschreibt die Standard-Arbeitsbedingungen.
     * @param arbeitsbedingungen Der gewünschte Text zu den Arbeitsbedingungen.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder withArbeitsbedingungen(String arbeitsbedingungen) {
        this.arbeitsbedingungen = arbeitsbedingungen;
        return this;
    }

    /**
     * Ersetzt die Standard-Skills vollständig durch die übergebenen Werte.
     * @param skills Eine variable Anzahl an Skill-Namen.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder withSkills(String... skills) {
        this.gesuchteSkills = new HashSet<>(Arrays.asList(skills));
        return this;
    }

    /**
     * Fügt einen einzelnen Skill zur bestehenden Liste hinzu.
     * @param skill Der hinzuzufügende Skill.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder addSkill(String skill) {
        this.gesuchteSkills.add(skill);
        return this;
    }

    /**
     * Entfernt alle gesuchten Skills.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder withoutSkills() {
        this.gesuchteSkills.clear();
        return this;
    }

    /**
     * "Brücken"-Methode, um einen vorkonfigurierten Gründer-Builder zu injizieren.
     * Dies ist der bevorzugte Weg, um eine Idee einem spezifischen Gründer zuzuordnen.
     * @param gruenderBuilder Der konfigurierte GruenderBuilder.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruendungsideeBuilder ownedBy(GruenderBuilder gruenderBuilder) {
        this.gruenderBuilder = gruenderBuilder;
        return this;
    }

    /**
     * Interne Hilfsmethode, die aus den Skill-Namen (Strings) die Skill-Entitäten baut.
     * @return Ein Set von {@link Skill}-Entitäten.
     */
    private Set<Skill> baueSkillEntities() {
        return this.gesuchteSkills.stream()
                .map(skillName -> {
                    Skill skill = new Skill();
                    skill.setName(skillName.trim());
                    return skill;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Baut die {@link Gruendungsidee}, den {@link Gruender} und den {@link Benutzer} aus den konfigurierten Werten.
     * @return Ein {@link TestGruendungsidee}-Objekt, das alle drei ungespeicherten Objekte enthält.
     */
    public TestGruendungsidee build() {
        // 1. Delegiere die Erstellung des Gründers (und dessen Benutzers) an den inneren GruenderBuilder.
        GruenderBuilder.TestGruender testGruender = this.gruenderBuilder.build();
        Benutzer benutzer = testGruender.getBenutzer();
        Gruender gruender = testGruender.getGruender();

        // 2. Erstelle das Gruendungsidee-Objekt.
        Gruendungsidee idee = new Gruendungsidee();
        idee.setBeschreibung(this.beschreibung);
        idee.setPosition(this.position);
        idee.setAnforderungen(this.anforderungen);
        idee.setArbeitsbedingungen(this.arbeitsbedingungen);
        idee.setGesuchteSkillEntities(baueSkillEntities());

        // WICHTIG: Die Verknüpfung der Objekte.
        // In einem echten Szenario würde der Benutzer gespeichert, seine ID würde dem Gründer zugewiesen,
        // der Gründer würde gespeichert, und seine ID würde dann der Idee zugewiesen.
        // Für POJO-Tests setzen wir die ID hier *nicht* manuell, da sie 0 wäre.
        // Die Objekt-Referenz ist für die meisten Unit-Tests ausreichend.
        // idee.setGruenderUserId(gruender.getUserId()); // <-- Dies wäre irreführend, da die ID nicht echt ist.

        // 4. Gib alle drei erstellten Objekte in einem sauberen Container zurück.
        return new TestGruendungsidee(benutzer, gruender, idee);
    }

    /**
     * Container-Klasse, um das Ergebnis des Builders (Benutzer, Gründer und Idee) sauber zurückzugeben.
     */
    public static class TestGruendungsidee {
        private final Benutzer benutzer;
        private final Gruender gruender;
        private final Gruendungsidee idee;

        public TestGruendungsidee(Benutzer benutzer, Gruender gruender, Gruendungsidee idee) {
            this.benutzer = benutzer;
            this.gruender = gruender;
            this.idee = idee;
        }

        public Benutzer getBenutzer() {
            return benutzer;
        }

        public Gruender getGruender() {
            return gruender;
        }

        public Gruendungsidee getIdee() {
            return idee;
        }
    }
}
