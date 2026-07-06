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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Control-Klasse für den Bewerbungsprozess.
 * Verwaltet das Anlegen, Auslesen und Ändern von Bewerbungen.
 */
@Component
public class BewerbungControl {

    private static final Path BEWERBUNG_UPLOAD_ORDNER = Paths.get("uploads", "bewerbungen");

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
     * Legt eine Bewerbung an und speichert die beiden PDF-Dateien im Upload-Ordner.
     */
    @Transactional(rollbackFor = Exception.class)
    public void bewerbenMitDateien(int studentUserId, int ideeId,
                                   String anschreibenOriginalDateiname, InputStream anschreibenInputStream,
                                   String lebenslaufOriginalDateiname, InputStream lebenslaufInputStream)
            throws BewerbungBereitsVorhandenException {

        if (bewerbungRepository.findByStudentUserIdAndIdeeId(studentUserId, ideeId).isPresent()) {
            throw new BewerbungBereitsVorhandenException("Sie haben sich bereits auf diese Idee beworben.");
        }

        pruefePdfDatei(anschreibenOriginalDateiname, anschreibenInputStream, "Anschreiben");
        pruefePdfDatei(lebenslaufOriginalDateiname, lebenslaufInputStream, "Lebenslauf");

        Bewerbung bewerbung = new Bewerbung();
        bewerbung.setStudentUserId(studentUserId);
        bewerbung.setIdeeId(ideeId);
        bewerbung.setBewerbungsdatum(LocalDate.now());
        bewerbung.setStatus("Eingegangen");

        // Erst speichern, damit eine eindeutige bewerbung_id erzeugt wird.
        bewerbung = bewerbungRepository.save(bewerbung);

        try {
            DateiSpeicherErgebnis anschreiben = speicherePdfDatei(
                    bewerbung.getBewerbungId(),
                    "anschreiben",
                    anschreibenOriginalDateiname,
                    anschreibenInputStream
            );

            DateiSpeicherErgebnis lebenslauf = speicherePdfDatei(
                    bewerbung.getBewerbungId(),
                    "lebenslauf",
                    lebenslaufOriginalDateiname,
                    lebenslaufInputStream
            );

            bewerbung.setAnschreibenDateiname(anschreiben.dateiname);
            bewerbung.setAnschreibenDateipfad(anschreiben.dateipfad);
            bewerbung.setLebenslaufDateiname(lebenslauf.dateiname);
            bewerbung.setLebenslaufDateipfad(lebenslauf.dateipfad);

            bewerbungRepository.save(bewerbung);
        } catch (IOException e) {
            throw new RuntimeException("Die PDF-Dateien konnten nicht gespeichert werden.", e);
        }
    }

    /**
     * Prüft, ob eine Datei vorhanden und eine PDF-Datei ist.
     */
    private void pruefePdfDatei(String dateiname, InputStream inputStream, String feldname) {
        if (dateiname == null || dateiname.isBlank() || inputStream == null) {
            throw new IllegalArgumentException("Bitte " + feldname + " als PDF hochladen.");
        }

        if (!dateiname.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(feldname + " muss eine PDF-Datei sein.");
        }
    }

    /**
     * Speichert eine PDF-Datei eindeutig im Ordner uploads/bewerbungen/{bewerbungId}.
     */
    private DateiSpeicherErgebnis speicherePdfDatei(int bewerbungId,
                                                    String dateiTyp,
                                                    String originalDateiname,
                                                    InputStream inputStream) throws IOException {
        Path zielOrdner = BEWERBUNG_UPLOAD_ORDNER.resolve(String.valueOf(bewerbungId));
        Files.createDirectories(zielOrdner);

        String sichtbarerDateiname = bereinigeDateiname(originalDateiname);
        String gespeicherterDateiname = dateiTyp + "_" + UUID.randomUUID() + ".pdf";
        Path zielDatei = zielOrdner.resolve(gespeicherterDateiname);

        Files.copy(inputStream, zielDatei, StandardCopyOption.REPLACE_EXISTING);

        // Relativer Pfad für die Datenbank, unabhängig von Windows/Linux-Schreibweise.
        String relativerPfad = zielDatei.toString().replace("\\", "/");

        return new DateiSpeicherErgebnis(sichtbarerDateiname, relativerPfad);
    }

    /**
     * Entfernt problematische Zeichen aus dem ursprünglichen Dateinamen.
     */
    private String bereinigeDateiname(String dateiname) {
        String bereinigt = dateiname.replace("\\", "_").replace("/", "_");
        bereinigt = bereinigt.replaceAll("[^a-zA-Z0-9._-]", "_");

        if (bereinigt.isBlank()) {
            return "datei.pdf";
        }

        return bereinigt;
    }

    private static class DateiSpeicherErgebnis {
        private final String dateiname;
        private final String dateipfad;

        private DateiSpeicherErgebnis(String dateiname, String dateipfad) {
            this.dateiname = dateiname;
            this.dateipfad = dateipfad;
        }
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
     * Lädt die Bewerbungen für genau eine eigene Gründungsidee eines Gründers.
     * Sicherheitsprüfung: Der Gründer darf nur Bewerbungen für seine eigenen Ideen sehen.
     */
    @Transactional
    public List<BewerbungDTO> ladeBewerbungenFuerEigeneIdee(int gruenderUserId, int ideeId) {

        Gruendungsidee idee = gruendungsideeRepository.findById(ideeId)
                .orElse(null);

        // Falls die Idee nicht existiert oder nicht dem Gründer gehört,
        // geben wir eine leere Liste zurück.
        if (idee == null || idee.getGruenderUserId() != gruenderUserId) {
            return List.of();
        }

        return bewerbungRepository.findByIdeeId(ideeId).stream()
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
