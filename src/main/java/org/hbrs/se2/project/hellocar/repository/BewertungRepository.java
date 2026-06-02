package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Bewertung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BewertungRepository extends JpaRepository<Bewertung, Integer> {

    List<Bewertung> findByStudentUserId(int studentUserId);

    List<Bewertung> findByIdeeId(int ideeId);

    Optional<Bewertung> findByStudentUserIdAndIdeeId(int studentUserId, int ideeId);
}