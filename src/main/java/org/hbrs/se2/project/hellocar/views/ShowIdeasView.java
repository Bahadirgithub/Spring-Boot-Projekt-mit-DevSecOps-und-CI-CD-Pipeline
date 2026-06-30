package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.hbrs.se2.project.hellocar.control.ManageIdeaControl;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.util.Globals;
import org.hbrs.se2.project.hellocar.control.AuthorizationControl;

import java.util.Collections;
import java.util.List;

/**
 * Darstellung einer Tabelle (bei Vaadin: ein Grid) zur Anzeige von Autos.
 * Hier wurden nur grundlegende Elemente verarbeitet. Weitere Optionen (z.B.
 * weitere Filter-Möglichkeiten) kann man hier entnehmen:
 * https://vaadin.com/components/vaadin-grid/java-examples/header-and-footer
 *
 */
@Route(value = Globals.Pages.SHOW_IDEAS, layout = AppView.class)
@PageTitle("Gründungsideen Übersicht")
@CssImport("./styles/views/showideas/show-ideas-view.css")
public class ShowIdeasView extends Div  {

    private final AuthorizationControl authorizationControl;
    private List<GruendungsideeDTO> ideeList;

    public ShowIdeasView(AuthorizationControl authorizationControl, ManageIdeaControl ideaControl) {
        this.authorizationControl = authorizationControl;
        addClassName("show-cars-view");

        // Auslesen aller Gründungsideen aus der DB
        this.ideeList = ideaControl.readAllIdeas();

        // Titel überhalb der Tabelle
        add(this.createTitle());

        // Hinzufügen der Tabelle (bei Vaadin: ein Grid)
        add(this.createGridTable());

        BenutzerDTO currentUser = (BenutzerDTO) UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER);

        if (currentUser != null && this.authorizationControl.canAccessEnterCar(currentUser)) {
            // Nur wenn es ein Gründer ist, wird die Methode ausgeführt und der Button hinzugefügt
            add(this.createAddIdeaButton());
        }
    }

    private Component createAddIdeaButton() {
        Button addIdeaButton = new Button();

        // Icon & Text vorbereiten
        Icon plusIcon = new Icon(VaadinIcon.PLUS);
        plusIcon.getStyle().set("font-size", "24px");
        Span buttonText = new Span("Gründungsidee hinzufügen");

        addIdeaButton.setIcon(plusIcon);
        addIdeaButton.setText("Gründungsidee hinzufügen");

        // Das gewünschte vertikale Styling (Icon oben, Text unten)
        addIdeaButton.getStyle().set("display", "flex");
        addIdeaButton.getStyle().set("flex-direction", "column");
        addIdeaButton.getStyle().set("align-items", "center");
        addIdeaButton.getStyle().set("justify-content", "center");
        addIdeaButton.getStyle().set("gap", "8px");
        addIdeaButton.getStyle().set("height", "auto");
        addIdeaButton.getStyle().set("padding", "16px 24px");

        // Zentrierung unter dem Div & Abstand nach oben zur Tabelle
        addIdeaButton.getStyle().set("margin", "25px auto 0 auto");

        // StartMeUp Primär-Look (z.B. Blau/Grün)
        addIdeaButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Klick-Logik: Weiterleitung zur Erstellungsseite (EnterIdeasView)
        addIdeaButton.addClickListener(e -> UI.getCurrent().navigate(EnterIdeasView.class));

        return addIdeaButton;
    }

    private Component createGridTable() {
        Grid<GruendungsideeDTO> grid = new Grid<>();

        // Befüllen der Tabelle mit den Gründungsideen
        ListDataProvider<GruendungsideeDTO> dataProvider = new ListDataProvider<>(ideeList);
        grid.setDataProvider(dataProvider);

        // Spalten-Definitionen basierend auf deinen DB-Feldern
        Grid.Column<GruendungsideeDTO> unternehmenColumn = grid
                .addColumn(GruendungsideeDTO::getUnternehmensname) // Fallback, solange kein Name da ist
                .setHeader("Unternehmen / Gründer");

        Grid.Column<GruendungsideeDTO> beschreibungColumn = grid
                .addColumn(GruendungsideeDTO::getBeschreibung)
                .setHeader("Beschreibung");

        Grid.Column<GruendungsideeDTO> positionColumn = grid
                .addColumn(GruendungsideeDTO::getPosition)
                .setHeader("Position");

        Grid.Column<GruendungsideeDTO> anforderungenColumn = grid
                .addColumn(GruendungsideeDTO::getAnforderungen)
                .setHeader("Anforderungen");

        Grid.Column<GruendungsideeDTO> skillsColumn = grid
                .addColumn(idee -> formatSkillsPreview(idee.getGesuchteSkills()))
                .setHeader("Gesuchte Skills");

        Grid.Column<GruendungsideeDTO> bedingungenColumn = grid
                .addColumn(GruendungsideeDTO::getArbeitsbedingungen)
                .setHeader("Arbeitsbedingungen");

        // Filter-Zeile im Header des Grids aktivieren
        HeaderRow filterRow = grid.appendHeaderRow();


        // 1. Filter: Unternehmen / Gründer
        TextField unternehmenFilterField = new TextField();
        unternehmenFilterField.addValueChangeListener(event -> dataProvider.addFilter(
                idee -> StringUtils.containsIgnoreCase(idee.getUnternehmensname(), unternehmenFilterField.getValue())
        ));
        unternehmenFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        unternehmenFilterField.setPlaceholder("Filter Unternehmen");
        unternehmenFilterField.setSizeFull();
        filterRow.getCell(unternehmenColumn).setComponent(unternehmenFilterField);

        // 2. Filter: Beschreibung
        TextField beschreibungFilterField = new TextField();
        beschreibungFilterField.addValueChangeListener(event -> dataProvider.addFilter(
                idee -> StringUtils.containsIgnoreCase(idee.getBeschreibung(), beschreibungFilterField.getValue())
        ));
        beschreibungFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        beschreibungFilterField.setPlaceholder("Filter Beschreibung");
        beschreibungFilterField.setSizeFull();
        filterRow.getCell(beschreibungColumn).setComponent(beschreibungFilterField);

        // 3. Filter: Position
        TextField positionFilterField = new TextField();
        positionFilterField.addValueChangeListener(event -> dataProvider.addFilter(
                idee -> StringUtils.containsIgnoreCase(idee.getPosition(), positionFilterField.getValue())
        ));
        positionFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        positionFilterField.setPlaceholder("Filter Position");
        positionFilterField.setSizeFull();
        filterRow.getCell(positionColumn).setComponent(positionFilterField);

        // 4. Filter: Anforderungen
        TextField anforderungenFilterField = new TextField();
        anforderungenFilterField.addValueChangeListener(event -> dataProvider.addFilter(
                idee -> StringUtils.containsIgnoreCase(idee.getAnforderungen(), anforderungenFilterField.getValue())
        ));
        anforderungenFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        anforderungenFilterField.setPlaceholder("Filter Anforderungen");
        anforderungenFilterField.setSizeFull();
        filterRow.getCell(anforderungenColumn).setComponent(anforderungenFilterField);

        // 5. Filter: Gesuchte Skills
        TextField skillsFilterField = new TextField();
        skillsFilterField.addValueChangeListener(event -> dataProvider.addFilter(
                idee -> idee.getGesuchteSkills() != null && idee.getGesuchteSkills().stream()
                        .anyMatch(skill -> StringUtils.containsIgnoreCase(skill, skillsFilterField.getValue()))
        ));
        skillsFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        skillsFilterField.setPlaceholder("Filter Skills");
        skillsFilterField.setSizeFull();
        filterRow.getCell(skillsColumn).setComponent(skillsFilterField);

        // 6. Filter: Arbeitsbedingungen
        TextField bedingungenFilterField = new TextField();
        bedingungenFilterField.addValueChangeListener(event -> dataProvider.addFilter(
                idee -> StringUtils.containsIgnoreCase(idee.getArbeitsbedingungen(), bedingungenFilterField.getValue())
        ));
        bedingungenFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        bedingungenFilterField.setPlaceholder("Filter Bedingungen");
        bedingungenFilterField.setSizeFull();
        filterRow.getCell(bedingungenColumn).setComponent(bedingungenFilterField);

        grid.setHeight("600px");

        return grid;
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

    private Component createTitle() {
        return new H3("Nach Gründungsideen suchen");
    }
}
