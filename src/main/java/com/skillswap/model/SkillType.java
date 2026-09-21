package com.skillswap.model;

/**
 * Same reasoning as SkillCategory in Phase 3: the "type" column in
 * user_skills can only ever be one of two things. Using an enum instead
 * of a raw String means the Java compiler — not a runtime bug — catches
 * a typo like "Teach" (wrong case) before the code ever runs.
 *
 * Notice this maps directly onto the ENUM('TEACH', 'LEARN') column we
 * just created in MySQL. UserSkillDAO is the class that translates
 * between this Java enum and that database column.
 */
public enum SkillType {
    TEACH,
    LEARN
}
