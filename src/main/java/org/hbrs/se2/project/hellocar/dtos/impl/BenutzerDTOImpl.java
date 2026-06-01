package org.hbrs.se2.project.hellocar.dtos.impl;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;

import java.time.LocalDate;

public class BenutzerDTOImpl implements BenutzerDTO {

    private int userId;
    private String vorname;
    private String nachname;
    private LocalDate geburtstag;
    private String username;
    private String email;
    private String telefonnummer;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public void setGeburtstag(LocalDate geburtstag) {
        this.geburtstag = geburtstag;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getVorname() {
        return vorname;
    }

    @Override
    public String getNachname() {
        return nachname;
    }

    @Override
    public LocalDate getGeburtstag() {
        return geburtstag;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getTelefonnummer() {
        return telefonnummer;
    }

    @Override
    public String toString() {
        return "BenutzerDTOImpl{" +
                "userId=" + userId +
                ", vorname='" + vorname + '\'' +
                ", nachname='" + nachname + '\'' +
                ", geburtstag=" + geburtstag +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", telefonnummer='" + telefonnummer + '\'' +
                '}';
    }
}