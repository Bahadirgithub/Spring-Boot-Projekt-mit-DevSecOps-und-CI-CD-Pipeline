package org.hbrs.se2.project.hellocar.dtos.impl;

import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;

import java.time.LocalDate;

public class BewerbungDTOImpl implements BewerbungDTO {

    private int bewerbungId;
    private int studentUserId;
    private int ideeId;
    private LocalDate bewerbungsdatum;
    private String status;
    private String anschreibenDateiname;
    private String anschreibenDateipfad;
    private String lebenslaufDateiname;
    private String lebenslaufDateipfad;

    @Override
    public int getBewerbungId() {
        return bewerbungId;
    }

    public void setBewerbungId(int bewerbungId) {
        this.bewerbungId = bewerbungId;
    }

    @Override
    public int getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(int studentUserId) {
        this.studentUserId = studentUserId;
    }

    @Override
    public int getIdeeId() {
        return ideeId;
    }

    public void setIdeeId(int ideeId) {
        this.ideeId = ideeId;
    }

    @Override
    public LocalDate getBewerbungsdatum() {
        return bewerbungsdatum;
    }

    public void setBewerbungsdatum(LocalDate bewerbungsdatum) {
        this.bewerbungsdatum = bewerbungsdatum;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getAnschreibenDateiname() {
        return anschreibenDateiname;
    }

    public void setAnschreibenDateiname(String anschreibenDateiname) {
        this.anschreibenDateiname = anschreibenDateiname;
    }

    @Override
    public String getAnschreibenDateipfad() {
        return anschreibenDateipfad;
    }

    public void setAnschreibenDateipfad(String anschreibenDateipfad) {
        this.anschreibenDateipfad = anschreibenDateipfad;
    }

    @Override
    public String getLebenslaufDateiname() {
        return lebenslaufDateiname;
    }

    public void setLebenslaufDateiname(String lebenslaufDateiname) {
        this.lebenslaufDateiname = lebenslaufDateiname;
    }

    @Override
    public String getLebenslaufDateipfad() {
        return lebenslaufDateipfad;
    }

    public void setLebenslaufDateipfad(String lebenslaufDateipfad) {
        this.lebenslaufDateipfad = lebenslaufDateipfad;
    }

    @Override
    public String toString() {
        return "BewerbungDTOImpl{" +
                "bewerbungId=" + bewerbungId +
                ", studentUserId=" + studentUserId +
                ", ideeId=" + ideeId +
                ", bewerbungsdatum=" + bewerbungsdatum +
                ", status='" + status + '\'' +
                '}';
    }
}