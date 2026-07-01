package org.hbrs.se2.project.hellocar.control;

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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
     * Lädt den Student neu aus der DB.
     * Die Skill-Collection wird innerhalb der Transaktion initialisiert,
     * damit die View danach student.getSkills() verwenden kann.
     */
    @Transactional
    public Student ladeStudent(int userId) {
        Optional<Student> studentTMP = studentRepository.findById(userId);
        studentTMP.ifPresent(s -> s.getSkillEntities().size());
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
    public void updateStudent(BenutzerDTO aktuellerBenutzer,
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
            throw new IllegalArgumentException("Kein Benutzer angegeben");
        }

        Optional <Benutzer> benutzerTMP = benutzerRepository.findById(aktuellerBenutzer.getUserId());
        Optional <Student> studentTMP = studentRepository.findById(aktuellerBenutzer.getUserId());

        if (benutzerTMP.isEmpty() || studentTMP.isEmpty()) {
            throw new IllegalArgumentException("Benutzer oder Student nicht gefunden.");
        }

        Benutzer benutzer = benutzerTMP.get();
        Student student = studentTMP.get();

        if (!vornameAendern(benutzer, vorname)) return false;
        if (!nachnameAendern(benutzer, nachname)) return false;
        if (!geburtstagAendern(benutzer, geburtstag)) return false;
        if (!emailAendern(benutzer, email)) return false;
        if (!telefonnummerAendern(benutzer, telefonnummer)) return false;

        // Student-spezifisch
        if (!universitaetAendern(student, universitaet)) return false;
        if (!semesterAendern(student, semester)) return false;
        if (!abschlussAendern(student, abschluss)) return false;
        if (!skillsAendern(student, skills)) return false;

        benutzerRepository.save(benutzer);
        studentRepository.save(student);
    }

    @Transactional
    public void updateGruender(BenutzerDTO aktuellerBenutzer,
                                  String vorname,
                                  String nachname,
                                  LocalDate geburtstag,
                                  String email,
                                  String telefonnummer,
                                  String unternehmensname,
                                  String standort

                                  ) throws InvalidEmailException {

        if (aktuellerBenutzer == null){
            throw new IllegalArgumentException("Kein Benutzer angegeben");
        }

        Optional <Benutzer> benutzerTMP = benutzerRepository.findById(aktuellerBenutzer.getUserId());
        Optional <Gruender> gruenderTMP = gruenderRepository.findById(aktuellerBenutzer.getUserId());

        if (benutzerTMP.isEmpty() || gruenderTMP.isEmpty()) {
            throw new IllegalArgumentException("Benutzer oder Student nicht gefunden.");
        }

        Benutzer benutzer = benutzerTMP.get();
        Gruender gruender = gruenderTMP.get();

        if (!vornameAendern(benutzer, vorname)) return false;
        if (!nachnameAendern(benutzer, nachname)) return false;
        if (!geburtstagAendern(benutzer, geburtstag)) return false;
        if (!emailAendern(benutzer, email)) return false;
        if (!telefonnummerAendern(benutzer, telefonnummer)) return false;

        // Gründer-spezifisch
        if (!unternehmensNameAendern(gruender, unternehmensname)) return false;
        if (!standortAendern(gruender, standort)) return false;

        benutzerRepository.save(benutzer);
        gruenderRepository.save(gruender);
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

    public boolean skillsAendern(Student student, List<String> x) {
        if (x == null) {
            return true;
        }

        List<String> skillNamen = x.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (skillNamen.isEmpty()) {
            student.setSkillEntities(new HashSet<>());
            System.out.println("Alle Skills wurden entfernt.");
            return true;
        }

        List<Skill> skillsAusDb = skillRepository.findByNameIn(skillNamen);

        if (skillsAusDb.size() != skillNamen.size()) {
            System.out.println("Mindestens ein Skill wurde nicht in der Datenbank gefunden: " + skillNamen);
            return false;
        }

        student.setSkillEntities(new HashSet<>(skillsAusDb));
        System.out.println("Die Skills " + skillNamen + " wurden eingegeben.");
        return true;
    }
}
