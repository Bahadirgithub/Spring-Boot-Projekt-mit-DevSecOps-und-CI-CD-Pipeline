package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.util.Globals;

/**
 * Der Haupteinstiegspunkt der Anwendung (Root-Route "").
 * Diese Klasse fungiert als "Weiche" (Router):
 * Sie zeigt selbst keine UI-Elemente an, sondern leitet den Benutzer
 * sofort zur korrekten Seite weiter, basierend auf seinem Login-Status.
 */
@Route(value = "")
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    public MainView() {
        // Diese View ist leer. Alle Aktion passiert in der beforeEnter-Methode.
    }

    /**
     * Diese Methode wird ausgeführt, BEVOR die Seite (die eh leer ist) geladen wird.
     * Hier treffen wir die Entscheidung, wo der Benutzer hingehört.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        
        // 1. Prüfen: Ist ein Benutzer-Objekt in der aktuellen Session gespeichert?
        Object currentUser = UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER);

        if (currentUser != null) {
            // Benutzer IST eingeloggt -> Weiterleitung in den internen Bereich
            event.forwardTo(Globals.Pages.SHOW_CARS);
        } else {
            // Benutzer IST NICHT eingeloggt -> Weiterleitung zur neuen Login-Seite
            event.forwardTo(LoginView.class);
        }
    }
}