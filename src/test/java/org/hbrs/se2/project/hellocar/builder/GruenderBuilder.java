package org.hbrs.se2.project.hellocar.builder;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;

/**
 * Test-Builder für die Erstellung eines vollständigen Gründers (Benutzer + Gründer-Rolle).
 * Dieses Pattern ermöglicht die einfache und flexible Erstellung von Test-Objekten für Unit-Tests.
 * Der Builder erzeugt reine Java-Objekte (POJOs), die NICHT automatisch in der Datenbank gespeichert werden.
  * -----------------------------------------------------------------------------------------------------------------
 * ANLEITUNG FÜR TEAMKOLLEGEN:
 * -----------------------------------------------------------------------------------------------------------------
 * WANN IST DIESER BUILDER ZU VERWENDEN?
 * Immer dann, wenn ihr ein Gründer-Szenario für einen Unit-Test benötigt, bei dem keine
 * echte Datenbank-Interaktion erforderlich ist (z.B. Testen von Controllern mit gemockten Repositories).
 * BEISPIEL 1: Einen Standard-Gründer (inkl. Standard-Benutzer) erstellen
 * {@code
 * TestGruender standard = GruenderBuilder.aGruender().build();
 * // Zugriff auf die Objekte:
 * // Benutzer benutzer = standard.getBenutzer();
 * // Gruender gruender = standard.getGruender();
 * }
 * BEISPIEL 2: Einen Gründer mit spezifischen Eigenschaften erstellen
 * {@code
 * TestGruender testGruender = GruenderBuilder.aGruender()
 *     .withGruenderId("g_my_test_id")
 *     .withUnternehmensname("Next Unicorn GmbH")
 *     .withStandort("Sankt Augustin")
 *     .build();
 * }
 * BEISPIEL 3: Einen Gründer mit einem spezifischen Benutzer-Setup erstellen
 * {@code
 * // Zuerst den Benutzer-Teil konfigurieren
 * BenutzerBuilder bb = BenutzerBuilder.aBenutzer().withUsername("startup_joe");
 * // Dann den Gründer-Builder damit füttern
 * TestGruender custom = GruenderBuilder.aGruender().withBenutzer(bb).build();
 * }
 *
 * UNTERSCHIED ZUR TestDatenFabrik:
 * - GruenderBuilder (dieser hier): Erstellt Java-Objekte im Speicher. Flexibel und schnell. Ideal für Unit-Tests.
 * - TestDatenFabrik: Erstellt und speichert Entitäten direkt in der Datenbank. Ideal für Integrationstests.
 * KI erstellte Doku
 * -----------------------------------------------------------------------------------------------------------------
 */
public class GruenderBuilder {

    // ** KOMPLEXER PUNKT 1: Komposition statt Vererbung/Kopieren **
    // Anstatt alle Benutzer-Felder hier zu duplizieren, halten wir eine Instanz des
    // bereits existierenden BenutzerBuilders. Das nennt man Komposition.
    // VORTEIL: Wenn sich der Benutzer ändert (z.B. neues Feld), müssen wir nur den
    // BenutzerBuilder anpassen. Dieser GruenderBuilder profitiert automatisch davon.
    // Das ist extrem wartungsfreundlich und folgt dem DRY-Prinzip (Don't Repeat Yourself).
    private BenutzerBuilder benutzerBuilder = BenutzerBuilder.aBenutzer();

    // Nur die Felder, die spezifisch für die Gründer-Rolle sind.
    private String gruenderId = "g_test_456";
    private String unternehmensname = "Test-Startup GmbH";
    private String standort = "Bonn";

    /**
     * Statische Factory-Methode für bessere Lesbarkeit im Testcode.
     * Startpunkt für jeden Build-Vorgang.
     *
     * @return Eine neue Instanz des GruenderBuilders.
     */
    public static GruenderBuilder aGruender() {
        return new GruenderBuilder();
    }

    /**
     * Überschreibt die system-interne, fachliche Gründer-ID.
     * @param gruenderId Die gewünschte ID für den Testfall.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruenderBuilder withGruenderId(String gruenderId) {
        this.gruenderId = gruenderId;
        return this;
    }

    /**
     * Überschreibt den Standard-Unternehmensnamen.
     * @param unternehmensname Der gewünschte Unternehmensname.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten (Fluent Interface).
     */
    public GruenderBuilder withUnternehmensname(String unternehmensname) {
        this.unternehmensname = unternehmensname;
        return this;
    }

    /**
     * Überschreibt den Standard-Standort.
     * @param standort Der gewünschte Standort.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruenderBuilder withStandort(String standort) {
        this.standort = standort;
        return this;
    }

    /**
     * ** KOMPLEXER PUNKT 2: Die "Brücken"-Methode **
     * Diese Methode ist der Schlüssel zur Flexibilität. Sie erlaubt es, von außen einen
     * komplett vorkonfigurierten BenutzerBuilder zu "injizieren".
     * So bleibt die Verantwortung für den Benutzer beim BenutzerBuilder.
     *
     * @param benutzerBuilder Der konfigurierte BenutzerBuilder.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public GruenderBuilder withBenutzer(BenutzerBuilder benutzerBuilder) {
        this.benutzerBuilder = benutzerBuilder;
        return this;
    }

    /**
     * Baut die {@link Gruender}- und {@link Benutzer}-Objekte aus den konfigurierten Werten.
     * Diese Methode schließt den Build-Prozess ab und gibt eine Container-Klasse zurück.
     *
     * @return Ein {@link TestGruender}-Objekt, das den ungespeicherten Benutzer und Gründer enthält.
     */
    public TestGruender build() {
        // 1. Delegiere die Erstellung des Benutzers an den inneren BenutzerBuilder.
        Benutzer benutzer = this.benutzerBuilder.build();

        // 2. Erstelle das Gründer-Objekt.
        Gruender gruender = new Gruender();

        // 3. Befülle das Gründer-Objekt mit den spezifischen Werten dieses Builders.
        gruender.setGruenderId(this.gruenderId);
        gruender.setUnternehmensname(this.unternehmensname);
        gruender.setStandort(this.standort);

        // HINWEIS: Die Verknüpfung (gruender.setUserId(...)) findet hier bewusst nicht statt,
        // da der Benutzer noch keine ID von der Datenbank hat. Das ist für reine
        // POJO-Tests (ohne DB) das korrekte Verhalten.

        // 4. Gib die beiden erstellten Objekte in einem sauberen Container zurück.
        return new TestGruender(benutzer, gruender);
    }

    /**
     * ** KOMPLEXER PUNKT 3: Die Container-Klasse (oder "Transporter") **
     * Eine Methode kann in Java nur einen Wert zurückgeben. Um sowohl den Benutzer als auch
     * den Gründer sauber zurückzugeben, packen wir sie in diese kleine, statische innere Klasse.
     * VORTEIL: Das ist typsicher. Der Aufrufer kann mit `result.getBenutzer()` und
     * `result.getGruender()` arbeiten und muss nicht mit unsicheren `Object[]`-Arrays oder Maps hantieren.
     */
    public static class TestGruender {
        private final Benutzer benutzer;
        private final Gruender gruender;

        public TestGruender(Benutzer benutzer, Gruender gruender) {
            this.benutzer = benutzer;
            this.gruender = gruender;
        }

        public Benutzer getBenutzer() {
            return benutzer;
        }

        public Gruender getGruender() {
            return gruender;
        }
    }
}
