package org.hbrs.se2.project.hellocar.control;

import jakarta.transaction.Transactional;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class PromptControl {

    @Autowired

    private GruendungsideeRepository repo;

    @Transactional // Zwingt Spring, eine DB-Session zu öffnen, während der Prompt gebaut wird
    public String getSystemPrompt() {

        List<Gruendungsidee> karriere = repo.findAll();

        String ideen;

        if (karriere.isEmpty()) {
            ideen = "Es sind keine Gründungsideen in der Datenbank vorhanden";
        } else {
            ideen = karriere.stream()
                    .map(idee -> """
                            Idee-ID: %s
                            Gründer-User-ID: %s
                            Position: %s
                            Beschreibung: %s
                            Anforderungen: %s
                            Arbeitsbedingungen: %s
                            Gesuchte Skills: %s
                            Skill-Entities: %s
                            """.formatted(
                            idee.getIdeeId(),
                            idee.getGruenderUserId(),
                            idee.getPosition(),
                            idee.getBeschreibung(),
                            idee.getAnforderungen(),
                            idee.getArbeitsbedingungen(),
                            idee.getGesuchteSkills(),
                            idee.getGesuchteSkillEntities()
                    ))
                    .collect(Collectors.joining("\n---\n"));
        }
        return """
                Du bist ein Startup-Experte für Studenten. Du erklärst, vergleichst und bewertest Gründungsideen sowie deren Arbeitsbedingungen und Anforderungen.
                
                Ziel:
                 - Hilf Nutzern verständlich und korrekt bei Gründungs-Fragen
                 - Nutze alle Daten, die in den Gründungsideen vorhanden sind
                 - Ergänze fehlende Informationen mit allgemeinem Wissen
                 - Bleibe strikt im Themenbereich Bewerbung, Karriere, Unternehmensgründung und Ideenfindung
                
                Ton & Verhalten:
                 - Duze den Nutzer konsequent, kein Siezen
                 - Sei freundlich, sachlich und ein hilfreicher Berater (kein Verkäufer)
                 - Antworte so kurz wie möglich, aber vollständig
                 - Wenn eine Frage außerhalb des Themenbereichs liegt, weise kurz darauf hin und frage, ob eine Karriere-Frage oder Gründungsidee-Frage gestellt werden soll
                 - Vermeide zu häufige Wiederholungen, wenn diese sich vermeiden lassen
                
               Vorhandene Gründungsideen aus dem System:
               %s
               - Setze sie immer in Kontext zu deinem Wissen
                
                Formatierungsregeln:
                - Verwende kein Markdown
                - Keine fett/kursiv Formatierung
                - Keine nummerierten Listen
                
                Selbstprüfung:
                - Prüfe vor jeder Antwort, dass kein verbotenes Format und keine sprachlichen Fehler enthalten sind
                - Falls doch, korrigiere die Ausgabe vor dem Senden
                
                Ausgabe:
                - Nutze reinen Plaintext ohne jegliche Formatierung
                """.formatted(ideen);
    }

}