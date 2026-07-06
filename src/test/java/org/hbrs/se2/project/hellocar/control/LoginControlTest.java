package org.hbrs.se2.project.hellocar.control;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.control.exception.PasswordInvalidException;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.util.CryptoProvider;
import org.hbrs.se2.project.hellocar.util.TestDatenFabrik;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit-Testfälle zur Benutzeranmeldung.
 * Diese Klasse testet die Logik der LoginControl.
 * Die Datenbank wird durch @Transactional nach jedem Test automatisch zurückgesetzt.
 * Die Testdaten werden über die TestDatenFabrik erzeugt, um eine saubere
 * Trennung von der RegistrationControl zu gewährleisten.
 */
@SpringBootTest
@Transactional
public class LoginControlTest {

    @Autowired
    private LoginControl loginControl;

    @Autowired
    private TestDatenFabrik testDatenFabrik;

    @Autowired
    private BenutzerRepository benutzerRepository;

    private Benutzer fabriksUser;

    @BeforeEach
    void setup() {
        // Erzeuge einen persistierten Benutzer über die Fabrik.
        fabriksUser = testDatenFabrik.erstelleUndSpeichereBenutzer("loginOK");

        // Fabrik speichert ein Klartext-Passwort; für LoginControl muss es gehasht vorliegen.
        fabriksUser.setPasswort(CryptoProvider.CreatePassword("SicheresPasswort123"));
        benutzerRepository.saveAndFlush(fabriksUser);
    }

    @Test
    void authenticate_success_withTestDatenFabrik() throws PasswordInvalidException, DatabaseUserException {
        var dto = loginControl.authenticate(fabriksUser.getUsername(), "SicheresPasswort123");
        assertNotNull(dto);
        assertEquals(fabriksUser.getUsername(), dto.getUsername());
        assertEquals(fabriksUser.getUserId(), dto.getUserId());
    }

    @Test
    void authenticate_wrongPassword_throwsPasswordInvalidException() {
        assertThrows(PasswordInvalidException.class, () ->
                loginControl.authenticate(fabriksUser.getUsername(), "falschesPasswort"));
    }

    @Test
    void authenticate_unknownUser_throwsDatabaseUserException() {
        assertThrows(DatabaseUserException.class, () ->
                loginControl.authenticate("nicht_existierender_user_123", "any"));
    }

    @Test
    void checkResetCredentials_success_returnsTrue() throws DatabaseUserException {
        Benutzer resetUser = testDatenFabrik.erstelleUndSpeichereBenutzer("reset");
        boolean result = loginControl.checkResetCredentials(resetUser.getUsername(), resetUser.getEmail());
        assertTrue(result);
    }

    @Test
    void checkResetCredentials_unknownUser_throwsDatabaseUserException() {
        assertThrows(DatabaseUserException.class, () ->
                loginControl.checkResetCredentials("nonexistent_user_x", "noemail@x.de"));
    }
}