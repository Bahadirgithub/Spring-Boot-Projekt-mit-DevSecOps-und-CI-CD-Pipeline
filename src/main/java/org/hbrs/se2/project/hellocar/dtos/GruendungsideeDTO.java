package org.hbrs.se2.project.hellocar.dtos;

public interface GruendungsideeDTO {

    public int getIdeeId();

    public String getBeschreibung();

    public String getPosition();

    public String getAnforderungen();

    public String getArbeitsbedingungen();

    public int getGruenderUserId();

    public String getUnternehmensname();

    public java.util.List<String> getGesuchteSkills();

    // Ermöglicht den Zugriff auf den Ersteller (Gründer) der Idee
    public GruenderDTO getGruender();
}