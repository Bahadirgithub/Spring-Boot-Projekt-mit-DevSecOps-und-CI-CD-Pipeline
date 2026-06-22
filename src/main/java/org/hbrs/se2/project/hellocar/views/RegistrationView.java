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
import org.hbrs.se2.project.hellocar.control.ValidateUserControl;
import org.hbrs.se2.project.hellocar.control.exception.EMPTY_FIELD;
import org.hbrs.se2.project.hellocar.control.exception.INVALID_EMAIL_FORMAT;
import org.hbrs.se2.project.hellocar.control.exception.PASSWORD_INVALID;
import org.hbrs.se2.project.hellocar.control.exception.USERNAME_ALREADY_EXISTS;

/**
 * View zur Registrierung neuer Benutzer (Student oder Gründer).
 */

// try catch für Fehlermeldungen

@Route("registration")
public class RegistrationView extends VerticalLayout {

    // Constructor Injection – Spring injiziert die RegistrationControl automatisch
    public RegistrationView(RegistrationControl registrationControl) {

        // Seitentitel
        add(new H2("Registrierung"));

        // Standard-Abstände reduzieren
        setSpacing(false);
        getStyle().set("gap", "2px");

        // Eingabefelder
        TextField firstNameField = new TextField("Vorname");
        TextField lastNameField = new TextField("Nachname");
        DatePicker birthdayField = ValidateUserControl.validDatePicker();

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

            Button registerButton = new Button("Account erstellen");
            registerButton.addClickListener(p -> {

                try {
                    // 2. Den Aufruf in den try-Block
                    String type = typeSelect.getValue();

                    if ("Student".equals(type)) {
                        // Semester sicher parsen
                        int semester = 0;
                        if (!semesterField.getValue().isEmpty()) {
                            try {
                                semester = Integer.parseInt(semesterField.getValue());
                            } catch (NumberFormatException ex) {
                                Notification.show("Bitte ein gültiges Semester (Zahl) eingeben.");
                                return;
                            }
                        }
                        // Aufruf der Control-Methode
                        registrationControl.registriereStudent(
                                firstNameField.getValue(), lastNameField.getValue(),
                                birthdayField.getValue(), usernameField.getValue(),
                                emailField.getValue(), passwordField.getValue(),
                                universitaetField.getValue(), semester
                        );
                    } else if ("Gründer".equals(type)) {
                        registrationControl.registriereGruender(
                                firstNameField.getValue(), lastNameField.getValue(),
                                birthdayField.getValue(), usernameField.getValue(),
                                emailField.getValue(), passwordField.getValue(),
                                unternehmensNameField.getValue()
                        );
                    } else {
                        Notification.show("Bitte einen Kontotyp auswählen.");
                        return;
                    }

                    // 3. Wenn wir hier ankommen, gab es keine Exception -> Erfolg!
                    Notification.show("Erfolgreich registriert!");
                    UI.getCurrent().navigate(""); // Passe den String an deine Login-Route an

                }

                catch(USERNAME_ALREADY_EXISTS e ){
                    usernameField.setErrorMessage("Nutzername ist bereits vergeben");
                    usernameField.setInvalid(true);
                }

                catch(INVALID_EMAIL_FORMAT e){
                    emailField.setErrorMessage("Die E-Mail ist ungültig.");
                    emailField.setInvalid(true); // Feld wird rot
                }

                catch(PASSWORD_INVALID e){

                    passwordField.setErrorMessage("Das Passwort muss mindestens 8 Zeichen, min. 1 Groß und Kleinbuchstaben, Sonderzeichen und min eine Zahl enthalten.");
                    passwordField.setInvalid(true); // Feld wird rot
                }

                catch(EMPTY_FIELD e){
                    Notification.show("Bitte füllen Sie alle Felder aus.");

                }

                catch (Exception ex) {
                    Notification.show("Fehler: " + ex.getMessage());
                }
            });


        ///////////////Design Änderungen//////////////////////////////
            // Container erstellen und Breite definieren
            VerticalLayout formContainer = new VerticalLayout();
            formContainer.setMaxWidth("360px");
            formContainer.setWidthFull();
            formContainer.setPadding(false);
            formContainer.setSpacing(true);

            // Allen Feldern sagen, dass sie sich dem Container anpassen sollen
            firstNameField.setWidthFull();
            lastNameField.setWidthFull();
            birthdayField.setWidthFull();
            emailField.setWidthFull();
            usernameField.setWidthFull();
            passwordField.setWidthFull();
            repeatPasswordField.setWidthFull();
            typeSelect.setWidthFull();
            universitaetField.setWidthFull();
            semesterField.setWidthFull();
            unternehmensNameField.setWidthFull();
            registerButton.setWidthFull();

            // Alle Felder in den Container packen
            formContainer.add(
                    firstNameField, lastNameField, birthdayField, emailField, usernameField,
                    passwordField, repeatPasswordField,
                    typeSelect,
                    universitaetField, semesterField, unternehmensNameField,
                    registerButton
            );

            // Container zur Haupt-View hinzufügen
            add(formContainer);

            setAlignItems(Alignment.CENTER);
        }
    }