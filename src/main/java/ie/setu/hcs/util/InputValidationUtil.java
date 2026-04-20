package ie.setu.hcs.util;

import ie.setu.hcs.exception.ValidationException;

import java.time.LocalDate;
import java.util.regex.Pattern;

public final class InputValidationUtil {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private InputValidationUtil() {
    }

    public static String requireNonBlank(String value, String fieldName) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value.trim();
    }

    public static String requireEmail(String email) throws ValidationException {
        String normalized = requireNonBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException("Please enter a valid email address.");
        }
        return normalized;
    }

    public static Integer requirePositiveInteger(Integer value, String fieldName) throws ValidationException {
        if (value == null || value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
        return value;
    }

    public static Integer requireNonNegativeInteger(Integer value, String fieldName) throws ValidationException {
        if (value == null || value < 0) {
            throw new ValidationException(fieldName + " must be zero or greater.");
        }
        return value;
    }

    public static Float requirePositiveAmount(Float value, String fieldName) throws ValidationException {
        if (value == null || value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
        return value;
    }

    public static LocalDate requireDate(LocalDate value, String fieldName) throws ValidationException {
        if (value == null) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value;
    }

    public static String optionalTrim(String value) {
        return value == null ? null : value.trim();
    }
}
