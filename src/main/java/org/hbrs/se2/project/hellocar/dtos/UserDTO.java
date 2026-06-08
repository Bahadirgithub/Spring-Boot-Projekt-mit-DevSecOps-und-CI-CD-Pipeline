package org.hbrs.se2.project.hellocar.dtos;

import org.hbrs.se2.project.hellocar.entities.Rolle;

import java.util.List;

public interface UserDTO {
    int getId();
    String getFirstName();
    String getLastName();
    List<RolleDTO> getRoles();
}
