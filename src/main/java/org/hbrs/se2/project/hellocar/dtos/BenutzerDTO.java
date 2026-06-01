package org.hbrs.se2.project.hellocar.dtos;

import java.time.LocalDate;

public interface BenutzerDTO {

    int getUserId();

    String getVorname();

    String getNachname();

    LocalDate getGeburtstag();

    String getUsername();

    String getEmail();

    String getTelefonnummer();
}