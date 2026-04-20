package ie.setu.hcs.util;

import ie.setu.hcs.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashReturnsDeterministicSha256Value() throws Exception {
        String first = PasswordUtil.hash("secret123");
        String second = PasswordUtil.hash("secret123");

        assertEquals(first, second);
        assertEquals(64, first.length());
        assertTrue(first.matches("[0-9a-f]{64}"));
    }

    @Test
    void hashThrowsValidationExceptionWhenPasswordIsNull() {
        ValidationException ex = assertThrows(ValidationException.class, () -> PasswordUtil.hash(null));
        assertEquals("Password cannot be null.", ex.getMessage());
    }
}
