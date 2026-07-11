package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractAuthView extends VerticalLayout {

    public AbstractAuthView() {
        // Gemeinsame Layout-Einstellungen
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setSpacing(false);
        getStyle().set("gap", "2px");

        // Gemeinsame Komponenten erstellen
        Image logo = new Image("images/logo.png", "Logo");
        logo.setWidth("80px");

        H1 brandText = new H1("St@rtMeUp");
        brandText.getStyle().set("margin", "0 0 2px 0"); // In LoginView stand -5px, 2px sieht meist einheitlicher aus

        Span subtitleText = new Span("powered by AhSoGehts");
        subtitleText.getStyle().set("font-size", "14px");
        subtitleText.getStyle().set("color", "gray");
        subtitleText.getStyle().set("margin", "0 0 30px 0");

        // Komponenten der Basis-View hinzufügen
        add(logo, brandText, subtitleText);
    }
}