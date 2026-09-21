package com.skillswap.dao;

import com.skillswap.model.User;
import com.skillswap.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * DAO = Data Access Object.
 *
 * This class's ONLY job is talking to the "users" table. Every SQL
 * statement in the whole application should live here (or in another
 * *DAO class), never inside a Servlet or Service. That separation means:
 *   - if we ever change database columns, we only touch this file.
 *   - UserService doesn't need to know any SQL at all — it just calls
 *     methods like registerUser(user) and trusts this class to do it.
 *
 * Every method uses PreparedStatement instead of building a SQL string
 * by concatenating user input. Why that matters:
 *
 *   BAD:  "SELECT * FROM users WHERE email = '" + email + "'"
 *   If someone types this as their "email":   x' OR '1'='1
 *   the final query becomes:
 *   SELECT * FROM users WHERE email = 'x' OR '1'='1'
 *   ...which matches EVERY row. That's a SQL injection attack.
 *
 *   GOOD: "SELECT * FROM users WHERE email = ?"
 *   PreparedStatement sends the query structure and the user's value
 *   separately to MySQL. MySQL always treats "?" as plain data, never
 *   as part of the SQL syntax — so injection like the above becomes
 *   impossible, and typed values (ints, timestamps) are handled correctly.
 */
public class UserDAO {

    /**
     * Inserts a new user row. Expects user.getPassword() to already be
     * HASHED (UserService is responsible for hashing before calling this).
     */
    public void registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";

        // try-with-resources: Connection and PreparedStatement both
        // implement AutoCloseable, so Java closes them automatically
        // when the block ends — even if an exception is thrown. This
        // avoids leaking open database connections.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());

            stmt.executeUpdate();
        }
    }

    /**
     * Looks up a user by email. Returns null if no matching row exists —
     * callers (UserService) decide what that means ("no such account").
     */
    public User findUserByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password, created_at, bio, profile_picture_url " +
                "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Looks up a user by id — useful once we need "who is currently
     * logged in?" beyond what's already cached in the session.
     */
    public User findUserById(int id) throws SQLException {
        String sql = "SELECT id, name, email, password, created_at, bio, profile_picture_url " +
                "FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Updates an existing user's name and email (not password — that
     * gets its own dedicated method later, since changing a password
     * involves re-hashing and usually a "confirm current password" step
     * we don't want to bundle into a generic update).
     */
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Updates the profile-facing fields: name, bio, profile picture URL.
     * Kept separate from updateUser() (which changes name + email)
     * because a profile edit form and an account-settings "change my
     * email" form are different use cases in a real app, even though
     * both end up as an UPDATE on the same table.
     */
    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, bio = ?, profile_picture_url = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getBio());
            stmt.setString(3, user.getProfilePictureUrl());
            stmt.setInt(4, user.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a user by id. The ON DELETE CASCADE we added to the
     * user_skills, skill_requests, and messages foreign keys in Phase 4's
     * SQL means MySQL automatically removes that user's related rows in
     * those tables too — we don't have to manually clean them up here.
     */
    public void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Converts the CURRENT row of a ResultSet into a User object.
    // Pulled into its own method because findUserByEmail and
    // findUserById would otherwise repeat this exact block twice.
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        user.setBio(rs.getString("bio"));
        user.setProfilePictureUrl(rs.getString("profile_picture_url"));

        return user;
    }
}
