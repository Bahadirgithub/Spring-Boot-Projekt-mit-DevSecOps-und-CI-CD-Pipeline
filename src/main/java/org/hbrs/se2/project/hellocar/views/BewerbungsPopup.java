package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.hbrs.se2.project.hellocar.control.BewerbungControl;
import org.hbrs.se2.project.hellocar.control.exception.BewerbungBereitsVorhandenException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;

/**
 * Pop-up-Fenster für die Bewerbung eines Studenten auf eine Gründungsidee.
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

        // Upload für das Anschreiben als PDF.
        Upload anschreibenUpload = new Upload(anschreibenBuffer);
        anschreibenUpload.setAcceptedFileTypes("application/pdf", ".pdf");
        anschreibenUpload.setMaxFiles(1);
        anschreibenUpload.addSucceededListener(event -> Notification.show("Anschreiben wurde hochgeladen."));
        anschreibenUpload.addFileRejectedListener(event -> Notification.show("Bitte nur PDF-Dateien hochladen."));

        // Upload für den Lebenslauf als PDF.
        Upload lebenslaufUpload = new Upload(lebenslaufBuffer);
        lebenslaufUpload.setAcceptedFileTypes("application/pdf", ".pdf");
        lebenslaufUpload.setMaxFiles(1);
        lebenslaufUpload.addSucceededListener(event -> Notification.show("Lebenslauf wurde hochgeladen."));
        lebenslaufUpload.addFileRejectedListener(event -> Notification.show("Bitte nur PDF-Dateien hochladen."));

        inhalt.add(
                new H3("Anschreiben (PDF)"), anschreibenUpload,
                new H3("Lebenslauf (PDF)"), lebenslaufUpload
        );

        Button abbrechenButton = new Button("Abbrechen", e -> close());

        Button absendenButton = new Button("Bewerbung absenden");
        absendenButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        absendenButton.addClickListener(e -> {
            String anschreibenDateiname = anschreibenBuffer.getFileName();
            String lebenslaufDateiname = lebenslaufBuffer.getFileName();

            if (dateiFehlt(anschreibenDateiname) || dateiFehlt(lebenslaufDateiname)) {
                Notification.show("Bitte Anschreiben und Lebenslauf als PDF hochladen.");
                return;
            }

            try {
                bewerbungControl.bewerbenMitDateien(
                        currentUser.getUserId(),
                        ideeId,
                        anschreibenBuffer.getFileName(),
                        anschreibenBuffer.getInputStream(),
                        lebenslaufBuffer.getFileName(),
                        lebenslaufBuffer.getInputStream()
                );

                Notification.show("Bewerbung wurde erfolgreich gespeichert.");
                close();

            } catch (BewerbungBereitsVorhandenException ex) {
                Notification.show(ex.getMessage());

            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage());

            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("Bewerbung konnte nicht gespeichert werden: " + ex.getMessage());
            }
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(absendenButton, abbrechenButton);

        add(inhalt);
        getFooter().add(buttonLayout);
    }

    private boolean dateiFehlt(String dateiname) {
        return dateiname == null || dateiname.isBlank();
    }
}
