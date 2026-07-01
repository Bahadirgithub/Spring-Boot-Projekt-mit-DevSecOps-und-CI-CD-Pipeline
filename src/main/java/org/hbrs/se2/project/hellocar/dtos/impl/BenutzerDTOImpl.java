package org.hbrs.se2.project.hellocar.dtos.impl;

import lombok.*;
import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BenutzerDTOImpl implements BenutzerDTO {

    @Getter @Setter private int userId;
    @Getter @Setter private String username;
    @Getter @Setter private String vorname;
    @Getter @Setter private String nachname;
    @Getter @Setter private LocalDate geburtstag;
    @Getter @Setter private String email;
    @Getter @Setter private String telefonnummer;
}