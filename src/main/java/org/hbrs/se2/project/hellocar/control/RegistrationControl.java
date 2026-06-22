package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.EMPTY_FIELD;
import org.hbrs.se2.project.hellocar.control.exception.INVALID_EMAIL_FORMAT;
import org.hbrs.se2.project.hellocar.control.exception.PASSWORD_INVALID;
import org.hbrs.se2.project.hellocar.control.exception.USERNAME_ALREADY_EXISTS;
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

    public void registriereStudent(String vorname, String nachname, LocalDate geburtstag,
                                                 String username, String email, String passwort,
                                                 String universitaet, int semester) {

        Benutzer benutzer = createUser(vorname,nachname,geburtstag,username,email,passwort);

        if(!ValidateUserControl.validFieldNotEmpty(universitaet)){
            throw new EMPTY_FIELD("Geben Sie die Universität ein an.");
        }

        if(semester <= 0){
            throw new IllegalArgumentException("Bitte geben Sie ein gültiges Semester ein");
        }

        Student student = new Student();
        student.setUserId(benutzer.getUserId());
        student.setStudentId("s" + benutzer.getUserId());
        student.setUniversitaet(universitaet);
        student.setSemester(semester);
        studentRepository.save(student);

    }



    public void registriereGruender(String vorname, String nachname, LocalDate geburtstag, String username,
                                                  String email, String passwort, String unternehmensname){

        Benutzer benutzer = createUser(vorname,nachname,geburtstag,username,email,passwort);

        if(!ValidateUserControl.validFieldNotEmpty(unternehmensname)){
            throw new EMPTY_FIELD("Geben Sie den Unternehmensnamen an.");
        }

        Gruender gruender = new Gruender();
        gruender.setUserId(benutzer.getUserId());
        gruender.setGruenderId("g" + benutzer.getUserId());
        gruender.setUnternehmensname(unternehmensname);
        gruenderRepository.save(gruender);

    }



    private Benutzer createUser(String vorname, String nachname,LocalDate geburtstag, String username,
                                      String email, String passwort) throws RuntimeException {

        validateBenutzer(vorname,  nachname, geburtstag,username, email,passwort);
        Benutzer benutzer = new Benutzer();
        benutzer.setVorname(vorname);
        benutzer.setNachname(nachname);
        benutzer.setGeburtstag(geburtstag);
        benutzer.setUsername(username);
        benutzer.setEmail(email);
        benutzer.setPasswort(passwort);
        benutzerRepository.saveAndFlush(benutzer);

        return benutzer;

    }

    private boolean validateBenutzer(String vorname, String nachname,LocalDate geburtstag, String username,
                                     String email, String passwort) throws RuntimeException
    {
        if(!ValidateUserControl.validFieldNotEmpty(vorname) || !ValidateUserControl.validFieldNotEmpty(nachname)){
            throw new EMPTY_FIELD("Die Pflichtfelder dürfen nicht leer sein.");
        }

        if(!ValidateUserControl.validateUserName(username,this.benutzerRepository)){
            throw new USERNAME_ALREADY_EXISTS("Username existiert bereits!");
        }
        if(!ValidateUserControl.validatePassword(passwort)){
           throw new PASSWORD_INVALID("Passwort muss min 8 Zeichen sein, inkl. Groß-/Kleinbuchstaben, Zahl und Sonderzeichen.");
        }
        if(!ValidateUserControl.validateEmail(email)){
           throw new INVALID_EMAIL_FORMAT("Email ist ungültig.");
        }

        return true;
    }



}