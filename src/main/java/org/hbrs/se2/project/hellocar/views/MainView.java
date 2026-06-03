package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouterLink;
import org.hbrs.se2.project.hellocar.control.LoginControl;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.util.Globals;

/**
 * View zur Darstellung der Startseite. Diese zeigt dem Benutzer ein Login-Formular an.
 */
@Route(value = "")
@RouteAlias(value = "login")
public class MainView extends VerticalLayout {

    private final LoginControl loginControl;

    // Konstruktor-Injection stellt sicher, dass loginControl vorhanden ist
    public MainView(LoginControl loginControl) {
        this.loginControl = loginControl;

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        LoginForm loginForm = new LoginForm();
        RouterLink registerLink = new RouterLink("Registrieren", RegistrationView.class);

        // Komponenten werden beim Start der View zum Layout hinzugefügt
        add(loginForm, registerLink);

        // Listener definiert nur die Reaktion auf den Button-Klick
        loginForm.addLoginListener(e -> {
            try {
                boolean isAuthenticated = loginControl.authentificate(e.getUsername(), e.getPassword());

                if (isAuthenticated) {
                    grabAndSetUserIntoSession();
                    navigateToMainPage();
                } else {
                    loginForm.setError(true);
                }
            } catch (DatabaseUserException databaseException) {
                Dialog dialog = new Dialog();
                dialog.add(new Text(databaseException.getReason()));
                dialog.setWidth("400px");
                dialog.open();
            }
        });
    }

    private void grabAndSetUserIntoSession() {
        BenutzerDTO benutzerDTO = loginControl.getCurrentUser();
        UI.getCurrent().getSession().setAttribute(Globals.CURRENT_USER, benutzerDTO);
    }

    private void navigateToMainPage() {
        UI.getCurrent().navigate(Globals.Pages.SHOW_CARS);
    }
}