package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDate;

// Wir greifen per Views auf den aktuellen User zu mit Vadiin Session! Und hier übergeben wir diesen User nur - Marius

@Component
public class VerwaltungControl {
    private final BenutzerRepository benutzerRepository;
    private final StudentRepository studentRepository;
    private final GruenderRepository gruenderRepository;

    public VerwaltungControl(BenutzerRepository benutzerRepository, StudentRepository studentRepository,
                             GruenderRepository gruenderRepository) {
        this.benutzerRepository = benutzerRepository;
        this.studentRepository = studentRepository;
        this.gruenderRepository = gruenderRepository;
    }


    private boolean istLeer(String x) {
        return x == null || x.isBlank();
    }

    @Transactional
    public boolean loeschen(BenutzerDTO currentUser){
        if (currentUser == null) {
            return false;
        }

        int userId = currentUser.getUserId();

        if (studentRepository.existsById(userId)) {
            studentRepository.deleteById(userId);
        }

        if (gruenderRepository.existsById(userId)) {
            gruenderRepository.deleteById(userId);
        }

        if (benutzerRepository.existsById(userId)) {
            benutzerRepository.deleteById(userId);
            return true;
        }

        return false;
    }

    @Transactional
    public boolean updateStudent(BenutzerDTO aktuellerBenutzer,
                                 String vorname,
                                 String nachname,
                                 LocalDate geburtstag,
                                 String email,
                                 String passwort,
                                 String telefonnummer,
                                 String universitaet,
                                 Integer semester,
                                 String abschluss,
                                 String skills){

        if (aktuellerBenutzer == null){
            System.out.println("Kein Eingeloggter Benutzer");
            return false;
        }
        Optional <Benutzer> benutzerTMP = benutzerRepository.findById(aktuellerBenutzer.getUserId());
        Optional <Student> studentTMP = studentRepository.findById(aktuellerBenutzer.getUserId());

        if (benutzerTMP.isEmpty() || studentTMP.isEmpty()) {
            System.out.println("Benutzer oder Student wurde nicht gefunden.");
            return false;
        }
        Benutzer benutzer = benutzerTMP.get();
        Student student = studentTMP.get();

        vornameAendern(benutzer, vorname);
        nachnameAendern(benutzer, nachname);
        geburtstagAendern(benutzer, geburtstag);
        emailAendern(benutzer, email);
        passwortAendern(benutzer, passwort);
        telefonnummerAendern(benutzer, telefonnummer);

        //Student-Spezifisch
        universitaetAendern(student, universitaet);
        semesterAendern(student, semester);
        abschlussAendern(student, abschluss);
        skillsAendern(student, skills);

        benutzerRepository.save(benutzer);
        studentRepository.save(student);
        return true;
    }

    @Transactional
    public boolean updateGruender(BenutzerDTO aktuellerBenutzer,
                                  String vorname,
                                  String nachname,
                                  LocalDate geburtstag,
                                  String email,
                                  String passwort,
                                  String telefonnummer,
                                  String unternehmensname,
                                  String standort

                                  ){

        if (aktuellerBenutzer == null){
            System.out.println("Kein Eingeloggter Benutzer");
            return false;
        }
        Optional <Benutzer> benutzerTMP = benutzerRepository.findById(aktuellerBenutzer.getUserId());
        Optional <Gruender> gruenderTMP = gruenderRepository.findById(aktuellerBenutzer.getUserId());

        if (benutzerTMP.isEmpty() || gruenderTMP.isEmpty()) {
            System.out.println("Benutzer oder Gründer nicht gefunden.");
            return false;
        }
        Benutzer benutzer = benutzerTMP.get();
        Gruender gruender = gruenderTMP.get();

        vornameAendern(benutzer, vorname);
        nachnameAendern(benutzer, nachname);
        geburtstagAendern(benutzer, geburtstag);
        emailAendern(benutzer, email);
        passwortAendern(benutzer, passwort);
        telefonnummerAendern(benutzer, telefonnummer);

        //Gründer-Spezifisch
        unternehmensNameAendern(gruender, unternehmensname);
        standortAendern(gruender, standort);

        benutzerRepository.save(benutzer);
        gruenderRepository.save(gruender);
        return true;
    }

    public boolean vornameAendern(Benutzer benutzer, String x){
        if(istLeer(x))return true;
        benutzer.setVorname(x);
        System.out.println("Der Vorname "+x+" wurde eingegeben.");
        return true;
    }

    public boolean nachnameAendern(Benutzer benutzer,String x){
        if(istLeer(x))return true;
        benutzer.setNachname(x);
        System.out.println("Der Nachname "+x+" wurde eingegeben.");
        return true;
    }

    public boolean emailAendern(Benutzer benutzer, String x){
        if(istLeer(x))return true;
        benutzer.setEmail(x);
        System.out.println("Die E-Mail "+x+" wurde eingegeben.");
        return true;
    }

    public boolean passwortAendern(Benutzer benutzer, String x){
        if(istLeer(x))return true;
        //hier muss eigentlich noch ne E-Mail gesendet werden als confirmation.
        benutzer.setPasswort(x);
        System.out.println("Das neue Passwort wurde eingegeben.");
        return true;
    }

    public boolean telefonnummerAendern(Benutzer benutzer, String x){
        if(istLeer(x))return true;
        benutzer.setTelefonnummer(x);
        System.out.println("Die Telefonnummer "+x+" wurde eingegeben.");
        return true;
    }

    public boolean universitaetAendern(Student student, String x){
        if(istLeer(x))return true;
        student.setUniversitaet(x);
        System.out.println("Die Universität "+x+" wurde eingegeben.");
        return true;
    }

    public boolean semesterAendern(Student student, Integer x){
        if(x == null)return true;
        student.setSemester(x);
        System.out.println("Das Semester "+x+" wurde eingegeben.");
        return true;
    }
    public boolean unternehmensNameAendern(Gruender gruender, String x){
        if(istLeer(x)) return true;
        gruender.setUnternehmensname(x);
        System.out.println("Das Unternehmen " + x + " wurde eingegeben.");
        return true;
    }

    public boolean standortAendern(Gruender gruender, String x){
        if(istLeer(x)) return true;
        gruender.setStandort(x);
        System.out.println("Der Standort " + x + " wurde eingegeben.");
        return true;
    }
    public boolean geburtstagAendern(Benutzer benutzer, LocalDate x){
        if(x == null) return true;
        benutzer.setGeburtstag(x);
        System.out.println("Das Geburtsdatum " + x + " wurde eingegeben.");
        return true;
    }

    public boolean abschlussAendern(Student student, String x){
        if(istLeer(x)) return true;
        student.setAbschluss(x);
        System.out.println("Der Abschluss " + x + " wurde eingegeben.");
        return true;
    }

    public boolean skillsAendern(Student student, String x){
        if(istLeer(x)) return true;
        student.setSkills(x);
        System.out.println("Die Skills " + x + " wurden eingegeben.");
        return true;
    }
}
