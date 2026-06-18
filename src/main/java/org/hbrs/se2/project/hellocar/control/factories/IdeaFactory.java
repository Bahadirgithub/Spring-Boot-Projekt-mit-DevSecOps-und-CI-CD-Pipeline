package org.hbrs.se2.project.hellocar.control.factories;

import org.hbrs.se2.project.hellocar.dtos.BenutzerDTO;
import org.hbrs.se2.project.hellocar.dtos.impl.GruendungsideeDTOImpl;
import org.hbrs.se2.project.hellocar.entities.Gruendungsidee;

public class IdeaFactory {
    /**
     * Erzeugt eine Gruendungsidee-Entity basierend auf den UI-Daten des DTOs.
     * Die ideeId wird automatisch von der Datenbank vergeben.
     */
    public static Gruendungsidee createGruendungsidee(GruendungsideeDTOImpl dto) {
        // 1. Eine neue leere Entity für die Datenbank erstellen
        Gruendungsidee entity = new Gruendungsidee();

        // 2. Die Werte aus dem DTO in die Entity kopieren (Mapping)
        entity.setGruenderUserId(dto.getGruenderUserId());
        entity.setBeschreibung(dto.getBeschreibung());
        entity.setPosition(dto.getPosition());
        entity.setAnforderungen(dto.getAnforderungen());
        entity.setArbeitsbedingungen(dto.getArbeitsbedingungen());

        // 3. Das fertige Objekt für das Control zurückgeben
        return entity;
    }
}
