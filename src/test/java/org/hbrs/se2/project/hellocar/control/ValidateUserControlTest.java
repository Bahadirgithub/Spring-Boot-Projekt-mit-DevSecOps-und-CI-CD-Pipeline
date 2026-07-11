package org.hbrs.se2.project.hellocar.control;

import com.vaadin.flow.component.datepicker.DatePicker;
import org.hbrs.se2.project.hellocar.repository.BenutzerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ValidateUserControlTest {

    @Mock
    private BenutzerRepository benutzerRepository;

    // validateUserName -----------------------------------------------------
    @Test
    void validateUserNameReturnsFalseWhenUsernameExists() {
        when(benutzerRepository.existsByUsername("exists")).thenReturn(true);
        boolean ok = ValidateUserControl.validateUserName("exists", benutzerRepository);
        assertFalse(ok);
    }

    @Test
    void validateUserNameReturnsTrueWhenUsernameNotExists() {
        when(benutzerRepository.existsByUsername("newuser")).thenReturn(false);
        boolean ok = ValidateUserControl.validateUserName("newuser", benutzerRepository);
        assertTrue(ok);
    }

    // validateUserNameNotEmpty ---------------------------------------------
    @Test
    void validateUserNameNotEmptyReturnsFalseForBlank() {
        assertFalse(ValidateUserControl.validateUserNameNotEmpty("   "));
    }

    @Test
    void validateUserNameNotEmptyReturnsTrueForNonBlank() {
        assertTrue(ValidateUserControl.validateUserNameNotEmpty("alice"));
    }

    // validateEmail -------------------------------------------------------
    @Test
    void validateEmailValidExamplesReturnTrue() {
        assertTrue(ValidateUserControl.validateEmail("user@example.com"));
        assertTrue(ValidateUserControl.validateEmail("firstname.lastname@sub.domain.de"));
    }

    @Test
    void validateEmailInvalidExamplesReturnFalse() {
        assertFalse(ValidateUserControl.validateEmail("no-at-symbol"));
        assertFalse(ValidateUserControl.validateEmail("bad@domain"));
        assertFalse(ValidateUserControl.validateEmail("toolonglocalparttoolonglocalparttoolonglocalparttoolong@d.de"));
    }

    @Test
    void validateEmailNullThrowsNullPointer() {
        assertThrows(NullPointerException.class, () -> ValidateUserControl.validateEmail(null));
    }

    // validatePassword ----------------------------------------------------
    @Test
    void validatePasswordNullOrBlankReturnsFalse() {
        assertFalse(ValidateUserControl.validatePassword(null));
        assertFalse(ValidateUserControl.validatePassword("    "));
    }

    @Test
    void validatePasswordValidatesPattern() {
        // meets: uppercase, lowercase, digit, special char from allowed set, length >=8, no spaces
        assertTrue(ValidateUserControl.validatePassword("Abcdef1!"));
        // fails because no special char
        assertFalse(ValidateUserControl.validatePassword("Abcdef12"));
        // fails because too short
        assertFalse(ValidateUserControl.validatePassword("A1!a"));
    }

    // validDatePicker / isAdult -------------------------------------------
    @Test
    void validDatePickerSetsMaxToNowMinus18Years() {
        DatePicker dp = ValidateUserControl.validDatePicker();
        LocalDate expectedMax = LocalDate.now().minusYears(18);
        assertEquals(expectedMax, dp.getMax());
    }

    @Test
    void isAdultNullOrUnder18ReturnsFalseAndExact18ReturnsTrue() {
        assertFalse(ValidateUserControl.isAdult(null));
        LocalDate under18 = LocalDate.now().minusYears(17);
        assertFalse(ValidateUserControl.isAdult(under18));
        LocalDate exactly18 = LocalDate.now().minusYears(18);
        assertTrue(ValidateUserControl.isAdult(exactly18));
        LocalDate older = LocalDate.now().minusYears(30);
        assertTrue(ValidateUserControl.isAdult(older));
    }

    // validFieldNotEmpty --------------------------------------------------
    @Test
    void validFieldNotEmptyBehaviour() {
        assertFalse(ValidateUserControl.validFieldNotEmpty("   "));
        assertTrue(ValidateUserControl.validFieldNotEmpty("not empty"));
    }
}