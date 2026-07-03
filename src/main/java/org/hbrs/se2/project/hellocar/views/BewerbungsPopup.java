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
 *
 * WICHTIG: Die Upload-Felder (anschreibenBuffer/lebenslaufBuffer) sind aktuell
 * NICHT an das Speichern angebunden. Sie sind rein visuell vorbereitet, damit
 * das UI schon so aussieht wie später evtl. "gewünscht". Der komplette Datei-Speicher-
 * prozess (Zielordner auf dem Server, das Schreiben der Datei aus dem
 * Buffer und den Pfad in die DB übernehmen) ist noch offen, das müssen wir noch klären.
 * Bis dahin werden beim Speichern einer Bewerbung bewusst nur "null"-Werte für alle vier Datei-Felder
 * übergeben.
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

        // Hinweis: Diese Upload-Komponente ist nur UI-Vorschau, siehe Klassenkommentar oben.
        // anschreibenBuffer wird aktuell NICHT ausgelesen oder gespeichert.
        Upload anschreibenUpload = new Upload(anschreibenBuffer);
        anschreibenUpload.setAcceptedFileTypes("application/pdf", ".pdf");
        anschreibenUpload.setMaxFiles(1);

        // Hinweis: lebenslaufBuffer wird ebenfalls aktuell NICHT ausgelesen oder gespeichert.
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
            /* Datei-Uploads werden bewusst noch nicht ausgewertet/gespeichert
            // (siehe Klassenkommentar)
            bewerbungControl.bewerben(
                    currentUser.getUserId(),
                    ideeId,
                    null,
                    null,
                    null,
                    null
            );
             */

            Notification.show("Bewerbung senden ist noch nicht vollständig implementiert.");
            close();

        });

        HorizontalLayout buttonLayout = new HorizontalLayout(absendenButton, abbrechenButton);

        add(inhalt);
        getFooter().add(buttonLayout);
    }
}