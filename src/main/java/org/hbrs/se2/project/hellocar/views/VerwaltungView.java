package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.VerwaltungControl;

/**
 * Die View ist jetzt irrelevant, wegen dem neuen MockUp.
 * Ich muss das wieder neu machen, aber für das erste will ich das schon mal commiten.
 */
@Route(value = "verwaltung")
public class VerwaltungView extends VerticalLayout {

    private VerwaltungControl vControl;

    //Konstruktor
    public VerwaltungView() {

        //Seitentitel
        add(new H2("Profilbearbeitung"));

        // Standard-Abstände reduzieren
        setSpacing(false);
        getStyle().set("gap", "4px");

        // Eingabefelder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        EmailField emailField = new EmailField("E-Mail Adresse");
        NumberField telefonnummerField = new NumberField("Telefonnummer");

        // Passwortfelder – Eingabe muss versteckt angezeigt werden
        PasswordField passwordField = new PasswordField("Passwort");
        PasswordField repeatPasswordField = new PasswordField("Passwort wiederholen");

        // Student oder Gründer
        Select<String> typeSelect = new Select<>();
        typeSelect.setLabel("Kontotyp");
        typeSelect.setItems("Student", "Gründer");
        typeSelect.setPlaceholder("Bitte wählen");

        // Felder der Studenten (zunächst versteckt)
        TextField universitaetField = new TextField("Universität");
        TextField semesterField = new TextField("Semester");
        universitaetField.setVisible(false);
        semesterField.setVisible(false);

        // Felder der Gründer
        TextField unternehmensNameField = new TextField("Unternehmensname");
        unternehmensNameField.setVisible(false);

        // Felder ein-/ausblenden je Kontotyp
        typeSelect.addValueChangeListener(event -> {
            String typ = event.getValue();
            universitaetField.setVisible("Student".equals(typ));
            semesterField.setVisible("Student".equals(typ));
            unternehmensNameField.setVisible("Gründer".equals(typ));
        });

        // Update-Button mit Logik
        Button updateButton = new Button("Aktualisieren");
        updateButton.addClickListener(e -> {

            //Typ aus dem Dropdown holen
            String typ = typeSelect.getValue();

            boolean erfolg = false;

            if ("Student".equals(typ)) {
                erfolg = vControl.updateStudent(
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        String.valueOf(telefonnummerField.getValue()),
                        universitaetField.getValue(),
                        Integer.parseInt(semesterField.getValue())
                );
            } else if ("Gründer".equals(typ)) {
                erfolg = vControl.updateGruender(
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        String.valueOf(telefonnummerField.getValue()),
                        unternehmensNameField.getValue()
                );
            }
            if (erfolg) {
                // Erfolgreich registriert – weiterleiten zur Login-Seite
                UI.getCurrent().navigate("");
            } else {
                // Registrierung fehlgeschlagen
                Notification.show("Aktualisierung fehlgeschlagen.");
            }
        });

        add(firstNameField, lastNameField, emailField,
        new Hr(),
        passwordField, repeatPasswordField,
        new Hr(),
        typeSelect,
        universitaetField, semesterField, unternehmensNameField,
        new Hr(),
        updateButton);

        // Ausrichtung
        setAlignItems(Alignment.CENTER);
    }
}
