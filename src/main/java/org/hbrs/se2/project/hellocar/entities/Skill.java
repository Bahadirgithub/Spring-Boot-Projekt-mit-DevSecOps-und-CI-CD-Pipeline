package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "skill", schema = "ahsogehts")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Integer skillId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "kategorie")
    private String kategorie;

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKategorie() {
        return kategorie;
    }

    public void setKategorie(String kategorie) {
        this.kategorie = kategorie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill skill)) return false;
        return Objects.equals(skillId, skill.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId);
    }
}
