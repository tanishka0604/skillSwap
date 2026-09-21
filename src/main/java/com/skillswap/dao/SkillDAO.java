package com.skillswap.dao;

import com.skillswap.model.Skill;
import com.skillswap.model.SkillCategory;
import com.skillswap.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the "skills" table — the platform-wide list of skills that
 * exist (e.g. one row for "JavaScript", one for "Photoshop"), separate
 * from WHO teaches or wants them (that link lives in user_skills,
 * handled by UserSkillDAO).
 */
public class SkillDAO {

    /**
     * Looks up a skill by exact name + category. We check this before
     * inserting so two users who both add "JavaScript" end up sharing
     * ONE row in "skills", not two duplicate ones — that's what makes
     * user_skills useful as a matching table later on.
     */
    public Skill findByNameAndCategory(String name, SkillCategory category) throws SQLException {
        String sql = "SELECT id, name, category FROM skills WHERE name = ? AND category = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, category.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Inserts a new skill row and returns it with its generated id filled
     * in. Uses RETURN_GENERATED_KEYS — this is the standard JDBC way to
     * get back the AUTO_INCREMENT value MySQL assigned, since the INSERT
     * statement itself doesn't return it.
     */
    public Skill insert(Skill skill) throws SQLException {
        String sql = "INSERT INTO skills (name, category) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, skill.getName());
            stmt.setString(2, skill.getCategory().name());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    skill.setId(keys.getInt(1));
                }
            }
        }
        return skill;
    }

    /**
     * The pattern "find it, or create it if it doesn't exist yet" comes
     * up constantly with reference data like skills. Wrapping it in one
     * method here means UserSkillDAO (Phase 4 continued) never has to
     * think about whether a skill row already exists — it just calls
     * this and gets back a Skill with a real id either way.
     */
    public Skill findOrCreate(String name, SkillCategory category) throws SQLException {
        Skill existing = findByNameAndCategory(name, category);
        if (existing != null) {
            return existing;
        }
        return insert(new Skill(name, category));
    }

    public List<Skill> findAll() throws SQLException {
        String sql = "SELECT id, name, category FROM skills ORDER BY category, name";
        List<Skill> skills = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                skills.add(mapRow(rs));
            }
        }
        return skills;
    }

    private Skill mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        SkillCategory category = SkillCategory.valueOf(rs.getString("category"));
        return new Skill(id, name, category);
    }
}
