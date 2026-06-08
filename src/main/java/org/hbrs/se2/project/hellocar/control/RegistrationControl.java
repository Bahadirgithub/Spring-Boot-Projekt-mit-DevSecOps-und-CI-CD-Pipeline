package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
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
                               GruenderRepository gruenderRepository)
    {
        this.benutzerRepository = benutzerRepository;
        this.studentRepository = studentRepository;
        this.gruenderRepository = gruenderRepository;

    }


    /**
     * Registriert einen neuen Benutzer als Student.
     * Speichert zuerst den Benutzer, dann den Student mit der generierten userId.
     * Gibt true zurück wenn erfolgreich, false wenn Username bereits vergeben.
     */
    public boolean registriereStudent(String vorname, String nachname, LocalDate geburtstag,
                                      String username, String email, String passwort,
                                      String universitaet, int semester) {

        // Prüfen ob Username bereits existiert
        if (benutzerRepository.existsByUsername(username)) {
            return false;
        }

        if(username.isBlank()){return false;}

        if(pwd(passwort)){
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            return false; // Validierung fehlgeschlagen
        }


        // Benutzer anlegen über Hilfsmethode danach speichern
        Benutzer benutzer = erstelleBenutzer(vorname, nachname, geburtstag, username, email, passwort);
        benutzerRepository.saveAndFlush(benutzer);

        // Schritt 2: Student anlegen mit der generierten userId
        Student student = new Student();
        student.setUserId(benutzer.getUserId());
        student.setStudentId("s" + benutzer.getUserId()); // Muster aus den Tests
        student.setUniversitaet(universitaet);
        student.setSemester(semester);
        // ToDo: Skills und Abschluss werden später im Profil ergänzt
        studentRepository.save(student);

        return true;
    }

    public boolean pwd(String passwort){

        if(passwort.length()< 8){return true;}

        return false;
    }


    /**
     * Registriert einen neuen Benutzer als Gründer.
     * Speichert zuerst den Benutzer, dann den Gründer mit der generierten userId.
     * Gibt true zurück wenn erfolgreich, false wenn Username bereits vergeben.
     */
    public boolean registriereGruender(String vorname, String nachname, LocalDate geburtstag,
                                       String username, String email, String passwort,
                                       String unternehmensname) {

        // Prüfen ob Username bereits existiert
        if (benutzerRepository.existsByUsername(username)) {
            return false;
        }

        if(username.isBlank()){return false;}

        if(pwd(passwort)){
            return false;
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            return false; // Validierung fehlgeschlagen
        }

        // Benutzer anlegen über Hilfsmethode danach speichern
        Benutzer benutzer = erstelleBenutzer(vorname, nachname, geburtstag, username, email, passwort);
        benutzerRepository.saveAndFlush(benutzer);

        // Schritt 2: Gründer anlegen mit der generierten userId
        Gruender gruender = new Gruender();
        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname(unternehmensname);
        // ToDo: Standort wird später im Profil ergänzt
        gruenderRepository.save(gruender);

        return true;
    }


    /**
     * Erstellt einen neuen Benutzer ohne ihn zu speichern.
     * Das Speichern übernimmt die aufrufende Methode.
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
        // ToDo: Passwort mit BCrypt verschlüsseln bevor es gespeichert wird
        return benutzer;
    }


}


