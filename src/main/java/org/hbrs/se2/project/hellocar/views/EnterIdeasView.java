package org.hbrs.se2.project.hellocar.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.hbrs.se2.project.hellocar.control.ManageIdeaControl;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.GruendungsideeDTOImpl;
import org.hbrs.se2.project.hellocar.util.Globals;

@Route(value = Globals.Pages.ENTER_IDEAS, layout = AppView.class)
@PageTitle("Gründungsidee hinzufügen")
@CssImport("./styles/views/enteridea/enter-ideas-view.css")
public class EnterIdeasView extends Div { // 3. Form (Spezialisierung / Vererbung)

  private TextField unternehmensname = new TextField("Unternehmensname");
  private TextField position = new TextField("Offene Position / Rolle");

  private TextArea beschreibung = new TextArea("Beschreibung der Idee");
  private TextArea anforderungen = new TextArea("Anforderungen an Bewerber");
  private TextArea arbeitsbedingungen = new TextArea("Arbeitsbedingungen");

  private Button cancel = new Button("Abbrechen");
  private Button save = new Button("Speichern");

  private Binder<GruendungsideeDTOImpl> binder = new Binder<>(GruendungsideeDTOImpl.class);

  private ManageIdeaControl ideaService;

  public EnterIdeasView(ManageIdeaControl ideaService) {
    this.ideaService = ideaService;
    addClassName("enter-ideas-view");

    // Das Unternehmensfeld soll vom Nutzer nicht verändert werden können
    unternehmensname.setReadOnly(true);

    add(createTitle());
    add(createFormLayout());
    add(createButtonLayout());

    // Automatisches Binding basierend auf den Variablennamen und DTO-Gettern
    binder.bindInstanceFields(this);
    clearForm();

    // Abbrechen leert das Formular wieder
    cancel.addClickListener(event -> clearForm());

    save.addClickListener(
      e -> {
        BenutzerDTO userDTO =
                (BenutzerDTO) UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER);

        if (userDTO != null) {
          // Idee erstellen
          GruendungsideeDTOImpl aktuelleIdee = new GruendungsideeDTOImpl();

          try {
            // DTO mit UI Eingaben befüllen
            binder.writeBean(aktuelleIdee);

            aktuelleIdee.setGruenderUserId(userDTO.getUserId());
            ideaService.createIdea(aktuelleIdee);

            Notification.show("Gründungsidee erfolgreich gespeichert.");
            clearForm(); // Formular für die nächste Eingabe leeren

          } catch (com.vaadin.flow.data.binder.ValidationException validationException) {
            // Falls der Nutzer unvollständige Daten eingegeben hat
            Notification.show("Fehler: Bitte überprüfen Sie Ihre Eingaben.");
          }
        } else {
          Notification.show("Fehler: Kein angemeldeter Benutzer gefunden.");
        }
      });
  }

  private void clearForm() {
    GruendungsideeDTOImpl frischesDto = new GruendungsideeDTOImpl();

    //Eingeloggten Benutzer holen
    BenutzerDTO userDTO = (BenutzerDTO) UI.getCurrent().getSession().getAttribute(Globals.CURRENT_USER);

    if (userDTO != null) {
      // Die ID des aktuellen Benutzers direkt im neuen DTO hinterlegen
      frischesDto.setGruenderUserId(userDTO.getUserId());

      // Den Namen des Gründers/Usernamen als Read-Only Text anzeigen,
      // bis die Idee final gespeichert wird.
      String Unternehmensname = ideaService.getUnternehmensnameByUserId(userDTO.getUserId());
      frischesDto.setUnternehmensname(Unternehmensname);
    }

    // Dem Binder das frisch vorbereitete DTO übergeben
    binder.setBean(frischesDto);
  }

  private Component createTitle() {
    return new H3("Neue Gründungsidee anlegen");
  }

  private Component createFormLayout() {
    FormLayout formLayout = new FormLayout();
    formLayout.add(unternehmensname, beschreibung, position, anforderungen, arbeitsbedingungen);
    return formLayout;
  }

  private Component createButtonLayout() {
    HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.addClassName("button-layout");
    save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    buttonLayout.add(save);
    buttonLayout.add(cancel);
    return buttonLayout;
  }
}
