package org.hbrs.se2.project.hellocar.dtos;

import java.time.LocalDate;

public interface CarDTO {

    String getBrand();

    String getModel();

    String getDescription();

    LocalDate getDate();

    String getPhone();

    String getPrice();

}