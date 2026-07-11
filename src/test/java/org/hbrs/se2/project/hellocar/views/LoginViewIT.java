package org.hbrs.se2.project.hellocar.views;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeContains;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;

/**
 * Selenium-Integrationstest für die LoginView.
 * Dieser Test simuliert die Interaktion eines Benutzers mit dem Login-Formular.
 */
public class LoginViewIT {

    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * Richtet den WebDriver vor jedem Test ein.
     */
    @BeforeEach
    public void setUp() {
        // Initialisiert den ChromeDriver. Stellt sicher, dass chromedriver.exe im System-PATH ist.
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    /**
     * Testet den Login-Vorgang mit ungültigen Anmeldedaten ("Sad Path").
     * Erwartet, dass nach dem fehlgeschlagenen Versuch eine Fehlermeldung angezeigt wird.
     */
    @Test
    public void testLoginWithInvalidCredentials() {
        // Die URL der Login-Seite aufrufen
        driver.get("http://localhost:8080/login");

        // Explizit warten, bis der Seitentitel korrekt ist
        wait.until(titleIs("Login | StartMeUp"));

        // Ungültige Anmeldedaten eingeben
        driver.findElement(By.cssSelector("vaadin-text-field[name='username'] input")).sendKeys("user");
        driver.findElement(By.cssSelector("vaadin-password-field[name='password'] input")).sendKeys("password");
        driver.findElement(By.xpath("//vaadin-button[contains(@theme, 'primary')]")).click();

        // Warten, bis das 'error'-Attribut im LoginForm erscheint.
        WebElement loginFormWrapper = driver.findElement(By.tagName("vaadin-login-form-wrapper"));
        wait.until(attributeContains(loginFormWrapper, "error", "true"));

        // Überprüfen, ob das 'error'-Attribut tatsächlich gesetzt ist.
        assertNotNull(loginFormWrapper.getAttribute("error"));
    }

    /**
     * Testet den "Happy Path" des Login-Vorgangs mit gültigen Anmeldedaten.
     * Erwartet eine erfolgreiche Weiterleitung zur Hauptseite.
     */
    @Test
    public void testLoginWithValidCredentials() {
        // Die URL der Login-Seite aufrufen
        driver.get("http://localhost:8080/login");

        // Warten, bis die Seite geladen ist
        wait.until(titleIs("Login | StartMeUp"));

        // Gültige Anmeldedaten des Test-Gründers eingeben
        // WICHTIG: Der Benutzer muss in der DB existieren.
        driver.findElement(By.cssSelector("vaadin-text-field[name='username'] input")).sendKeys("G-test");
        driver.findElement(By.cssSelector("vaadin-password-field[name='password'] input")).sendKeys("123.Abc+");
        driver.findElement(By.xpath("//vaadin-button[contains(@theme, 'primary')]")).click();

        // Warten auf die Weiterleitung und Überprüfen des korrekten Seitentitels der Hauptseite
        wait.until(titleIs("Gründungsideen Übersicht"));
    }

    /**
     * Testet die Navigation von der Login-Seite zur Registrierungs-Seite.
     */
    @Test
    public void testNavigationToRegistrationView() {
        // Die URL der Login-Seite aufrufen
        driver.get("http://localhost:8080/login");
        wait.until(titleIs("Login | StartMeUp"));

        // Den Link zur Registrierung finden und klicken
        driver.findElement(By.linkText("Noch kein Konto? Jetzt registrieren")).click();

        // Warten, bis die Registrierungs-Seite geladen ist und ein markantes Element prüfen
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[text()='Registrierung']")));

        // Optional: Auch die URL prüfen
        assertEquals("http://localhost:8080/registration", driver.getCurrentUrl());
    }

    /**
     * Testet die Navigation zum "Passwort vergessen"-Flow.
     */
    @Test
    public void testNavigationToForgotPasswordView() {
        // Die URL der Login-Seite aufrufen
        driver.get("http://localhost:8080/login");
        wait.until(titleIs("Login | StartMeUp"));

        // Den "Passwort vergessen"-Button finden und klicken.
        // Dieser Button ist ein zugewiesener Slot im LoginForm-Web-Component.
        WebElement forgotPasswordButton = driver.findElement(By.xpath("//vaadin-button[@slot='forgot-password']"));
        forgotPasswordButton.click();

        // Warten, bis die Verifizierungs-Seite geladen ist
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[text()='Identität verifizieren']")));

        // URL prüfen
        assertEquals("http://localhost:8080/verify-identity", driver.getCurrentUrl());
    }

    /**
     * Schließt den WebDriver nach jedem Test, um Ressourcen freizugeben.
     */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}