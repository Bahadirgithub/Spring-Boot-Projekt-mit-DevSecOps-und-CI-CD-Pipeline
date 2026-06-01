package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.hbrs.se2.project.hellocar.util.Globals;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationControl {

    private final StudentRepository studentRepository;
    private final GruenderRepository gruenderRepository;

    public AuthorizationControl(StudentRepository studentRepository,
                                GruenderRepository gruenderRepository) {
        this.studentRepository = studentRepository;
        this.gruenderRepository = gruenderRepository;
    }

    public boolean isLoggedIn(BenutzerDTO benutzer) {
        return benutzer != null;
    }

    public boolean isStudent(BenutzerDTO benutzer) {
        if (benutzer == null) {
            return false;
        }

        return studentRepository.existsById(benutzer.getUserId());
    }

    public boolean isGruender(BenutzerDTO benutzer) {
        if (benutzer == null) {
            return false;
        }

        return gruenderRepository.existsById(benutzer.getUserId());
    }

    public boolean canAccessShowCars(BenutzerDTO benutzer) {
        return isLoggedIn(benutzer);
    }

    public boolean canAccessEnterCar(BenutzerDTO benutzer) {
        /*
         * Neue Logik:
         * Wenn wir keine Rollen haben, kann man hier z. B. sagen:
         * Nur Gründer dürfen diese Seite sehen.
         * Dann noch ändern mit ACCESS!
         */
        return isGruender(benutzer);
    }

    public boolean canAccessFeature(BenutzerDTO benutzer, String feature) {
        if (feature.equals(Globals.Pages.SHOW_CARS)) {
            return canAccessShowCars(benutzer);
        }

        if (feature.equals(Globals.Pages.ENTER_CAR)) {
            return canAccessEnterCar(benutzer);
        }

        return false;
    }
}