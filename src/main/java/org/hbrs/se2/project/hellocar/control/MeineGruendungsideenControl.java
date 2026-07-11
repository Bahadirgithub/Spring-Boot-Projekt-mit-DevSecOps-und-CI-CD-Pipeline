package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BewerbungDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Bewertung;
import org.hbrs.se2.project.hellocar.entities.Bewerbung;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BewertungRepository;
import org.hbrs.se2.project.hellocar.repository.BewerbungRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Control Klasse für die Ansicht "Meine Gründungsideen". Verwaltung der eigenen Gründungsideen.
 *
 */
@Component
public class MeineGruendungsideenControl {

    // Status, kann der Gründer später in der View auswählen. Noch nicht weiter implementiert - MM
    private static final List<String> ERLAUBTE_STATUS = List.of(
            "Eingegangen",
            "In Prüfung",
            "Gespräch",
            "Abgelehnt",
            "Eingestellt"
    );

    // Wird benutzt, um die eigenen Ideen des Gründers zu laden.
    private final ManageIdeaControl ideaControl;

    private final BewerbungControl bewerbungControl;

    // Repository für Bewerbungen.
    private final BewerbungRepository bewerbungRepository;

    // Repository für Bewertungen.
    private final BewertungRepository bewertungRepository;

    private final GruendungsideeRepository gruendungsideeRepository;

    // Laden von Benutzerdaten und Studentendaten.
    private final VerwaltungControl verwaltungControl;

    public MeineGruendungsideenControl(ManageIdeaControl ideaControl,
                                       BewerbungRepository bewerbungRepository,
                                       BewertungRepository bewertungRepository,
                                       GruendungsideeRepository gruendungsideeRepository,
                                       VerwaltungControl verwaltungControl,
                                       BewerbungControl bewerbungControl) {
        this.ideaControl = ideaControl;
        this.bewerbungRepository = bewerbungRepository;
        this.bewertungRepository = bewertungRepository;
        this.gruendungsideeRepository = gruendungsideeRepository;
        this.verwaltungControl = verwaltungControl;
        this.bewerbungControl = bewerbungControl;
    }

    /**
     * Lädt alle Gründungsideen, die dem eingeloggten Gründer gehören.
     */
    @Transactional
    public List<GruendungsideeDTO> ladeEigeneGruendungsideen(int gruenderUserId) {
        return ideaControl.readIdeasByGruender(gruenderUserId);
    }

    /**
     * Prüfung ob Idee dem Grüpnder gehört, dann laden der Bewerbungen zu der Idee.
     */
    @Transactional
    public List<BewerbungDTO> ladeBewerbungenFuerEigeneIdee(int gruenderUserId, int ideeId) {
        if (!gehoertIdeeZuGruender(gruenderUserId, ideeId)) {
            return List.of();
        }

        return bewerbungRepository.findByIdeeId(ideeId).stream()
                .map(bewerbungControl::mapToDTO) // <- Aufruf der public Methode
                .toList();
    }

    /**
     * Gleiche Logik, lädt die Bewertungen zu einer Idee.
     */
    @Transactional
    public List<Bewertung> ladeBewertungenFuerEigeneIdee(int gruenderUserId, int ideeId) {
        if (!gehoertIdeeZuGruender(gruenderUserId, ideeId)) {
            return List.of();
        }

        // Neueste Bewertungen sollen oben stehen.
        return bewertungRepository.findByIdeeId(ideeId).stream().sorted(Comparator.comparing(Bewertung::getZeitstempel,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .toList();
    }

    /**
     * Statusänderung - Noch keine weitere Implementation, Chat etc. Ablehnung kommt eventuell noch
     */
    @Transactional
    public boolean aendereStatusFuerBewerbung(int gruenderUserId, int bewerbungId, String neuerStatus) {
        if (neuerStatus == null || !ERLAUBTE_STATUS.contains(neuerStatus)) {
            return false;
        }

        Bewerbung bewerbung = bewerbungRepository.findById(bewerbungId).orElse(null);
        if (bewerbung == null) {
            return false;
        }

        // Sicherheit: Gründer darf nicht fremde Bewerbungen ändern.
        if (!gehoertIdeeZuGruender(gruenderUserId, bewerbung.getIdeeId())) {
            return false;
        }

        bewerbung.setStatus(neuerStatus);
        bewerbungRepository.save(bewerbung);
        return true;
    }

    public List<String> ladeErlaubteStatus() {
        return ERLAUBTE_STATUS;
    }

    // Lädt die Benutzerdaten eines Studenten.
    public BenutzerDTO ladeStudentBenutzer(int studentUserId) {
        return verwaltungControl.ladeBenutzer(studentUserId);
    }

    /**
     * Lädt das Studentenprofil.
     * Hier sind dann z.B. Uni, Semester, Abschluss und Skills drin.
     */
    @Transactional
    public Student ladeStudentProfil(int studentUserId) {
        return verwaltungControl.ladeStudent(studentUserId);
    }

    /**
     * Prüft, ob ein Skill vom Studenten auch in der Idee gesucht wird. - Und dann Matching in der View :)
     */
    public boolean istSkillPassendZurIdee(String studentSkillName, GruendungsideeDTO idee) {
        if (studentSkillName == null || studentSkillName.isBlank()) {
            return false;
        }

        if (idee == null || idee.getGesuchteSkills() == null) {
            return false;
        }

        String studentSkillNormalisiert = normalisiereSkill(studentSkillName);

        return idee.getGesuchteSkills().stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(this::normalisiereSkill)
                .anyMatch(skill -> skill.equals(studentSkillNormalisiert));
    }

    /**
     * Kleine Hilfsmethode.
     * Prüft, ob eine Idee wirklich dem Gründer gehört.
     */
    private boolean gehoertIdeeZuGruender(int gruenderUserId, int ideeId) {
        Gruendungsidee idee = gruendungsideeRepository.findById(ideeId).orElse(null);

        if (idee == null) {
            return false;
        }

        return idee.getGruenderUserId() == gruenderUserId;
    }

    /**
     * Leichte Normalisierung des Skills, lower + leerzeichen, aber eigentlich nicht notwendig.
     */
    private String normalisiereSkill(String skill) {
        return skill.trim().toLowerCase(Locale.ROOT);
    }
}