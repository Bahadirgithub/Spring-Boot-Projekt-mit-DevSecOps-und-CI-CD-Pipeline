package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;
import org.hbrs.se2.project.hellocar.dtos.GruendungsideeDTO;
import org.hbrs.se2.project.hellocar.entities.Bewerbung;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.BewerbungRepository;
import org.hbrs.se2.project.hellocar.repository.BewertungRepository;
import org.hbrs.se2.project.hellocar.repository.GruenderRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.hbrs.se2.project.hellocar.repository.SkillRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeineGruendungsideenControlTest {

    @Mock
    private BewerbungRepository bewerbungRepository;
    @Mock
    private BewertungRepository bewertungRepository;
    @Mock
    private GruendungsideeRepository gruendungsideeRepository;
    @Mock
    private BenutzerRepository benutzerRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private GruenderRepository gruenderRepository;
    @Mock
    private SkillRepository skillRepository;

    // Echte Instanzen nutzen, um Mockito-Fehler bei der Bytecode-Generierung zu vermeiden
    private ManageIdeaControl ideaControl;
    private BewerbungControl bewerbungControl;
    private VerwaltungControl verwaltungControl;
    private MeineGruendungsideenControl meineGruendungsideenControl;

    private Gruendungsidee testIdee;
    private Bewerbung testBewerbung;

    @BeforeEach
    void setUp() {
        // Controls als echte Objekte instanziieren
        ideaControl = new ManageIdeaControl();
        bewerbungControl = new BewerbungControl(bewerbungRepository, gruendungsideeRepository);
        verwaltungControl = new VerwaltungControl(benutzerRepository, studentRepository, gruenderRepository, skillRepository);

        meineGruendungsideenControl = new MeineGruendungsideenControl(
                ideaControl,
                bewerbungRepository,
                bewertungRepository,
                gruendungsideeRepository,
                verwaltungControl,
                bewerbungControl
        );

        testIdee = new Gruendungsidee();
        testIdee.setIdeeId(100);
        testIdee.setGruenderUserId(42);

        testBewerbung = new Bewerbung();
        testBewerbung.setIdeeId(100);

        // DTO-Felder für den echten Mapper in BewerbungControl setzen
        testBewerbung.setBewerbungId(1);
        testBewerbung.setStudentUserId(10);
        testBewerbung.setStatus("Eingegangen");
    }

    @Test
    void testLadeBewerbungenFuerEigeneIdeeErfolg() {
        int gruenderId = 42;
        int ideeId = 100;

        when(gruendungsideeRepository.findById(ideeId)).thenReturn(Optional.of(testIdee));
        when(bewerbungRepository.findByIdeeId(ideeId)).thenReturn(List.of(testBewerbung));

        List<BewerbungDTO> result = meineGruendungsideenControl.ladeBewerbungenFuerEigeneIdee(gruenderId, ideeId);

        // Assert gegen das echte gemappte DTO
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBewerbungId());
        assertEquals("Eingegangen", result.get(0).getStatus());
    }

    @Test
    void testLadeBewerbungenFuerFremdeIdeeSicherheitsblock() {
        int gruenderId = 42;
        int fremdeIdeeId = 200;

        Gruendungsidee fremdeIdee = new Gruendungsidee();
        fremdeIdee.setIdeeId(fremdeIdeeId);
        fremdeIdee.setGruenderUserId(999); // ID gehört anderem User

        when(gruendungsideeRepository.findById(fremdeIdeeId)).thenReturn(Optional.of(fremdeIdee));

        List<BewerbungDTO> result = meineGruendungsideenControl.ladeBewerbungenFuerEigeneIdee(gruenderId, fremdeIdeeId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIstSkillPassendZurIdeeMatchingLogik() {
        GruendungsideeDTO mockIdeeDTO = mock(GruendungsideeDTO.class);
        when(mockIdeeDTO.getGesuchteSkills()).thenReturn(List.of("Java", "Vaadin"));

        // Normalisierung prüfen (Leerzeichen & Case-Insensitivity)
        assertTrue(meineGruendungsideenControl.istSkillPassendZurIdee("  java  ", mockIdeeDTO));
        assertTrue(meineGruendungsideenControl.istSkillPassendZurIdee("VAADIN", mockIdeeDTO));
        assertFalse(meineGruendungsideenControl.istSkillPassendZurIdee("C#", mockIdeeDTO));
    }
}