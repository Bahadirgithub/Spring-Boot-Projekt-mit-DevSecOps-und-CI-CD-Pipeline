package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Bewerbung;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BewerbungRepository extends JpaRepository<Bewerbung, Integer> {

    List<Bewerbung> findByStudentUserId(int studentUserId);

    List<Bewerbung> findByIdeeId(int ideeId);

    Optional<Bewerbung> findByStudentUserIdAndIdeeId(int studentUserId, int ideeId);
}