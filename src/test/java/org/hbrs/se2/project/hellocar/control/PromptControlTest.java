package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptControlTest {

    @Mock
    private GruendungsideeRepository repo;

    @InjectMocks
    private PromptControl control;

    @Test
    void testGetSystemPromptOhneIdeen() {
        when(repo.findAll()).thenReturn(List.of());

        String prompt = control.getSystemPrompt();

        assertNotNull(prompt);
        assertTrue(prompt.contains("Du bist ein Startup-Experte für Studenten."));
        assertTrue(prompt.contains("Es sind keine Gründungsideen in der Datenbank vorhanden"));
        verify(repo, times(1)).findAll();
    }

    @Test
    void testGetSystemPromptMitEinerIdee() {
        Gruendungsidee idee = new Gruendungsidee();
        idee.setIdeeId(1);
        idee.setGruenderUserId(10);
        idee.setPosition("Backend Entwickler");
        idee.setBeschreibung("Eine tolle Idee");
        idee.setAnforderungen("Java Kenntnisse");
        idee.setArbeitsbedingungen("Remote");
        idee.setGesuchteSkills(List.of("Java", "Spring"));

        when(repo.findAll()).thenReturn(List.of(idee));

        String prompt = control.getSystemPrompt();

        assertTrue(prompt.contains("Idee-ID: 1"));
        assertTrue(prompt.contains("Gründer-User-ID: 10"));
        assertTrue(prompt.contains("Position: Backend Entwickler"));
        assertTrue(prompt.contains("Beschreibung: Eine tolle Idee"));
        assertTrue(prompt.contains("Anforderungen: Java Kenntnisse"));
        assertTrue(prompt.contains("Arbeitsbedingungen: Remote"));
        assertTrue(prompt.contains("Gesuchte Skills: [Java, Spring]"));
        assertFalse(prompt.contains("Es sind keine Gründungsideen"));
    }

    @Test
    void testGetSystemPromptMitMehrerenIdeenNutztTrennzeichen() {
        Gruendungsidee idee1 = new Gruendungsidee();
        idee1.setIdeeId(1);
        idee1.setGruenderUserId(10);

        Gruendungsidee idee2 = new Gruendungsidee();
        idee2.setIdeeId(2);
        idee2.setGruenderUserId(20);

        when(repo.findAll()).thenReturn(List.of(idee1, idee2));

        String prompt = control.getSystemPrompt();

        assertTrue(prompt.contains("Idee-ID: 1"));
        assertTrue(prompt.contains("Idee-ID: 2"));
        assertTrue(prompt.contains("\n---\n"));
    }
}
