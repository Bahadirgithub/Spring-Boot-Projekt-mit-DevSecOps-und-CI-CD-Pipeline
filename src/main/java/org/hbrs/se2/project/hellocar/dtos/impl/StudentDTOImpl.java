package org.hbrs.se2.project.hellocar.dtos.impl;

import org.hbrs.se2.project.hellocar.dtos.StudentDTO;

public class StudentDTOImpl implements StudentDTO {

    private int userId;
    private String studentId;
    private String universitaet;
    private int semester;
    private String skills;
    private String abschluss;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setUniversitaet(String universitaet) {
        this.universitaet = universitaet;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void setAbschluss(String abschluss) {
        this.abschluss = abschluss;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getStudentId() {
        return studentId;
    }

    @Override
    public String getUniversitaet() {
        return universitaet;
    }

    @Override
    public int getSemester() {
        return semester;
    }

    @Override
    public String getSkills() {
        return skills;
    }

    @Override
    public String getAbschluss() {
        return abschluss;
    }

    @Override
    public String toString() {
        return "StudentDTOImpl{" +
                "userId=" + userId +
                ", studentId='" + studentId + '\'' +
                ", universitaet='" + universitaet + '\'' +
                ", semester=" + semester +
                ", skills='" + skills + '\'' +
                ", abschluss='" + abschluss + '\'' +
                '}';
    }
}