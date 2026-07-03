package org.hbrs.se2.project.hellocar.dtos;

import java.time.LocalDate;

public interface BewerbungDTO {

    int getBewerbungId();

    int getStudentUserId();

    int getIdeeId();

    LocalDate getBewerbungsdatum();

    String getStatus();

    String getAnschreibenDateiname();

    String getAnschreibenDateipfad();

    String getLebenslaufDateiname();

    String getLebenslaufDateipfad();
}