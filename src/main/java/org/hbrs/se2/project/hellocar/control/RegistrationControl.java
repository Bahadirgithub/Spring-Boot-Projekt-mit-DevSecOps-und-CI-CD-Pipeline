package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.results.RegistrationResult;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

/**
 * Control-Klasse für die Registrierung neuer Benutzer.
 * Verwaltet die Anlage von Benutzer + Student bzw. Benutzer + Gründer.
 */
@Component
public class RegistrationControl {

    private final BenutzerRepository benutzerRepository;
    private final StudentRepository studentRepository;
    private final GruenderRepository gruenderRepository;

    // Constructor Injection – Spring injiziert die Repositories automatisch
    public RegistrationControl(BenutzerRepository benutzerRepository,
                               StudentRepository studentRepository,
                               GruenderRepository gruenderRepository) {
        this.benutzerRepository = benutzerRepository;
        this.studentRepository = studentRepository;
        this.gruenderRepository = gruenderRepository;
    }

    /**
     * Registriert einen neuen Benutzer als Student.
     * Führt eine Reihe von Validierungen durch und gibt ein spezifisches Ergebnis zurück.
     *
     * @return Ein RegistrationResult-Enum, das den Erfolg oder den genauen Grund des Fehlschlags angibt.
     */
    public RegistrationResult registriereStudent(String vorname, String nachname, LocalDate geburtstag,
                                                 String username, String email, String passwort,
                                                 String universitaet, int semester) {

        // 1. Zentrale Validierung und Benutzer-Erstellung aufrufen
        Object validationResult = validateAndCreateUser(vorname, nachname, geburtstag, username, email, passwort);

        // 2. Prüfen, ob ein Fehler aufgetreten ist (Rückgabe ist ein RegistrationResult Enum)
        if (validationResult instanceof RegistrationResult) {
            return (RegistrationResult) validationResult;
        }

        // 3. Wenn erfolgreich, den zurückgegebenen Benutzer nehmen und den Studententeil erledigen
        Benutzer benutzer = (Benutzer) validationResult;
        
        Student student = new Student();
        student.setUserId(benutzer.getUserId());
        student.setStudentId("s" + benutzer.getUserId());
        student.setUniversitaet(universitaet);
        student.setSemester(semester);
        studentRepository.save(student);

        return RegistrationResult.SUCCESS;
    }

    /**
     * Registriert einen neuen Benutzer als Gründer.
     * Führt eine Reihe von Validierungen durch und gibt ein spezifisches Ergebnis zurück.
     *
     * @return Ein RegistrationResult-Enum, das den Erfolg oder den genauen Grund des Fehlschlags angibt.
     */
    public RegistrationResult registriereGruender(String vorname, String nachname, LocalDate geburtstag,
                                                  String username, String email, String passwort,
                                                  String unternehmensname) {

        // 1. Zentrale Validierung und Benutzer-Erstellung aufrufen
        Object validationResult = validateAndCreateUser(vorname, nachname, geburtstag, username, email, passwort);

        // 2. Prüfen, ob ein Fehler aufgetreten ist (Rückgabe ist ein RegistrationResult Enum)
        if (validationResult instanceof RegistrationResult) {
            return (RegistrationResult) validationResult;
        }

        // 3. Wenn erfolgreich, den zurückgegebenen Benutzer nehmen und den Gründerteil erledigen
        Benutzer benutzer = (Benutzer) validationResult;

        Gruender gruender = new Gruender();
        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname(unternehmensname);
        gruenderRepository.save(gruender);

        return RegistrationResult.SUCCESS;
    }

    /**
     * Zentrale Hilfsmethode, die den doppelten Code vermeidet.
     * Sie validiert die allgemeinen Benutzerdaten und legt, falls gültig, den Benutzer an.
     *
     * @return Ein RegistrationResult (bei Validierungsfehlern) ODER das neue Benutzer-Objekt (bei Erfolg).
     */
    private Object validateAndCreateUser(String vorname, String nachname, LocalDate geburtstag,
                                         String username, String email, String passwort) {
        
        // Gemeinsame Validierung
        if (username == null || username.isBlank()) {
            return RegistrationResult.USERNAME_IS_EMPTY;
        }
        if (isPasswordInvalid(passwort)) {
            return RegistrationResult.PASSWORD_TOO_SHORT;
        }
        if (isEmailInvalid(email)) {
            return RegistrationResult.INVALID_EMAIL_FORMAT;
        }
        if (benutzerRepository.existsByUsername(username)) {
            return RegistrationResult.USERNAME_ALREADY_EXISTS;
        }

        // Gemeinsame Erstellung des Basis-Benutzers
        Benutzer benutzer = erstelleBenutzer(vorname, nachname, geburtstag, username, email, passwort);
        benutzerRepository.saveAndFlush(benutzer);

        return benutzer;
    }

    /**
     * Private Hilfsmethode zur Passwort-Validierung.
     * @return true, wenn das Passwort ungültig ist (null, leer oder zu kurz).
     */
    private boolean isPasswordInvalid(String passwort) {
        return passwort == null || passwort.length() < 8;
    }

    /**
     * Private Hilfsmethode zur E-Mail-Validierung.
     * @return true, wenn die E-Mail ein ungültiges Format hat.
     */
    private boolean isEmailInvalid(String email) {
        if (email == null) return true;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return !email.matches(emailRegex);
    }

    /**
     * Erstellt ein neues Benutzer-Objekt (ohne es zu speichern).
     */
    private Benutzer erstelleBenutzer(String vorname, String nachname,
                                      LocalDate geburtstag, String username,
                                      String email, String passwort) {
        Benutzer benutzer = new Benutzer();
        benutzer.setVorname(vorname);
        benutzer.setNachname(nachname);
        benutzer.setGeburtstag(geburtstag);
        benutzer.setUsername(username);
        benutzer.setEmail(email);
        benutzer.setPasswort(passwort);
        return benutzer;
    }
}