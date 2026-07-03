package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.control.exception.BewerbungBereitsVorhandenException;
import org.hbrs.se2.project.hellocar.control.exception.BewerbungNichtGefundenException;
import org.hbrs.se2.project.hellocar.dtos.BewerbungDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BewerbungDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Bewerbung;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.hbrs.se2.project.hellocar.repository.BewerbungRepository;
import org.hbrs.se2.project.hellocar.repository.GruendungsideeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Control-Klasse für den Bewerbungsprozess.
 * Verwaltet das Anlegen, Auslesen und Ändern von Bewerbungen.
 */
@Component
public class BewerbungControl {

    private final BewerbungRepository bewerbungRepository;
    private final GruendungsideeRepository gruendungsideeRepository;

    public BewerbungControl(BewerbungRepository bewerbungRepository,
                            GruendungsideeRepository gruendungsideeRepository) {
        this.bewerbungRepository = bewerbungRepository;
        this.gruendungsideeRepository = gruendungsideeRepository;
    }

    private BewerbungDTO mapToDTO(Bewerbung entity) {
        BewerbungDTOImpl dto = new BewerbungDTOImpl();
        dto.setBewerbungId(entity.getBewerbungId());
        dto.setStudentUserId(entity.getStudentUserId());
        dto.setIdeeId(entity.getIdeeId());
        dto.setBewerbungsdatum(entity.getBewerbungsdatum());
        dto.setStatus(entity.getStatus());
        dto.setAnschreibenDateiname(entity.getAnschreibenDateiname());
        dto.setAnschreibenDateipfad(entity.getAnschreibenDateipfad());
        dto.setLebenslaufDateiname(entity.getLebenslaufDateiname());
        dto.setLebenslaufDateipfad(entity.getLebenslaufDateipfad());
        return dto;
    }

    /**
     * Legt eine neue Bewerbung an. Ein Student kann sich pro Idee nur einmal bewerben.
     */
    @Transactional
    public void bewerben(int studentUserId, int ideeId,
                         String anschreibenDateiname, String anschreibenDateipfad,
                         String lebenslaufDateiname, String lebenslaufDateipfad)
            throws BewerbungBereitsVorhandenException {

        if (bewerbungRepository.findByStudentUserIdAndIdeeId(studentUserId, ideeId).isPresent()) {
            throw new BewerbungBereitsVorhandenException("Sie haben sich bereits auf diese Idee beworben.");
        }

        Bewerbung bewerbung = new Bewerbung();
        bewerbung.setStudentUserId(studentUserId);
        bewerbung.setIdeeId(ideeId);
        bewerbung.setBewerbungsdatum(LocalDate.now());
        bewerbung.setStatus("Eingegangen");
        bewerbung.setAnschreibenDateiname(anschreibenDateiname);
        bewerbung.setAnschreibenDateipfad(anschreibenDateipfad);
        bewerbung.setLebenslaufDateiname(lebenslaufDateiname);
        bewerbung.setLebenslaufDateipfad(lebenslaufDateipfad);

        bewerbungRepository.save(bewerbung);
    }

    /**
     * Lädt alle Bewerbungen, die ein bestimmter Student selbst abgegeben hat.
     */
    @Transactional
    public List<BewerbungDTO> ladeEigeneBewerbungen(int studentUserId) {
        return bewerbungRepository.findByStudentUserId(studentUserId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Lädt alle Bewerbungen, die für die Gründungsideen eines bestimmten Gründers eingegangen sind.
     */
    @Transactional
    public List<BewerbungDTO> ladeEingegangeneBewerbungen(int gruenderUserId) {
        List<Gruendungsidee> eigeneIdeen = gruendungsideeRepository.findByGruenderUserId(gruenderUserId);

        return eigeneIdeen.stream()
                .flatMap(idee -> bewerbungRepository.findByIdeeId(idee.getIdeeId()).stream())
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Liefert den Firmennamen/Titel der Idee zu einer Bewerbung, für die Anzeige in der Studenten-Übersicht.
     */
    @Transactional
    public String ladeIdeeBeschreibungByIdeeId(int ideeId) throws BewerbungNichtGefundenException {
        return gruendungsideeRepository.findById(ideeId)
                .map(Gruendungsidee::getBeschreibung)
                .orElseThrow(() -> new BewerbungNichtGefundenException("Zugehörige Gründungsidee nicht gefunden."));
    }
}