package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "gruendungsidee", schema = "ahsogehts")
public class Gruendungsidee {

    private int ideeId;
    private int gruenderUserId;
    private String beschreibung;
    private String position;
    private String anforderungen;
    private String arbeitsbedingungen;

    private Set<Skill> gesuchteSkillEntities = new HashSet<>();

    // Nur als nicht-persistenter Fallback für DTO-/Testcode.
    private List<String> gesuchteSkillsNamen = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idee_id")
    public int getIdeeId() {
        return ideeId;
    }

    public void setIdeeId(int ideeId) {
        this.ideeId = ideeId;
    }

    @Basic
    @Column(name = "gruender_user_id")
    public int getGruenderUserId() {
        return gruenderUserId;
    }

    public void setGruenderUserId(int gruenderUserId) {
        this.gruenderUserId = gruenderUserId;
    }

    @Basic
    @Column(name = "beschreibung")
    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    @Basic
    @Column(name = "position")
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Basic
    @Column(name = "anforderungen")
    public String getAnforderungen() {
        return anforderungen;
    }

    public void setAnforderungen(String anforderungen) {
        this.anforderungen = anforderungen;
    }

    @Basic
    @Column(name = "arbeitsbedingungen")
    public String getArbeitsbedingungen() {
        return arbeitsbedingungen;
    }

    public void setArbeitsbedingungen(String arbeitsbedingungen) {
        this.arbeitsbedingungen = arbeitsbedingungen;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "gruendungsidee_skills",
            schema = "ahsogehts",
            joinColumns = @JoinColumn(name = "idee_id", referencedColumnName = "idee_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id", referencedColumnName = "skill_id")
    )
    public Set<Skill> getGesuchteSkillEntities() {
        return gesuchteSkillEntities;
    }

    public void setGesuchteSkillEntities(Set<Skill> gesuchteSkillEntities) {
        this.gesuchteSkillEntities = gesuchteSkillEntities == null ? new HashSet<>() : gesuchteSkillEntities;
    }

    /**
     * Nur Hilfsmethode für DTO/View: gibt die Namen der gesuchten Skills zurück.
     * Wird nicht als eigene Datenbankspalte gemappt.
     */
    @Transient
    public List<String> getGesuchteSkills() {
        if (gesuchteSkillEntities != null && !gesuchteSkillEntities.isEmpty()) {
            return gesuchteSkillEntities.stream()
                    .map(Skill::getName)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();
        }

        return gesuchteSkillsNamen == null ? Collections.emptyList() : gesuchteSkillsNamen;
    }

    /**
     * Kompatibilitäts-Setter für DTO-/Testcode. Persistiert wird über setGesuchteSkillEntities(...).
     */
    public void setGesuchteSkills(List<String> gesuchteSkills) {
        this.gesuchteSkillsNamen = gesuchteSkills == null ? new ArrayList<>() : new ArrayList<>(gesuchteSkills);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gruendungsidee idee)) return false;
        return ideeId == idee.ideeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ideeId);
    }
}
