package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.hbrs.se2.project.hellocar.control.AuthorizationControl;
import org.hbrs.se2.project.hellocar.control.ChatControl;
import org.hbrs.se2.project.hellocar.control.LoginControl;
import org.hbrs.se2.project.hellocar.control.MeineGruendungsideenControl;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.entities.Bewertung;
import org.hbrs.se2.project.hellocar.entities.Skill;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.util.Globals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * View für Gründer.
 * Hier sieht der Gründer seine eigenen Gründungsideen + passende Bewerbungen/Bewertungen.
 */
@Route(value = "meine-gruendungsideen", layout = AppView.class)
@PageTitle("Meine Gründungsideen")
@CssImport("./styles/views/showideas/show-ideas-view.css")
public class MeineGruendungsideenView extends Div {

    private final AuthorizationControl authorizationControl;
    private final MeineGruendungsideenControl meineGruendungsideenControl;

    private final VerticalLayout bewerbungenContainer = new VerticalLayout();
    private final VerticalLayout bewertungenContainer = new VerticalLayout();

    private BenutzerDTO currentUser;

    private ChatView chatView;

    public MeineGruendungsideenView(AuthorizationControl authorizationControl,
                                    MeineGruendungsideenControl meineGruendungsideenControl , ChatControl chatControl, LoginControl loginControl) {
        this.authorizationControl = authorizationControl;
        this.meineGruendungsideenControl = meineGruendungsideenControl;
        this.chatView = new ChatView(chatControl,loginControl);

        addClassName("show-cars-view");
        add(this.chatView);
        currentUser = (BenutzerDTO) UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER);

        // Falls jemand die Route direkt aufruft und nicht eingeloggt ist.
        if (currentUser == null) {
            add(new H3("Bitte zuerst einloggen."));
            return;
        }

        // Sollte von Studenten nicht sichtbar sein!
        if (!authorizationControl.canAccessEnterCar(currentUser)) {
            add(new H3("Diese Seite ist nur für Gründer sichtbar."));
            return;
        }

        add(new H3("Meine Gründungsideen"));
        add(createGridTable());
        add(createDetailLayout());
        add(this.createChatbotAvatar());
    }

    private Component createGridTable() {
        Grid<GruendungsideeDTO> grid = new Grid<>();

        // Erstmal alle eigenen Ideen holen. Filtern passiert im Control, nicht hier in der View.
        List<GruendungsideeDTO> eigeneIdeen =
                meineGruendungsideenControl.ladeEigeneGruendungsideen(currentUser.getUserId());

        ListDataProvider<GruendungsideeDTO> dataProvider = new ListDataProvider<>(eigeneIdeen);
        grid.setDataProvider(dataProvider);

        grid.addColumn(GruendungsideeDTO::getUnternehmensname)
                .setHeader("Unternehmen");

        grid.addColumn(GruendungsideeDTO::getBeschreibung)
                .setHeader("Beschreibung");

        grid.addColumn(GruendungsideeDTO::getPosition)
                .setHeader("Position");

        grid.addColumn(GruendungsideeDTO::getAnforderungen)
                .setHeader("Anforderungen");

        grid.addColumn(idee -> formatSkillsPreview(idee.getGesuchteSkills()))
                .setHeader("Gesuchte Skills");

        grid.addColumn(GruendungsideeDTO::getArbeitsbedingungen)
                .setHeader("Arbeitsbedingungen");

        grid.setHeight("350px");

        // Bei Auswahl einer Idee werden die Details darunter neu geladen.
        grid.asSingleSelect().addValueChangeListener(event -> {
            GruendungsideeDTO selectedIdea = event.getValue();

            if (selectedIdea != null) {
                ladeDetailsZurIdee(selectedIdea);
            }
        });

        // Doppelklick geadded, weil praktischer.
        grid.addItemDoubleClickListener(event -> ladeDetailsZurIdee(event.getItem()));

        return grid;
    }

    private Component createDetailLayout() {
        HorizontalLayout detailLayout = new HorizontalLayout();
        detailLayout.setWidthFull();
        detailLayout.setSpacing(true);
        detailLayout.getStyle().set("margin-top", "25px");
        detailLayout.getStyle().set("align-items", "flex-start");

        bewerbungenContainer.setPadding(false);
        bewerbungenContainer.setSpacing(true);
        bewerbungenContainer.setWidth("65%");
        bewerbungenContainer.add(new Span("Wähle eine Gründungsidee aus, um die Bewerbungen zu sehen."));

        bewertungenContainer.setPadding(false);
        bewertungenContainer.setSpacing(true);
        bewertungenContainer.setWidth("35%");
        bewertungenContainer.add(new Span("Wähle eine Gründungsidee aus, um die Bewertungen zu sehen."));

        detailLayout.add(bewerbungenContainer, bewertungenContainer);

        return detailLayout;
    }

    private void ladeDetailsZurIdee(GruendungsideeDTO idee) {
        if (idee == null) {
            return;
        }

        ladeBewerbungen(idee);
        ladeBewertungen(idee);
    }

    private void ladeBewerbungen(GruendungsideeDTO idee) {
        bewerbungenContainer.removeAll();

        H3 titel = new H3("Eingegangene Bewerbungen für: " + sichererText(idee.getPosition()));
        bewerbungenContainer.add(titel);

        List<BewerbungDTO> bewerbungen =
                meineGruendungsideenControl.ladeBewerbungenFuerEigeneIdee(
                        currentUser.getUserId(),
                        idee.getIdeeId()
                );

        if (bewerbungen.isEmpty()) {
            bewerbungenContainer.add(new Span("Für diese Gründungsidee sind noch keine Bewerbungen eingegangen."));
            return;
        }

        for (BewerbungDTO bewerbung : bewerbungen) {
            bewerbungenContainer.add(createBewerbungBlock(bewerbung, idee));
        }
    }

    private void ladeBewertungen(GruendungsideeDTO idee) {
        bewertungenContainer.removeAll();

        H3 titel = new H3("Bewertungen");
        bewertungenContainer.add(titel);

        List<Bewertung> bewertungen =
                meineGruendungsideenControl.ladeBewertungenFuerEigeneIdee(
                        currentUser.getUserId(),
                        idee.getIdeeId()
                );

        if (bewertungen.isEmpty()) {
            bewertungenContainer.add(new Span("Für diese Gründungsidee wurden noch keine Bewertungen abgegeben."));
            return;
        }

        // Auf der Detailseite zeigen wir nur die ersten drei, sonst wird der rechte Bereich schnell zu lang.
        bewertungen.stream().limit(3).forEach(bewertung -> bewertungenContainer.add(createBewertungBlock(bewertung)));

        if (bewertungen.size() > 3) {
            Anchor alleBewertungenLink = new Anchor(
                    "show-all-ratings/" + idee.getIdeeId(),
                    "Alle " + bewertungen.size() + " Bewertungen ansehen"
            );

            alleBewertungenLink.getStyle().set("margin-top", "8px");
            bewertungenContainer.add(alleBewertungenLink);
        }
    }

    private Component createBewerbungBlock(BewerbungDTO bewerbung, GruendungsideeDTO idee) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(true);
        block.setSpacing(false);
        block.getStyle().set("border", "1px solid #d0d7de");
        block.getStyle().set("border-radius", "8px");
        block.getStyle().set("margin-bottom", "15px");
        block.getStyle().set("background", "white");
        block.setWidthFull();

        BenutzerDTO benutzer =
                meineGruendungsideenControl.ladeStudentBenutzer(bewerbung.getStudentUserId());

        Student student =
                meineGruendungsideenControl.ladeStudentProfil(bewerbung.getStudentUserId());

        String name = "Unbekannter Student";

        if (benutzer != null) {
            name = sichererText(benutzer.getVorname()) + " " + sichererText(benutzer.getNachname());
        }

        HorizontalLayout kopfLayout = new HorizontalLayout();
        kopfLayout.setWidthFull();
        kopfLayout.getStyle().set("justify-content", "space-between");
        kopfLayout.getStyle().set("align-items", "center");

        H4 nameTitel = new H4(name);
        nameTitel.getStyle().set("margin", "0");

        Button chatButton = new Button("Chat");
        chatButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        // Chat ist im Moment nur vorbereitet. Die eigentliche Chat-Logik ggf. später implementieren
        kopfLayout.add(nameTitel, chatButton);
        block.add(kopfLayout);

        block.add(new Span("Beworben auf: " + sichererText(idee.getPosition())));
        block.add(createStatusAuswahl(bewerbung));

        if (bewerbung.getBewerbungsdatum() != null) {
            block.add(new Span("Bewerbungsdatum: "
                    + bewerbung.getBewerbungsdatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        }

        if (benutzer != null) {
            block.add(new Span("Username: " + sichererText(benutzer.getUsername())));
            block.add(new Span("E-Mail: " + sichererText(benutzer.getEmail())));
            block.add(new Span("Telefonnummer: " + sichererText(benutzer.getTelefonnummer())));

            if (benutzer.getGeburtstag() != null) {
                block.add(new Span("Geburtstag: "
                        + benutzer.getGeburtstag().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            }
        }

        if (student != null) {
            block.add(new Span("Student-ID: " + sichererText(student.getStudentId())));
            block.add(new Span("Universität: " + sichererText(student.getUniversitaet())));
            block.add(new Span("Semester: " + student.getSemester()));
            block.add(new Span("Abschluss: " + sichererText(student.getAbschluss())));

            block.add(new Span("Skills:"));
            block.add(createStudentSkillsAnzeige(student, idee));
        }

        HorizontalLayout dateiLayout = new HorizontalLayout();
        dateiLayout.setSpacing(true);
        dateiLayout.getStyle().set("margin-top", "10px");

        Component anschreibenLink = createDownloadLink(
                "Anschreiben öffnen",
                bewerbung.getAnschreibenDateiname(),
                bewerbung.getAnschreibenDateipfad()
        );

        Component lebenslaufLink = createDownloadLink(
                "Lebenslauf öffnen",
                bewerbung.getLebenslaufDateiname(),
                bewerbung.getLebenslaufDateipfad()
        );

        dateiLayout.add(anschreibenLink, lebenslaufLink);
        block.add(dateiLayout);

        return block;
    }

    private Component createStatusAuswahl(BewerbungDTO bewerbung) {
        ComboBox<String> statusBox = new ComboBox<>("Status");
        statusBox.setItems(meineGruendungsideenControl.ladeErlaubteStatus());
        statusBox.setWidth("220px");

        String aktuellerStatus;

        if (bewerbung.getStatus() == null || bewerbung.getStatus().isBlank()) {
            aktuellerStatus = "Eingegangen";
        } else {
            aktuellerStatus = bewerbung.getStatus();
        }

        statusBox.setValue(aktuellerStatus);

        statusBox.addValueChangeListener(event -> {
            // Wichtig, sonst wird eventuell auch beim Setzen aus dem Code direkt gespeichert.
            if (!event.isFromClient()) {
                return;
            }

            boolean gespeichert = meineGruendungsideenControl.aendereStatusFuerBewerbung(
                    currentUser.getUserId(),
                    bewerbung.getBewerbungId(),
                    event.getValue()
            );

            if (gespeichert) {
                Notification.show("Status wurde aktualisiert.");
            } else {
                Notification.show("Status konnte nicht geändert werden.");

                // Zurück auf den alten Wert, damit die Anzeige nicht etwas falsches zeigt.
                statusBox.setValue(event.getOldValue());
            }
        });

        return statusBox;
    }

    private Component createStudentSkillsAnzeige(Student student, GruendungsideeDTO idee) {
        if (student.getSkillEntities() == null || student.getSkillEntities().isEmpty()) {
            return new Span("-");
        }

        Div skillsLayout = new Div();
        skillsLayout.getStyle().set("display", "flex");
        skillsLayout.getStyle().set("flex-wrap", "wrap");
        skillsLayout.getStyle().set("gap", "6px");
        skillsLayout.getStyle().set("max-width", "100%");
        skillsLayout.getStyle().set("margin-top", "4px");

        /*
         * Passende Skills sollen vorne stehen.
         * Das Matching selbst liegt im Control, weil die View nicht wissen muss,
         * wie Skills genau verglichen werden.
         */
        List<String> sortierteSkills = student.getSkillEntities().stream()
                .map(Skill::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(Comparator
                        .comparing((String skill) -> !meineGruendungsideenControl.istSkillPassendZurIdee(skill, idee))
                        .thenComparing(String::compareToIgnoreCase))
                .toList();

        for (String skill : sortierteSkills) {
            boolean passt = meineGruendungsideenControl.istSkillPassendZurIdee(skill, idee);

            Span chip = new Span(skill);
            chip.getStyle().set("padding", "3px 8px");
            chip.getStyle().set("border-radius", "12px");
            chip.getStyle().set("font-size", "0.85em");

            // Lange Skillnamen sollen nicht mehr rechts aus dem Bewerbungsblock laufen.
            chip.getStyle().set("max-width", "100%");
            chip.getStyle().set("white-space", "normal");
            chip.getStyle().set("overflow-wrap", "anywhere");

            if (passt) {
                chip.getStyle().set("background", "#d4f8d4");
                chip.getStyle().set("border", "1px solid #39a845");
                chip.getStyle().set("color", "#176b22");
            } else {
                chip.getStyle().set("background", "#eef0f2");
                chip.getStyle().set("border", "1px solid #c8ccd0");
                chip.getStyle().set("color", "#3d444d");
            }

            skillsLayout.add(chip);
        }

        return skillsLayout;
    }

    private Component createBewertungBlock(Bewertung bewertung) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(true);
        block.setSpacing(false);
        block.getStyle().set("border", "1px solid #d0d7de");
        block.getStyle().set("border-radius", "8px");
        block.getStyle().set("margin-bottom", "12px");
        block.getStyle().set("background", "white");
        block.setWidthFull();

        Span sterne = new Span(createStarRating(bewertung.getBewertung()));
        sterne.getStyle().set("color", "#B8860B");
        sterne.getStyle().set("font-size", "1.7em");
        sterne.getStyle().set("line-height", "1.1");
        block.add(sterne);

        if (bewertung.getZeitstempel() != null) {
            block.add(createKleinerText("Bewertet am: "
                    + bewertung.getZeitstempel().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
        } else {
            block.add(createKleinerText("Bewertet am: Datum unbekannt"));
        }

        BenutzerDTO bewertenderStudent =
                meineGruendungsideenControl.ladeStudentBenutzer(bewertung.getStudentUserId());

        String username = "Unbekannt";

        if (bewertenderStudent != null) {
            username = sichererText(bewertenderStudent.getUsername());
        }

        block.add(createKleinerText("Bewertet von: " + username));

        Span kommentar = new Span("Kommentar: " + sichererText(bewertung.getKommentar()));
        kommentar.getStyle().set("margin-top", "6px");
        kommentar.getStyle().set("white-space", "normal");
        kommentar.getStyle().set("overflow-wrap", "anywhere");
        block.add(kommentar);

        return block;
    }

    private Span createKleinerText(String text) {
        Span span = new Span(text);
        span.getStyle().set("font-size", "0.9em");
        span.getStyle().set("color", "gray");

        return span;
    }

    private String createStarRating(int rating) {
        StringBuilder stars = new StringBuilder();
        int maxStars = 5;

        // Zur Sicherheit begrenzen, falls aus der DB mal ein falscher Wert kommt.
        if (rating < 0) {
            rating = 0;
        }

        if (rating > maxStars) {
            rating = maxStars;
        }

        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }

        for (int i = rating; i < maxStars; i++) {
            stars.append("☆");
        }

        return stars.toString();
    }

    private Component createDownloadLink(String text, String dateiname, String dateipfad) {
        if (dateipfad == null || dateipfad.isBlank()) {
            return new Span(text + ": keine Datei vorhanden");
        }

        Path pfad = Paths.get(dateipfad);

        final String downloadName;
        if (dateiname == null || dateiname.isBlank()) {
            downloadName = "datei.pdf";
        } else {
            downloadName = dateiname;
        }

        StreamResource resource = new StreamResource(downloadName, () -> {
            try {
                return Files.newInputStream(pfad);
            } catch (IOException e) {
                // Für das Projekt erstmal ausreichend. Später könnte man hier noch Logging einbauen.
                System.out.println("Datei konnte nicht geöffnet werden: " + downloadName);
                return new ByteArrayInputStream(new byte[0]);
            }
        });

        Anchor anchor = new Anchor(resource, text);
        anchor.getElement().setAttribute("download", true);

        return anchor;
    }

    private String formatSkillsPreview(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "-";
        }

        List<String> sortierteSkills = skills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();

        if (sortierteSkills.isEmpty()) {
            return "-";
        }

        int maxAnzeigen = 3;
        int anzahl = Math.min(maxAnzeigen, sortierteSkills.size());

        String preview = String.join(", ", sortierteSkills.subList(0, anzahl));
        int rest = sortierteSkills.size() - anzahl;

        if (rest > 0) {
            return preview + " +" + rest;
        }

        return preview;
    }

    private String sichererText(String text) {
        if (text == null || text.isBlank()) {
            return "-";
        }

        return text;
    }

  /*  private Component createChatbotAvatar(){
        Avatar chatbotAvatar = new Avatar();
        chatbotAvatar.setId("chatbot-avatar");
        chatbotAvatar.setImage("images/logo.png");

        chatbotAvatar.getStyle().set("position", "absolute");
        chatbotAvatar.getStyle().set("bottom", "20px");
        chatbotAvatar.getStyle().set("right", "30px");

        chatbotAvatar.getElement().addEventListener("click", event -> {chatView.open();});

        return chatbotAvatar;
    }*/

    private Component createChatbotAvatar(){
        Avatar chatbotAvatar = new Avatar();
        chatbotAvatar.setId("chatbot-avatar");
        chatbotAvatar.setImage("images/logo.png");

        // 1. WICHTIG: fixed statt absolute nutzen, damit der Button am Bildschirm "klebt"
        chatbotAvatar.getStyle().set("position", "absolute");
        chatbotAvatar.getStyle().set("bottom", "20px");
        chatbotAvatar.getStyle().set("right", "30px");

        chatbotAvatar.getElement().addEventListener("click", event -> { chatView.open(); });

        return chatbotAvatar;
    }

}