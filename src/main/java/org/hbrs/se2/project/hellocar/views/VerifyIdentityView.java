package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.LoginControl;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;

@Route("verify-identity")
@PageTitle("Identität verifizieren | StartMeUp")
public class VerifyIdentityView extends VerticalLayout {

    public VerifyIdentityView(LoginControl loginControl) {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        // Seitentitel
        H2 title = new H2("Identität verifizieren");

        // Hinweistext für die Verifizierung
        Paragraph instructionText = new Paragraph("Zur Verifizierung Ihrer Identität ist die Eingabe von Benutzername und E-Mail-Adresse erforderlich.");

        // Standard-Abstände
        setSpacing(false);
        getStyle().set("gap", "2px");

        // Eingabefelder
        TextField usernameField = new TextField("Benutzername");
        TextField emailField = new TextField("E-Mail-Adresse");
        Button nextButton = new Button("Weiter");

        // Button-Logik
        nextButton.addClickListener(e -> {
            try {
                boolean isValid = loginControl.checkResetCredentials(usernameField.getValue(), emailField.getValue());

                if (isValid) {
                    // Weiterleitung zur finalen Passwort-Reset-Page
                    UI.getCurrent().navigate(PasswordResetView.class);
                } else {
                    usernameField.setInvalid(true);
                    emailField.setInvalid(true);
                    emailField.setErrorMessage("Kombination aus Benutzername und E-Mail ist falsch.");
                }
            } catch (DatabaseUserException dbEx) {
                emailField.setInvalid(true);
                emailField.setErrorMessage("Datenbankfehler: " + dbEx.getReason());
            }
        });

        // Formular-Container
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setMaxWidth("360px");
        formContainer.setWidthFull();
        formContainer.setPadding(false);
        formContainer.setSpacing(true);

        // Breiten anpassen
        usernameField.setWidthFull();
        emailField.setWidthFull();
        nextButton.setWidthFull();

        // Komponenten in den Container packen
        formContainer.add(usernameField, emailField, nextButton);
        add(title, instructionText, formContainer);

        // Alles zentrieren
        setAlignItems(Alignment.CENTER);
    }
}