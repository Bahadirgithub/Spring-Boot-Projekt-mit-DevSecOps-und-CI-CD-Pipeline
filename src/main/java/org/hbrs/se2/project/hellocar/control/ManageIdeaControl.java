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

    /**
     * Holt alle Gründungsideen aus der Datenbank und gibt sie als DTOs zurück.
     */
    @Transactional
    public List<GruendungsideeDTO> readAllIdeas() {
        // Entities aus der Datenbank laden
        List<Gruendungsidee> entities = repository.findAll();

        // Leere Liste für die DTO-Interfaces erstellen
        List<GruendungsideeDTO> dtoList = new ArrayList<>();

        // Jede Entity einzeln durchgehen und Werte kopieren
        for (Gruendungsidee entity : entities) {
            GruendungsideeDTOImpl dto = new GruendungsideeDTOImpl();

            dto.setIdeeId(entity.getIdeeId());
            dto.setGruenderUserId(entity.getGruenderUserId());
            dto.setBeschreibung(entity.getBeschreibung());
            dto.setPosition(entity.getPosition());
            dto.setAnforderungen(entity.getAnforderungen());
            dto.setArbeitsbedingungen(entity.getArbeitsbedingungen());
            dto.setGesuchteSkills(entity.getGesuchteSkills());
            dto.setGruender(null); // Dummy, solange keine Relation aktiv ist

            // Da der Gründer immer existiert, laden wir ihn direkt per .orElseThrow()
            Gruender gruender = gruenderRepository.findById(entity.getGruenderUserId())
                    .orElseThrow(() -> new IllegalStateException("Datenkonsistenz verletzt: Gründer mit ID " + entity.getGruenderUserId() + " nicht gefunden!"));

            // Unternehmensnamen direkt übergeben
            dto.setUnternehmensname(gruender.getUnternehmensname());

            // Das befüllte DTO in die Interface-Liste einfügen
            dtoList.add(dto);
        }

        return dtoList;
    }

    /**
     * Speichert eine neu erstellte Gründungsidee in der Datenbank.
     */
    @Transactional
    public void createIdea(GruendungsideeDTOImpl dto) {
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

    //Hilfsmethode um Unternehmensname zu bekommen
    public String getUnternehmensnameByUserId(int userId) {
        // Wir suchen im Gründer-Repository nach der ID des eingeloggten Nutzers
        return gruenderRepository.findById(userId)
                .map(Gruender::getUnternehmensname)
                .orElse("Kein Unternehmen hinterlegt");
        // Fallback, falls der Nutzer (z.B. ein Admin oder Bewerber) kein Gründer ist
    }
}
