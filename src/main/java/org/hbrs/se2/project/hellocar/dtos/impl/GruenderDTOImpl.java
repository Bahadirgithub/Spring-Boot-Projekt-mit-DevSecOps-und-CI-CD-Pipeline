package org.hbrs.se2.project.hellocar.dtos.impl;

import org.hbrs.se2.project.hellocar.dtos.GruenderDTO;

public class GruenderDTOImpl implements GruenderDTO {

    private int userId;
    private String gruenderId;
    private String unternehmensname;
    private String standort;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setGruenderId(String gruenderId) {
        this.gruenderId = gruenderId;
    }

    public void setUnternehmensname(String unternehmensname) {
        this.unternehmensname = unternehmensname;
    }

    public void setStandort(String standort) {
        this.standort = standort;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getGruenderId() {
        return gruenderId;
    }

    @Override
    public String getUnternehmensname() {
        return unternehmensname;
    }

    @Override
    public String getStandort() {
        return standort;
    }

    @Override
    public String toString() {
        return "GruenderDTOImpl{" +
                "userId=" + userId +
                ", gruenderId='" + gruenderId + '\'' +
                ", unternehmensname='" + unternehmensname + '\'' +
                ", standort='" + standort + '\'' +
                '}';
    }
}