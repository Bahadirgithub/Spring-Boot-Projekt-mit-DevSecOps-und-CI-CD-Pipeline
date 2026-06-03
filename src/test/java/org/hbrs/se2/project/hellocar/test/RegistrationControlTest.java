package org.hbrs.se2.project.hellocar.test;

import org.hbrs.se2.project.hellocar.control.RegistrationControl;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit-Testfälle zur Benutzerregistrierung.
 * Diese Klasse testet die Validierungslogik der RegistrationControl.
 * Die Datenbank wird durch @Transactional nach jedem Test automatisch zurückgesetzt.
 */
@SpringBootTest
@Transactional
public class RegistrationControlTest {

    @Autowired
    private RegistrationControl registrationControl;

    @Autowired
    private BenutzerRepository benutzerRepository;

    /**
     * Testfall 1: Positivtest - Alle Registrierungsdaten sind valide und der Username ist frei.
     */
    @Test
    void testRegistrierungErfolgreich() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Mustermann", LocalDate.of(1999, 1, 1), 
                "startup_founder_1", "test@mail.de", "Passwort123", "Uni Bonn", 1
        );
        assertTrue(result, "Registrierung sollte erfolgreich sein.");
        
        // TODO: Rückgabetyp verbessern. Statt eines boolean sollte ein DTO oder Result-Objekt (z.B. RegistrationResult) 
        // zurückgegeben werden, um im Erfolgsfall auch direkt die Benutzer-ID oder ähnliches für das UI verfügbar zu haben.
    }

    /**
     * Testfall 2: Negativtest - Username existiert bereits in der Datenbank (Kollisionsprüfung).
     */
    @Test
    void testRegistrierungSchlaegtFehlWennUsernameVergeben() {
        // Vorbereitung: Einen Benutzer anlegen, damit der Name vergeben ist.
        registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "stud_max", "erste_mail@mail.de", "Passwort123", "Uni Bonn", 1
        );

        // Eigentlicher Test: Versuch, den gleichen Namen nochmal zu nutzen.
        boolean result = registrationControl.registriereStudent(
                "Anna", "Schmidt", LocalDate.now(), "stud_max", "andere_mail@mail.de", "Passwort123", "Uni Köln", 2
        );
        assertFalse(result, "Registrierung sollte fehlschlagen, da 'stud_max' bereits existiert.");
        
        // TODO: Exception-Handling einbauen. Aktuell gibt die Methode nur 'false' zurück. Besser wäre es, hier eine 
        // spezifische Exception wie 'UsernameAlreadyExistsException' zu werfen, damit das Frontend (Vaadin) 
        // weiß, WELCHER Fehler aufgetreten ist und eine passende Meldung ("Benutzername bereits vergeben") anzeigen kann.
    }

    /**
     * Testfall 3: Positivtest - E-Mail existiert bereits, aber der Username ist frei.
     */
    @Test
    void testRegistrierungErfolgreichTrotzDoppelterEmail() {
        // Vorbereitung: Benutzer mit einer bestimmten E-Mail anlegen.
        registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "user_1", "test@mail.de", "Passwort123", "Uni Bonn", 1
        );

        // Eigentlicher Test: Anderer Username, aber GLEICHE E-Mail.
        boolean result = registrationControl.registriereStudent(
                "Anna", "Schmidt", LocalDate.now(), "neuer_user_2", "test@mail.de", "SicheresPasswort99", "Uni Köln", 2
        );
        assertTrue(result, "Registrierung sollte erfolgreich sein, da doppelte E-Mails laut Anforderung erlaubt sind.");
    }

    /**
     * Testfall 4: Negativtest - Das Passwort unterschreitet die Mindestlänge von 8 Zeichen.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennPasswortZuKurz() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "neuer_user_3", "user3@mail.de", "123", "Uni Bonn", 1
        );
        
        assertFalse(result, "Registrierung sollte blockiert werden, wenn das Passwort kürzer als 8 Zeichen ist.");
        
        // TODO: Auch hier sollte im Produktivcode idealerweise eine ValidationException geworfen 
        // werden (z. B. "Passwort zu kurz"). Aktuell gibt der Test false zurück, weil wir das im Code implementieren werden.
    }

    /**
     * Testfall 5: Negativtest - Die E-Mail-Adresse besitzt ein ungültiges Format.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennEmailUngueltig() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "neuer_user_4", "testmail", "Passwort123", "Uni Bonn", 1
        );
        
        assertFalse(result, "Registrierung sollte bei ungültigem E-Mail-Format fehlschlagen.");
    }

    /**
     * Testfall 6: Negativtest - Das Passwortfeld wurde leer übergeben.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennPasswortLeer() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "neuer_user_5", "user5@mail.de", "", "Uni Bonn", 1
        );
        
        assertFalse(result, "Registrierung sollte bei leerem Passwort fehlschlagen.");
    }

    /**
     * Testfall 7: Negativtest - Der Benutzername wurde leer übergeben.
     */
    @Test
    void testRegistrierungSchlaegtFehlWennUsernameLeer() {
        boolean result = registrationControl.registriereStudent(
                "Max", "Muster", LocalDate.now(), "", "user6@mail.de", "Passwort123", "Uni Bonn", 1
        );
        
        assertFalse(result, "Registrierung sollte bei leerem Benutzernamen fehlschlagen.");
        
        // TODO: Die Validierung sollte GANZ AM ANFANG der Methode 'registriereStudent'
        // stehen, noch bevor irgendwelche Datenbankabfragen (wie 'existsByUsername') gemacht werden.
    }
}