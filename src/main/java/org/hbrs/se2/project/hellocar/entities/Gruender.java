package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "gruender", schema = "ahsogehts")
public class Gruender {

    private int userId;
    private String gruenderId;
    private String unternehmensname;
    private String standort;

    @Id
    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "gruender_id")
    public String getGruenderId() {
        return gruenderId;
    }

    public void setGruenderId(String gruenderId) {
        this.gruenderId = gruenderId;
    }

    @Basic
    @Column(name = "unternehmensname")
    public String getUnternehmensname() {
        return unternehmensname;
    }

    public void setUnternehmensname(String unternehmensname) {
        this.unternehmensname = unternehmensname;
    }

    @Basic
    @Column(name = "standort")
    public String getStandort() {
        return standort;
    }

    public void setStandort(String standort) {
        this.standort = standort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gruender gruender)) return false;
        return userId == gruender.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}