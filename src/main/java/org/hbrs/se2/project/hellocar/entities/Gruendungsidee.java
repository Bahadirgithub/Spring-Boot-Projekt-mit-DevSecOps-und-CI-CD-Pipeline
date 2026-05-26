package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "gruendungsidee", schema = "ahsogehts")
public class Gruendungsidee {

    private int ideeId;
    private int gruenderUserId;
    private String beschreibung;
    private String position;
    private String anforderungen;
    private String arbeitsbedingungen;

    @Id
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