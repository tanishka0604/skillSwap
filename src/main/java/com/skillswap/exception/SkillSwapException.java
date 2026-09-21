package com.skillswap.exception;

/**
 * A custom exception for SkillSwap's own business rules —
 * things like "email already registered" or "wrong password".
 *
 * Why create our own exception class instead of just using
 * RuntimeException or SQLException everywhere?
 *   - It lets Servlets catch ONE specific type and know for sure
 *     it's a "safe to show the user" message, not a raw database error.
 *   - It's a normal part of Java: you extend Exception (or RuntimeException)
 *     to describe a problem that is specific to your own application.
 *
 * This is a CHECKED exception (it extends Exception, not RuntimeException),
 * which means any method that can throw it must either handle it with
 * try/catch or declare "throws SkillSwapException" — the compiler enforces
 * that callers don't forget about it.
 */
public class SkillSwapException extends Exception {

    public SkillSwapException(String message) {
        super(message);
    }

    public SkillSwapException(String message, Throwable cause) {
        super(message, cause);
    }
}
