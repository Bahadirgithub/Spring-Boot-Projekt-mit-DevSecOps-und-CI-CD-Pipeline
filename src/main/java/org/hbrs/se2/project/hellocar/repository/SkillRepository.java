package org.hbrs.se2.project.hellocar.repository;

import org.hbrs.se2.project.hellocar.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Integer> {

    List<Skill> findByNameIn(Collection<String> namen);

    Optional<Skill> findByName(String name);

    List<Skill> findAllByOrderByKategorieAscNameAsc();
}