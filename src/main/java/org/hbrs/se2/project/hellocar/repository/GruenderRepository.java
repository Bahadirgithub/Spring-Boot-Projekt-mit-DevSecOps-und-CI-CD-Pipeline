package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Gruender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GruenderRepository extends JpaRepository<Gruender, Integer> {

    Optional<Gruender> findByGruenderId(String gruenderId);

    List<Gruender> findByUnternehmensname(String unternehmensname);

    List<Gruender> findByStandort(String standort);
}