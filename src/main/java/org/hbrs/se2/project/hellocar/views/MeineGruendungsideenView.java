package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.hbrs.se2.project.hellocar.control.AuthorizationControl;
import org.hbrs.se2.project.hellocar.control.BewerbungControl;
import org.hbrs.se2.project.hellocar.control.ManageIdeaControl;
import org.hbrs.se2.project.hellocar.control.VerwaltungControl;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.entities.Skill;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.util.Globals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * View für Gründer: zeigt nur eigene Gründungsideen und die Bewerbungen dazu.
 */
@Route(value = "meine-gruendungsideen", layout = AppView.class)
@PageTitle("Meine Gründungsideen")
@CssImport("./styles/views/showideas/show-ideas-view.css")
public class MeineGruendungsideenView extends Div {

    private final AuthorizationControl authorizationControl;
    private final ManageIdeaControl ideaControl;
    private final BewerbungControl bewerbungControl;
    private final VerwaltungControl verwaltungControl;

    private final VerticalLayout bewerbungenContainer = new VerticalLayout();
    private BenutzerDTO currentUser;

    public MeineGruendungsideenView(AuthorizationControl authorizationControl,
                                    ManageIdeaControl ideaControl,
                                    BewerbungControl bewerbungControl,
                                    VerwaltungControl verwaltungControl) {
        this.authorizationControl = authorizationControl;
        this.ideaControl = ideaControl;
        this.bewerbungControl = bewerbungControl;
        this.verwaltungControl = verwaltungControl;

        addClassName("show-cars-view");

        currentUser = (BenutzerDTO) UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER);

        if (currentUser == null) {
            add(new H3("Bitte zuerst einloggen."));
            return;
        }

        if (!authorizationControl.canAccessEnterCar(currentUser)) {
            add(new H3("Diese Seite ist nur für Gründer sichtbar."));
            return;
        }

        add(new H3("Meine Gründungsideen"));
        add(createGridTable());

        bewerbungenContainer.setPadding(false);
        bewerbungenContainer.setSpacing(true);
        bewerbungenContainer.getStyle().set("margin-top", "25px");
        bewerbungenContainer.add(new Span("Wähle eine Gründungsidee aus, um die Bewerbungen zu sehen."));
        add(bewerbungenContainer);
    }

    private Component createGridTable() {
        Grid<GruendungsideeDTO> grid = new Grid<>();

        // Es werden nur die Ideen des eingeloggten Gründers geladen.
        List<GruendungsideeDTO> eigeneIdeen = ideaControl.readIdeasByGruender(currentUser.getUserId());
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

        grid.asSingleSelect().addValueChangeListener(event -> {
            GruendungsideeDTO selectedIdea = event.getValue();
            if (selectedIdea != null) {
                ladeBewerbungen(selectedIdea);
            }
        });

        grid.addItemDoubleClickListener(event -> ladeBewerbungen(event.getItem()));

        return grid;
    }

    private void ladeBewerbungen(GruendungsideeDTO idee) {
        bewerbungenContainer.removeAll();

        H3 titel = new H3("Eingegangene Bewerbungen für: " + sichererText(idee.getPosition()));
        bewerbungenContainer.add(titel);

        List<BewerbungDTO> bewerbungen = bewerbungControl.ladeBewerbungenFuerEigeneIdee(
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

    private Component createBewerbungBlock(BewerbungDTO bewerbung, GruendungsideeDTO idee) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(true);
        block.setSpacing(false);
        block.getStyle().set("border", "1px solid #d0d7de");
        block.getStyle().set("border-radius", "8px");
        block.getStyle().set("margin-bottom", "15px");
        block.getStyle().set("background", "white");

        BenutzerDTO benutzer = verwaltungControl.ladeBenutzer(bewerbung.getStudentUserId());
        Student student = verwaltungControl.ladeStudent(bewerbung.getStudentUserId());

        String name = "Unbekannter Student";
        if (benutzer != null) {
            name = sichererText(benutzer.getVorname()) + " " + sichererText(benutzer.getNachname());
        }

        block.add(new H4(name));
        block.add(new Span("Beworben auf: " + sichererText(idee.getPosition())));
        block.add(new Span("Status: " + sichererText(bewerbung.getStatus())));

        if (bewerbung.getBewerbungsdatum() != null) {
            block.add(new Span("Bewerbungsdatum: " + bewerbung.getBewerbungsdatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        }

        if (benutzer != null) {
            block.add(new Span("Username: " + sichererText(benutzer.getUsername())));
            block.add(new Span("E-Mail: " + sichererText(benutzer.getEmail())));
            block.add(new Span("Telefonnummer: " + sichererText(benutzer.getTelefonnummer())));
            if (benutzer.getGeburtstag() != null) {
                block.add(new Span("Geburtstag: " + benutzer.getGeburtstag().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            }
        }

        if (student != null) {
            block.add(new Span("Student-ID: " + sichererText(student.getStudentId())));
            block.add(new Span("Universität: " + sichererText(student.getUniversitaet())));
            block.add(new Span("Semester: " + student.getSemester()));
            block.add(new Span("Abschluss: " + sichererText(student.getAbschluss())));
            block.add(new Span("Skills: " + formatStudentSkills(student)));
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

    private Component createDownloadLink(String text, String dateiname, String dateipfad) {
        if (dateipfad == null || dateipfad.isBlank()) {
            return new Span(text + ": keine Datei vorhanden");
        }

        Path pfad = Paths.get(dateipfad);
        String downloadName = (dateiname == null || dateiname.isBlank()) ? "datei.pdf" : dateiname;

        StreamResource resource = new StreamResource(downloadName, () -> {
            try {
                return Files.newInputStream(pfad);
            } catch (IOException e) {
                System.out.println("Datei konnte nicht geöffnet werden: " + downloadName);
                return new ByteArrayInputStream(new byte[0]);
            }
        });

        Anchor anchor = new Anchor(resource, text);
        anchor.getElement().setAttribute("download", true);
        return anchor;
    }

    private String formatStudentSkills(Student student) {
        if (student.getSkillEntities() == null || student.getSkillEntities().isEmpty()) {
            return "-";
        }

        return student.getSkillEntities().stream()
                .map(Skill::getName)
                .filter(name -> name != null && !name.isBlank())
                .sorted()
                .collect(Collectors.joining(", "));
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

        return rest > 0 ? preview + " +" + rest : preview;
    }

    private String sichererText(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }
}
