package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.InvalidEmailException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.control.ValidateUserControl;

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
    public void loeschen(BenutzerDTO currentUser){
        if (currentUser == null) {
            return;
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
        }
    }


    /**
     * Lädt den aktuellen Benutzer frisch aus der DB und gibt ihn als DTO zurück.
     * Wird nach einem Update aufgerufen um die Session zu aktualisieren.
     */
    public BenutzerDTO ladeBenutzer(int userId) {
        Optional<Benutzer> benutzerTMP = benutzerRepository.findById(userId);
        if (benutzerTMP.isEmpty()) return null;

        Benutzer benutzer = benutzerTMP.get();
        BenutzerDTOImpl dto = new BenutzerDTOImpl();
        dto.setUserId(benutzer.getUserId());
        dto.setVorname(benutzer.getVorname());
        dto.setNachname(benutzer.getNachname());
        dto.setGeburtstag(benutzer.getGeburtstag());
        dto.setUsername(benutzer.getUsername());
        dto.setEmail(benutzer.getEmail());
        dto.setTelefonnummer(benutzer.getTelefonnummer());
        return dto;
    }

    /**
     * Lädt den Student neu aus der DB
     */
    public Student ladeStudent(int userId) {
        Optional<Student> studentTMP = studentRepository.findById(userId);
        return studentTMP.orElse(null);
    }

    /**
     * Lädt den Gründer neu aus der DB
     */
    public Gruender ladeGruender(int userId) {
        Optional<Gruender> gruenderTMP = gruenderRepository.findById(userId);
        return gruenderTMP.orElse(null);
    }



    @Transactional
    public boolean updateStudent(BenutzerDTO aktuellerBenutzer,
                                 String vorname,
                                 String nachname,
                                 LocalDate geburtstag,
                                 String email,
                                 String telefonnummer,
                                 String universitaet,
                                 Integer semester,
                                 String abschluss,
                                 String skills) throws InvalidEmailException {

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
                                  String telefonnummer,
                                  String unternehmensname,
                                  String standort

                                  ) throws InvalidEmailException {

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
        telefonnummerAendern(benutzer, telefonnummer);

        //Gründer-Spezifisch
        unternehmensNameAendern(gruender, unternehmensname);
        standortAendern(gruender, standort);

        benutzerRepository.save(benutzer);
        gruenderRepository.save(gruender);
        return true;
    }

    public void vornameAendern(Benutzer benutzer, String x){
        if(istLeer(x))return;
        benutzer.setVorname(x);
        System.out.println("Der Vorname "+x+" wurde eingegeben.");
    }

    public void nachnameAendern(Benutzer benutzer,String x){
        if(istLeer(x))return;
        benutzer.setNachname(x);
        System.out.println("Der Nachname "+x+" wurde eingegeben.");
    }

    public void emailAendern(Benutzer benutzer, String x) throws InvalidEmailException {
        if(istLeer(x))return;
        if(!ValidateUserControl.validateEmail(x)){
            throw new InvalidEmailException("Das ist keine validierte Email"); // Validierung fehlgeschlagen
        }
        benutzer.setEmail(x);
        System.out.println("Die E-Mail "+x+" wurde eingegeben.");
    }

    public void passwortAendern(Benutzer benutzer, String x){
        if(istLeer(x))return;
        //hier muss eigentlich noch ne E-Mail gesendet werden als confirmation.
        if(x.length() < 8){return;} // passwort muss länger als 8 Zeichen sein
        benutzer.setPasswort(x);
        System.out.println("Das neue Passwort wurde eingegeben.");
    }

    public void telefonnummerAendern(Benutzer benutzer, String x){
        if(istLeer(x))return;
        benutzer.setTelefonnummer(x);
        System.out.println("Die Telefonnummer "+x+" wurde eingegeben.");
    }

    public void universitaetAendern(Student student, String x){
        if(istLeer(x))return;
        student.setUniversitaet(x);
        System.out.println("Die Universität "+x+" wurde eingegeben.");
    }

    public void semesterAendern(Student student, Integer x){
        if(x == null)return;
        student.setSemester(x);
        System.out.println("Das Semester "+x+" wurde eingegeben.");
    }
    public void unternehmensNameAendern(Gruender gruender, String x){
        if(istLeer(x)) return;
        gruender.setUnternehmensname(x);
        System.out.println("Das Unternehmen " + x + " wurde eingegeben.");
    }

    public void standortAendern(Gruender gruender, String x){
        if(istLeer(x)) return;
        gruender.setStandort(x);
        System.out.println("Der Standort " + x + " wurde eingegeben.");
    }
    public void geburtstagAendern(Benutzer benutzer, LocalDate x){
        if(x == null) return;
        LocalDate vor18Jahren = LocalDate.now().minusYears(18);
        if(x.isBefore(vor18Jahren) ){return;}
        benutzer.setGeburtstag(x);
        System.out.println("Das Geburtsdatum " + x + " wurde eingegeben.");
    }

    public void abschlussAendern(Student student, String x){
        if(istLeer(x)) return;
        student.setAbschluss(x);
        System.out.println("Der Abschluss " + x + " wurde eingegeben.");
    }

    public void skillsAendern(Student student, String x){
        if(istLeer(x)) return;
        student.setSkills(x);
        System.out.println("Die Skills " + x + " wurden eingegeben.");
    }
}
