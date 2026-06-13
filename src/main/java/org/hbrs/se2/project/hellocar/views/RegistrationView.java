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

        usernameField.setMinLength(1);

        // Passwortfelder – Eingabe wird versteckt angezeigt
        PasswordField passwordField = new PasswordField("Passwort");
        PasswordField repeatPasswordField = new PasswordField("Passwort wiederholen");
        passwordField.setMinLength(8);
        repeatPasswordField.setMinLength(8);




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

            // 2. NEU: Prüfung auf Passwort-Übereinstimmung
            if (!passwordField.getValue().equals(repeatPasswordField.getValue())) {
                Notification.show("Die Passwörter stimmen nicht überein!");
                return; // Bricht die Registrierung ab
            }

            if(repeatPasswordField.getValue().length() <= 7){
                Notification.show("Das Passwort ist zu kurz.");
                return;
            }
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

            // =================================================================================
            // HINWEIS FÜR DAS ENTWICKLERTEAM:
            //
            // WARUM DIE FEHLERMELDUNG HIER IRREFÜHREND IST:
            //
            // Die Methode 'registriereStudent' gibt aktuell nur 'true' (hat geklappt) oder 'false' (hat nicht geklappt) zurück.
            // Wir wissen aber nicht, WARUM es nicht geklappt hat.
            //
            // Das Problem: Der untere 'else'-Block wird für JEDEN Fehler ausgeführt. Egal ob das Passwort zu kurz ist,
            // die E-Mail falsch ist oder der Benutzername schon existiert – wir zeigen IMMER die gleiche,
            // fest einprogrammierte Nachricht an: "Benutzername bereits vergeben".
            //
            // Das verwirrt beim Testen, weil man denkt, es liegt immer am Benutzernamen, obwohl der Fehler
            // vielleicht ein zu kurzes Passwort war.
            //
            // TODO: Die 'registriereStudent'-Methode in der 'RegistrationControl' so umbauen, dass sie uns den
            //       genauen Grund für den Fehler zurückgibt (z.B. mit einem Ergebnis-Objekt oder einer Exception).
            //       Danach können wir hier im 'else'-Block die richtige Fehlermeldung anzeigen.
            // =================================================================================
            if (erfolg) {
                // Erfolgreich registriert – weiterleiten zur show-car-view
                UI.getCurrent().navigate("");
            } else {
                // Registrierung fehlgeschlagen
                Notification.show("Registrierung fehlgeschlagen. Benutzername bereits vergeben.");
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