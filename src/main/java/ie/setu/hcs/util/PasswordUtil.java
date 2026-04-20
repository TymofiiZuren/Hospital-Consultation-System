package ie.setu.hcs.util;

import ie.setu.hcs.exception.OperationFailedException;
import ie.setu.hcs.exception.ValidationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String rawPassword) throws OperationFailedException, ValidationException {
        if (rawPassword == null) {
            throw new ValidationException("Password cannot be null.");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte value : hashed) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new OperationFailedException("Password hashing is not available.", ex);
        }
    }
}
