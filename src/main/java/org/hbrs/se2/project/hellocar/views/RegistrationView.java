package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.RegistrationControl;
import org.hbrs.se2.project.hellocar.control.results.RegistrationResult;

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
        getStyle().set("gap", "2px");

        // Eingabefelder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        DatePicker birthdayField = new DatePicker("Geburtsdatum");
        birthdayField.setMax(java.time.LocalDate.now());

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
        Button registerButton = new Button("Speichern");
        registerButton.addClickListener(e -> {

            // Vorab-Prüfung auf UI-Ebene, die keine Backend-Logik benötigt
            if (!passwordField.getValue().equals(repeatPasswordField.getValue())) {
                Notification.show("Die Passwörter stimmen nicht überein!");
                return; // Bricht die Registrierung ab
            }

            // Alle anderen Validierungen werden jetzt von der Control-Schicht übernommen.
            // Das ist sauberer, da die UI nicht die Geschäftslogik kennen muss.

            String typ = typeSelect.getValue();
            RegistrationResult result = null;

            if ("Student".equals(typ)) {
                // Semester sicher parsen (Fehler abfangen, falls keine Zahl eingegeben wurde)
                int semester = 0; // Standardwert, falls Feld leer ist
                if (!semesterField.getValue().isEmpty()) {
                    try {
                        semester = Integer.parseInt(semesterField.getValue());
                    } catch (NumberFormatException ex) {
                        Notification.show("Bitte ein gültiges Semester (Zahl) eingeben.");
                        return;
                    }
                }

                result = registrationControl.registriereStudent(
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        birthdayField.getValue(),
                        usernameField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        universitaetField.getValue(),
                        semester
                );
            } else if ("Gründer".equals(typ)) {
                result = registrationControl.registriereGruender(
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        birthdayField.getValue(),
                        usernameField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        unternehmensNameField.getValue()
                );
            }

            if (result == null) {
                Notification.show("Bitte einen Kontotyp auswählen.");
                return;
            }

            // Auswertung des RegistrationResult-Enums für spezifisches Benutzer-Feedback
            switch (result) {
                case SUCCESS:
                    // Erfolgreich registriert – weiterleiten zur Startseite
                    UI.getCurrent().navigate("");
                    Notification.show("Erfolgreich registriert!");
                    break;
                case USERNAME_ALREADY_EXISTS:
                    Notification.show("Dieser Benutzername ist leider schon vergeben.");
                    break;
                case USERNAME_IS_EMPTY:
                    Notification.show("Der Benutzername darf nicht leer sein.");
                    break;
                case INVALID_EMAIL_FORMAT:
                    Notification.show("Die E-Mail-Adresse hat ein ungültiges Format.");
                    break;
                case PASSWORD_TOO_SHORT:
                    Notification.show("Das Passwort muss mindestens 8 Zeichen lang sein.");
                    break;
                default:
                    Notification.show("Ein unbekannter Fehler ist aufgetreten.");
            }
        });

        // Alle Komponenten zur View hinzufügen
        add(firstNameField, lastNameField, birthdayField, emailField, usernameField,
                passwordField, repeatPasswordField,
                typeSelect,
                universitaetField, semesterField, unternehmensNameField,
                registerButton);

        // Alles zentriert ausrichten
        setAlignItems(Alignment.CENTER);
    }
}