package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.BewerbungBereitsVorhandenException;
import org.hbrs.se2.project.hellocar.control.exception.BewerbungNichtGefundenException;
import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;
import org.hbrs.se2.project.hellocar.entities.Bewerbung;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.repository.BewerbungRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BewerbungControlTest {

    @Mock
    private BewerbungRepository bewerbungRepository;

    @Mock
    private GruendungsideeRepository gruendungsideeRepository;

    @InjectMocks
    private BewerbungControl control;

    private Bewerbung sampleBewerbung;
    private Gruendungsidee sampleIdee;

    @BeforeEach
    void setUp() {
        sampleBewerbung = new Bewerbung();
        sampleBewerbung.setBewerbungId(1);
        sampleBewerbung.setStudentUserId(100);
        sampleBewerbung.setIdeeId(200);
        sampleBewerbung.setBewerbungsdatum(LocalDate.of(2023, 1, 1));
        sampleBewerbung.setStatus("Eingegangen");
        sampleBewerbung.setAnschreibenDateiname("a.pdf");
        sampleBewerbung.setLebenslaufDateiname("cv.pdf");

        sampleIdee = new Gruendungsidee();
        sampleIdee.setIdeeId(200);
        sampleIdee.setGruenderUserId(50);
        sampleIdee.setBeschreibung("Eine tolle Idee");
    }

    @Test
    void testBewerbungSuccess() throws BewerbungBereitsVorhandenException {
        when(bewerbungRepository.findByStudentUserIdAndIdeeId(100, 200)).thenReturn(Optional.empty());

        control.bewerben(100, 200, "a.pdf", "path/a.pdf", "cv.pdf", "path/cv.pdf");

        ArgumentCaptor<Bewerbung> captor = ArgumentCaptor.forClass(Bewerbung.class);
        verify(bewerbungRepository, times(1)).save(captor.capture());
        Bewerbung saved = captor.getValue();

        assertEquals(100, saved.getStudentUserId());
        assertEquals(200, saved.getIdeeId());
        assertEquals("a.pdf", saved.getAnschreibenDateiname());
        assertEquals("cv.pdf", saved.getLebenslaufDateiname());
        assertEquals("Eingegangen", saved.getStatus());
        assertNotNull(saved.getBewerbungsdatum());
    }

    @Test
    void testBewerbungAlreadyExists() {
        when(bewerbungRepository.findByStudentUserIdAndIdeeId(100, 200)).thenReturn(Optional.of(sampleBewerbung));

        assertThrows(BewerbungBereitsVorhandenException.class, () ->
                control.bewerben(100, 200, "a.pdf", "path/a.pdf", "cv.pdf", "path/cv.pdf")
        );

        verify(bewerbungRepository, never()).save(any());
    }

    @Test
    void testLadeEigeneBewerbungen() {
        Bewerbung b1 = new Bewerbung();
        b1.setBewerbungId(2);
        b1.setStudentUserId(100);
        b1.setIdeeId(201);
        b1.setStatus("Eingegangen");

        when(bewerbungRepository.findByStudentUserId(100)).thenReturn(List.of(b1));

        List<?> result = control.ladeEigeneBewerbungen(100);
        assertEquals(1, result.size());
        Object dto = result.get(0);
        assertNotNull(dto);

        assertTrue(dto.toString().toLowerCase().contains("bewerbung") || dto.toString().contains("bewerbungId"));
    }

    @Test
    void testLadeEingegangeneBewerbungen() {
        Gruendungsidee idee = new Gruendungsidee();
        idee.setIdeeId(300);
        idee.setGruenderUserId(400);

        Bewerbung b = new Bewerbung();
        b.setBewerbungId(3);
        b.setIdeeId(300);
        b.setStudentUserId(777);

        when(gruendungsideeRepository.findByGruenderUserId(400)).thenReturn(List.of(idee));
        when(bewerbungRepository.findByIdeeId(300)).thenReturn(List.of(b));

        List<?> result = control.ladeEingegangeneBewerbungen(400);
        assertEquals(1, result.size());
    }

    @Test
    void testLadeIdeeBeschreibungByIdeeIdFound() throws BewerbungNichtGefundenException {
        when(gruendungsideeRepository.findById(200)).thenReturn(Optional.of(sampleIdee));

        String beschreibung = control.ladeIdeeBeschreibungByIdeeId(200);
        assertEquals("Eine tolle Idee", beschreibung);
    }

    @Test
    void testLadeIdeeBeschreibungByIdeeIdNotFound() {
        when(gruendungsideeRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BewerbungNichtGefundenException.class, () -> control.ladeIdeeBeschreibungByIdeeId(999));
    }

    @Test
    void testLadeBewerbungenFuerEigeneIdeeErfolg() {
        when(gruendungsideeRepository.findById(200)).thenReturn(Optional.of(sampleIdee));
        when(bewerbungRepository.findByIdeeId(200)).thenReturn(List.of(sampleBewerbung));

        List<BewerbungDTO> result = control.ladeBewerbungenFuerEigeneIdee(50, 200);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBewerbungId());
        assertEquals("Eingegangen", result.get(0).getStatus());
    }

    @Test
    void testLadeBewerbungenFuerEigeneIdeeFremderGruender() {
        when(gruendungsideeRepository.findById(200)).thenReturn(Optional.of(sampleIdee));

        List<BewerbungDTO> result = control.ladeBewerbungenFuerEigeneIdee(999, 200);

        assertTrue(result.isEmpty());
        verify(bewerbungRepository, never()).findByIdeeId(anyInt());
    }

    @Test
    void testLadeBewerbungenFuerEigeneIdeeIdeeNichtGefunden() {
        when(gruendungsideeRepository.findById(999)).thenReturn(Optional.empty());

        List<BewerbungDTO> result = control.ladeBewerbungenFuerEigeneIdee(50, 999);

        assertTrue(result.isEmpty());
        verify(bewerbungRepository, never()).findByIdeeId(anyInt());
    }

    @Test
    void testBewerbenMitDateienAlreadyExists() {
        when(bewerbungRepository.findByStudentUserIdAndIdeeId(100, 200)).thenReturn(Optional.of(sampleBewerbung));

        assertThrows(BewerbungBereitsVorhandenException.class, () ->
                control.bewerbenMitDateien(100, 200,
                        "anschreiben.pdf", new ByteArrayInputStream(new byte[]{1, 2, 3}),
                        "lebenslauf.pdf", new ByteArrayInputStream(new byte[]{1, 2, 3}))
        );

        verify(bewerbungRepository, never()).save(any());
    }

    @Test
    void testBewerbenMitDateienAnschreibenFehlt() {
        when(bewerbungRepository.findByStudentUserIdAndIdeeId(100, 200)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                control.bewerbenMitDateien(100, 200,
                        null, null,
                        "lebenslauf.pdf", new ByteArrayInputStream(new byte[]{1, 2, 3}))
        );

        assertTrue(ex.getMessage().contains("Anschreiben"));
        verify(bewerbungRepository, never()).save(any());
    }

    @Test
    void testBewerbenMitDateienAnschreibenKeinPdf() {
        when(bewerbungRepository.findByStudentUserIdAndIdeeId(100, 200)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                control.bewerbenMitDateien(100, 200,
                        "anschreiben.docx", new ByteArrayInputStream(new byte[]{1, 2, 3}),
                        "lebenslauf.pdf", new ByteArrayInputStream(new byte[]{1, 2, 3}))
        );

        assertTrue(ex.getMessage().contains("Anschreiben"));
        verify(bewerbungRepository, never()).save(any());
    }

    @Test
    void testBewerbenMitDateienLebenslaufFehlt() {
        when(bewerbungRepository.findByStudentUserIdAndIdeeId(100, 200)).thenReturn(Optional.empty());
        InputStream fehlenderStream = null;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                control.bewerbenMitDateien(100, 200,
                        "anschreiben.pdf", new ByteArrayInputStream(new byte[]{1, 2, 3}),
                        "lebenslauf.pdf", fehlenderStream)
        );

        assertTrue(ex.getMessage().contains("Lebenslauf"));
        verify(bewerbungRepository, never()).save(any());
    }

    @Test
    void testBewerbenMitDateienLebenslaufKeinPdf() {
        when(bewerbungRepository.findByStudentUserIdAndIdeeId(100, 200)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                control.bewerbenMitDateien(100, 200,
                        "anschreiben.pdf", new ByteArrayInputStream(new byte[]{1, 2, 3}),
                        "lebenslauf.txt", new ByteArrayInputStream(new byte[]{1, 2, 3}))
        );

        assertTrue(ex.getMessage().contains("Lebenslauf"));
        verify(bewerbungRepository, never()).save(any());
    }
}