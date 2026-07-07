package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BewerbungDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Bewerbung;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BewerbungRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Control-Klasse für die Gründer-Ansicht "Meine Gründungsideen".
 * Hier liegt die Logik zum Laden eigener Ideen, Bewerbungen und Statusänderungen.
 */
@Component
public class MeineGruendungsideenControl {

    private static final List<String> ERLAUBTE_STATUS = List.of(
            "Eingegangen",
            "In Prüfung",
            "Gespräch",
            "Abgelehnt",
            "Eingestellt"
    );

    private final ManageIdeaControl ideaControl;
    private final BewerbungRepository bewerbungRepository;
    private final GruendungsideeRepository gruendungsideeRepository;
    private final VerwaltungControl verwaltungControl;

    public MeineGruendungsideenControl(ManageIdeaControl ideaControl,
                                       BewerbungRepository bewerbungRepository,
                                       GruendungsideeRepository gruendungsideeRepository,
                                       VerwaltungControl verwaltungControl) {
        this.ideaControl = ideaControl;
        this.bewerbungRepository = bewerbungRepository;
        this.gruendungsideeRepository = gruendungsideeRepository;
        this.verwaltungControl = verwaltungControl;
    }

    /**
     * Lädt nur die Gründungsideen des eingeloggten Gründers.
     */
    @Transactional
    public List<GruendungsideeDTO> ladeEigeneGruendungsideen(int gruenderUserId) {
        return ideaControl.readIdeasByGruender(gruenderUserId);
    }

    /**
     * Lädt die Bewerbungen für genau eine eigene Gründungsidee.
     * Ein Gründer darf nur Bewerbungen für seine eigenen Ideen sehen.
     */
    @Transactional
    public List<BewerbungDTO> ladeBewerbungenFuerEigeneIdee(int gruenderUserId, int ideeId) {
        if (!gehoertIdeeZuGruender(gruenderUserId, ideeId)) {
            return List.of();
        }

        return bewerbungRepository.findByIdeeId(ideeId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Ändert den Status einer Bewerbung.
     * Sicherheitsprüfung: Die Bewerbung muss zu einer Idee des Gründers gehören.
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

        if (!gehoertIdeeZuGruender(gruenderUserId, bewerbung.getIdeeId())) {
            return false;
        }

        bewerbung.setStatus(neuerStatus);
        bewerbungRepository.save(bewerbung);
        return true;
    }

    /**
     * Statuswerte für die Auswahl im Frontend.
     */
    public List<String> ladeErlaubteStatus() {
        return ERLAUBTE_STATUS;
    }

    /**
     * Lädt die Benutzer-Stammdaten eines Studenten.
     */
    public BenutzerDTO ladeStudentBenutzer(int studentUserId) {
        return verwaltungControl.ladeBenutzer(studentUserId);
    }

    /**
     * Lädt das Studentenprofil inklusive Skills.
     */
    @Transactional
    public Student ladeStudentProfil(int studentUserId) {
        return verwaltungControl.ladeStudent(studentUserId);
    }

    /**
     * Prüft, ob ein Studentenskill zu den gesuchten Skills der Idee passt.
     */
    public boolean istSkillPassendZurIdee(String studentSkillName, GruendungsideeDTO idee) {
        if (studentSkillName == null || studentSkillName.isBlank() || idee == null || idee.getGesuchteSkills() == null) {
            return false;
        }

        String studentSkillNormalisiert = normalisiereSkill(studentSkillName);

        return idee.getGesuchteSkills().stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(this::normalisiereSkill)
                .anyMatch(skill -> skill.equals(studentSkillNormalisiert));
    }

    /**
     * Prüft, ob eine Idee dem Gründer gehört.
     */
    private boolean gehoertIdeeZuGruender(int gruenderUserId, int ideeId) {
        Gruendungsidee idee = gruendungsideeRepository.findById(ideeId).orElse(null);
        return idee != null && idee.getGruenderUserId() == gruenderUserId;
    }

    /**
     * Mapping von Entity auf DTO für die View.
     */
    private BewerbungDTO mapToDTO(Bewerbung entity) {
        BewerbungDTOImpl dto = new BewerbungDTOImpl();
        dto.setBewerbungId(entity.getBewerbungId());
        dto.setStudentUserId(entity.getStudentUserId());
        dto.setIdeeId(entity.getIdeeId());
        dto.setBewerbungsdatum(entity.getBewerbungsdatum());
        dto.setStatus(entity.getStatus());
        dto.setAnschreibenDateiname(entity.getAnschreibenDateiname());
        dto.setAnschreibenDateipfad(entity.getAnschreibenDateipfad());
        dto.setLebenslaufDateiname(entity.getLebenslaufDateiname());
        dto.setLebenslaufDateipfad(entity.getLebenslaufDateipfad());
        return dto;
    }

    /**
     * Einfache Normalisierung für Skill-Vergleich.
     */
    private String normalisiereSkill(String skill) {
        return skill.trim().toLowerCase(Locale.ROOT);
    }
}
