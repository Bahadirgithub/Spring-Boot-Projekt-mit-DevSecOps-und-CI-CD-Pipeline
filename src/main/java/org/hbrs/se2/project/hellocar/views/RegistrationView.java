package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.RegistrationControl;

/**
 * View zur Registrierung neuer Benutzer (Student oder Gründer).
 */
@Route("registration")
public class RegistrationView extends VerticalLayout {

    // Constructor Injection – Spring injiziert die RegistrationControl automatisch
    public RegistrationView(RegistrationControl registrationControl) {

        // Seitentitel
        add(new H2("Register"));

        // Standard-Abstände reduzieren
        setSpacing(false);
        getStyle().set("gap", "4px");

        // Eingabefelder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        DatePicker birthdayField = new DatePicker("Geburtsdatum");
        EmailField emailField = new EmailField("E-Mail Adresse");
        TextField usernameField = new TextField("Benutzername");

        // Passwortfelder – Eingabe wird versteckt angezeigt
        PasswordField passwordField = new PasswordField("Passwort");
        PasswordField repeatPasswordField = new PasswordField("Passwort wiederholen");

        // Dropdown zur Auswahl ob Student oder Gründer
        Select<String> typeSelect = new Select<>();
        typeSelect.setLabel("Kontotyp");
        typeSelect.setItems("Student", "Gründer");
        typeSelect.setPlaceholder("Bitte wählen");

        // Felder nur für Studenten (zunächst versteckt)
        TextField universitaetField = new TextField("Universität");
        TextField semesterField = new TextField("Semester");
        universitaetField.setVisible(false);
        semesterField.setVisible(false);

        // Felder nur für Gründer (zunächst versteckt)
        TextField unternehmensNameField = new TextField("Unternehmensname");
        unternehmensNameField.setVisible(false);

        // Felder dynamisch ein-/ausblenden je nach Kontotyp
        typeSelect.addValueChangeListener(event -> {
            String typ = event.getValue();
            universitaetField.setVisible("Student".equals(typ));
            semesterField.setVisible("Student".equals(typ));
            unternehmensNameField.setVisible("Gründer".equals(typ));
        });

        // Registrieren-Button mit Logik
        Button registerButton = new Button("Registrieren");
        registerButton.addClickListener(e -> {

            // Pflichtfelder prüfen bevor Registrierung gestartet wird
            if (firstNameField.isEmpty() || lastNameField.isEmpty() || birthdayField.isEmpty()
                    || emailField.isEmpty() || usernameField.isEmpty()
                    || passwordField.isEmpty() || typeSelect.isEmpty()) {
                Notification.show("Bitte alle Pflichtfelder ausfüllen.");
                return;
            }

            //Typ aus dem Dropdown holen
            String typ = typeSelect.getValue();

            boolean erfolg = false;

            if ("Student".equals(typ)) {
                erfolg = registrationControl.registriereStudent(
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        birthdayField.getValue(),
                        usernameField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        universitaetField.getValue(),
                        Integer.parseInt(semesterField.getValue())
                );
            } else if ("Gründer".equals(typ)) {
                erfolg = registrationControl.registriereGruender(
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        birthdayField.getValue(),
                        usernameField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        unternehmensNameField.getValue()
                );
            }

            if (erfolg) {
                // Erfolgreich registriert – weiterleiten zur Login-Seite
                UI.getCurrent().navigate("");
            } else {
                // Registrierung fehlgeschlagen
                Notification.show("Registrierung fehlgeschlagen. Benutzername bereits vergeben.");
            }
        });

        // Alle Komponenten zur View hinzufügen
        add(firstNameField, lastNameField, birthdayField, emailField, usernameField,
                new Hr(),
                passwordField, repeatPasswordField,
                new Hr(),
                typeSelect,
                universitaetField, semesterField, unternehmensNameField,
                new Hr(),
                registerButton);

        // Alles zentriert ausrichten
        setAlignItems(Alignment.CENTER);
    }
}