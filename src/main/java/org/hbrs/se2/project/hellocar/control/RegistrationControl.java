package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.*;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.util.CryptoProvider;
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

    public void registriereStudent(String vorname, String nachname, LocalDate geburtstag,
                                                 String username, String email, String passwort,
                                                 String universitaet, int semester) throws Exception {

        if(!ValidateUserControl.validFieldNotEmpty(universitaet)){
            throw new UniEmptyException("Bitte geben Sie Ihre Universität an");
        }

        if(semester <= 0){
            throw new IllegalArgumentException("Bitte geben Sie ein gültiges Semester ein");
        }

        Benutzer benutzer = createUser(vorname,nachname,geburtstag,username,email,passwort);

        Student student = new Student();
        student.setUserId(benutzer.getUserId());
        student.setStudentId("s" + benutzer.getUserId());
        student.setUniversitaet(universitaet);
        student.setSemester(semester);
        studentRepository.save(student);
    }

    public void registriereGruender(String vorname, String nachname, LocalDate geburtstag, String username,
                                                  String email, String passwort, String unternehmensname) throws Exception {

        if(!ValidateUserControl.validFieldNotEmpty(unternehmensname)) {
            throw new StartUpEmptyException("Geben Sie den Unternehmensnamen an.");
        }

        Benutzer benutzer = createUser(vorname,nachname,geburtstag,username,email,passwort);

        Gruender gruender = new Gruender();
        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname(unternehmensname);
        gruenderRepository.save(gruender);

    }

    private Benutzer createUser(String vorname, String nachname,LocalDate geburtstag, String username,
                                      String email, String passwort) throws Exception {

        validateBenutzer(vorname,  nachname, geburtstag,username, email,passwort);
        Benutzer benutzer = new Benutzer();
        benutzer.setVorname(vorname);
        benutzer.setNachname(nachname);
        benutzer.setGeburtstag(geburtstag);
        benutzer.setUsername(username);
        benutzer.setEmail(email);
        benutzer.setPasswort(CryptoProvider.CreatePassword(passwort));
        benutzerRepository.saveAndFlush(benutzer);

        return benutzer;
    }

    private void validateBenutzer(String vorname, String nachname, LocalDate geburtstag, String username,
                                  String email, String passwort) throws Exception {

        if(!ValidateUserControl.validFieldNotEmpty(vorname)) {
            throw new FirstNameEmptyException("Bitte geben Sie Ihren Vornamen an.");
        }

        if(!ValidateUserControl.validFieldNotEmpty(nachname)) {
            throw new LastNameEmptyException("Bitte geben Sie Ihren Nachnamen an.");
        }

        if(!ValidateUserControl.validateUserName(username,this.benutzerRepository)) {
            throw new UsernameAlreadyExistsException("Username existiert bereits!");
        }

        if(!ValidateUserControl.validateUserNameNotEmpty(username)) {
            throw new UsernameEmptyException("Der Nutzername darf nicht leer sein.");
        }

        if(!ValidateUserControl.validatePassword(passwort)) {
           throw new PasswordInvalidException("Passwort muss min 8 Zeichen sein, inkl. Groß-/Kleinbuchstaben, Zahl und Sonderzeichen.");
        }
        if(!ValidateUserControl.validateEmail(email)) {
           throw new InvalidEmailException("Email ist ungültig.");
        }

        if(!ValidateUserControl.isAdult(geburtstag)) {
            throw new InvalidBirthdayException("Ungültiges Geburtsdatum.");
        }
    }
}