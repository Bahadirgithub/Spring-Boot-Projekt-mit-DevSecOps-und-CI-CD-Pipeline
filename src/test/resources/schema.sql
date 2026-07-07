-- Test-Schema-Initialisierung für H2 In-Memory DB
-- Diese Datei wird von Spring Boot automatisch beim Starten der Tests ausgeführt.

-- Schema erstellen (falls nicht vorhanden)
CREATE SCHEMA IF NOT EXISTS ahsogehts;

-- Tabelle: benutzer
CREATE TABLE IF NOT EXISTS ahsogehts.benutzer (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    vorname VARCHAR(255),
    nachname VARCHAR(255),
    username VARCHAR(255) UNIQUE,
    passwort VARCHAR(255),
    email VARCHAR(255),
    telefonnummer VARCHAR(255),
    geburtstag DATE
);

-- Tabelle: student
CREATE TABLE IF NOT EXISTS ahsogehts.student (
    user_id INT PRIMARY KEY,
    student_id VARCHAR(255) UNIQUE,
    universitaet VARCHAR(255),
    semester INT,
    abschluss VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES ahsogehts.benutzer(user_id) ON DELETE CASCADE
);

-- Tabelle: gruender
CREATE TABLE IF NOT EXISTS ahsogehts.gruender (
    user_id INT PRIMARY KEY,
    gruender_id VARCHAR(255) UNIQUE,
    unternehmensname VARCHAR(255),
    standort VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES ahsogehts.benutzer(user_id) ON DELETE CASCADE
);

-- Tabelle: gruendungsidee
CREATE TABLE IF NOT EXISTS ahsogehts.gruendungsidee (
    idee_id INT PRIMARY KEY AUTO_INCREMENT,
    gruender_user_id INT NOT NULL,
    beschreibung VARCHAR(2000),
    position VARCHAR(255),
    anforderungen VARCHAR(1000),
    arbeitsbedingungen VARCHAR(1000),
    FOREIGN KEY (gruender_user_id) REFERENCES ahsogehts.gruender(user_id) ON DELETE CASCADE
);

-- Tabelle: bewerbung
-- Hinweis: Der Status wird als VARCHAR gespeichert (H2-kompatibel, ohne PostgreSQL-Cast)
CREATE TABLE IF NOT EXISTS ahsogehts.bewerbung (
    bewerbung_id INT PRIMARY KEY AUTO_INCREMENT,
    student_user_id INT NOT NULL,
    idee_id INT NOT NULL,
    bewerbungsdatum DATE,
    status VARCHAR(255),
    anschreiben_dateiname VARCHAR(255),
    anschreiben_dateipfad VARCHAR(1000),
    lebenslauf_dateiname VARCHAR(255),
    lebenslauf_dateipfad VARCHAR(1000),
    FOREIGN KEY (student_user_id) REFERENCES ahsogehts.student(user_id) ON DELETE CASCADE,
    FOREIGN KEY (idee_id) REFERENCES ahsogehts.gruendungsidee(idee_id) ON DELETE CASCADE
);

-- Tabelle: skill
CREATE TABLE IF NOT EXISTS ahsogehts.skill (
    skill_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255),
    kategorie VARCHAR(255)
);

-- Tabelle: student_skills (Join-Tabelle)
CREATE TABLE IF NOT EXISTS ahsogehts.student_skills (
    student_user_id INT NOT NULL,
    skill_id INT NOT NULL,
    PRIMARY KEY (student_user_id, skill_id),
    FOREIGN KEY (student_user_id) REFERENCES ahsogehts.student(user_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES ahsogehts.skill(skill_id) ON DELETE CASCADE
);

-- Tabelle: gruendungsidee_skills (Join-Tabelle)
CREATE TABLE IF NOT EXISTS ahsogehts.gruendungsidee_skills (
    idee_id INT NOT NULL,
    skill_id INT NOT NULL,
    PRIMARY KEY (idee_id, skill_id),
    FOREIGN KEY (idee_id) REFERENCES ahsogehts.gruendungsidee(idee_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES ahsogehts.skill(skill_id) ON DELETE CASCADE
);

