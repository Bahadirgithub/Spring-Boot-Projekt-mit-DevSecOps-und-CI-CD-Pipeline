package org.hbrs.se2.project.hellocar.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "benutzer", schema = "ahsogehts")
public class Benutzer {

    private int userId;
    private LocalDate geburtstag;
    private String email;
    private String vorname;
    private String nachname;
    private String username;
    private String passwort;
    private String telefonnummer;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "geburtstag")
    public LocalDate getGeburtstag() {
        return geburtstag;
    }

    public void setGeburtstag(LocalDate geburtstag) {
        this.geburtstag = geburtstag;
    }

    @Basic
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "vorname")
    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    @Basic
    @Column(name = "nachname")
    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    @Basic
    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Basic
    @Column(name = "passwort")
    public String getPasswort() {
        return passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }

    @Basic
    @Column(name = "telefonnummer")
    public String getTelefonnummer() {
        return telefonnummer;
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Benutzer benutzer)) return false;
        return userId == benutzer.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}