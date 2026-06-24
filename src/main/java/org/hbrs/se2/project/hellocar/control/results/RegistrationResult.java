package org.hbrs.se2.project.hellocar.control.results;

/**
 * Definiert die möglichen Ergebnisse einer Benutzer-Registrierung.
 * Ersetzt einen einfachen boolean, um dem Aufrufer (UI oder Test)
 * spezifisches Feedback zum Grund eines Fehlschlags zu geben.
 */
public enum RegistrationResult {
    SUCCESS,                    // Registrierung war erfolgreich
    USERNAME_ALREADY_EXISTS,    // Der Benutzername ist bereits vergeben
    USERNAME_IS_EMPTY,          // Der Benutzername ist leer
    PASSWORD_INVALID,         // Das Passwort erfüllt nicht die Mindestlänge (oder ist leer)
    INVALID_EMAIL_FORMAT,
    EMPTY_FIELD             // Die E-Mail-Adresse hat ein ungültiges Format
}
// die folgenden Enums sollten zu Exceptions umgewandelt werden
// ValidateAndCreate User sollte in eigene Klasse ausgelagert werden
// Anforderungen sollten in die ValidateKlasse gehen