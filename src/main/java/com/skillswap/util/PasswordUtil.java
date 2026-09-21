package com.skillswap.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Turns a plain password into something safe to store, and checks a plain
 * password against that stored value at login time.
 *
 * PLAIN PASSWORD vs HASHED PASSWORD
 * ----------------------------------
 * A plain password is exactly what the user typed: "mySecret123".
 * If we stored that directly in MySQL and the database ever leaked,
 * every user's real password would be exposed immediately.
 *
 * A hash is the OUTPUT of a one-way math function: you can turn a
 * password INTO a hash easily, but you cannot turn a hash back INTO
 * the original password. So instead of storing "mySecret123", we store
 * something like "K7x9Qm...==" (a long unreadable string). At login,
 * we hash whatever the user just typed and compare the two hashes —
 * we never need to know or store the real password at all.
 *
 * We also add a "salt": a random value generated per user, mixed into
 * the hash. Without a salt, two users with the same password would end
 * up with the identical stored hash, and an attacker with a precomputed
 * table of common password hashes could crack many accounts at once.
 * With a random salt, the same password hashes differently every time.
 *
 * We use PBKDF2WithHmacSHA256 here because it ships inside the standard
 * Java library (javax.crypto) — no extra jar needed, which matches the
 * "beginner-friendly, minimal dependencies" goal for this phase. It also
 * deliberately repeats its internal hashing thousands of times, which
 * makes brute-force guessing slow on purpose.
 */
public class PasswordUtil {

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16; // bytes

    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password. Returns a single string in the format
     * "saltBase64:hashBase64" so both pieces can be stored in one column.
     */
    public static String hashPassword(String plainPassword) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] hash = pbkdf2(plainPassword.toCharArray(), salt);

            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return saltBase64 + ":" + hashBase64;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Could not hash password", e);
        }
    }

    /**
     * Checks a plain-text password (typed at login) against a stored
     * "saltBase64:hashBase64" value (read from the users table).
     */
    public static boolean verifyPassword(String plainPassword, String storedValue) {
        try {
            String[] parts = storedValue.split(":");
            if (parts.length != 2) {
                return false; // stored value is not in the expected format
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt);

            // A constant-time-ish comparison is ideal for password checks;
            // MessageDigest.isEqual guards against timing side-channels.
            return java.security.MessageDigest.isEqual(expectedHash, actualHash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Could not verify password", e);
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}
