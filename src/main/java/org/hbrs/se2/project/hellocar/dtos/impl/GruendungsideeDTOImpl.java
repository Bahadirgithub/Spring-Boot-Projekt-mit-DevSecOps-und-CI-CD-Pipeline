package org.hbrs.se2.project.hellocar.dtos.impl;

import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.dtos.GruenderDTO;

public class GruendungsideeDTOImpl implements GruendungsideeDTO {

    private int ideeId;
    private String beschreibung;
    private String position;
    private String anforderungen;
    private String arbeitsbedingungen;
    private GruenderDTO gruender;
    private int gruenderUserId;
    private String unternehmensname;

    @Override
    public int getIdeeId() {
        return ideeId;
    }

    public void setIdeeId(int ideeId) {
        this.ideeId = ideeId;
    }

    @Override
    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    @Override
    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String getAnforderungen() {
        return anforderungen;
    }

    public void setAnforderungen(String anforderungen) {
        this.anforderungen = anforderungen;
    }

    @Override
    public String getArbeitsbedingungen() {
        return arbeitsbedingungen;
    }

    public void setArbeitsbedingungen(String arbeitsbedingungen) {
        this.arbeitsbedingungen = arbeitsbedingungen;
    }

    @Override
    public GruenderDTO getGruender() {
        return gruender;
    }

    @Override
    public int getGruenderUserId() {
        return gruenderUserId;
    }

    public void setGruenderUserId(int gruenderUserId) {
        this.gruenderUserId = gruenderUserId;
    }

    public void setGruender(GruenderDTO gruender) {
        this.gruender = gruender;
    }

    @Override
    public String getUnternehmensname() {
        return unternehmensname;
    }

    public void setUnternehmensname(String unternehmensname) {
        this.unternehmensname = unternehmensname;
    }
}