package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.hbrs.se2.project.hellocar.control.ManageIdeaControl;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.util.Globals;

import java.util.List;

/**
 * Darstellung einer Tabelle (bei Vaadin: ein Grid) zur Anzeige von Autos.
 * Hier wurden nur grundlegende Elemente verarbeitet. Weitere Optionen (z.B.
 * weitere Filter-Möglichkeiten) kann man hier entnehmen:
 * https://vaadin.com/components/vaadin-grid/java-examples/header-and-footer
 *
 */
@Route(value = Globals.Pages.SHOW_CARS, layout = AppView.class)
@PageTitle("Gründungsideen")
@CssImport("./styles/views/showcars/show-cars-view.css")
public class ShowIdeasView extends Div  {

    private List<GruendungsideeDTO> ideeList;

    public ShowIdeasView(ManageIdeaControl ideaControl) {
            addClassName("show-cars-view");

            // Auslesen aller Gründungsideen aus der DB
            this.ideeList = ideaControl.readAllIdeas();

            // Titel überhalb der Tabelle
            add(this.createTitle());

            // Hinzufügen der Tabelle (bei Vaadin: ein Grid)
            add(this.createGridTable());
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

        Grid.Column<GruendungsideeDTO> bedingungenColumn = grid
                .addColumn(GruendungsideeDTO::getArbeitsbedingungen)
                .setHeader("Arbeitsbedingungen");

        // Ein Filter für die Spalte "Position"
        HeaderRow filterRow = grid.appendHeaderRow();
        TextField positionFilterField = new TextField();
        positionFilterField.addValueChangeListener(event -> dataProvider.addFilter(
                idee -> StringUtils.containsIgnoreCase(idee.getPosition(), positionFilterField.getValue())
        ));
        positionFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        positionFilterField.setPlaceholder("Filter Position");
        positionFilterField.setSizeFull();

        filterRow.getCell(positionColumn).setComponent(positionFilterField);

        return grid;
    }

    private Component createTitle() {
        return new H3("Nach Gründungsideen suchen");
    }


};
