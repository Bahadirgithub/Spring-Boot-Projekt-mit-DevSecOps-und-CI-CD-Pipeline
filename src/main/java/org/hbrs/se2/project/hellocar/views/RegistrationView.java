package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.html.Hr;
import org.hbrs.se2.project.hellocar.control.RegistrationControl;
import com.vaadin.flow.component.notification.Notification;
import java.time.LocalDate;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;

/**
 * View zur Registrierung neuer Benutzer (Student oder Gründer).
 */
@Route("registration")
public class RegistrationView extends VerticalLayout {

    // RegistrationControl wird von Spring injiziert
    public RegistrationView(RegistrationControl registrationControl) {
        // Seitentitel
        add(new H2("Register"));

        // Standard-Abstände reduzieren
        setSpacing(false);
        getStyle().set("gap", "4px");

        //Eingabe Felder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        DatePicker birthdayField = new DatePicker("Geburtsdatum");
        EmailField emailField = new EmailField("E-Mail Addresse");
        TextField usernameField = new TextField("Benutzername");

        // Passwortfelder – (Eingabe wird versteckt)
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
            // Studentenfelder nur anzeigen wenn "Student" gewählt
            universitaetField.setVisible("Student".equals(typ));
            semesterField.setVisible("Student".equals(typ));
            // Gründerfelder nur anzeigen wenn "Gründer" gewählt
            unternehmensNameField.setVisible("Gründer".equals(typ));
        });


        // Registrieren-Button – verbindungs Logik und Pflichtfeld prüfung
        Button registerButton = new Button("Registrieren");
        registerButton.addClickListener(e -> {

            // Pflichtfelder prüfen bevor Registrierung gestartet wird
            if (firstNameField.isEmpty() || lastNameField.isEmpty() || birthdayField.isEmpty()
                    || emailField.isEmpty() || usernameField.isEmpty()
                    || passwordField.isEmpty() || typeSelect.isEmpty()) {
                Notification.show("Bitte alle Pflichtfelder ausfüllen.");
                return;
            }

            // prüft welcher typ gewählt wurde Student/Gründer
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
                // Registrierung fehlgeschlagen – Hinweis anzeigen
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
