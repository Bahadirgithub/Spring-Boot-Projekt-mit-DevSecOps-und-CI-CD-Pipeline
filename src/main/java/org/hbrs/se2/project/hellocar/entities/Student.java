package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "student", schema = "ahsogehts")
public class Student {

    private int userId;
    private String studentId;
    private String universitaet;
    private int semester;
    private String abschluss;

    private Set<Skill> skillEntities = new HashSet<>();

    @Id
    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "student_id")
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @Basic
    @Column(name = "universitaet")
    public String getUniversitaet() {
        return universitaet;
    }

    public void setUniversitaet(String universitaet) {
        this.universitaet = universitaet;
    }

    @Basic
    @Column(name = "semester")
    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    @Basic
    @Column(name = "abschluss")
    public String getAbschluss() {
        return abschluss;
    }

    public void setAbschluss(String abschluss) {
        this.abschluss = abschluss;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_skills",
            schema = "ahsogehts",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id", referencedColumnName = "skill_id")
    )
    public Set<Skill> getSkillEntities() {
        return skillEntities;
    }

    public void setSkillEntities(Set<Skill> skillEntities) {
        this.skillEntities = skillEntities == null ? new HashSet<>() : skillEntities;
    }
    @Transient
    public List<String> getSkills() {
        if (skillEntities == null) {
            return Collections.emptyList();
        }

        return skillEntities.stream()
                .map(Skill::getName)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;
        return userId == student.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
