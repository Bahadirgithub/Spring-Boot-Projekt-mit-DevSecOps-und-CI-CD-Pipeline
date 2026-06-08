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
import com.vaadin.flow.component.datepicker.DatePicker;

import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.AuthorizationControl;
import org.hbrs.se2.project.hellocar.control.VerwaltungControl;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.util.Globals;

/**
 * Die View ist jetzt irrelevant, wegen dem neuen MockUp.
 * Ich muss das wieder neu machen, aber für das erste will ich das schon mal commiten.
 */
@Route(value = "verwaltung")
public class VerwaltungView extends VerticalLayout {

    //Wichtigster Part! Das hier holt den aktuellen User aus der Vaadin Sitzung, dann als Getter als DTO Type - Marius
    private BenutzerDTO getCurrentUser() {
        return (BenutzerDTO) UI.getCurrent()
                .getSession()
                .getAttribute(Globals.CURRENT_USER);
    }

    private VerwaltungControl vControl;

    //Konstruktor
    public VerwaltungView(VerwaltungControl vControl) {
        this.vControl = vControl;

        BenutzerDTO currentUser = getCurrentUser();

        if (currentUser == null) {
            Notification.show("Bitte zuerst einloggen.");
            UI.getCurrent().navigate(Globals.Pages.LOGIN_VIEW);
            return;
        }

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
        DatePicker geburtstagField = new DatePicker("Geburtsdatum");

        // Aktuelle Benutzerdaten anzeigen
        firstNameField.setValue(currentUser.getVorname() == null ? "" : currentUser.getVorname());
        lastNameField.setValue(currentUser.getNachname() == null ? "" : currentUser.getNachname());
        emailField.setValue(currentUser.getEmail() == null ? "" : currentUser.getEmail());

        if (currentUser.getGeburtstag() != null) {
            geburtstagField.setValue(currentUser.getGeburtstag());
        }

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

        TextField abschlussField = new TextField("Abschluss");
        TextField skillsField = new TextField("Skills");
        abschlussField.setVisible(false);
        skillsField.setVisible(false);

        // Felder der Gründer
        TextField unternehmensNameField = new TextField("Unternehmensname");
        unternehmensNameField.setVisible(false);
        TextField standortField = new TextField("Standort");
        standortField.setVisible(false);

        // Felder ein-/ausblenden je Kontotyp
        typeSelect.addValueChangeListener(event -> {
            String typ = event.getValue();

            universitaetField.setVisible("Student".equals(typ));
            semesterField.setVisible("Student".equals(typ));
            abschlussField.setVisible("Student".equals(typ));
            skillsField.setVisible("Student".equals(typ));

            unternehmensNameField.setVisible("Gründer".equals(typ));
            standortField.setVisible("Gründer".equals(typ));
        });

        // Update-Button mit Logik
        Button updateButton = new Button("Aktualisieren");
        updateButton.addClickListener(e -> {

            BenutzerDTO aktuellerBenutzer = getCurrentUser();

            if (aktuellerBenutzer == null) {
                Notification.show("Kein eingeloggter Benutzer gefunden.");
                UI.getCurrent().navigate(Globals.Pages.LOGIN_VIEW);
                return;
            }

            //Typ aus dem Dropdown holen
            String typ = typeSelect.getValue();

            boolean erfolg = false;

            if ("Student".equals(typ)) {
                erfolg = vControl.updateStudent(
                        aktuellerBenutzer,
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        geburtstagField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        String.valueOf(telefonnummerField.getValue()),
                        universitaetField.getValue(),
                        semesterField.getValue() == null || semesterField.getValue().isBlank()
                                ? null
                                : Integer.parseInt(semesterField.getValue()),
                        abschlussField.getValue(),
                        skillsField.getValue()
                );
            } else if ("Gründer".equals(typ)) {
                erfolg = vControl.updateGruender(
                        aktuellerBenutzer,
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        geburtstagField.getValue(),
                        emailField.getValue(),
                        passwordField.getValue(),
                        String.valueOf(telefonnummerField.getValue()),
                        unternehmensNameField.getValue(),
                        standortField.getValue()
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

        add(firstNameField, lastNameField, geburtstagField, emailField,
                new Hr(),
                passwordField, repeatPasswordField,
                new Hr(),
                typeSelect,
                universitaetField, semesterField, abschlussField, skillsField,
                unternehmensNameField, standortField,
                new Hr(),
                updateButton);

        // Ausrichtung
        setAlignItems(Alignment.CENTER);
    }
}