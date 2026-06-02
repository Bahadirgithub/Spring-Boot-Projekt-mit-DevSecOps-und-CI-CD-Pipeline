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

/**
 * View zur Registrierung neuer Benutzer (Student oder Gründer).
 */
@Route("registration")
public class RegistrationView extends VerticalLayout {

    public RegistrationView() {
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

        // Registrieren-Button – Logik folgt später
        Button registerButton = new Button("Registrieren");
        // ToDo: Registrierungslogik folgt


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
