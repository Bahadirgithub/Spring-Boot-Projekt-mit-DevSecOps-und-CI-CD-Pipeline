package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.dependency.CssImport;
import org.hbrs.se2.project.hellocar.control.BewertungControl;
import org.hbrs.se2.project.hellocar.control.ManageIdeaControl;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.entities.Bewertung;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "show-all-ratings", layout = AppView.class)
@PageTitle("Gründungsidee Alle Bewertungen")
@CssImport("./styles/views/showallratings/show-all-ratings-view.css")
public class AllRatingsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ManageIdeaControl ideaService;
    private final BewertungControl bewertungControl;
    private final VerticalLayout listContainer = new VerticalLayout();

    public AllRatingsView(ManageIdeaControl ideaService, BewertungControl bewertungControl) {
        this.ideaService = ideaService;
        this.bewertungControl = bewertungControl;
        addClassName("show-all-ratings-view");
        setAlignItems(Alignment.CENTER);
        add(listContainer);
    }

    @Override
    public void setParameter(BeforeEvent event, Long ideaId) {
        GruendungsideeDTO idee = ideaService.findIdeaById(ideaId.intValue());
        if (idee != null) {
            listContainer.removeAll();
            listContainer.add(new H2("Gründungsidee - " + idee.getUnternehmensname()));
            listContainer.add(new H2("Alle Bewertungen"));

            List<Bewertung> bewertungen = bewertungControl.ladeBewertungenFuerIdeeSortiert(ideaId.intValue());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (Bewertung b : bewertungen) {
                VerticalLayout block = new VerticalLayout();
                block.setSpacing(false);
                block.setPadding(false);

                // Sterne + Melden Button
                HorizontalLayout header = new HorizontalLayout();
                header.setWidthFull();
                header.setJustifyContentMode(JustifyContentMode.BETWEEN);
                header.setAlignItems(Alignment.CENTER);
                header.setSpacing(true);

                Span stars = new Span(BewertungControl.createStarRating(b.getBewertung()));
                stars.getStyle().set("color", "#B8860B");
                stars.getStyle().set("font-size", "2em");

                // Zeitstempel
                String zeitstempelString = (b.getZeitstempel() != null) ? b.getZeitstempel().format(formatter) : "Datum unbekannt";
                Span dateSpan = new Span("Bewertet am: " + zeitstempelString);
                dateSpan.getStyle().set("font-size", "0.9em");
                dateSpan.getStyle().set("color", "gray");
                dateSpan.getStyle().set("margin-bottom", "5px");

                String username = bewertungControl.ermittleBenutzernameFuerBewertung(b);
                Span userSpan = new Span("Bewertet von: " + username);
                userSpan.getStyle().set("font-size", "0.9em").set("color", "gray").set("margin-bottom", "5px");

                Anchor reportLink = new Anchor("#", "Kommentar melden");
                reportLink.getElement().addEventListener("click", e -> {
                    // Später Logik implementieren
                });

                header.add(stars, reportLink);
                block.add(header);
                block.add(dateSpan);
                block.add(userSpan);
                // Kommentar
                block.add(new Span("Kommentar: " + b.getKommentar()));

                block.getStyle().set("border-bottom", "1px solid #ccc");
                block.getStyle().set("padding-bottom", "15px");
                block.getStyle().set("margin-top", "10px");

                listContainer.add(block);
            }
        }
    }
}
