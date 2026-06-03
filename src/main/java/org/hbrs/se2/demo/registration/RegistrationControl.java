package org.hbrs.se2.demo.registration;

// TODO: Für eine echte Implementierung wären hier Imports für JPA-Repositories notwendig.
// import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;

public class RegistrationControl {
	
    // TODO: Eine Referenz auf ein Repository wäre hier notwendig, um die Existenz des Benutzernamens zu prüfen.
    // private final BenutzerRepository benutzerRepository;
    //
    // public RegistrationControl(BenutzerRepository benutzerRepository) {
    //     this.benutzerRepository = benutzerRepository;
    // }

	public RegistrationResult registerUser( UserDTO dto ) {
		RegistrationResult result = new RegistrationResult();

		String mailAddress = dto.getAddress(); // E-Mail-Adresse
        String username = dto.getUserID();
        String password = dto.getPassword();

        // TODO: GANZ AM ANFANG der Registrierung: Lokale Prüfungen durchführen, bevor die Datenbank angefragt wird!

        // Username darf nicht leer sein (Testfall 7)
        if (username == null || username.trim().isEmpty()) {
            // TODO: Konstante in RegistrationResult für USERNAME_MISSING definieren
            result.setReason("Benutzername darf nicht leer sein"); 
            result.setResult(false);
            return result;
        }

        // Passwort darf nicht leer sein (Testfall 6) - Das war die bereits vorhandene Prüfung, leicht angepasst für sauberen Kontrollfluss
		if ( password == null || password.trim().equals("")) {
			result.setReason(RegistrationResult.PASSWORD_MISSING);
			result.setResult(false);
            return result;
		} 

        // Passwort muss mindestens 8 Zeichen lang sein (Testfall 4)
        if (password.length() < 8) {
            // TODO: Konstante in RegistrationResult für PASSWORD_TOO_SHORT definieren
            result.setReason("Passwort zu kurz"); 
            result.setResult(false);
            return result;
        }

        // E-Mail-Format prüfen (Testfall 5)
        if (mailAddress == null || !mailAddress.contains("@")) {
            // TODO: Konstante in RegistrationResult für INVALID_EMAIL_FORMAT definieren
            result.setReason("Ungültiges E-Mail-Format"); 
            result.setResult(false);
            return result;
        }

		// CheckOnDB( mailAddress )  --> Check Existenz über eine DAO (z.B. UserDAO) - ToDo
        // TODO: Datenbank-Validierung: Prüfen ob Username bereits existiert (Testfall 2)
        // Dies erfordert ein echtes Repository, das hier nur simuliert wird.
        // if (benutzerRepository.existsByUsername(username)) {
        //     result.setReason("Benutzername bereits vergeben"); // TODO: Konstante definieren
        //     result.setResult(false);
        //     return result;
        // }

        // Wenn alle Prüfungen erfolgreich sind (Das war der bestehende else-Block)
        result.setReason(RegistrationResult.REGISTRATION_SUCCESSFULL);
        result.setResult(true);
        
		return result;
	}

}