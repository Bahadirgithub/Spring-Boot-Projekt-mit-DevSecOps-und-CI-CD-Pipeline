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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.RouterLink;

import java.security.AllPermission;

/**
 * View zur Registrierung neuer Benutzer (Student oder Gründer).
 */
@Route("registration")
public class RegistrationView extends VerticalLayout {

    public RegistrationView() {
        // Seitentitel
        add(new H2("Register"));

        //Eingabe Felder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
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

        // Alle Komponenten zur View hinzufügen
        add(firstNameField, lastNameField,emailField, usernameField, passwordField, repeatPasswordField, typeSelect);
        // Alles zentriert ausrichten
        setAlignItems(Alignment.CENTER);

    }
}
