package org.hbrs.se2.project.hellocar.builder;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import java.time.LocalDate;

/**
 * Test-Builder für die {@link Benutzer}-Entität.
 * Dieses Pattern ermöglicht die einfache und flexible Erstellung von Test-Objekten für Unit- und Integrationstests.
 * Der Builder erzeugt ein reines Java-Objekt (POJO), das NICHT automatisch in der Datenbank gespeichert wird.
 * -----------------------------------------------------------------------------------------------------------------
 * ANLEITUNG FÜR TEAMKOLLEGEN:
 * -----------------------------------------------------------------------------------------------------------------
 * WANN IST DIESER BUILDER ZU VERWENDEN?
 * Immer dann, wenn ihr ein {@link Benutzer}-Objekt für einen Test benötigt, insbesondere wenn ihr
 * nur bestimmte Eigenschaften für euren Testfall anpassen müsst.
 * BEISPIEL 1: Einen Standard-Benutzer erstellen
 * Ein Benutzer mit validen, aber unspezifischen Daten.
 * {@code
 * Benutzer standardBenutzer = BenutzerBuilder.aBenutzer().build();
 * }
 *
 * BEISPIEL 2: Einen Benutzer mit spezifischen Eigenschaften erstellen
 * Nützlich, um gezielte Szenarien zu testen (z.B. Login mit einem bestimmten Usernamen).
 * {@code
 * Benutzer testBenutzer = BenutzerBuilder.aBenutzer()
 *     .withUsername("testuser123")
 *     .withPasswort("geheimesPasswort")
 *     .build();
 * }
 *
 * BEISPIEL 3: Einen Benutzer für einen Mocking-Test mit einer festen ID erstellen
 * {@code
 * Benutzer mockBenutzer = BenutzerBuilder.aBenutzer()
 *     .withUserId(123)
 *     .build();
 * }
 *
 * UNTERSCHIED ZUR TestDatenFabrik:
 * - {@link BenutzerBuilder} (dieser hier): Erstellt ein Java-Objekt im Speicher. Flexibel und schnell. Ideal für Unit-Tests.
 * - {@code TestDatenFabrik}: Erstellt und speichert eine Entität direkt in der Datenbank. Ideal für Integrationstests,
 *   bei denen ein in der DB existierendes Objekt benötigt wird.
 * Beide ergänzen sich und sollten je nach Testanforderung gewählt werden.
 * -----------------------------------------------------------------------------------------------------------------
 */
public class BenutzerBuilder {

    // Standardwerte für einen "perfekten" Benutzer, um Tests kurz und lesbar zu halten.
    private Integer userId = null; // Standardmäßig keine ID, da sie von der DB kommt.
    private String vorname = "Max";
    private String nachname = "Mustermann";
    private String username = "standard_Max1";
    private String passwort = "SicheresPasswort123!";
    private String email = "max.mustermann@example.com";
    private LocalDate geburtstag = LocalDate.of(1990, 1, 1);
    private String telefonnummer = "0123456789";

    /**
     * Statische Factory-Methode für bessere Lesbarkeit im Testcode.
     * Startpunkt für jeden Build-Vorgang.
     *
     * @return Eine neue Instanz des BenutzerBuilders.
     */
    public static BenutzerBuilder aBenutzer() {
        return new BenutzerBuilder();
    }

    /**
     * Setzt die User-ID. WICHTIG: Nur für Test-Szenarien verwenden,
     * in denen eine ID für Mocking-Zwecke benötigt wird.
     * @param userId Die zu setzende User-ID.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withUserId(Integer userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Überschreibt den Standard-Vornamen.
     * @param vorname Der gewünschte Vorname.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withVorname(String vorname) {
        this.vorname = vorname;
        return this;
    }

    /**
     * Überschreibt den Standard-Nachnamen.
     * @param nachname Der gewünschte Nachname.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withNachname(String nachname) {
        this.nachname = nachname;
        return this;
    }

    /**
     * Überschreibt den Standard-Benutzernamen.
     * @param username Der gewünschte Benutzername.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Überschreibt das Standard-Passwort.
     * @param passwort Das gewünschte Passwort.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withPasswort(String passwort) {
        this.passwort = passwort;
        return this;
    }

    /**
     * Überschreibt die Standard-E-Mail-Adresse.
     * @param email Die gewünschte E-Mail-Adresse.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Überschreibt das Standard-Geburtsdatum.
     * @param geburtstag Das gewünschte Geburtsdatum.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withGeburtstag(LocalDate geburtstag) {
        this.geburtstag = geburtstag;
        return this;
    }

    /**
     * Überschreibt die Standard-Telefonnummer.
     * @param telefonnummer Die gewünschte Telefonnummer.
     * @return Der Builder selbst, um weitere Aufrufe zu verketten.
     */
    public BenutzerBuilder withTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
        return this;
    }

    /**
     * Baut das {@link Benutzer}-Objekt aus den konfigurierten Werten.
     * Diese Methode schließt den Build-Prozess ab und gibt das finale Objekt zurück.
     *
     * @return Ein neues {@link Benutzer}-Objekt mit den gesetzten Attributen.
     */
    public Benutzer build() {
        Benutzer benutzer = new Benutzer();
        // Setze die ID nur, wenn sie explizit im Test angegeben wurde.
        if (this.userId != null) {
            benutzer.setUserId(this.userId);
        }
        benutzer.setVorname(this.vorname);
        benutzer.setNachname(this.nachname);
        benutzer.setUsername(this.username);
        benutzer.setPasswort(this.passwort);
        benutzer.setEmail(this.email);
        benutzer.setGeburtstag(this.geburtstag);
        benutzer.setTelefonnummer(this.telefonnummer);
        return benutzer;
    }
}
