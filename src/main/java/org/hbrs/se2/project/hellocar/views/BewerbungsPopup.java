package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.hbrs.se2.project.hellocar.control.BewerbungControl;
import org.hbrs.se2.project.hellocar.control.exception.BewerbungBereitsVorhandenException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;

/**
 * Pop-up-Fenster für die Bewerbung eines Studenten auf eine Gründungsidee.
 * Der eigentliche Speicherort der hochgeladenen Dateien ist noch offen (TODO),
 * aktuell wird nur der Dateiname zwischengespeichert.
 */
public class BewerbungsPopup extends Dialog {

    private final MemoryBuffer anschreibenBuffer = new MemoryBuffer();
    private final MemoryBuffer lebenslaufBuffer = new MemoryBuffer();

    public BewerbungsPopup(BewerbungControl bewerbungControl, BenutzerDTO currentUser, int ideeId) {
        setHeaderTitle("Jetzt bewerben");
        setWidth("450px");

        VerticalLayout inhalt = new VerticalLayout();
        inhalt.setPadding(false);
        inhalt.setSpacing(true);

        TextArea anschreibenText = new TextArea("Anschreiben (optional, als Hinweistext)");
        anschreibenText.setWidthFull();

        // TODO: Zielordner für die tatsächliche Ablage der Dateien ist noch offen.
        Upload anschreibenUpload = new Upload(anschreibenBuffer);
        anschreibenUpload.setAcceptedFileTypes("application/pdf", ".pdf");
        anschreibenUpload.setMaxFiles(1);

        Upload lebenslaufUpload = new Upload(lebenslaufBuffer);
        lebenslaufUpload.setAcceptedFileTypes("application/pdf", ".pdf");
        lebenslaufUpload.setMaxFiles(1);

        inhalt.add(
                new H3("Anschreiben (PDF)"), anschreibenUpload,
                new H3("Lebenslauf (PDF)"), lebenslaufUpload
        );

        Button abbrechenButton = new Button("Abbrechen", e -> close());

        Button absendenButton = new Button("Bewerbung absenden");
        absendenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        absendenButton.addClickListener(e -> {
            try {
                // TODO: Dateipfad noch nicht final - aktuell nur Dateiname als Platzhalter
                String anschreibenDateiname = anschreibenBuffer.getFileName();
                String lebenslaufDateiname = lebenslaufBuffer.getFileName();

                bewerbungControl.bewerben(
                        currentUser.getUserId(),
                        ideeId,
                        anschreibenDateiname,
                        null, // Dateipfad TODO
                        lebenslaufDateiname,
                        null  // Dateipfad TODO
                );

                Notification.show("Bewerbung erfolgreich eingereicht.");
                close();

            } catch (BewerbungBereitsVorhandenException ex) {
                Notification.show(ex.getMessage());
            }
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(absendenButton, abbrechenButton);

        add(inhalt);
        getFooter().add(buttonLayout);
    }
}