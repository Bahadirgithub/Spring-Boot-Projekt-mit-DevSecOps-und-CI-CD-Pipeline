package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.RegistrationControl;
import org.hbrs.se2.project.hellocar.control.ValidateUserControl;
import org.hbrs.se2.project.hellocar.control.exception.*;

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

        Image logo = new Image("images/logo.png", "Logo");
        logo.setWidth("80px");

        H1 brandText = new H1("St@rtMeUp");
        brandText.getStyle().set("margin", "0 0 2px 0");

        Span subtitleText = new Span("powered by AhSoGehts");
        subtitleText.getStyle().set("font-size", "14px");
        subtitleText.getStyle().set("color", "gray");
        subtitleText.getStyle().set("margin", "0 0 30px 0");

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
                registerButton.setEnabled(false);

                // Versuche die Felder einzutragen und fange Exceptions falls etwas nicht klappt
                try {

                    //PasswordMissMatch testen, es wird hier getestet, weil wir nur hier das Wiederholungsfeld haben
                    if(!passwordField.getValue().equals(repeatPasswordField.getValue())){
                        throw new PasswordMissmatchException("Passwörter ungleich");
                    }

                    // 2. Den Aufruf in den try-Block
                    String type = typeSelect.getValue();

                    if ("Student".equals(type)) {
                        // Semester sicher parsen
                        int semester = 0;
                        if (!semesterField.getValue().isEmpty()) {
                            try {
                                semester = Integer.parseInt(semesterField.getValue());
                            }
                            catch (NumberFormatException ex) {
                                semesterField.setErrorMessage("Bitte ein gültiges Semester (Zahl) eingeben.");
                                semesterField.setInvalid(true);
                                return;
                            }
                        }
                        // Aufruf der Control-Methode - hier könnten exceptions geworfen werden
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
                        typeSelect.setErrorMessage("Bitte einen Kontotyp auswählen.");
                        typeSelect.setInvalid(true);
                        return;
                    }

                    UI.getCurrent().navigate(""); // Passe den String an deine Login-Route an

                }

                catch(PasswordMissmatchException e){
                    passwordField.setErrorMessage("Passwörter stimmen nicht überein");
                    passwordField.setInvalid(true);
                    repeatPasswordField.setErrorMessage("Bitte passen Sie die Passwörter an.");
                    repeatPasswordField.setInvalid(true);

                }

                catch(UsernameEmptyException e){
                    usernameField.setErrorMessage("Der Nutzername darf nicht leer sein.");
                    usernameField.setInvalid(true);
                }

                catch(UsernameAlreadyExistsException e ){
                    usernameField.setErrorMessage("Der Nutzername ist bereits vergeben");
                    usernameField.setInvalid(true);
                }

                catch(InvalidEmailException e){
                    emailField.setErrorMessage("Die E-Mail ist ungültig.");
                    emailField.setInvalid(true); // Feld wird rot
                }

                catch(PasswordInvalidException e){
                    passwordField.setErrorMessage("Das Passwort muss min. 8 Zeichen, davon min. eine Zahl, einen Großbuchstaben, einen Kleinbuchstaben und min ein Sonderzeichen enthalten.");
                    passwordField.setInvalid(true); // Feld wird rot
                }

                catch(IllegalArgumentException e){
                    semesterField.setErrorMessage("Bitte geben Sie ein gültiges Semester ein.");
                    semesterField.setInvalid(true);
                }

                catch(FirstNameEmptyException e){
                    firstNameField.setErrorMessage("Bitte geben Sie Ihren Vornamen an.");
                    firstNameField.setInvalid(true);
                }

                catch(LastNameEmptyException e){
                    lastNameField.setErrorMessage("Bitte geben Sie Ihren Nachnamen an.");
                    lastNameField.setInvalid(true);
                }

                catch(StartUpEmptyException e){
                    unternehmensNameField.setErrorMessage("Bitte geben Sie Ihr Unternehmen an.");
                    unternehmensNameField.setInvalid(true);
                }

                catch(UniEmptyException e){
                    universitaetField.setErrorMessage("Bitte geben Sie Ihre Universität an.");
                    universitaetField.setInvalid(true);
                }

                catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }

                finally {
                    //  Immer wieder freigeben, wenn wir nicht navigiert sind (Fehlerfall)
                    registerButton.setEnabled(true);
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