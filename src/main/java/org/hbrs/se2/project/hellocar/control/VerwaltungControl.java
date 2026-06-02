package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.springframework.stereotype.Component;

/**
 * Bitte noch nicht mergen!
 * Es werden Änderungen und Verbesserungen hier noch geschehen.
 * :)
 */
@Component
public class VerwaltungControl {
    private final BenutzerRepository benutzerRepository;
    private final StudentRepository studentRepository;
    private final GruenderRepository gruenderRepository;

    private Benutzer benutzer=null;

    public VerwaltungControl(BenutzerRepository benutzerRepository, StudentRepository studentRepository, GruenderRepository gruenderRepository) {
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

    public boolean update(){
        if(isNull(benutzer)){
            System.out.println("Kein Benutzer gegeben");
            return false;
        }
        benutzerRepository.save(benutzer);
        return true;
    }

    public boolean vornameAendern(String x){
        if(!isNull(x))return false;
        benutzer.setVorname(x);
        return true;
    }

    public boolean nachnameAendern(String x){
        if(!isNull(x))return false;
        benutzer.setNachname(x);
        return true;
    }

    public boolean emailAendern(String x){
        if(!isNull(x))return false;
        benutzer.setEmail(x);
        return true;
    }

    public boolean passwortAendern(String x){
        if(!isNull(x))return false;
        //hier muss eigentlich noch ne E-Mail gesendet werden als confirmation.
        benutzer.setPasswort(x);
        return true;
    }

    public boolean telefonnummerAendern(String x){
        if(!isNull(x))return false;
        benutzer.setTelefonnummer(x);
        return true;
    }
}
