package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "bewerbung", schema = "ahsogehts")
public class Bewerbung {

    private int bewerbungId;
    private int studentUserId;
    private int ideeId;
    private LocalDate bewerbungsdatum;
    private String status;
    private String anschreibenDateiname;
    private String anschreibenDateipfad;
    private String lebenslaufDateiname;
    private String lebenslaufDateipfad;

    @Id
    @Column(name = "bewerbung_id")
    public int getBewerbungId() {
        return bewerbungId;
    }

    public void setBewerbungId(int bewerbungId) {
        this.bewerbungId = bewerbungId;
    }

    @Basic
    @Column(name = "student_user_id")
    public int getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(int studentUserId) {
        this.studentUserId = studentUserId;
    }

    @Basic
    @Column(name = "idee_id")
    public int getIdeeId() {
        return ideeId;
    }

    public void setIdeeId(int ideeId) {
        this.ideeId = ideeId;
    }

    @Basic
    @Column(name = "bewerbungsdatum")
    public LocalDate getBewerbungsdatum() {
        return bewerbungsdatum;
    }

    public void setBewerbungsdatum(LocalDate bewerbungsdatum) {
        this.bewerbungsdatum = bewerbungsdatum;
    }

    @Basic
    @Column(name = "status")
    @ColumnTransformer(write = "?::ahsogehts.bewerbung_status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Basic
    @Column(name = "anschreiben_dateiname")
    public String getAnschreibenDateiname() {
        return anschreibenDateiname;
    }

    public void setAnschreibenDateiname(String anschreibenDateiname) {
        this.anschreibenDateiname = anschreibenDateiname;
    }

    @Basic
    @Column(name = "anschreiben_dateipfad")
    public String getAnschreibenDateipfad() {
        return anschreibenDateipfad;
    }

    public void setAnschreibenDateipfad(String anschreibenDateipfad) {
        this.anschreibenDateipfad = anschreibenDateipfad;
    }

    @Basic
    @Column(name = "lebenslauf_dateiname")
    public String getLebenslaufDateiname() {
        return lebenslaufDateiname;
    }

    public void setLebenslaufDateiname(String lebenslaufDateiname) {
        this.lebenslaufDateiname = lebenslaufDateiname;
    }

    @Basic
    @Column(name = "lebenslauf_dateipfad")
    public String getLebenslaufDateipfad() {
        return lebenslaufDateipfad;
    }

    public void setLebenslaufDateipfad(String lebenslaufDateipfad) {
        this.lebenslaufDateipfad = lebenslaufDateipfad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bewerbung bewerbung)) return false;
        return bewerbungId == bewerbung.bewerbungId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bewerbungId);
    }
}