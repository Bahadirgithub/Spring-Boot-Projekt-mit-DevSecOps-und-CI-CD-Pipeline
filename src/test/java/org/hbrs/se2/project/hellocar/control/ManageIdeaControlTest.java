package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.GruendungsideeDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManageIdeaControlTest {

    @Mock
    private GruendungsideeRepository repository;

    @Mock
    private GruenderRepository gruenderRepository;

    @InjectMocks
    private ManageIdeaControl control;

    private Gruendungsidee idea;
    private Gruender gruender;

    @BeforeEach
    void setUp() {
        idea = new Gruendungsidee();
        idea.setIdeeId(1);
        idea.setGruenderUserId(10);
        idea.setBeschreibung("Test Idee");

        gruender = new Gruender();
        gruender.setUnternehmensname("Test GmbH");
    }

    // -------------------------
    // TEST 1: readAllIdeas()
    // -------------------------
    @Test
    void testReadAllIdeas() {
        when(repository.findAll()).thenReturn(List.of(idea));
        when(gruenderRepository.findById(10)).thenReturn(Optional.of(gruender));

        List<GruendungsideeDTO> result = control.readAllIdeas();

        assertEquals(1, result.size());
        assertEquals("Test Idee", result.get(0).getBeschreibung());
        assertEquals("Test GmbH", result.get(0).getUnternehmensname());
    }

    // -------------------------
    // TEST 2: createIdea()
    // -------------------------
    @Test
    void testCreateIdea() {
        GruendungsideeDTOImpl dto = new GruendungsideeDTOImpl();
        dto.setGruenderUserId(10);
        dto.setBeschreibung("New Idea");

        control.createIdea(dto);

        verify(repository, times(1)).save(any(Gruendungsidee.class));
    }

    /**
     * Prüft, dass eine Gründungsidee ohne Beschreibung nicht gespeichert wird.
     */
    @Test
    void gruendungsideeOhneBeschreibungWirdNichtGespeichert() {
        GruendungsideeDTOImpl dto = new GruendungsideeDTOImpl();
        dto.setGruenderUserId(10);
        dto.setBeschreibung("   ");

        assertThrows(IllegalArgumentException.class, () -> control.createIdea(dto));
        verify(repository, never()).save(any(Gruendungsidee.class));
    }

    // -------------------------
    // TEST 3: getUnternehmensnameByUserId()
    // -------------------------
    @Test
    void testGetUnternehmensnameByUserIdFound() {
        when(gruenderRepository.findById(10)).thenReturn(Optional.of(gruender));

        String result = control.getUnternehmensnameByUserId(10);

        assertEquals("Test GmbH", result);
    }

    @Test
    void testGetUnternehmensnameByUserIdNotFound() {
        when(gruenderRepository.findById(99)).thenReturn(Optional.empty());

        String result = control.getUnternehmensnameByUserId(99);

        assertEquals("Kein Unternehmen hinterlegt", result);
    }
}
