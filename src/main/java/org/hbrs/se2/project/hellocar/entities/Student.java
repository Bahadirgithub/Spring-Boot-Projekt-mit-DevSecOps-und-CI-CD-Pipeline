package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "student", schema = "ahsogehts")
public class Student {

    private int userId;
    private String studentId;
    private String universitaet;
    private int semester;
    private String skills;
    private String abschluss;

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
    @Column(name = "skills")
    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    @Basic
    @Column(name = "abschluss")
    public String getAbschluss() {
        return abschluss;
    }

    public void setAbschluss(String abschluss) {
        this.abschluss = abschluss;
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