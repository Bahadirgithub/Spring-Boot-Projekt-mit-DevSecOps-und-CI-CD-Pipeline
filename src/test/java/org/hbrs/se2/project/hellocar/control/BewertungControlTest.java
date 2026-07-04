package org.hbrs.se2.project.hellocar.control;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.BenutzerDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.hbrs.se2.project.hellocar.entities.Bewertung;
import org.hbrs.se2.project.hellocar.control.exception.BewertungNotLoggedInException;
import org.hbrs.se2.project.hellocar.control.exception.BewertungInvalidException;
import org.hbrs.se2.project.hellocar.control.exception.BewertungNurStudentException;
import org.hbrs.se2.project.hellocar.entities.Student;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.hbrs.se2.project.hellocar.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BewertungControlTest {

    @Autowired
    private BewertungControl bewertungControl;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BenutzerRepository benutzerRepository;

    private BenutzerDTO studentUser;
    private BenutzerDTO gruenderUser;

    @BeforeEach
    void setUp() {
        Benutzer b = new Benutzer();
        b.setUserId(100);
        b.setUsername("maxstudent");
        benutzerRepository.save(b);

        Student s = new Student();
        s.setUserId(100); // Muss dieselbe ID wie der Benutzer haben
        s.setUniversitaet("HBRS");
        s.setStudentId("12345");
        s.setSemester(1);
        studentRepository.save(s);

        BenutzerDTOImpl sUser = new BenutzerDTOImpl();
        sUser.setUserId(100);
        this.studentUser = sUser;

        BenutzerDTOImpl gUser = new BenutzerDTOImpl();
        gUser.setUserId(200);
        this.gruenderUser = gUser;
    }

    /**
     * Student kann Bewertung & Kommentar abgeben
     */
    @Test
    void testStudentKannBewertenUndKommentieren() {
        // Arrange - Student muss in DB existieren
        // (In echtem Test über TestDatenFabrik oder Builder mit DB-Save)

        // Act
        Bewertung ergebnis = bewertungControl.bewertungErstellen(
                studentUser,
                5,
                4,
                "Tolle Gründungsidee!"
        );

        // Assert
        assertThat(ergebnis).isNotNull();
        assertThat(ergebnis.getStudentUserId()).isEqualTo(100);
        assertThat(ergebnis.getIdeeId()).isEqualTo(5);
        assertThat(ergebnis.getBewertung()).isEqualTo(4);
        assertThat(ergebnis.getKommentar()).isEqualTo("Tolle Gründungsidee!");
    }

    /**
     * Gründer darf NICHT bewerten
     */
    @Test
    void testGruenderDarfNichtBewerten() {
        // Act & Assert
        assertThatThrownBy(() -> bewertungControl.bewertungErstellen(
                gruenderUser,
                5,
                5,
                "Interessantes Konzept!"
        ))
                .isInstanceOf(BewertungNurStudentException.class) // ✅ Eigene Exception
                .hasMessageContaining("Nur Studenten");

        // Wichtig: Es wurde NICHTS gespeichert!
        // verify(bewertungRepository, never()).save(any(Bewertung.class));
    }

    /**
     * Gäste (nicht eingeloggt) können KEINE Kommentare senden
     */
    @Test
    void testGastKannNichtKommentieren() {
        // Arrange
        BenutzerDTO gastUser = null;

        // Act & Assert
        assertThatThrownBy(() -> bewertungControl.bewertungErstellen(
                gastUser,
                5,
                3,
                "Test Kommentar"
        ))
                .isInstanceOf(BewertungNotLoggedInException.class)
                .hasMessageContaining("eingeloggt");
    }

    /**
     * Bewertung muss im gültigen Bereich sein (1-5)
     */
    @Test
    void testBewertungNurVon1Bis5() {
        assertThatThrownBy(() -> bewertungControl.bewertungErstellen(
                studentUser, 5, 0, "Test"
        ))
                .isInstanceOf(BewertungInvalidException.class);

        assertThatThrownBy(() -> bewertungControl.bewertungErstellen(
                studentUser, 5, 6, "Test"
        ))
                .isInstanceOf(BewertungInvalidException.class);
    }

    /**
     * Kommentar darf nicht leer sein
     */
    @Test
    void testKommentarDarfNichtLeerSein() {
        assertThatThrownBy(() -> bewertungControl.bewertungErstellen(
                studentUser, 5, 4, ""
        ))
                .isInstanceOf(BewertungInvalidException.class);

        assertThatThrownBy(() -> bewertungControl.bewertungErstellen(
                studentUser, 5, 4, null
        ))
                .isInstanceOf(BewertungInvalidException.class);
    }
}