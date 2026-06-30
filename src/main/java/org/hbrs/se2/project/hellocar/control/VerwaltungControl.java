package org.hbrs.se2.project.hellocar.control;

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
    public boolean updateStudent(BenutzerDTO aktuellerBenutzer,
                                 String vorname,
                                 String nachname,
                                 LocalDate geburtstag,
                                 String email,
                                 String telefonnummer,
                                 String universitaet,
                                 Integer semester,
                                 String abschluss,
                                 List<String> skills){

        if (aktuellerBenutzer == null){
            System.out.println("Kein eingeloggter Benutzer");
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
                                  String standort){

        if (aktuellerBenutzer == null){
            System.out.println("Kein eingeloggter Benutzer");
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
        return true;
    }

    public boolean vornameAendern(Benutzer benutzer, String x){
        if(istLeer(x)) return true;
        benutzer.setVorname(x);
        System.out.println("Der Vorname " + x + " wurde eingegeben.");
        return true;
    }

    public boolean nachnameAendern(Benutzer benutzer,String x){
        if(istLeer(x)) return true;
        benutzer.setNachname(x);
        System.out.println("Der Nachname " + x + " wurde eingegeben.");
        return true;
    }

    public boolean emailAendern(Benutzer benutzer, String x){
        if(istLeer(x)) return true;
        if(!ValidateUserControl.validateEmail(x)){
            return false;
        }
        benutzer.setEmail(x);
        System.out.println("Die E-Mail " + x + " wurde eingegeben.");
        return true;
    }

    public boolean passwortAendern(Benutzer benutzer, String x){
        if(istLeer(x)) return true;
        if(x.length() < 8) return false;
        benutzer.setPasswort(x);
        System.out.println("Das neue Passwort wurde eingegeben.");
        return true;
    }

    public boolean telefonnummerAendern(Benutzer benutzer, String x){
        if(istLeer(x)) return true;
        benutzer.setTelefonnummer(x);
        System.out.println("Die Telefonnummer " + x + " wurde eingegeben.");
        return true;
    }

    public boolean universitaetAendern(Student student, String x){
        if(istLeer(x)) return true;
        student.setUniversitaet(x);
        System.out.println("Die Universität " + x + " wurde eingegeben.");
        return true;
    }

    public boolean semesterAendern(Student student, Integer x){
        if(x == null) return true;
        student.setSemester(x);
        System.out.println("Das Semester " + x + " wurde eingegeben.");
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
        // Keine Zukunftsdaten erlauben.
        if(x.isAfter(LocalDate.now())) return false;
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
