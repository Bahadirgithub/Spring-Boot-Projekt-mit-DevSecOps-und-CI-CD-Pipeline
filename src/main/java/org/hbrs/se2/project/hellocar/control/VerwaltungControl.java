package org.hbrs.se2.project.hellocar.control;

import lombok.NonNull;
import org.hbrs.se2.project.hellocar.control.exception.DatabaseUserException;
import org.hbrs.se2.project.hellocar.control.exception.InvalidBirthdayException;
import org.hbrs.se2.project.hellocar.control.exception.InvalidEmailException;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Skill;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.SkillRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDate;
import java.util.Set;
import java.util.HashSet;

// TODO angucken
import java.util.List;
// Wir greifen per Views auf den aktuellen User zu mit Vaadin Session! Und hier übergeben wir diesen User nur - Marius

@Component
public class VerwaltungControl {
    private final BenutzerRepository benutzerRepository;
    private final StudentRepository studentRepository;
    private final GruenderRepository gruenderRepository;
    private final SkillRepository skillRepository;

    public VerwaltungControl(BenutzerRepository benutzerRepository,
                             StudentRepository studentRepository,
                             GruenderRepository gruenderRepository,
                             SkillRepository skillRepository) {
        this.benutzerRepository = benutzerRepository;
        this.studentRepository = studentRepository;
        this.gruenderRepository = gruenderRepository;
        this.skillRepository = skillRepository;
    }

    public List<String> ladeAlleSkillNamen() {
        return skillRepository.findAllByOrderByKategorieAscNameAsc()
                .stream()
                .map(Skill::getName)
                .toList();
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
        }
        return false;
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
    @Transactional
    public Student ladeStudent(int userId) {
        Optional<Student> studentTMP = studentRepository.findById(userId);
        studentTMP.ifPresent(s -> s.getSkillEntities().size()); // erzwingt das Laden von skillEntities innerhalb der Transaktion
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
    public void updateStudent(@NonNull BenutzerDTO aktuellerBenutzer,
                                 String vorname,
                                 String nachname,
                                 LocalDate geburtstag,
                                 String email,
                                 String telefonnummer,
                                 String universitaet,
                                 Integer semester,
                                 String abschluss,
                                 List<String> skillNamen) throws NullPointerException, DatabaseUserException, InvalidBirthdayException,InvalidEmailException {


        Benutzer benutzer = benutzerRepository.findById(aktuellerBenutzer.getUserId())
                .orElseThrow(DatabaseUserException::new);

        Student student = studentRepository.findById(aktuellerBenutzer.getUserId())
                .orElseThrow(DatabaseUserException::new);

        vornameAendern(benutzer, vorname);
        nachnameAendern(benutzer, nachname);

        if(!ValidateUserControl.isAdult(geburtstag)){
            throw new InvalidBirthdayException("Sie müssen min. 18 Jahre alt sein.");
        }

        geburtstagAendern(benutzer, geburtstag);
        emailAendern(benutzer, email);
        telefonnummerAendern(benutzer, telefonnummer);

        //Student-Spezifisch
        universitaetAendern(student, universitaet);
        semesterAendern(student, semester);
        abschlussAendern(student, abschluss);
        skillsAendern(student, skillNamen);

        benutzerRepository.save(benutzer);
        studentRepository.save(student);

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

                                  ) throws InvalidBirthdayException,InvalidEmailException {

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

        if(!ValidateUserControl.isAdult(geburtstag)){
            throw new InvalidBirthdayException("Sie müssen min. 18 Jahre sein.");
        }
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
        if(!ValidateUserControl.validateEmail(x)){
            return false; // Validierung fehlgeschlagen
        }
        benutzer.setEmail(x);
        System.out.println("Die E-Mail "+x+" wurde eingegeben.");
        return true;
    }

    public boolean passwortAendern(Benutzer benutzer, String x){
        if(istLeer(x))return true;
        //hier muss eigentlich noch ne E-Mail gesendet werden als confirmation.
        if(!ValidateUserControl.validatePassword(x)){return false;} // passwort muss länger als 8 Zeichen sein
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
    public boolean geburtstagAendern(Benutzer benutzer, LocalDate x) throws InvalidBirthdayException {
        if(x == null) return true;
        if(!ValidateUserControl.isAdult(x)){
            throw new InvalidBirthdayException("Sie müssen min. 18 Jahre alt sein.");
        }
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

    public boolean skillsAendern(Student student, List<String> skillNamen){
        if(skillNamen == null){
            return true;
        }
        Set<Skill> aufgeloesteSkills = new HashSet<>(skillRepository.findByNameIn(skillNamen));
        student.setSkillEntities(aufgeloesteSkills);
        System.out.println("Die Skills " + skillNamen + " wurden eingegeben.");
        return true;
    }
}
