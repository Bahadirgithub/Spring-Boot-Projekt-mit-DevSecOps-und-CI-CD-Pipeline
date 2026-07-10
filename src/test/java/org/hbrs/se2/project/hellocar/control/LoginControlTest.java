package org.hbrs.se2.project.hellocar.control;


import org.hbrs.se2.project.hellocar.builder.BenutzerBuilder;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.control.exception.PasswordInvalidException;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.util.CryptoProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstests für die {@link LoginControl}.
 * Diese Klasse testet die Logik der Benutzeranmeldung.
 * Die Datenbank wird durch @Transactional nach jedem Test automatisch zurückgesetzt.
 * Die Testdaten werden über den {@link BenutzerBuilder} erstellt, um eine saubere,
 * explizite und wartbare Test-Vorbereitung zu gewährleisten.
 */
@SpringBootTest
@Transactional
@DisplayName("Integrationstests für die LoginControl")
public class LoginControlTest {

    @Autowired
    private LoginControl loginControl;

    @Autowired
    private BenutzerRepository benutzerRepository;

    private Benutzer standardBenutzer;
    private static final String KLARTEXT_PASSWORT = "SicheresPasswort123";

    /**
     * Bereitet vor jedem Test einen Standard-Benutzer vor.
     * 1. Erstellt ein Benutzer-Objekt im Speicher mit dem {@link BenutzerBuilder}.
     * 2. Hashes the plain text password, as the LoginControl expects a hashed password in the database.
     * 3. Speichert den vorbereiteten Benutzer in der Datenbank.
     */
    @BeforeEach
    void setup() {
        // Erzeuge einen Benutzer mit dem Builder
        standardBenutzer = BenutzerBuilder.aBenutzer()
                .withUsername("login_user_ok")
                .withPasswort(CryptoProvider.CreatePassword(KLARTEXT_PASSWORT)) // Passwort direkt hashen
                .build();

        // Speichere den vorbereiteten Benutzer in der DB
        benutzerRepository.saveAndFlush(standardBenutzer);
    }

    @Test
    @DisplayName("Authentifizierung mit korrekten Daten ist erfolgreich.")
    void authenticate_success() throws PasswordInvalidException, DatabaseUserException {
        // Führe die Authentifizierung mit dem korrekten Klartext-Passwort durch
        var dto = loginControl.authenticate(standardBenutzer.getUsername(), KLARTEXT_PASSWORT);

        // Überprüfe, ob das zurückgegebene DTO korrekt ist
        assertNotNull(dto);
        assertEquals(standardBenutzer.getUsername(), dto.getUsername());
        assertEquals(standardBenutzer.getUserId(), dto.getUserId());
    }

    @Test
    @DisplayName("Authentifizierung mit falschem Passwort wirft PasswordInvalidException.")
    void authenticate_wrongPassword_throwsPasswordInvalidException() {
        // Erwarte eine Exception, wenn ein falsches Passwort verwendet wird
        assertThrows(PasswordInvalidException.class, () ->
                loginControl.authenticate(standardBenutzer.getUsername(), "falschesPasswort"));
    }

    @Test
    @DisplayName("Authentifizierung mit unbekanntem Benutzer wirft DatabaseUserException.")
    void authenticate_unknownUser_throwsDatabaseUserException() {
        // Erwarte eine Exception, wenn der Benutzername nicht existiert
        assertThrows(DatabaseUserException.class, () ->
                loginControl.authenticate("nicht_existierender_user_123", "any"));
    }

    @Test
    @DisplayName("Passwort-Reset-Prüfung mit korrekten Daten gibt true zurück.")
    void checkResetCredentials_success_returnsTrue() throws DatabaseUserException {
        // Vorbereitung: Erstelle einen spezifischen Benutzer für diesen Testfall
        Benutzer resetUser = BenutzerBuilder.aBenutzer()
                .withUsername("reset_user")
                .withEmail("reset@mail.de")
                .build();
        benutzerRepository.save(resetUser);

        // Führe die Prüfung durch
        boolean result = loginControl.checkResetCredentials(resetUser.getUsername(), resetUser.getEmail());

        // Überprüfe das Ergebnis
        assertTrue(result);
    }

    @Test
    @DisplayName("Passwort-Reset-Prüfung mit unbekanntem Benutzer wirft DatabaseUserException.")
    void checkResetCredentials_unknownUser_throwsDatabaseUserException() {
        // Erwarte eine Exception, wenn der Benutzer für den Reset nicht existiert
        assertThrows(DatabaseUserException.class, () ->
                loginControl.checkResetCredentials("nonexistent_user_x", "noemail@x.de"));
    }
}