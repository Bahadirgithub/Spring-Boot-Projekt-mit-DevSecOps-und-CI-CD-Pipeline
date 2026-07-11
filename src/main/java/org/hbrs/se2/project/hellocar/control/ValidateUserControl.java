package org.hbrs.se2.project.hellocar.control;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.PasswordField;
import lombok.NonNull;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.regex.Pattern;
import com.vaadin.flow.component.datepicker.DatePicker;


@Component
public class ValidateUserControl {

    // Klasse zur Validierung bei Registrierung und Änderung des Profils
    // Einheitliche Anforderungen an Passwortlänge, Geburtsdatum
    // Duplizierter Code soll damit verhindert werden
    // funktionale Klasse statt ObjektOrientiert
    // min 18
    // Passwort muss min 8 Zeichen, min 1 Großbuchstabe, min 1 Zahl, min 1 Kleinbuchstabe, 1 Zeichen
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+/=.?,;!*_'~])(?=\\S+$).{8,}");
    // E-Mail-Validierung: Einfache, sichere Regex ohne catastrophic backtracking.
    // Pattern: localpart@domain mit mindestens einem Punkt in der Domain.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9.\\-_]{1,64}@[A-Za-z0-9][A-Za-z0-9.]*[A-Za-z0-9]\\.[A-Za-z]{2,}$"
    );

    // Tests geben true zurück, wenn Daten valide sind , ansonsten false.
    public static boolean validateUserName(String username, BenutzerRepository benutzerRepository) {
        return !benutzerRepository.existsByUsername(username);
    }


    // prüfe ob der nutzername nicht leer ist -> falls leer -> false falls nicht leer -> true
    public static boolean validateUserNameNotEmpty(String username) {
        return !username.isBlank();
    }

    public static boolean validateEmail(@NonNull String email) throws NullPointerException {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean validatePassword(String passwort) {
        // null-Check ist wichtig, um NullPointerException zu vermeiden
        if (passwort == null || passwort.isBlank()) {
            return false;
        }

        return PASSWORD_PATTERN.matcher(passwort).matches();
    }

    // Geburtsdatum korrekt setzen mit mindestalter 18 Jahre+
    public static DatePicker validDatePicker(){
        DatePicker b = new DatePicker("Geburtsdatum");
        LocalDate vor18Jahren = LocalDate.now().minusYears(18);
        b.setMax(vor18Jahren);
        return b;
    }

    // Validierung des Alters des Nutzers
    public static boolean isAdult(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now().minusYears(18));
    }

    // Feld darf nicht leer sein
    public static boolean validFieldNotEmpty(String input){
        return !input.isBlank();
    }

    // Prüft, ob Passwort und Passwort-Wiederholung übereinstimmen
    public static boolean validatePasswordMatch(String passwort, String passwortWiederholung) {
        return passwort != null && passwort.equals(passwortWiederholung);
    }

    // Parst die Semester-Eingabe eines Textfelds. Liefert null bei leerer oder nicht-numerischer Eingabe.
    public static Integer parseSemester(String eingabe) {
        if (eingabe == null || eingabe.isBlank() || !eingabe.matches("\\d+")) {
            return null;
        }
        return Integer.parseInt(eingabe);
    }



}
