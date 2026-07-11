package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.GruendungsideeDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.entities.Skill;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.hbrs.se2.project.hellocar.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ManageIdeaControl {

    @Autowired
    private GruendungsideeRepository repository;

    @Autowired
    private GruenderRepository gruenderRepository;

    @Autowired
    private SkillRepository skillRepository;

    public List<String> ladeAlleSkillNamen() {
        return skillRepository.findAllByOrderByKategorieAscNameAsc()
                .stream()
                .map(Skill::getName)
                .toList();
    }

    private GruendungsideeDTO mapToDTO(Gruendungsidee entity) {
        GruendungsideeDTOImpl dto = new GruendungsideeDTOImpl();
        dto.setIdeeId(entity.getIdeeId());
        dto.setGruenderUserId(entity.getGruenderUserId());
        dto.setBeschreibung(entity.getBeschreibung());
        dto.setPosition(entity.getPosition());
        dto.setAnforderungen(entity.getAnforderungen());
        dto.setArbeitsbedingungen(entity.getArbeitsbedingungen());
        dto.setGesuchteSkills(entity.getGesuchteSkills()); // Falls deine Entity das direkt zurückgibt

        // Gründerdaten laden
        Gruender gruender = gruenderRepository.findById(entity.getGruenderUserId())
                .orElseThrow(() -> new IllegalStateException("Datenkonsistenz verletzt!"));
        dto.setUnternehmensname(gruender.getUnternehmensname());

        return dto;
    }

    @Transactional
    public GruendungsideeDTO findIdeaById(int ideeId) {
        return repository.findById(ideeId)
                .map(this::mapToDTO)
                .orElse(null);
    }

    /**
     * Holt alle Gründungsideen aus der Datenbank und gibt sie als DTOs zurück.
     */
    @Transactional
    public List<GruendungsideeDTO> readAllIdeas() {
        // Nutze Stream und die Hilfsmethode, statt die Liste manuell zu befüllen
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Holt nur die Gründungsideen des aktuell eingeloggten Gründers.
     */
    @Transactional
    public List<GruendungsideeDTO> readIdeasByGruender(int gruenderUserId) {
        return repository.findByGruenderUserId(gruenderUserId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Speichert eine neu erstellte Gründungsidee in der Datenbank.
     */
    @Transactional
    public void createIdea(GruendungsideeDTOImpl dto) {
        //Exception werfen wenn die Beschreibung leer ist
        if (dto.getBeschreibung() == null || dto.getBeschreibung().isBlank()) {
            throw new IllegalArgumentException("Beschreibung darf nicht leer sein.");
        }

        // 1. Eine neue leere Entity für die Datenbank erstellen
        Gruendungsidee entity = new Gruendungsidee();

        // 2. Die Werte aus dem DTO in die Entity kopieren (Mapping)
        // Hinweis: Die ideeId wird von der DB per Auto-Increment (Id) erzeugt, daher setzen wir sie hier nicht.
        entity.setGruenderUserId(dto.getGruenderUserId());
        entity.setBeschreibung(dto.getBeschreibung());
        entity.setPosition(dto.getPosition());
        entity.setAnforderungen(dto.getAnforderungen());
        entity.setArbeitsbedingungen(dto.getArbeitsbedingungen());
        entity.setGesuchteSkillEntities(ladeSkillEntities(dto.getGesuchteSkills()));

        // 3. Die Entity über das Spring-Data-Repository in die DB schreiben
        repository.save(entity);
    }

    private Set<Skill> ladeSkillEntities(List<String> skillNamen) {
        if (skillNamen == null || skillNamen.isEmpty()) {
            return new HashSet<>();
        }

        List<String> namen = skillNamen.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (namen.isEmpty()) {
            return new HashSet<>();
        }

        return new HashSet<>(skillRepository.findByNameIn(namen));
    }

    /**
     * Kurzvorschau der gesuchten Skills für die Tabellenanzeige: sortiert, auf maximal 3 Einträge
     * gekürzt, restliche Anzahl als "+N" angehängt.
     */
    public static String formatSkillsPreview(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return "-";
        }

        List<String> sortierteSkills = skills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();

        if (sortierteSkills.isEmpty()) {
            return "-";
        }

        int maxAnzeigen = 3;
        int anzahl = Math.min(maxAnzeigen, sortierteSkills.size());
        String preview = String.join(", ", sortierteSkills.subList(0, anzahl));
        int rest = sortierteSkills.size() - anzahl;

        return rest > 0 ? preview + " +" + rest : preview;
    }

    //Hilfsmethode um Unternehmensname zu bekommen
    public String getUnternehmensnameByUserId(int userId) {
        // Wir suchen im Gründer-Repository nach der ID des eingeloggten Nutzers
        return gruenderRepository.findById(userId)
                .map(Gruender::getUnternehmensname)
                .orElse("Kein Unternehmen hinterlegt");
        // Fallback, falls der Nutzer (z.B. ein Admin oder Bewerber) kein Gründer ist
    }
}
