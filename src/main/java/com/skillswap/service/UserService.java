package com.skillswap.service;

import com.skillswap.dao.UserDAO;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.User;
import com.skillswap.util.PasswordUtil;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * The SERVICE layer holds business rules — the "is this actually allowed?"
 * logic that sits between a Servlet (which only knows about HTTP) and a
 * DAO (which only knows about SQL). Neither of those should contain
 * rules like "an email can only be registered once" — that logic
 * belongs here.
 *
 * Why bother with a separate layer instead of putting this in the
 * Servlet? Two reasons that matter even in a small project:
 *   1. RegisterServlet and (later) a bulk-import feature could both
 *      call registerUser() without duplicating the validation rules.
 *   2. It's much easier to reason about: a Servlet's job is "translate
 *      HTTP requests to and from method calls", nothing more.
 */
public class UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Validates input, checks for a duplicate email, hashes the password,
     * and saves the new user.
     *
     * Throws SkillSwapException for anything the USER did wrong (bad
     * input, duplicate email) — RegisterServlet catches this and shows
     * the message directly. A raw SQLException means something went
     * wrong on OUR side (e.g. the database is down), so we wrap it in
     * a generic SkillSwapException instead of leaking database details
     * to the browser.
     */
    public User register(String name, String email, String password) throws SkillSwapException {
        validateName(name);
        validateEmail(email);
        validatePassword(password);

        try {
            if (userDAO.findUserByEmail(email) != null) {
                throw new SkillSwapException("An account with this email already exists.");
            }

            String hashedPassword = PasswordUtil.hashPassword(password);
            User newUser = new User(name.trim(), email.trim().toLowerCase(), hashedPassword);

            userDAO.registerUser(newUser);
            return newUser;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SkillSwapException("Something went wrong while creating your account. Please try again.", e);
        }
    }

    /**
     * Looks up the user by email and checks the password. Throws
     * SkillSwapException with a message deliberately generic enough
     * that it never confirms whether the EMAIL specifically was wrong
     * vs. the password — that's a small but real security habit: it
     * stops an attacker from using the login form to discover which
     * emails are registered.
     */
    public User login(String email, String password) throws SkillSwapException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new SkillSwapException("Email and password are required.");
        }

        try {
            User user = userDAO.findUserByEmail(email.trim().toLowerCase());

            if (user == null || !PasswordUtil.verifyPassword(password, user.getPassword())) {
                throw new SkillSwapException("Incorrect email or password.");
            }

            return user;

        } catch (SQLException e) {
            e.printStackTrace(); // TEMPORARY — remove once login works again
            throw new SkillSwapException("Something went wrong while logging you in. Please try again.", e);
        }
    }

    /**
     * Looks up a user by id — used by ProfileApiServlet, which only has
     * the userId stored in the session, not a full User object.
     */
    public User getById(int userId) throws SkillSwapException {
        try {
            User user = userDAO.findUserById(userId);
            if (user == null) {
                throw new SkillSwapException("User not found.");
            }
            return user;

        } catch (SQLException e) {
            throw new SkillSwapException("Could not load your profile. Please try again.", e);
        }
    }

    /**
     * Updates the editable parts of a profile: name, bio, profile
     * picture URL. Deliberately does NOT touch email or password —
     * those have their own rules (uniqueness, hashing) that don't
     * belong mixed into a general "save my profile" action.
     */
    public User updateProfile(int userId, String name, String bio, String profilePictureUrl)
            throws SkillSwapException {

        validateName(name);
        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()
                && !profilePictureUrl.trim().matches("^https?://.+")) {
            throw new SkillSwapException("Profile picture must be a valid http:// or https:// URL.");
        }

        User user = getById(userId);
        user.setName(name.trim());
        user.setBio(bio == null ? null : bio.trim());
        user.setProfilePictureUrl(profilePictureUrl == null || profilePictureUrl.trim().isEmpty()
                ? null : profilePictureUrl.trim());

        try {
            userDAO.updateProfile(user);
            return user;

        } catch (SQLException e) {
            throw new SkillSwapException("Could not save your profile. Please try again.", e);
        }
    }

    // ---- Validation helpers ----

    private void validateName(String name) throws SkillSwapException {
        if (name == null || name.trim().isEmpty()) {
            throw new SkillSwapException("Name is required.");
        }
    }

    private void validateEmail(String email) throws SkillSwapException {
        if (email == null || email.trim().isEmpty()) {
            throw new SkillSwapException("Email is required.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new SkillSwapException("Please enter a valid email address.");
        }
    }

    private void validatePassword(String password) throws SkillSwapException {
        if (password == null || password.length() < 6) {
            throw new SkillSwapException("Password must be at least 6 characters long.");
        }
    }
}
