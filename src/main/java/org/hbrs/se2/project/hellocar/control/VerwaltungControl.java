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
        if(a == null){
            return true;
        }
        return false;
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

    private void vornameAendern(String x){
        if(!isNull(x))return;
        benutzer.setVorname(x);
    }

    private void nachnameAendern(String x){
        if(!isNull(x))return;
        benutzer.setNachname(x);
    }

    private void emailAendern(String x){
        if(!isNull(x))return;
        benutzer.setEmail(x);
    }

    private void passwortAendern(String x){
        if(!isNull(x))return;
        //hier muss eigentlich noch ne email gesendet werden als confirmation.
        benutzer.setPasswort(x);
    }

    private void telefonnummerAendern(String x){
        if(!isNull(x))return;
        benutzer.setTelefonnummer(x);
    }
}
