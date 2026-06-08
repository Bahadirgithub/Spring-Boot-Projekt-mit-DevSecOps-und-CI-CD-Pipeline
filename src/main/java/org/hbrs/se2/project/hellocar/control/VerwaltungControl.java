package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VerwaltungControl {
    private final BenutzerRepository benutzerRepository;
    private final StudentRepository studentRepository;
    private final GruenderRepository gruenderRepository;

    private Benutzer benutzer=null;
    private Optional<Student> student= Optional.empty();
    private Optional<Gruender> gruender= Optional.empty();

    public VerwaltungControl(BenutzerRepository benutzerRepository, StudentRepository studentRepository,
                             GruenderRepository gruenderRepository) {
        this.benutzerRepository = benutzerRepository;
        this.studentRepository = studentRepository;
        this.gruenderRepository = gruenderRepository;
    }

    private <T> boolean isNull(T a){
        return a == null;
    }

    public void loeschen(BenutzerDTO x){
        if(isNull(x))return;
        benutzerRepository.deleteById(x.getUserId());
    }

    private boolean updateBenutzer(String a, String b, String c, String d, String e){
        vornameAendern(a);
        nachnameAendern(b);
        emailAendern(c);
        passwortAendern(d);
        telefonnummerAendern(e);
        benutzerRepository.save(benutzer);
        return true;
    }

    public boolean updateGruender(String a, String b, String c, String d, String e, String f){
        if(isNull(benutzer)){
            System.out.println("Kein Benutzer gegeben");
            return false;
        }
        updateBenutzer(a,b,c,d,e);
        unternehmensNameAendern(f);
        return true;
    }

    public boolean updateStudent(String a, String b, String c, String d, String e, String f, int g){
        if(isNull(benutzer)){
            System.out.println("Kein Benutzer gegeben");
            return false;
        }
        updateBenutzer(a,b,c,d,e);
        universitaetAendern(f);
        semesterAendern(g);
        return true;
    }

    public boolean vornameAendern(String x){
        if(!isNull(x))return true;
        benutzer.setVorname(x);
        System.out.println("Der Vorname "+x+" wurde eingegeben.");
        return true;
    }

    public boolean nachnameAendern(String x){
        if(!isNull(x))return true;
        benutzer.setNachname(x);
        System.out.println("Der Nachname "+x+" wurde eingegeben.");
        return true;
    }

    public boolean emailAendern(String x){
        if(!isNull(x))return true;
        benutzer.setEmail(x);
        System.out.println("Die E-Mail "+x+" wurde eingegeben.");
        return true;
    }

    public boolean passwortAendern(String x){
        if(!isNull(x))return true;
        //hier muss eigentlich noch ne E-Mail gesendet werden als confirmation.
        benutzer.setPasswort(x);
        System.out.println("Das neue Passwort wurde eingegeben.");
        return true;
    }

    public boolean telefonnummerAendern(String x){
        if(!isNull(x))return true;
        benutzer.setTelefonnummer(x);
        System.out.println("Die Telefonnummer "+x+" wurde eingegeben.");
        return true;
    }

    public boolean universitaetAendern(String x){
        if(!isNull(x))return true;
        student=studentRepository.findById(benutzer.getUserId());
        student.get().setUniversitaet(x);
        System.out.println("Die Universität "+x+" wurde eingegeben.");
        return true;
    }

    public boolean semesterAendern(int x){
        if(!isNull(x))return true;
        student=studentRepository.findById(benutzer.getUserId());
        student.get().setSemester(x);
        System.out.println("Das Semester "+x+" wurde eingegeben.");
        return true;
    }

    public boolean unternehmensNameAendern(String x){
        if(!isNull(x))return true;
        gruender=gruenderRepository.findById(benutzer.getUserId());
        gruender.get().setUnternehmensname(x);
        System.out.println("Das Unternehmen "+x+" wurde eingegeben.");
        return true;
    }
}
