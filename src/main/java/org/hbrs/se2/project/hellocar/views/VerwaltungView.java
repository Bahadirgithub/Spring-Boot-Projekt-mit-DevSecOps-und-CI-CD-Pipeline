package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.AuthorizationControl;
import org.hbrs.se2.project.hellocar.control.ValidateUserControl;
import org.hbrs.se2.project.hellocar.control.VerwaltungControl;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.util.Globals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Durch "layout = AppView.class" behalten wir die Navigation und den Header.
 * Nur der Content-Bereich wird ausgetauscht.
 */
@Route(value = "verwaltung", layout = AppView.class)
public class VerwaltungView extends VerticalLayout {

    // Wichtigster Part! Das hier holt den aktuellen User aus der Vaadin-Sitzung, dann als Getter als DTO Type - Marius
    private BenutzerDTO getCurrentUser() {
        return (BenutzerDTO) UI.getCurrent()
                .getSession()
                .getAttribute(Globals.CURRENT_USER);
    }

    private final VerwaltungControl vControl;
    private final AuthorizationControl authControl;

    // Constructor Injection – Spring injiziert beide Controls automatisch
    public VerwaltungView(VerwaltungControl vControl, AuthorizationControl authControl) {
        this.vControl = vControl;
        this.authControl = authControl;

        BenutzerDTO currentUser = getCurrentUser();

        if (currentUser == null) {
            Notification.show("Bitte zuerst einloggen.");
            UI.getCurrent().navigate(Globals.Pages.LOGIN_VIEW);
            return;
        }

        // Typ des eingeloggten Benutzers ermitteln
        boolean istStudent = authControl.isStudent(currentUser);
        boolean istGruender = authControl.isGruender(currentUser);

        // Seitentitel
        add(new H2("Profil anpassen"));

        // Profilbild-Platzhalter als Avatar – zeigt Initialen wenn kein Bild vorhanden
        Avatar avatar = new Avatar(currentUser.getVorname() + " " + currentUser.getNachname());
        avatar.setWidth("80px");
        avatar.setHeight("80px");
        avatar.setColorIndex(Math.floorMod(currentUser.getUsername().hashCode(), 7));
        add(avatar);

        // Benutzername nicht bearbeitbar anzeigen
        add(new com.vaadin.flow.component.html.Paragraph("Benutzername: " + currentUser.getUsername()));

        // Zweispaltiges Layout – links Benutzereinstellungen, rechts typ-spezifische Felder
        HorizontalLayout spaltenLayout = new HorizontalLayout();
        spaltenLayout.setSpacing(true);

        // Linke Spalte – Benutzereinstellungen
        VerticalLayout linksLayout = new VerticalLayout();
        linksLayout.setSpacing(false);
        linksLayout.getStyle().set("gap", "4px");

        // Rechte Spalte – Student oder Gründer spezifisch
        VerticalLayout rechtsLayout = new VerticalLayout();
        rechtsLayout.setSpacing(false);
        rechtsLayout.getStyle().set("gap", "4px");

        // Eingabefelder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        EmailField emailField = new EmailField("E-Mail Adresse");
        NumberField telefonnummerField = new NumberField("Telefonnummer");
        DatePicker geburtstagField = ValidateUserControl.validDatePicker();

        // Aktuelle Benutzerdaten anzeigen
        firstNameField.setValue(currentUser.getVorname() == null ? "" : currentUser.getVorname());
        lastNameField.setValue(currentUser.getNachname() == null ? "" : currentUser.getNachname());
        emailField.setValue(currentUser.getEmail() == null ? "" : currentUser.getEmail());

        if (currentUser.getTelefonnummer() != null && !currentUser.getTelefonnummer().isBlank()) {
            try {
                telefonnummerField.setValue(Double.valueOf(currentUser.getTelefonnummer()));
            } catch (NumberFormatException ignored) {
                // Falls alte Telefonnummern nicht rein numerisch sind, Feld leer lassen.
            }
        }

        if (currentUser.getGeburtstag() != null) {
            geburtstagField.setValue(currentUser.getGeburtstag());
        }

        // Sichtbarkeit direkt nach Typ setzen
        TextField universitaetField = new TextField("Universität");
        TextField semesterField = new TextField("Semester");
        semesterField.setPattern("[0-9]*");
        semesterField.setErrorMessage("Nur Zahlen erlaubt.");
        universitaetField.setVisible(istStudent);
        semesterField.setVisible(istStudent);

        TextField abschlussField = new TextField("Abschluss");

        // Skills als Mehrfachauswahl aus DB-Tabelle ahsogehts.skill
        MultiSelectComboBox<String> skillsField = new MultiSelectComboBox<>("Skills");
        skillsField.setItems(vControl.ladeAlleSkillNamen());

        abschlussField.setVisible(istStudent);
        skillsField.setVisible(istStudent);

        // Felder der Gründer
        TextField unternehmensNameField = new TextField("Unternehmensname");
        TextField standortField = new TextField("Standort");
        unternehmensNameField.setVisible(istGruender);
        standortField.setVisible(istGruender);

        // Gründer-Felder ausfüllen
        if (istGruender) {
            Gruender gruender = vControl.ladeGruender(currentUser.getUserId());
            if (gruender != null) {
                unternehmensNameField.setValue(gruender.getUnternehmensname() == null ? "" : gruender.getUnternehmensname());
                standortField.setValue(gruender.getStandort() == null ? "" : gruender.getStandort());
            }
        }

        // Student-Felder ausfüllen
        if (istStudent) {
            Student student = vControl.ladeStudent(currentUser.getUserId());
            if (student != null) {
                universitaetField.setValue(student.getUniversitaet() == null ? "" : student.getUniversitaet());
                semesterField.setValue(student.getSemester() == 0 ? "" : String.valueOf(student.getSemester()));
                abschlussField.setValue(student.getAbschluss() == null ? "" : student.getAbschluss());
                skillsField.setValue(student.getSkills() == null
                        ? Collections.emptySet()
                        : new HashSet<>(student.getSkills()));
            }
        }

        // Update-Button mit Logik
        Button updateButton = new Button("Aktualisieren");
        updateButton.addClickListener(e -> {

            BenutzerDTO aktuellerBenutzer = getCurrentUser();

            if (aktuellerBenutzer == null) {
                Notification.show("Kein eingeloggter Benutzer gefunden.");
                UI.getCurrent().navigate(Globals.Pages.LOGIN_VIEW);
                return;
            }

            boolean erfolg = false;

            if (istStudent) {
                erfolg = vControl.updateStudent(
                        aktuellerBenutzer,
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        geburtstagField.getValue(),
                        emailField.getValue(),
                        telefonnummerField.getValue() != null ? String.valueOf(telefonnummerField.getValue().longValue()) : "",
                        universitaetField.getValue(),
                        semesterField.getValue() == null || semesterField.getValue().isBlank()
                                ? null
                                : semesterField.getValue().matches("\\d+") ? Integer.parseInt(semesterField.getValue()) : null,
                        abschlussField.getValue(),
                        new ArrayList<>(skillsField.getValue())
                );
            } else if (istGruender) {
                erfolg = vControl.updateGruender(
                        aktuellerBenutzer,
                        firstNameField.getValue(),
                        lastNameField.getValue(),
                        geburtstagField.getValue(),
                        emailField.getValue(),
                        telefonnummerField.getValue() != null ? String.valueOf(telefonnummerField.getValue().longValue()) : "",
                        unternehmensNameField.getValue(),
                        standortField.getValue()
                );
            }

            if (erfolg) {
                Notification.show("Profil erfolgreich aktualisiert.");

                // Benutzer neu aus der DB laden
                BenutzerDTO aktualisierterBenutzer = vControl.ladeBenutzer(aktuellerBenutzer.getUserId());

                if (aktualisierterBenutzer != null) {
                    // Session überschreiben damit beim nächsten View-Wechsel die neuen Daten geladen werden
                    UI.getCurrent().getSession().setAttribute(Globals.CURRENT_USER, aktualisierterBenutzer);

                    // Felder sofort aktualisieren
                    firstNameField.setValue(aktualisierterBenutzer.getVorname() == null ? "" : aktualisierterBenutzer.getVorname());
                    lastNameField.setValue(aktualisierterBenutzer.getNachname() == null ? "" : aktualisierterBenutzer.getNachname());
                    emailField.setValue(aktualisierterBenutzer.getEmail() == null ? "" : aktualisierterBenutzer.getEmail());

                    if (aktualisierterBenutzer.getGeburtstag() != null) {
                        geburtstagField.setValue(aktualisierterBenutzer.getGeburtstag());
                    }

                    // Student-Felder aktualisieren
                    if (istStudent) {
                        Student student = vControl.ladeStudent(aktuellerBenutzer.getUserId());
                        if (student != null) {
                            universitaetField.setValue(student.getUniversitaet() == null ? "" : student.getUniversitaet());
                            semesterField.setValue(student.getSemester() == 0 ? "" : String.valueOf(student.getSemester()));
                            abschlussField.setValue(student.getAbschluss() == null ? "" : student.getAbschluss());
                            skillsField.setValue(student.getSkills() == null
                                    ? Collections.emptySet()
                                    : new HashSet<>(student.getSkills()));
                        }
                    }

                    // Gründer-Felder aktualisieren
                    if (istGruender) {
                        Gruender gruender = vControl.ladeGruender(aktuellerBenutzer.getUserId());
                        if (gruender != null) {
                            unternehmensNameField.setValue(gruender.getUnternehmensname() == null ? "" : gruender.getUnternehmensname());
                            standortField.setValue(gruender.getStandort() == null ? "" : gruender.getStandort());
                        }
                    }
                }

            } else {
                Notification.show("Aktualisierung fehlgeschlagen.");
            }
        });

        // Roter Löschen-Button
        Button deleteButton = new Button("Account löschen");
        deleteButton.getStyle().set("background-color", "#e53935");
        deleteButton.getStyle().set("color", "white");

        // Bestätigungsdialog vor dem Löschen
        ConfirmDialog loeschenDialog = new ConfirmDialog();
        loeschenDialog.setHeader("Account wirklich löschen?");
        loeschenDialog.setText("Diese Aktion kann nicht rückgängig gemacht werden. Dein Account wird dauerhaft gelöscht.");
        loeschenDialog.setCancelable(true);
        loeschenDialog.setCancelText("Abbrechen");
        loeschenDialog.setConfirmText("Ja, löschen");
        loeschenDialog.setConfirmButtonTheme("error primary");

        // Bei Bestätigung – Account löschen und zur Login-Seite
        loeschenDialog.addConfirmListener(event -> {
            boolean erfolg = vControl.loeschen(getCurrentUser());
            if (erfolg) {
                UI.getCurrent().getSession().close();
                UI.getCurrent().navigate("");
            } else {
                Notification.show("Löschen fehlgeschlagen.");
            }
        });

        // Button öffnet den Dialog
        deleteButton.addClickListener(e -> loeschenDialog.open());

        // Felder in linke Spalte
        linksLayout.add(firstNameField, lastNameField, geburtstagField, emailField, telefonnummerField);

        // Felder in rechte Spalte
        rechtsLayout.add(universitaetField, semesterField, abschlussField, skillsField,
                unternehmensNameField, standortField);

        // Vertikale Trennlinie
        com.vaadin.flow.component.html.Div trennlinie = new com.vaadin.flow.component.html.Div();
        trennlinie.getStyle().set("border-left", "1px solid #e0e0e0");
        trennlinie.getStyle().set("height", "auto");
        trennlinie.getStyle().set("align-self", "auto");

        // Spalten zusammensetzen
        spaltenLayout.add(linksLayout, trennlinie, rechtsLayout);

        spaltenLayout.setFlexGrow(1, linksLayout);
        spaltenLayout.setFlexGrow(1, rechtsLayout);

        // Alles zur View hinzufügen
        add(spaltenLayout, new Hr(), updateButton, deleteButton);

        // Ausrichtung
        setAlignItems(Alignment.CENTER);
    }
}
