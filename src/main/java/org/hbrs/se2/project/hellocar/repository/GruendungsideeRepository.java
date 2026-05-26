package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GruendungsideeRepository extends JpaRepository<Gruendungsidee, Integer> {

    List<Gruendungsidee> findByGruenderUserId(int gruenderUserId);

    List<Gruendungsidee> findByPosition(String position);

    boolean existsByGruenderUserId(int gruenderUserId);  // ← das hinzufügen
}