package com.skillswap.dao;

import com.skillswap.model.Skill;
import com.skillswap.model.SkillCategory;
import com.skillswap.model.SkillType;
import com.skillswap.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the user_skills join table — this is where "Aisha teaches
 * JavaScript" actually becomes a stored fact, linking a user_id and a
 * skill_id with a TEACH or LEARN type.
 */
public class UserSkillDAO {

    /**
     * Links a user to a skill with a given type. INSERT IGNORE tells
     * MySQL "if this would violate the uniq_user_skill_type constraint
     * we defined in the SQL (i.e. this exact link already exists), skip
     * it silently instead of throwing an error" — a database-level
     * safety net that backs up the equals()-based duplicate check we
     * already do in memory on the User class in Phase 3.
     */
    public void linkUserToSkill(int userId, int skillId, SkillType type) throws SQLException {
        String sql = "INSERT IGNORE INTO user_skills (user_id, skill_id, type) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, skillId);
            stmt.setString(3, type.name());

            stmt.executeUpdate();
        }
    }

    public void unlinkUserFromSkill(int userId, int skillId, SkillType type) throws SQLException {
        String sql = "DELETE FROM user_skills WHERE user_id = ? AND skill_id = ? AND type = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, skillId);
            stmt.setString(3, type.name());

            stmt.executeUpdate();
        }
    }

    /**
     * Returns every skill a user teaches, or every skill they want to
     * learn, depending on `type`. Notice the SQL JOIN: we're not just
     * reading user_skills (which only has ids) — we JOIN to the skills
     * table to pull back the actual name and category, so this method
     * can hand back real Skill objects instead of bare numbers.
     */
    public List<Skill> findSkillsForUser(int userId, SkillType type) throws SQLException {
        String sql = "SELECT s.id, s.name, s.category " +
                "FROM skills s " +
                "JOIN user_skills us ON us.skill_id = s.id " +
                "WHERE us.user_id = ? AND us.type = ?";

        List<Skill> skills = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    SkillCategory category = SkillCategory.valueOf(rs.getString("category"));
                    skills.add(new Skill(id, name, category));
                }
            }
        }
        return skills;
    }

    /**
     * The reverse direction of findSkillsForUser: instead of "what skills
     * does this user have?", it answers "which users have THIS skill,
     * with this type?" MatchService uses this to answer "who teaches
     * JavaScript?" when it's checking who could satisfy something I
     * want to learn.
     */
    public List<Integer> findUserIdsBySkillAndType(int skillId, SkillType type) throws SQLException {
        String sql = "SELECT user_id FROM user_skills WHERE skill_id = ? AND type = ?";

        List<Integer> userIds = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, skillId);
            stmt.setString(2, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        }
        return userIds;
    }
}
