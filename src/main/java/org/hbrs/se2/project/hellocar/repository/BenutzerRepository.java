package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
/**
 * JPA-Repository für die Abfrage von registrierten User. Die Bezeichnung einer Methode
 * bestimmt dabei die Selektionsbedingung (den WHERE-Teil). Der Rückgabewert einer
 * Methode bestimmt den Projectionsbedingung (den SELECT-Teil).
 * Mehr Information über die Entwicklung von Queries in JPA:
 * https://www.baeldung.com/spring-data-jpa-projections
 * https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods
 *
 */
public interface BenutzerRepository extends JpaRepository<Benutzer, Integer> {

    Optional<Benutzer> findByUsernameAndPasswort(String username, String passwort);

    Optional<Benutzer> findByUsername(String username);

    List<Benutzer> findByNachname(String nachname);

    boolean existsByUsername(String username);
}

