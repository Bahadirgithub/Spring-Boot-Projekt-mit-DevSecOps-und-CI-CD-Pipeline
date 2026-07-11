package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import org.hbrs.se2.project.hellocar.control.AuthorizationControl;
import org.hbrs.se2.project.hellocar.control.BewertungControl;
import org.hbrs.se2.project.hellocar.control.ManageIdeaControl;
import org.hbrs.se2.project.hellocar.control.BewerbungControl;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.entities.Bewertung;
import org.hbrs.se2.project.hellocar.util.Globals;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

/**
 * View zur Anzeige der Details einer Gründungsidee.
 */
@Route(value = "show-idea-details", layout = AppView.class)
@PageTitle("Gründungsidee Details")
@CssImport("./styles/views/showideadetails/show-idea-details-view.css")
public class ShowIdeaDetailsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ManageIdeaControl ideaService;
    private final BewertungControl bewertungControl;
    private final AuthorizationControl authorizationControl;
    private final BewerbungControl bewerbungControl;

    // Felder wie in EnterIdeasView
    private TextField unternehmensname = new TextField("Unternehmensname");
    private TextField position = new TextField("Offene Position / Rolle");
    private TextArea beschreibung = new TextArea("Beschreibung der Idee");
    private TextArea anforderungen = new TextArea("Anforderungen an Bewerber");
    private TextArea arbeitsbedingungen = new TextArea("Arbeitsbedingungen");
    private MultiSelectComboBox<String> gesuchteSkills = new MultiSelectComboBox<>("Gesuchte Skills");

    private int currentIdeaId;

    private VerticalLayout reviewsContainer = new VerticalLayout();

    public ShowIdeaDetailsView(ManageIdeaControl ideaService,
                               BewertungControl bewertungControl, AuthorizationControl authorizationControl,
                               BewerbungControl bewerbungControl) {
        this.ideaService = ideaService;
        this.bewertungControl = bewertungControl;
        this.authorizationControl = authorizationControl;
        this.bewerbungControl = bewerbungControl;
        addClassName("show-idea-details-view");

        BenutzerDTO currentUser = (BenutzerDTO) UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER);

        // Alle Felder auf Read-Only setzen
        setAllFieldsReadOnly(true);

        add(new H3("Details zur Gründungsidee"));
        add(createFormLayout());

        if (currentUser != null && authorizationControl.isStudent(currentUser)) {
            Button applyButton = new Button("Jetzt bewerben");
            applyButton.setWidthFull();
            applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            applyButton.addClickListener(e -> {
                BewerbungsPopup popup = new BewerbungsPopup(bewerbungControl, currentUser, currentIdeaId);
                popup.open();
            });
            add(applyButton);
        }

        // Erste Linie (nach dem Formular)
        Hr line1 = new Hr();
        line1.addClassName("divider");
        add(line1);

        //Title
        H3 title = new H3("Bewertungen");
        title.addClassName("section-title");
        add(title);

        //Bewertungen
        reviewsContainer.setSpacing(false);
        reviewsContainer.setMargin(false);
        reviewsContainer.addClassName("reviews-container");
        add(reviewsContainer);

        if (currentUser != null && authorizationControl.isStudent(currentUser)) {
            // Zweite Linie (nach den Bewertungen)
            Hr line2 = new Hr();
            line2.addClassName("divider");
            add(line2);

            VerticalLayout reviewFormContainer = new VerticalLayout();

            H3 ratingTitle = new H3("Bewertung schreiben");
            ratingTitle.addClassName("section-title");
            add(ratingTitle);

            reviewFormContainer.setSpacing(false);
            reviewFormContainer.setMargin(false);
            reviewFormContainer.addClassName("review-form-container");

            final int[] selectedStars = {0};
            HorizontalLayout starRatingLayout = new HorizontalLayout();
            starRatingLayout.setSpacing(false);

            for (int i = 1; i <= 5; i++) {
                int ratingValue = i;
                Span star = new Span("★");
                star.addClassName("star");
                star.addClickListener(e -> {
                    selectedStars[0] = ratingValue;
                    updateStars(starRatingLayout, ratingValue);
                });
                starRatingLayout.add(star);
            }
            reviewFormContainer.add(new Span("Deine Bewertung:"), starRatingLayout);

            TextArea commentField = new TextArea("Dein Kommentar");
            commentField.setWidthFull();

            Button saveButton = new Button("Bewertung speichern");
            saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            saveButton.addClickListener(e -> {
                System.out.println("Button wurde geklickt. Ausgewählte Sterne: " + selectedStars[0]);
                if (selectedStars[0] == 0) {
                    com.vaadin.flow.component.notification.Notification.show("Bitte Sterne wählen.");
                    return;
                }
                bewertungControl.bewertungErstellen(currentUser, currentIdeaId, selectedStars[0], commentField.getValue());
                com.vaadin.flow.component.notification.Notification.show("Bewertung gespeichert!");

                commentField.clear();
                selectedStars[0] = 0;
                updateStars(starRatingLayout, 0);
                loadReviews(currentIdeaId);
            });

            reviewFormContainer.add(commentField, saveButton);
            add(reviewFormContainer);
        }
    }

    private void updateStars(HorizontalLayout layout, int rating) {
        for (int i = 0; i < 5; i++) {
            Span star = (Span) layout.getComponentAt(i);
            if (i < rating) star.addClassName("star-selected");
            else star.removeClassName("star-selected");
        }
    }

    private void setAllFieldsReadOnly(boolean readOnly) {
        unternehmensname.setReadOnly(readOnly);
        position.setReadOnly(readOnly);
        beschreibung.setReadOnly(readOnly);
        anforderungen.setReadOnly(readOnly);
        arbeitsbedingungen.setReadOnly(readOnly);
        gesuchteSkills.setReadOnly(readOnly);
    }

    @Override
    public void setParameter(BeforeEvent event, Long ideaId) {
        this.currentIdeaId = ideaId.intValue();
        GruendungsideeDTO idee = ideaService.findIdeaById(ideaId.intValue());

        if (idee != null) {
            unternehmensname.setValue(idee.getUnternehmensname());
            position.setValue(idee.getPosition());
            beschreibung.setValue(idee.getBeschreibung());
            anforderungen.setValue(idee.getAnforderungen());
            arbeitsbedingungen.setValue(idee.getArbeitsbedingungen());

            // Skills setzen
            if (idee.getGesuchteSkills() != null) {
                gesuchteSkills.setItems(idee.getGesuchteSkills());
                gesuchteSkills.setValue(new HashSet<>(idee.getGesuchteSkills()));
            }

            loadReviews(ideaId.intValue());
        }
    }

    private void loadReviews(int ideeId) {
        reviewsContainer.removeAll();
        List<Bewertung> alleBewertungen = bewertungControl.ladeBewertungenFuerIdeeSortiert(ideeId);

        List<Bewertung> letzteZwei = alleBewertungen.stream()
                .limit(2)
                .toList();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (Bewertung b : letzteZwei) {
            VerticalLayout reviewBlock = new VerticalLayout();

            reviewBlock.setPadding(false);
            reviewBlock.setMargin(false);
            reviewBlock.setSpacing(false);

            // Sterne-String erzeugen
            Span starSpan = new Span(BewertungControl.createStarRating(b.getBewertung()));
            starSpan.addClassName("review-stars");

            String zeitstempelString = (b.getZeitstempel() != null) ? b.getZeitstempel().format(formatter) : "Datum unbekannt";
            Span dateSpan = new Span("Bewertet am: " + zeitstempelString);
            dateSpan.addClassName("review-date");

            String username = bewertungControl.ermittleBenutzernameFuerBewertung(b);
            Span userSpan = new Span("Bewertet von: " + username);
            userSpan.addClassName("review-user");

            Span commentSpan = new Span("Kommentar: " + b.getKommentar());
            commentSpan.addClassName("review-comment");

            reviewBlock.add(starSpan, dateSpan, userSpan, commentSpan);
            reviewBlock.addClassName("review-block");

            reviewsContainer.add(reviewBlock);
        }

        if (alleBewertungen.size() > 2) {
            String linkText = "Alle " + alleBewertungen.size() + " Bewertungen ansehen";
            Anchor allReviewsLink = new Anchor("show-all-ratings/" + ideeId, linkText);
            allReviewsLink.addClassName("all-reviews-link");
            reviewsContainer.add(allReviewsLink);
        }
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(unternehmensname, beschreibung, position, anforderungen, arbeitsbedingungen, gesuchteSkills);
        return formLayout;
    }
}