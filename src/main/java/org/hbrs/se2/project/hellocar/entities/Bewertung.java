package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bewertung", schema = "ahsogehts")
public class Bewertung {

    private int bewertungId;
    private int studentUserId;
    private int ideeId;
    private int bewertung;
    private String kommentar;
    private LocalDateTime zeitstempel;

    @Id
    @Column(name = "bewertung_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getBewertungId() {
        return bewertungId;
    }

    public void setBewertungId(int bewertungId) {
        this.bewertungId = bewertungId;
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
    @Column(name = "bewertung")
    public int getBewertung() {
        return bewertung;
    }

    public void setBewertung(int bewertung) {
        this.bewertung = bewertung;
    }

    @Basic
    @Column(name = "kommentar")
    public String getKommentar() {
        return kommentar;
    }

    public void setKommentar(String kommentar) {
        this.kommentar = kommentar;
    }

    @Basic
    @Column(name = "zeitstempel")
    public LocalDateTime getZeitstempel() {
        return zeitstempel;
    }

    public void setZeitstempel(LocalDateTime zeitstempel) {
        this.zeitstempel = zeitstempel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bewertung bewertung)) return false;
        return bewertungId == bewertung.bewertungId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bewertungId);
    }
}