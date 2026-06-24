package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.LoginControl;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;

@Route("reset-password")
@PageTitle("Passwort zurücksetzen | StartMeUp")
public class PasswordResetView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginControl loginControl;

    public PasswordResetView(LoginControl loginControl) {
        this.loginControl = loginControl;

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        Image logo = new Image("images/logo.png", "Logo");
        logo.setWidth("80px");

        H1 brandText = new H1("St@rtMeUp");
        brandText.getStyle().set("margin", "0 0 2px 0");

        Span subtitleText = new Span("powered by AhSoGehts");
        subtitleText.getStyle().set("font-size", "14px");
        subtitleText.getStyle().set("color", "gray");
        subtitleText.getStyle().set("margin", "0 0 30px 0");

        // Abstände anpassen
        setSpacing(false);
        getStyle().set("gap", "2px");

        // Komponenten erstellen
        H2 title = new H2("Neues Passwort vergeben");

        // Hinweistext für die Verifizierung
        Paragraph instructionText = new Paragraph("Bitte vergeben Sie ein neues Passwort für Ihren Account. Dieses muss aus mindestens 8 Zeichen bestehen.");

        PasswordField passwordField = new PasswordField("Neues Passwort");
        PasswordField confirmPasswordField = new PasswordField("Passwort wiederholen");

        // Passwort-Richtlinie
        passwordField.setMinLength(8);
        confirmPasswordField.setMinLength(8);

        Button saveButton = new Button("Passwort speichern");

        // Logik beim Klick auf Speichern
        saveButton.addClickListener(e -> {
            String newPw = passwordField.getValue();
            String confirmPw = confirmPasswordField.getValue();

            passwordField.setInvalid(false);
            confirmPasswordField.setInvalid(false);

            // 1. Validierung: Darf nicht leer sein
            if (newPw.isEmpty()) {
                passwordField.setInvalid(true);
                passwordField.setErrorMessage("Das Passwort darf nicht leer sein.");
                return;
            }

            // 2. Validierung: Mindestlänge manuell erzwingen
            if (newPw.length() < 8) {
                passwordField.setInvalid(true);
                passwordField.setErrorMessage("Das Passwort muss mindestens 8 Zeichen lang sein.");
                return;
            }

            // 3. Validierung: Stimmen die Passwörter überein?
            if (!newPw.equals(confirmPw)) {
                confirmPasswordField.setInvalid(true);
                confirmPasswordField.setErrorMessage("Die Passwörter stimmen nicht überein!");
                return;
            }

            // Wenn alle Prüfungen erfolgreich bestanden wurden:
            try {
                // Passwort über das Control in der DB aktualisieren
                loginControl.updatePassword(newPw);

                // Erfolgsmeldung anzeigen
                Notification.show("Passwort erfolgreich geändert!", 3000, Notification.Position.TOP_CENTER);

                // Erfolgreich beendet -> Zurück zur Login-Maske schicken
                UI.getCurrent().navigate(LoginView.class);
            } catch (DatabaseUserException dbEx) {
                confirmPasswordField.setInvalid(true);
                confirmPasswordField.setErrorMessage("Fehler: " + dbEx.getReason());
            }
        });

        // Formular-Container
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("360px");
        formContainer.setWidthFull();
        formContainer.setPadding(false);
        formContainer.setSpacing(true);

        // Breiten an den Container anpassen
        passwordField.setWidthFull();
        confirmPasswordField.setWidthFull();
        saveButton.setWidthFull();

        // Erst Felder in den Container, dann alles zentriert anzeigen
        formContainer.add( passwordField, confirmPasswordField, saveButton);
        add(logo, brandText, subtitleText, title, instructionText, formContainer);
    }

    /**
     * WICHTIGER SCHUTZWALL:
     * Diese Methode blockiert den direkten Zugriff per URL-Eingabe.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Object resetUserId = UI.getCurrent().getSession().getAttribute("reset_user_id");

        if (resetUserId == null) {
            // Kein aktiver Reset-Prozess -> Zurück zum Login werfen
            event.rerouteTo(LoginView.class);
        }
    }
}