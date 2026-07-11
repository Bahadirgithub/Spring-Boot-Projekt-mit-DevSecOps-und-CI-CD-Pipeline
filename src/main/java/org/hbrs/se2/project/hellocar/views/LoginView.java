package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.hbrs.se2.project.hellocar.control.LoginControl;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.control.exception.PasswordInvalidException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.util.Globals;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * View zur Darstellung des Login-Formulars.
 * Diese Seite ist der alleinige Einstiegspunkt für nicht-authentifizierte Benutzer.
 */
@Route("login")
@PageTitle("Login | StartMeUp")
public class LoginView extends AbstractAuthView implements BeforeEnterObserver, LocaleChangeObserver {

    private final LoginControl loginControl;

    private LoginForm loginForm;

    public LoginView(LoginControl loginControl) {
        super();
        this.loginControl = loginControl;
        CreateLayout();
    }

    private void CreateLayout() {
        // LoginForm-Komponente mit deutschen Texten erstellen
        LoginForm loginForm = this.createLoginForm();

        // Link zur Registrierungs-Seite
        RouterLink registerLink = new RouterLink("Noch kein Konto? Jetzt registrieren", RegistrationView.class);

        add(loginForm, registerLink);

        // Listener für den Klick auf "Anmelden"
        loginForm.addLoginListener(e -> {
            try {
                BenutzerDTO benutzer = this.loginControl.authenticate(e.getUsername(), e.getPassword());

                System.out.println("a");
                grabAndSetUserIntoSession(benutzer);
                System.out.println("b");
                navigateToMainPage();
                System.out.println("c");

            } catch (DatabaseUserException ex) {
                // If Username is not found in Database!
                this.loginForm.setError(true);

            } catch (PasswordInvalidException ex) {
                // If Password is not equal
                this.loginForm.setError(true);
                // TODO Nachricht einfügen weil das Password nicht übereinstimmt

            } catch (DataAccessResourceFailureException ex) {
                Dialog dialog = new Dialog();
                dialog.add(new Text("Interner Server Fehler!"));
                dialog.setWidth("400px");
                dialog.open();
            }
        });

        loginForm.addForgotPasswordListener(event -> {
            // Navigiert direkt auf die neue, eigenständige Verifizierungsseite
            UI.getCurrent().navigate("verify-identity");
        });
    }

    /**
     * Erstellt ein Internationalisierungs-Objekt (i18n) mit deutschen Texten
     * für das Login-Formular und die zugehörige Fehlermeldung.
     * @return Ein konfiguriertes LoginI18n-Objekt.
     */
    private LoginForm createLoginForm() {
        this.loginForm = new LoginForm();

        return loginForm;
    }

    /**
     * Speichert das DTO des aktuell eingeloggten Benutzers in der Vaadin-Session.
     */
    private void grabAndSetUserIntoSession(BenutzerDTO benutzer) {
        UI.getCurrent().getSession().setAttribute(Globals.CURRENT_USER, benutzer);
    }

    /**
     * Leitet den Benutzer zur Hauptseite für eingeloggte Benutzer weiter.
     */
    private void navigateToMainPage() {
        // Das Ziel sollte die erste Seite sein, die ein eingeloggter Benutzer sieht.
        UI.getCurrent().navigate(ShowIdeasView.class);
    }

    /**
     * Diese Methode wird von Vaadin aufgerufen, BEVOR die View angezeigt wird.
     * Wir nutzen sie, um zu prüfen, ob der Benutzer vielleicht schon eingeloggt ist.
     * Wenn ja, wird er direkt zur Hauptseite weitergeleitet, anstatt den Login erneut zu sehen.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER) != null) {
            event.forwardTo(ShowIdeasView.class);
        }
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        LoginI18n i18n = LoginI18n.createDefault();

        // Formular-Texte
        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setTitle("Login");
        i18nForm.setUsername("Benutzername");
        i18nForm.setPassword("Passwort");
        i18nForm.setSubmit("Anmelden");
        i18nForm.setForgotPassword("Passwort vergessen");
        i18n.setForm(i18nForm);

        // Fehler-Texte
        LoginI18n.ErrorMessage i18nErrorMessage = i18n.getErrorMessage();

        // Allgemeine Fehlermeldung (rote Box oben) bei falschem Passwort/Username
        i18nErrorMessage.setTitle("Anmeldung fehlgeschlagen");
        i18nErrorMessage.setMessage("Bitte überprüfen Sie Ihren Benutzernamen und Ihr Passwort.");

        // Spezifische Meldungen für leere Felder (direkt unter dem Feld)
        i18nErrorMessage.setUsername("Benutzername Fehlt");
        i18nErrorMessage.setPassword("Passwort Fehlt");

        i18n.setErrorMessage(i18nErrorMessage);

        loginForm.setI18n(i18n);
    }
}