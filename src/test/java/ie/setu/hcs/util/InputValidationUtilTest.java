package ie.setu.hcs.util;

import ie.setu.hcs.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputValidationUtilTest {

    @Test
    void requireEmailTrimsAndAcceptsValidValue() throws Exception {
        assertEquals("test@icloud.com", InputValidationUtil.requireEmail("  test@icloud.com  "));
    }

    @Test
    void requireEmailRejectsInvalidValue() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> InputValidationUtil.requireEmail("not-an-email")
        );

        assertEquals("Please enter a valid email address.", ex.getMessage());
    }

    @Test
    void integerHelpersRespectPositiveAndNonNegativeRules() throws Exception {
        assertEquals(2, InputValidationUtil.requirePositiveInteger(2, "Years of experience"));
        assertEquals(0, InputValidationUtil.requireNonNegativeInteger(0, "Consultation fee"));

        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> InputValidationUtil.requireNonNegativeInteger(-1, "Consultation fee")
        );
        assertEquals("Consultation fee must be zero or greater.", ex.getMessage());
    }

    @Test
    void requireDateRejectsNullValues() throws Exception {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> InputValidationUtil.requireDate(null, "Date of birth")
        );
        assertEquals("Date of birth is required.", ex.getMessage());

        assertEquals(LocalDate.of(2020, 1, 1),
                InputValidationUtil.requireDate(LocalDate.of(2020, 1, 1), "Date of birth"));
    }
}
