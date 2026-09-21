package com.skillswap.service;

import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.UserSkillDAO;
import com.skillswap.exception.SkillSwapException;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillCategory;
import com.skillswap.model.SkillType;

import java.sql.SQLException;
import java.util.List;

/**
 * Business rules for skill management, sitting between the Servlets
 * and the two DAOs (SkillDAO, UserSkillDAO) from Phase 4 — same
 * layering as UserService from Phase 2.
 */
public class SkillService {

    private final SkillDAO skillDAO;
    private final UserSkillDAO userSkillDAO;

    public SkillService() {
        this.skillDAO = new SkillDAO();
        this.userSkillDAO = new UserSkillDAO();
    }

    /**
     * Adds a skill to a user's TEACH or LEARN list. rawName and
     * rawCategory come straight from the HTML form as Strings — this
     * method is where they get turned into real Java types (a trimmed
     * name, a SkillCategory, a SkillType) and validated.
     */
    public Skill addSkill(int userId, String rawName, String rawCategory, String rawType)
            throws SkillSwapException {

        String name = validateName(rawName);
        SkillCategory category = parseCategory(rawCategory);
        SkillType type = parseType(rawType);

        try {
            // findOrCreate means: if someone already added "JavaScript" /
            // PROGRAMMING, we reuse that same skills row instead of making
            // a second one — this is what lets two different users end up
            // pointing at the identical Skill, which Phase 9 (matching)
            // depends on.
            Skill skill = skillDAO.findOrCreate(name, category);
            userSkillDAO.linkUserToSkill(userId, skill.getId(), type);
            return skill;

        } catch (SQLException e) {
            throw new SkillSwapException("Something went wrong while saving that skill. Please try again.", e);
        }
    }

    public void removeSkill(int userId, int skillId, String rawType) throws SkillSwapException {
        SkillType type = parseType(rawType);

        try {
            userSkillDAO.unlinkUserFromSkill(userId, skillId, type);
        } catch (SQLException e) {
            throw new SkillSwapException("Something went wrong while removing that skill. Please try again.", e);
        }
    }

    public List<Skill> getSkillsToTeach(int userId) throws SkillSwapException {
        try {
            return userSkillDAO.findSkillsForUser(userId, SkillType.TEACH);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load skills right now. Please try again.", e);
        }
    }

    public List<Skill> getSkillsToLearn(int userId) throws SkillSwapException {
        try {
            return userSkillDAO.findSkillsForUser(userId, SkillType.LEARN);
        } catch (SQLException e) {
            throw new SkillSwapException("Could not load skills right now. Please try again.", e);
        }
    }

    // ---- Validation helpers ----

    private String validateName(String rawName) throws SkillSwapException {
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new SkillSwapException("Skill name is required.");
        }
        return rawName.trim();
    }

    private SkillCategory parseCategory(String rawCategory) throws SkillSwapException {
        try {
            return SkillCategory.valueOf(rawCategory.trim().toUpperCase());
        } catch (Exception e) {
            // Catches both "rawCategory was null" (NullPointerException)
            // and "not one of the 8 known names" (IllegalArgumentException,
            // which is what valueOf() throws for an unrecognized value).
            throw new SkillSwapException("Please choose a valid skill category.");
        }
    }

    private SkillType parseType(String rawType) throws SkillSwapException {
        try {
            return SkillType.valueOf(rawType.trim().toUpperCase());
        } catch (Exception e) {
            throw new SkillSwapException("Please specify whether this is a skill to teach or learn.");
        }
    }
}
